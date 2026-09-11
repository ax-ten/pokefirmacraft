package com.kingtrapinch.tfcobblemon.belt;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/** Il poco che serve sapere sulla cintura da fuori. */
public final class Belts {
    private Belts() {}

    private static Boolean curios;

    private static boolean curios() {
        if (curios == null) {
            curios = ModList.get().isLoaded("curios");
        }
        return curios;
    }

    /** La cintura indossata, o vuoto: senza Curios non se ne indossano. */
    public static ItemStack worn(Player player) {
        return curios() ? CuriosBelt.worn(player) : ItemStack.EMPTY;
    }

    /** Quanti posti concede quello che il giocatore ha addosso. */
    public static int capacity(Player player) {
        final ItemStack cintura = worn(player);
        return cintura.getItem() instanceof TrainerBeltItem belt ? belt.slots() : 0;
    }

    /**
     * Infila una ball nella cintura indossata, se c'e' posto. La ball si
     * riduce di uno: quello che resta e' affare di chi chiama.
     */
    public static boolean insert(Player player, ItemStack ball) {
        return curios() && CuriosBelt.insert(player, ball);
    }
}
