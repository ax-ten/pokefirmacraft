package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import kotlin.Unit;

import java.util.UUID;

/**
 * Il deposito delle ball che non si portano addosso. E' un PC, ma non <em>il</em>
 * PC.
 *
 * <p>Esiste come classe a se' per una ragione precisa, e non per ordine.
 * Cobblemon decide di chi e' un Pokemon dal deposito in cui sta:
 * {@code Pokemon.getOwnerUUID()} per un {@link PCStore} restituisce l'UUID
 * <em>del deposito</em>. Quindi il nostro box deve avere come UUID quello del
 * giocatore, altrimenti i Pokemon che ci vivono non risultano di nessuno — e
 * {@code PokemonEntity.mobInteract} confronta proprio quell'UUID con chi
 * interagisce, per cui non si potrebbero ne' cavalcare ne' toccare.
 *
 * <p>Ma un PCStore con l'UUID del giocatore <b>e' il suo PC</b>: la fabbrica
 * tiene una cache e una cartella per <em>classe</em> di deposito, e chiedendone
 * uno con la stessa classe e lo stesso UUID si ottiene lo stesso oggetto e lo
 * stesso file. Il nome della classe invece entra nel percorso
 * ({@code storeClass.getSimpleName().toLowerCase()}), quindi una sottoclasse con
 * lo stesso UUID e' un deposito separato, con un file suo, che il PC non trova
 * mai perche' lo cerca come {@code PCStore}.
 *
 * <p>Da cui: proprietario giusto e deposito separato, insieme.
 *
 * <p><b>E deve avere delle box, altrimenti non accetta niente.</b> Un
 * {@code PCStore} appena costruito ne ha zero, e
 * {@code getFirstAvailablePosition()} scorre le box per trovare un posto
 * libero: con zero box risponde sempre "nessun posto", quindi {@code add}
 * falliva sempre e ogni Pokemon sfrattato dalla squadra ci rimbalzava dentro.
 * Il PC vero non ha questo problema perche' la fabbrica lo costruisce con una
 * sua funzione che le crea; il nostro passa dal costruttore riflessivo, che
 * quella funzione non la chiama.
 *
 * <p>Le box si creano alla costruzione e crescono quando finiscono, e la
 * crescita a richiesta non e' ridondante: un salvataggio fatto quando il box
 * era rotto contiene zero box, e ricaricandolo tornerebbe a rifiutare tutto.
 */
public class BallBoxStore extends PCStore {

    /** Con quante box nasce, e di quante cresce quando finiscono. */
    private static final int BOX = 30;

    public BallBoxStore(UUID uuid) {
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
