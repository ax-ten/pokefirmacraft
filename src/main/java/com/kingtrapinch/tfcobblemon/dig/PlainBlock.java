package com.kingtrapinch.tfcobblemon.dig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Il blocco normale che sta sotto un sito sospetto. Tirato fuori tutto, il sito
 * non si sbriciola: torna a essere la sabbia, la ghiaia, la pietra o il
 * cristallo che era, senza piu' niente dentro.
 */
public final class PlainBlock {
    private PlainBlock() {}

    public static BlockState of(Block suspicious) {
        final String path = BuiltInRegistries.BLOCK.getKey(suspicious).getPath();
        final int slash = path.indexOf('/');
        final String variant = slash < 0 ? "" : path.substring(slash + 1);
        final String plain = switch (slash < 0 ? path : path.substring(0, slash)) {
            case "suspicious_sand" -> "tfc:sand/" + variant;
            case "suspicious_gravel" -> "tfc:rock/gravel/" + variant;
            case "suspicious_stone" -> "tfc:rock/raw/" + variant;
            case "suspicious_crystal" -> crystal(variant);
            default -> "minecraft:air";
        };
        final Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(plain));
        return block == Blocks.AIR ? Blocks.AIR.defaultBlockState() : block.defaultBlockState();
    }

    private static String crystal(String variant) {
        return switch (variant) {
            case "amethyst" -> "minecraft:amethyst_block";
            case "tumblestone" -> "cobblemon:tumblestone_block";
            case "sky_tumblestone" -> "cobblemon:sky_tumblestone_block";
            case "black_tumblestone" -> "cobblemon:black_tumblestone_block";
            // l'opale non ha un blocco suo: e' la sua vena nel quarzite
            case "opal" -> "tfc:ore/opal/quartzite";
            default -> "minecraft:amethyst_block";
        };
    }
}
