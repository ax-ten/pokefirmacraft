package com.kingtrapinch.tfcobblemon.dig;

import net.minecraft.util.RandomSource;

/**
 * Che roba e' un sito. Cambia gli strati che ci sono dentro, e quindi anche
 * quali attrezzi servono: nel cristallo non c'e' pulviscolo, per cui la
 * spazzola non trova mai niente da spazzolare.
 */
public enum SiteKind {
    /** Sabbia, ghiaia, pietra: roccia, calce e pulviscolo. */
    SEDIMENT(1, 0.55F, 0.78F),
    /**
     * Due soli strati di cristallo, il piu' esterno chiaro. Fragile: il martello
     * ci va giu' come un macigno e finisce il sito in pochi colpi.
     */
    CRYSTAL(4, 0.45F, 1.01F);

    /** Quanto moltiplica il consumo di sito del martello. */
    public final int hammerPenalty;
    private final float rockBelow;
    private final float limeBelow;

    SiteKind(int hammerPenalty, float rockBelow, float limeBelow) {
        this.hammerPenalty = hammerPenalty;
        this.rockBelow = rockBelow;
        this.limeBelow = limeBelow;
    }

    /** Lo strato di una cella, dato il rumore. Sopra limeBelow c'e' il pulviscolo. */
    public Layer layerFor(float noise) {
        if (noise < rockBelow) {
            return Layer.ROCK;
        }
        return noise < limeBelow ? Layer.LIME : Layer.DUST;
    }

    public static SiteKind of(String blockPath) {
        return blockPath.contains("crystal") ? CRYSTAL : SEDIMENT;
    }

    /** TODO testing: quanto spesso un sito e' di cristallo, se lo piazza la worldgen. */
    public static boolean rollCrystal(RandomSource random) {
        return random.nextFloat() < 0.04F;
    }
}
