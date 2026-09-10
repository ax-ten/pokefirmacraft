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
            return new DigSkin(EMPTY, EMPTY, EMPTY,
                    ours("crystal/" + variant + "_outer"), ours("crystal/" + variant + "_inner"));
        }
        if (path.startsWith("suspicious_sand")) {
            return new DigSkin(tfc("sandstone/cut/" + variant),
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
    public ResourceLocation forCell(Layer layer, int depth) {
        return switch (layer) {
            case ROCK -> rock;
            case LIME -> lime;
            case DUST -> dust;
            case CRYSTAL -> depth == 0 ? crystalOuter : crystalInner;
            case EMPTY -> EMPTY;
        };
    }
}
