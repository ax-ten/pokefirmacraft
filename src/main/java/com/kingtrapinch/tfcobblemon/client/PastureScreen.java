package com.kingtrapinch.tfcobblemon.client;

import com.kingtrapinch.tfcobblemon.pasture.BallBasket;
import com.kingtrapinch.tfcobblemon.pasture.PastureMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * La cesta del pascolo: due file da otto e l'inventario sotto, nella forma di
 * una cassa bassa. Disegnata e non ritagliata da un png, come la finestra dello
 * scavo: sono due riquadri e sedici conche.
 */
public class PastureScreen extends AbstractContainerScreen<PastureMenu> {

    private static final int COLONNE = 8;

    public PastureScreen(PastureMenu menu, Inventory inventario, Component titolo) {
        super(menu, inventario, titolo);
        this.imageWidth = 176;
        this.imageHeight = 150;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        final int x = leftPos;
        final int y = topPos;
        GuiFrame.panel(g, x, y, imageWidth, imageHeight);
        for (int i = 0; i < BallBasket.POSTI; i++) {
            GuiFrame.well(g, x + 16 + (i % COLONNE) * 18, y + 17 + (i / COLONNE) * 18, 18, 18);
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                GuiFrame.well(g, x + 7 + col * 18, y + 66 + row * 18, 18, 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            GuiFrame.well(g, x + 7 + col * 18, y + 124, 18, 18);
        }
    }
}
