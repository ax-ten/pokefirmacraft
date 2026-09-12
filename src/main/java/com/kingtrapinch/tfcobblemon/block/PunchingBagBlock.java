package com.kingtrapinch.tfcobblemon.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Il sacco da boxe: due metri di tela di juta piena di sabbia, appesa.
 *
 * <p>Si piazza <b>solo appeso dall'alto</b>: non e' un blocco che si posa, e'
 * un blocco che pende, quindi vuole un soffitto sopra e il vuoto sotto. Il
 * pezzo che si piazza e' quello <em>alto</em> — cioe' dove si clicca e' dove
 * sta l'attacco — e il basso lo mette lui.
 *
 * <p>Si consuma con l'uso, e il consumo si vede: tre stadi come l'incudine di
 * vanilla, che passa per scheggiata e rovinata prima di rompersi. Il passaggio
 * lo decide chi lo usa chiamando {@link #logora}: qui c'e' solo la catena.
 */
public class PunchingBagBlock extends Block {

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    /** Il sacco alto, che comprende l'attacco al soffitto. */
    private static final VoxelShape ALTO = Block.box(4, 0, 4, 12, 16, 12);
    /** Quello basso pende: due pixel di aria sotto, come un sacco vero. */
    private static final VoxelShape BASSO = Block.box(4, 2, 4, 12, 16, 12);

    /** Lo stadio successivo di logoramento, o {@code null} se si sfascia. */
    private final Supplier<Block> dopo;

    public PunchingBagBlock(Properties properties, @Nullable Supplier<Block> dopo) {
        super(properties);
        this.dopo = dopo;
        registerDefaultState(stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? ALTO : BASSO;
    }

    /** Sopra ci vuole qualcosa a cui appendersi. */
    private static boolean appeso(LevelReader level, BlockPos alto) {
        final BlockPos sopra = alto.above();
        return level.getBlockState(sopra).isFaceSturdy(level, sopra, Direction.DOWN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        if (pos.getY() <= level.getMinBuildHeight() || !appeso(level, pos)) {
            return null;
        }
        if (!level.getBlockState(pos.below()).canBeReplaced(context)) {
            return null;
        }
        return defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity chi, ItemStack cosa) {
        level.setBlock(pos.below(), state.setValue(HALF, DoubleBlockHalf.LOWER), Block.UPDATE_ALL);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return appeso(level, pos);
        }
        final BlockState sopra = level.getBlockState(pos.above());
        return sopra.is(this) && sopra.getValue(HALF) == DoubleBlockHalf.UPPER;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction verso, BlockState vicino,
                                     LevelAccessor level, BlockPos pos, BlockPos posVicino) {
        return state.canSurvive(level, pos) ? state
                : Blocks.AIR.defaultBlockState();
    }

    /**
     * Rompendo il pezzo alto si porta via anche il basso, ed e' il basso a
     * lasciare il sacco: la tabella di loot e' solo la sua, cosi' da qualunque
     * parte lo si rompa ne cade uno e uno solo.
     */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            level.destroyBlock(pos.below(), !player.isCreative(), player);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Un giro di logoramento. Il sacco passa allo stadio dopo, e all'ultimo si
     * sfascia senza lasciare niente: una tela sfondata non si ricuce.
     *
     * @param pos uno qualunque dei due pezzi
     */
    public void logora(ServerLevel level, BlockPos pos) {
        final BlockState stato = level.getBlockState(pos);
        if (!stato.is(this)) {
            return;
        }
        final BlockPos alto = stato.getValue(HALF) == DoubleBlockHalf.UPPER ? pos : pos.above();
        if (dopo == null) {
            level.destroyBlock(alto.below(), false);
            level.destroyBlock(alto, false);
            return;
        }
        final Block nuovo = dopo.get();
        level.setBlock(alto, nuovo.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER),
                Block.UPDATE_CLIENTS);
        level.setBlock(alto.below(), nuovo.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER),
                Block.UPDATE_CLIENTS);
    }
}
