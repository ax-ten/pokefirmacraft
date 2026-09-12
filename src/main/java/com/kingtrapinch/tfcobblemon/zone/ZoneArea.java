package com.kingtrapinch.tfcobblemon.zone;

import com.cobblemon.mod.common.block.StackableItemBlock;
import com.kingtrapinch.tfcobblemon.block.PunchingBagBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Cosa c'e' nell'area di una zona.
 *
 * <p>L'area e' una stanza, non un raggio sferico: quattro blocchi per lato e
 * dall'altezza del pavimento a poco sopra la testa del controllore, che sta in
 * cima al pascolo. Si conta una volta per giro di lavoro e si butta.
 */
public final class ZoneArea {

    /** Quanto si allarga la stanza ai lati. */
    public static final int LATO = 4;
    /** Da quanto sotto il controllore a quanto sopra. */
    public static final int GIU = 3;
    public static final int SU = 2;

    /** Un sacco, col suo posto e la statistica che allena. */
    public record Sacco(BlockPos pos, PunchingBagBlock blocco, String allena) {}

    private final List<Sacco> sacchi = new ArrayList<>();
    private final List<BlockPos> bottiglie = new ArrayList<>();

    public static ZoneArea guarda(ServerLevel level, BlockPos centro) {
        final ZoneArea area = new ZoneArea();
        for (BlockPos pos : BlockPos.betweenClosed(
                centro.offset(-LATO, -GIU, -LATO), centro.offset(LATO, SU, LATO))) {
            final BlockState stato = level.getBlockState(pos);
            if (stato.getBlock() instanceof PunchingBagBlock sacco) {
                // si conta il pezzo basso: un sacco e' alto due, e va contato una volta
                if (stato.getValue(PunchingBagBlock.HALF) == DoubleBlockHalf.LOWER) {
                    area.sacchi.add(new Sacco(pos.immutable(), sacco, sacco.allena()));
                }
            } else if (stato.getBlock() instanceof StackableItemBlock) {
                area.bottiglie.add(pos.immutable());
            }
        }
        return area;
    }

    public List<Sacco> sacchi() {
        return sacchi;
    }

    /** I sacchi che allenano una certa statistica. */
    public List<Sacco> sacchi(String statistica) {
        final List<Sacco> suoi = new ArrayList<>();
        for (Sacco sacco : sacchi) {
            if (sacco.allena().equals(statistica)) {
                suoi.add(sacco);
            }
        }
        return suoi;
    }

    public boolean haBottiglie() {
        return !bottiglie.isEmpty();
    }

    /**
     * Si beve una bottiglia: scende il numero di quelle appoggiate in un
     * blocco, e quando finiscono il blocco sparisce. La scorta che si svuota si
     * vede da fuori, che e' il motivo per cui le bottiglie sono blocchi e non
     * un serbatoio.
     */
    public void bevi(ServerLevel level) {
        if (bottiglie.isEmpty()) {
            return;
        }
        final BlockPos pos = bottiglie.get(0);
        final BlockState stato = level.getBlockState(pos);
        if (!(stato.getBlock() instanceof StackableItemBlock)) {
            bottiglie.remove(0);
            return;
        }
        final var quante = StackableItemBlock.Companion.getAMOUNT();
        final int resta = stato.getValue(quante) - 1;
        if (resta <= 0) {
            level.destroyBlock(pos, false);
            bottiglie.remove(0);
        } else {
            level.setBlock(pos, stato.setValue(quante, resta), Block.UPDATE_CLIENTS);
        }
    }

    /** Un contenitore attaccato al controllore, dove mettere il raccolto. */
    @Nullable
    public static Container cassa(ServerLevel level, BlockPos centro) {
        for (Direction verso : Direction.values()) {
            final BlockEntity be = level.getBlockEntity(centro.relative(verso));
            if (be instanceof Container cassa) {
                return cassa;
            }
        }
        return null;
    }
}
