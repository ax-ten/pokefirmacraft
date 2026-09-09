package com.kingtrapinch.tfcobblemon.item.custom;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class BlankOrbItem extends Item {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TFCobblemon.MODID);

    private static final Properties ORB_PROPERTIES = new Properties()
            .stacksTo(1)
            .fireResistant();

    public BlankOrbItem() {
        super(ORB_PROPERTIES);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tfcobblemon.blank_orb"));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    public static final DeferredItem<Item> BLANK_ORB = ITEMS.register("blank_orb", BlankOrbItem::new);

    public static void registerAll(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
