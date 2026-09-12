package com.kingtrapinch.tfcobblemon.world;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Quello che mettiamo nel mondo di nostro. */
public final class ModWorld {
    private ModWorld() {}

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, TFCobblemon.MODID);

    public static final DeferredHolder<Feature<?>, GeodeTrailFeature> GEODE_TRAIL =
            FEATURES.register("geode_trail",
                    () -> new GeodeTrailFeature(GeodeTrailFeature.Config.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
