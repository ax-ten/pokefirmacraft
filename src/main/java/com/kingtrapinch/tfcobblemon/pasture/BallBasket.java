package com.kingtrapinch.tfcobblemon.pasture;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Le ball appese a un pascolo. Il campo lo aggiunge un mixin alla block entity
 * di Cobblemon — che e' final e non si estende — e questa interfaccia e' il
 * modo di arrivarci da fuori.
 */
public interface BallBasket {

    List<ItemStack> tfcobblemon$balls();
}
