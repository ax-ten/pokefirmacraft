package com.kingtrapinch.tfcobblemon.dig;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;

/**
 * Lo stato di uno scavo: una griglia di strati piu' la durabilita' del sito.
 *
 * <p>Il disegno degli strati e' rumore generato al momento in cui il sito si
 * apre, non una tabella di forme: due giri di valori casuali lisciati sui
 * vicini, cosi' la roccia viene a chiazze invece che a scacchiera, e sotto la
 * roccia c'e' sempre qualcosa di piu' friabile.
 */
public final class DigSite {
    /** Lato della griglia. I giochi la fanno rettangolare perche' erano su DS. */
    public static final int SIZE = 8;
    public static final int CELLS = SIZE * SIZE;
    /** TODO testing: 100 e' un numero messo a caso, va tarato in gioco. */
    public static final int DURABILITY = 100;

    private final Layer[] layers = new Layer[CELLS];
    private int durability;

    private DigSite() {}

    public static DigSite generate(BlockPos pos, long seed) {
        final DigSite site = new DigSite();
        final RandomSource random = RandomSource.create(seed ^ pos.asLong());
        final float[] noise = new float[CELLS];
        for (int i = 0; i < CELLS; i++) {
            noise[i] = random.nextFloat();
        }
        // due passate di media sui quattro vicini: basta per fare chiazze
        for (int pass = 0; pass < 2; pass++) {
            final float[] next = new float[CELLS];
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    float sum = noise[index(x, y)];
                    int count = 1;
                    for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                        final int nx = x + d[0];
                        final int ny = y + d[1];
                        if (nx >= 0 && nx < SIZE && ny >= 0 && ny < SIZE) {
                            sum += noise[index(nx, ny)];
                            count++;
                        }
                    }
                    next[index(x, y)] = sum / count;
                }
            }
            System.arraycopy(next, 0, noise, 0, CELLS);
        }
        for (int i = 0; i < CELLS; i++) {
            site.layers[i] = noise[i] < 0.42F ? Layer.ROCK : noise[i] < 0.56F ? Layer.LIME : Layer.DUST;
        }
        site.durability = DURABILITY;
        return site;
    }

    public static int index(int x, int y) {
        return y * SIZE + x;
    }

    public Layer layer(int x, int y) {
        return layers[index(x, y)];
    }

    public int durability() {
        return durability;
    }

    public boolean exhausted() {
        return durability <= 0;
    }

    /** Toglie uno strato dalla cella, se l'attrezzo ci arriva. Dice se ha morso. */
    public boolean strip(int x, int y, DigTool tool) {
        final Layer here = layers[index(x, y)];
        if (here == Layer.EMPTY || here.harderThan(tool.reaches)) {
            return false;
        }
        layers[index(x, y)] = here.below();
        return true;
    }

    public void spend(int amount) {
        durability = Math.max(0, durability - amount);
    }

    /** Gli strati appiattiti, per mandarli al client. */
    public byte[] snapshot() {
        final byte[] flat = new byte[CELLS];
        for (int i = 0; i < CELLS; i++) {
            flat[i] = (byte) layers[i].ordinal();
        }
        return flat;
    }

    public CompoundTag save() {
        final CompoundTag tag = new CompoundTag();
        final byte[] flat = new byte[CELLS];
        for (int i = 0; i < CELLS; i++) {
            flat[i] = (byte) layers[i].ordinal();
        }
        tag.putByteArray("layers", flat);
        tag.putInt("durability", durability);
        return tag;
    }

    public static DigSite load(CompoundTag tag) {
        final DigSite site = new DigSite();
        final byte[] flat = tag.getByteArray("layers");
        for (int i = 0; i < CELLS; i++) {
            site.layers[i] = Layer.values()[i < flat.length ? flat[i] : Layer.DUST.ordinal()];
        }
        site.durability = tag.getInt("durability");
        return site;
    }
}
