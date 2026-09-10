package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.kingtrapinch.tfcobblemon.catching.ModCatchRates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Il moltiplicatore di una PokeBall e' immutabile e si costruisce in codice, non
 * dal datapack, quindi per le nostre ball lo si sostituisce qui.
 */
@Mixin(PokeBall.class)
public abstract class PokeBallMixin {

    @Inject(method = "getCatchRateModifier", at = @At("HEAD"), cancellable = true)
    private void tfcobblemon$ourCatchRates(CallbackInfoReturnable<CatchRateModifier> callback) {
        final CatchRateModifier ours = ModCatchRates.get(((PokeBall) (Object) this).getName());
        if (ours != null) {
            callback.setReturnValue(ours);
        }
    }
}
