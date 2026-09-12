package com.kingtrapinch.tfcobblemon.dig;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

/**
 * La finestra dello scavo. La griglia non e' fatta di slot — sono celle di
 * terreno — ma i tesori si: uno slot per tesoro, messo dove il tesoro sta
 * sepolto, che compare solo quando le sue quattro celle sono pulite. Cosi' si
 * intravede dentro lo scavo e si trascina via da la', senza cassetti.
 */
public class DigMenu extends AbstractContainerMenu {
    /** Dove sta ogni tesoro nella griglia, nell'ordine del contenitore. */
    public record Spot(int x, int y) {}

    private final Container contents;
    @Nullable
    private final DigSiteBlockEntity site;
    private final Player chi;
    /** Arriva al client da se': e' il gioco a sincronizzare i DataSlot. */
    private final net.minecraft.world.inventory.DataSlot finito =
            net.minecraft.world.inventory.DataSlot.standalone();
    private final BlockPos pos;
    private final List<Spot> spots;

    public static DigMenu decode(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        final BlockPos pos = buf.readBlockPos();
        final int count = buf.readByte();
        final List<Spot> spots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            spots.add(new Spot(buf.readByte(), buf.readByte()));
        }
        return new DigMenu(id, inventory, pos, spots);
    }

    public DigMenu(int id, Inventory inventory, BlockPos pos, List<Spot> spots) {
        super(ModDig.DIG_MENU.get(), id);
        this.pos = pos;
        this.spots = List.copyOf(spots);
        final var be = inventory.player.level().getBlockEntity(pos);
        this.site = be instanceof DigSiteBlockEntity s ? s : null;
        this.chi = inventory.player;
        addDataSlot(finito);
        this.contents = site != null ? site.contents()
                : new SimpleContainer(DigSiteBlockEntity.TREASURES);

        final boolean client = inventory.player.level().isClientSide;
        for (int i = 0; i < this.spots.size(); i++) {
            final Spot spot = this.spots.get(i);
            final int index = i;
            // si intravede appena una cella e' pulita, ma si prende solo quando
            // il quadrato e' libero del tutto
            final IntPredicate glimpsed = client
                    ? unused -> clearAround(spot, false)
                    : unused -> site != null && site.glimpsed(index);
            final IntPredicate freed = client
                    ? unused -> clearAround(spot, true)
                    : unused -> site != null && site.exposed(index);
            addSlot(new TreasureSlot(contents, i,
                    DigLayout.treasureX(spot.x()), DigLayout.treasureY(spot.y()),
                    glimpsed, freed, site));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9,
                        8 + col * 18, DigLayout.INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, DigLayout.HOTBAR_Y));
        }
    }

    /** Sul client gli strati li sa {@link ClientDigState}, il block entity no. */
    private static boolean clearAround(Spot spot, boolean all) {
        int clean = 0;
        final int cells = DigSiteBlockEntity.TREASURE_SIZE * DigSiteBlockEntity.TREASURE_SIZE;
        for (int dy = 0; dy < DigSiteBlockEntity.TREASURE_SIZE; dy++) {
            for (int dx = 0; dx < DigSiteBlockEntity.TREASURE_SIZE; dx++) {
                if (ClientDigState.layer(spot.x() + dx, spot.y() + dy) == Layer.EMPTY) {
                    clean++;
                }
            }
        }
        return all ? clean == cells : clean > 0;
    }

    /** Uno slot che si intravede appena il terreno sopra comincia ad andare via. */
    public static class TreasureSlot extends Slot {
        private final IntPredicate glimpsed;
        private final IntPredicate freed;
        @Nullable
        private final DigSiteBlockEntity owner;

        TreasureSlot(Container container, int index, int x, int y,
                     IntPredicate glimpsed, IntPredicate freed,
                     @Nullable DigSiteBlockEntity owner) {
            super(container, index, x, y);
            this.glimpsed = glimpsed;
            this.freed = freed;
            this.owner = owner;
        }

        @Override
        public boolean isActive() {
            return glimpsed.test(index);
        }

        /** Ancora mezzo sepolto: si vede ma non si tira via. */
        public boolean stuck() {
            return !freed.test(index);
        }

        @Override
        public boolean mayPickup(Player player) {
            return freed.test(index);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        /**
         * Tirato fuori l'ultimo tesoro il sito non ha piu' niente da nascondere:
         * si chiude la finestra e il blocco torna quello normale di prima.
         */
        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            if (!player.level().isClientSide() && owner != null && owner.emptied()) {
                owner.finisci();
            }
        }
    }

    /**
     * Il mezzo secondo di festa. Il conto lo tiene questo metodo, che il gioco
     * chiama a ogni tick finche' la finestra e' aperta: scaduto, il blocco si
     * posa e la finestra si chiude.
     */
    @Override
    public void broadcastChanges() {
        if (site != null && !chi.level().isClientSide()) {
            finito.set(site.finito() ? 1 : 0);
        }
        super.broadcastChanges();
        if (site != null && site.finito() && site.scala()
                && chi instanceof net.minecraft.server.level.ServerPlayer giocatore
                && giocatore.level() instanceof net.minecraft.server.level.ServerLevel level) {
            site.settle(level);
            giocatore.closeContainer();
        }
    }

    /** A festa cominciata non si scava piu'. */
    @Override
    public void clicked(int slot, int bottone, net.minecraft.world.inventory.ClickType tipo,
                        Player player) {
        if (site != null && site.finito()) {
            return;
        }
        super.clicked(slot, bottone, tipo, player);
    }

    /** Se il sito ha finito e sta solo aspettando di chiudersi. */
    public boolean finito() {
        return finito.get() != 0;
    }

    public BlockPos pos() {
        return pos;
    }

    public List<Spot> spots() {
        return spots;
    }

    @Nullable
    public DigSiteBlockEntity site() {
        return site;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        final Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        final ItemStack stack = slot.getItem();
        final ItemStack copy = stack.copy();
        final int tesori = spots.size();
        if (index < tesori) {
            if (!moveItemStackTo(stack, tesori, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        // lo spostamento rapido non passa da Slot.onTake, quindi il controllo
        // "e' rimasto qualcosa?" va rifatto anche qui
        if (index < tesori && !player.level().isClientSide()
                && site != null && site.emptied()) {
            site.finisci();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return site != null && !site.isRemoved()
                && player.distanceToSqr(pos.getCenter()) <= 64.0;
    }
}
