package com.kingtrapinch.tfcobblemon.pasture;

import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import kotlin.Unit;

import java.util.UUID;

/**
 * Il PC del pascolo: dove vivono i Pokemon che stanno fuori a pascolare.
 *
 * <p>Stessa idea del box invisibile della cintura, e per la stessa ragione: un
 * {@link PCStore} con l'UUID <b>del giocatore</b>, perche' Cobblemon decide di
 * chi e' un Pokemon dal deposito in cui sta — {@code getOwnerUUID()} per un PC
 * restituisce l'UUID del deposito — e un Pokemon che non risulta di nessuno non
 * si puo' nemmeno accarezzare, perche' {@code PokemonEntity.mobInteract}
 * confronta quell'UUID con chi interagisce. Un pascolo con un deposito per
 * blocco sarebbe stato piu' pulito da guardare e avrebbe tolto il padrone a
 * tutto quello che ci sta dentro.
 *
 * <p>Che sia un deposito separato dal PC vero e dal box lo fa la classe: la
 * fabbrica di Cobblemon tiene cache e cartella per <em>classe</em> di deposito,
 * e il nome della classe entra nel percorso del file. Quindi tre classi con lo
 * stesso UUID sono tre depositi che non si vedono a vicenda.
 *
 * <p>Le box vanno create a mano — un PCStore appena costruito ne ha zero e
 * rifiuta tutto, e la fabbrica passa dal costruttore riflessivo che non chiama
 * la funzione che le crea.
 */
public class PastureStore extends PCStore {

    private static final int BOX = 1;

    public PastureStore(UUID uuid) {
        super(uuid);
        cresci();
    }

    @Override
    public PCPosition getFirstAvailablePosition() {
        final PCPosition libero = super.getFirstAvailablePosition();
        if (libero != null) {
            return libero;
        }
        cresci();
        return super.getFirstAvailablePosition();
    }

    private void cresci() {
        resize(getBoxes().size() + BOX, false, mon -> Unit.INSTANCE);
    }
}
