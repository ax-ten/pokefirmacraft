package com.kingtrapinch.tfcobblemon.zone;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.world.item.BlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/** I tre controllori delle zone, e la loro block entity. */
public final class ModZones {
    private ModZones() {}

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(TFCobblemon.MODID);
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TFCobblemon.MODID);

    public static final Map<ZoneKind, DeferredBlock<ZoneBlock>> ZONE = new LinkedHashMap<>();

    static {
        for (ZoneKind genere : ZoneKind.values()) {
            final String nome = "zone/" + genere.nome();
            final DeferredBlock<ZoneBlock> blocco = BLOCKS.register(nome,
                    () -> new ZoneBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion(), genere));
            ZONE.put(genere, blocco);
            ITEMS.register(nome, () -> new BlockItem(blocco.get(), new Item.Properties()));
        }
    }

    /** Una block entity sola per tutti e tre i controllori. */
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TFCobblemon.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ZoneBlockEntity>> ZONA_BE =
            BLOCK_ENTITIES.register("zone", () -> BlockEntityType.Builder.of(
                    ZoneBlockEntity::new, blocchi()).build(null));

    /** La finestrella dei controllori. */
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, TFCobblemon.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ZoneMenu>> ZONA_MENU =
            MENUS.register("zone", () -> IMenuTypeExtension.create(ZoneMenu::decode));

    private static Block[] blocchi() {
        return ZONE.values().stream().map(DeferredBlock::get).toArray(Block[]::new);
    }

    public static Stream<DeferredBlock<ZoneBlock>> tutte() {
        return ZONE.values().stream();
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENUS.register(eventBus);
    }
}
