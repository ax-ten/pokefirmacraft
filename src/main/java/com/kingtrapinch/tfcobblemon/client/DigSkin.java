package com.kingtrapinch.tfcobblemon.client;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.dig.Layer;
import net.minecraft.resources.ResourceLocation;

/**
 * Che texture mostrare per ogni strato di un sito. Non ne disegniamo di nuove
 * per la roccia: si prendono quelle di TFC del materiale su cui stai scavando,
 * cosi' un sito nella ghiaia di calcare ha dentro il calcare e non un granito
 * qualunque. Il cristallo invece ha due facce sue, quella esterna piu' chiara.
 */
public record DigSkin(ResourceLocation rock, ResourceLocation lime, ResourceLocation dust,
                      ResourceLocation crystalOuter, ResourceLocation crystalInner) {
    private static final ResourceLocation EMPTY = ours("empty");

    /** Il fondo dello scavo: la roccia del posto, che non si scava piu'. */
    public ResourceLocation floor() {
        return rock;
    }

    private static ResourceLocation tfc(String path) {
        return ResourceLocation.fromNamespaceAndPath("tfc", "textures/block/" + path + ".png");
    }

    private static ResourceLocation ours(String name) {
        return ResourceLocation.fromNamespaceAndPath(TFCobblemon.MODID, "textures/gui/dig/" + name + ".png");
    }

    /** Dal path del blocco sospetto alle facce di quello che ci sta sotto. */
    public static DigSkin of(ResourceLocation block) {
        final String path = block.getPath();
        final int slash = path.indexOf('/');
        final String variant = slash < 0 ? "" : path.substring(slash + 1);
        if (path.startsWith("suspicious_crystal")) {
            // il fondo di un geode e' pietra come tutti gli altri
            return new DigSkin(tfc("rock/raw/granite"), EMPTY, EMPTY,
                    crystalBlock(variant), ours("crystal/" + variant + "_inner"));
        }
        if (path.startsWith("suspicious_sand")) {
            // le facce lisce dell'arenaria, non quella intagliata
            return new DigSkin(tfc("sandstone/bottom/" + variant),
                    tfc("sandstone/side/" + variant),
                    tfc("sand/" + variant), EMPTY, EMPTY);
        }
        // ghiaia e pietra condividono le tre facce della loro roccia
        return new DigSkin(tfc("rock/raw/" + variant),
                tfc("rock/cobble/" + variant),
                tfc("rock/gravel/" + variant), EMPTY, EMPTY);
    }

    /**
     * Il materiale dice quale faccia, ma nel cristallo i due strati sono
     * entrambi cristallo: li distingue la profondita'.
     */
    /**
     * Lo strato esterno del cristallo e' la faccia del blocco vero — ametista,
     * diamante, tumblestone — che esiste gia': non serve ridisegnarla.
     */
    private static ResourceLocation crystalBlock(String variant) {
        final String path = switch (variant) {
            case "amethyst" -> "minecraft:textures/block/amethyst_block.png";
            case "diamond" -> "minecraft:textures/block/diamond_block.png";
            case "emerald" -> "minecraft:textures/block/emerald_block.png";
            case "lapis" -> "minecraft:textures/block/lapis_block.png";
            case "tumblestone" -> "cobblemon:textures/block/tumblestone/tumblestone_block.png";
            case "sky_tumblestone" -> "cobblemon:textures/block/tumblestone/sky_tumblestone_block.png";
            case "black_tumblestone" -> "cobblemon:textures/block/tumblestone/black_tumblestone_block.png";
            default -> "minecraft:textures/block/amethyst_block.png";
        };
        return ResourceLocation.parse(path);
    }

    public ResourceLocation forCell(Layer layer, int depth) {
        return switch (layer) {
            case ROCK -> rock;
            case LIME -> lime;
            case DUST -> dust;
            case CRYSTAL -> depth == 0 ? crystalOuter : crystalInner;
            case EMPTY -> rock;
        };
    }
}
