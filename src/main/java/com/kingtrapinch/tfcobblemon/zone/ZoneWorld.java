package com.kingtrapinch.tfcobblemon.zone;

import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Quello che il mondo intorno ha da dire a una zona: che ora e', e quanto
 * rende il posto.
 *
 * <p>E' una cucitura aperta di proposito, come quella delle ball sul pascolo.
 * Qui dentro non entra niente di TFC — questa famiglia di blocchi deve poter
 * vivere in una mod che funziona anche senza — ma **fuori** si puo' installare
 * una versione che sa piu' cose, e nel nostro caso ne sa parecchie:
 *
 * <ul>
 *   <li>il <b>calendario</b>, per far passare il tempo anche a chunk
 *       scaricato, come fa TFC con le colture;</li>
 *   <li>le <b>stagioni</b>, perche' un Pokemon di ghiaccio rende piu'
 *       d'inverno e meno d'estate, uno di fuoco il contrario, uno d'erba in
 *       primavera ed estate;</li>
 *   <li><b>temperatura e umidita'</b> del posto, che TFC sa per ogni
 *       coordinata;</li>
 *   <li>e prima o poi la <b>sete</b>, che va insieme alla fame.</li>
 * </ul>
 *
 * <p>Di base, senza nessuno che risponda, il tempo sono i tick del mondo e
 * l'unica cosa che cambia la resa e' il <b>livello</b>.
 */
public interface ZoneWorld {

    /** Quello attivo. Si sostituisce all'avvio, una volta. */
    ZoneWorld[] ATTIVO = {new ZoneWorld() {}};

    static ZoneWorld attivo() {
        return ATTIVO[0];
    }

    static void installa(ZoneWorld mondo) {
        ATTIVO[0] = mondo;
    }

    /** Che ora e'. */
    default long adesso(ServerLevel level) {
        return level.getGameTime();
    }

    /**
     * Quanto rende questo Pokemon in questo posto, come moltiplicatore.
     *
     * <p>Di base solo il livello: uno di livello 100 rende <b>una volta e
     * mezza</b> uno di livello 1, e in mezzo si sale liscio. Non e' una scala
     * ripida di proposito — un Pokemon appena catturato deve poter lavorare.
     */
    default float resa(ServerLevel level, BlockPos pos, Pokemon mon) {
        return 1.0F + 0.5F * (Math.max(1, mon.getLevel()) - 1) / 99.0F;
    }
}
