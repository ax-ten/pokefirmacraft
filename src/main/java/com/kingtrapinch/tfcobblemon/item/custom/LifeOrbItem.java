package com.kingtrapinch.tfcobblemon.item.custom;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.util.ModSounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class LifeOrbItem extends Item {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TFCobblemon.MODID);

    private static final Properties LIFE_ORB_PROPERTIES = new Properties()
            .stacksTo(1)
            .fireResistant()
            .durability(100);

    public LifeOrbItem() {
        super(LIFE_ORB_PROPERTIES);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tfcobblemon.life_orb_charging"));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xE347C1;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        final ItemStack held = player.getItemInHand(hand);
        if (level.isClientSide || held.getDamageValue() > 0) {
            return InteractionResultHolder.pass(held);
        }

        final Item lifeOrb = BuiltInRegistries.ITEM.get(ResourceLocation.parse("cobblemon:life_orb"));
        if (lifeOrb == Items.AIR) {
            return InteractionResultHolder.pass(held);
        }

        final ItemStack finished = new ItemStack(lifeOrb);
        player.setItemInHand(hand, finished);
        ModSounds.playAt(level, player, "minecraft:entity.allay.ambient_without_item");
        return InteractionResultHolder.consume(finished);
    }

    public static final DeferredItem<Item> LIFE_ORB_CHARGING = ITEMS.register("life_orb_charging", LifeOrbItem::new);

    public static void registerAll(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
