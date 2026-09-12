package com.kingtrapinch.tfcobblemon.pasture;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.belt.BallLink;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Col pascolo davanti, una ball non si lancia: si apre la cesta.
 *
 * <p>{@code useWithoutItem} scatta solo a mani vuote, quindi con una ball in
 * mano il blocco non lo vedrebbe nessuno e il Pokemon partirebbe in campo. Gli
 * altri oggetti non hanno bisogno di questo: non facendo niente, il turno
 * torna al blocco da se'.
 */
@EventBusSubscriber(modid = TFCobblemon.MODID)
public final class PastureEvents {
    private PastureEvents() {}

    @SubscribeEvent
    public static void conLaBallSiApreLaCesta(PlayerInteractEvent.RightClickBlock event) {
        if (!BallLink.filled(event.getItemStack())) {
            return;
        }
        final var pascolo = Pastures.pascolo(event.getLevel(), event.getPos());
        // col modulo installato le ball non c'entrano piu' niente col pascolo
        if (pascolo == null || Pastures.collegato(pascolo)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getEntity() instanceof ServerPlayer player) {
            Pastures.apri(player, event.getLevel(), event.getPos());
        }
    }
}
