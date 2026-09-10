package com.kingtrapinch.tfcobblemon.item;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.dries007.tfc.common.items.MoldItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * I pezzi in cui si smonta una Poké Ball: il guscio porta il tier del metallo,
 * il meccanismo si forgia in due passaggi, l'apricorn tagliato a meta' porta il
 * colore e quindi l'identita' della ball.
 */
public final class ModBallParts {
    private ModBallParts() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TFCobblemon.MODID);

    public static final List<String> APRICORN_COLOURS =
            List.of("black", "blue", "green", "pink", "red", "white", "yellow");


    /** Quello che lo stampo dei core accetta: solo il nostro fuso. */
    public static final TagKey<Fluid> CORE_MOLD_FLUIDS = TagKey.create(Registries.FLUID,
            ResourceLocation.fromNamespaceAndPath(TFCobblemon.MODID, "usable_in_core_mold"));

    /**
     * La ender pearl macinata, e l'impasto che ne esce con quattro tumblestone.
     * L'impasto fonde a 1200 gradi: il legno arriva a 757, serve la carbonella.
     */
    public static final DeferredItem<Item> ENDER_POWDER =
            ITEMS.registerSimpleItem("ender_powder");
    public static final DeferredItem<Item> ENDER_TUMBLESTONE_MIX =
            ITEMS.registerSimpleItem("ender_tumblestone_mix");

    /**
     * Lo stampo dei core, una teglia da otto: si versa una volta e si estraggono
     * otto core, invece di colarli uno per uno come le basi rustic.
     */
    public static final DeferredItem<Item> UNFIRED_CORE_MOLD =
            ITEMS.registerSimpleItem("unfired_core_mold");
    public static final DeferredItem<Item> CORE_MOLD =
            ITEMS.register("core_mold", () -> new MoldItem(() -> 200, CORE_MOLD_FLUIDS,
                    new Item.Properties()));

    /** Tumblestone lucidata a carta vetrata: il cuore di ogni ball. */
    public static final DeferredItem<Item> CAPTURE_CORE =
            ITEMS.registerSimpleItem("capture_core");


    /** Le tre semisfere del ramo leggero: stessa bolla, polvere diversa. */
    public static final DeferredItem<Item> FEATHER_BALL_BASE =
            ITEMS.registerSimpleItem("feather_ball_base");
    public static final DeferredItem<Item> WING_BALL_BASE =
            ITEMS.registerSimpleItem("wing_ball_base");
    public static final DeferredItem<Item> JET_BALL_BASE =
            ITEMS.registerSimpleItem("jet_ball_base");

    /** Le tre basi del ramo pesante: stesso stampo, lega diversa. */
    public static final DeferredItem<Item> HEAVY_BALL_BASE =
            ITEMS.registerSimpleItem("heavy_ball_base");
    public static final DeferredItem<Item> LEADEN_BALL_BASE =
            ITEMS.registerSimpleItem("leaden_ball_base");
    public static final DeferredItem<Item> GIGATON_BALL_BASE =
            ITEMS.registerSimpleItem("gigaton_ball_base");

    /** Il metallo della base fa il tier della ball. */
    public static final DeferredItem<Item> COPPER_BALL_BASE =
            ITEMS.registerSimpleItem("copper_ball_base");
    public static final DeferredItem<Item> BLACK_BRONZE_BALL_BASE =
            ITEMS.registerSimpleItem("black_bronze_ball_base");





    /**
     * TFC macina solo minerali, mai leghe, quindi la polvere di black bronze non
     * esiste: serve a colorare il vetro delle ball leggere di tier alto.
     */
    public static final DeferredItem<Item> BLACK_BRONZE_POWDER =
            ITEMS.registerSimpleItem("black_bronze_powder");

    /**
     * Il guscio delle ball moderne. Una lamiera ne rende quattro: e' un guscio
     * stampato sottile, non la semisfera piena delle rustic.
     *
     * <p>Il metallo e' la scala del tier, e basta lui: lo stagno per le ball
     * di base, il ferro battuto per quelle di secondo grado. Non serve mettere
     * il ferro sopra lo stagno — se il guscio e' di ferro, e' di ferro.
     */
    public static final DeferredItem<Item> TIN_BALL_BASE =
            ITEMS.registerSimpleItem("tin_ball_base");
    public static final DeferredItem<Item> WROUGHT_IRON_BALL_BASE =
            ITEMS.registerSimpleItem("wrought_iron_ball_base");

    /** La sky tumblestone macinata alla mola, da mescolare alla sabbia. */
    public static final DeferredItem<Item> SKY_TUMBLESTONE_POWDER =
            ITEMS.registerSimpleItem("sky_tumblestone_powder");

    /**
     * La sky tumblestone macinata e impastata con la sabbia: un batch di vetro
     * di terzo livello. TFC scrive il tipo di vetro come lore fissa sull'item e
     * ne conosce solo quattro, quindi la nostra la mettiamo a mano nello stesso
     * formato, altrimenti la cannuccia non mostra di che vetro e' carica.
     */
    public static final DeferredItem<Item> SKY_GLASS_BATCH =
            ITEMS.register("sky_glass_batch", () -> new Item(new Item.Properties()
                    .component(DataComponents.LORE, new ItemLore(List.of(
                            Component.translatable("tfcobblemon.tooltip.glass.sky")
                                    .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))))));

    /** Una meta' di apricorn per colore, indicizzata per poterle scorrere. */
    public static final Map<String, DeferredItem<Item>> APRICORN_HALVES = new LinkedHashMap<>();

    static {
        for (String colour : APRICORN_COLOURS) {
            APRICORN_HALVES.put(colour, ITEMS.registerSimpleItem("half_" + colour + "_apricorn"));
        }
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
