package com.kingtrapinch.tfcobblemon.zone;

import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;

/**
 * Il tempo di una zona, e da dove vengono i suoi numeri.
 *
 * <p>La regola e' una sola, ed e' quella che detta tutto il resto: <b>in una
 * settimana di gioco un Pokemon arriva al massimo</b> — 252 in una statistica,
 * 255 di amicizia. Da cui il passo: una settimana sono sette giorni di
 * calendario, e in quel tempo devono cadere 252 punti, quindi un punto ogni
 * settimana/252. Non e' un numero scritto a mano: si ricava, cosi' se la
 * lunghezza del giorno cambia la settimana resta una settimana.
 *
 * <p>E il tempo e' quello del <b>calendario di TFC</b>, non i tick del blocco.
 * Lo fanno cosi' le colture e il cibo di TFC, e la ragione e' la stessa: una
 * settimana deve passare anche mentre non sei li' a guardare, altrimenti
 * l'allenamento diventa un premio a chi tiene il chunk caricato.
 */
public final class ZoneClock {
    private ZoneClock() {}

    /** I punti che si prendono in una settimana: il tetto di una statistica. */
    public static final int PUNTI_A_SETTIMANA = 252;

    /** Quanti tick di calendario vale un punto. */
    public static long passo() {
        return Math.max(1L, (long) ICalendar.TICKS_IN_DAY * 7 / PUNTI_A_SETTIMANA);
    }

    /** Che ora e' per il calendario. */
    public static long adesso() {
        return Calendars.SERVER.getTicks();
    }

    /**
     * Quanti punti sono maturati da un certo momento, e da quando ripartire.
     * Il resto non si butta: si tiene indietro l'orologio di quello che non e'
     * ancora maturato, altrimenti passi lunghi perderebbero sempre un pezzo.
     */
    public static int punti(long da) {
        final long passo = passo();
        final long passato = adesso() - da;
        return passato <= 0 ? 0 : (int) Math.min(passato / passo, PUNTI_A_SETTIMANA * 4L);
    }
}
