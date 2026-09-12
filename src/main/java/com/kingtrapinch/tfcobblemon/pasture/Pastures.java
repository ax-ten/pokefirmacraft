package com.kingtrapinch.tfcobblemon.pasture;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.PokemonStore;
import com.cobblemon.mod.common.api.scheduling.SchedulingFunctionsKt;
import com.cobblemon.mod.common.api.storage.StoreCoordinates;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.block.PastureBlock;
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingtrapinch.tfcobblemon.belt.BallLink;
import com.kingtrapinch.tfcobblemon.belt.BeltParty;
import kotlin.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Il pascolo come cesta di ball.
 *
 * <p>Cobblemon lo ha fatto come finestra sul PC: al click apre la schermata del
 * PC in modalita' pascolo e da li' si trascinano dentro i Pokemon. Da noi non
 * c'e' nessuna schermata. Si appende una ball al blocco e il Pokemon esce a
 * pascolare, la si stacca e rientra. La ball <em>e'</em> l'interfaccia, come
 * sulla cintura.
 *
 * <p>Sotto, quello che si sposta non e' il Pokemon ma la sua residenza: dal box
 * invisibile al {@link PastureStore}, che e' il terzo deposito del giocatore
 * dopo il PC vero e il box. Il legame di Cobblemon ricorda il Pokemon come
 * {@code (pcId, pokemonId)} e lo ritrova con {@code getPC(pcId)}: il pcId che
 * ci scrive e' l'UUID del giocatore, che e' esattamente la chiave del nostro
 * deposito, per cui basta far ripiegare la ricerca sul deposito del pascolo
 * quando il PC vero non ha niente — e lo fa un mixin di tre righe.
 */
public final class Pastures {
    private Pastures() {}

    public static PastureStore store(UUID giocatore, RegistryAccess registri) {
        return Cobblemon.INSTANCE.getStorage()
                .getCustomStore(PastureStore.class, giocatore, registri);
    }

    public static PastureStore store(ServerPlayer player) {
        return store(player.getUUID(), player.registryAccess());
    }

    /** La cesta di un pascolo, che il mixin gli ha attaccato. */
    public static NonNullList<ItemStack> cesta(PokemonPastureBlockEntity pascolo) {
        return ((BallBasket) (Object) pascolo).tfcobblemon$balls();
    }

    /**
     * Il pascolo su cui si e' cliccato. Il blocco e' alto due e la block entity
     * sta in basso, quindi cliccando la parte alta non si troverebbe niente.
     */
    public static PokemonPastureBlockEntity pascolo(Level level, BlockPos pos) {
        final BlockState stato = level.getBlockState(pos);
        final BlockPos base = stato.getBlock() instanceof PastureBlock blocco
                ? blocco.getBasePosition(stato, pos) : pos;
        return level.getBlockEntity(base) instanceof PokemonPastureBlockEntity pascolo ? pascolo : null;
    }

    /** Da che parte escono i Pokemon: davanti al blocco. */
    private static Direction verso(Level level, BlockPos pos) {
        final BlockState stato = level.getBlockState(pos);
        return stato.hasProperty(HorizontalDirectionalBlock.FACING)
                ? stato.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
    }

    /**
     * Cambia deposito a un Pokemon. Si toglie da dove sta — e {@code remove} lo
     * richiama, ma un Pokemon che sta in una ball non ha un'entita' in giro,
     * quindi non si vede niente — e si aggiunge dall'altra parte.
     */
    public static boolean trasloca(Pokemon mon, PokemonStore<?> verso) {
        final StoreCoordinates<?> dove = mon.getStoreCoordinates().get();
        if (dove != null) {
            dove.getStore().remove(mon);
        }
        return verso.add(mon);
    }

    /**
     * Apre la cesta. Prima di mostrarla si fa un giro di pulizia: un Pokemon
     * che sta nel deposito del pascolo senza un legame — puo' succedere se il
     * server e' morto nel mezzo mentre rientrava — tornerebbe altrimenti
     * irraggiungibile, e invece torna nel box.
     */
    public static void apri(ServerPlayer player, Level level, BlockPos pos) {
        final PokemonPastureBlockEntity pascolo = pascolo(level, pos);
        if (pascolo == null) {
            return;
        }
        final List<Pokemon> sciolti = new ArrayList<>();
        for (Pokemon mon : store(player)) {
            if (mon.getTetheringId() == null) {
                sciolti.add(mon);
            }
        }
        for (Pokemon mon : sciolti) {
            trasloca(mon, BeltParty.deposito(player));
        }
        player.openMenu(new PastureMenuProvider(pascolo.getBlockPos()),
                buf -> buf.writeBlockPos(pascolo.getBlockPos()));
    }

    /**
     * Sistema il pascolo su quello che c'e' nella cesta: chi ha perso la sua
     * ball rientra, chi l'ha appena appesa esce. Si chiama a fine clic e non
     * durante, perche' un clic in una finestra e' tre mosse e gli stati di
     * mezzo non vanno inseguiti.
     */
    public static void allinea(PokemonPastureBlockEntity pascolo, ServerPlayer player) {
        final Set<UUID> appesi = new HashSet<>();
        for (ItemStack ball : cesta(pascolo)) {
            final BallLink legame = BallLink.read(ball);
            if (legame != null) {
                appesi.add(legame.pokemon());
            }
        }

        // 1. chi non ha piu' la sua ball nella cesta smette di pascolare
        for (PokemonPastureBlockEntity.Tethering legame
                : List.copyOf(pascolo.getTetheredPokemon())) {
            if (appesi.contains(legame.getPokemonId())) {
                continue;
            }
            final Pokemon mon = legame.getPokemon();
            pascolo.releasePokemon(legame.getPokemonId());
            if (mon != null) {
                rientra(mon, legame.getPlayerId(), player.registryAccess());
            }
        }

        // 2. le ball appena appese mandano fuori il loro Pokemon
        final Set<UUID> fuori = new HashSet<>();
        for (PokemonPastureBlockEntity.Tethering legame : pascolo.getTetheredPokemon()) {
            fuori.add(legame.getPokemonId());
        }
        for (ItemStack ball : cesta(pascolo)) {
            final BallLink legame = BallLink.read(ball);
            if (legame == null || fuori.contains(legame.pokemon())) {
                continue;
            }
            final Pokemon mon = BeltParty.trova(player, legame.pokemon());
            if (mon == null || mon.isFainted()) {
                continue;
            }
            if (!trasloca(mon, store(player))) {
                continue;
            }
            if (!pascolo.tether(player, mon, verso(player.level(), pascolo.getBlockPos()))) {
                // non c'e' posto dove uscire: il Pokemon torna da dove veniva e
                // la ball resta appesa, che ci riprova al prossimo giro
                trasloca(mon, BeltParty.deposito(player));
            }
        }
        pascolo.setChanged();
    }

    /**
     * Rimanda un Pokemon nel box del suo padrone, ma solo se stava nel deposito
     * del pascolo: uno messo al pascolo dal PC — alla maniera di Cobblemon,
     * prima di noi — nel PC ci resta.
     */
    public static void casa(Pokemon mon, UUID padrone, RegistryAccess registri) {
        final StoreCoordinates<?> dove = mon.getStoreCoordinates().get();
        if (dove != null && dove.getStore() instanceof PastureStore) {
            trasloca(mon, BeltParty.deposito(padrone, registri));
        }
    }

    /**
     * Il rientro di chi smette di pascolare: si accende il velo del pascolo —
     * lo stesso effetto con cui ne e' uscito — e mezzo secondo dopo il Pokemon
     * rientra e torna nel box del suo padrone.
     *
     * <p>Non e' il richiamo verso il giocatore, col raggio: nessuno lo ha
     * ritirato in tasca da lontano, ha solo smesso di stare al pascolo. E il
     * mezzo secondo serve perche' l'effetto si veda: togliere il Pokemon dal
     * deposito lo richiama subito, e non si vedrebbe niente.
     */
    public static void rientra(Pokemon mon, UUID padrone, RegistryAccess registri) {
        mon.setTetheringId(null);
        final PokemonEntity entita = mon.getEntity();
        if (entita == null) {
            casa(mon, padrone, registri);
            return;
        }
        entita.setBeamMode(2);
        SchedulingFunctionsKt.afterOnServer(0.5F, () -> {
            mon.recall();
            casa(mon, padrone, registri);
            return Unit.INSTANCE;
        });
    }

    /**
     * Cerca un Pokemon in un deposito scorrendolo. L'indice per UUID di
     * Cobblemon resta indietro dopo i travasi, e su quello ci abbiamo gia'
     * perso una giornata con la cintura.
     */
    public static Pokemon cerca(PokemonStore<?> deposito, UUID id) {
        for (Pokemon mon : deposito) {
            if (mon.getUuid().equals(id)) {
                return mon;
            }
        }
        return null;
    }

}
