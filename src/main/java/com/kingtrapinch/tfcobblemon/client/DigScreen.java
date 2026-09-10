package com.kingtrapinch.tfcobblemon.client;

import com.kingtrapinch.tfcobblemon.dig.ClientDigState;
import com.kingtrapinch.tfcobblemon.dig.DigLayout;
import com.kingtrapinch.tfcobblemon.dig.DigMenu;
import com.kingtrapinch.tfcobblemon.dig.DigPayload;
import com.kingtrapinch.tfcobblemon.dig.DigSite;
import com.kingtrapinch.tfcobblemon.dig.DigTool;
import com.kingtrapinch.tfcobblemon.dig.Layer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo scavo. La griglia e' disegnata a mano — sono celle di terreno, non slot —
 * e i tesori compaiono dentro la griglia quando il terreno sopra e' via.
 */
public class DigScreen extends AbstractContainerScreen<DigMenu> {
    private static final int BODY = 0xFFC6C6C6;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int SHADE = 0xFF555555;
    private static final int WELL = 0xFF8B8B8B;
    private static final int WELL_DARK = 0xFF373737;

    /** Un attrezzo trovato in inventario: quale tipo, e in che slot sta. */
    private record Handy(DigTool tool, int slot, ItemStack stack) {}

    private final List<Handy> handy = new ArrayList<>();
    private DigSkin skin = DigSkin.of(net.minecraft.resources.ResourceLocation
            .fromNamespaceAndPath("tfcobblemon", "suspicious_gravel/granite"));
    /** Nessun attrezzo scelto: sulla griglia non si combina niente. */
    private int chosen = -1;

    public DigScreen(DigMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = DigLayout.WIDTH;
        this.imageHeight = DigLayout.HEIGHT;
        this.inventoryLabelY = DigLayout.INV_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
        final var block = minecraft.level.getBlockState(menu.pos()).getBlock();
        final var id = BuiltInRegistries.BLOCK.getKey(block);
        skin = DigSkin.of(id);
        ClientDigState.kind(com.kingtrapinch.tfcobblemon.dig.SiteKind.of(id.getPath()));
        refresh();
    }

    private void refresh() {
        handy.clear();
        final Inventory inv = minecraft.player.getInventory();
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
            chosen = -1;
        }
    }

    /** Il pannello alla maniera vanilla: corpo chiaro, luce sopra, ombra sotto. */
    private static void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, BODY);
        g.fill(x, y, x + w - 1, y + 1, LIGHT);
        g.fill(x, y, x + 1, y + h - 1, LIGHT);
        g.fill(x + 1, y + h - 1, x + w, y + h, SHADE);
        g.fill(x + w - 1, y + 1, x + w, y + h, SHADE);
    }

    /** Una conca da slot: scura sopra a sinistra, chiara sotto a destra. */
    private static void well(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, WELL);
        g.fill(x, y, x + w - 1, y + 1, WELL_DARK);
        g.fill(x, y, x + 1, y + h - 1, WELL_DARK);
        g.fill(x + 1, y + h - 1, x + w, y + h, LIGHT);
        g.fill(x + w - 1, y + 1, x + w, y + h, LIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        final int x = leftPos;
        final int y = topPos;
        panel(graphics, x, y, imageWidth, imageHeight);

        // le celle, attaccate: nessuna griglia di separazione in mezzo
        for (int gy = 0; gy < DigSite.SIZE; gy++) {
            for (int gx = 0; gx < DigSite.SIZE; gx++) {
                graphics.blit(skin.forCell(ClientDigState.layer(gx, gy), ClientDigState.depthAt(gx, gy)),
                        x + DigLayout.cellX(gx), y + DigLayout.cellY(gy),
                        0, 0, DigLayout.CELL, DigLayout.CELL, 16, 16);
            }
        }

        // gli attrezzi che il giocatore ha addosso
        for (int i = 0; i < handy.size(); i++) {
            final int tx = x + DigLayout.TOOLS_X + i * 20;
            final int ty = y + DigLayout.TOOLS_Y;
            well(graphics, tx, ty, 18, 18);
            if (i == chosen) {
                graphics.renderOutline(tx - 1, ty - 1, 20, 20, 0xFFFFF0A0);
            }
        }

        // le conche dell'inventario
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                well(graphics, x + 7 + col * 18, y + DigLayout.INV_Y - 1 + row * 18, 18, 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            well(graphics, x + 7 + col * 18, y + DigLayout.HOTBAR_Y - 1, 18, 18);
        }

        // la barra del sito
        final int left = Math.max(0, ClientDigState.durability()) * DigLayout.GRID_SPAN
                / DigSite.DURABILITY;
        final int by = y + DigLayout.BAR_Y;
        graphics.fill(x + DigLayout.GRID_X, by, x + DigLayout.GRID_X + DigLayout.GRID_SPAN, by + 5, WELL_DARK);
        graphics.fill(x + DigLayout.GRID_X, by, x + DigLayout.GRID_X + left, by + 5, 0xFF6ABE30);
    }

    /** Le celle che l'attrezzo scelto colpirebbe da qui. */
    private void preview(GuiGraphics graphics, int mouseX, int mouseY) {
        if (chosen < 0 || chosen >= handy.size()) {
            return;
        }
        final int gx = (int) ((mouseX - leftPos - DigLayout.GRID_X) / (double) DigLayout.CELL);
        final int gy = (int) ((mouseY - topPos - DigLayout.GRID_Y) / (double) DigLayout.CELL);
        if (gx < 0 || gx >= DigSite.SIZE || gy < 0 || gy >= DigSite.SIZE) {
            return;
        }
        final DigTool tool = handy.get(chosen).tool();
        final int offset = tool.size % 2 == 0 ? 0 : tool.size / 2;
        for (int dy = 0; dy < tool.size; dy++) {
            for (int dx = 0; dx < tool.size; dx++) {
                final int cx = gx - offset + dx;
                final int cy = gy - offset + dy;
                if (cx < 0 || cx >= DigSite.SIZE || cy < 0 || cy >= DigSite.SIZE) {
                    continue;
                }
                final boolean bites = tool.bites(ClientDigState.layer(cx, cy));
                graphics.fill(leftPos + DigLayout.cellX(cx), topPos + DigLayout.cellY(cy),
                        leftPos + DigLayout.cellX(cx) + DigLayout.CELL,
                        topPos + DigLayout.cellY(cy) + DigLayout.CELL,
                        bites ? 0x60FFFFFF : 0x50FF4040);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        preview(graphics, mouseX, mouseY);
        for (int i = 0; i < handy.size(); i++) {
            final int tx = leftPos + DigLayout.TOOLS_X + i * 20 + 1;
            final int ty = topPos + DigLayout.TOOLS_Y + 1;
            graphics.renderItem(handy.get(i).stack(), tx, ty);
            graphics.renderItemDecorations(font, handy.get(i).stack(), tx, ty);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        refresh();
        for (int i = 0; i < handy.size(); i++) {
            final int tx = leftPos + DigLayout.TOOLS_X + i * 20;
            final int ty = topPos + DigLayout.TOOLS_Y;
            if (mouseX >= tx && mouseX < tx + 18 && mouseY >= ty && mouseY < ty + 18) {
                chosen = i;
                return true;
            }
        }
        // uno slot col tesoro dentro vince sulla cella: si trascina, non si picchia
        if (getSlotUnderMouse() == null || !getSlotUnderMouse().isActive()) {
            final int gx = (int) ((mouseX - leftPos - DigLayout.GRID_X) / DigLayout.CELL);
            final int gy = (int) ((mouseY - topPos - DigLayout.GRID_Y) / DigLayout.CELL);
            if (gx >= 0 && gx < DigSite.SIZE && gy >= 0 && gy < DigSite.SIZE
                    && chosen >= 0 && chosen < handy.size()) {
                final Handy pick = handy.get(chosen);
                PacketDistributor.sendToServer(new DigPayload(
                        DigSite.index(gx, gy), pick.tool().ordinal(), pick.slot()));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        ClientDigState.reset();
        super.onClose();
    }
}
