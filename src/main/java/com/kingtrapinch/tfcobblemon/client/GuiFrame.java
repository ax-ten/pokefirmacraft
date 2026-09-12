package com.kingtrapinch.tfcobblemon.client;

import net.minecraft.client.gui.GuiGraphics;

/**
 * I due riquadri di cui e' fatta una finestra di vanilla, disegnati invece che
 * ritagliati da un png: il pannello, che sporge, e la conca, che rientra. Non
 * servono texture e le finestre nostre restano della stessa pasta di quelle di
 * Minecraft anche se cambia la risoluzione.
 */
public final class GuiFrame {
    private GuiFrame() {}

    private static final int BODY = 0xFFC6C6C6;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int SHADE = 0xFF555555;
    private static final int WELL = 0xFF8B8B8B;
    private static final int WELL_DARK = 0xFF373737;

    /** Il corpo della finestra: chiaro in alto a sinistra, scuro in basso. */
    public static void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, BODY);
        g.fill(x, y, x + w - 1, y + 1, LIGHT);
        g.fill(x, y, x + 1, y + h - 1, LIGHT);
        g.fill(x + 1, y + h - 1, x + w, y + h, SHADE);
        g.fill(x + w - 1, y + 1, x + w, y + h, SHADE);
    }

    /** Una conca, cioe' uno slot: le ombre girate al contrario. */
    public static void well(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, WELL);
        g.fill(x, y, x + w - 1, y + 1, WELL_DARK);
        g.fill(x, y, x + 1, y + h - 1, WELL_DARK);
        g.fill(x + 1, y + h - 1, x + w, y + h, LIGHT);
        g.fill(x + w - 1, y + 1, x + w, y + h, LIGHT);
    }
}
