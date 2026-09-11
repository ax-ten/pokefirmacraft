package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.net.messages.server.SendOutPokemonPacket;
import com.cobblemon.mod.common.net.serverhandling.storage.SendOutPokemonHandler;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingtrapinch.tfcobblemon.belt.BallHandover;
import com.kingtrapinch.tfcobblemon.belt.Belts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * In campo va solo chi si ha addosso.
 *
 * <p>Le frecce e il tasto R dell'elenco a sinistra mandano in campo per numero
 * di posto, e non passano dal nostro tasto destro: era la via per cui un Pokemon
 * rimasto in squadra ma senza la sua ball addosso usciva comunque. Il cancello
 * va qui, dove la richiesta arriva.
 *
 * <p>In creativa non si applica.
 */
@Mixin(SendOutPokemonHandler.class)
public abstract class SendOutGateMixin {

    @Inject(method = "handle(Lcom/cobblemon/mod/common/net/messages/server/SendOutPokemonPacket;"
            + "Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At("HEAD"), cancellable = true)
    private void tfcobblemon$soloChiSiHaAddosso(SendOutPokemonPacket packet, MinecraftServer server,
                                                ServerPlayer player, CallbackInfo callback) {
        if (Belts.senzaLimiti(player)) {
            return;
        }
        final Pokemon mon = Cobblemon.INSTANCE.getStorage().getParty(player).get(packet.getSlot());
        if (mon == null || BallHandover.onBelt(player, mon.getUuid())) {
            return;
        }
        callback.cancel();
        player.displayClientMessage(Component.translatable(
                "tfcobblemon.ball.non_addosso", mon.getDisplayName(false))
                .withStyle(ChatFormatting.GRAY), true);
    }
}
