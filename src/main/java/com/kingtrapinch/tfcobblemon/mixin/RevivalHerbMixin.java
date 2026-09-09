package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.block.RevivalHerbBlock;
import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/**
 * La Revival Herb accetta anche la terra e il fango di TFC, oltre a quello che
 * ammette gia' di suo.
 */
@Pseudo
@Mixin(RevivalHerbBlock.class)
public abstract class RevivalHerbMixin extends CropBlock {
    public RevivalHerbMixin(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return super.mayPlaceOn(state, level, pos)
                || state.is(TFCTags.Blocks.MUD)
                || state.is(TFCTags.Blocks.FARMLANDS);
    }
}
