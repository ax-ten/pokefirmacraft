package com.kingtrapinch.tfcobblemon.dig;

/**
 * Le misure della finestra, in un posto solo perche' le usano sia il menu (per
 * mettere gli slot) sia lo schermo (per disegnare), e devono coincidere.
 *
 * <p>La cella e' diciotto pixel come uno slot: cosi' la griglia dello scavo e
 * quella dell'inventario sono larghe uguale e stanno incolonnate.
 */
public final class DigLayout {
    private DigLayout() {}

    public static final int CELL = 18;
    public static final int WIDTH = 176;

    public static final int TOOLS_X = 8;
    public static final int TOOLS_Y = 18;

    public static final int GRID_X = 8;
    public static final int GRID_Y = 40;
    public static final int GRID_SPAN = DigSite.SIZE * CELL;

    public static final int BAR_Y = GRID_Y + GRID_SPAN + 5;

    public static final int INV_LABEL_Y = BAR_Y + 12;
    public static final int INV_Y = INV_LABEL_Y + 11;
    public static final int HOTBAR_Y = INV_Y + 58;

    public static final int HEIGHT = HOTBAR_Y + 18 + 7;

    public static int cellX(int gx) {
        return GRID_X + gx * CELL;
    }

    public static int cellY(int gy) {
        return GRID_Y + gy * CELL;
    }

    /** Il centro del quadrato 2x2 di un tesoro, dove ci sta lo slot. */
    public static int treasureX(int x) {
        return cellX(x) + CELL - 8;
    }

    public static int treasureY(int y) {
        return cellY(y) + CELL - 8;
    }
}
