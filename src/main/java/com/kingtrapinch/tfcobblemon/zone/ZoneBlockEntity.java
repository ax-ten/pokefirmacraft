package com.kingtrapinch.tfcobblemon.zone;

import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Il cervello di una zona: sta <b>attaccato</b> a un pascolo, guarda cosa gli
 * hanno costruito intorno, e lavora su chi sta al pascolo.
 *
 * <p><b>E' il controllore a cercare il pascolo, non il contrario.</b> Cosi' il
 * pascolo non sa niente di noi: si leggono i suoi Pokemon con
 * {@code getTetheredPokemon()}, che e' pubblico, e non si entra in casa di
 * nessuno.
 *
 * <p>Attaccato vuol dire <b>in cima</b>: sopra la testa del pascolo, e da
 * nessun'altra parte. Un posto solo, che si vede da fuori, e che rende "un
 * pascolo, una specializzazione" una conseguenza della geometria invece di una
 * regola da far rispettare.
 */
public class ZoneBlockEntity extends BlockEntity {

    /** Ogni quanti tick si guarda l'orologio. */
    private static final int BATTITO = 40;

    private int attesa = BATTITO;
    /** Il tick dell'ultimo punto maturato. */
    private long visto;
    @Nullable
    private BlockPos pascolo;
    /** Quanto e' maturato ogni Pokemon, in millesimi di pezzo. */
    private final Map<UUID, Integer> maturazione = new HashMap<>();
    /** Che statistica allena ciascun Pokemon, scelta nella finestra. */
    private final Map<UUID, String> scelte = new HashMap<>();
    /** Chi non deve allenarsi, anche se un sacco ci sarebbe. */
    private final Set<UUID> fermi = new HashSet<>();

    public ZoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModZones.ZONA_BE.get(), pos, state);
    }

    /**
     * Aggiunge maturazione a un Pokemon e dice quanti pezzi sono pronti. Il
     * resto resta da parte: il raccolto e' continuo, e i decimali non si
     * buttano.
     */
    public int matura(UUID chi, int millesimi) {
        final int totale = maturazione.getOrDefault(chi, 0) + millesimi;
        final int pezzi = totale / (PEZZO_IN_MILLESIMI);
        maturazione.put(chi, totale - pezzi * PEZZO_IN_MILLESIMI);
        setChanged();
        return pezzi;
    }

    /** Quanti millesimi vale un pezzo: quarantadue punti. */
    private static final int PEZZO_IN_MILLESIMI = 42 * 1000;

    /** La statistica scelta per un Pokemon, o null se non e' stata scelta. */
    @Nullable
    public String scelta(UUID chi) {
        return scelte.get(chi);
    }

    public void scegli(UUID chi, @Nullable String statistica) {
        if (statistica == null) {
            scelte.remove(chi);
        } else {
            scelte.put(chi, statistica);
        }
        setChanged();
    }

    public boolean fermo(UUID chi) {
        return fermi.contains(chi);
    }

    public void ferma(UUID chi, boolean sta) {
        if (sta) {
            fermi.add(chi);
        } else {
            fermi.remove(chi);
        }
        setChanged();
    }

    public ZoneKind genere() {
        return getBlockState().getBlock() instanceof ZoneBlock zona ? zona.genere() : ZoneKind.GYM;
    }

    /**
     * Il pascolo sotto, se c'e'.
     *
     * <p>Le macchine si attaccano <b>in cima</b> e da nessun'altra parte: il
     * pascolo e' alto due e la sua block entity sta nel pezzo basso, quindi
     * stando sopra la testa del pascolo il posto da guardare e' uno solo, due
     * blocchi sotto. Un posto solo vuol dire anche che <b>una macchina per
     * pascolo</b> non e' una regola da far rispettare: e' la geometria.
     */
    @Nullable
    public PokemonPastureBlockEntity pascolo(ServerLevel level) {
        final BlockPos sotto = getBlockPos().below(2);
        if (level.getBlockEntity(sotto) instanceof PokemonPastureBlockEntity trovato) {
            pascolo = sotto;
            return trovato;
        }
        pascolo = null;
        return null;
    }

    /** Chi sta al pascolo accanto, in questo momento. */
    public List<Pokemon> alPascolo(ServerLevel level) {
        final PokemonPastureBlockEntity vicino = pascolo(level);
        final List<Pokemon> dentro = new ArrayList<>();
        if (vicino == null) {
            return dentro;
        }
        for (PokemonPastureBlockEntity.Tethering legame : vicino.getTetheredPokemon()) {
            final Pokemon mon = legame.getPokemon();
            if (mon != null) {
                dentro.add(mon);
            }
        }
        return dentro;
    }

    /**
     * Il giro di lavoro. Il battito serve solo a guardare l'orologio: quanti
     * punti sono maturati lo dicono i tick passati.
     */
    public void tick(ServerLevel level) {
        if (--attesa > 0) {
            return;
        }
        attesa = BATTITO;
        final List<Pokemon> dentro = alPascolo(level);
        if (dentro.isEmpty()) {
            // senza nessuno da allenare l'orologio non corre: il tempo fermo
            // non si accumula per essere speso tutto insieme dopo
            visto = ZoneWorld.attivo().adesso(level);
            return;
        }
        final long adesso = ZoneWorld.attivo().adesso(level);
        if (visto == 0L) {
            visto = adesso;
            return;
        }
        // quante volte piu' veloce: le vitamine intorno accorciano il passo
        final int fretta = genere().fretta(level, this);
        final int punti = ZoneClock.punti(adesso, visto, fretta);
        if (punti <= 0) {
            return;
        }
        visto += (long) punti * Math.max(1L, ZoneClock.passo() / Math.max(1, fretta));
        setChanged();
        genere().lavora(level, this, dentro, punti);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registri) {
        super.loadAdditional(tag, registri);
        pascolo = tag.contains("Pasture") ? NbtUtils.readBlockPos(tag, "Pasture").orElse(null) : null;
        visto = tag.getLong("Seen");
        maturazione.clear();
        scelte.clear();
        fermi.clear();
        final CompoundTag cresce = tag.getCompound("Ripening");
        for (String chiave : cresce.getAllKeys()) {
            maturazione.put(UUID.fromString(chiave), cresce.getInt(chiave));
        }
        final CompoundTag scelto = tag.getCompound("Chosen");
        for (String chiave : scelto.getAllKeys()) {
            scelte.put(UUID.fromString(chiave), scelto.getString(chiave));
        }
        for (String chiave : tag.getCompound("Paused").getAllKeys()) {
            fermi.add(UUID.fromString(chiave));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registri) {
        super.saveAdditional(tag, registri);
        if (pascolo != null) {
            tag.put("Pasture", NbtUtils.writeBlockPos(pascolo));
        }
        tag.putLong("Seen", visto);
        final CompoundTag cresce = new CompoundTag();
        maturazione.forEach((chi, quanto) -> cresce.putInt(chi.toString(), quanto));
        tag.put("Ripening", cresce);
        final CompoundTag scelto = new CompoundTag();
        scelte.forEach((chi, statistica) -> scelto.putString(chi.toString(), statistica));
        tag.put("Chosen", scelto);
        final CompoundTag fermati = new CompoundTag();
        fermi.forEach(chi -> fermati.putBoolean(chi.toString(), true));
        tag.put("Paused", fermati);
    }
}
