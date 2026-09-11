package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.block.PCBlock;
import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Le abitudini della cintura e delle ball piene. */
@EventBusSubscriber(modid = TFCobblemon.MODID)
public final class BeltEvents {
    private BeltEvents() {}

    /**
     * Una ball con un Pokemon dentro non si perde: a terra non scade e non la
     * intacca niente. Il Pokemon vive nel deposito, ma la ball e' l'unico modo
     * di richiamarlo senza passare dal PC, e vederla bruciare in una colata di
     * lava sarebbe una punizione sproporzionata.
     */
    @SubscribeEvent
    public static void ballPieneResistono(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity item && BallLink.filled(item.getItem())) {
            item.setUnlimitedLifetime();
            item.setInvulnerable(true);
        }
    }

    /**
     * Shift + tasto destro con una ball in mano la infila nella cintura, se c'e'
     * posto. Senza questo si dovrebbe passare dall'inventario ogni volta, e la
     * cintura si riempie molto piu' spesso di quanto si apra una schermata.
     */
    @SubscribeEvent
    public static void infilaConLoShift(PlayerInteractEvent.RightClickItem event) {
        final Player player = event.getEntity();
        final ItemStack inMano = event.getItemStack();
        if (!player.isShiftKeyDown() || !TrainerBeltItem.isBall(inMano)) {
            return;
        }
        final ItemStack cintura = Belts.worn(player);
        if (!(cintura.getItem() instanceof TrainerBeltItem belt) || !belt.haPosto(cintura)) {
            // cintura piena: la ball si lancia, come sempre
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!player.level().isClientSide() && Belts.insert(player, inMano)) {
            clic(player);
        }
    }

    /**
     * Una ball piena raccolta da terra va sulla cintura, se c'e' un posto: e'
     * un Pokemon, il suo posto e' addosso, non in fondo allo zaino.
     */
    @SubscribeEvent
    public static void raccoltaVaSullaCintura(ItemEntityPickupEvent.Pre event) {
        final ItemStack ball = event.getItemEntity().getItem();
        if (ball.getCount() != 1 || !BallLink.filled(ball)) {
            return;
        }
        final Player player = event.getPlayer();
        if (player.level().isClientSide() || !Belts.insert(player, ball)) {
            return;
        }
        event.setCanPickup(TriState.FALSE);
        event.getItemEntity().discard();
        clic(player);
    }

    /**
     * Al PC si fa ordine: tutti dentro le proprie ball, poi si allinea. Aprire
     * il deposito e trovarci dentro un Pokemon che sta pascolando nel prato
     * sarebbe un modo eccellente di perderlo.
     */
    @SubscribeEvent
    public static void alPcSiFaOrdine(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getLevel().getBlockState(event.getPos()).getBlock() instanceof PCBlock) {
            BeltParty.tidy(player);
        }
    }

    /** Il verso del sacco, sentito da chi lo fa e non dal mondo intorno. */
    static void clic(Player player) {
        if (player instanceof ServerPlayer chi) {
            chi.playNotifySound(SoundEvents.BUNDLE_INSERT, chi.getSoundSource(), 0.8F, 1.0F);
        }
    }
}
