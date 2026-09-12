package com.kingtrapinch.tfcobblemon.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;

public record ZoneMenuProvider(BlockPos pos, ZoneKind genere,
                               List<ZoneMenu.Riga> righe, int sacchi) implements MenuProvider {

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.tfcobblemon.zone." + genere.nome());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventario, Player player) {
        return new ZoneMenu(id, inventario, pos, righe, sacchi);
    }
}
