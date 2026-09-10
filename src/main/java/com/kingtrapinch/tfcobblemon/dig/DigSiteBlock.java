package com.kingtrapinch.tfcobblemon.dig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Un blocco sospetto. Non si spazzola in mondo come quelli vanilla: col brush
 * in mano si apre lo scavo, e da li' si lavora con gli attrezzi che il
 * giocatore ha addosso.
 */
public class DigSiteBlock extends Block implements EntityBlock {
    public DigSiteBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player,
                                              net.minecraft.world.InteractionHand hand,
                                              BlockHitResult hit) {
        if (!stack.is(Items.BRUSH)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level instanceof ServerLevel server
                && level.getBlockEntity(pos) instanceof DigSiteBlockEntity site) {
            final DigSite scavo = site.site(server);
            player.openMenu(new DigMenuProvider(pos), buf -> buf.writeBlockPos(pos));
            if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                sp.connection.send(new DigSyncPayload(scavo.snapshot(), scavo.durability()));
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DigSiteBlockEntity(pos, state);
    }
}
