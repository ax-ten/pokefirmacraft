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

    /**
     * Quante ball tiene ogni grado di cintura. Senza cintura si indossa una
     * ball nuda, quindi la scala e' 1, 3, 6.
     */
    public static final Map<String, Integer> TIERS = Map.of(
            "leather_trainer_belt", 3,
            "trainer_belt", 6);

    public static final Map<String, DeferredItem<Item>> BELTS = new LinkedHashMap<>();

    static {
        for (String nome : new String[] {"leather_trainer_belt", "trainer_belt"}) {
            final int slots = TIERS.get(nome);
            BELTS.put(nome, ITEMS.register(nome, () -> new TrainerBeltItem(slots, new Item.Properties())));
        }
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        // la capability sul bus della mod si registra da qui, perche' l'evento
        // che la vuole arriva presto
        if (curios()) {
            CuriosBelt.hookMod(eventBus);
        }
    }

    /**
     * Gli agganci al bus di gioco, da {@code FMLCommonSetupEvent} e non dal
     * costruttore: {@code ModList.get()} nel costruttore non e' garantito, e se
     * risponde male l'aggancio non avviene e non lo dice nessuno — la cintura
     * continua a funzionare per tutto quello che passa dal nostro codice, e
     * smette di accorgersi dell'unica cosa che non passa da noi, cioe' che il
     * giocatore se l'e' messa o togliata.
     */
    public static void setup() {
        TFCobblemon.LOGGER.debug("cintura: Curios {}", curios() ? "c'e'" : "non c'e'");
        if (curios()) {
            CuriosBelt.hookGame();
        }
    }

    private static boolean curios() {
        return ModList.get() != null && ModList.get().isLoaded("curios");
    }
}
