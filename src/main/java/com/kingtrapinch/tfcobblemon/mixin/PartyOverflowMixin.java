package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.kingtrapinch.tfcobblemon.belt.BeltParty;
import net.minecraft.core.RegistryAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Chi non entra in squadra va nel nostro box, non nel PC.
 *
 * <p>Cobblemon, a squadra piena, ripiega su {@code getOverflowPC}, che e' il PC
 * del giocatore. Ma il PC non esiste ancora per meta' partita, e riempirlo da
 * se' lo ridurrebbe a un magazzino automatico invece di un traguardo: i Pokemon
 * in eccesso stanno nelle loro ball, e il posto dove le ball tengono i dati e'
 * il nostro box.
 */
@Mixin(PlayerPartyStore.class)
public abstract class PartyOverflowMixin {

    @Inject(method = "getOverflowPC", at = @At("HEAD"), cancellable = true)
    private void tfcobblemon$nelNostroBox(RegistryAccess registryAccess,
                                          CallbackInfoReturnable<PCStore> callback) {
        callback.setReturnValue(BeltParty.deposito(
                ((PlayerPartyStore) (Object) this).getPlayerUUID(), registryAccess));
    }
}
