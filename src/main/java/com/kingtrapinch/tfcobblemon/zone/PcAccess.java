package com.kingtrapinch.tfcobblemon.zone;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonNetwork;
import com.cobblemon.mod.common.api.pasture.PastureLink;
import com.cobblemon.mod.common.api.pasture.PastureLinkManager;
import com.cobblemon.mod.common.api.pasture.PasturePermissionControllers;
import com.cobblemon.mod.common.api.pasture.PasturePermissions;
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.net.messages.client.pasture.OpenPasturePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Il fondo: la zona si apre come il pascolo di Cobblemon, cioe' con la
 * finestra del PC in modalita' pascolo.
 *
 * <p>E' tutta roba loro chiamata da fuori, senza innesti: si registra un link
 * fra giocatore e blocco, si manda il pacchetto che apre la schermata, e da
 * quel momento il loro gestore lavora per noi — trascinando un Pokemon dentro,
 * cerca la block entity all'indirizzo del link, la trova (e' la sua) e lo lega.
 */
public class PcAccess implements ZoneAccess {

    @Override
    public void apri(ServerPlayer player, PokemonPastureBlockEntity zona) {
        final UUID pc = Cobblemon.INSTANCE.getStorage().getPC(player).getUuid();
        final UUID link = UUID.randomUUID();
        final PasturePermissions permessi = PasturePermissionControllers.permit(player, zona);
        final List<OpenPasturePacket.PasturePokemonDataDTO> dentro = new ArrayList<>();
        for (PokemonPastureBlockEntity.Tethering legame : zona.getTetheredPokemon()) {
            final OpenPasturePacket.PasturePokemonDataDTO dto = legame.toDTO(player);
            if (dto != null) {
                dentro.add(dto);
            }
        }
        CobblemonNetwork.INSTANCE.sendPacketToPlayer(player,
                new OpenPasturePacket(pc, link, zona.getMaxTethered(), dentro, permessi));
        PastureLinkManager.createLink(player.getUUID(), new PastureLink(link, pc,
                ResourceLocation.tryParse(player.level().dimensionTypeRegistration().getRegisteredName()),
                zona.getBlockPos(), permessi));
    }
}
