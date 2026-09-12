package com.kingtrapinch.tfcobblemon.pasture;

import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Il posto del modulo, visto come contenitore da uno. Non tiene niente di suo:
 * legge e scrive il modulo dentro la block entity del pascolo.
 */
public class PastureModule implements Container {

    @Nullable
    private final PokemonPastureBlockEntity pascolo;

    public PastureModule(@Nullable PokemonPastureBlockEntity pascolo) {
        this.pascolo = pascolo;
    }

    private BallBasket cesta() {
        return (BallBasket) (Object) pascolo;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getItem(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return pascolo == null ? ItemStack.EMPTY : cesta().tfcobblemon$modulo();
    }

    @Override
    public ItemStack removeItem(int slot, int quanti) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack cosa) {
        if (pascolo != null) {
            cesta().tfcobblemon$modulo(cosa);
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void setChanged() {
        if (pascolo != null) {
            pascolo.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return pascolo != null;
    }

    @Override
    public void clearContent() {
        setItem(0, ItemStack.EMPTY);
    }
}
