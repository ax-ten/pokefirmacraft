package com.kingtrapinch.tfcobblemon.dig;

import net.minecraft.util.RandomSource;

import java.util.List;

/**
 * Che roba e' un sito, cioe' in che ordine sono impilati gli strati.
 *
 * <p>Un sito di sabbia o ghiaia ha il pulviscolo fuori e la pietra dentro; uno
 * di pietra viva e' il contrario, la crosta dura sopra e il molle sotto. Nel
 * cristallo ci sono due strati di cristallo e basta, per cui la spazzola —
 * che porta via solo pulviscolo — non trova mai niente da fare.
 */
public enum SiteKind {
    /** Sciolto: pulviscolo, calce, roccia. */
    SEDIMENT(List.of(Layer.DUST, Layer.LIME, Layer.ROCK), 1, 0.55F, 0.78F),
    /** Pietra viva: crosta dura sopra, e sotto si sfarina. */
    STONE(List.of(Layer.ROCK, Layer.LIME, Layer.DUST), 1, 0.55F, 0.78F),
    /** Cristallo, due strati: quello esterno piu' chiaro. Fragile. */
    CRYSTAL(List.of(Layer.CRYSTAL, Layer.CRYSTAL), 4, 0.5F, 1.01F);

    /** Gli strati dall'esterno verso il fondo. */
    public final List<Layer> stack;
    /** Quanto moltiplica il consumo di sito del martello. */
    public final int hammerPenalty;
    private final float firstBelow;
    private final float secondBelow;

    SiteKind(List<Layer> stack, int hammerPenalty, float firstBelow, float secondBelow) {
        this.stack = stack;
        this.hammerPenalty = hammerPenalty;
        this.firstBelow = firstBelow;
        this.secondBelow = secondBelow;
    }

    public int depth() {
        return stack.size();
    }

    /** Il materiale a una certa profondita'. Oltre il fondo non c'e' piu' niente. */
    public Layer materialAt(int depth) {
        return depth >= 0 && depth < stack.size() ? stack.get(depth) : Layer.EMPTY;
    }

    /**
     * A che profondita' parte una cella. Il rumore fa le chiazze: piu' e' basso
     * piu' la cella e' intatta, cosi' i siti vengono a macchie e non piatti.
     */
    public int startDepth(float noise) {
        if (noise < firstBelow) {
            return 0;
        }
        return noise < secondBelow ? Math.min(1, stack.size() - 1) : Math.min(2, stack.size() - 1);
    }

    public static SiteKind of(String blockPath) {
        if (blockPath.startsWith("suspicious_crystal")) {
            return CRYSTAL;
        }
        return blockPath.startsWith("suspicious_stone") ? STONE : SEDIMENT;
    }

    /** TODO testing: quanto spesso un sito e' di cristallo, se lo piazza la worldgen. */
    public static boolean rollCrystal(RandomSource random) {
        return random.nextFloat() < 0.04F;
    }
}
