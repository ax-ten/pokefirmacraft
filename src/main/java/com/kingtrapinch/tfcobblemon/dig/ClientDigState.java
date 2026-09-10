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

    public static void accept(DigSyncPayload payload) {
        depth = payload.layers();
        durability = payload.durability();
    }

    public static void kind(SiteKind value) {
        kind = value;
    }

    public static SiteKind kind() {
        return kind;
    }

    public static int depthAt(int x, int y) {
        final int i = DigSite.index(x, y);
        return i < depth.length ? depth[i] : 0;
    }

    public static Layer layer(int x, int y) {
        return kind.materialAt(depthAt(x, y));
    }

    public static void reset() {
        depth = new byte[DigSite.CELLS];
        durability = DigSite.DURABILITY;
    }

    public static int durability() {
        return durability;
    }
}
