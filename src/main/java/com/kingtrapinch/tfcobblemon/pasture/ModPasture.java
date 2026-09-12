package com.kingtrapinch.tfcobblemon.pasture;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Quello che serve registrare per il pascolo: solo la sua finestra. */
public final class ModPasture {
    private ModPasture() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, TFCobblemon.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<PastureMenu>> PASTURE_MENU =
            MENUS.register("pasture", () -> IMenuTypeExtension.create(PastureMenu::decode));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
