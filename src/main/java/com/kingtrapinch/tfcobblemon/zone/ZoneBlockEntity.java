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
 * Il cervello di una zona: cerca il pascolo che ha accanto, guarda cosa gli
 * hanno costruito intorno, e lavora su chi sta al pascolo.
 *
 * <p><b>E' il controllore a cercare il pascolo, non il contrario.</b> Cosi' il
 * pascolo non sa niente di noi: si leggono i suoi Pokemon con
 * {@code getTetheredPokemon()}, che e' pubblico, e non si entra in casa di
 * nessuno. L'indirizzo trovato si tiene, e si ricontrolla solo quando non
 * risponde piu'.
 */
public class ZoneBlockEntity extends BlockEntity {

    /** Quanto lontano puo' stare il pascolo. */
    public static final int PORTATA = 6;
    /** Ogni quanti tick si lavora. Come il pascolo, due secondi. */
    private static final int PASSO = 40;

    private int attesa = PASSO;
    @Nullable
    private BlockPos pascolo;

    public ZoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModZones.ZONA_BE.get(), pos, state);
    }

    public ZoneKind genere() {
        return getBlockState().getBlock() instanceof ZoneBlock zona ? zona.genere() : ZoneKind.GYM;
    }

    /** Il pascolo accanto, se c'e'. */
    @Nullable
    public PokemonPastureBlockEntity pascolo(ServerLevel level) {
        if (pascolo != null
                && level.getBlockEntity(pascolo) instanceof PokemonPastureBlockEntity trovato) {
            return trovato;
        }
        pascolo = null;
        for (BlockPos pos : BlockPos.betweenClosed(
                getBlockPos().offset(-PORTATA, -PORTATA, -PORTATA),
                getBlockPos().offset(PORTATA, PORTATA, PORTATA))) {
            if (level.getBlockEntity(pos) instanceof PokemonPastureBlockEntity trovato) {
                pascolo = pos.immutable();
                setChanged();
                return trovato;
            }
        }
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

    public void tick(ServerLevel level) {
        if (--attesa > 0) {
            return;
        }
        attesa = PASSO;
        final List<Pokemon> dentro = alPascolo(level);
        if (!dentro.isEmpty()) {
            genere().lavora(level, this, dentro);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registri) {
        super.loadAdditional(tag, registri);
        pascolo = tag.contains("Pasture") ? NbtUtils.readBlockPos(tag, "Pasture").orElse(null) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registri) {
        super.saveAdditional(tag, registri);
        if (pascolo != null) {
            tag.put("Pasture", NbtUtils.writeBlockPos(pascolo));
        }
    }
}
