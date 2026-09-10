package com.kingtrapinch.tfcobblemon.client;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.dig.Layer;
import net.minecraft.resources.ResourceLocation;

/**
 * Che texture mostrare per ogni strato di un sito. Non ne disegniamo di nuove:
 * si prendono quelle di TFC del materiale su cui stai scavando, cosi' un sito
 * nella ghiaia di calcare ha dentro il calcare e non un granito qualunque.
 */
public record DigSkin(ResourceLocation rock, ResourceLocation lime, ResourceLocation dust) {
    private static final ResourceLocation EMPTY =
            ResourceLocation.fromNamespaceAndPath(TFCobblemon.MODID, "textures/gui/dig/empty.png");

    private static ResourceLocation tfc(String path) {
        return ResourceLocation.fromNamespaceAndPath("tfc", "textures/block/" + path + ".png");
    }

    private static ResourceLocation ours(String name) {
        return ResourceLocation.fromNamespaceAndPath(TFCobblemon.MODID, "textures/gui/dig/" + name + ".png");
    }

    /** Dal path del blocco sospetto ai tre strati che ci stanno sotto. */
    public static DigSkin of(ResourceLocation block) {
        final String path = block.getPath();
        final int slash = path.indexOf('/');
        final String variant = slash < 0 ? "" : path.substring(slash + 1);
        if (path.startsWith("suspicious_crystal")) {
            return new DigSkin(ours("crystal_inner"), ours("crystal_outer"), ours("crystal_outer"));
        }
        if (path.startsWith("suspicious_sand")) {
            return new DigSkin(tfc("sandstone/cut/" + variant),
                    tfc("sandstone/side/" + variant),
                    tfc("sand/" + variant));
        }
        // ghiaia e pietra condividono le tre facce della loro roccia
        return new DigSkin(tfc("rock/raw/" + variant),
                tfc("rock/cobble/" + variant),
                tfc("rock/gravel/" + variant));
    }

    public ResourceLocation forLayer(Layer layer) {
        return switch (layer) {
            case ROCK -> rock;
            case LIME -> lime;
            case DUST -> dust;
            case EMPTY -> EMPTY;
        };
    }
}
