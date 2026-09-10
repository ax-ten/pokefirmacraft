package com.kingtrapinch.tfcobblemon.dig;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
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

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TFCobblemon.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TFCobblemon.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TFCobblemon.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, TFCobblemon.MODID);

    /** Le sabbie di TFC, e le ghiaie una per tipo di roccia. */
    public static final List<String> SANDS = List.of("black", "brown", "green", "pink", "red", "white", "yellow");
    public static final List<String> GRAVELS = List.of("andesite", "basalt", "chalk", "chert", "claystone", "conglomerate", "dacite", "diorite", "dolomite", "gabbro", "gneiss", "granite", "limestone", "marble", "phyllite", "quartzite", "rhyolite", "schist", "shale", "slate", "tuff");

    public static final Map<String, DeferredBlock<DigSiteBlock>> SUSPICIOUS_SAND =
            siti("suspicious_sand", SANDS, MapColor.SAND, SoundType.SAND);
    public static final Map<String, DeferredBlock<DigSiteBlock>> SUSPICIOUS_GRAVEL =
            siti("suspicious_gravel", GRAVELS, MapColor.STONE, SoundType.GRAVEL);
    /** Anche la pietra viva puo' essere sospetta, non solo lo sciolto. */
    public static final Map<String, DeferredBlock<DigSiteBlock>> SUSPICIOUS_STONE =
            siti("suspicious_stone", GRAVELS, MapColor.STONE, SoundType.STONE);
    /**
     * Il sito raro: due strati di cristallo, la spazzola non ci fa niente e il
     * martello lo sbriciola in quattro colpi. Dentro c'e' la roba buona.
     */
    public static final List<String> CRYSTALS = List.of("amethyst", "diamond", "emerald",
            "lapis", "tumblestone", "sky_tumblestone", "black_tumblestone");

    public static final Map<String, DeferredBlock<DigSiteBlock>> SUSPICIOUS_CRYSTAL =
            siti("suspicious_crystal", CRYSTALS, MapColor.COLOR_PURPLE, SoundType.AMETHYST);

    /**
     * Un sito per variante: la texture e' quella di TFC con sopra il pulviscolo
     * dei suspicious vanilla, cosi' lo scavo si nota ma non stona col terreno.
     * Non si raccolgono e non droppano niente: si aprono e si scavano.
     */
    private static Map<String, DeferredBlock<DigSiteBlock>> siti(
            String prefisso, List<String> varianti, MapColor colore, SoundType suono) {
        final Map<String, DeferredBlock<DigSiteBlock>> mappa = new LinkedHashMap<>();
        for (String variante : varianti) {
            final var blocco = BLOCKS.register(prefisso + "/" + variante,
                    () -> new DigSiteBlock(BlockBehaviour.Properties.of()
                            .mapColor(colore)
                            .strength(0.5F)
                            .sound(suono)
                            .noLootTable()));
            mappa.put(variante, blocco);
            // rotto non da' niente, ma un item serve per piazzarlo a mano
            ITEMS.register(prefisso + "/" + variante,
                    () -> new BlockItem(blocco.get(), new Item.Properties()));
        }
        return mappa;
    }

    /** Un solo tipo di block entity per tutte le ventotto varianti. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DigSiteBlockEntity>> DIG_SITE =
            BLOCK_ENTITIES.register("dig_site", () -> BlockEntityType.Builder.of(
                    DigSiteBlockEntity::new, siti()).build(null));

    private static Block[] siti() {
        return allSites().map(DeferredBlock::get).toArray(Block[]::new);
    }

    public static final DeferredHolder<MenuType<?>, MenuType<DigMenu>> DIG_MENU =
            MENUS.register("dig_site", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension
                    .create(DigMenu::decode));

    /** Tutti i siti, per elencarli nella scheda creativa. */
    public static java.util.stream.Stream<DeferredBlock<DigSiteBlock>> allSites() {
        return java.util.stream.Stream.of(SUSPICIOUS_SAND, SUSPICIOUS_GRAVEL,
                        SUSPICIOUS_STONE, SUSPICIOUS_CRYSTAL)
                .flatMap(m -> m.values().stream());
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENUS.register(eventBus);
    }
}
