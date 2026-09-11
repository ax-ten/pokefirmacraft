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
     * Shift + tasto destro: quello che hai in mano va addosso.
     *
     * <p>Nell'ordine: se indossi una cintura con un posto libero la ball ci
     * finisce dentro; se lo slot cintura e' vuoto ci finisce quello che hai in
     * mano, cintura o ball nuda che sia; altrimenti niente. Un gesto solo per
     * tutta la faccenda, senza aprire nessuna schermata.
     *
     * <p><b>E una ball piena non si lancia mai.</b> Il lancio di Cobblemon
     * consuma l'oggetto e manda in volo una ball vuota: su una ball piena
     * vorrebbe dire buttare via l'unico modo di richiamare quel Pokemon. Quindi
     * se non c'e' posto dove metterla il gesto non fa niente, invece di fare la
     * cosa sbagliata.
     */
    @SubscribeEvent
    public static void addossoConLoShift(PlayerInteractEvent.RightClickItem event) {
        final Player player = event.getEntity();
        final ItemStack inMano = event.getItemStack();
        final boolean ball = TrainerBeltItem.isBall(inMano);
        if (!player.isShiftKeyDown() || (!ball && !(inMano.getItem() instanceof TrainerBeltItem))) {
            return;
        }

        final ItemStack cintura = Belts.worn(player);
        final boolean dentroLaCintura = ball
                && cintura.getItem() instanceof TrainerBeltItem belt && belt.haPosto(cintura);
        final boolean slotLibero = Belts.inBeltSlot(player).isEmpty();
        if (!dentroLaCintura && !slotLibero && !BallLink.filled(inMano)) {
            // ball vuota e nessun posto dove metterla: si lancia, come sempre
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(dentroLaCintura || slotLibero
                ? InteractionResult.SUCCESS : InteractionResult.FAIL);
        if (player.level().isClientSide()) {
            return;
        }
        if (dentroLaCintura ? Belts.insert(player, inMano) : Belts.equip(player, inMano)) {
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
