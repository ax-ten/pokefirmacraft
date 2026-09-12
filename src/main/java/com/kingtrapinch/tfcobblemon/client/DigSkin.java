package com.kingtrapinch.tfcobblemon.client;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.dig.Layer;
import net.minecraft.resources.ResourceLocation;

/**
 * Che texture mostrare per ogni strato di un sito. Non ne disegniamo di nuove
 * per la roccia: si prendono quelle di TFC del materiale su cui stai scavando,
 * cosi' un sito nella ghiaia di calcare ha dentro il calcare e non un granito
 * qualunque. Anche il cristallo fuori e' roccia — la faccia del blocco
 * sospetto stesso, cioe' la quarzite venata del geode — e il cristallo si
 * vede solo sotto, quando la crosta e' venuta via.
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
            // il guscio di un geode di tumblestone e' quarzite, e il fondo e' lo
            // stesso guscio: il cristallo e' solo la vena che ci sta in mezzo
            final String guscio = variant.endsWith("tumblestone") ? "quartzite" : "granite";
            return new DigSkin(tfc("rock/raw/" + guscio), EMPTY, EMPTY,
                    crust(path), ours("crystal/" + variant + "_inner"));
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
     * La crosta di un sito di cristallo e' la faccia del blocco sospetto: la
     * roccia del geode con le sue vene, la stessa che si vedeva da fuori prima
     * di mettersi a scavare.
     */
    private static ResourceLocation crust(String block) {
        return ResourceLocation.fromNamespaceAndPath(TFCobblemon.MODID, "textures/block/" + block + ".png");
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
