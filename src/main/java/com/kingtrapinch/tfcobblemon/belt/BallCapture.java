package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * Quello che succede appena un Pokemon e' catturato: la sua ball diventa un
 * oggetto vero in mano al giocatore.
 *
 * <p>Non e' una ball qualsiasi. Cobblemon si ricorda con che ball l'hai preso
 * ({@code Pokemon.getCaughtBall()}), e quella e' la sua per sempre: non si
 * richiama un Pokemon dentro un'altra. Se la cintura ha un posto libero la ball
 * ci finisce da se', altrimenti resta nell'inventario.
 */
public final class BallCapture {
    private BallCapture() {}

    public static void hook() {
        CobblemonEvents.POKEMON_CAPTURED.subscribe(BallCapture::catturato);
    }

    private static void catturato(PokemonCapturedEvent event) {
        final Pokemon mon = event.getPokemon();
        final ServerPlayer player = event.getPlayer();

        // l'handle e' l'identita' della ball: il Pokemon si ricorda di questa e
        // di nessun'altra, cosi' una copia dell'item non risponde piu'
        final UUID handle = UUID.randomUUID();
        mon.getPersistentData().putString(BallLink.OWNER, handle.toString());

        final ItemStack ball = mon.getCaughtBall().stack(1);
        ball.set(ModBallData.BALL_LINK.get(), BallLink.of(mon, handle));
        consegna(player, ball);
    }

    /** Prima la cintura, poi l'inventario, e in ultimo per terra. */
    static void consegna(ServerPlayer player, ItemStack ball) {
        if (Belts.insert(player, ball)) {
            BeltEvents.clic(player);
            return;
        }
        if (!player.getInventory().add(ball)) {
            player.drop(ball, false);
        }
    }
}
