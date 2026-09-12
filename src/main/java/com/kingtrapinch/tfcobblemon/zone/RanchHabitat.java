package com.kingtrapinch.tfcobblemon.zone;

import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Quanto il posto dove sta il ranch somiglia a dove quel Pokemon nascerebbe da
 * solo.
 *
 * <p>E' l'unico numero del disegno delle zone che la regola dei tre giorni non
 * decide: la palestra e l'ozio hanno un tetto noto — 252 e 255 — e quindi un
 * passo che si ricava, mentre il raccolto non ha tetto, e cosa lo faccia
 * rendere di piu' e' una scelta, non un conto. Per quello sta qui da solo, e
 * per quello i numeri sono ancora vuoti.
 *
 * <p><b>La bozza di disegno sta in {@code design_ranch_habitat.md}</b>, alla
 * radice del repository. Finche' non e' decisa, {@link #punteggio} risponde
 * {@link #PIENO} e il ranch si comporta esattamente come prima: il livello e'
 * l'unica cosa che cambia la resa.
 *
 * <p>I nomi qui sotto sono quelli con cui la bozza chiama le stesse cose, cosi'
 * quando i numeri arrivano si riempiono le costanti e basta.
 */
public final class RanchHabitat {
    private RanchHabitat() {}

    /** Il posto giusto: il Pokemon rende per quello che vale. */
    public static final float PIENO = 1.0F;

    /**
     * Il posto che non c'entra niente. Non sara' zero — un ranch che non rende
     * niente e' un ranch rotto, e il giocatore non ha modo di capire perche' —
     * ma quanto valga e' da decidere.
     *
     * <p>Finche' non lo e', vale quanto il posto giusto: la regola non e'
     * ancora accesa.
     */
    public static final float MINIMO = PIENO;

    /** Il valore che hanno i pesi finche' non sono decisi. */
    private static final int DA_DECIDERE = 0;

    // Le clausole che Cobblemon mette in una condizione di spawn, una per una.
    // Sono i nomi veri della sua API in 1.8, non una nostra parafrasi.

    /** {@code biomes} — l'unica clausola che c'e' sempre. */
    public static final int PESO_BIOMA = DA_DECIDERE;
    /** {@code minLight} / {@code maxLight}. */
    public static final int PESO_LUCE = DA_DECIDERE;
    /** {@code minSkyLight} / {@code maxSkyLight} e {@code canSeeSky}. */
    public static final int PESO_CIELO = DA_DECIDERE;
    /** {@code minHeight} / {@code maxHeight}, che nel mondo di TFC contano. */
    public static final int PESO_ALTEZZA = DA_DECIDERE;
    /** {@code neededNearbyBlocks} — il fiore, la sabbia, il fungo. */
    public static final int PESO_BLOCCHI_VICINI = DA_DECIDERE;
    /** {@code neededBaseBlocks} — su cosa poggia. */
    public static final int PESO_BLOCCHI_SOTTO = DA_DECIDERE;
    /** {@code fluid}, {@code minDepth} / {@code maxDepth}. */
    public static final int PESO_FLUIDO = DA_DECIDERE;
    /** {@code timeRange} — giorno, notte, alba. */
    public static final int PESO_ORA = DA_DECIDERE;
    /** {@code isRaining} / {@code isThundering}. */
    public static final int PESO_METEO = DA_DECIDERE;
    /** {@code moonPhase}. */
    public static final int PESO_LUNA = DA_DECIDERE;
    /** {@code structures}. */
    public static final int PESO_STRUTTURA = DA_DECIDERE;
    /** {@code dimensions}. */
    public static final int PESO_DIMENSIONE = DA_DECIDERE;

    /**
     * Quanto rende il posto per questo Pokemon, da {@link #MINIMO} a
     * {@link #PIENO}.
     *
     * <p>Oggi risponde sempre {@link #PIENO}: il conto vero aspetta la bozza.
     */
    public static float punteggio(ServerLevel level, BlockPos dove, Pokemon mon) {
        return PIENO;
    }
}
