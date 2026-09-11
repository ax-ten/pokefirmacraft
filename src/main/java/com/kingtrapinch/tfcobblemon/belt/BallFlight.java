package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonRecallEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonSentEvent;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * La ball che sta nell'inventario si lancia per davvero.
 *
 * <p>L'animazione di Cobblemon e' un lancio, e allora che sia un lancio: il
 * Pokemon esce e la sua ball lascia l'inventario. Quando rientra la ball torna
 * — nell'inventario se c'e' posto, altrimenti per terra dove stava. Cosi' non
 * serve un'icona di ball aperta per dire che e' fuori: la ball non c'e'.
 *
 * <p>Quella sulla cintura invece non si muove: il posto e' del suo Pokemon
 * anche mentre e' in campo, ed e' quello che tiene la squadra insieme.
 *
 * <p>Si aggancia agli eventi di Cobblemon e non al nostro tasto destro, perche'
 * in campo si manda anche dalle frecce e da R, e da li' non passiamo.
 */
public final class BallFlight {
    private BallFlight() {}

    public static void hook() {
        CobblemonEvents.POKEMON_SENT_POST.subscribe(BallFlight::uscito);
        CobblemonEvents.POKEMON_RECALL_PRE.subscribe(BallFlight::rientra);
    }

    private static void uscito(PokemonSentEvent.Post event) {
        final Pokemon mon = event.getPokemon();
        final ServerPlayer player = mon.getOwnerPlayer();
        if (player == null) {
            return;
        }
        if (BallHandover.onBelt(player, mon.getUuid())) {
            // addosso ci resta, ma segnata: finche' e' in campo non si sfila
            BallHandover.mark(player, mon.getUuid(), true);
            return;
        }
        BallHandover.takeFromInventory(player, mon.getUuid());
    }

    private static void rientra(PokemonRecallEvent.Pre event) {
        final Pokemon mon = event.getPokemon();
        final ServerPlayer player = mon.getOwnerPlayer();
        if (player == null) {
            return;
        }
        if (BallHandover.anywhere(player, mon.getUuid())) {
            BallHandover.mark(player, mon.getUuid(), false);
            return;
        }
        final PokemonEntity uscito = event.getOldEntity();
        final Vec3 dove = uscito == null ? player.position() : uscito.position();
        BallHandover.give(player, mon, dove);
    }
}
