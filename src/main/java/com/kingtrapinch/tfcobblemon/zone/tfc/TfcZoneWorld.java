package com.kingtrapinch.tfcobblemon.zone.tfc;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingtrapinch.tfcobblemon.zone.ZoneWorld;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.Season;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * La versione di {@link ZoneWorld} che sa di stare dentro TFC.
 *
 * <p>Le zone funzionano anche da sole — e devono, perche' un giorno saranno una
 * mod a se' — ma dove TFC c'e' sarebbe sciocco far finta di niente: il
 * calendario, le stagioni e il clima di quel posto sono gia' li', scritti
 * meglio di come li scriveremmo noi.
 *
 * <p><b>Quello che gia' fa.</b> Il tempo non sono piu' i tick del mondo ma i
 * <b>tick di calendario</b> di TFC, che e' un conto globale e non uno che
 * dipende dal fatto che il chunk sia caricato. Da cui due cose in regalo: una
 * palestra lasciata indietro recupera quando si torna a trovarla, e dormire la
 * notte fa passare la notte anche per lei, esattamente come per le colture.
 *
 * <p><b>Quello che aspetta i numeri.</b> Stagione e clima ci sono come metodi,
 * ma rispondono {@link #NEUTRO} finche' non si decide quanto valgono: un
 * Pokemon di ghiaccio che rende di piu' d'inverno e' una bella idea, ma
 * "quanto" e' una scelta di gioco e non si indovina. I nomi qui sotto sono
 * quelli con cui ne parliamo.
 *
 * <p><b>Quello che manca del tutto.</b> La sete. Prima di poterla appoggiare
 * qui serve una cucitura dall'altra parte — oggi bere e' roba di
 * {@code ZoneArea}, che guarda le bottiglie intorno — e finche' quella non
 * c'e', questo non e' il posto dove metterla.
 */
public final class TfcZoneWorld implements ZoneWorld {

    /** Ne' avanti ne' indietro. */
    public static final float NEUTRO = 1.0F;

    /** La sua stagione. Da decidere. */
    public static final float IN_STAGIONE = NEUTRO;
    /** La stagione opposta alla sua. Da decidere. */
    public static final float FUORI_STAGIONE = NEUTRO;

    /** Il posto e' del clima che gli piace. Da decidere. */
    public static final float CLIMA_GIUSTO = NEUTRO;
    /** Il posto e' del clima sbagliato per lui. Da decidere. */
    public static final float CLIMA_SBAGLIATO = NEUTRO;

    private TfcZoneWorld() {}

    /** Si installa una volta sola, all'avvio, se TFC c'e'. */
    public static void installa() {
        ZoneWorld.installa(new TfcZoneWorld());
    }

    /**
     * I tick di calendario: quelli che corrono anche mentre nessuno guarda, e
     * che saltano avanti quando si dorme.
     */
    @Override
    public long adesso(ServerLevel level) {
        return Calendars.get(level).getCalendarTicks();
    }

    @Override
    public float resa(ServerLevel level, BlockPos pos, Pokemon mon) {
        return ZoneWorld.super.resa(level, pos, mon)
                * stagione(level, mon)
                * clima(level, pos, mon);
    }

    /**
     * Quanto conta il momento dell'anno per questo Pokemon: {@link
     * #stagioneDi} da una parte e {@link Pokemon#getPrimaryType()}
     * dall'altra, e in mezzo la tabella che ancora non c'e'.
     */
    public float stagione(ServerLevel level, Pokemon mon) {
        return NEUTRO;
    }

    /**
     * Quanto conta il clima del posto per questo Pokemon: {@link
     * Climate#getAverageTemperature} e {@link Climate#getAverageRainfall},
     * che TFC sa per ogni coordinata, contro il tipo del Pokemon.
     */
    public float clima(ServerLevel level, BlockPos pos, Pokemon mon) {
        return NEUTRO;
    }

    /**
     * In che stagione siamo.
     *
     * <p>Assoluta, per ora. TFC sa anche fare l'emisfero — al nord e al sud le
     * stagioni sono opposte, e la Z dice da che parte sei — ma se una fattoria
     * debba rendere diversa a seconda di dove sta nel mondo e' una domanda di
     * gioco, non di codice, e sta fra quelle aperte.
     */
    public static Season stagioneDi(ServerLevel level) {
        return Calendars.get(level).getAbsoluteCalendarMonthOfYear().getSeason();
    }
}
