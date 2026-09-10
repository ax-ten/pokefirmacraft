package com.kingtrapinch.tfcobblemon.item;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.world.item.Item;
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


    /** Tumblestone intagliata: il cuore di ogni ball, in tutte e tre le linee. */
    public static final DeferredItem<Item> TUMBLESTONE_CORE =
            ITEMS.registerSimpleItem("tumblestone_core");


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

    /** Il metallo della ciotola fa il tier della ball. */
    public static final DeferredItem<Item> COPPER_BALL_BOWL =
            ITEMS.registerSimpleItem("copper_ball_bowl");
    public static final DeferredItem<Item> BLACK_BRONZE_BALL_BOWL =
            ITEMS.registerSimpleItem("black_bronze_ball_bowl");





    /**
     * TFC macina solo minerali, mai leghe, quindi la polvere di black bronze non
     * esiste: serve a colorare il vetro delle ball leggere di tier alto.
     */
    public static final DeferredItem<Item> BLACK_BRONZE_POWDER =
            ITEMS.registerSimpleItem("black_bronze_powder");

    /** La sky tumblestone fusa: un batch di vetro di terzo livello. */
    public static final DeferredItem<Item> SKY_GLASS_BATCH =
            ITEMS.registerSimpleItem("sky_glass_batch");

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
