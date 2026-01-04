package dev. gideonwhite1029.horizon.matter;

import net. minecraft.util.Mth;
import net.minecraft.util. RandomSource;
import net.minecraft.world.level.levelgen. LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class WorldgenCryptoRandom extends WorldgenRandom {
    private static final long[] HASHED_ZERO_SEED = Hashing.hashWorldSeed(new long[Globals.WORLD_SEED_LONGS]);
    private static final ThreadLocal<long[]> LAST_SEEN_WORLD_SEED = ThreadLocal.withInitial(() -> new long[Globals.WORLD_SEED_LONGS]);
    private static final ThreadLocal<long[]> HASHED_WORLD_SEED = ThreadLocal.withInitial(() -> HASHED_ZERO_SEED);

    private final long[] worldSeed = new long[Globals.WORLD_SEED_LONGS];
    private final long[] randomBits = new long[8];
    private int randomBitIndex;
    private static final int MAX_RANDOM_BIT_INDEX = 512; // 64 * 8
    private long counter;
    private final long[] message = new long[16];
    private final long[] cachedInternalState = new long[16];

    public WorldgenCryptoRandom(int x, int z, Globals.Salt typeSalt, long salt) {
        super(new LegacyRandomSource(0L));
        if (typeSalt != null) {
            this.setSecureSeed(x, z, typeSalt, salt);
        }
    }

    public void setSecureSeed(int x, int z, Globals.Salt typeSalt, long salt) {
        System.arraycopy(Globals. worldSeed, 0, this.worldSeed, 0, Globals.WORLD_SEED_LONGS);

        // Compact message packing
        message[0] = ((long) x << 32) | (z & 0xFFFFFFFFL);
        message[1] = ((long) Globals.dimension.get() << 32) | (salt & 0xFFFFFFFFL);
        message[2] = typeSalt.ordinal();
        message[3] = counter = 0;

        randomBitIndex = MAX_RANDOM_BIT_INDEX; // Force refresh on first use
    }

    private long[] getHashedWorldSeed() {
        if (!Arrays.equals(worldSeed, LAST_SEEN_WORLD_SEED.get())) {
            HASHED_WORLD_SEED.set(Hashing.hashWorldSeed(worldSeed));
            System.arraycopy(worldSeed, 0, LAST_SEEN_WORLD_SEED.get(), 0, Globals.WORLD_SEED_LONGS);
        }
        return HASHED_WORLD_SEED.get();
    }

    private void moreRandomBits() {
        message[3] = counter++;
        System.arraycopy(getHashedWorldSeed(), 0, randomBits, 0, 8);
        Hashing.hash(message, randomBits, cachedInternalState, 64, true);
    }

    private long getBits(int count) {
        if (randomBitIndex >= MAX_RANDOM_BIT_INDEX) {
            moreRandomBits();
            randomBitIndex = 0;
        }

        int wordIndex = randomBitIndex >>> 6; // divide by 64
        int bitOffset = randomBitIndex & 63;   // modulo 64

        randomBitIndex += count;

        // Fast path:  all bits in same word
        if (bitOffset + count <= 64) {
            return (randomBits[wordIndex] >>> bitOffset) & ((1L << count) - 1);
        }

        // Slow path: bits span two words
        int bitsFromFirst = 64 - bitOffset;
        long result = randomBits[wordIndex] >>> bitOffset;

        // Check if we need refresh
        if (randomBitIndex >= MAX_RANDOM_BIT_INDEX) {
            moreRandomBits();
            randomBitIndex = count - bitsFromFirst;
        }

        int bitsFromSecond = count - bitsFromFirst;
        result |= (randomBits[randomBitIndex >>> 6] & ((1L << bitsFromSecond) - 1)) << bitsFromFirst;

        return result;
    }

    @Override
    public @NotNull RandomSource fork() {
        WorldgenCryptoRandom fork = new WorldgenCryptoRandom(0, 0, null, 0);
        System.arraycopy(this.worldSeed, 0, fork.worldSeed, 0, Globals.WORLD_SEED_LONGS);
        System.arraycopy(this.message, 0, fork.message, 0, 4);
        fork.randomBitIndex = this. randomBitIndex;
        fork.counter = this.counter;
        fork.nextLong(); // Advance state
        return fork;
    }

    @Override
    public int next(int bits) {
        return (int) getBits(bits);
    }

    @Override
    public void consumeCount(int count) {
        randomBitIndex += count;
        if (randomBitIndex >= MAX_RANDOM_BIT_INDEX) {
            int skips = randomBitIndex / MAX_RANDOM_BIT_INDEX;
            counter += skips;
            randomBitIndex %= MAX_RANDOM_BIT_INDEX;
            randomBitIndex += MAX_RANDOM_BIT_INDEX; // Force refresh
        }
    }

    @Override
    public int nextInt(int bound) {
        if ((bound & (bound - 1)) == 0) { // power of 2
            return (int) ((bound * getBits(31)) >> 31);
        }

        int bits, result;
        do {
            bits = (int) getBits(31);
            result = bits % bound;
        } while (bits - result + (bound - 1) < 0);

        return result;
    }

    @Override
    public long nextLong() {
        return getBits(64);
    }

    @Override
    public double nextDouble() {
        return (getBits(53) >>> 11) * 0x1.0p-53;
    }

    @Override
    public long setDecorationSeed(long worldSeed, int blockX, int blockZ) {
        setSecureSeed(blockX, blockZ, Globals.Salt. POPULATION, 0);
        return ((long) blockX << 32) | (blockZ & 0xFFFFFFFFL);
    }

    @Override
    public void setFeatureSeed(long populationSeed, int index, int step) {
        setSecureSeed((int) (populationSeed >> 32), (int) populationSeed,
                Globals.Salt. DECORATION, index + 10000L * step);
    }

    public static RandomSource seedSlimeChunk(int chunkX, int chunkZ) {
        return new WorldgenCryptoRandom(chunkX, chunkZ, Globals.Salt.SLIME_CHUNK, 0);
    }
}