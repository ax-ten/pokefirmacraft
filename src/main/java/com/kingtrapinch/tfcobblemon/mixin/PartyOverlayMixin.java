package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.client.gui.PartyOverlay;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingtrapinch.tfcobblemon.belt.Belts;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.List;

/**
 * L'elenco a sinistra mostra solo i posti che la cintura concede, non sei
 * segnaposto da subito.
 *
 * <p>La lista si accorcia solo in base alla cintura, e non a quanti Pokemon ci
 * sono: la squadra e' gia' limitata dallo stesso numero, quindi non c'e' niente
 * di legittimo da nascondere, e un conteggio in piu' sarebbe solo un altro
 * posto dove sbagliare.
 */
@Mixin(PartyOverlay.class)
public abstract class PartyOverlayMixin {

    /**
     * L'overlay chiede la lista degli slot tre volte: per sapere se e' vuota,
     * per l'altezza complessiva e per il conto dei Pokemon veri. A tutte
     * rispondiamo con la lista accorciata, cosi' il riquadro si stringe da se'
     * e resta centrato.
     */
    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lcom/cobblemon/mod/common/client/storage/ClientParty;getSlots()Ljava/util/List;"))
    private List<Pokemon> tfcobblemon$soloIPostiDellaCintura(ClientParty party) {
        return tfcobblemon$taglia(party.getSlots());
    }

    /**
     * I riquadri veri pero' non si disegnano dalla lista: il giro grande scorre
     * la squadra per conto suo — che e' la stessa lista, ma entra da un'altra
     * porta — e percio' finora si vedevano sei caselle anche senza cintura.
     *
     * <p>Chiudiamo anche quella porta. Invece di contare le chiamate, che
     * cambierebbero al primo aggiornamento di Cobblemon, guardiamo chi c'e'
     * dall'altra parte: accorciamo solo se a girare e' la squadra.
     */
    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;"))
    private Iterator<?> tfcobblemon$soloIPostiInGiro(Iterable<?> chi) {
        return chi instanceof ClientParty squadra
                ? tfcobblemon$taglia(squadra.getSlots()).iterator()
                : chi.iterator();
    }

    @Unique
    private static List<Pokemon> tfcobblemon$taglia(List<Pokemon> posti) {
        if (Minecraft.getInstance().player == null) {
            return posti;
        }
        final int quanti = Belts.capacity(Minecraft.getInstance().player);
        return quanti >= posti.size() ? posti : posti.subList(0, Math.max(1, quanti));
    }
}
