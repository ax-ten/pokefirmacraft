package com.kingtrapinch.tfcobblemon.dig;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * La finestra dello scavo. La griglia non e' fatta di slot — sono celle di
 * terreno, non oggetti — quindi qui dentro ci sono solo il cassetto dei tesori
 * trovati e l'inventario del giocatore, che e' dove i tesori vanno trascinati.
 * Gli attrezzi restano nell'inventario e li si guarda da la'.
 */
public class DigMenu extends AbstractContainerMenu {
    private final Container found;
    @Nullable
    private final DigSiteBlockEntity site;
    private final BlockPos pos;

    public DigMenu(int id, Inventory inventory, BlockPos pos) {
        super(ModDig.DIG_MENU.get(), id);
        this.pos = pos;
        final var be = inventory.player.level().getBlockEntity(pos);
        this.site = be instanceof DigSiteBlockEntity s ? s : null;
        this.found = site != null ? site.found() : new SimpleContainer(DigSiteBlockEntity.TREASURES);

        for (int i = 0; i < DigSiteBlockEntity.TREASURES; i++) {
            addSlot(new Slot(found, i, 8 + i * 18, 122) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 150 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 208));
        }
    }

    public BlockPos pos() {
        return pos;
    }

    @Nullable
    public DigSiteBlockEntity site() {
        return site;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        final Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        final ItemStack stack = slot.getItem();
        final ItemStack copy = stack.copy();
        final int tesori = DigSiteBlockEntity.TREASURES;
        if (index < tesori) {
            if (!moveItemStackTo(stack, tesori, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, tesori, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return site != null && !site.isRemoved()
                && player.distanceToSqr(pos.getCenter()) <= 64.0;
    }
}
