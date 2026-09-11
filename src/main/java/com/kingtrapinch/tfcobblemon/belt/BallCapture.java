package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Quello che succede appena un Pokemon e' catturato: la sua ball diventa un
 * oggetto vero in mano al giocatore.
 *
 * <p>Non e' una ball qualsiasi. Cobblemon si ricorda con che ball l'hai preso
 * ({@code Pokemon.getCaughtBall()}), e quella e' la sua per sempre: non si
 * richiama un Pokemon dentro un'altra.
 *
 * <p>E la ball cade a terra dove e' caduto il lancio, da raccogliere. E' il
 * gesto dei giochi, e non c'e' fretta: una ball piena non scade e non la
 * intacca niente. Raccogliendola finisce sulla cintura da se', se c'e' posto.
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

        // la ball cade dove e' caduta, e la si va a raccogliere: e' il gesto che
        // fanno i giochi, e raccogliendola finisce sulla cintura da se'. Non
        // scade e non la intacca niente, quindi aspetta quanto serve.
        final EmptyPokeBallEntity lancio = event.getPokeBallEntity();
        final Vec3 dove = lancio == null ? player.position() : lancio.position();
        player.serverLevel().addFreshEntity(
                new ItemEntity(player.serverLevel(), dove.x, dove.y, dove.z, ball));
    }
}
