package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingtrapinch.tfcobblemon.belt.Belts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * La squadra non e' piu' grande di quello che il giocatore porta addosso.
 *
 * <p>La struttura resta di sei posti — toccarla romperebbe i salvataggi — ma
 * oltre il limite della cintura {@code add} risponde no, e chi chiama fa quello
 * che ha sempre fatto a squadra piena: manda nel PC. Il Pokemon non si perde,
 * semplicemente non e' a portata.
 *
 * <p>A giocatore offline non ci mettiamo in mezzo: uno scambio o un comando che
 * arriva mentre non c'e' nessuno non ha una cintura da guardare.
 */
@Mixin(PlayerPartyStore.class)
public abstract class PartyGateMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void tfcobblemon$soloQuantoLaCinturaConcede(Pokemon pokemon,
                                                        CallbackInfoReturnable<Boolean> callback) {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        final ServerPlayer player = server.getPlayerList()
                .getPlayer(((PlayerPartyStore) (Object) this).getPlayerUUID());
        if (player == null) {
            return;
        }
        if (((PartyStore) (Object) this).occupied() >= Belts.capacity(player)) {
            callback.setReturnValue(false);
        }
    }
}
