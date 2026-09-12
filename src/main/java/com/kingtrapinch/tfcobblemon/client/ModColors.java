package com.kingtrapinch.tfcobblemon.client;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.block.ModBags;
import com.kingtrapinch.tfcobblemon.block.PunchingBagBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * I colori messi a schermo invece che nelle texture.
 *
 * <p>La fascia di un sacco da boxe e' una sola texture in bianco e nero, e il
 * colore della statistica ce lo mette il tint moltiplicando: cosi' la tela
 * dipinta resta tela invece di diventare vernice, e diciotto sacchi stanno in
 * quattro texture invece di ventuno.
 */
@EventBusSubscriber(modid = TFCobblemon.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModColors {
    private ModColors() {}

    /** Il colore della statistica che quel sacco allena. */
    private static int colore(Block blocco) {
        return blocco instanceof PunchingBagBlock sacco
                ? 0xFF000000 | ModBags.STATS.getOrDefault(sacco.allena(), 0xFFFFFF)
                : -1;
    }

    @SubscribeEvent
    public static void blocchi(RegisterColorHandlersEvent.Block event) {
        ModBags.tutti().forEach(sacco -> event.register(
                (BlockState stato, net.minecraft.world.level.BlockAndTintGetter mondo,
                 net.minecraft.core.BlockPos pos, int tint) ->
                        tint == 0 ? colore(stato.getBlock()) : -1,
                sacco.get()));
    }

    @SubscribeEvent
    public static void oggetti(RegisterColorHandlersEvent.Item event) {
        ModBags.tutti().forEach(sacco -> event.register(
                (ItemStack cosa, int tint) ->
                        tint == 0 ? colore(Block.byItem(cosa.getItem())) : -1,
                sacco.get()));
    }
}
