package com.kingtrapinch.tfcobblemon.zone;

/**
 * Il tempo di una zona, e da dove vengono i suoi numeri.
 *
 * <p>La regola e' una: <b>in tre giorni di gioco un Pokemon arriva al
 * massimo</b> — 252 in una statistica, 255 di amicizia. Da qui si ricava il
 * passo, un punto ogni {@code tempo / 252}, invece di scegliere un numero a
 * mano: se si cambia il traguardo cambia tutto il resto da se'.
 *
 * <p>Tre giorni sono un'ora di gioco vero per una statistica, due ore per uno
 * spread completo (il tetto totale e' 510). La via industriale non alza il
 * tetto — un tetto piu' alto non esiste, 252 e' 252 — <b>accorcia il
 * tempo</b>: e' quello che fanno le vitamine, ed e' anche il motivo per cui
 * hanno senso solo quando si possono produrre.
 *
 * <p>Il tempo sono i <b>tick del mondo</b> e non il calendario di TFC, per una
 * ragione che non e' tecnica: questa famiglia di blocchi deve poter vivere in
 * una mod a se' che funziona <b>anche senza TFC</b>. Niente di TFC entra in
 * questo pacchetto.
 */
public final class ZoneClock {
    private ZoneClock() {}

    /** I punti che servono per arrivare al tetto di una statistica. */
    public static final int PUNTI_AL_TETTO = 252;

    /** In quanti giorni di gioco si arriva al tetto. */
    public static final int GIORNI_AL_TETTO = 3;

    /** Quanto dura un giorno di Minecraft. */
    private static final int TICK_AL_GIORNO = 24000;

    /**
     * Quante volte piu' veloce va l'orologio. Vale <b>uno</b> e si tocca solo
     * col comando di prova: tre giorni di gioco sono un'ora vera, e guardare
     * un'ora un sacco per sapere se si logora non e' un modo di lavorare.
     */
    private static int prova = 1;

    public static int prova() {
        return prova;
    }

    public static void prova(int quante) {
        prova = Math.max(1, quante);
    }

    /** Quanti tick vale un punto, senza acceleratori. */
    public static long passo() {
        return Math.max(1L, (long) TICK_AL_GIORNO * GIORNI_AL_TETTO / PUNTI_AL_TETTO / prova);
    }

    /**
     * Quanti punti sono maturati da un certo momento. Il resto non si butta:
     * chi chiama riporta indietro l'orologio di quello che non e' ancora
     * maturato, altrimenti ogni giro perderebbe un pezzo di avanzamento.
     */
    public static int punti(long adesso, long da, int quanteVolte) {
        final long passo = Math.max(1L, passo() / Math.max(1, quanteVolte));
        final long passato = adesso - da;
        return passato <= 0 ? 0 : (int) Math.min(passato / passo, PUNTI_AL_TETTO);
    }
}
