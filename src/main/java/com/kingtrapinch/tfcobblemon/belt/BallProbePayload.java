package com.kingtrapinch.tfcobblemon.belt;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** "A che livello sta, adesso, questo Pokemon?" */
public record BallProbePayload(UUID pokemon) implements CustomPacketPayload {
    public static final Type<BallProbePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TFCobblemon.MODID, "ball_probe"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BallProbePayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, BallProbePayload::pokemon,
                    BallProbePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
