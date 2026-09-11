package com.kingtrapinch.tfcobblemon.belt;

import net.minecraft.server.level.ServerPlayer;
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

    /** Cosa il giocatore porta nello slot belt, qualunque cosa sia. */
    public static ItemStack inBeltSlot(Player player) {
        return curios() ? CuriosBelt.wornInBelt(player) : ItemStack.EMPTY;
    }

    /**
     * Quanti Pokemon il giocatore ha a portata.
     *
     * <p>Una cintura concede i suoi posti. Senza cintura si indossa la ball
     * stessa, e allora ne vale uno — ma solo se dentro c'e' qualcuno: una ball
     * vuota al collo non e' un Pokemon a portata.
     */
    public static int capacity(Player player) {
        final ItemStack nelloSlot = inBeltSlot(player);
        if (nelloSlot.getItem() instanceof TrainerBeltItem belt) {
            return belt.slots();
        }
        return BallLink.filled(nelloSlot) ? 1 : 0;
    }

    /**
     * Infila una ball nella cintura indossata, se c'e' posto. La ball si
     * riduce di uno: quello che resta e' affare di chi chiama.
     */
    public static boolean insert(Player player, ItemStack ball) {
        if (!curios() || !CuriosBelt.insert(player, ball)) {
            return false;
        }
        if (player instanceof ServerPlayer chi) {
            BeltParty.align(chi);
        }
        return true;
    }
}
