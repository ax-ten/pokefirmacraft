package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.item.PokeBallItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;

/**
 * La cintura da allenatore. Lo slot {@code belt} di Curios e' di dimensione uno
 * ed e' condiviso con altre mod, quindi la cintura non lo allarga: la cintura
 * <em>e'</em> l'oggetto indossato, e tiene lei le ball in un contenitore suo.
 *
 * <p>Senza cintura si indossa una ball nuda, e si ha un Pokemon a portata.
 *
 * <p>Nell'inventario si maneggia come un sacco: tasto destro con la cintura sul
 * cursore sopra una ball la infila, sopra uno slot vuoto ne fa uscire l'ultima.
 * Un ball per posto — i posti della cintura sono gli slot della squadra, non un
 * magazzino.
 */
public class TrainerBeltItem extends Item {
    private final int slots;

    public TrainerBeltItem(int slots, Properties properties) {
        super(properties.stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
        this.slots = slots;
    }

    public int slots() {
        return slots;
    }

    public static boolean isBall(ItemStack stack) {
        return stack.getItem() instanceof PokeBallItem;
    }

    /** Le ball che la cintura sta portando, nell'ordine in cui compaiono. */
    public static List<ItemStack> carried(ItemStack belt) {
        final ItemContainerContents contents = belt.get(DataComponents.CONTAINER);
        return contents == null ? List.of() : contents.stream().filter(s -> !s.isEmpty()).toList();
    }

    /** Gli stessi posti, ma lunghi quanto la cintura: i buchi restano buchi. */
    public List<ItemStack> posti(ItemStack belt) {
        final List<ItemStack> posti = new ArrayList<>(slots);
        final ItemContainerContents contents = belt.get(DataComponents.CONTAINER);
        if (contents != null) {
            contents.stream().limit(slots).forEach(posti::add);
        }
        while (posti.size() < slots) {
            posti.add(ItemStack.EMPTY);
        }
        return posti;
    }

    /** Se c'e' ancora un posto libero. */
    public boolean haPosto(ItemStack belt) {
        return carried(belt).size() < slots;
    }

    void salva(ItemStack belt, List<ItemStack> posti) {
        belt.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(posti));
    }

    /**
     * Infila una ball nel primo posto libero, una sola per volta. Restituisce
     * true se e' entrata.
     */
    public boolean infila(ItemStack belt, ItemStack ball) {
        if (ball.isEmpty() || !isBall(ball)) {
            return false;
        }
        final List<ItemStack> posti = posti(belt);
        for (int i = 0; i < posti.size(); i++) {
            if (posti.get(i).isEmpty()) {
                posti.set(i, ball.split(1));
                salva(belt, posti);
                return true;
            }
        }
        return false;
    }

    /**
     * Tira fuori l'ultima ball infilata, saltando quelle il cui Pokemon e' in
     * campo: quelle stanno ferme finche' non rientra, altrimenti spostare la
     * ball mentre lui e' nel mondo lascia due ball o nessuna.
     */
    public ItemStack sfila(ItemStack belt) {
        final List<ItemStack> posti = posti(belt);
        for (int i = posti.size() - 1; i >= 0; i--) {
            if (!posti.get(i).isEmpty() && !BallLink.bloccata(posti.get(i))) {
                final ItemStack uscita = posti.get(i);
                posti.set(i, ItemStack.EMPTY);
                salva(belt, posti);
                return uscita;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack belt, Slot slot, ClickAction action, Player player) {
        if (belt.getCount() != 1 || action != ClickAction.SECONDARY) {
            return false;
        }
        final ItemStack sotto = slot.getItem();
        if (sotto.isEmpty()) {
            final ItemStack uscita = sfila(belt);
            if (uscita.isEmpty()) {
                return false;
            }
            final ItemStack avanzo = slot.safeInsert(uscita);
            if (!avanzo.isEmpty()) {
                infila(belt, avanzo);
            }
            suonoFuori(player);
            allinea(player);
            return true;
        }
        if (!isBall(sotto) || !slot.allowModification(player)) {
            return false;
        }
        if (!infila(belt, sotto)) {
            return false;
        }
        if (sotto.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        suonoDentro(player);
        allinea(player);
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack belt, ItemStack altro, Slot slot,
                                            ClickAction action, Player player,
                                            net.minecraft.world.entity.SlotAccess access) {
        if (belt.getCount() != 1 || action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }
        if (altro.isEmpty()) {
            final ItemStack uscita = sfila(belt);
            if (uscita.isEmpty()) {
                return false;
            }
            access.set(uscita);
            suonoFuori(player);
            allinea(player);
            return true;
        }
        if (!isBall(altro) || !infila(belt, altro)) {
            return false;
        }
        suonoDentro(player);
        allinea(player);
        return true;
    }

    /**
     * Dopo ogni gesto che cambia il contenuto della cintura la squadra va
     * rifatta: questi gesti passano dall'inventario e non da {@code Belts},
     * quindi l'allineamento non lo farebbe nessuno — ed e' il motivo per cui un
     * Pokemon appena messo sulla cintura non entrava in squadra.
     */
    private static void allinea(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer chi) {
            BeltParty.align(chi);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !carried(stack).isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.min(13, 13 * carried(stack).size() / slots);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xCC3B3B;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.tfcobblemon.trainer_belt",
                carried(stack).size(), slots).withStyle(ChatFormatting.GRAY));
        for (ItemStack ball : carried(stack)) {
            final BallLink legame = BallLink.read(ball);
            final Component riga = legame == null
                    ? ball.getHoverName()
                    : Component.empty()
                            .append(Component.literal("Lv. " + legame.level())
                                    .withStyle(ChatFormatting.DARK_GRAY))
                            .append(" ").append(legame.label());
            tooltip.add(Component.literal(" ").append(riga).withStyle(ChatFormatting.GRAY));
        }
    }

    private void suonoDentro(Entity chi) {
        chi.playSound(SoundEvents.BUNDLE_INSERT, 0.8F,
                0.8F + chi.level().getRandom().nextFloat() * 0.4F);
    }

    private void suonoFuori(Entity chi) {
        chi.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F,
                0.8F + chi.level().getRandom().nextFloat() * 0.4F);
    }
}
