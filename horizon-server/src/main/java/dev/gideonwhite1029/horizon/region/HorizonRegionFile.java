package dev.gideonwhite1029.horizon.region;

import ca.spottedleaf.moonrise.patches.chunk_system.io.MoonriseRegionFileIO;
import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.mojang.logging.LogUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

import net.openhft.hashing.LongHashFunction;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;

// LinearRegionFile_implementation_version_0_5byXymb
// Just gonna use this string to inform other forks about updates ;-)
public class HorizonRegionFile implements IRegionFile {
    private static final long SUPERBLOCK = 0xc3ff13183cca9d9aL;
    private static final byte VERSION = 3;
    private static final int HEADER_SIZE = 27;
    private static final int FOOTER_SIZE = 8;
    private static final Logger LOGGER = LogUtils.getLogger();

    private byte[][] bucketBuffers;
    private final byte[][] buffer = new byte[1024][];
    private final int[] bufferUncompressedSize = new int[1024];

    private final long[] chunkTimestamps = new long[1024];
    private final boolean[] chunkExistenceBitmap = new boolean[1024];
    private final LZ4Compressor compressor;
    private final LZ4FastDecompressor decompressor;

    private final AtomicBoolean markedToSave = new AtomicBoolean(false);
    private volatile long lastWritten = 0; // System.nanoTime() of last write
    private volatile boolean closed = false;
    private final AtomicBoolean flushing = new AtomicBoolean(false);

    public final ReentrantLock fileLock = new ReentrantLock(true);
    public Path regionFile;

    private final int compressionLevel;
    private int gridSize = 8;
    private int bucketSize = 4;

    public Path getRegionFile() {
        return this.regionFile;
    }

    public ReentrantLock getFileLock() {
        return this.fileLock;
    }

    private int chunkToBucketIdx(int chunkX, int chunkZ) {
        int bx = chunkX / bucketSize, bz = chunkZ / bucketSize;
        return bx * gridSize + bz;
    }

    private void openBucket(int chunkX, int chunkZ) {
        chunkX = Math.floorMod(chunkX, 32);
        chunkZ = Math.floorMod(chunkZ, 32);
        int idx = chunkToBucketIdx(chunkX, chunkZ);

        if (bucketBuffers == null) return;
        if (bucketBuffers[idx] != null) {
            try (ByteArrayInputStream bucketByteStream = new ByteArrayInputStream(bucketBuffers[idx]);
                ZstdInputStream zstdStream = new ZstdInputStream(bucketByteStream)) {
                ByteBuffer bucketBuffer = ByteBuffer.wrap(zstdStream.readAllBytes());

                int bx = chunkX / bucketSize, bz = chunkZ / bucketSize;

                for (int cx = 0; cx < 32 / gridSize; cx++) {
                    for (int cz = 0; cz < 32 / gridSize; cz++) {
                        int chunkIndex = (bx * (32 / gridSize) + cx) + (bz * (32 / gridSize) + cz) * 32;

                        int chunkSize = bucketBuffer.getInt();
                        long timestamp = bucketBuffer.getLong();
                        this.chunkTimestamps[chunkIndex] = timestamp;

                        if (chunkSize > 0) {
                            byte[] chunkData = new byte[chunkSize - 8];
                            bucketBuffer.get(chunkData);

                            int maxCompressedLength = this.compressor.maxCompressedLength(chunkData.length);
                            byte[] compressed = new byte[maxCompressedLength];
                            int compressedLength = this.compressor.compress(chunkData, 0, chunkData.length, compressed, 0, maxCompressedLength);
                            byte[] finalCompressed = new byte[compressedLength];
                            System.arraycopy(compressed, 0, finalCompressed, 0, compressedLength);

                            if (chunkX == cx && chunkZ == cz) {
                                this.buffer[chunkIndex] = finalCompressed;
                                this.bufferUncompressedSize[chunkIndex] = chunkData.length;
                                this.chunkExistenceBitmap[chunkIndex] = true;
                                return;
                            }
                            this.buffer[chunkIndex] = finalCompressed;
                            this.bufferUncompressedSize[chunkIndex] = chunkData.length;
                            this.chunkExistenceBitmap[chunkIndex] = true;
                        } else {
                            this.chunkExistenceBitmap[chunkIndex] = false;
                        }
                    }
                }
            } catch (IOException ex) {
                LOGGER.error("Region file corrupted: " + regionFile + " bucket: " + idx);
                MinecraftServer.getServer().safeShutdown(true, false);
            }
            bucketBuffers[idx] = null;
        }
    }

    public volatile boolean regionFileOpen = false;

    public boolean isMarkedToSave() {
        return this.markedToSave.get();
    }

    public long getLastWritten() {
        return this.lastWritten;
    }

    public boolean isClosedVolatile() {
        return this.closed;
    }

    public boolean tryMarkFlushing() {
        return this.flushing.compareAndSet(false, true);
    }

    public void syncIfNeeded() throws IOException {
        try {
            flush();
        } finally {
            this.flushing.set(false);
        }
    }

    private synchronized void openRegionFile() {
        if (regionFileOpen) return;
        regionFileOpen = true;

        File regionFile = new File(this.regionFile.toString());

        if(!regionFile.canRead()) {
            return;
        }

        try {
            byte[] fileContent = Files.readAllBytes(this.regionFile);
            ByteBuffer buffer = ByteBuffer.wrap(fileContent);

            long superBlock = buffer.getLong();
            if (superBlock != SUPERBLOCK)
                throw new RuntimeException("Invalid superblock: " + superBlock + " file " + this.regionFile);

            byte version = buffer.get();
            if (version == 1 || version == 2) {
                parseLinearV1(buffer);
            } else if (version == 3) {
                parseLinearV2(buffer);
            } else {
                throw new RuntimeException("Invalid version: " + version + " file " + this.regionFile);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to open region file " + this.regionFile, e);
        }
    }

    private void parseLinearV1(ByteBuffer buffer) throws IOException {
        final int HEADER_SIZE = 32;
        final int FOOTER_SIZE = 8;

        // Skip newestTimestamp (Long) + Compression level (Byte) + Chunk count (Short): Unused.
        buffer.position(buffer.position() + 11);

        int dataCount = buffer.getInt();
        long fileLength = this.regionFile.toFile().length();
        if (fileLength != HEADER_SIZE + dataCount + FOOTER_SIZE) {
            throw new IOException("Invalid file length: " + this.regionFile + " " + fileLength + " " + (HEADER_SIZE + dataCount + FOOTER_SIZE));
        }

        buffer.position(buffer.position() + 8); // Skip data hash (Long): Unused.

        byte[] rawCompressed = new byte[dataCount];
        buffer.get(rawCompressed);

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(rawCompressed);
             ZstdInputStream zstdInputStream = new ZstdInputStream(byteArrayInputStream)) {
            ByteBuffer decompressedBuffer = ByteBuffer.wrap(zstdInputStream.readAllBytes());

            int[] starts = new int[1024];
            for (int i = 0; i < 1024; i++) {
                starts[i] = decompressedBuffer.getInt();
                decompressedBuffer.getInt(); // Skip timestamps (Int): Unused.
            }

            for (int i = 0; i < 1024; i++) {
                if (starts[i] > 0) {
                    int size = starts[i];
                    byte[] chunkData = new byte[size];
                    decompressedBuffer.get(chunkData);

                    int maxCompressedLength = this.compressor.maxCompressedLength(size);
                    byte[] compressed = new byte[maxCompressedLength];
                    int compressedLength = this.compressor.compress(chunkData, 0, size, compressed, 0, maxCompressedLength);
                    byte[] finalCompressed = new byte[compressedLength];
                    System.arraycopy(compressed, 0, finalCompressed, 0, compressedLength);

                    this.buffer[i] = finalCompressed;
                    this.bufferUncompressedSize[i] = size;
                    this.chunkExistenceBitmap[i] = true;
                    this.chunkTimestamps[i] = getTimestamp();
                } else {
                    this.chunkExistenceBitmap[i] = false;
                }
            }
        }
    }

    private void parseLinearV2(ByteBuffer buffer) throws IOException {
        buffer.getLong(); // Skip newestTimestamp (Long)
        gridSize = buffer.get();
        if (gridSize != 1 && gridSize != 2 && gridSize != 4 && gridSize != 8 && gridSize != 16 && gridSize != 32)
            throw new RuntimeException("Invalid grid size: " + gridSize + " file " + this.regionFile);
        bucketSize = 32 / gridSize;

        buffer.getInt(); // Skip region_x (Int)
        buffer.getInt(); // Skip region_z (Int)

        boolean[] existenceBitmap = deserializeExistenceBitmap(buffer);

        while (true) {
            byte featureNameLength = buffer.get();
            if (featureNameLength == 0) break;
            byte[] featureNameBytes = new byte[featureNameLength];
            buffer.get(featureNameBytes);
            String featureName = new String(featureNameBytes);
            int featureValue = buffer.getInt();
        }

        int[] bucketSizes = new int[gridSize * gridSize];
        byte[] bucketCompressionLevels = new byte[gridSize * gridSize];
        long[] bucketHashes = new long[gridSize * gridSize];
        for (int i = 0; i < gridSize * gridSize; i++) {
            bucketSizes[i] = buffer.getInt();
            bucketCompressionLevels[i] = buffer.get();
            bucketHashes[i] = buffer.getLong();
        }

        bucketBuffers = new byte[gridSize * gridSize][];
        for (int i = 0; i < gridSize * gridSize; i++) {
            if (bucketSizes[i] > 0) {
                bucketBuffers[i] = new byte[bucketSizes[i]];
                buffer.get(bucketBuffers[i]);
                long rawHash = LongHashFunction.xx().hashBytes(bucketBuffers[i]);
                if (rawHash != bucketHashes[i]) throw new IOException("Region file hash incorrect " + this.regionFile);
            }
        }

        System.arraycopy(existenceBitmap, 0, this.chunkExistenceBitmap, 0, existenceBitmap.length);

        long footerSuperBlock = buffer.getLong();
        if (footerSuperBlock != SUPERBLOCK)
            throw new IOException("Footer superblock invalid " + this.regionFile);
    }

    public HorizonRegionFile(RegionStorageInfo storageKey, Path directory, Path path, boolean dsync, int compressionLevel) throws IOException {
        this(storageKey, directory, path, RegionFileVersion.getCompressionFormat(), dsync, compressionLevel);
    }

    public HorizonRegionFile(RegionStorageInfo storageKey, Path path, Path directory, RegionFileVersion compressionFormat, boolean dsync, int compressionLevel) throws IOException {
        this.regionFile = path;
        this.compressionLevel = compressionLevel;
        this.compressor = LZ4Factory.fastestInstance().fastCompressor();
        this.decompressor = LZ4Factory.fastestInstance().fastDecompressor();
        LinearRegionFileFlusher.INSTANCE.addFile(this);
    }

    private void markToSave() {
        this.markedToSave.set(true);
        this.lastWritten = System.nanoTime();
    }

    public synchronized boolean doesChunkExist(ChunkPos pos) throws Exception {
        openRegionFile();
        return chunkExistenceBitmap[getChunkIndex(pos.x, pos.z)];
    }

    public synchronized void flush() throws IOException {
        if (!this.markedToSave.compareAndSet(true, false)) return;

        openRegionFile();

        long timestamp = getTimestamp();

        File tempFile = new File(regionFile.toString() + ".tmp");
        try (FileOutputStream fileStream = new FileOutputStream(tempFile);
             DataOutputStream dataStream = new DataOutputStream(fileStream)) {

            dataStream.writeLong(SUPERBLOCK);
            dataStream.writeByte(VERSION);
            dataStream.writeLong(timestamp);
            dataStream.writeByte(gridSize);

            String fileName = regionFile.getFileName().toString();
            String[] parts = fileName.split("\\.");
            int regionX = 0;
            int regionZ = 0;
            try {
                if (parts.length >= 4) {
                    regionX = Integer.parseInt(parts[1]);
                    regionZ = Integer.parseInt(parts[2]);
                } else {
                    LOGGER.warn("Unexpected file name format: " + fileName);
                }
            } catch (NumberFormatException e) {
                LOGGER.error("Failed to parse region coordinates from file name: " + fileName, e);
            }

            dataStream.writeInt(regionX);
            dataStream.writeInt(regionZ);

            for (int i = 0; i < 1024; i++) {
                chunkExistenceBitmap[i] = (this.bufferUncompressedSize[i] > 0);
            }
            writeSerializedExistenceBitmap(dataStream, chunkExistenceBitmap);

            writeNBTFeatures(dataStream);

            byte[][] buckets = new byte[gridSize * gridSize][];
            for (int bx = 0; bx < gridSize; bx++) {
                for (int bz = 0; bz < gridSize; bz++) {
                    if (bucketBuffers != null && bucketBuffers[bx * gridSize + bz] != null) {
                        buckets[bx * gridSize + bz] = bucketBuffers[bx * gridSize + bz];
                        continue;
                    }

                    try (ByteArrayOutputStream bucketStream = new ByteArrayOutputStream();
                         ZstdOutputStream zstdStream = new ZstdOutputStream(bucketStream, this.compressionLevel);
                         DataOutputStream bucketDataStream = new DataOutputStream(zstdStream)) {

                        boolean hasData = false;
                        for (int cx = 0; cx < 32 / gridSize; cx++) {
                            for (int cz = 0; cz < 32 / gridSize; cz++) {
                                int chunkIndex = (bx * 32 / gridSize + cx) + (bz * 32 / gridSize + cz) * 32;
                                if (this.bufferUncompressedSize[chunkIndex] > 0) {
                                    hasData = true;
                                    byte[] chunkData = new byte[this.bufferUncompressedSize[chunkIndex]];
                                    this.decompressor.decompress(this.buffer[chunkIndex], 0, chunkData, 0, this.bufferUncompressedSize[chunkIndex]);
                                    bucketDataStream.writeInt(chunkData.length + 8);
                                    bucketDataStream.writeLong(this.chunkTimestamps[chunkIndex]);
                                    bucketDataStream.write(chunkData);
                                } else {
                                    bucketDataStream.writeInt(0);
                                    bucketDataStream.writeLong(this.chunkTimestamps[chunkIndex]);
                                }
                            }
                        }

                        if (hasData) {
                            bucketDataStream.flush();
                            buckets[bx * gridSize + bz] = bucketStream.toByteArray();
                        }
                    }
                }
            }

            for (int i = 0; i < gridSize * gridSize; i++) {
                dataStream.writeInt(buckets[i] != null ? buckets[i].length : 0);
                dataStream.writeByte(this.compressionLevel);
                long rawHash = 0;
                if (buckets[i] != null) {
                    rawHash = LongHashFunction.xx().hashBytes(buckets[i]);
                }
                dataStream.writeLong(rawHash);
            }

            for (int i = 0; i < gridSize * gridSize; i++) {
                if (buckets[i] != null) {
                    dataStream.write(buckets[i]);
                }
            }

            dataStream.writeLong(SUPERBLOCK);
            dataStream.flush();
            fileStream.getChannel().force(true);
        }

        Files.move(tempFile.toPath(), this.regionFile, StandardCopyOption.REPLACE_EXISTING);
    }

    private void writeNBTFeatures(DataOutputStream dataStream) throws IOException {
        dataStream.writeByte(0); // End of NBT features
    }

    private void writeNBTFeature(DataOutputStream dataStream, String featureName, int featureValue) throws IOException {
        byte[] featureNameBytes = featureName.getBytes();
        dataStream.writeByte(featureNameBytes.length);
        dataStream.write(featureNameBytes);
        dataStream.writeInt(featureValue);
    }

    public static final int MAX_CHUNK_SIZE = 500 * 1024 * 1024;

    public synchronized void write(ChunkPos pos, ByteBuffer buffer) {
        openRegionFile();
        openBucket(pos.x, pos.z);
        try {
            byte[] b = byteBufferToArray(buffer);
            int uncompressedSize = b.length;

            if (uncompressedSize > MAX_CHUNK_SIZE) {
                LOGGER.error("Chunk dupe attempt " + this.regionFile);
                clear(pos);
            } else {
                int maxCompressedLength = this.compressor.maxCompressedLength(b.length);
                byte[] compressed = new byte[maxCompressedLength];
                int compressedLength = this.compressor.compress(b, 0, b.length, compressed, 0, maxCompressedLength);
                byte[] finalCompressed = new byte[compressedLength];
                System.arraycopy(compressed, 0, finalCompressed, 0, compressedLength);

                int index = getChunkIndex(pos.x, pos.z);
                this.buffer[index] = finalCompressed;
                this.chunkTimestamps[index] = getTimestamp();
                this.bufferUncompressedSize[index] = uncompressedSize;
                this.chunkExistenceBitmap[index] = true;
            }
        } catch (IOException e) {
            LOGGER.error("Chunk write IOException " + e + " " + this.regionFile);
        }
        markToSave();
    }

    public DataOutputStream getChunkDataOutputStream(ChunkPos pos) {
        openRegionFile();
        openBucket(pos.x, pos.z);
        return new DataOutputStream(new BufferedOutputStream(new HorizonRegionFile.ChunkBuffer(pos)));
    }

    @Override
    public MoonriseRegionFileIO.RegionDataController.WriteData moonrise$startWrite(CompoundTag data, ChunkPos pos) throws IOException {
        final DataOutputStream out = this.getChunkDataOutputStream(pos);

        return new ca.spottedleaf.moonrise.patches.chunk_system.io.MoonriseRegionFileIO.RegionDataController.WriteData(
                data, ca.spottedleaf.moonrise.patches.chunk_system.io.MoonriseRegionFileIO.RegionDataController.WriteData.WriteResult.WRITE,
                out, regionFile -> out.close()
        );
    }

    private class ChunkBuffer extends ByteArrayOutputStream {

        private final ChunkPos pos;

        public ChunkBuffer(ChunkPos chunkcoordintpair) {
            super();
            this.pos = chunkcoordintpair;
        }

        public void close() throws IOException {
            ByteBuffer bytebuffer = ByteBuffer.wrap(this.buf, 0, this.count);
            HorizonRegionFile.this.write(this.pos, bytebuffer);
        }
    }

    private byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] tempBuffer = new byte[4096];

        int length;
        while ((length = in.read(tempBuffer)) >= 0) {
            out.write(tempBuffer, 0, length);
        }

        return out.toByteArray();
    }

    private byte[] byteBufferToArray(ByteBuffer buf) throws IOException {
        ByteBuffer slice = buf.slice();
        if (slice.hasArray()) {
            int offset = slice.arrayOffset() + slice.position();
            int length = slice.remaining();
            byte[] copy = new byte[length];
            System.arraycopy(slice.array(), offset, copy, 0, length);
            return copy;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] temp = new byte[Math.max(4096, slice.remaining())];
            while (slice.hasRemaining()) {
                int n = Math.min(temp.length, slice.remaining());
                slice.get(temp, 0, n);
                out.write(temp, 0, n);
            }
            return out.toByteArray();
        }
    }

    @Nullable
    public synchronized DataInputStream getChunkDataInputStream(ChunkPos pos) {
        openRegionFile();
        openBucket(pos.x, pos.z);

        int idx = getChunkIndex(pos.x, pos.z);
        if(this.bufferUncompressedSize[idx] != 0 && this.chunkExistenceBitmap[idx]) {
            byte[] content = new byte[bufferUncompressedSize[idx]];
            this.decompressor.decompress(this.buffer[idx], 0, content, 0, bufferUncompressedSize[idx]);
            return new DataInputStream(new ByteArrayInputStream(content));
        }
        return null;
    }

    public synchronized void clear(ChunkPos pos) {
        openRegionFile();
        openBucket(pos.x, pos.z);
        int i = getChunkIndex(pos.x, pos.z);
        this.buffer[i] = null;
        this.bufferUncompressedSize[i] = 0;
        this.chunkTimestamps[i] = 0;
        this.chunkExistenceBitmap[i] = false;
        markToSave();
    }

    public synchronized boolean hasChunk(ChunkPos pos) {
        openRegionFile();
        openBucket(pos.x, pos.z);
        return this.bufferUncompressedSize[getChunkIndex(pos.x, pos.z)] > 0;
    }

    public synchronized void close() throws IOException {
        LinearRegionFileFlusher.INSTANCE.removeFile(this);
        this.closed = true;

        openRegionFile();
        this.markedToSave.set(true);
        try {
            flush();
        } catch (IOException e) {
            throw new IOException("Region flush IOException " + e + " " + this.regionFile);
        }
    }

    private static int getChunkIndex(int x, int z) {
        return (x & 31) + ((z & 31) << 5);
    }

    private static long getTimestamp() {
        return System.currentTimeMillis() / 1000L;
    }

    public boolean recalculateHeader() {
        return false;
    }

    private final java.util.concurrent.atomic.AtomicInteger recalculateCount = new java.util.concurrent.atomic.AtomicInteger();

    public int getRecalculateCount() {
        return this.recalculateCount.get();
    }

    public void setOversized(int x, int z, boolean something) {}

    public CompoundTag getOversizedData(int x, int z) throws IOException {
        throw new IOException("getOversizedData is a stub " + this.regionFile);
    }

    public boolean isOversized(int x, int z) {
        return false;
    }

    public Path getPath() {
        return this.regionFile;
    }

    private boolean[] deserializeExistenceBitmap(ByteBuffer buffer) {
        boolean[] result = new boolean[1024];
        for (int i = 0; i < 128; i++) {
            byte b = buffer.get();
            for (int j = 0; j < 8; j++) {
                result[i * 8 + j] = ((b >> (7 - j)) & 1) == 1;
            }
        }
        return result;
    }

    private void writeSerializedExistenceBitmap(DataOutputStream out, boolean[] bitmap) throws IOException {
        for (int i = 0; i < 128; i++) {
            byte b = 0;
            for (int j = 0; j < 8; j++) {
                if (bitmap[i * 8 + j]) {
                    b |= (1 << (7 - j));
                }
            }
            out.writeByte(b);
        }
    }
}