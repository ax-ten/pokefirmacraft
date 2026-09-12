package com.kingtrapinch.tfcobblemon.block;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * I sacchi da boxe: sei statistiche per tre stadi di usura.
 *
 * <p>Sei perche' ogni sacco allena la sua statistica, e non c'e' un sacco
 * generico: allenare vuol dire scegliere, e gli EV hanno un tetto che si
 * spende una volta sola.
 *
 * <p>Tre stadi come blocchi distinti e non come proprieta' di usura, per la
 * stessa ragione per cui l'incudine di vanilla ne ha tre: uno stadio che si
 * porta dietro l'item giusto quando lo si stacca dal soffitto, e un sacco
 * mezzo sfondato che resta mezzo sfondato anche in inventario.
 */
public final class ModBags {
    private ModBags() {}

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(TFCobblemon.MODID);
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TFCobblemon.MODID);

    /** Le sei statistiche, coi nomi con cui le chiama Cobblemon. */
    public static final List<String> STATS = List.of(
            "hp", "attack", "defence", "special_attack", "special_defence", "speed");

    /** Sfondato: chi lo usa ancora se lo apre del tutto e non resta niente. */
    public static final Map<String, DeferredBlock<PunchingBagBlock>> TORN = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<PunchingBagBlock>> WORN = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<PunchingBagBlock>> WHOLE = new LinkedHashMap<>();

    static {
        for (String stat : STATS) {
            TORN.put(stat, sacco(stat, "_torn", stat, null));
            WORN.put(stat, sacco(stat, "_worn", stat, TORN.get(stat)::get));
            WHOLE.put(stat, sacco(stat, "", stat, WORN.get(stat)::get));
        }
    }

    private static DeferredBlock<PunchingBagBlock> sacco(
            String stat, String suffisso, String allena,
            java.util.function.Supplier<net.minecraft.world.level.block.Block> dopo) {
        final String nome = "punching_bag/" + stat + suffisso;
        final DeferredBlock<PunchingBagBlock> blocco = BLOCKS.register(nome,
                () -> new PunchingBagBlock(tela(), allena, dopo));
        ITEMS.register(nome, () -> new BlockItem(blocco.get(), new Item.Properties()));
        return blocco;
    }

    private static BlockBehaviour.Properties tela() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_YELLOW)
                .strength(0.8F)
                .sound(SoundType.WOOL)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY);
    }

    /** Tutti e diciotto, per elencarli nella scheda creativa. */
    public static Stream<DeferredBlock<PunchingBagBlock>> tutti() {
        return Stream.of(WHOLE, WORN, TORN).flatMap(m -> m.values().stream());
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
