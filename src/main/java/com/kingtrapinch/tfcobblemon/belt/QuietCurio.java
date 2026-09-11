package com.kingtrapinch.tfcobblemon.belt;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.List;

/**
 * Un curio che non dice niente. Curios aggiunge da se' la riga "Slot: belt" a
 * ogni oggetto indossabile: con una cinquantina di ball diventa rumore, e
 * {@code getSlotsTooltip} e' un metodo default, quindi basta restituire la
 * lista come arriva.
 */
public record QuietCurio(ItemStack stack) implements ICurio {
    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, Item.TooltipContext context) {
        return tooltips;
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips) {
        return tooltips;
    }
}
