package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.api.storage.pc.PCStore;

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
 */
public class BallBoxStore extends PCStore {

    public BallBoxStore(UUID uuid) {
        super(uuid);
    }
}
