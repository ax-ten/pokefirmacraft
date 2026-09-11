package com.kingtrapinch.tfcobblemon.belt;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** La risposta: il livello vero, quello di ora. */
public record BallLevelPayload(UUID pokemon, int level) implements CustomPacketPayload {
    public static final Type<BallLevelPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TFCobblemon.MODID, "ball_level"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BallLevelPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, BallLevelPayload::pokemon,
                    ByteBufCodecs.VAR_INT, BallLevelPayload::level,
                    BallLevelPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
