package com.kingtrapinch.tfcobblemon.pasture;

import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.belt.BallLink;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Appendere una ball al pascolo. E' un click destro col ball in mano, e non
 * passa da {@code useWithoutItem} — quello scatta solo a mani vuote — percio'
 * si prende l'interazione prima che la ball faccia il suo mestiere, che
 * altrimenti manderebbe il Pokemon in campo.
 */
@EventBusSubscriber(modid = TFCobblemon.MODID)
public final class PastureEvents {
    private PastureEvents() {}

    @SubscribeEvent
    public static void laBallSiAppende(PlayerInteractEvent.RightClickBlock event) {
        final ItemStack cosa = event.getItemStack();
        if (!BallLink.filled(cosa)) {
            return;
        }
        final PokemonPastureBlockEntity pascolo =
                Pastures.pascolo(event.getLevel(), event.getPos());
        if (pascolo == null) {
            return;
        }
        // si ferma qui in ogni caso: col pascolo davanti, una ball non si lancia
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (Pastures.infila(player, pascolo, cosa)) {
            cosa.shrink(1);
            return;
        }
        if (Pastures.cesta(pascolo).size() >= pascolo.getMaxTethered()) {
            player.displayClientMessage(
                    Component.translatable("tfcobblemon.pasture.pieno", pascolo.getMaxTethered()), true);
        }
    }
}
