package com.kingtrapinch.tfcobblemon;

import com.kingtrapinch.tfcobblemon.belt.ModBallData;
import com.kingtrapinch.tfcobblemon.belt.ModBelt;
import com.kingtrapinch.tfcobblemon.dig.ModDig;
import com.kingtrapinch.tfcobblemon.fluid.ModFluids;
import com.kingtrapinch.tfcobblemon.glass.ModGlassOperations;
import com.kingtrapinch.tfcobblemon.item.ModCreativeModTabs;
import com.kingtrapinch.tfcobblemon.item.ModItems;
import com.kingtrapinch.tfcobblemon.item.custom.GolettItem;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(TFCobblemon.MODID)
public class TFCobblemon {
    public static final String MODID = "tfcobblemon";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TFCobblemon(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.register(modEventBus);
        ModCreativeModTabs.register(modEventBus);
        ModGlassOperations.register(modEventBus);
        ModBelt.register(modEventBus);
        ModBallData.register(modEventBus);
        ModDig.register(modEventBus);
        ModFluids.register(modEventBus);

        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(GolettItem.GOLETT);
        }
    }
}
