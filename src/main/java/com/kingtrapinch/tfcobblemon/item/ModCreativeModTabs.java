package com.kingtrapinch.tfcobblemon.item;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.item.custom.BlankOrbItem;
import com.kingtrapinch.tfcobblemon.item.custom.GolettItem;
import com.kingtrapinch.tfcobblemon.item.custom.LifeOrbItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TFCobblemon.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> POKEFIRMACRAFT_TAB =
            CREATIVE_MODE_TABS.register("tfcobblemon_tab",
                    () -> CreativeModeTab.builder()
                            .icon(() -> new ItemStack(GolettItem.GOLETT.get()))
                            .title(Component.translatable("creativetab.tfcobblemon_tab"))
                            .displayItems((parameters, output) -> {
                                output.accept(BlankOrbItem.BLANK_ORB.get());
                                output.accept(LifeOrbItem.LIFE_ORB_CHARGING.get());
                                output.accept(ModItems.RAW_PROTECTOR.get());
                                output.accept(ModBallParts.RAW_TUMBLESTONE.get());
                                output.accept(ModBallParts.RAW_SKY_TUMBLESTONE.get());
                                output.accept(ModBallParts.RAW_BLACK_TUMBLESTONE.get());
                                output.accept(ModBallParts.TUMBLESTONE_POWDER.get());

                                output.accept(GolettItem.GOLETT.get());
                                output.accept(GolettItem.GOLETT_BLACK.get());
                                output.accept(GolettItem.GOLETT_BLUE.get());
                                output.accept(GolettItem.GOLETT_CYAN.get());
                                output.accept(GolettItem.GOLETT_GRAY.get());
                                output.accept(GolettItem.GOLETT_GREEN.get());
                                output.accept(GolettItem.GOLETT_LIGHTBLUE.get());
                                output.accept(GolettItem.GOLETT_LIME.get());
                                output.accept(GolettItem.GOLETT_MAGENTA.get());
                                output.accept(GolettItem.GOLETT_ORANGE.get());
                                output.accept(GolettItem.GOLETT_PINK.get());
                                output.accept(GolettItem.GOLETT_PURPLE.get());
                                output.accept(GolettItem.GOLETT_RED.get());
                                output.accept(GolettItem.GOLETT_WHITE.get());
                                output.accept(GolettItem.GOLETT_YELLOW.get());

                                com.kingtrapinch.tfcobblemon.block.ModBags.tutti()
                                        .forEach(sacco -> output.accept(sacco.get()));
                                com.kingtrapinch.tfcobblemon.zone.ModZones.tutte()
                                        .forEach(zona -> output.accept(zona.get()));
                                output.accept(com.kingtrapinch.tfcobblemon.pasture.ModPasture.PC_LINK.get());
                                com.kingtrapinch.tfcobblemon.block.ModOres.MINERALI.values()
                                        .forEach(minerale -> output.accept(minerale.get()));

                                com.kingtrapinch.tfcobblemon.dig.ModDig.allSites()
                                        .forEach(sito -> output.accept(sito.get()));
                            })
                            .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
