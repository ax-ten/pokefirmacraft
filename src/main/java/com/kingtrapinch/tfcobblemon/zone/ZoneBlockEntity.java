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
import java.util.List;

/**
 * Il cervello di una zona: sta <b>attaccato</b> a un pascolo, guarda cosa gli
 * hanno costruito intorno, e lavora su chi sta al pascolo.
 *
 * <p><b>E' il controllore a cercare il pascolo, non il contrario.</b> Cosi' il
 * pascolo non sa niente di noi: si leggono i suoi Pokemon con
 * {@code getTetheredPokemon()}, che e' pubblico, e non si entra in casa di
 * nessuno.
 *
 * <p>Attaccato vuol dire attaccato: uno dei quattro lati, all'altezza del
 * pascolo o del suo pezzo alto. Non un raggio — un pascolo e la sua macchina
 * si toccano, e si vede da fuori quale macchina serve quale pascolo.
 *
 * <p><b>Un pascolo, una specializzazione.</b> Se ce ne sono due attaccate allo
 * stesso pascolo non lavora nessuna delle due: e' un conflitto, e va detto,
 * non risolto a caso scegliendone una.
 */
public class ZoneBlockEntity extends BlockEntity {

    /** Ogni quanti tick si guarda l'orologio. Il lavoro lo misura il calendario. */
    private static final int BATTITO = 40;

    private int attesa = BATTITO;
    /** L'ora di calendario dell'ultimo punto maturato. */
    private long visto;
    @Nullable
    private BlockPos pascolo;

    public ZoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModZones.ZONA_BE.get(), pos, state);
    }

    public ZoneKind genere() {
        return getBlockState().getBlock() instanceof ZoneBlock zona ? zona.genere() : ZoneKind.GYM;
    }

    /** I sei posti in cui puo' stare il pascolo attaccato a questo blocco. */
    private Iterable<BlockPos> intorno() {
        final List<BlockPos> posti = new ArrayList<>(8);
        for (net.minecraft.core.Direction verso : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            final BlockPos lato = getBlockPos().relative(verso);
            posti.add(lato);
            // il pezzo con la block entity e' quello basso: se il controllore e'
            // all'altezza di quello alto, il pascolo sta di sbieco sotto
            posti.add(lato.below());
        }
        return posti;
    }

    /** Il pascolo attaccato, se c'e' e se e' solo suo. */
    @Nullable
    public PokemonPastureBlockEntity pascolo(ServerLevel level) {
        if (pascolo != null
                && level.getBlockEntity(pascolo) instanceof PokemonPastureBlockEntity trovato) {
            return conteso(level, pascolo) ? null : trovato;
        }
        pascolo = null;
        for (BlockPos pos : intorno()) {
            if (level.getBlockEntity(pos) instanceof PokemonPastureBlockEntity trovato) {
                pascolo = pos.immutable();
                setChanged();
                return conteso(level, pascolo) ? null : trovato;
            }
        }
        return null;
    }

    /** Se un altro controllore e' attaccato allo stesso pascolo. */
    public boolean conteso(ServerLevel level, BlockPos pascolo) {
        for (net.minecraft.core.Direction verso : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            for (BlockPos pos : new BlockPos[] {pascolo.relative(verso),
                    pascolo.relative(verso).above()}) {
                if (!pos.equals(getBlockPos())
                        && level.getBlockEntity(pos) instanceof ZoneBlockEntity) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Se c'e' un pascolo attaccato ma ha gia' un'altra macchina. */
    public boolean inConflitto(ServerLevel level) {
        for (BlockPos pos : intorno()) {
            if (level.getBlockEntity(pos) instanceof PokemonPastureBlockEntity
                    && conteso(level, pos)) {
                return true;
            }
        }
        return false;
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
     * punti sono maturati lo dice il calendario, percio' una settimana passa
     * anche mentre il chunk era scaricato.
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
            visto = ZoneClock.adesso();
            return;
        }
        if (visto == 0L) {
            visto = ZoneClock.adesso();
            return;
        }
        final int punti = ZoneClock.punti(visto);
        if (punti <= 0) {
            return;
        }
        visto += (long) punti * ZoneClock.passo();
        setChanged();
        genere().lavora(level, this, dentro, punti);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registri) {
        super.loadAdditional(tag, registri);
        pascolo = tag.contains("Pasture") ? NbtUtils.readBlockPos(tag, "Pasture").orElse(null) : null;
        visto = tag.getLong("Seen");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registri) {
        super.saveAdditional(tag, registri);
        if (pascolo != null) {
            tag.put("Pasture", NbtUtils.writeBlockPos(pascolo));
        }
        tag.putLong("Seen", visto);
    }
}
