package com.kingtrapinch.tfcobblemon.dig;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(modid = TFCobblemon.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class DigNetwork {
    private DigNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final var registrar = event.registrar("1");
        registrar.playToServer(DigPayload.TYPE, DigPayload.STREAM_CODEC, DigNetwork::onDig);
        registrar.playToClient(DigSyncPayload.TYPE, DigSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> ClientDigState.accept(payload)));
    }

    private static void onDig(DigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.containerMenu instanceof DigMenu menu)
                    || menu.site() == null
                    || !(player.level() instanceof ServerLevel level)) {
                return;
            }
            final DigSiteBlockEntity be = menu.site();
            final DigSite site = be.site(level);
            if (site.exhausted() || payload.cell() < 0 || payload.cell() >= DigSite.CELLS) {
                return;
            }
            final DigTool tool = DigTool.values()[Math.floorMod(payload.tool(), DigTool.values().length)];
            final ItemStack held = player.getInventory().getItem(payload.slot());
            if (!tool.matches(held)) {
                return;
            }

            final int cx = payload.cell() % DigSite.SIZE;
            final int cy = payload.cell() / DigSite.SIZE;
            // dispari centrato sulla cella, pari ancorato in alto a sinistra
            final int offset = tool.size % 2 == 0 ? 0 : tool.size / 2;
            boolean bit = false;
            for (int dy = 0; dy < tool.size; dy++) {
                for (int dx = 0; dx < tool.size; dx++) {
                    final int x = cx - offset + dx;
                    final int y = cy - offset + dy;
                    if (x >= 0 && x < DigSite.SIZE && y >= 0 && y < DigSite.SIZE) {
                        bit |= site.strip(x, y, tool);
                    }
                }
            }

            // l'attrezzo si consuma anche a vuoto: la spazzola sulla roccia non
            // combina niente ma le setole si rovinano comunque
            held.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            if (tool == DigTool.BRUSH) {
                if (level.random.nextBoolean()) {
                    site.spend(1);
                }
            } else if (bit) {
                site.spend(tool.siteCost(held));
            }

            be.harvest();
            be.setChanged();
            player.connection.send(new DigSyncPayload(site.snapshot(), site.durability()));
        });
    }
}
