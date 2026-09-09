package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.block.VivichokeBlock;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/**
 * Il Vivichoke si pianta solo sulla terra arata di TFC, non su quella vanilla.
 */
@Pseudo
@Mixin(VivichokeBlock.class)
public abstract class VivichokeMixin extends CropBlock {
    public VivichokeMixin(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return Helpers.isBlock(state, TFCTags.Blocks.FARMLANDS);
    }
}
