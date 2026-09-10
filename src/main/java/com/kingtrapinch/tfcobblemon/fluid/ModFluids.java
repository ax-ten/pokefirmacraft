package com.kingtrapinch.tfcobblemon.fluid;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * I metalli di TFC sono definiti nel suo codice e non c'e' un registro dati per
 * aggiungerne, quindi la black tumblestone fusa e le due leghe che ne derivano
 * vanno registrate qui. Servono solo a essere colate: niente secchio, niente
 * interazioni particolari.
 */
public final class ModFluids {
    private ModFluids() {}

    public static final DeferredRegister<FluidType> TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, TFCobblemon.MODID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, TFCobblemon.MODID);
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(TFCobblemon.MODID);

    /** Il colore con cui si tinge la texture del fuso, presa in prestito da TFC. */
    public record Molten(
            DeferredHolder<FluidType, FluidType> type,
            DeferredHolder<Fluid, FlowingFluid> source,
            DeferredHolder<Fluid, FlowingFluid> flowing,
            int tint
    ) {}

    public static final Molten BLACK_TUMBLESTONE = molten("molten_black_tumblestone", 0xFF485C53);
    public static final Molten LEADEN_ALLOY = molten("molten_leaden_alloy", 0xFF7E5C42);
    public static final Molten GIGATON_ALLOY = molten("molten_gigaton_alloy", 0xFF5A405D);

    private static Molten molten(String nome, int tint) {
        final DeferredHolder<FluidType, FluidType> type = TYPES.register(nome, () -> new TintedMolten(
                tint,
                FluidType.Properties.create()
                        .density(3000)
                        .viscosity(6000)
                        .temperature(1300)
                        .lightLevel(15)
                        .canDrown(false)
                        .canSwim(false)
                        .canPushEntity(false)
                        .canExtinguish(false)
                        .supportsBoating(false)));

        // il riferimento incrociato fra sorgente e corrente si chiude con dei holder
        final DeferredHolder<Fluid, FlowingFluid>[] box = new DeferredHolder[2];
        final DeferredHolder<Block, LiquidBlock>[] blocco = new DeferredHolder[1];

        box[0] = FLUIDS.register(nome, () -> new BaseFlowingFluid.Source(properties(type, box, blocco)));
        box[1] = FLUIDS.register("flowing_" + nome, () -> new BaseFlowingFluid.Flowing(properties(type, box, blocco)));
        blocco[0] = BLOCKS.register(nome, () -> new LiquidBlock(box[0].get(),
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_GRAY)
                        .replaceable()
                        .noCollission()
                        .strength(100.0F)
                        .pushReaction(PushReaction.DESTROY)
                        .noLootTable()
                        .liquid()));

        return new Molten(type, box[0], box[1], tint);
    }

    private static BaseFlowingFluid.Properties properties(
            DeferredHolder<FluidType, FluidType> type,
            DeferredHolder<Fluid, FlowingFluid>[] box,
            DeferredHolder<Block, LiquidBlock>[] blocco) {
        return new BaseFlowingFluid.Properties(type, box[0], box[1]).block(blocco[0]);
    }

    /**
     * Le texture sono quelle generiche del fuso di TFC, tinte col colore della
     * lega: e' come le disegna TFC stessa e non serve copiare nessun file.
     */
    private static final class TintedMolten extends FluidType {
        private static final ResourceLocation STILL = ResourceLocation.parse("tfc:block/molten_still");
        private static final ResourceLocation FLOWING = ResourceLocation.parse("tfc:block/molten_flow");

        private final int tint;

        private TintedMolten(int tint, Properties properties) {
            super(properties);
            this.tint = tint;
        }

        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return STILL;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return FLOWING;
                }

                @Override
                public int getTintColor() {
                    return tint;
                }
            });
        }
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
        FLUIDS.register(eventBus);
        BLOCKS.register(eventBus);
    }
}
