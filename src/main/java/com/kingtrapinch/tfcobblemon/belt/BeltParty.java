package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * La squadra e' la cintura.
 *
 * <p>Cobblemon tiene una squadra di sei posti fissi, e quella struttura non si
 * tocca: i salvataggi esistenti la vogliono cosi'. Quello che cambia e' chi ci
 * puo' stare — solo i Pokemon delle ball che il giocatore porta addosso — e
 * quanti posti si vedono nell'elenco a sinistra.
 *
 * <p>L'allineamento si fa nei momenti in cui qualcosa cambia davvero: quando la
 * cintura si mette o si toglie, quando una ball entra o esce, quando si apre il
 * PC. Non a tempo: un controllo al secondo su ogni giocatore e' il modo piu'
 * sicuro di rovinare un server, e non c'e' niente da controllare fra un gesto e
 * l'altro.
 *
 * <p><b>Un Pokemon fuori dalla sua ball non si tocca.</b> Se e' nel mondo ce
 * l'hai messo tu, e spedirlo nel PC perche' la sua ball ha cambiato posto
 * svuoterebbe di senso l'averlo fuori. Si fa ordine solo al PC, dove ordine
 * serve: prima rientrano tutti, poi si allinea.
 */
public final class BeltParty {
    private BeltParty() {}

    /**
     * Le ball che il giocatore porta addosso, nell'ordine, coi buchi al loro
     * posto: il posto i della squadra spetta alla ball i della cintura.
     */
    public static UUID[] wanted(Player player) {
        final ItemStack nelloSlot = Belts.inBeltSlot(player);
        if (nelloSlot.getItem() instanceof TrainerBeltItem belt) {
            final List<ItemStack> posti = belt.posti(nelloSlot);
            final UUID[] voluti = new UUID[posti.size()];
            for (int i = 0; i < posti.size(); i++) {
                final BallLink legame = BallLink.read(posti.get(i));
                voluti[i] = legame == null ? null : legame.pokemon();
            }
            return voluti;
        }
        final BallLink sola = BallLink.read(nelloSlot);
        return sola == null ? new UUID[0] : new UUID[] {sola.pokemon()};
    }

    /**
     * Rimette la squadra d'accordo con la cintura: chi non ci sta va nel PC,
     * chi ci sta torna al suo posto. Chi e' fuori dalla sua ball resta dov'e'.
     */
    public static void align(ServerPlayer player) {
        // in combattimento la squadra e' quella registrata all'inizio: togliersi
        // la cintura a meta' scontro non cambia le carte in tavola
        if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
            return;
        }
        final PlayerPartyStore squadra = Cobblemon.INSTANCE.getStorage().getParty(player);
        final PCStore pc = Cobblemon.INSTANCE.getStorage().getPC(player);
        final UUID[] voluti = wanted(player);

        final Set<UUID> insieme = new HashSet<>();
        for (UUID id : voluti) {
            if (id != null) {
                insieme.add(id);
            }
        }

        // niente addosso, o niente dentro: non c'e' nulla a cui allinearsi, e il
        // posto che spetta comunque a tutti lo tiene il cancello su add. Senza
        // questo, chi gioca senza cintura si vedrebbe svuotare la squadra.
        if (insieme.isEmpty()) {
            return;
        }

        // chi non e' su una ball addosso non e' a portata — a meno che non sia
        // fuori, e allora e' affare del giocatore, non nostro
        for (int i = 0; i < squadra.size(); i++) {
            final Pokemon mon = squadra.get(i);
            if (mon == null || insieme.contains(mon.getUuid()) || mon.getEntity() != null) {
                continue;
            }
            squadra.remove(mon);
            pc.add(mon);
        }

        // e chi lo e' va al posto della sua ball
        for (int i = 0; i < voluti.length && i < squadra.size(); i++) {
            final UUID id = voluti[i];
            if (id == null) {
                continue;
            }
            final Pokemon giaAlPosto = squadra.get(i);
            if (giaAlPosto != null && giaAlPosto.getUuid().equals(id)) {
                continue;
            }
            final Pokemon altrove = squadra.get(id);
            if (altrove != null) {
                final int suo = indiceDi(squadra, altrove);
                if (suo >= 0) {
                    squadra.swap(new PartyPosition(i), new PartyPosition(suo));
                }
                continue;
            }
            final Pokemon nelPc = pc.get(id);
            if (nelPc != null) {
                pc.remove(nelPc);
                squadra.set(i, nelPc);
            }
        }
    }

    /**
     * Al PC si fa ordine: tutti dentro le proprie ball, poi si allinea. E' il
     * solo momento in cui si richiama d'ufficio, perche' e' il solo momento in
     * cui serve sapere con certezza dove sta ciascuno.
     */
    public static void tidy(ServerPlayer player) {
        if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
            return;
        }
        final PlayerPartyStore squadra = Cobblemon.INSTANCE.getStorage().getParty(player);
        for (int i = 0; i < squadra.size(); i++) {
            final Pokemon mon = squadra.get(i);
            if (mon != null && mon.getEntity() != null) {
                mon.recall();
            }
        }
        align(player);
    }

    private static int indiceDi(PlayerPartyStore squadra, Pokemon mon) {
        for (int i = 0; i < squadra.size(); i++) {
            if (squadra.get(i) == mon) {
                return i;
            }
        }
        return -1;
    }
}
