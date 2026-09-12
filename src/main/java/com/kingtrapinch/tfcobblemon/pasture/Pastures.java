package com.kingtrapinch.tfcobblemon.pasture;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.PokemonStore;
import com.cobblemon.mod.common.api.storage.StoreCoordinates;
import com.cobblemon.mod.common.block.PastureBlock;
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.belt.BallLink;
import com.kingtrapinch.tfcobblemon.belt.BeltParty;
import com.kingtrapinch.tfcobblemon.belt.Belts;
import com.kingtrapinch.tfcobblemon.belt.ModBallData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
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
    public static List<ItemStack> cesta(PokemonPastureBlockEntity pascolo) {
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
     * Una ball si appende al pascolo: il Pokemon passa al deposito del pascolo e
     * viene messo in giro. Il limite e' quello di Cobblemon — sedici — che e'
     * anche quanti Pokemon il pascolo sa tenere legati.
     */
    public static boolean infila(ServerPlayer player, PokemonPastureBlockEntity pascolo, ItemStack ball) {
        final BallLink legame = BallLink.read(ball);
        if (legame == null || legame.out()) {
            return false;
        }
        final List<ItemStack> cesta = cesta(pascolo);
        if (cesta.size() >= pascolo.getMaxTethered()) {
            return false;
        }
        final Pokemon mon = BeltParty.trova(player, legame.pokemon());
        if (mon == null || mon.isFainted()) {
            return false;
        }
        // la copia si prende adesso: legando il Pokemon parte l'evento di uscita
        // in campo, che marcherebbe la ball in mano come "fuori"
        final ItemStack appesa = ball.copy();
        appesa.setCount(1);
        if (!trasloca(mon, store(player))) {
            return false;
        }
        if (!pascolo.tether(player, mon, verso(player.level(), pascolo.getBlockPos()))) {
            // niente posto dove uscire: il Pokemon torna da dove veniva
            trasloca(mon, BeltParty.deposito(player));
            return false;
        }
        appesa.set(ModBallData.BALL_LINK.get(), legame.withOut(false));
        cesta.add(appesa);
        pascolo.setChanged();
        return true;
    }

    /**
     * L'ultima ball appesa torna in mano, e con lei il Pokemon: prima rientra
     * con la sua animazione, poi si stacca il legame, poi cambia deposito.
     */
    public static ItemStack sfila(ServerPlayer player, PokemonPastureBlockEntity pascolo) {
        final List<ItemStack> cesta = cesta(pascolo);
        if (cesta.isEmpty()) {
            return ItemStack.EMPTY;
        }
        final ItemStack ball = cesta.remove(cesta.size() - 1);
        pascolo.setChanged();
        final BallLink legame = BallLink.read(ball);
        if (legame != null) {
            final Pokemon mon = cerca(store(player), legame.pokemon());
            if (mon == null) {
                TFCobblemon.LOGGER.debug("pascolo: la ball di {} non trova il suo Pokemon", legame.pokemon());
            } else {
                rientra(mon);
                pascolo.releasePokemon(mon.getUuid());
                trasloca(mon, BeltParty.deposito(player));
            }
        }
        return ball;
    }

    /** Il rientro nella ball, con l'animazione: un Pokemon non svanisce. */
    public static void rientra(Pokemon mon) {
        if (mon.getEntity() != null) {
            mon.tryRecallWithAnimation();
        }
        mon.setTetheringId(null);
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

    /** La ball che torna: sulla cintura se c'e' posto, altrimenti in mano. */
    public static void rendi(ServerPlayer player, ItemStack ball) {
        if (ball.isEmpty() || Belts.insert(player, ball)) {
            return;
        }
        if (!player.getInventory().add(ball)) {
            player.drop(ball, false);
        }
    }
}
