package com.kingtrapinch.tfcobblemon.dig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Un sito di scavo aperto. Tiene la griglia degli strati, la durabilita' che
 * resta e i tesori sepolti dentro, ognuno su un quadrato 2x2: finche' quelle
 * quattro celle non sono pulite del tutto il tesoro non si prende.
 *
 * <p>I tesori arrivano dalle loot table archeologiche di Cobblemon, quindi la
 * rarita' e' quella che ha scritto lui e non ce ne inventiamo un'altra.
 */
public class DigSiteBlockEntity extends BlockEntity {
    /** TODO testing: una tabella sola per i siti normali, va scelta dalla struttura. */
    public static final ResourceLocation SEDIMENT_LOOT =
            ResourceLocation.fromNamespaceAndPath("cobblemon", "fossils/common/prehistoric_mud_pit");
    /** Il cristallo ha la sua, ed e' quella che vale la pena aprire. */
    public static final ResourceLocation CRYSTAL_LOOT =
            ResourceLocation.fromNamespaceAndPath("tfcobblemon", "dig/crystal");
    /**
     * La tumblestone ne ha una a parte: e' li' che vive la origin ball, una
     * volta su dieci. Nelle altre pietre non si trova affatto.
     */
    public static final ResourceLocation TUMBLESTONE_LOOT =
            ResourceLocation.fromNamespaceAndPath("tfcobblemon", "dig/crystal_tumblestone");
    /** TODO testing: quanti tesori per sito. */
    public static final int TREASURES = 2;
    /** Il lato del quadrato che occupa un tesoro. */
    public static final int TREASURE_SIZE = 2;

    /** Un tesoro sepolto: dove sta e cosa e'. */
    public record Buried(int x, int y, ItemStack stack, boolean taken) {
        public boolean covers(int cx, int cy) {
            return cx >= x && cx < x + TREASURE_SIZE && cy >= y && cy < y + TREASURE_SIZE;
        }
    }

    @Nullable
    private DigSite site;
    private final List<Buried> buried = new ArrayList<>();
    /**
     * I tesori, uno per slot, nell'ordine in cui sono sepolti. Restano qui
     * dentro anche da scoperti: lo slot li mostra dove stanno nella griglia e
     * il giocatore se li trascina via da la'.
     */
    private final SimpleContainer contents = new SimpleContainer(TREASURES);

    public DigSiteBlockEntity(BlockPos pos, BlockState state) {
        super(ModDig.DIG_SITE.get(), pos, state);
    }

    /** Il sito si disegna al primo colpo d'occhio, non quando il mondo lo genera. */
    public SiteKind kind() {
        return SiteKind.of(net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getKey(getBlockState().getBlock()).getPath());
    }

    public DigSite site(ServerLevel level) {
        if (site == null) {
            site = DigSite.generate(worldPosition, level.getSeed(), kind());
            scatter(level);
            setChanged();
        }
        return site;
    }

    @Nullable
    public DigSite siteOrNull() {
        return site;
    }

    public List<Buried> buried() {
        return buried;
    }

    private void scatter(ServerLevel level) {
        final ResourceLocation which;
        if (kind() != SiteKind.CRYSTAL) {
            which = SEDIMENT_LOOT;
        } else {
            final String variante = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                    .getKey(getBlockState().getBlock()).getPath();
            which = variante.contains("tumblestone") ? TUMBLESTONE_LOOT : CRYSTAL_LOOT;
        }
        final LootTable table = level.getServer().reloadableRegistries()
                .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, which));
        final LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, worldPosition.getCenter())
                .create(LootContextParamSets.ARCHAEOLOGY);
        final int slots = DigSite.SIZE - TREASURE_SIZE + 1;
        for (int i = 0; i < TREASURES; i++) {
            final List<ItemStack> rolled = table.getRandomItems(params);
            if (rolled.isEmpty()) {
                continue;
            }
            for (int attempt = 0; attempt < 24; attempt++) {
                final int x = level.random.nextInt(slots);
                final int y = level.random.nextInt(slots);
                if (buried.stream().noneMatch(b -> overlaps(b, x, y))) {
                    contents.setItem(buried.size(), rolled.getFirst().copy());
                    buried.add(new Buried(x, y, rolled.getFirst(), false));
                    break;
                }
            }
        }
    }

    private static boolean overlaps(Buried other, int x, int y) {
        return Math.abs(other.x() - x) < TREASURE_SIZE && Math.abs(other.y() - y) < TREASURE_SIZE;
    }

    /** I tesori le cui quattro celle sono ormale vuote, e che nessuno ha ancora preso. */
    public List<Buried> uncovered() {
        if (site == null) {
            return List.of();
        }
        final List<Buried> ready = new ArrayList<>();
        for (Buried b : buried) {
            if (b.taken()) {
                continue;
            }
            boolean clean = true;
            for (int dy = 0; dy < TREASURE_SIZE && clean; dy++) {
                for (int dx = 0; dx < TREASURE_SIZE; dx++) {
                    if (!site.cleared(b.x() + dx, b.y() + dy)) {
                        clean = false;
                        break;
                    }
                }
            }
            if (clean) {
                ready.add(b);
            }
        }
        return ready;
    }

    /**
     * La durabilita' e' finita: il blocco si sbriciola e quello che non e' stato
     * tirato fuori resta sotto. Non cade niente, nemmeno il blocco.
     */
    public void collapse(ServerLevel level) {
        contents.clearContent();
        // come i blocchi sospetti di vanilla: si sfalda con un tintinnio di vetro
        level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.GLASS_BREAK,
                net.minecraft.sounds.SoundSource.BLOCKS, 0.9F, 0.9F);
        level.destroyBlock(worldPosition, false);
    }

    /**
     * Mezzo secondo fra l'ultimo tesoro e la chiusura, per far leggere il
     * "Completed!". In quel mezzo secondo il sito e' fermo: non si scava piu',
     * e nessun altro puo' aprirlo.
     *
     * <p>Il blocco si posa solo alla fine, e non subito, per una ragione
     * pratica: posarlo fa sparire questa block entity, e la finestra — che
     * controlla di averla ancora davanti — si chiuderebbe da sola.
     */
    private static final int ATTESA = 10;

    private int chiusura = -1;

    public boolean finito() {
        return chiusura >= 0;
    }

    public void finisci() {
        chiusura = ATTESA;
    }

    /** Un tick di attesa. Vero quando e' ora di posare il blocco. */
    public boolean scala() {
        return chiusura >= 0 && --chiusura < 0;
    }

    /**
     * Tirato fuori tutto: il sito non ha piu' niente da nascondere, quindi
     * torna il blocco normale che era. Niente si rompe e niente si perde.
     */
    public void settle(ServerLevel level) {
        contents.clearContent();
        level.setBlockAndUpdate(worldPosition, PlainBlock.of(getBlockState().getBlock()));
    }

    public SimpleContainer contents() {
        return contents;
    }

    /** Se non e' rimasto niente da tirare fuori, il sito non ha piu' motivo di stare in piedi. */
    public boolean emptied() {
        for (int i = 0; i < contents.getContainerSize(); i++) {
            if (!contents.getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** Se almeno una delle quattro celle sopra il tesoro e' pulita. */
    public boolean glimpsed(int slot) {
        if (site == null || slot < 0 || slot >= buried.size()) {
            return false;
        }
        final Buried b = buried.get(slot);
        for (int dy = 0; dy < TREASURE_SIZE; dy++) {
            for (int dx = 0; dx < TREASURE_SIZE; dx++) {
                if (site.cleared(b.x() + dx, b.y() + dy)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Se le quattro celle sopra il tesoro numero {@code slot} sono pulite. */
    public boolean exposed(int slot) {
        if (site == null || slot < 0 || slot >= buried.size()) {
            return false;
        }
        final Buried b = buried.get(slot);
        for (int dy = 0; dy < TREASURE_SIZE; dy++) {
            for (int dx = 0; dx < TREASURE_SIZE; dx++) {
                if (!site.cleared(b.x() + dx, b.y() + dy)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (site != null) {
            tag.put("site", site.save());
        }
        final net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (Buried b : buried) {
            final CompoundTag entry = new CompoundTag();
            entry.putByte("x", (byte) b.x());
            entry.putByte("y", (byte) b.y());
            entry.putBoolean("taken", b.taken());
            entry.put("stack", b.stack().save(registries));
            list.add(entry);
        }
        tag.put("buried", list);
        tag.put("contents", contents.createTag(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        site = tag.contains("site") ? DigSite.load(tag.getCompound("site")) : null;
        buried.clear();
        final net.minecraft.nbt.ListTag list = tag.getList("buried", 10);
        for (int i = 0; i < list.size(); i++) {
            final CompoundTag entry = list.getCompound(i);
            buried.add(new Buried(entry.getByte("x"), entry.getByte("y"),
                    ItemStack.parse(registries, entry.get("stack")).orElse(ItemStack.EMPTY),
                    entry.getBoolean("taken")));
        }
        contents.fromTag(tag.getList("contents", 10), registries);
    }
}
