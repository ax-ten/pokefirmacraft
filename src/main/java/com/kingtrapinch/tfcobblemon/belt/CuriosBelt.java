package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.item.PokeBallItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;

/**
 * Tutto quello che tocca Curios sta qui dentro, e qui dentro soltanto.
 *
 * <p>Curios e' una dipendenza opzionale: tenendo le sue classi in un posto solo
 * il caricamento non le cerca mai se la mod non c'e'. Per questo la classe non
 * e' annotata con {@code @EventBusSubscriber} — quelle vengono caricate sempre,
 * senza guardare chi c'e' — e si aggancia a mano da {@link ModBelt#register}.
 */
final class CuriosBelt {
    private CuriosBelt() {}

    static void hook(IEventBus eventBus) {
        eventBus.addListener(RegisterCapabilitiesEvent.class, CuriosBelt::zittisciLeBall);
    }

    /**
     * Curios scrive "Slot: belt" sotto ogni oggetto indossabile. Sotto le
     * cinture ci sta — e' la loro unica ragione di esistere — ma sotto una
     * cinquantina di ball e' rumore, quindi alle ball attacchiamo un curio che
     * non dice niente. Gli item di Cobblemon non vanno toccati:
     * {@code CuriosCapability.ITEM} e' una capability di NeoForge e si registra
     * dall'esterno.
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
     * Cosa c'e' nello slot {@code belt}, qualunque cosa sia: una nostra
     * cintura, una ball nuda, o il tool belt di un'altra mod. Lo slot e' di
     * dimensione uno, quindi la domanda ha una risposta sola.
     */
    static ItemStack wornInBelt(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(inventario -> inventario.getStacksHandler("belt"))
                .map(handler -> handler.getStacks().getStackInSlot(0))
                .orElse(ItemStack.EMPTY);
    }

    /** La cintura che il giocatore ha addosso, o vuoto se non ne porta. */
    static ItemStack worn(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(inventario -> inventario.findFirstCurio(
                        stack -> stack.getItem() instanceof TrainerBeltItem))
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }

    /**
     * Infila una ball nella cintura indossata. La cintura va riscritta nel suo
     * slot e non solo modificata: Curios si accorge che un curio e' cambiato
     * confrontando gli stack, e cambiare un componente dello stesso stack non
     * gli basta a mandarlo al client.
     */
    static boolean insert(Player player, ItemStack ball) {
        return CuriosApi.getCuriosInventory(player).map(inventario ->
                inventario.findFirstCurio(stack -> stack.getItem() instanceof TrainerBeltItem)
                        .map(trovata -> {
                            final ItemStack cintura = trovata.stack();
                            if (!(cintura.getItem() instanceof TrainerBeltItem belt)
                                    || !belt.infila(cintura, ball)) {
                                return false;
                            }
                            final SlotContext dove = trovata.slotContext();
                            inventario.getStacksHandler(dove.identifier()).ifPresent(
                                    handler -> handler.getStacks().setStackInSlot(dove.index(), cintura));
                            return true;
                        })
                        .orElse(false))
                .orElse(false);
    }
}
