package com.kingtrapinch.tfcobblemon.belt;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Le tre cinture.
 *
 * <p>Lo slot {@code belt} non lo dichiariamo: esiste dentro Curios e lo usa
 * anche ToolBelt. Quello che dichiariamo e' quali oggetti ci si possono
 * infilare, e sta nel tag {@code curios:belt}.
 */
public final class ModBelt {
    private ModBelt() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TFCobblemon.MODID);

    /** Quante ball tiene ogni grado di cintura. */
    public static final Map<String, Integer> TIERS = Map.of(
            "leather_trainer_belt", 2,
            "reinforced_trainer_belt", 4,
            "trainer_belt", 6);

    public static final Map<String, DeferredItem<Item>> BELTS = new LinkedHashMap<>();

    static {
        for (String nome : new String[] {"leather_trainer_belt", "reinforced_trainer_belt", "trainer_belt"}) {
            final int slots = TIERS.get(nome);
            BELTS.put(nome, ITEMS.register(nome, () -> new TrainerBeltItem(slots, new Item.Properties())));
        }
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        if (ModList.get().isLoaded("curios")) {
            CuriosBelt.hook(eventBus);
        }
    }
}
