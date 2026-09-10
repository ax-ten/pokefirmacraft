package com.kingtrapinch.tfcobblemon.dig;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Il kit dell'archeologo e i siti di scavo.
 *
 * <p>TFC non ha blocchi sospetti — l'unico "suspicious" nei suoi dati e' lo
 * stufato — quindi i siti sono nostri, fatti perche' si confondano con la
 * sabbia e la ghiaia di TFC invece che con quelle vanilla.
 */
public final class ModDig {
    private ModDig() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TFCobblemon.MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TFCobblemon.MODID);
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, TFCobblemon.MODID);

    /** Quanto dura ogni attrezzo del kit appena costruito. */
    public static final KitWear NUOVO = new KitWear(64, 128, 192);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<KitWear>> KIT_WEAR =
            COMPONENTS.register("kit_wear", () -> DataComponentType.<KitWear>builder()
                    .persistent(KitWear.CODEC)
                    .networkSynchronized(KitWear.STREAM_CODEC)
                    .build());

    public static final DeferredItem<Item> ARCHAEOLOGIST_KIT =
            ITEMS.register("archaeologist_kit", () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .component(KIT_WEAR.get(), NUOVO)));

    /** Le sabbie di TFC, e le ghiaie una per tipo di roccia. */
    public static final List<String> SANDS = List.of("black", "brown", "green", "pink", "red", "white", "yellow");
    public static final List<String> GRAVELS = List.of("andesite", "basalt", "chalk", "chert", "claystone", "conglomerate", "dacite", "diorite", "dolomite", "gabbro", "gneiss", "granite", "limestone", "marble", "phyllite", "quartzite", "rhyolite", "schist", "shale", "slate", "tuff");

    public static final Map<String, DeferredHolder<Block, Block>> SUSPICIOUS_SAND =
            siti("suspicious_sand", SANDS, MapColor.SAND, SoundType.SAND);
    public static final Map<String, DeferredHolder<Block, Block>> SUSPICIOUS_GRAVEL =
            siti("suspicious_gravel", GRAVELS, MapColor.STONE, SoundType.GRAVEL);

    /**
     * Un sito per variante: la texture e' quella di TFC con sopra il pulviscolo
     * dei suspicious vanilla, cosi' lo scavo si nota ma non stona col terreno.
     * Non si raccolgono e non droppano niente: si aprono e si scavano.
     */
    private static Map<String, DeferredHolder<Block, Block>> siti(
            String prefisso, List<String> varianti, MapColor colore, SoundType suono) {
        final Map<String, DeferredHolder<Block, Block>> mappa = new LinkedHashMap<>();
        for (String variante : varianti) {
            mappa.put(variante, BLOCKS.register(prefisso + "/" + variante,
                    () -> new Block(BlockBehaviour.Properties.of()
                            .mapColor(colore)
                            .strength(0.5F)
                            .sound(suono)
                            .noLootTable())));
        }
        return mappa;
    }

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
    }
}
