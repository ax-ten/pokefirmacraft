package com.kingtrapinch.tfcobblemon.block;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * I sacchi da boxe, tre stadi della stessa tela.
 *
 * <p>Sono blocchi distinti e non uno con una proprieta' di usura, per la stessa
 * ragione per cui l'incudine di vanilla ne ha tre: uno stadio che si porta
 * dietro l'item giusto quando lo si stacca dal soffitto, e un sacco mezzo
 * sfondato che resta mezzo sfondato anche in inventario.
 */
public final class ModBags {
    private ModBags() {}

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(TFCobblemon.MODID);
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TFCobblemon.MODID);

    private static BlockBehaviour.Properties tela() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_YELLOW)
                .strength(0.8F)
                .sound(SoundType.WOOL)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY);
    }

    /** L'ultimo stadio: chi lo usa ancora se lo sfonda e non resta niente. */
    public static final DeferredBlock<PunchingBagBlock> TORN =
            BLOCKS.register("torn_punching_bag", () -> new PunchingBagBlock(tela(), null));

    public static final DeferredBlock<PunchingBagBlock> WORN =
            BLOCKS.register("worn_punching_bag", () -> new PunchingBagBlock(tela(), TORN::get));

    public static final DeferredBlock<PunchingBagBlock> WHOLE =
            BLOCKS.register("punching_bag", () -> new PunchingBagBlock(tela(), WORN::get));

    public static final DeferredItem<Item> TORN_ITEM =
            ITEMS.register("torn_punching_bag", () -> new BlockItem(TORN.get(), new Item.Properties()));
    public static final DeferredItem<Item> WORN_ITEM =
            ITEMS.register("worn_punching_bag", () -> new BlockItem(WORN.get(), new Item.Properties()));
    public static final DeferredItem<Item> WHOLE_ITEM =
            ITEMS.register("punching_bag", () -> new BlockItem(WHOLE.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
