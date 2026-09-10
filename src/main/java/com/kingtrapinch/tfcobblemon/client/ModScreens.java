package com.kingtrapinch.tfcobblemon.client;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.dig.ModDig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = TFCobblemon.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModScreens {
    private ModScreens() {}

    @SubscribeEvent
    public static void register(RegisterMenuScreensEvent event) {
        event.register(ModDig.DIG_MENU.get(), DigScreen::new);
    }
}
