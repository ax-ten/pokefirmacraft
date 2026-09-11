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
        // una riga sola, nell'ordine in cui la si legge: livello, nome, sesso
        final MutableComponent riga = Component.empty()
                .append(Component.literal("Lv. " + ClientBallLevels.level(legame.pokemon(), legame.level()))
                        .withStyle(ChatFormatting.GRAY))
                .append(" ")
                .append(legame.label().copy().withStyle(ChatFormatting.WHITE));
        final Component sesso = legame.genderMark();
        if (sesso != null) {
            riga.append(" ").append(sesso);
        }
        if (legame.shiny()) {
            riga.append(" ").append(Component.literal("★").withStyle(ChatFormatting.GOLD));
        }

        final List<Component> righe = event.getToolTip();
        righe.add(Math.min(1, righe.size()), riga);
    }

    @SubscribeEvent
    public static void esceDalMondo(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientBallLevels.forget();
    }
}
