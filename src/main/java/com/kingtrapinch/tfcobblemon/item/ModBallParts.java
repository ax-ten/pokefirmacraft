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

    public static final DeferredItem<Item> COPPER_BALL_BASE =
            ITEMS.registerSimpleItem("copper_ball_base");
    public static final DeferredItem<Item> UNFINISHED_COPPER_RUSTIC_MECHANISM =
            ITEMS.registerSimpleItem("unfinished_copper_rustic_mechanism");
    public static final DeferredItem<Item> COPPER_RUSTIC_MECHANISM =
            ITEMS.registerSimpleItem("copper_rustic_mechanism");

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
