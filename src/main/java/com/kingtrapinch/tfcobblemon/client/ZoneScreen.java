package com.kingtrapinch.tfcobblemon.client;

import com.kingtrapinch.tfcobblemon.block.ModBags;
import com.kingtrapinch.tfcobblemon.zone.ZoneMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * Il quadro comandi di una zona: una riga per Pokemon, e in fondo
 * l'interruttore che disegna l'area.
 *
 * <p>Disegnata e non ritagliata da un png, come le altre finestre nostre.
 */
public class ZoneScreen extends AbstractContainerScreen<ZoneMenu> {

    private static final int RIGA = 18;
    private static final int CIMA = 20;

    public ZoneScreen(ZoneMenu menu, Inventory inventario, Component titolo) {
        super(menu, inventario, titolo);
        this.imageWidth = 190;
        this.imageHeight = CIMA + ZoneMenu.RIGHE * RIGA + 34;
    }

    @Override
    protected void init() {
        super.init();
        final List<ZoneMenu.Riga> righe = menu.righe();
        for (int i = 0; i < righe.size(); i++) {
            final int riga = i;
            addRenderableWidget(Button.builder(scritta(riga), bottone -> {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, riga);
                        bottone.setMessage(scritta(riga));
                    })
                    .bounds(leftPos + 96, topPos + CIMA + riga * RIGA, 84, 16)
                    .build());
        }
        addRenderableWidget(Button.builder(area(), bottone -> {
                    ZoneOverlay.gira();
                    bottone.setMessage(area());
                })
                .bounds(leftPos + 8, topPos + imageHeight - 26, 172, 18)
                .build());
    }

    private Component area() {
        return Component.translatable(ZoneOverlay.mostra()
                ? "tfcobblemon.zone.area_si" : "tfcobblemon.zone.area_no");
    }

    /** Cosa c'e' scritto sul bottone di una riga. */
    private Component scritta(int riga) {
        final int stato = menu.stato(riga);
        if (stato == ZoneMenu.FERMO) {
            return Component.translatable("tfcobblemon.zone.fermo");
        }
        if (stato == ZoneMenu.COMEVIENE) {
            return Component.translatable("tfcobblemon.zone.come_viene");
        }
        final List<String> nomi = ModBags.STATS.keySet().stream().toList();
        final String statistica = nomi.get(Math.floorMod(stato, nomi.size()));
        return Component.translatable("block.tfcobblemon.punching_bag." + statistica);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        GuiFrame.panel(g, leftPos, topPos, imageWidth, imageHeight);
        final List<ZoneMenu.Riga> righe = menu.righe();
        for (int i = 0; i < ZoneMenu.RIGHE; i++) {
            GuiFrame.well(g, leftPos + 7, topPos + CIMA + i * RIGA - 1, 90, 18);
            if (i < righe.size()) {
                final ZoneMenu.Riga riga = righe.get(i);
                g.drawString(font, riga.nome(), leftPos + 11, topPos + CIMA + i * RIGA + 4,
                        0xFF404040, false);
                g.drawString(font, "Lv." + riga.livello(), leftPos + 74,
                        topPos + CIMA + i * RIGA + 4, 0xFF707070, false);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0xFF404040, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        // lo stato di una riga puo' cambiare dal server: le scritte si rinfrescano
        int i = 0;
        for (var widget : renderables) {
            if (widget instanceof Button bottone && i < menu.righe().size()) {
                bottone.setMessage(scritta(i));
                i++;
            }
        }
    }
}
