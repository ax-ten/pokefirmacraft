package com.kingtrapinch.tfcobblemon.glass;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.item.ModBallParts;
import net.dries007.tfc.common.component.glass.GlassOperation;
import net.dries007.tfc.common.component.heat.Heat;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashSet;
import java.util.Set;

/**
 * Le operazioni di soffiatura di TFC si attivano con le polveri di *minerale*,
 * quindi nessuna lega puo' colorare il vetro. Il black bronze ci serve per il
 * tier alto delle ball leggere, e la polvere di lega la macina GregTech: qui si
 * registra l'operazione mancante.
 *
 * Senza GregTech installato l'operazione resta registrata ma senza polvere che
 * la inneschi, quindi e' semplicemente inerte.
 */
public final class ModGlassOperations {
    private ModGlassOperations() {}

    public static final DeferredRegister<GlassOperation> OPERATIONS =
            DeferredRegister.create(GlassOperation.KEY, TFCobblemon.MODID);

    private static final ResourceLocation GREGTECH_DUST =
            ResourceLocation.parse("gtceu:black_bronze_dust");

    public static final DeferredHolder<GlassOperation, GlassOperation> BLACK_BRONZE =
            OPERATIONS.register("black_bronze", () -> new GlassOperation(
                    blackBronzeDust(),
                    Holder.direct(SoundEvents.ANVIL_USE),
                    Heat.FAINT_RED.getMin(),
                    true));

    /** La nostra polvere, piu' il dust di GregTech se c'e': vanno bene entrambi. */
    private static Set<Holder<Item>> blackBronzeDust() {
        final Set<Holder<Item>> polveri = new HashSet<>();
        polveri.add(ModBallParts.BLACK_BRONZE_POWDER.asItem().builtInRegistryHolder());
        BuiltInRegistries.ITEM
                .getHolder(ResourceKey.create(Registries.ITEM, GREGTECH_DUST))
                .ifPresent(polveri::add);
        return polveri;
    }

    public static void register(IEventBus eventBus) {
        OPERATIONS.register(eventBus);
    }
}
