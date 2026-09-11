package com.kingtrapinch.tfcobblemon.belt;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** "Fai rientrare questo." */
public record RecallPayload(UUID pokemon) implements CustomPacketPayload {
    public static final Type<RecallPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TFCobblemon.MODID, "recall"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RecallPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, RecallPayload::pokemon,
                    RecallPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
