package com.kingtrapinch.tfcobblemon.dig;

/**
 * Di che materiale e' fatto uno strato. Non e' una scala di durezza: l'ordine
 * in cui gli strati si susseguono lo decide {@link SiteKind}, perche' in un
 * sito di sabbia il pulviscolo sta fuori e la pietra sotto, in uno di pietra e'
 * il contrario.
 */
public enum Layer {
    ROCK,
    LIME,
    DUST,
    CRYSTAL,
    EMPTY
}
