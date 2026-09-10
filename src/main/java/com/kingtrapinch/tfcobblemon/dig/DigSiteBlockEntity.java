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
    /** TODO testing: una tabella sola per tutti i siti, va scelta dalla struttura. */
    public static final ResourceLocation DEFAULT_LOOT =
            ResourceLocation.fromNamespaceAndPath("cobblemon", "fossils/common/prehistoric_mud_pit");
    /** TODO testing: quanti tesori per sito. */
    public static final int TREASURES = 4;
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
    /** I tesori tirati fuori, in attesa che il giocatore se li trascini via. */
    private final SimpleContainer found = new SimpleContainer(TREASURES);

    public DigSiteBlockEntity(BlockPos pos, BlockState state) {
        super(ModDig.DIG_SITE.get(), pos, state);
    }

    /** Il sito si disegna al primo colpo d'occhio, non quando il mondo lo genera. */
    public DigSite site(ServerLevel level) {
        if (site == null) {
            site = DigSite.generate(worldPosition, level.getSeed());
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
        final LootTable table = level.getServer().reloadableRegistries()
                .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, DEFAULT_LOOT));
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
                    if (site.layer(b.x() + dx, b.y() + dy) != Layer.EMPTY) {
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

    public SimpleContainer found() {
        return found;
    }

    /**
     * Sposta nel cassetto i tesori che sono venuti fuori del tutto. Restano
     * sul block entity, quindi chiudere la finestra non li perde.
     */
    public boolean harvest() {
        boolean any = false;
        for (Buried target : uncovered()) {
            for (int slot = 0; slot < found.getContainerSize(); slot++) {
                if (found.getItem(slot).isEmpty()) {
                    found.setItem(slot, target.stack().copy());
                    buried.replaceAll(b -> b == target
                            ? new Buried(b.x(), b.y(), b.stack(), true) : b);
                    any = true;
                    break;
                }
            }
        }
        if (any) {
            setChanged();
        }
        return any;
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
        tag.put("found", found.createTag(registries));
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
        found.fromTag(tag.getList("found", 10), registries);
    }
}
