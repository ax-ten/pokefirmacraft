package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePCPokemonToPartyPacket;
import com.cobblemon.mod.common.net.serverhandling.storage.pc.MovePCPokemonToPartyHandler;
import com.kingtrapinch.tfcobblemon.belt.BallHandover;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ritirare dal PC e' riprendere il Pokemon in mano, quindi la ball torna —
 * quella vera, perche' Cobblemon si ricorda con che ball l'hai preso.
 *
 * <p>Senza questo il deposito sarebbe una trappola: depositi, ritiri, e ti
 * ritrovi un Pokemon che non hai modo di far uscire.
 */
@Mixin(MovePCPokemonToPartyHandler.class)
public abstract class PcWithdrawMixin {

    @Inject(method = "handle(Lcom/cobblemon/mod/common/net/messages/server/storage/pc/MovePCPokemonToPartyPacket;"
            + "Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At("TAIL"))
    private void tfcobblemon$rendiLaBall(MovePCPokemonToPartyPacket packet, MinecraftServer server,
                                         ServerPlayer player, CallbackInfo callback) {
        BallHandover.afterPc(player, () -> BallHandover.hand(player, packet.getPokemonID()));
    }
}
