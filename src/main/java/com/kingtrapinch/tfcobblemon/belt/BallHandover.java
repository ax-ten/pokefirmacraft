package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

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

    /** Quanti tick la ball resta inerte dopo che il Pokemon e' rientrato. */
    private static final int RESPIRO = 20;

    /**
     * Via la ball di chi e' stato depositato, dalla cintura e dall'inventario.
     * Ogni copia: se ne girano due per lo stesso Pokemon, nessuna deve
     * sopravvivere al deposito.
     */
    public static void forget(ServerPlayer player, UUID pokemon) {
        final ItemStack cintura = Belts.inBeltSlot(player);
        if (punta(cintura, pokemon)) {
            // la ball nuda che si portava addosso
            Belts.store(player, ItemStack.EMPTY);
        } else if (cintura.getItem() instanceof TrainerBeltItem belt) {
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

    /**
     * Se il Pokemon sta in una ball che il giocatore ha addosso. Vale anche la
     * ball nuda nello slot cintura, che e' il caso che mi era sfuggito: non
     * essendo una cintura non veniva guardata, e al rientro se ne creava una
     * seconda.
     */
    public static boolean onBelt(ServerPlayer player, UUID pokemon) {
        for (UUID addosso : BeltParty.wanted(player)) {
            if (pokemon.equals(addosso)) {
                return true;
            }
        }
        return false;
    }

    /** Se il giocatore ha da qualche parte una ball che punta questo Pokemon. */
    public static boolean anywhere(ServerPlayer player, UUID pokemon) {
        if (onBelt(player, pokemon)) {
            return true;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (punta(player.getInventory().getItem(i), pokemon)) {
                return true;
            }
        }
        return false;
    }

    /** Toglie dall'inventario — e solo da li' — la ball di questo Pokemon. */
    public static void takeFromInventory(ServerPlayer player, UUID pokemon) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (punta(player.getInventory().getItem(i), pokemon)) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
                return;
            }
        }
    }

    /**
     * Rimette in mano la ball di un Pokemon: nell'inventario se c'e' posto,
     * altrimenti a terra dove indicato. La ball e' identica a quella di prima —
     * l'handle non si inventa, lo tiene il Pokemon nei suoi dati.
     */
    public static void give(ServerPlayer player, Pokemon mon, Vec3 dove) {
        final String padrone = mon.getPersistentData().getString(BallLink.OWNER);
        final UUID handle = padrone.isEmpty() ? UUID.randomUUID() : UUID.fromString(padrone);
        if (padrone.isEmpty()) {
            mon.getPersistentData().putString(BallLink.OWNER, handle.toString());
        }
        final ItemStack ball = mon.getCaughtBall().stack(1);
        ball.set(ModBallData.BALL_LINK.get(), BallLink.of(mon, handle));
        // un secondo di respiro: la ball torna in mano nell'istante in cui il
        // Pokemon rientra, e senza pausa un doppio clic lo rispedisce fuori
        // prima che l'animazione di rientro sia finita
        player.getCooldowns().addCooldown(ball.getItem(), RESPIRO);
        if (player.getInventory().add(ball)) {
            return;
        }
        final ItemEntity caduta = new ItemEntity(player.serverLevel(), dove.x, dove.y, dove.z, ball);
        player.serverLevel().addFreshEntity(caduta);
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
