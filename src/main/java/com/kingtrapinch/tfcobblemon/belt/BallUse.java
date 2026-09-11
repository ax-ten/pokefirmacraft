package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingtrapinch.tfcobblemon.TFCobblemon;
import kotlin.Unit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * La ball come maniglia del suo Pokemon: tasto destro e esce, tasto destro e
 * rientra. Non c'e' da selezionare niente in un elenco prima — la ball
 * <em>e'</em> la selezione.
 */
@EventBusSubscriber(modid = TFCobblemon.MODID)
public final class BallUse {
    private BallUse() {}

    /** Quanto avanti al giocatore compare il Pokemon evocato. */
    private static final double DAVANTI = 2.0D;

    /**
     * Tasto destro su una ball piena: se il Pokemon e' fuori rientra, se e'
     * dentro esce. E' quello che farebbe R sull'elenco di sinistra, solo che il
     * bersaglio e' quello puntato dalla ball in mano.
     */
    @SubscribeEvent
    public static void evocaORichiama(PlayerInteractEvent.RightClickItem event) {
        final ItemStack ball = event.getItemStack();
        final BallLink legame = BallLink.read(ball);
        if (legame == null || event.getEntity().isShiftKeyDown()) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        final Pokemon mon = trova(player, legame.pokemon());
        if (mon == null) {
            avvisa(player, "tfcobblemon.ball.lontano");
            return;
        }
        if (!nostro(mon, legame)) {
            avvisa(player, "tfcobblemon.ball.non_risponde");
            return;
        }

        if (mon.getEntity() != null) {
            mon.tryRecallWithAnimation();
            return;
        }
        final Vec3 dove = player.getEyePosition().add(player.getLookAngle().scale(DAVANTI));
        mon.sendOutWithAnimation(player, level, dove, null, true, null, entity -> Unit.INSTANCE);
    }

    /**
     * Tasto destro con una ball vuota su un proprio Pokemon fuori: lo richiama
     * dentro quella ball, e da quel momento e' la sua. Una ball vuota addosso a
     * un Pokemon selvatico resta il lancio di sempre, che non passa da qui.
     */
    @SubscribeEvent
    public static void legaAlProprio(PlayerInteractEvent.EntityInteract event) {
        final ItemStack ball = event.getItemStack();
        if (!TrainerBeltItem.isBall(ball) || BallLink.filled(ball)
                || !(event.getTarget() instanceof PokemonEntity target)) {
            return;
        }
        final Player player = event.getEntity();
        final Pokemon mon = target.getPokemon();
        if (mon.isWild() || !player.getUUID().equals(mon.getOwnerUUID())) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(player instanceof ServerPlayer)) {
            return;
        }

        // l'handle e' l'identita' della ball: il Pokemon si ricorda di questa e
        // di nessun'altra, cosi' una copia dell'item non risponde piu'
        final UUID handle = UUID.randomUUID();
        mon.getPersistentData().putString(BallLink.OWNER, handle.toString());

        final ItemStack legata = ball.split(1);
        legata.set(ModBallData.BALL_LINK.get(), BallLink.of(mon, handle));
        mon.recall();
        if (!player.getInventory().add(legata)) {
            player.drop(legata, false);
        }
    }

    /** Il Pokemon puntato, cercato dove vive: prima la squadra, poi il PC. */
    static @Nullable Pokemon trova(ServerPlayer player, UUID pokemon) {
        final var storage = Cobblemon.INSTANCE.getStorage();
        final Pokemon inSquadra = storage.getParty(player).get(pokemon);
        return inSquadra != null ? inSquadra : storage.getPC(player).get(pokemon);
    }

    /** Se questa ball e' ancora quella che il Pokemon riconosce. */
    static boolean nostro(Pokemon mon, BallLink legame) {
        final String padrone = mon.getPersistentData().getString(BallLink.OWNER);
        return padrone.isEmpty() || padrone.equals(legame.handle().toString());
    }

    private static void avvisa(ServerPlayer player, String chiave) {
        player.displayClientMessage(
                Component.translatable(chiave).withStyle(ChatFormatting.GRAY), true);
    }
}
