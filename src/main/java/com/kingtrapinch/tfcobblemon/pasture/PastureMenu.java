package com.kingtrapinch.tfcobblemon.pasture;

import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * La finestra del pascolo: sedici caselle e niente altro, come una cassa
 * piccola. Ci si mettono le ball dall'inventario e chi ha la sua ball appesa
 * qui sta al pascolo; la si ritira e torna in tasca.
 *
 * <p>Chi pascola e chi no non si decide al momento del clic, si <b>riconcilia
 * dopo</b>: a fine clic si confronta la cesta con i legami del pascolo e si
 * sistema la differenza. E' la stessa scelta fatta per la cintura, e per la
 * stessa ragione — un clic in una finestra e' tre mosse (prendi, sposta,
 * deponi), e provare a reagire a ognuna significa inseguire stati di mezzo.
 */
public class PastureMenu extends AbstractContainerMenu {

    /** Le caselle della cesta: otto per due, come una cassa bassa. */
    private static final int COLONNE = 8;

    private final Container cesta;
    private final PokemonPastureBlockEntity pascolo;

    public static PastureMenu decode(int id, Inventory inventario, RegistryFriendlyByteBuf buf) {
        return new PastureMenu(id, inventario, buf.readBlockPos());
    }

    public PastureMenu(int id, Inventory inventario, BlockPos pos) {
        super(ModPasture.PASTURE_MENU.get(), id);
        this.pascolo = inventario.player.level().getBlockEntity(pos)
                instanceof PokemonPastureBlockEntity p ? p : null;
        this.cesta = pascolo != null ? new PastureBasket(pascolo) : new SimpleContainer(BallBasket.POSTI);

        for (int i = 0; i < BallBasket.POSTI; i++) {
            addSlot(new Slot(cesta, i, 17 + (i % COLONNE) * 18, 18 + (i / COLONNE) * 18) {
                @Override
                public boolean mayPlace(ItemStack cosa) {
                    return cesta.canPlaceItem(getContainerSlot(), cosa);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventario, col + row * 9 + 9, 8 + col * 18, 67 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventario, col, 8 + col * 18, 125));
        }
    }

    @Override
    public void clicked(int slot, int bottone, ClickType tipo, Player player) {
        super.clicked(slot, bottone, tipo, player);
        allinea(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        allinea(player);
    }

    private void allinea(Player player) {
        if (pascolo != null && player instanceof ServerPlayer giocatore) {
            Pastures.allinea(pascolo, giocatore);
        }
    }

    /**
     * Lo shift-clic: dalla cesta all'inventario e viceversa. Una ball che non
     * sta in una casella libera resta dov'e', come in ogni cassa.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int indice) {
        final Slot slot = slots.get(indice);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        final ItemStack cosa = slot.getItem();
        final ItemStack prima = cosa.copy();
        if (indice < BallBasket.POSTI) {
            if (!moveItemStackTo(cosa, BallBasket.POSTI, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(cosa, 0, BallBasket.POSTI, false)) {
            return ItemStack.EMPTY;
        }
        if (cosa.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return prima;
    }

    @Override
    public boolean stillValid(Player player) {
        return cesta.stillValid(player);
    }
}
