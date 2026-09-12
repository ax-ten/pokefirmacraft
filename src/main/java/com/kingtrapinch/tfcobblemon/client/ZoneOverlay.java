package com.kingtrapinch.tfcobblemon.client;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.zone.ZoneArea;
import com.kingtrapinch.tfcobblemon.zone.ZoneBlockEntity;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Il riquadro che mostra fin dove arriva una zona.
 *
 * <p>E' un interruttore da debug, ma non e' un vezzo da sviluppatore: l'area la
 * si conta in silenzio, e senza un modo di vederla il giocatore non ha nessun
 * appiglio per capire perche' un sacco messo la' non conta e uno messo qua si'.
 *
 * <p>Sta tutto sul client e non viaggia: l'interruttore e' nella finestra, il
 * disegno lo fa qui, e il server non ne sa niente.
 */
@EventBusSubscriber(modid = TFCobblemon.MODID, value = Dist.CLIENT)
public final class ZoneOverlay {
    private ZoneOverlay() {}

    /** Fin dove si cercano i controllori da illuminare. */
    private static final int VICINO = 24;

    private static boolean mostra;

    public static boolean mostra() {
        return mostra;
    }

    public static void gira() {
        mostra = !mostra;
    }

    @SubscribeEvent
    public static void disegna(RenderLevelStageEvent event) {
        if (!mostra || event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        final Minecraft gioco = Minecraft.getInstance();
        if (gioco.level == null || gioco.player == null) {
            return;
        }
        final var occhio = event.getCamera().getPosition();
        final VertexConsumer righe = gioco.renderBuffers().bufferSource()
                .getBuffer(RenderType.lines());
        final BlockPos dove = gioco.player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
                dove.offset(-VICINO, -VICINO, -VICINO), dove.offset(VICINO, VICINO, VICINO))) {
            if (!(gioco.level.getBlockEntity(pos) instanceof ZoneBlockEntity)) {
                continue;
            }
            final AABB stanza = new AABB(
                    pos.getX() - ZoneArea.LATO, pos.getY() - ZoneArea.GIU, pos.getZ() - ZoneArea.LATO,
                    pos.getX() + ZoneArea.LATO + 1, pos.getY() + ZoneArea.SU + 1,
                    pos.getZ() + ZoneArea.LATO + 1).move(-occhio.x, -occhio.y, -occhio.z);
            LevelRenderer.renderLineBox(event.getPoseStack(), righe, stanza,
                    1.0F, 0.85F, 0.2F, 0.9F);
        }
        gioco.renderBuffers().bufferSource().endBatch(RenderType.lines());
    }
}
