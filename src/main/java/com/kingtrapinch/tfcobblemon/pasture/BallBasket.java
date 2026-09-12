package com.kingtrapinch.tfcobblemon.pasture;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/**
 * Le ball appese a un pascolo, sedici posti a caselle fisse come in qualunque
 * cassa. Il campo lo aggiunge un mixin alla block entity di Cobblemon — che e'
 * final e non si estende — e questa interfaccia e' il modo di arrivarci da
 * fuori.
 */
public interface BallBasket {

    /** Quante ball ci stanno, e quanti Pokemon il pascolo sa tenere legati. */
    int POSTI = 16;

    NonNullList<ItemStack> tfcobblemon$balls();

    /**
     * Se il pascolo e' stato collegato al PC. Da quel momento non lavora piu'
     * a ball: pesca dal PC come il pascolo di Cobblemon.
     */
    boolean tfcobblemon$collegato();

    void tfcobblemon$collega(boolean si);

    /** Il modulo installato, che si vede nello slot e non si tira via. */
    ItemStack tfcobblemon$modulo();

    void tfcobblemon$modulo(ItemStack cosa);
}
