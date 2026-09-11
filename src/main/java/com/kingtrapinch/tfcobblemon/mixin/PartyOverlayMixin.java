package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.client.gui.PartyOverlay;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.pokemon.Pokemon;
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
 * <p>Mai piu' corta di quanti Pokemon ci sono davvero: togliendosi la cintura
 * la squadra resta abitata il tempo di riallinearsi, e in quel momento e'
 * meglio vederli che non vederli.
 */
@Mixin(PartyOverlay.class)
public abstract class PartyOverlayMixin {

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lcom/cobblemon/mod/common/client/storage/ClientParty;getSlots()Ljava/util/List;"))
    private List<Pokemon> tfcobblemon$soloIPostiDellaCintura(ClientParty party) {
        final List<Pokemon> slots = party.getSlots();
        if (Minecraft.getInstance().player == null) {
            return slots;
        }
        int quanti = Belts.capacity(Minecraft.getInstance().player);
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i) != null) {
                quanti = Math.max(quanti, i + 1);
            }
        }
        return quanti >= slots.size() ? slots : slots.subList(0, quanti);
    }
}
