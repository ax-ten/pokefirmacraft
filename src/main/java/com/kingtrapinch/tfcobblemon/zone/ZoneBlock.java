package com.kingtrapinch.tfcobblemon.zone;

import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
 * Una zona: un blocco che dichiara un'area e legge quello che ci sta dentro.
 *
 * <p>La block entity <b>non e' nostra, e' quella del pascolo di Cobblemon</b>, e
 * non per pigrizia: il pezzo che riceve "metti questo Pokemon al pascolo" dalla
 * finestra del PC pretende di trovare all'indirizzo del link una
 * {@code PokemonPastureBlockEntity} e non altro, quindi con un'entita' nostra
 * la finestra del PC non funzionerebbe. Ospitando la loro arrivano gratis il
 * link, i permessi, il legame, il vagabondaggio nei confini e il controllo
 * periodico.
 *
 * <p>Il prezzo e' un innesto di quattro righe: il loro tick chiama
 * {@code togglePastureOn}, che fa un cast secco a {@code PastureBlock}, e senza
 * quel freno un blocco nostro esploderebbe venti volte al secondo.
 *
 * <p>Cosa si vede aprendola non lo decide il blocco: lo decide
 * {@link ZoneAccess}, che e' il PC in un mondo Cobblemon qualunque e le ball
 * dove c'e' la cintura.
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
        return new PokemonPastureBlockEntity(pos, state);
    }

    /**
     * Il tick e' il loro, meno una riga.
     *
     * <p>Il loro fa tre cose: il conto alla rovescia fino al controllo dei
     * legami, il metabolismo dei Pokemon al pascolo, e accendere il blocco
     * mentre qualcuno guarda. L'ultima passa da {@code togglePastureOn}, che
     * fa un cast secco a {@code PastureBlock} e su un blocco nostro
     * esploderebbe venti volte al secondo — ed e' l'unica cosa di quel tick
     * che non e' pubblica. Rifacendolo noi non serve nessun innesto: si
     * chiamano gli stessi metodi pubblici e si lascia fuori quella riga.
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> tipo) {
        if (level.isClientSide()) {
            return null;
        }
        return (mondo, pos, stato, be) -> {
            if (be instanceof PokemonPastureBlockEntity zona) {
                tick(zona);
            }
        };
    }

    private static void tick(PokemonPastureBlockEntity zona) {
        zona.setTicksUntilCheck(zona.getTicksUntilCheck() - 1);
        if (zona.getTicksUntilCheck() <= 0) {
            // rimette lui il contatore
            zona.checkPokemon();
        }
        for (PokemonPastureBlockEntity.Tethering legame : zona.getTetheredPokemon()) {
            final com.cobblemon.mod.common.pokemon.Pokemon mon = legame.getPokemon();
            if (mon == null) {
                continue;
            }
            if (mon.getCurrentFullness() > 0) {
                mon.tickMetabolism(1);
            }
            if (!mon.getInteractionCooldowns().isEmpty()) {
                mon.tickInteractionCooldown(1);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               net.minecraft.world.entity.player.Player player,
                                               BlockHitResult hit) {
        if (player instanceof ServerPlayer giocatore
                && level.getBlockEntity(pos) instanceof PokemonPastureBlockEntity zona) {
            ZoneAccess.attivo().apri(giocatore, zona);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity chi, ItemStack cosa) {
        if (chi instanceof ServerPlayer giocatore
                && level.getBlockEntity(pos) instanceof PokemonPastureBlockEntity zona) {
            zona.setOwnerId(giocatore.getUUID());
            zona.setOwnerName(giocatore.getGameProfile().getName());
            zona.setChanged();
        }
    }

    @Override
    public void onRemove(BlockState vecchio, Level level, BlockPos pos, BlockState nuovo, boolean moved) {
        if (!vecchio.is(nuovo.getBlock())
                && level.getBlockEntity(pos) instanceof PokemonPastureBlockEntity zona) {
            zona.onBroken();
        }
        super.onRemove(vecchio, level, pos, nuovo, moved);
    }
}
