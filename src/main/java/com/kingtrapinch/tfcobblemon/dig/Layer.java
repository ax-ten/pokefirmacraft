package com.kingtrapinch.tfcobblemon.dig;

/**
 * I tre strati sovrapposti di un sito, dal piu' duro al piu' friabile. Ogni
 * cella della griglia sta su uno di questi, e si scende togliendo quello sopra.
 */
public enum Layer {
    ROCK,
    LIME,
    DUST,
    EMPTY;

    public Layer below() {
        return this == EMPTY ? EMPTY : values()[ordinal() + 1];
    }

    public boolean harderThan(Layer other) {
        return ordinal() < other.ordinal();
    }
}
