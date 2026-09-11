package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingtrapinch.tfcobblemon.TFCobblemon;
import kotlin.Unit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
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
     * In combattimento le ball dell'inventario non si usano: valgono solo
     * quelle che si hanno addosso. Chi si e' preparato male resta con quello che
     * ha alla cintura.
     *
     * <p>Il blocco e' di parte server, che e' l'unica che sa delle battaglie: il
     * client muove il braccio e non succede niente, che e' brutto ma innocuo —
     * il lancio di una ball lo fa il server, non lui.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void nienteBallInCombattimento(PlayerInteractEvent.RightClickItem event) {
        if (!TrainerBeltItem.isBall(event.getItemStack())
                || !(event.getEntity() instanceof ServerPlayer player)
                || BattleRegistry.getBattleByParticipatingPlayer(player) == null) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
        avvisa(player, "tfcobblemon.ball.in_combattimento");
    }

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
