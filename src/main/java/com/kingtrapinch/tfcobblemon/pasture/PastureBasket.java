package com.kingtrapinch.tfcobblemon.pasture;

import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.kingtrapinch.tfcobblemon.belt.BallLink;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * La cesta di un pascolo vista come contenitore, cioe' quello che serve alla
 * finestra. Non tiene niente di suo: e' una veste sulla lista che il mixin ha
 * attaccato alla block entity.
 */
public class PastureBasket implements Container {

    private final PokemonPastureBlockEntity pascolo;
    private final NonNullList<ItemStack> ball;

    public PastureBasket(PokemonPastureBlockEntity pascolo) {
        this.pascolo = pascolo;
        this.ball = Pastures.cesta(pascolo);
    }

    @Override
    public int getContainerSize() {
        return ball.size();
    }

    @Override
    public boolean isEmpty() {
        return ball.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return ball.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int quanti) {
        return ContainerHelper.removeItem(ball, slot, quanti);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(ball, slot);
    }

    @Override
    public void setItem(int slot, ItemStack cosa) {
        ball.set(slot, cosa);
    }

    /** Una casella per ball: impilarle nasconderebbe dei Pokemon. */
    @Override
    public int getMaxStackSize() {
        return 1;
    }

    /** Ci va solo una ball con un Pokemon dentro. */
    @Override
    public boolean canPlaceItem(int slot, ItemStack cosa) {
        return BallLink.filled(cosa);
    }

    @Override
    public void setChanged() {
        pascolo.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return pascolo.getLevel() != null
                && pascolo.getLevel().getBlockEntity(pascolo.getBlockPos()) == pascolo
                && player.distanceToSqr(pascolo.getBlockPos().getCenter()) <= 64.0;
    }

    @Override
    public void clearContent() {
        ball.clear();
    }
}
