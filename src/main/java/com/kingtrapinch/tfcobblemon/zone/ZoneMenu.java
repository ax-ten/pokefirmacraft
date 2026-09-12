package com.kingtrapinch.tfcobblemon.zone;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingtrapinch.tfcobblemon.block.ModBags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * La finestrella di una zona. Non ha slot: e' un quadro comandi.
 *
 * <p>Una riga per Pokemon al pascolo, e su ogni riga si gira fra le
 * statistiche di cui c'e' un sacco nella stanza, piu' "fermo" e "come viene".
 * I nomi e le statistiche disponibili arrivano all'apertura; lo stato di ogni
 * riga sta in un {@link DataSlot}, che il gioco sincronizza da se' senza che
 * serva un pacchetto nostro, e i clic tornano indietro come
 * {@code clickMenuButton}, che e' la strada di vanilla per i bottoni.
 */
public class ZoneMenu extends AbstractContainerMenu {

    /** Quante righe ci stanno nella finestra. */
    public static final int RIGHE = 8;

    /** Lo stato di una riga: fermo, o l'indice della statistica, o "come viene". */
    public static final int FERMO = -1;
    public static final int COMEVIENE = 6;

    /** Un Pokemon in elenco. */
    public record Riga(UUID chi, String nome, int livello) {}

    private final BlockPos pos;
    private final List<Riga> righe;
    /** Le statistiche di cui c'e' un sacco: un bit per statistica. */
    private final int sacchi;
    @Nullable
    private final ZoneBlockEntity zona;
    private final boolean client;
    private final List<DataSlot> stati = new ArrayList<>();
    /** Lo stato come lo vede il client: lo riempie la sincronizzazione. */
    private final int[] cache = new int[RIGHE];

    public static ZoneMenu decode(int id, Inventory inventario, RegistryFriendlyByteBuf buf) {
        final BlockPos pos = buf.readBlockPos();
        final int sacchi = buf.readByte();
        final int quante = buf.readByte();
        final List<Riga> righe = new ArrayList<>(quante);
        for (int i = 0; i < quante; i++) {
            righe.add(new Riga(buf.readUUID(), buf.readUtf(), buf.readByte()));
        }
        return new ZoneMenu(id, inventario, pos, righe, sacchi);
    }

    public ZoneMenu(int id, Inventory inventario, BlockPos pos, List<Riga> righe, int sacchi) {
        super(ModZones.ZONA_MENU.get(), id);
        this.pos = pos;
        this.righe = List.copyOf(righe);
        this.sacchi = sacchi;
        this.client = inventario.player.level().isClientSide();
        this.zona = inventario.player.level().getBlockEntity(pos) instanceof ZoneBlockEntity z
                ? z : null;
        for (int i = 0; i < this.righe.size(); i++) {
            final int riga = i;
            stati.add(addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return leggi(riga);
                }

                @Override
                public void set(int valore) {
                    // lato client il valore arriva da qui e basta tenerlo
                    cache[riga] = valore;
                }
            }));
        }
    }

    /**
     * Lo stato di una riga. Sul client viene dalla sincronizzazione e non dalla
     * block entity: quella c'e' anche di qua, ma le scelte non ci arrivano —
     * viaggiano nei {@link DataSlot}.
     */
    private int leggi(int riga) {
        if (client || zona == null || riga >= righe.size()) {
            return cache[Math.min(riga, cache.length - 1)];
        }
        final UUID chi = righe.get(riga).chi();
        if (zona.fermo(chi)) {
            return FERMO;
        }
        final String scelta = zona.scelta(chi);
        return scelta == null ? COMEVIENE : ModBags.STATS.keySet().stream().toList().indexOf(scelta);
    }

    public int stato(int riga) {
        return riga < stati.size() ? stati.get(riga).get() : COMEVIENE;
    }

    public List<Riga> righe() {
        return righe;
    }

    /** Se di questa statistica c'e' un sacco nella stanza. */
    public boolean cSacco(int statistica) {
        return (sacchi & (1 << statistica)) != 0;
    }

    /**
     * Un clic su una riga la fa girare allo stato dopo: le statistiche di cui
     * c'e' un sacco, poi "come viene", poi "fermo". Girare solo su quelle che
     * esistono evita di far scegliere una cosa che non puo' succedere.
     */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (zona == null || id < 0 || id >= righe.size()) {
            return false;
        }
        final UUID chi = righe.get(id).chi();
        final List<String> nomi = ModBags.STATS.keySet().stream().toList();
        int stato = leggi(id);
        for (int giro = 0; giro < nomi.size() + 2; giro++) {
            stato = avanti(stato, nomi.size());
            if (stato == FERMO || stato == COMEVIENE || cSacco(stato)) {
                break;
            }
        }
        zona.ferma(chi, stato == FERMO);
        zona.scegli(chi, stato >= 0 && stato < nomi.size() ? nomi.get(stato) : null);
        return true;
    }

    private static int avanti(int stato, int quante) {
        if (stato == FERMO) {
            return 0;
        }
        return stato + 1 > quante ? FERMO : stato + 1;
    }

    /** Le righe da mandare al client: chi c'e' al pascolo, in ordine. */
    public static List<Riga> righe(ServerLevel level, ZoneBlockEntity zona) {
        final List<Riga> righe = new ArrayList<>();
        for (Pokemon mon : zona.alPascolo(level)) {
            if (righe.size() >= RIGHE) {
                break;
            }
            righe.add(new Riga(mon.getUuid(), mon.getDisplayName(false).getString(), mon.getLevel()));
        }
        return righe;
    }

    /** Un bit per statistica di cui c'e' un sacco nella stanza. */
    public static int sacchi(ServerLevel level, ZoneBlockEntity zona) {
        final ZoneArea area = ZoneArea.guarda(level, zona.getBlockPos());
        final List<String> nomi = ModBags.STATS.keySet().stream().toList();
        int mappa = 0;
        for (int i = 0; i < nomi.size(); i++) {
            if (!area.sacchi(nomi.get(i)).isEmpty()) {
                mappa |= 1 << i;
            }
        }
        return mappa;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(pos) instanceof ZoneBlockEntity
                && player.distanceToSqr(pos.getCenter()) <= 64.0;
    }
}
