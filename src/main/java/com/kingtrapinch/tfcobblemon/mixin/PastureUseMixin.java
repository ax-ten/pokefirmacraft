package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.block.PastureBlock;
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.kingtrapinch.tfcobblemon.pasture.Pastures;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A mani vuote il pascolo non apre niente: sgancia l'ultima ball appesa.
 *
 * <p>La schermata del PC in modalita' pascolo e' la strada di Cobblemon per
 * metterci dentro i Pokemon, e da noi non serve: il deposito da cui pescare e'
 * la cintura, e quello che si appende al blocco e' una ball.
 */
@Mixin(PastureBlock.class)
public abstract class PastureUseMixin {

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void tfcobblemon$nienteSchermata(BlockState state, Level level, BlockPos pos, Player player,
                                             BlockHitResult hit,
                                             CallbackInfoReturnable<InteractionResult> callback) {
        callback.setReturnValue(InteractionResult.SUCCESS);
        if (!(player instanceof ServerPlayer giocatore)) {
            return;
        }
        final PokemonPastureBlockEntity pascolo = Pastures.pascolo(level, pos);
        if (pascolo == null) {
            return;
        }
        final ItemStack ball = Pastures.sfila(giocatore, pascolo);
        Pastures.rendi(giocatore, ball);
    }
}
