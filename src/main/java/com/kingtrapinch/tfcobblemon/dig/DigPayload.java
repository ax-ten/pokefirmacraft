package com.kingtrapinch.tfcobblemon.dig;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Un colpo: quale cella, con quale attrezzo, e con quale stack dell'inventario. */
public record DigPayload(int cell, int tool, int slot) implements CustomPacketPayload {
    public static final Type<DigPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TFCobblemon.MODID, "dig"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DigPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, DigPayload::cell,
                    ByteBufCodecs.VAR_INT, DigPayload::tool,
                    ByteBufCodecs.VAR_INT, DigPayload::slot,
                    DigPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
