package dev.gideonwhite1029.horizon.region;

import ca.spottedleaf.moonrise.patches.chunk_system.io.MoonriseRegionFileIO;
import com.github.luben.zstd.Zstd;
import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// buffered_region_file_implementation_version_0_1byHorizon
public class BufferedRegionFile implements IRegionFile, IFlushableRegionFile {
    private static final double AUTO_COMPACT_PERCENT = 3.0 / 5.0; // compact when waste > 60% of used data
    private static final long AUTO_COMPACT_SIZE = 1024 * 1024; // minimum 1 MiB waste to trigger compact
    private static final long SUPER_BLOCK = 0x1145141919810L;
    private static final int HASH_SEED = 0x0721;
    private static final byte VERSION = 0x01;

    private final Path filePath;
    private final ReentrantReadWriteLock fileAccessLock = new ReentrantReadWriteLock();
    private final XXHash32 xxHash32 = XXHashFactory.fastestInstance().hash32();
    private final Sector[] sectors = new Sector[1024];
    private final AtomicInteger recalculateCount = new AtomicInteger(0);

    // IFlushableRegionFile state
    private final AtomicBoolean markedToSave = new AtomicBoolean(false);
    private final AtomicBoolean flushing = new AtomicBoolean(false);
    private volatile long lastWritten = 0L;
    private volatile boolean closed = false;

    private long currentAcquiredIndex;
    private byte compressionLevel;
    private int xxHash32Seed = HASH_SEED;
    private FileChannel channel;

    public BufferedRegionFile(Path filePath, int compressionLevel) throws IOException {
        this.filePath = filePath;
        this.compressionLevel = (byte) compressionLevel;
        this.currentAcquiredIndex = headerSize();

        for (int i = 0; i < 1024; i++) {
            this.sectors[i] = new Sector(i, headerSize(), 0, false);
        }

        this.channel = FileChannel.open(
            filePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.READ
        );

        this.readHeaders();
        LinearRegionFileFlusher.INSTANCE.addFile(this);
    }

    @Override
    public boolean isMarkedToSave() {
        return this.markedToSave.get();
    }

    @Override
    public long getLastWritten() {
        return this.lastWritten;
    }

    @Override
    public boolean isClosedVolatile() {
        return this.closed;
    }

    @Override
    public boolean tryMarkFlushing() {
        return this.flushing.compareAndSet(false, true);
    }

    @Override
    public Path getPath() {
        return this.filePath;
    }

    @Override
    public void syncIfNeeded() throws IOException {
        try {
            flush();
        } finally {
            this.flushing.set(false);
        }
    }

    private int headerSize() {
        return Long.BYTES      // SUPER_BLOCK
            + Byte.BYTES       // VERSION
            + Byte.BYTES       // compressionLevel
            + Integer.BYTES    // xxHash32Seed
            + Long.BYTES       // currentAcquiredIndex
            + 1024 * Sector.ENCODED_SIZE; // sector table
    }

    private void readHeaders() throws IOException {
        if (this.channel.size() < this.headerSize()) {
            return;
        }

        final ByteBuffer buf = ByteBuffer.allocateDirect(this.headerSize());
        this.channel.read(buf, 0);
        buf.flip();

        long magic = buf.getLong();
        byte version = buf.get();
        if (magic != SUPER_BLOCK || version != VERSION) {
            throw new IOException("Invalid buffered region file header in " + this.filePath);
        }

        this.compressionLevel = buf.get();
        this.xxHash32Seed = buf.getInt();
        this.currentAcquiredIndex = buf.getLong();

        for (Sector sector : this.sectors) {
            sector.restoreFrom(buf);
            if (sector.hasData) {
                this.currentAcquiredIndex = Math.max(this.currentAcquiredIndex, sector.offset + sector.length);
            }
        }
    }

    private void writeHeaders() throws IOException {
        writeHeadersToChannel(this.channel);
    }

    private void writeHeadersToChannel(FileChannel ch) throws IOException {
        final ByteBuffer buf = ByteBuffer.allocateDirect(this.headerSize());

        buf.putLong(SUPER_BLOCK);
        buf.put(VERSION);
        buf.put(this.compressionLevel);
        buf.putInt(this.xxHash32Seed);
        buf.putLong(this.currentAcquiredIndex);
        for (Sector sector : this.sectors) {
            sector.encode(buf);
        }
        buf.flip();

        long offset = 0;
        while (buf.hasRemaining()) {
            offset += ch.write(buf, offset);
        }
    }

    private static int getChunkIndex(int x, int z) {
        return (x & 31) + ((z & 31) << 5);
    }

    private void writeChunk(int x, int z, @NotNull ByteBuffer data) throws IOException {
        final int chunkIndex = getChunkIndex(x, z);

        // Compute checksum before compress (position must be restored)
        final int savedPos = data.position();
        final int hash = this.xxHash32.hash(data, this.xxHash32Seed);
        data.position(savedPos);

        final ByteBuffer compressed = compress(ensureDirectBuffer(data));
        // Chunk section layout: uncompressed length (int) + timestamp (long) + xxHash32 (int) + compressed data
        final ByteBuffer section = ByteBuffer.allocateDirect(compressed.remaining() + 4 + 8 + 4);
        section.putInt(data.remaining());
        section.putLong(System.currentTimeMillis());
        section.putInt(hash);
        section.put(compressed);
        section.flip();

        this.currentAcquiredIndex = this.sectors[chunkIndex].store(section, this.channel, this.currentAcquiredIndex);

        this.markedToSave.set(true);
        this.lastWritten = System.nanoTime();
    }

    private @Nullable ByteBuffer readChunk(int x, int z) throws IOException {
        final Sector sector = this.sectors[getChunkIndex(x, z)];
        if (!sector.hasData) return null;

        final ByteBuffer raw = sector.read(this.channel);

        final int uncompressedLength = raw.getInt();
        raw.getLong(); // timestamp — not used
        final int expectedHash = raw.getInt();

        final ByteBuffer decompressed = decompress(ensureDirectBuffer(raw), uncompressedLength);

        final int savedPos = decompressed.position();
        final int actualHash = this.xxHash32.hash(decompressed, this.xxHash32Seed);
        decompressed.position(savedPos);

        if (actualHash != expectedHash) {
            throw new IOException("XXHash32 mismatch in " + this.filePath
                + " for chunk [" + x + "," + z + "]: expected " + expectedHash + " got " + actualHash);
        }

        return decompressed;
    }

    private void clearChunk(int chunkIndex) throws IOException {
        this.sectors[chunkIndex].clear();
        this.writeHeaders();
        this.markedToSave.set(true);
        this.lastWritten = System.nanoTime();
    }

    private @NotNull ByteBuffer ensureDirectBuffer(@NotNull ByteBuffer buf) {
        if (buf.isDirect()) return buf;
        final ByteBuffer direct = ByteBuffer.allocateDirect(buf.remaining());
        final int saved = buf.position();
        direct.put(buf);
        direct.flip();
        buf.position(saved);
        return direct;
    }

    private @NotNull ByteBuffer compress(@NotNull ByteBuffer input) throws IOException {
        final byte[] inputArray = toByteArray(input);
        try {
            final byte[] compressed = Zstd.compress(inputArray, this.compressionLevel);
            final ByteBuffer result = ByteBuffer.allocateDirect(compressed.length);
            result.put(compressed);
            result.flip();
            return result;
        } catch (Exception e) {
            throw new IOException("Compression failed", e);
        }
    }

    private @NotNull ByteBuffer decompress(@NotNull ByteBuffer input, int originalSize) throws IOException {
        final byte[] inputArray = toByteArray(input);
        try {
            final byte[] decompressed = Zstd.decompress(inputArray, originalSize);
            if (decompressed.length != originalSize) {
                throw new IOException("Decompression size mismatch: expected " + originalSize + ", got " + decompressed.length);
            }
            final ByteBuffer result = ByteBuffer.allocateDirect(originalSize);
            result.put(decompressed);
            result.flip();
            return result;
        } catch (Exception e) {
            throw new IOException("Decompression failed", e);
        }
    }

    private byte[] toByteArray(@NotNull ByteBuffer buf) {
        final int saved = buf.position();
        final byte[] arr = new byte[buf.remaining()];
        buf.get(arr);
        buf.position(saved);
        return arr;
    }

    /**
     * Writes the header and triggers compaction if the file has accumulated significant wasted space.
     * Must be called with the write lock held.
     */
    private void flushInternal() throws IOException {
        if (this.closed) return;

        this.writeHeaders();

        long fileSize = this.channel.size();
        long usedData = 0;
        for (Sector sector : this.sectors) {
            usedData += sector.length;
        }
        long spareSize = fileSize - this.headerSize() - usedData;

        if (spareSize > AUTO_COMPACT_SIZE && (double) spareSize > (double) usedData * AUTO_COMPACT_PERCENT) {
            this.compact();
        }
    }

    /**
     * Crash-safe compaction: all chunk data and the correct header are written to a .tmp file,
     * fsync'd, then atomically renamed over the original. No data loss window.
     * Must be called with the write lock held.
     */
    private void compact() throws IOException {
        final Path tmpPath = Path.of(this.filePath.toString() + ".tmp");

        long offsetPointer = this.headerSize();

        try (FileChannel tmp = FileChannel.open(
            tmpPath,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.READ,
            StandardOpenOption.TRUNCATE_EXISTING
        )) {
            tmp.position(this.headerSize());

            for (Sector sector : this.sectors) {
                if (!sector.hasData) continue;

                final long srcOffset = sector.offset;
                final long dataLen = sector.length;

                long transferred = 0;
                while (transferred < dataLen) {
                    transferred += this.channel.transferTo(srcOffset + transferred, dataLen - transferred, tmp);
                }

                // Replace sector with updated offset pointing into the compacted file
                this.sectors[sector.index] = new Sector(sector.index, offsetPointer, dataLen, true);
                offsetPointer += dataLen;
            }

            this.currentAcquiredIndex = offsetPointer;

            this.writeHeadersToChannel(tmp);
            tmp.force(true);
        }

        this.channel.close();
        Files.move(tmpPath, this.filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        reopenChannel();
    }

    private void reopenChannel() throws IOException {
        this.channel = FileChannel.open(
            this.filePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.READ
        );
    }

    @Override
    public @Nullable DataInputStream getChunkDataInputStream(@NotNull ChunkPos pos) throws IOException {
        this.fileAccessLock.readLock().lock();
        try {
            final ByteBuffer data = this.readChunk(pos.x, pos.z);
            if (data == null) return null;
            final byte[] bytes = new byte[data.remaining()];
            data.get(bytes);
            return new DataInputStream(new ByteArrayInputStream(bytes));
        } finally {
            this.fileAccessLock.readLock().unlock();
        }
    }

    @Override
    public boolean doesChunkExist(@NotNull ChunkPos pos) {
        this.fileAccessLock.readLock().lock();
        try {
            return this.sectors[getChunkIndex(pos.x, pos.z)].hasData;
        } finally {
            this.fileAccessLock.readLock().unlock();
        }
    }

    @Override
    public DataOutputStream getChunkDataOutputStream(@NotNull ChunkPos pos) {
        return new DataOutputStream(new ChunkBufferHelper(pos));
    }

    @Override
    public void write(@NotNull ChunkPos pos, ByteBuffer buf) throws IOException {
        this.fileAccessLock.writeLock().lock();
        try {
            this.writeChunk(pos.x, pos.z, buf);
        } finally {
            this.fileAccessLock.writeLock().unlock();
        }
    }

    @Override
    public void clear(@NotNull ChunkPos pos) throws IOException {
        this.fileAccessLock.writeLock().lock();
        try {
            this.clearChunk(getChunkIndex(pos.x, pos.z));
        } finally {
            this.fileAccessLock.writeLock().unlock();
        }
    }

    @Override
    public boolean hasChunk(@NotNull ChunkPos pos) {
        this.fileAccessLock.readLock().lock();
        try {
            return this.sectors[getChunkIndex(pos.x, pos.z)].hasData;
        } finally {
            this.fileAccessLock.readLock().unlock();
        }
    }

    @Override
    public void flush() throws IOException {
        this.fileAccessLock.writeLock().lock();
        try {
            if (!this.markedToSave.compareAndSet(true, false)) return;
            this.flushInternal();
            this.channel.force(true);
        } finally {
            this.fileAccessLock.writeLock().unlock();
        }
    }

    @Override
    public void close() throws IOException {
        LinearRegionFileFlusher.INSTANCE.removeFile(this);
        this.fileAccessLock.writeLock().lock();
        try {
            this.closed = true;
            this.markedToSave.set(false);
            this.compact();
            this.channel.force(true);
            this.channel.close();
        } finally {
            this.fileAccessLock.writeLock().unlock();
        }
    }

    @Override
    public MoonriseRegionFileIO.RegionDataController.WriteData moonrise$startWrite(CompoundTag data, ChunkPos pos) throws IOException {
        final DataOutputStream out = this.getChunkDataOutputStream(pos);
        return new MoonriseRegionFileIO.RegionDataController.WriteData(
            data,
            MoonriseRegionFileIO.RegionDataController.WriteData.WriteResult.WRITE,
            out,
            regionFile -> out.close()
        );
    }

    @Override
    public CompoundTag getOversizedData(int x, int z) throws IOException {
        return null;
    }

    @Override
    public boolean isOversized(int x, int z) {
        return false;
    }

    @Override
    public boolean recalculateHeader() {
        this.recalculateCount.incrementAndGet();
        return false;
    }

    @Override
    public void setOversized(int x, int z, boolean oversized) {}

    @Override
    public int getRecalculateCount() {
        return this.recalculateCount.get();
    }

    private static final class Sector {
        static final int ENCODED_SIZE = Long.BYTES + Long.BYTES + Byte.BYTES;

        final int index;
        long offset;
        long length;
        boolean hasData;

        Sector(int index, long offset, long length, boolean hasData) {
            this.index = index;
            this.offset = offset;
            this.length = length;
            this.hasData = hasData;
        }

        /** Appends data to the file at acquiredIndex, returns the updated acquiredIndex. */
        long store(@NotNull ByteBuffer data, @NotNull FileChannel channel, long acquiredIndex) throws IOException {
            this.hasData = true;
            this.length = data.remaining();
            this.offset = acquiredIndex;
            long pos = this.offset;
            while (data.hasRemaining()) {
                pos += channel.write(data, pos);
            }
            return acquiredIndex + this.length;
        }

        void encode(@NotNull ByteBuffer buf) {
            buf.putLong(this.offset);
            buf.putLong(this.length);
            buf.put((byte) (this.hasData ? 1 : 0));
        }

        void restoreFrom(@NotNull ByteBuffer buf) {
            this.offset = buf.getLong();
            this.length = buf.getLong();
            this.hasData = buf.get() == 1;
            if (this.offset < 0 || this.length < 0) {
                throw new IllegalStateException("Invalid sector data: offset=" + offset + " length=" + length);
            }
        }

        @NotNull ByteBuffer read(@NotNull FileChannel channel) throws IOException {
            final ByteBuffer result = ByteBuffer.allocateDirect((int) this.length);
            channel.read(result, this.offset);
            result.flip();
            return result;
        }

        void clear() {
            this.hasData = false;
        }
    }

    private class ChunkBufferHelper extends ByteArrayOutputStream {
        private final ChunkPos pos;

        ChunkBufferHelper(ChunkPos pos) {
            this.pos = pos;
        }

        @Override
        public void close() throws IOException {
            BufferedRegionFile.this.fileAccessLock.writeLock().lock();
            try {
                BufferedRegionFile.this.writeChunk(this.pos.x, this.pos.z, ByteBuffer.wrap(this.buf, 0, this.count));
            } finally {
                BufferedRegionFile.this.fileAccessLock.writeLock().unlock();
            }
        }
    }
}
