package com.kingtrapinch.tfcobblemon.item;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.item.custom.BlankOrbItem;
import com.kingtrapinch.tfcobblemon.item.custom.GolettItem;
import com.kingtrapinch.tfcobblemon.item.custom.LifeOrbItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TFCobblemon.MODID);

    public static final DeferredItem<Item> RAW_PROTECTOR = ITEMS.register("raw_protector",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        GolettItem.registerAll(eventBus);
        LifeOrbItem.registerAll(eventBus);
        BlankOrbItem.registerAll(eventBus);
        ModBallParts.register(eventBus);
    }
}
