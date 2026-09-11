package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.client.gui.PartyOverlay;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.belt.Belts;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * L'elenco a sinistra mostra solo i posti che la cintura concede, non sei
 * segnaposto da subito.
 *
 * <p>L'overlay chiede la lista degli slot piu' volte — una per l'altezza
 * complessiva, una per disegnarli — e a tutte rispondiamo con la lista
 * accorciata: cosi' il riquadro si stringe da se' e resta centrato.
 *
 * <p>La lista si accorcia solo in base alla cintura, e non a quanti Pokemon ci
 * sono: la squadra e' gia' limitata dallo stesso numero, quindi non c'e' niente
 * di legittimo da nascondere, e un conteggio in piu' sarebbe solo un altro
 * posto dove sbagliare.
 */
@Mixin(PartyOverlay.class)
public abstract class PartyOverlayMixin {

    /** Una riga sola la prima volta, per sapere se il dirottamento arriva qui. */
    private static boolean detto;

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lcom/cobblemon/mod/common/client/storage/ClientParty;getSlots()Ljava/util/List;"))
    private List<Pokemon> tfcobblemon$soloIPostiDellaCintura(ClientParty party) {
        final List<Pokemon> slots = party.getSlots();
        if (Minecraft.getInstance().player == null) {
            return slots;
        }
        final int quanti = Belts.capacity(Minecraft.getInstance().player);
        if (!detto) {
            detto = true;
            TFCobblemon.LOGGER.info("elenco squadra: {} posti su {} slot", quanti, slots.size());
        }
        return quanti >= slots.size() ? slots : slots.subList(0, Math.max(1, quanti));
    }
}
