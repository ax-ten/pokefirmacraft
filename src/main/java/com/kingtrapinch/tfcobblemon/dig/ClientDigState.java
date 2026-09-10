package com.kingtrapinch.tfcobblemon.dig;

/**
 * La copia del sito che tiene il client: quanto e' scavata ogni cella. Il
 * materiale che ci corrisponde lo sa il tipo di sito, che il client legge dal
 * blocco. La finestra e' aperta una alla volta, quindi basta un posto solo.
 */
public final class ClientDigState {
    private ClientDigState() {}

    private static byte[] depth = new byte[DigSite.CELLS];
    private static int durability = DigSite.DURABILITY;
    private static SiteKind kind = SiteKind.SEDIMENT;

    /** Le celle che hanno ceduto con l'ultimo colpo, per il pulviscolo. */
    private static final java.util.List<int[]> broken = new java.util.ArrayList<>();

    private static int found = -1;

    public static int drainFound() {
        final int v = found;
        found = -1;
        return v;
    }

    /** Il primo pacchetto e' la fotografia d'apertura: non ha rotto niente. */
    private static boolean primo = true;

    public static void accept(DigSyncPayload payload) {
        final byte[] prima = depth;
        found = payload.found();
        depth = payload.layers();
        durability = payload.durability();
        broken.clear();
        if (primo) {
            primo = false;
            found = -1;
            return;
        }
        for (int i = 0; i < Math.min(prima.length, depth.length); i++) {
            if ((depth[i] & 0x0F) > (prima[i] & 0x0F)) {
                broken.add(new int[] {i % DigSite.SIZE, i / DigSite.SIZE});
            }
        }
    }

    public static java.util.List<int[]> drainBroken() {
        final java.util.List<int[]> copia = java.util.List.copyOf(broken);
        broken.clear();
        return copia;
    }

    public static void kind(SiteKind value) {
        kind = value;
    }

    public static SiteKind kind() {
        return kind;
    }

    public static int depthAt(int x, int y) {
        final int i = DigSite.index(x, y);
        return i < depth.length ? depth[i] & 0x0F : 0;
    }

    public static boolean isCracked(int x, int y) {
        final int i = DigSite.index(x, y);
        return i < depth.length && (depth[i] & 0x10) != 0;
    }

    public static Layer layer(int x, int y) {
        return kind.materialAt(depthAt(x, y));
    }

    public static void reset() {
        depth = new byte[DigSite.CELLS];
        durability = DigSite.DURABILITY;
        broken.clear();
    }

    public static int durability() {
        return durability;
    }
}
