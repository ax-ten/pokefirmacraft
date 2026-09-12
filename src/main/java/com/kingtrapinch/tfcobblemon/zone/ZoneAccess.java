package com.kingtrapinch.tfcobblemon.zone;

import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import net.minecraft.server.level.ServerPlayer;

/**
 * Come si guarda dentro una zona: la cucitura fra il fondo e quello che questa
 * mod aggiunge.
 *
 * <p>Il fondo e' {@link PcAccess}: la zona si attacca al PC come fa il pascolo
 * di Cobblemon, e in un mondo Cobblemon qualunque funziona cosi'. Sopra, chi ha
 * la cintura installa la versione a ball — le ball fisiche al posto del
 * computer sono quello che aggiunge TFC, dove il PC non e' un elettrodomestico
 * che hai da sempre.
 *
 * <p>E' un'interfaccia e non un innesto di proposito: la zona non sa quale
 * delle due e' installata, e il giorno che questa famiglia di blocchi esce di
 * qui si porta dietro solo il fondo.
 */
public interface ZoneAccess {

    /** Quello attivo. Si sostituisce all'avvio, una volta. */
    ZoneAccess[] ATTIVO = {new PcAccess()};

    static ZoneAccess attivo() {
        return ATTIVO[0];
    }

    static void installa(ZoneAccess strato) {
        ATTIVO[0] = strato;
    }

    /** Apre quello che fa vedere i Pokemon della zona a questo giocatore. */
    void apri(ServerPlayer player, PokemonPastureBlockEntity zona);
}
