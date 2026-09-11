package com.kingtrapinch.tfcobblemon.belt;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/** Il poco che serve sapere sulla cintura da fuori. */
public final class Belts {
    private Belts() {}

    /** I posti della squadra di Cobblemon: il massimo che si possa concedere. */
    private static final int PIENA = 6;

    private static Boolean curios;

    private static boolean curios() {
        if (curios == null) {
            curios = ModList.get().isLoaded("curios");
        }
        return curios;
    }

    /** La cintura indossata, o vuoto se nello slot c'e' altro o niente. */
    public static ItemStack worn(Player player) {
        final ItemStack nelloSlot = inBeltSlot(player);
        return nelloSlot.getItem() instanceof TrainerBeltItem ? nelloSlot : ItemStack.EMPTY;
    }

    /**
     * In creativa i limiti non si applicano. La creativa serve a provare le
     * cose, non a rispettarle: la cintura non fa da cancello, la squadra non si
     * riallinea, e si evoca qualunque Pokemon si abbia.
     */
    public static boolean senzaLimiti(Player player) {
        return player.isCreative();
    }

    /** Cosa il giocatore porta nello slot belt, qualunque cosa sia. */
    public static ItemStack inBeltSlot(Player player) {
        return curios() ? CuriosBelt.wornInBelt(player) : ItemStack.EMPTY;
    }

    /**
     * Quanti Pokemon il giocatore ha a portata.
     *
     * <p>Una cintura concede i suoi posti. Senza cintura ne vale uno: un posto
     * ce l'hai sempre, che tu indossi la ball o niente. Zero posti vorrebbe dire
     * cominciare la partita senza poter tenere il primo Pokemon che prendi, e
     * l'elenco a sinistra vuoto non spiega perche'.
     */
    public static int capacity(Player player) {
        if (senzaLimiti(player)) {
            return PIENA;
        }
        final ItemStack nelloSlot = inBeltSlot(player);
        if (nelloSlot.getItem() instanceof TrainerBeltItem belt) {
            return belt.slots();
        }
        return 1;
    }

    /** Indossa quello che ha in mano, se lo slot cintura e' libero. */
    public static boolean equip(Player player, ItemStack cosa) {
        return curios() && CuriosBelt.equip(player, cosa);
    }

    /** Riscrive la cintura nel suo slot dopo averla modificata. */
    public static void store(Player player, ItemStack cintura) {
        if (curios()) {
            CuriosBelt.store(player, cintura);
        }
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
