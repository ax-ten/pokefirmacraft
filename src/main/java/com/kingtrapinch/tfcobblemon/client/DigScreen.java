package com.kingtrapinch.tfcobblemon.client;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.dig.ClientDigState;
import com.kingtrapinch.tfcobblemon.dig.DigMenu;
import com.kingtrapinch.tfcobblemon.dig.DigPayload;
import com.kingtrapinch.tfcobblemon.dig.DigSite;
import com.kingtrapinch.tfcobblemon.dig.DigTool;
import com.kingtrapinch.tfcobblemon.dig.Layer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo scavo. La griglia non e' fatta di slot, sono celle di terreno disegnate a
 * mano: si sceglie l'attrezzo dalla fila in alto, che elenca quelli che il
 * giocatore ha in inventario, e si picchia sulla cella.
 */
public class DigScreen extends AbstractContainerScreen<DigMenu> {
    private static final int CELL = 16;
    private static final int GRID_X = 20;
    private static final int GRID_Y = 32;

    private static ResourceLocation cella(String nome) {
        return ResourceLocation.fromNamespaceAndPath(TFCobblemon.MODID, "textures/gui/dig/" + nome + ".png");
    }

    private static final ResourceLocation ROCK = cella("rock");
    private static final ResourceLocation LIME = cella("lime");
    private static final ResourceLocation DUST = cella("dust");
    private static final ResourceLocation EMPTY = cella("empty");

    /** Un attrezzo trovato in inventario: quale tipo, e in che slot sta. */
    private record Handy(DigTool tool, int slot, ItemStack stack) {}

    private final List<Handy> handy = new ArrayList<>();
    private int chosen;

    public DigScreen(DigMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 232;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        refresh();
    }

    private void refresh() {
        handy.clear();
        final Inventory inv = menu.slots.get(menu.slots.size() - 1).container instanceof Inventory i
                ? i : minecraft.player.getInventory();
        for (DigTool tool : DigTool.values()) {
            for (int slot = 0; slot < inv.getContainerSize(); slot++) {
                final ItemStack stack = inv.getItem(slot);
                if (tool.matches(stack)) {
                    handy.add(new Handy(tool, slot, stack));
                    break;
                }
            }
        }
        if (chosen >= handy.size()) {
            chosen = 0;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        final int x = leftPos;
        final int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFF8B8B8B);
        graphics.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, 0xFFC6C6C6);

        // la griglia
        for (int gy = 0; gy < DigSite.SIZE; gy++) {
            for (int gx = 0; gx < DigSite.SIZE; gx++) {
                final Layer layer = ClientDigState.layer(gx, gy);
                final ResourceLocation tex = switch (layer) {
                    case ROCK -> ROCK;
                    case LIME -> LIME;
                    case DUST -> DUST;
                    case EMPTY -> EMPTY;
                };
                graphics.blit(tex, x + GRID_X + gx * CELL, y + GRID_Y + gy * CELL,
                        0, 0, CELL, CELL, CELL, CELL);
            }
        }
        graphics.renderOutline(x + GRID_X - 1, y + GRID_Y - 1,
                DigSite.SIZE * CELL + 2, DigSite.SIZE * CELL + 2, 0xFF373737);

        // gli attrezzi in mano, in fila sopra la griglia
        for (int i = 0; i < handy.size(); i++) {
            final int tx = x + GRID_X + i * 20;
            final int ty = y + 8;
            graphics.fill(tx - 2, ty - 2, tx + 18, ty + 18, i == chosen ? 0xFFFFF0A0 : 0xFF6A6A6A);
            graphics.renderItem(handy.get(i).stack(), tx, ty);
            graphics.renderItemDecorations(font, handy.get(i).stack(), tx, ty);
        }

        // la barra del sito
        final int barW = DigSite.SIZE * CELL;
        final int left = Math.max(0, ClientDigState.durability()) * barW / DigSite.DURABILITY;
        final int by = y + GRID_Y + DigSite.SIZE * CELL + 4;
        graphics.fill(x + GRID_X, by, x + GRID_X + barW, by + 5, 0xFF373737);
        graphics.fill(x + GRID_X, by, x + GRID_X + left, by + 5, 0xFF6ABE30);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, Component.translatable("tfcobblemon.dig.site",
                        ClientDigState.durability(), DigSite.DURABILITY),
                GRID_X, GRID_Y + DigSite.SIZE * CELL + 12, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        refresh();
        for (int i = 0; i < handy.size(); i++) {
            final int tx = leftPos + GRID_X + i * 20;
            final int ty = topPos + 8;
            if (mouseX >= tx - 2 && mouseX < tx + 18 && mouseY >= ty - 2 && mouseY < ty + 18) {
                chosen = i;
                return true;
            }
        }
        final int gx = (int) ((mouseX - leftPos - GRID_X) / CELL);
        final int gy = (int) ((mouseY - topPos - GRID_Y) / CELL);
        if (gx >= 0 && gx < DigSite.SIZE && gy >= 0 && gy < DigSite.SIZE && !handy.isEmpty()) {
            final Handy pick = handy.get(chosen);
            PacketDistributor.sendToServer(new DigPayload(
                    DigSite.index(gx, gy), pick.tool().ordinal(), pick.slot()));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        ClientDigState.reset();
        super.onClose();
    }
}
