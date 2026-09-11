package com.kingtrapinch.tfcobblemon.client;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.belt.BallLink;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/**
 * Cosa c'e' dentro una ball, scritto sotto il suo nome. La ball non e' nostra —
 * e' un item di Cobblemon — quindi il tooltip si aggiunge dall'evento invece
 * che da {@code appendHoverText}.
 */
@EventBusSubscriber(modid = TFCobblemon.MODID, value = Dist.CLIENT)
public final class BallTooltip {
    private BallTooltip() {}

    @SubscribeEvent
    public static void cosaCiSta(ItemTooltipEvent event) {
        final BallLink legame = BallLink.read(event.getItemStack());
        if (legame == null) {
            return;
        }
        final MutableComponent nome = Component.empty().append(legame.label());
        final Component sesso = legame.genderMark();
        if (sesso != null) {
            nome.append(" ").append(sesso);
        }
        if (legame.shiny()) {
            nome.append(" ").append(Component.literal("★").withStyle(ChatFormatting.GOLD));
        }

        final List<Component> righe = event.getToolTip();
        final int dove = Math.min(1, righe.size());
        righe.add(dove, Component.literal("Lv. " + ClientBallLevels.level(legame.pokemon(), legame.level()))
                .withStyle(ChatFormatting.GRAY));
        righe.add(dove, nome.withStyle(ChatFormatting.WHITE));
    }

    @SubscribeEvent
    public static void esceDalMondo(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientBallLevels.forget();
    }
}
