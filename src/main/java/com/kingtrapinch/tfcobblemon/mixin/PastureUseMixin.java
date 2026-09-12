package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.block.PastureBlock;
import com.kingtrapinch.tfcobblemon.pasture.Pastures;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Il pascolo non apre piu' il PC: apre la sua cesta.
 *
 * <p>La schermata del PC in modalita' pascolo e' la strada di Cobblemon per
 * metterci dentro i Pokemon, e presuppone di avere un PC. Da noi si aprono
 * sedici caselle e ci si mettono le ball che si hanno addosso.
 *
 * <p>Salvo che il pascolo abbia il <b>modulo</b>: da quel momento e' collegato
 * al PC ed e' giusto che si apra il loro, quindi qui si lascia correre.
 */
@Mixin(PastureBlock.class)
public abstract class PastureUseMixin {

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void tfcobblemon$apriLaCesta(BlockState state, Level level, BlockPos pos, Player player,
                                         BlockHitResult hit,
                                         CallbackInfoReturnable<InteractionResult> callback) {
        // col modulo installato il pascolo torna quello di Cobblemon: si lascia
        // correre il loro metodo, che apre il PC
        final var pascolo = Pastures.pascolo(level, pos);
        if (pascolo != null && Pastures.collegato(pascolo)) {
            return;
        }
        callback.setReturnValue(InteractionResult.SUCCESS);
        if (player instanceof ServerPlayer giocatore) {
            Pastures.apri(giocatore, level, pos);
        }
    }
}
