package com.kingtrapinch.tfcobblemon.client;

import com.kingtrapinch.tfcobblemon.dig.ClientDigState;
import com.kingtrapinch.tfcobblemon.dig.DigLayout;
import com.kingtrapinch.tfcobblemon.dig.DigMenu;
import com.kingtrapinch.tfcobblemon.dig.DigPayload;
import com.kingtrapinch.tfcobblemon.dig.DigSite;
import com.kingtrapinch.tfcobblemon.dig.DigSiteBlockEntity;
import com.kingtrapinch.tfcobblemon.dig.DigTool;
import com.kingtrapinch.tfcobblemon.dig.Layer;
import com.kingtrapinch.tfcobblemon.dig.SiteKind;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Lo scavo. La griglia e' disegnata a mano — sono celle di terreno, non slot —
 * e l'attrezzo si scegle direttamente nell'inventario, dove tutto quello che
 * non serve a scavare resta ingrigito.
 */
public class DigScreen extends AbstractContainerScreen<DigMenu> {
    private static final int WELL_DARK = 0xFF373737;
    /** Il velo su quello che non si puo' usare per scavare. */
    private static final int GREYED = 0xB0202020;

    /** I dieci stadi di rottura di vanilla: ne pesco uno per cella, stabile. */
    private static final ResourceLocation[] CRACKS = new ResourceLocation[10];

    static {
        for (int i = 0; i < CRACKS.length; i++) {
            CRACKS[i] = ResourceLocation.withDefaultNamespace("textures/block/destroy_stage_" + i + ".png");
        }
    }

    /** Un numero stabile per cella, per non far ballare crepe e bordi. */
    private static int scramble(int x, int y) {
        int h = x * 374761393 + y * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        return (h ^ (h >>> 16)) & 0x7FFFFFFF;
    }

    private final DigDust dust = new DigDust();

    /** Quanto dura la comparsa della scritta di fine, in millesimi. */
    private static final int DISSOLVENZA = 250;

    /** Quando la scritta di fine e' comparsa, per la dissolvenza. */
    private long apparso;

    /** Le scintille di un tesoro non vengono da una texture: sono luce. */
    private static final int[] SCINTILLE = {0xFFF0A0, 0xFFFFD0, 0xFFE070};
    private DigSkin skin = DigSkin.of(ResourceLocation
            .fromNamespaceAndPath("tfcobblemon", "suspicious_gravel/granite"));
    /** Lo slot dell'inventario da cui viene l'attrezzo scelto, o -1. */
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
        final var id = BuiltInRegistries.BLOCK.getKey(minecraft.level.getBlockState(menu.pos()).getBlock());
        skin = DigSkin.of(id);
        ClientDigState.kind(SiteKind.of(id.getPath()));
    }

    private static DigTool toolOf(ItemStack stack) {
        for (DigTool tool : DigTool.values()) {
            if (tool.matches(stack)) {
                return tool;
            }
        }
        return null;
    }

    private DigTool selected() {
        if (chosen < 0) {
            return null;
        }
        return toolOf(minecraft.player.getInventory().getItem(chosen));
    }

    /**
     * L'ombra sui lati dove la zolla si affaccia su una cella piu' scavata.
     * E' il trucco delle connected texture ridotto all'osso: non servono
     * quarantasette tasselli, basta sapere quali vicini sono piu' bassi perche'
     * il bordo dell'isola venga irregolare da solo.
     */
    private void edges(GuiGraphics g, int gx, int gy, int px, int py) {
        final int mine = ClientDigState.depthAt(gx, gy);
        final int c = DigLayout.CELL;
        for (int[] d : new int[][] {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}) {
            final int nx = gx + d[0];
            final int ny = gy + d[1];
            final boolean lower = nx < 0 || nx >= DigSite.SIZE || ny < 0 || ny >= DigSite.SIZE
                    ? false : ClientDigState.depthAt(nx, ny) > mine;
            if (!lower) {
                continue;
            }
            // il lato in ombra e' quello verso il basso e verso destra
            final int alpha = d[0] > 0 || d[1] > 0 ? 0x66000000 : 0x33000000;
            // lo spessore cambia pixel per pixel: il bordo viene sinuoso invece
            // che una riga dritta, e resta stabile perche' dipende dalla cella
            final int seme = scramble(gx * 4 + d[0], gy * 4 + d[1]);
            for (int i = 0; i < c; i++) {
                final int spessore = 1 + ((seme >>> (i % 24)) & 1) + ((seme >>> ((i * 3) % 24)) & 1);
                if (d[1] < 0) {
                    g.fill(px + i, py, px + i + 1, py + spessore, alpha);
                } else if (d[1] > 0) {
                    g.fill(px + i, py + c - spessore, px + i + 1, py + c, alpha);
                } else if (d[0] < 0) {
                    g.fill(px, py + i, px + spessore, py + i + 1, alpha);
                } else {
                    g.fill(px + c - spessore, py + i, px + c, py + i + 1, alpha);
                }
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        final int x = leftPos;
        final int y = topPos;
        GuiFrame.panel(graphics, x, y, imageWidth, imageHeight);
        // la griglia va incassata, altrimenti galleggia sul pannello
        GuiFrame.well(graphics, x + DigLayout.GRID_X - 2, y + DigLayout.GRID_Y - 2,
                DigLayout.GRID_SPAN + 4, DigLayout.GRID_SPAN + 4);

        for (int gy = 0; gy < DigSite.SIZE; gy++) {
            for (int gx = 0; gx < DigSite.SIZE; gx++) {
                final int px = x + DigLayout.cellX(gx);
                final int py = y + DigLayout.cellY(gy);
                final Layer layer = ClientDigState.layer(gx, gy);
                graphics.pose().pushPose();
                final int giri = scramble(gx, gy) % 4;
                graphics.pose().translate(px + DigLayout.CELL / 2.0, py + DigLayout.CELL / 2.0, 0);
                graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(giri * 90F));
                graphics.pose().translate(-DigLayout.CELL / 2.0, -DigLayout.CELL / 2.0, 0);
                graphics.blit(skin.forCell(layer, ClientDigState.depthAt(gx, gy)),
                        0, 0, 0, 0, DigLayout.CELL, DigLayout.CELL, 16, 16);
                graphics.pose().popPose();
                if (layer == Layer.EMPTY) {
                    // il fondo e' la stessa roccia, ma in ombra: si vede che non si scava
                    graphics.fill(px, py, px + DigLayout.CELL, py + DigLayout.CELL, 0xA0101014);
                }
                if (ClientDigState.isCracked(gx, gy)) {
                    // le crepe sono in trasparenza: senza blend coprono la zolla
                    com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                    graphics.blit(CRACKS[4 + scramble(gx, gy) % 6], px, py,
                            0, 0, DigLayout.CELL, DigLayout.CELL, 16, 16);
                    com.mojang.blaze3d.systems.RenderSystem.disableBlend();
                }
                edges(graphics, gx, gy, px, py);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                GuiFrame.well(graphics, x + 7 + col * 18, y + DigLayout.INV_Y - 1 + row * 18, 18, 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            GuiFrame.well(graphics, x + 7 + col * 18, y + DigLayout.HOTBAR_Y - 1, 18, 18);
        }

        final int resta = Math.max(0, ClientDigState.durability());
        final int left = resta * DigLayout.GRID_SPAN / DigSite.DURABILITY;
        final int by = y + DigLayout.BAR_Y;
        // la barra in una conca come gli slot, altrimenti galleggia sul pannello
        GuiFrame.well(graphics, x + DigLayout.GRID_X - 1, by - 1, DigLayout.GRID_SPAN + 2, 7);
        graphics.fill(x + DigLayout.GRID_X, by, x + DigLayout.GRID_X + DigLayout.GRID_SPAN, by + 5, WELL_DARK);
        graphics.fill(x + DigLayout.GRID_X, by, x + DigLayout.GRID_X + left, by + 5, 0xFF6ABE30);
        // quanto se ne mangerebbe il prossimo colpo, in coda alla parte piena:
        // cosi' si vede se il sito regge un'altra martellata prima di darla
        final int costo = previewCost();
        if (costo > 0 && resta > 0) {
            final int morso = Math.min(left, costo * DigLayout.GRID_SPAN / DigSite.DURABILITY);
            graphics.fill(x + DigLayout.GRID_X + left - morso, by,
                    x + DigLayout.GRID_X + left, by + 5, 0xFFD9552B);
        }
    }

    /**
     * I tesori nella buca si vedono in grande, ma solo dove il terreno e' stato
     * tolto: l'oggetto si disegna una volta per cella pulita, ritagliato su
     * quella cella. Quello che sta ancora sotto la zolla resta nascosto, cosi'
     * si capisce da guardarlo che c'e' altro da scavare. Presi, tornano
     * normali, perche' li disegna lo slot dell'inventario.
     */
    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot) {
        if (slot.index >= menu.spots().size()
                || !(slot.container instanceof net.minecraft.world.SimpleContainer)) {
            super.renderSlot(graphics, slot);
            return;
        }
        final ItemStack stack = slot.getItem();
        if (stack.isEmpty()) {
            return;
        }
        final DigMenu.Spot spot = menu.spots().get(slot.index);
        final int side = DigSiteBlockEntity.TREASURE_SIZE;
        for (int dy = 0; dy < side; dy++) {
            for (int dx = 0; dx < side; dx++) {
                if (ClientDigState.layer(spot.x() + dx, spot.y() + dy) != Layer.EMPTY) {
                    continue;
                }
                final int cx = leftPos + DigLayout.cellX(spot.x() + dx);
                final int cy = topPos + DigLayout.cellY(spot.y() + dy);
                graphics.enableScissor(cx, cy, cx + DigLayout.CELL, cy + DigLayout.CELL);
                graphics.pose().pushPose();
                graphics.pose().translate(slot.x + 8, slot.y + 8, 0);
                graphics.pose().scale(1.5F, 1.5F, 1.0F);
                graphics.pose().translate(-8, -8, 0);
                graphics.renderItem(stack, 0, 0);
                graphics.pose().popPose();
                graphics.disableScissor();
            }
        }
    }

    /**
     * Quanto sito costerebbe un colpo con l'attrezzo scelto: e' lo stesso conto
     * che fa il server, tenuto qui per poterlo mostrare prima di picchiare.
     * La spazzola consuma solo una volta su due, quindi si conta come uno.
     */
    private int previewCost() {
        final DigTool tool = selected();
        if (tool == null) {
            return 0;
        }
        if (tool == DigTool.BRUSH) {
            return 1;
        }
        final int base = tool.siteCost(minecraft.player.getInventory().getItem(chosen));
        return tool == DigTool.HAMMER || tool == DigTool.PICKAXE
                ? base * ClientDigState.kind().hammerPenalty : base;
    }

    /** Le celle che l'attrezzo colpirebbe da qui. */
    private void preview(GuiGraphics graphics, int mouseX, int mouseY) {
        final DigTool tool = selected();
        if (tool == null) {
            return;
        }
        final int gx = (int) ((mouseX - leftPos - DigLayout.GRID_X) / (double) DigLayout.CELL);
        final int gy = (int) ((mouseY - topPos - DigLayout.GRID_Y) / (double) DigLayout.CELL);
        if (gx < 0 || gx >= DigSite.SIZE || gy < 0 || gy >= DigSite.SIZE) {
            return;
        }
        for (int[] cell : tool.area(gx, gy)) {
            final int cx = cell[0];
            final int cy = cell[1];
            if (cx < 0 || cx >= DigSite.SIZE || cy < 0 || cy >= DigSite.SIZE) {
                continue;
            }
            if (!tool.bites(ClientDigState.layer(cx, cy))) {
                continue;
            }
            graphics.fill(leftPos + DigLayout.cellX(cx), topPos + DigLayout.cellY(cy),
                    leftPos + DigLayout.cellX(cx) + DigLayout.CELL,
                    topPos + DigLayout.cellY(cy) + DigLayout.CELL, 0x38FFFFFF);
        }
    }

    /** Il velo su tutto quello che non e' un attrezzo da scavo. */
    private void greyOut(GuiGraphics graphics) {
        for (Slot slot : menu.slots) {
            if (!(slot.container instanceof Inventory)) {
                continue;
            }
            final DigTool tool = toolOf(slot.getItem());
            if (tool == null) {
                graphics.fill(leftPos + slot.x, topPos + slot.y,
                        leftPos + slot.x + 16, topPos + slot.y + 16, GREYED);
                graphics.fill(leftPos + slot.x, topPos + slot.y,
                        leftPos + slot.x + 16, topPos + slot.y + 16, 0x50808080);
            } else if (slot.getContainerSlot() == chosen) {
                graphics.renderOutline(leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18, 0xFFFFF0A0);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (int[] cell : ClientDigState.drainBroken()) {
            final int prima = cell[2];
            final Layer caduto = ClientDigState.kind().materialAt(prima);
            dust.burst(leftPos + DigLayout.cellX(cell[0]), topPos + DigLayout.cellY(cell[1]),
                    DigLayout.CELL, DigPalette.of(skin.forCell(caduto, prima)));
        }
        // un tesoro appena venuto fuori: scintille chiare sopra il punto giusto
        final int trovato = ClientDigState.drainFound();
        if (trovato >= 0 && trovato < menu.spots().size()) {
            final DigMenu.Spot spot = menu.spots().get(trovato);
            for (int i = 0; i < 3; i++) {
                dust.burst(leftPos + DigLayout.treasureX(spot.x()),
                        topPos + DigLayout.treasureY(spot.y()), 16, SCINTILLE);
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        greyOut(graphics);
        if (!menu.finito()) {
            preview(graphics, mouseX, mouseY);
        }
        dust.render(graphics);
        cursorTool(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
        finito(graphics);
    }

    /**
     * L'attrezzo scelto segue il cursore, cosi' si sa con cosa si sta
     * picchiando senza andare a guardare l'inventario. Se il giocatore ha
     * qualcosa in mano lascia stare: al cursore ci pensa vanilla.
     */
    private void cursorTool(GuiGraphics graphics, int mouseX, int mouseY) {
        if (chosen < 0 || !menu.getCarried().isEmpty()) {
            return;
        }
        final ItemStack stack = minecraft.player.getInventory().getItem(chosen);
        if (toolOf(stack) == null) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        graphics.renderItem(stack, mouseX + 2, mouseY + 2);
        graphics.pose().popPose();
    }

    /**
     * Lo scavo e' finito: un secondo di scritta in mezzo alla griglia, poi la
     * finestra si chiude da se'. Il conto lo tiene il server; qui si disegna, e
     * la comparsa e' una dissolvenza.
     *
     * <p>La dissolvenza va a orologio del client e non a tick: un tick e'
     * cinquanta millesimi, e a quella grana una comparsa in un quarto di
     * secondo verrebbe a cinque scatti invece che liscia.
     */
    private void finito(GuiGraphics graphics) {
        if (!menu.finito()) {
            apparso = 0L;
            return;
        }
        if (apparso == 0L) {
            apparso = net.minecraft.Util.getMillis();
        }
        final float quanto = Math.min(1.0F, (net.minecraft.Util.getMillis() - apparso) / (float) DISSOLVENZA);
        final int x = leftPos + DigLayout.GRID_X;
        final int y = topPos + DigLayout.GRID_Y;
        final int meta = y + DigLayout.GRID_SPAN / 2;
        graphics.fill(x, meta - 12, x + DigLayout.GRID_SPAN, meta + 12,
                (int) (0xC0 * quanto) << 24 | 0x101014);
        final Component detto = Component.translatable("tfcobblemon.dig.completed");
        graphics.drawCenteredString(font, detto, x + DigLayout.GRID_SPAN / 2, meta - 4,
                (int) (0xFF * quanto) << 24 | 0xFFFF55);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.finito()) {
            return true;
        }
        final Slot under = getSlotUnderMouse();
        if (under != null && under.container instanceof Inventory && toolOf(under.getItem()) != null) {
            // riclicco sullo stesso e lo depongo: senza attrezzo in mano si
            // guarda il sito senza rischiare di picchiarlo per sbaglio
            chosen = under.getContainerSlot() == chosen ? -1 : under.getContainerSlot();
            return true;
        }
        // su un tesoro ancora mezzo sepolto si continua a scavare, non si trascina
        final boolean daPrendere = under instanceof DigMenu.TreasureSlot t
                && !t.stuck() && under.hasItem();
        if (!daPrendere) {
            final int gx = (int) ((mouseX - leftPos - DigLayout.GRID_X) / DigLayout.CELL);
            final int gy = (int) ((mouseY - topPos - DigLayout.GRID_Y) / DigLayout.CELL);
            final DigTool tool = selected();
            if (tool != null && gx >= 0 && gx < DigSite.SIZE && gy >= 0 && gy < DigSite.SIZE) {
                PacketDistributor.sendToServer(new DigPayload(
                        DigSite.index(gx, gy), tool.ordinal(), chosen));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        ClientDigState.reset();
        dust.clear();
        super.onClose();
    }
}
