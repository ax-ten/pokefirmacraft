package com.kingtrapinch.tfcobblemon.zone;

import com.cobblemon.mod.common.api.drop.DropEntry;
import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.drop.ItemDropEntry;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Le tre letture di un'area. Lo scheletro e' lo stesso — un controllore accanto
 * a un pascolo, e quello che gli hai costruito intorno decide la resa — e
 * cambia solo cosa si guarda e cosa ne viene.
 *
 * <p>Non c'e' un numero di posti: <b>il limite e' l'attrezzatura</b>. La
 * palestra allena tanti Pokemon quanti sono i sacchi che le hai messo intorno,
 * e se il pascolo ne tiene dieci ma i sacchi sono due, se ne allenano due.
 */
public enum ZoneKind {
    /** Palestra: si contano i sacchi da boxe, e salgono gli EV. */
    GYM("gym"),
    /**
     * Ozio: si conta l'arredo, e sale l'amicizia. Il nome guarda alla Ball
     * Chic — la Luxury Ball, quella che nei giochi fa salire l'amicizia piu'
     * in fretta — che e' il rimando giusto per una stanza dove i Pokemon
     * stanno bene e non fanno niente.
     */
    LEISURE("leisure"),
    /**
     * Ranch: si contano le condizioni di spawn soddisfatte, e rende quello che
     * il Pokemon droppa. Non si chiama "habitat" per non confonderlo con
     * l'Habitat Block dell'era elettrica, che fa un altro mestiere.
     */
    RANCH("ranch");

    /** Una volta su quante un punto logora il sacco: 84 x 3 stadi = 252. */
    private static final int USURA = 84;
    /** Ogni quanti punti si vuota una bottiglia. */
    private static final int BOTTIGLIA = 21;
    /**
     * Quanti punti vale un pezzo di raccolto, a resa uno. Il raccolto e'
     * <b>continuo</b>: un pezzo per volta appena e' maturo, non una cesta a
     * fine giornata. Quarantadue punti sono mezzo giorno di gioco, quindi due
     * pezzi al giorno per un Pokemon di livello basso e tre per uno di
     * livello 100.
     */
    private static final int PEZZO = 42;
    /** La maturazione si conta in millesimi, per non perdere i decimali. */
    private static final int FINO = 1000;
    /** Il tetto dell'amicizia. */
    private static final int AMICIZIA = 255;

    /** Dal nome della statistica a quella di Cobblemon. */
    private static final Map<String, Stats> STATISTICHE = new LinkedHashMap<>();

    static {
        STATISTICHE.put("hp", Stats.HP);
        STATISTICHE.put("attack", Stats.ATTACK);
        STATISTICHE.put("defence", Stats.DEFENCE);
        STATISTICHE.put("special_attack", Stats.SPECIAL_ATTACK);
        STATISTICHE.put("special_defence", Stats.SPECIAL_DEFENCE);
        STATISTICHE.put("speed", Stats.SPEED);
    }

    private final String nome;

    ZoneKind(String nome) {
        this.nome = nome;
    }

    public String nome() {
        return nome;
    }

    /**
     * Il lavoro di un giro: chi sta al pascolo sotto, e quello che la zona
     * trova intorno a se'.
     *
     * <p>{@code punti} sono i punti maturati dal giro scorso, e il loro valore
     * viene dalla regola unica: tre giorni di gioco portano un Pokemon al
     * massimo, cioe' 252. Un punto e' un EV per la palestra, un punto di
     * amicizia per l'ozio, e un passo verso il prossimo pezzo di raccolto per
     * il ranch.
     */
    public void lavora(ServerLevel level, ZoneBlockEntity zona, List<Pokemon> dentro, int punti) {
        switch (this) {
            case GYM -> palestra(level, zona, dentro, punti);
            case LEISURE -> ozio(dentro, punti);
            case RANCH -> ranch(level, zona, dentro, punti);
        }
    }

    /**
     * La palestra: <b>un sacco allena un Pokemon</b>, e se i sacchi sono meno
     * dei Pokemon si allenano solo quelli che hanno un sacco. Non c'e' un
     * numero di posti da nessuna parte: il limite e' l'attrezzatura.
     *
     * <p>Il sacco si logora a ogni punto, una volta su ottantaquattro: tre
     * stadi fanno duecentocinquantadue, cioe' <b>un sacco consumato per intero
     * e' una statistica portata al tetto</b>.
     */
    private static void palestra(ServerLevel level, ZoneBlockEntity zona,
                                 List<Pokemon> dentro, int punti) {
        final ZoneArea area = ZoneArea.guarda(level, zona.getBlockPos());
        final List<ZoneArea.Sacco> liberi = new ArrayList<>(area.sacchi());
        for (Pokemon mon : dentro) {
            if (liberi.isEmpty() || zona.fermo(mon.getUuid())) {
                continue;
            }
            // un sacco per Pokemon: quello della statistica scelta nella
            // finestra, o il primo che capita se non e' stata scelta
            final String voluta = zona.scelta(mon.getUuid());
            ZoneArea.Sacco preso = null;
            for (java.util.Iterator<ZoneArea.Sacco> giro = liberi.iterator(); giro.hasNext();) {
                final ZoneArea.Sacco sacco = giro.next();
                if (voluta == null || sacco.allena().equals(voluta)) {
                    preso = sacco;
                    giro.remove();
                    break;
                }
            }
            final Stats statistica = preso == null ? null : STATISTICHE.get(preso.allena());
            if (statistica == null) {
                continue;
            }
            mon.getEvs().add(statistica, punti);
            for (int colpo = 0; colpo < punti; colpo++) {
                if (level.getRandom().nextInt(USURA) == 0) {
                    preso.blocco().logora(level, preso.pos());
                    break;
                }
            }
        }
        if (area.haBottiglie()) {
            for (int bevuta = 0; bevuta < punti; bevuta += BOTTIGLIA) {
                area.bevi(level);
            }
        }
    }

    /**
     * L'ozio: sale l'amicizia, e intanto si guarisce. Le due cose vanno
     * insieme perche' una sorgente calda che non rimette in piedi non e' una
     * sorgente calda, ed e' quello che da' alla stanza un motivo di esistere
     * anche quando l'amicizia e' al tetto.
     */
    private static void ozio(List<Pokemon> dentro, int punti) {
        for (Pokemon mon : dentro) {
            mon.setFriendship(Math.min(AMICIZIA, mon.getFriendship() + punti), false);
            if (mon.getCurrentHealth() < mon.getMaxHealth()) {
                mon.setCurrentHealth(Math.min(mon.getMaxHealth(), mon.getCurrentHealth() + punti));
            } else if (mon.getStatus() != null) {
                // le alterazioni passano quando si e' tornati in forze
                mon.setStatus(null);
            }
        }
    }

    /**
     * Il ranch: si raccoglie quello che quel Pokemon lascerebbe morendo, senza
     * che muoia. La tabella e' la sua, non una nostra —
     * {@code getForm().getDrops()} — e il raccolto va in un contenitore
     * attaccato al controllore se c'e', altrimenti a terra.
     *
     * <p><b>Continuo</b>: ogni Pokemon matura per conto suo e lascia cadere un
     * pezzo appena e' pronto. Quanto in fretta lo dice {@link ZoneWorld} —
     * di base il livello, e dove c'e' TFC anche la stagione e il clima — per
     * cui due Pokemon nella stessa stanza non rendono uguale.
     */
    private static void ranch(ServerLevel level, ZoneBlockEntity zona,
                              List<Pokemon> dentro, int punti) {
        for (Pokemon mon : dentro) {
            final float resa = ZoneWorld.attivo().resa(level, zona.getBlockPos(), mon);
            final int maturo = zona.matura(mon.getUuid(), (int) (punti * FINO * resa));
            for (int pezzi = 0; pezzi < maturo; pezzi++) {
                raccogli(level, zona, mon);
            }
        }
    }

    /** Un pezzo solo, pescato dalla tabella di quel Pokemon. */
    private static void raccogli(ServerLevel level, ZoneBlockEntity zona, Pokemon mon) {
        final DropTable tabella = mon.getForm().getDrops();
        final List<DropEntry> tirate = tabella.getDrops(new kotlin.ranges.IntRange(1, 1), mon);
        if (tirate.isEmpty()) {
            return;
        }
        final DropEntry voce = tirate.get(0);
        if (!(voce instanceof ItemDropEntry roba)) {
            return;
        }
        final Item cosa = BuiltInRegistries.ITEM.get(roba.getItem());
        if (cosa == Items.AIR) {
            return;
        }
        final ItemStack pila = new ItemStack(cosa, Math.max(1, roba.getQuantity()));
        final Container cassa = ZoneArea.cassa(level, zona.getBlockPos());
        if (cassa == null || !infila(cassa, pila)) {
            Containers.dropItemStack(level, zona.getBlockPos().getX() + 0.5,
                    zona.getBlockPos().getY() + 1.0, zona.getBlockPos().getZ() + 0.5, pila);
        }
    }

    private static boolean infila(Container cassa, ItemStack pila) {
        for (int posto = 0; posto < cassa.getContainerSize(); posto++) {
            if (cassa.getItem(posto).isEmpty()) {
                cassa.setItem(posto, pila);
                cassa.setChanged();
                return true;
            }
        }
        return false;
    }

    /**
     * Di quante volte va piu' veloce questa zona, per quello che le hanno
     * messo intorno.
     *
     * <p>E' l'unica leva dell'era industriale, e non alza il tetto: un tetto
     * piu' alto non esiste, 252 e' 252. Le bottiglie appoggiate intorno — e piu'
     * avanti i drink e le vitamine — <b>accorciano il tempo</b>.
     */
    public int fretta(ServerLevel level, ZoneBlockEntity zona) {
        return ZoneArea.guarda(level, zona.getBlockPos()).haBottiglie() ? 2 : 1;
    }
}
