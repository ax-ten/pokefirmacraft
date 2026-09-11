package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.battles.BattleRegistry;
import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.cobblemon.mod.common.item.PokeBallItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.event.CurioCanUnequipEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

/**
 * Tutto quello che tocca Curios sta qui dentro, e qui dentro soltanto.
 *
 * <p>Curios e' una dipendenza opzionale: tenendo le sue classi in un posto solo
 * il caricamento non le cerca mai se la mod non c'e'. Per questo la classe non
 * e' annotata con {@code @EventBusSubscriber} — quelle vengono caricate sempre,
 * senza guardare chi c'e' — e si aggancia a mano da {@link ModBelt#register}.
 *
 * <p>Lo slot {@code belt} e' di dimensione uno, quindi qui si parla sempre del
 * posto zero: niente ricerche per tipo di oggetto, che e' anche il modo di
 * trattare allo stesso modo una cintura e una ball indossata nuda.
 */
final class CuriosBelt {
    private CuriosBelt() {}

    private static final String SLOT = "belt";

    static void hookMod(IEventBus eventBus) {
        eventBus.addListener(RegisterCapabilitiesEvent.class, CuriosBelt::zittisciLeBall);
    }

    static void hookGame() {
        NeoForge.EVENT_BUS.addListener(CurioChangeEvent.class, CuriosBelt::laCinturaCambiaMano);
        NeoForge.EVENT_BUS.addListener(CurioCanUnequipEvent.class, CuriosBelt::nonInCombattimento);
    }

    /**
     * Curios scrive "Slot: belt" sotto ogni oggetto indossabile. Alle ball
     * attacchiamo un curio che non dice niente — non basta da solo, perche' le
     * capability si registrano in ordine di caricamento, ma non costa e in
     * qualche caso arriva prima.
     */
    private static void zittisciLeBall(RegisterCapabilitiesEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof PokeBallItem) {
                event.registerItem(CuriosCapability.ITEM,
                        (stack, unused) -> new QuietCurio(stack), item);
            }
        }
    }

    /**
     * Mettersi o togliersi la cintura cambia chi si ha a portata, quindi la
     * squadra si allinea qui. E' il gesto piu' importante, ed e' anche l'unico
     * che il giocatore non fa passando da una nostra riga di codice.
     */
    private static void laCinturaCambiaMano(CurioChangeEvent event) {
        TFCobblemon.LOGGER.info("curio cambiato: slot {}, da {} a {}", event.getIdentifier(),
                event.getFrom().getItem(), event.getTo().getItem());
        if (!SLOT.equals(event.getIdentifier())
                || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        // Attenzione: questo evento NON e' un cambio d'oggetto. Curios confronta
        // lo stack indossato con una sua copia del tick prima usando
        // ItemStack.matches, che in 1.21.1 guarda anche i componenti: quindi
        // riscrivere il contenuto della cintura — cosa che facciamo noi ogni
        // volta che una ball entra o che si marca un Pokemon in campo — arriva
        // qui come "cintura sfilata". Trattarlo come tale richiamava dal mondo
        // il Pokemon che si era appena mandato in campo.
        //
        // Il cambio e' vero solo se l'oggetto e' un altro.
        final boolean sfilata = event.getFrom().getItem() != event.getTo().getItem();
        if (sfilata) {
            // chi stava in quello che si e' sfilato rientra: togliersi la
            // cintura non lascia Pokemon in giro per il mondo senza un posto
            // dove tornare
            BeltParty.recallAll(player, event.getFrom());
        }
        BeltParty.align(player);
    }

    /**
     * In combattimento la cintura non si sfila. Fuori si', e chi era dentro
     * rientra — ma a meta' scontro la squadra e' quella registrata all'inizio,
     * e cambiarla sotto i piedi alla battaglia non ha un esito sensato.
     */
    private static void nonInCombattimento(CurioCanUnequipEvent event) {
        if (SLOT.equals(event.getSlotContext().identifier())
                && event.getEntity() instanceof ServerPlayer player
                && BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
            event.setUnequipResult(TriState.FALSE);
        }
    }

    private static Optional<IDynamicStackHandler> posto(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(inventario -> inventario.getStacksHandler(SLOT))
                .map(handler -> handler.getStacks());
    }

    /** Cosa c'e' nello slot belt, qualunque cosa sia. */
    static ItemStack wornInBelt(Player player) {
        return posto(player).map(stacks -> stacks.getStackInSlot(0)).orElse(ItemStack.EMPTY);
    }

    /**
     * Riscrive quello che sta nello slot belt. Va fatto dopo ogni modifica:
     * Curios si accorge che un curio e' cambiato confrontando gli stack, e
     * cambiare un componente dello stesso stack non gli basta a mandarlo al
     * client.
     */
    static void store(Player player, ItemStack cosa) {
        posto(player).ifPresent(stacks -> stacks.setStackInSlot(0, cosa));
    }

    /** Infila una ball nella cintura indossata, se e' una cintura e ha posto. */
    static boolean insert(Player player, ItemStack ball) {
        final ItemStack cintura = wornInBelt(player);
        if (!(cintura.getItem() instanceof TrainerBeltItem belt) || !belt.infila(cintura, ball)) {
            return false;
        }
        store(player, cintura);
        return true;
    }

    /** Indossa quello che ha in mano, se lo slot e' libero. Uno solo per volta. */
    static boolean equip(Player player, ItemStack cosa) {
        if (!wornInBelt(player).isEmpty()) {
            return false;
        }
        final boolean[] fatto = {false};
        posto(player).ifPresent(stacks -> {
            stacks.setStackInSlot(0, cosa.split(1));
            fatto[0] = true;
        });
        return fatto[0];
    }
}
