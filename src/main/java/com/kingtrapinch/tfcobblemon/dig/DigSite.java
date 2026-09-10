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
    /**
     * Lato della griglia. Nove come le colonne dell'inventario, cosi' le due
     * griglie si allineano; i giochi la fanno rettangolare perche' erano su DS.
     */
    public static final int SIZE = 9;
    public static final int CELLS = SIZE * SIZE;
    /** TODO testing: 100 e' un numero messo a caso, va tarato in gioco. */
    public static final int DURABILITY = 100;

    /** Quanto e' stata scavata ogni cella. Il materiale lo dice il tipo di sito. */
    private final byte[] depth = new byte[CELLS];
    private int durability;
    private SiteKind kind = SiteKind.SEDIMENT;

    private DigSite() {}

    public static DigSite generate(BlockPos pos, long seed, SiteKind kind) {
        final DigSite site = new DigSite();
        site.kind = kind;
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
            site.depth[i] = (byte) kind.startDepth(noise[i]);
        }
        site.durability = DURABILITY;
        return site;
    }

    public static int index(int x, int y) {
        return y * SIZE + x;
    }

    public Layer layer(int x, int y) {
        return kind.materialAt(depth[index(x, y)]);
    }

    public int durability() {
        return durability;
    }

    public SiteKind kind() {
        return kind;
    }

    public boolean exhausted() {
        return durability <= 0;
    }

    /** Toglie uno strato dalla cella, se l'attrezzo ci arriva. Dice se ha morso. */
    public boolean strip(int x, int y, DigTool tool) {
        final int i = index(x, y);
        if (!tool.bites(kind.materialAt(depth[i]))) {
            return false;
        }
        depth[i]++;
        return true;
    }

    public boolean cleared(int x, int y) {
        return kind.materialAt(depth[index(x, y)]) == Layer.EMPTY;
    }

    public int depthAt(int x, int y) {
        return depth[index(x, y)];
    }

    public void spend(int amount) {
        durability = Math.max(0, durability - amount);
    }

    /** Le profondita' appiattite, per mandarle al client. */
    public byte[] snapshot() {
        return depth.clone();
    }

    public CompoundTag save() {
        final CompoundTag tag = new CompoundTag();
        tag.putByteArray("depth", depth);
        tag.putInt("durability", durability);
        tag.putString("kind", kind.name());
        return tag;
    }

    public static DigSite load(CompoundTag tag) {
        final DigSite site = new DigSite();
        final byte[] flat = tag.getByteArray("depth");
        System.arraycopy(flat, 0, site.depth, 0, Math.min(flat.length, CELLS));
        site.durability = tag.getInt("durability");
        site.kind = tag.contains("kind")
                ? SiteKind.valueOf(tag.getString("kind")) : SiteKind.SEDIMENT;
        return site;
    }
}
