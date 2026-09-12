package com.kingtrapinch.tfcobblemon.pasture;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record PastureMenuProvider(BlockPos pos) implements MenuProvider {

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.tfcobblemon.pasture");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventario, Player player) {
        return new PastureMenu(id, inventario, pos);
    }
}
