package com.kingtrapinch.tfcobblemon.dig;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public record DigMenuProvider(BlockPos pos) implements MenuProvider {
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.tfcobblemon.dig_site");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        final var be = player.level().getBlockEntity(pos);
        final java.util.List<DigMenu.Spot> spots = new java.util.ArrayList<>();
        if (be instanceof DigSiteBlockEntity site) {
            for (DigSiteBlockEntity.Buried b : site.buried()) {
                spots.add(new DigMenu.Spot(b.x(), b.y()));
            }
        }
        return new DigMenu(id, inventory, pos, spots);
    }
}
