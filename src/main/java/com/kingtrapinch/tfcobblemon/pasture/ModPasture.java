package com.kingtrapinch.tfcobblemon.pasture;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Quello che serve registrare per il pascolo: la sua finestra e il modulo. */
public final class ModPasture {
    private ModPasture() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, TFCobblemon.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<PastureMenu>> PASTURE_MENU =
            MENUS.register("pasture", () -> IMenuTypeExtension.create(PastureMenu::decode));

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TFCobblemon.MODID);

    /**
     * Il modulo che collega un pascolo al PC.
     *
     * <p>Non e' un accessorio: si installa e non si tira via, e nel momento in
     * cui entra i Pokemon appesi alle ball passano al PC vero. Da quel momento
     * il pascolo e' quello di Cobblemon — si sfoglia tutto il PC invece delle
     * sedici ball che ti porti dietro.
     *
     * <p>Ricetta non ancora scritta: il suo posto e' sopra il Pokemon Storage
     * Component (sezione 5.7), che ancora non esiste.
     */
    public static final DeferredItem<Item> PC_LINK =
            ITEMS.registerSimpleItem("pc_link");

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
