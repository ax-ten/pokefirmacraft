package com.kingtrapinch.tfcobblemon.dig;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Lo stato del sito che il client deve disegnare: uno strato per cella piu' la
 * durabilita' che resta. Sono sessantacinque byte, tanto vale mandarli tutti
 * dopo ogni colpo invece di tenere il conto delle differenze.
 */
public record DigSyncPayload(byte[] layers, int durability) implements CustomPacketPayload {
    public static final Type<DigSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TFCobblemon.MODID, "dig_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DigSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BYTE_ARRAY, DigSyncPayload::layers,
                    ByteBufCodecs.VAR_INT, DigSyncPayload::durability,
                    DigSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
