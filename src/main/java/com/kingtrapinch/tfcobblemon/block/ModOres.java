package com.kingtrapinch.tfcobblemon.block;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * La tumblestone dentro la roccia, alla maniera di TFC.
 *
 * <p>Il geode di TFC non ha cristalli appesi nel vuoto: e' una sacca di
 * quarzite con vene di ametista <em>nella</em> roccia, e la cavita' e' vuota.
 * Questo e' il nostro equivalente — e la ragione non e' solo di stile: un
 * cluster e' un blocco attaccato, vive e muore col suo appoggio, e ogni
 * grattacapo che abbiamo avuto coi geodi veniva da li'. Una vena nella roccia
 * non ha appoggio da perdere.
 *
 * <p>Il modello e' quello di TFC ({@code tfc:block/ore}) con la quarzite sotto
 * e il nostro strato sopra: un minerale di TFC e' fatto cosi', e non c'e'
 * motivo di farlo diversamente.
 */
public final class ModOres {
    private ModOres() {}

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(TFCobblemon.MODID);

    /** Tumblestone in quarzite: la roccia in cui TFC mette i suoi geodi. */
    public static final DeferredBlock<Block> TUMBLESTONE_QUARTZITE =
            BLOCKS.register("tumblestone_quartzite", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.TERRACOTTA_WHITE)
                            .strength(6.0F, 10.0F)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
