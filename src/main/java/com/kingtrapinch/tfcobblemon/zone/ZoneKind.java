package com.kingtrapinch.tfcobblemon.zone;

/**
 * Le tre letture di un'area. Lo scheletro e' lo stesso — un controllore accanto
 * a un pascolo, e quello che gli hai costruito intorno decide la resa — e
 * cambia solo cosa si guarda e cosa ne viene.
 *
 * <p>Non c'e' un numero di posti: <b>il limite e' l'attrezzatura</b>. La
 * palestra allena tanti Pokemon quanti sono i sacchi che le hai messo intorno,
 * e se il pascolo ne tiene dieci ma i sacchi sono due, se ne allenano due.
 */
public enum ZoneKind {
    /** Palestra: si contano i sacchi da boxe, e salgono gli EV. */
    GYM("gym"),
    /** Terme: si conta l'arredo, e sale l'amicizia. */
    SPA("spa"),
    /**
     * Recinto: si contano le condizioni di spawn soddisfatte, e rende quello
     * che il Pokemon droppa. Non si chiama "habitat" per non confonderlo con
     * l'Habitat Block dell'era elettrica, che fa un altro mestiere.
     */
    RANCH("ranch");

    private final String nome;

    ZoneKind(String nome) {
        this.nome = nome;
    }

    public String nome() {
        return nome;
    }

    /**
     * Il lavoro di un giro: chi sta al pascolo accanto, e quello che la zona
     * trova intorno a se'.
     *
     * <p>{@code punti} sono i punti maturati dal giro scorso, e il loro valore
     * viene dalla regola unica: una settimana di calendario porta un Pokemon
     * al massimo, cioe' 252. Un punto e' un EV per la palestra e un punto di
     * amicizia per le terme.
     *
     * <p>I tre effetti non sono ancora scritti: manca il come, non il dove.
     */
    public void lavora(net.minecraft.server.level.ServerLevel level, ZoneBlockEntity zona,
                       java.util.List<com.cobblemon.mod.common.pokemon.Pokemon> dentro,
                       int punti) {
        // TODO i tre effetti, con i numeri decisi
    }
}
