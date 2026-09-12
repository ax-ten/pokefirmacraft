package com.kingtrapinch.tfcobblemon.zone;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Il controllore di una zona: si piazza <b>accanto a un pascolo</b> e lavora sui
 * Pokemon che ci stanno dentro.
 *
 * <p>Non tiene Pokemon. Il primo giro l'avevo fatto con tre blocchi-pascolo
 * nostri che ospitavano la block entity di Cobblemon, e per farlo serviva
 * scrivere a mano i blocchi validi di una block entity altrui e riscrivere il
 * loro tick per non farlo esplodere sul cast al loro blocco: due porte di
 * servizio per avere un contenitore che c'era gia'. Cosi' invece il pascolo
 * resta uno, le specializzazioni si sommano — palestra da un lato, terme
 * dall'altro, sugli stessi Pokemon — e di Cobblemon si usano solo metodi
 * pubblici.
 */
public class ZoneBlock extends BaseEntityBlock {

    public static final MapCodec<ZoneBlock> CODEC = simpleCodec(
            properties -> new ZoneBlock(properties, ZoneKind.GYM));

    private final ZoneKind genere;

    public ZoneBlock(Properties properties, ZoneKind genere) {
        super(properties);
        this.genere = genere;
        registerDefaultState(stateDefinition.any()
                .setValue(HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.NORTH));
    }

    public ZoneKind genere() {
        return genere;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZoneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> tipo) {
        if (level.isClientSide()) {
            return null;
        }
        return (mondo, pos, stato, be) -> {
            if (be instanceof ZoneBlockEntity zona && mondo instanceof ServerLevel server) {
                zona.tick(server);
            }
        };
    }

    /**
     * A mani vuote dice se ha trovato un pascolo a cui attaccarsi: e' l'unica
     * cosa che uno ha bisogno di sapere guardandolo.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               net.minecraft.world.entity.player.Player player,
                                               BlockHitResult hit) {
        if (player instanceof ServerPlayer giocatore && level instanceof ServerLevel server
                && level.getBlockEntity(pos) instanceof ZoneBlockEntity zona) {
            final net.minecraft.network.chat.Component detto;
            if (zona.inConflitto(server)) {
                detto = net.minecraft.network.chat.Component.translatable("tfcobblemon.zone.conteso");
            } else if (zona.pascolo(server) == null) {
                detto = net.minecraft.network.chat.Component.translatable("tfcobblemon.zone.senza_pascolo");
            } else {
                detto = net.minecraft.network.chat.Component.translatable(
                        "tfcobblemon.zone.al_lavoro", zona.alPascolo(server).size());
            }
            giocatore.displayClientMessage(detto, true);
        }
        return InteractionResult.SUCCESS;
    }

}
