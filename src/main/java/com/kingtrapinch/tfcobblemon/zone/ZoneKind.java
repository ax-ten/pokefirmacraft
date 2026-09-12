package com.kingtrapinch.tfcobblemon.zone;

/**
 * Le tre letture di un'area. Lo scheletro e' lo stesso — un blocco dichiara
 * un'area, quello che c'e' dentro decide la resa — e cambia solo cosa si
 * guarda e cosa ne viene.
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

    /** Quanti Pokemon ci stanno al massimo, attrezzatura permettendo. */
    public static final int POSTI = 4;

    private final String nome;

    ZoneKind(String nome) {
        this.nome = nome;
    }

    public String nome() {
        return nome;
    }
}
