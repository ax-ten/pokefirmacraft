package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingtrapinch.tfcobblemon.pasture.Pastures;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Un Pokemon al pascolo lo si cerca anche nel deposito del pascolo.
 *
 * <p>Il legame fa {@code getPC(pcId).get(pokemonId)}: una riga, ed e' il cardine
 * di tutto, perche' il controllo periodico scioglie i legami il cui Pokemon non
 * si trova piu'. Da noi il Pokemon non e' nel PC — sta nel
 * {@code PastureStore}, che porta lo stesso UUID ma e' un'altra classe, quindi
 * un altro file — e senza questo ripiego ogni Pokemon appeso al pascolo
 * risulterebbe scomparso entro due secondi.
 */
@Mixin(PokemonPastureBlockEntity.Tethering.class)
public abstract class PastureLookupMixin {

    @Inject(method = "getPokemon", at = @At("RETURN"), cancellable = true)
    private void tfcobblemon$anchePascolo(CallbackInfoReturnable<Pokemon> callback) {
        if (callback.getReturnValue() != null) {
            return;
        }
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        final PokemonPastureBlockEntity.Tethering legame =
                (PokemonPastureBlockEntity.Tethering) (Object) this;
        callback.setReturnValue(Pastures.cerca(
                Pastures.store(legame.getPcId(), server.registryAccess()), legame.getPokemonId()));
    }
}
