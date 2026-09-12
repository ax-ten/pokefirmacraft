package com.kingtrapinch.tfcobblemon.zone;

import com.cobblemon.mod.common.CobblemonBlockEntities;
import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.mixins.BlockEntityTypeAccessor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * I tre blocchi delle zone, e l'unico pezzo di colla che serve: dire alla
 * block entity del pascolo di Cobblemon che puo' abitare anche nei nostri.
 */
@EventBusSubscriber(modid = TFCobblemon.MODID, bus = EventBusSubscriber.Bus.MOD)
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

    /**
     * La block entity e' quella del pascolo, e va detto al suo tipo che puo'
     * abitare anche nei nostri blocchi.
     *
     * <p>L'evento di NeoForge fatto per questo — {@code modify} — qui non si
     * puo' usare: <b>rifiuta un blocco che non discenda dal blocco che c'e'
     * gia'</b>, e quello e' {@code PastureBlock}, che e' final. Quel controllo
     * e' una rete generica contro le block entity che castano il proprio
     * blocco, e nel pascolo di cast ce n'e' <em>uno</em>, in
     * {@code togglePastureOn}, chiamato solo dal loro tick — che noi non
     * usiamo (vedi {@link ZoneBlock#getTicker}). Quindi si scrive l'insieme dei
     * blocchi validi a mano, con l'accessorio pubblico di NeoForge che l'evento
     * usa lui stesso, invece di far finta di discendere da una classe chiusa.
     */
    @SubscribeEvent
    public static void ospiti(BlockEntityTypeAddBlocksEvent event) {
        final Set<Block> validi = new HashSet<>(CobblemonBlockEntities.PASTURE.getValidBlocks());
        ZONE.values().forEach(zona -> validi.add(zona.get()));
        ((BlockEntityTypeAccessor) (Object) CobblemonBlockEntities.PASTURE)
                .neoforge$setValidBlocks(validi);
    }

    public static Stream<DeferredBlock<ZoneBlock>> tutte() {
        return ZONE.values().stream();
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
