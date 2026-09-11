package com.kingtrapinch.tfcobblemon.belt;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;

/**
 * La cintura da allenatore. Lo slot {@code belt} di Curios e' di dimensione uno
 * ed e' condiviso con altre mod, quindi la cintura non lo allarga: la cintura
 * <em>e'</em> l'oggetto indossato, e tiene lei le ball in un contenitore suo.
 *
 * <p>Senza cintura si indossa una ball nuda, e si ha un Pokemon a portata.
 */
public class TrainerBeltItem extends Item {
    private final int slots;

    public TrainerBeltItem(int slots, Properties properties) {
        super(properties.stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
        this.slots = slots;
    }

    public int slots() {
        return slots;
    }

    /** Le ball che la cintura sta portando, nell'ordine in cui compaiono. */
    public static List<ItemStack> carried(ItemStack belt) {
        final ItemContainerContents contents = belt.get(DataComponents.CONTAINER);
        return contents == null ? List.of() : contents.stream().toList();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        final long piene = carried(stack).stream().filter(s -> !s.isEmpty()).count();
        tooltip.add(Component.translatable("tooltip.tfcobblemon.trainer_belt", piene, slots)
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
