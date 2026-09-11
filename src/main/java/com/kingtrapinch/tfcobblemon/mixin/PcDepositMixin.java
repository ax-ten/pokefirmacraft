package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePartyPokemonToPCPacket;
import com.cobblemon.mod.common.net.serverhandling.storage.pc.MovePartyPokemonToPCHandler;
import com.kingtrapinch.tfcobblemon.belt.BallHandover;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Depositare nel PC e' consegnare il Pokemon, quindi la sua ball non serve piu'.
 *
 * <p>Il gancio sta sull'azione e non sullo stato: cosi' si distingue un deposito
 * voluto da un Pokemon che finisce nel PC per altre strade — una cattura a
 * cintura piena, per esempio, dove la ball invece te la tieni.
 */
@Mixin(MovePartyPokemonToPCHandler.class)
public abstract class PcDepositMixin {

    @Inject(method = "handle(Lcom/cobblemon/mod/common/net/messages/server/storage/pc/MovePartyPokemonToPCPacket;"
            + "Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At("TAIL"))
    private void tfcobblemon$viaLaBall(MovePartyPokemonToPCPacket packet, MinecraftServer server,
                                       ServerPlayer player, CallbackInfo callback) {
        BallHandover.forget(player, packet.getPokemonID());
    }
}
