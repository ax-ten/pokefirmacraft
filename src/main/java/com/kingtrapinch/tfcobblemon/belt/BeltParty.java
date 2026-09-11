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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

    /** Chi e' dentro un allineamento, per non rientrarci dai nostri eventi. */
    private static final Set<UUID> DENTRO = new HashSet<>();

    /**
     * Il deposito delle ball che non si portano addosso, e <b>non e' il PC.</b>
     *
     * <p>Cobblemon pretende che ogni Pokemon stia registrato in un deposito: uno
     * senza coordinate non e' "dentro la ball", e' in nessun posto, e non lo
     * salva nessuno. Ma usare il PC del giocatore per questo avrebbe una
     * conseguenza sbagliata: il giorno in cui il blocco PC esiste, dentro ci si
     * troverebbe tutto quello che si ha in una cassa, e il PC diventerebbe
     * l'indice gratuito di ogni Pokemon mai preso — il contrario del punto delle
     * ball fisiche.
     *
     * <p>Quindi un deposito nostro, dedicato e invisibile.
     * {@code getCustomStore} lo tiene in cache e lo salva su file come gli
     * altri, e la chiave e' un UUID derivato da quello del giocatore: il PC
     * cerca col suo UUID e questo non lo trova mai. Nel PC finisce solo quello
     * che il giocatore vi deposita di proposito.
     */
    public static PCStore deposito(ServerPlayer player) {
        final UUID chiave = UUID.nameUUIDFromBytes(
                ("tfcobblemon:balls/" + player.getUUID()).getBytes(StandardCharsets.UTF_8));
        return Cobblemon.INSTANCE.getStorage()
                .getCustomStore(PCStore.class, chiave, player.registryAccess());
    }

    /**
     * Il Pokemon puntato da una ball, cercato dove puo' stare: la squadra, il
     * nostro deposito, e il PC vero — perche' una ball resta valida anche per un
     * Pokemon che il giocatore ha depositato di sua mano.
     */
    public static Pokemon trova(ServerPlayer player, UUID pokemon) {
        final var storage = Cobblemon.INSTANCE.getStorage();
        Pokemon mon = storage.getParty(player).get(pokemon);
        if (mon == null) {
            mon = deposito(player).get(pokemon);
        }
        if (mon == null) {
            mon = storage.getPC(player).get(pokemon);
        }
        return mon;
    }

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
        if (BattleRegistry.getBattleByParticipatingPlayer(player) != null
                || Belts.senzaLimiti(player)) {
            return;
        }
        // i nostri stessi spostamenti fanno scattare eventi che tornano qui
        if (!DENTRO.add(player.getUUID())) {
            return;
        }
        try {
            allinea(player);
        } finally {
            DENTRO.remove(player.getUUID());
        }
    }

    /**
     * Il riallineamento vero, in tre passaggi separati e in quest'ordine.
     *
     * <p>L'ordine non e' estetica, e' l'unica cosa che lo rende sicuro. Le tre
     * operazioni di Cobblemon si comportano cosi':
     * <ul>
     *   <li>{@code set} su un posto <b>occupato</b> togli chi c'era <em>da
     *       questo store e da nessun altro</em>: il precedente occupante finisce
     *       senza coordinate, cioe' in nessun deposito. Qui {@code set} si usa
     *       <b>solo su posti liberi</b>.
     *   <li>{@code remove} lascia il Pokemon senza coordinate: va sempre seguito
     *       da un {@code add} che riesca, altrimenti si e' perso. Se il PC
     *       rifiuta, si rimette dove stava.
     *   <li>{@code swap} non distrugge niente, ed e' il solo modo di mettere in
     *       ordine.
     * </ul>
     */
    private static void allinea(ServerPlayer player) {
        final PlayerPartyStore squadra = Cobblemon.INSTANCE.getStorage().getParty(player);
        final PCStore pc = deposito(player);
        final UUID[] voluti = wanted(player);

        final Set<UUID> insieme = new HashSet<>();
        for (UUID id : voluti) {
            if (id != null) {
                insieme.add(id);
            }
        }

        // 1. escono quelli di cui non si porta la ball. Chi e' in campo no: ce
        //    l'ha messo il giocatore, e non e' affare nostro.
        for (int i = 0; i < squadra.size(); i++) {
            final Pokemon mon = squadra.get(i);
            if (mon == null || insieme.contains(mon.getUuid()) || mon.getEntity() != null) {
                continue;
            }
            squadra.remove(mon);
            if (pc.get(mon.getUuid()) == null && !pc.add(mon)) {
                // il deposito non lo accetta: meglio a portata che in nessun
                // posto. Il posto e' appena stato liberato, quindi rientra li'.
                squadra.set(i, mon);
            }
        }

        // 2. entrano quelli di cui si porta la ball, in un posto libero: cosi'
        //    set() non ha niente da distruggere. Se i posti finiscono si
        //    fermano fuori, che e' un dispiacere e non un danno.
        for (UUID id : voluti) {
            if (id == null || indiceDi(squadra, id) >= 0) {
                continue;
            }
            final Pokemon nelPc = pc.get(id);
            if (nelPc == null || !id.equals(nelPc.getUuid())) {
                continue;
            }
            final int libero = primoLibero(squadra);
            if (libero < 0) {
                break;
            }
            pc.remove(nelPc);
            squadra.set(libero, nelPc);
        }

        // 3. e vanno in ordine, solo scambiandosi di posto
        for (int i = 0; i < voluti.length && i < squadra.size(); i++) {
            final UUID id = voluti[i];
            if (id == null) {
                continue;
            }
            final int dove = indiceDi(squadra, id);
            if (dove < 0 || dove == i) {
                continue;
            }
            squadra.swap(new PartyPosition(i), new PartyPosition(dove));
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
                // con l'animazione: sfilarsi la cintura non fa svanire i Pokemon
                // dal mondo, li fa rientrare. Il rientro finisce qualche tick
                // dopo, e il riallineamento lo fa POKEMON_RECALL_POST.
                mon.tryRecallWithAnimation();
            }
        }
        align(player);
    }

    /**
     * Tira in squadra un Pokemon che non ci sta, per farlo uscire come
     * <b>compagno di viaggio</b>: una cavalcatura, o qualcuno che ti tiene
     * compagnia, la cui ball non e' sulla cintura.
     *
     * <p>Deve passare dalla squadra e non puo' restare nel deposito, e il motivo
     * e' nel modo in cui Cobblemon decide di chi e' un Pokemon:
     * {@code getOwnerUUID()} lo ricava dal deposito in cui sta — per un PCStore
     * restituisce l'UUID <em>del deposito</em>. Il nostro box ha una chiave
     * derivata, quindi un Pokemon che vive li' non risulta di nessun giocatore,
     * e {@code mobInteract} di PokemonEntity confronta proprio quell'UUID con
     * quello di chi interagisce: non si potrebbe ne' cavalcare ne' toccare.
     *
     * <p>Uno solo per volta, e serve un posto libero in squadra: la cintura
     * piena non lascia spazio per un compagno di viaggio, ed e' un prezzo
     * giusto. Rientrando, l'allineamento lo rimanda nel box da se', perche' la
     * sua ball non e' addosso.
     */
    public static Pokemon comeSupporto(ServerPlayer player, UUID pokemon) {
        if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
            return null;
        }
        final PlayerPartyStore squadra = Cobblemon.INSTANCE.getStorage().getParty(player);
        // un compagno per volta: chi c'e' gia' fuori senza la ball addosso
        // rientra prima, altrimenti se ne accumulerebbero quanti sono i posti
        for (int i = 0; i < squadra.size(); i++) {
            final Pokemon fuori = squadra.get(i);
            if (fuori != null && fuori.getEntity() != null
                    && !BallHandover.onBelt(player, fuori.getUuid())) {
                fuori.tryRecallWithAnimation();
                return null;
            }
        }
        final int libero = primoLibero(squadra);
        if (libero < 0) {
            return null;
        }
        final PCStore box = deposito(player);
        final Pokemon mon = box.get(pokemon);
        if (mon == null || !pokemon.equals(mon.getUuid())) {
            return null;
        }
        box.remove(mon);
        squadra.set(libero, mon);
        return mon;
    }

    /**
     * Fa rientrare tutti i Pokemon che stavano in quello che si e' appena
     * sfilato. Togliersi la cintura non lascia i Pokemon in giro per il mondo
     * senza piu' un posto dove tornare: prima rientrano, poi si allinea.
     */
    public static void recallAll(ServerPlayer player, ItemStack cosa) {
        for (UUID id : contenuti(cosa)) {
            final Pokemon mon = Cobblemon.INSTANCE.getStorage().getParty(player).get(id);
            if (mon != null && mon.getEntity() != null) {
                // con l'animazione: sfilarsi la cintura non fa svanire i Pokemon
                // dal mondo, li fa rientrare. Il rientro finisce qualche tick
                // dopo, e il riallineamento lo fa POKEMON_RECALL_POST.
                mon.tryRecallWithAnimation();
            }
        }
    }

    /** Gli UUID che un oggetto porta: una ball sola, o quelli di una cintura. */
    private static List<UUID> contenuti(ItemStack cosa) {
        final BallLink sola = BallLink.read(cosa);
        if (sola != null) {
            return List.of(sola.pokemon());
        }
        if (!(cosa.getItem() instanceof TrainerBeltItem belt)) {
            return List.of();
        }
        final List<UUID> dentro = new ArrayList<>();
        for (ItemStack ball : belt.posti(cosa)) {
            final BallLink legame = BallLink.read(ball);
            if (legame != null) {
                dentro.add(legame.pokemon());
            }
        }
        return dentro;
    }

    /**
     * Dove sta questo Pokemon nella squadra, scorrendo i posti. Non si usa
     * {@code get(UUID)}: quella passa da un indice interno che gli spostamenti
     * fra depositi possono lasciare stantio, e qui una risposta sbagliata
     * significa perdere un Pokemon.
     */
    private static int indiceDi(PlayerPartyStore squadra, UUID id) {
        for (int i = 0; i < squadra.size(); i++) {
            final Pokemon mon = squadra.get(i);
            if (mon != null && id.equals(mon.getUuid())) {
                return i;
            }
        }
        return -1;
    }

    private static int primoLibero(PlayerPartyStore squadra) {
        for (int i = 0; i < squadra.size(); i++) {
            if (squadra.get(i) == null) {
                return i;
            }
        }
        return -1;
    }
}
