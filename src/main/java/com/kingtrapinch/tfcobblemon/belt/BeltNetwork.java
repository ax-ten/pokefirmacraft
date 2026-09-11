package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.client.ClientBallLevels;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(modid = TFCobblemon.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class BeltNetwork {
    private BeltNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final var registrar = event.registrar("1");
        registrar.playToServer(BallProbePayload.TYPE, BallProbePayload.STREAM_CODEC, BeltNetwork::onProbe);
        registrar.playToServer(RecallPayload.TYPE, RecallPayload.STREAM_CODEC, BeltNetwork::onRecall);
        registrar.playToClient(BallLevelPayload.TYPE, BallLevelPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientBallLevels.accept(payload)));
    }

    /**
     * Il rientro chiesto dalla ruota delle interazioni. La ruota e' di
     * Cobblemon e gira sul client, quindi il gesto arriva qui; e arriva per
     * UUID e non per posto in squadra, perche' un compagno di viaggio in
     * squadra non ci sta.
     */
    private static void onRecall(RecallPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            final Pokemon mon = BeltParty.trova(player, payload.pokemon());
            if (mon == null || mon.getEntity() == null
                    || !player.getUUID().equals(mon.getOwnerUUID())) {
                return;
            }
            mon.tryRecallWithAnimation();
        });
    }

    /**
     * Il livello in cache sulla ball e' quello di quando il Pokemon vi e'
     * rientrato l'ultima volta: cresce mentre e' fuori, e una ball nello zaino
     * non si accorge di niente. Quindi il tooltip lo richiede, e qui si guarda
     * dove il Pokemon vive davvero — prima la squadra, poi il PC.
     */
    private static void onProbe(BallProbePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            final Pokemon mon = BeltParty.trova(player, payload.pokemon());
            if (mon != null) {
                PacketDistributor.sendToPlayer(player,
                        new BallLevelPayload(payload.pokemon(), mon.getLevel()));
            }
        });
    }
}
