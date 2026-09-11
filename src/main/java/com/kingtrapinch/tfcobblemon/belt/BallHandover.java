package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * Il passaggio di mano fra la ball e il PC.
 *
 * <p>La regola e' semplice: **il PC tiene il Pokemon, la ball lo tiene in mano.**
 * Depositare vuol dire consegnarlo, quindi la sua ball non serve piu' e
 * sparisce; ritirarlo vuol dire riprenderlo in mano, e la ball torna — quella
 * vera, perche' Cobblemon si ricorda con che ball l'hai preso.
 *
 * <p>Senza la seconda meta' la prima sarebbe una trappola: depositi, ritiri, e
 * ti ritrovi un Pokemon che non hai modo di far uscire.
 */
public final class BallHandover {
    private BallHandover() {}

    /**
     * Via la ball di chi e' stato depositato, dalla cintura e dall'inventario.
     * Ogni copia: se ne girano due per lo stesso Pokemon, nessuna deve
     * sopravvivere al deposito.
     */
    public static void forget(ServerPlayer player, UUID pokemon) {
        final ItemStack cintura = Belts.worn(player);
        if (cintura.getItem() instanceof TrainerBeltItem belt) {
            final List<ItemStack> posti = belt.posti(cintura);
            boolean toccata = false;
            for (int i = 0; i < posti.size(); i++) {
                if (punta(posti.get(i), pokemon)) {
                    posti.set(i, ItemStack.EMPTY);
                    toccata = true;
                }
            }
            if (toccata) {
                belt.salva(cintura, posti);
                Belts.store(player, cintura);
            }
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (punta(player.getInventory().getItem(i), pokemon)) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
    }

    /**
     * La ball di chi e' stato ritirato, consegnata in mano: prima la cintura se
     * c'e' posto, altrimenti l'inventario. Sopra i posti disponibili si esce
     * comunque, come oggetto — e' il modo di portarsi via piu' di quanti se ne
     * possano usare.
     */
    public static void hand(ServerPlayer player, UUID pokemon) {
        final Pokemon mon = cerca(player, pokemon);
        if (mon == null) {
            return;
        }
        // handle nuovo: la ball vecchia e' stata distrutta col deposito, e se
        // una sua copia girasse ancora non deve rispondere
        final UUID handle = UUID.randomUUID();
        mon.getPersistentData().putString(BallLink.OWNER, handle.toString());

        final ItemStack ball = mon.getCaughtBall().stack(1);
        ball.set(ModBallData.BALL_LINK.get(), BallLink.of(mon, handle));
        if (Belts.insert(player, ball)) {
            return;
        }
        if (!player.getInventory().add(ball)) {
            player.drop(ball, false);
        }
    }

    private static boolean punta(ItemStack stack, UUID pokemon) {
        final BallLink legame = BallLink.read(stack);
        return legame != null && legame.pokemon().equals(pokemon);
    }

    private static Pokemon cerca(ServerPlayer player, UUID pokemon) {
        final var storage = Cobblemon.INSTANCE.getStorage();
        final Pokemon inSquadra = storage.getParty(player).get(pokemon);
        return inSquadra != null ? inSquadra : storage.getPC(player).get(pokemon);
    }
}
