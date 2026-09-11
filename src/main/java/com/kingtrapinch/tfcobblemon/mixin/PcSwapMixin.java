package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.net.messages.server.storage.SwapPCPartyPokemonPacket;
import com.cobblemon.mod.common.net.serverhandling.storage.SwapPCPartyPokemonHandler;
import com.kingtrapinch.tfcobblemon.belt.BallHandover;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Uno scambio fra PC e squadra e' un deposito e un ritiro in un gesto solo, e le
 * ball si scambiano di conseguenza: sparisce quella di chi entra nel PC, torna
 * quella di chi ne esce.
 */
@Mixin(SwapPCPartyPokemonHandler.class)
public abstract class PcSwapMixin {

    @Inject(method = "handle(Lcom/cobblemon/mod/common/net/messages/server/storage/SwapPCPartyPokemonPacket;"
            + "Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At("TAIL"))
    private void tfcobblemon$scambiaLeBall(SwapPCPartyPokemonPacket packet, MinecraftServer server,
                                           ServerPlayer player, CallbackInfo callback) {
        BallHandover.afterPc(player, () -> {
            BallHandover.forget(player, packet.getPartyPokemonID());
            BallHandover.hand(player, packet.getPcPokemonID());
        });
    }
}
