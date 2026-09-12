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
     * Recinto: si contano le condizioni di spawn soddisfatte, e rende quello
     * che il Pokemon droppa. Non si chiama "habitat" per non confonderlo con
     * l'Habitat Block dell'era elettrica, che fa un altro mestiere.
     */
    RANCH("ranch");

    /** Una volta su quante un punto logora il sacco: 84 x 3 stadi = 252. */
    private static final int USURA = 84;
    /** Ogni quanti punti si vuota una bottiglia. */
    private static final int BOTTIGLIA = 21;
    /** Ogni quanti punti il recinto raccoglie: 84 punti sono un giorno. */
    private static final int RACCOLTO = 84;
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
     * amicizia per l'ozio, e un passo verso il prossimo raccolto per il
     * recinto.
     */
    public void lavora(ServerLevel level, ZoneBlockEntity zona, List<Pokemon> dentro, int punti) {
        switch (this) {
            case GYM -> palestra(level, zona, dentro, punti);
            case LEISURE -> ozio(dentro, punti);
            case RANCH -> recinto(level, zona, dentro, punti);
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
        final List<ZoneArea.Sacco> sacchi = area.sacchi();
        for (int i = 0; i < Math.min(dentro.size(), sacchi.size()); i++) {
            final ZoneArea.Sacco sacco = sacchi.get(i);
            final Stats statistica = STATISTICHE.get(sacco.allena());
            if (statistica == null) {
                continue;
            }
            dentro.get(i).getEvs().add(statistica, punti);
            for (int colpo = 0; colpo < punti; colpo++) {
                if (level.getRandom().nextInt(USURA) == 0) {
                    sacco.blocco().logora(level, sacco.pos());
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
     * Il recinto: ogni tanto si raccoglie quello che quel Pokemon lascerebbe
     * morendo, senza che muoia. La tabella e' la sua, non una nostra —
     * {@code getForm().getDrops()} — e il raccolto va in un contenitore
     * attaccato al controllore se c'e', altrimenti a terra.
     *
     * <p>Il passo e' un raccolto al giorno di gioco per Pokemon: ottantaquattro
     * punti sono un terzo del cammino verso il tetto, cioe' un giorno.
     */
    private static void recinto(ServerLevel level, ZoneBlockEntity zona,
                                List<Pokemon> dentro, int punti) {
        int gruzzolo = zona.resto() + punti;
        while (gruzzolo >= RACCOLTO) {
            gruzzolo -= RACCOLTO;
            raccogli(level, zona, dentro.get(level.getRandom().nextInt(dentro.size())));
        }
        zona.resto(gruzzolo);
    }

    private static void raccogli(ServerLevel level, ZoneBlockEntity zona, Pokemon mon) {
        final DropTable tabella = mon.getForm().getDrops();
        final Container cassa = ZoneArea.cassa(level, zona.getBlockPos());
        for (DropEntry voce : tabella.getDrops(tabella.getAmount(), mon)) {
            if (!(voce instanceof ItemDropEntry roba)) {
                continue;
            }
            final Item cosa = BuiltInRegistries.ITEM.get(roba.getItem());
            if (cosa == Items.AIR) {
                continue;
            }
            final ItemStack pila = new ItemStack(cosa, Math.max(1, roba.getQuantity()));
            if (cassa == null || !infila(cassa, pila)) {
                Containers.dropItemStack(level, zona.getBlockPos().getX() + 0.5,
                        zona.getBlockPos().getY() + 1.0, zona.getBlockPos().getZ() + 0.5, pila);
            }
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
