package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.item.PokeBallItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
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

    static void hook(IEventBus eventBus) {
        eventBus.addListener(RegisterCapabilitiesEvent.class, CuriosBelt::zittisciLeBall);
        NeoForge.EVENT_BUS.addListener(CurioChangeEvent.class, CuriosBelt::laCinturaCambiaMano);
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
        if (SLOT.equals(event.getIdentifier())
                && event.getEntity() instanceof ServerPlayer player) {
            BeltParty.align(player);
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
