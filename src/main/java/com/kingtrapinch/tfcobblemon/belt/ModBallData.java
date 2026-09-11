package com.kingtrapinch.tfcobblemon.belt;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Il componente che la ball si porta addosso. */
public final class ModBallData {
    private ModBallData() {}

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, TFCobblemon.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BallLink>> BALL_LINK =
            COMPONENTS.register("ball_link", () -> DataComponentType.<BallLink>builder()
                    .persistent(BallLink.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(BallLink.CODEC))
                    .build());

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}
