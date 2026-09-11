package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonRecallEvent;
import com.cobblemon.mod.common.api.events.battles.BattleStartedEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonSentEvent;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.level.ServerPlayer;

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
        CobblemonEvents.POKEMON_RECALL_POST.subscribe(BallFlight::rientrato);
        CobblemonEvents.BATTLE_STARTED_PRE.subscribe(BallFlight::siCombatte);
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
        BallHandover.takeLoose(player, mon.getUuid());
    }

    /**
     * A rientro finito la squadra si rimette in pari. Serve perche' il rientro
     * con animazione finisce qualche tick dopo il gesto che l'ha chiesto: fino a
     * quel momento il Pokemon ha ancora un'entita' nel mondo e l'allineamento lo
     * salta di proposito, quindi senza questo resterebbe in squadra dopo che la
     * sua cintura e' stata sfilata.
     */
    /**
     * Comincia uno scontro: il compagno di viaggio rientra e lascia il campo a
     * chi si ha in squadra. Una cavalcatura non e' un combattente, e trovarsela
     * in campo al posto del Pokemon scelto sarebbe un modo eccellente di
     * perdere uno scontro.
     *
     * <p><b>A meno che non sia l'unico che si ha.</b> Chi va a zonzo con una
     * ball sola in tasca e nessuna sulla cintura si ritroverebbe a combattere
     * senza niente: in quel caso il compagno di viaggio <em>e'</em> la squadra.
     */
    private static void siCombatte(BattleStartedEvent.Pre event) {
        for (BattleActor attore : event.getBattle().getActors()) {
            if (!(attore instanceof PlayerBattleActor giocatore)) {
                continue;
            }
            final ServerPlayer player = giocatore.getEntity();
            if (player == null) {
                continue;
            }
            final PlayerPartyStore squadra = Cobblemon.INSTANCE.getStorage().getParty(player);
            Pokemon compagno = null;
            int inSquadra = 0;
            for (int i = 0; i < squadra.size(); i++) {
                final Pokemon mon = squadra.get(i);
                if (mon == null) {
                    continue;
                }
                if (BallHandover.onBelt(player, mon.getUuid())) {
                    inSquadra++;
                } else if (mon.getEntity() != null) {
                    compagno = mon;
                }
            }
            if (compagno != null && inSquadra > 0) {
                compagno.tryRecallWithAnimation();
            }
        }
    }

    private static void rientrato(PokemonRecallEvent.Post event) {
        if (event.getOldEntity() == null) {
            return;
        }
        final ServerPlayer player = event.getPokemon().getOwnerPlayer();
        if (player != null) {
            BeltParty.align(player);
        }
    }

    private static void rientra(PokemonRecallEvent.Pre event) {
        final PokemonEntity uscito = event.getOldEntity();
        if (uscito == null) {
            // Non e' un rientro: e' contabilita'. PokemonStore.remove(Pokemon)
            // chiama Pokemon.recall() su qualunque Pokemon tolga da uno store, e
            // recall() emette questo evento come sua prima istruzione senza
            // guardare se c'e' un'entita' da richiamare. Quindi ogni spostamento
            // fra squadra e PC — anche quelli di Cobblemon, come il ritiro dal
            // PC — passava da qui e si faceva coniare una ball.
            return;
        }
        final Pokemon mon = event.getPokemon();
        final ServerPlayer player = mon.getOwnerPlayer();
        if (player == null) {
            return;
        }
        if (BallHandover.anywhere(player, mon.getUuid())) {
            BallHandover.mark(player, mon.getUuid(), false);
            return;
        }
        BallHandover.give(player, mon, uscito.position());
    }
}
