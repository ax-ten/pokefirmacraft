package com.kingtrapinch.tfcobblemon.dig;

/**
 * La copia del sito che tiene il client. La finestra e' aperta una alla volta,
 * quindi basta un posto solo.
 */
public final class ClientDigState {
    private ClientDigState() {}

    private static byte[] layers = new byte[DigSite.CELLS];
    private static int durability = DigSite.DURABILITY;

    public static void accept(DigSyncPayload payload) {
        layers = payload.layers();
        durability = payload.durability();
    }

    public static Layer layer(int x, int y) {
        final int i = DigSite.index(x, y);
        return Layer.values()[i < layers.length ? layers[i] : Layer.DUST.ordinal()];
    }

    public static int durability() {
        return durability;
    }

    public static void reset() {
        layers = new byte[DigSite.CELLS];
        durability = DigSite.DURABILITY;
    }
}
