package com.kingtrapinch.tfcobblemon.item.custom;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.util.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

    /** L'orb parte scarico: i 100 punti di durabilita' sono i Pokemon da abbattere. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        final ItemStack held = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(held);
        }

        final ItemStack charging = new ItemStack(LifeOrbItem.LIFE_ORB_CHARGING.get());
        charging.setDamageValue(charging.getMaxDamage());
        player.setItemInHand(hand, charging);
        ModSounds.playAt(level, player, "minecraft:entity.allay.ambient_with_item");
        return InteractionResultHolder.consume(charging);
    }

    public static final DeferredItem<Item> BLANK_ORB = ITEMS.register("blank_orb", BlankOrbItem::new);

    public static void registerAll(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
