package com.kingtrapinch.tfcobblemon.block;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.dries007.tfc.common.blocks.rock.LooseRockBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * I sassi di tumblestone che si trovano per terra.
 *
 * <p>TFC sparge i suoi sassi in superficie per dirti che roccia hai sotto i
 * piedi, e la tumblestone segue la stessa regola. Non c'e' niente da inventare:
 * la classe e' la {@link LooseRockBlock} di TFC, che porta gia' la quantita' da
 * uno a tre, il raccoglierli col tasto destro e la forma bassa che non
 * intralcia. Anche i modelli sono i suoi — un sasso e' un sasso — e l'unica
 * cosa che cambia e' la texture.
 */
public final class ModGroundcover {
    private ModGroundcover() {}

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(TFCobblemon.MODID);

    public static final Map<String, DeferredBlock<Block>> SASSI = new LinkedHashMap<>();

    static {
        for (String nome : new String[] {
                "loose_tumblestone", "loose_sky_tumblestone", "loose_black_tumblestone"}) {
            SASSI.put(nome, BLOCKS.register(nome, () -> new LooseRockBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(0.05F, 0.0F)
                            .sound(SoundType.STONE)
                            .noCollission()
                            .instabreak()
                            .pushReaction(PushReaction.DESTROY))));
        }
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
