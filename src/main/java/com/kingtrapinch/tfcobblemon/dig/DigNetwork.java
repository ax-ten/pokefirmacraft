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
            // a scavo finito il sito e' fermo: aspetta solo di chiudersi
            if (be.finito() || site.exhausted()
                    || payload.cell() < 0 || payload.cell() >= DigSite.CELLS) {
                return;
            }
            final DigTool tool = DigTool.values()[Math.floorMod(payload.tool(), DigTool.values().length)];
            final ItemStack held = player.getInventory().getItem(payload.slot());
            if (!tool.matches(held)) {
                return;
            }

            // quali tesori si vedevano prima del colpo, per capire se ne esce uno
            final boolean[] visti = new boolean[be.buried().size()];
            for (int i = 0; i < visti.length; i++) {
                visti[i] = be.glimpsed(i);
            }

            final int cx = payload.cell() % DigSite.SIZE;
            final int cy = payload.cell() / DigSite.SIZE;
            final boolean bit = switch (tool) {
                case HAMMER -> hammer(site, level, cx, cy);
                case PICKAXE -> pickaxe(site, level, cx, cy);
                case CHISEL -> chisel(site, level, cx, cy);
                case BRUSH -> brush(site, cx, cy);
            };

            // l'attrezzo si consuma anche a vuoto: la spazzola sulla roccia non
            // combina niente ma le setole si rovinano comunque
            held.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            if (tool == DigTool.BRUSH) {
                if (level.random.nextBoolean()) {
                    site.spend(1);
                }
            } else if (bit) {
                final int cost = tool == DigTool.HAMMER || tool == DigTool.PICKAXE
                        ? tool.siteCost(held) * site.kind().hammerPenalty
                        : tool.siteCost(held);
                site.spend(cost);
            }

            be.setChanged();
            int trovato = -1;
            for (int i = 0; i < visti.length; i++) {
                if (!visti[i] && be.glimpsed(i)) {
                    trovato = i;
                    break;
                }
            }
            if (trovato >= 0) {
                player.playNotifySound(net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.2F);
            }
            player.connection.send(new DigSyncPayload(site.snapshot(), site.durability(), trovato));

            // finito il sito si sbriciola, e quello che non hai tirato fuori
            // resta sotto: non cade niente
            if (site.exhausted()) {
                be.collapse(level);
                player.closeContainer();
            }
        });
    }

    /**
     * Sul pulviscolo il ferro non fa presa: il martello dimezza le probabilita'
     * di portare via lo strato, lo scalpello riesce solo a incrinarlo. Per la
     * sabbia c'e' la spazzola.
     */
    private static boolean loose(DigSite site, int x, int y) {
        return site.layerAt(x, y) == Layer.DUST;
    }

    /**
     * Il martello prende un diamante di raggio due. Al centro sfonda; intorno,
     * nel quadrato, tre volte su dieci resta solo incrinato; sulle quattro
     * punte cede una volta su due, altrimenti niente.
     */
    private static boolean hammer(DigSite site, ServerLevel level, int cx, int cy) {
        boolean any = colpo(site, level, cx, cy, DigTool.HAMMER, 1.0F);
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                any |= colpo(site, level, cx + dx, cy + dy, DigTool.HAMMER, 0.70F);
            }
        }
        for (int[] tip : new int[][] {{2, 0}, {-2, 0}, {0, 2}, {0, -2}}) {
            if (level.random.nextFloat() < 0.50F) {
                any |= colpo(site, level, cx + tip[0], cy + tip[1], DigTool.HAMMER, 1.0F);
            }
        }
        return any;
    }

    /** Il piccone fa una croce: al centro sfonda, sui quattro bracci sette volte su dieci. */
    private static boolean pickaxe(DigSite site, ServerLevel level, int cx, int cy) {
        boolean any = colpo(site, level, cx, cy, DigTool.PICKAXE, 1.0F);
        for (int[] arm : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            any |= colpo(site, level, cx + arm[0], cy + arm[1], DigTool.PICKAXE, 0.70F);
        }
        return any;
    }

    /** Lo scalpello prende la cella, e tre volte su dieci anche quella sotto. */
    private static boolean chisel(DigSite site, ServerLevel level, int cx, int cy) {
        boolean any = colpo(site, level, cx, cy, DigTool.CHISEL, 1.0F);
        if (any && level.random.nextFloat() < 0.30F) {
            any |= colpo(site, level, cx, cy, DigTool.CHISEL, 1.0F);
        }
        return any;
    }

    /**
     * Un colpo che cede con probabilita' {@code chance}, dimezzata sul
     * pulviscolo; lo scalpello sul pulviscolo si limita a incrinare.
     */
    private static boolean colpo(DigSite site, ServerLevel level, int x, int y,
                                 DigTool tool, float chance) {
        if (x < 0 || x >= DigSite.SIZE || y < 0 || y >= DigSite.SIZE) {
            return false;
        }
        if (loose(site, x, y)) {
            if (tool == DigTool.CHISEL) {
                return site.hit(x, y, tool, false);
            }
            chance *= 0.5F;
        }
        return site.hit(x, y, tool, chance >= 1.0F || level.random.nextFloat() < chance);
    }

    private static boolean brush(DigSite site, int cx, int cy) {
        boolean any = false;
        for (int dy = 0; dy < DigTool.BRUSH.size; dy++) {
            for (int dx = 0; dx < DigTool.BRUSH.size; dx++) {
                any |= at(site, cx + dx, cy + dy, DigTool.BRUSH, true);
            }
        }
        return any;
    }

    private static boolean at(DigSite site, int x, int y, DigTool tool, boolean full) {
        if (x < 0 || x >= DigSite.SIZE || y < 0 || y >= DigSite.SIZE) {
            return false;
        }
        return site.hit(x, y, tool, full);
    }
}
