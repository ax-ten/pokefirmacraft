package com.kingtrapinch.tfcobblemon.dig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumSet;
import java.util.Set;

/**
 * I tre modi di scavare. Gli attrezzi non stanno in un kit: si prendono
 * dall'inventario, quindi il tier del metallo e la durabilita' sono quelli veri
 * dell'attrezzo di TFC e non serve inventare niente.
 */
public enum DigTool {
    /** Sfonda qualunque cosa in area, ma spreca sito. */
    HAMMER(3, EnumSet.of(Layer.ROCK, Layer.LIME, Layer.DUST, Layer.CRYSTAL), 7,
            ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "tools/hammer"))),
    /** Un colpo per volta, il piu' parsimonioso, e l'unico buono nel cristallo. */
    CHISEL(1, EnumSet.of(Layer.ROCK, Layer.LIME, Layer.DUST, Layer.CRYSTAL), 1,
            ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "tools/chisel"))),
    /**
     * Una croce di cinque celle: meno del martello ma piu' dello scalpello, e
     * come il martello sui bracci non sempre sfonda.
     */
    PICKAXE(3, EnumSet.of(Layer.ROCK, Layer.LIME, Layer.DUST, Layer.CRYSTAL), 3,
            ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "tools/pickaxe"))),
    /** Solo pulviscolo: nel cristallo non ce n'e', quindi non serve a niente. */
    BRUSH(2, EnumSet.of(Layer.DUST), 1, null);

    /** Il lato dell'area, in celle. */
    public final int size;
    /** Cosa riesce a portare via. */
    public final Set<Layer> reaches;
    /** Quanto sito consuma un colpo, prima dello sconto del tier. */
    public final int baseSiteCost;

    private final TagKey<Item> tag;

    DigTool(int size, Set<Layer> reaches, int baseSiteCost, TagKey<Item> tag) {
        this.size = size;
        this.reaches = reaches;
        this.baseSiteCost = baseSiteCost;
        this.tag = tag;
    }

    /**
     * Le celle che un colpo tocca da qui: x, y, e se e' un colpo sicuro (1) o
     * a probabilita' (0). Il martello fa un diamante di raggio due, non un
     * quadrato: le quattro punte ortogonali sono in piu' rispetto al 3x3.
     */
    public java.util.List<int[]> area(int cx, int cy) {
        final java.util.List<int[]> cells = new java.util.ArrayList<>();
        switch (this) {
            case HAMMER -> {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        cells.add(new int[] {cx + dx, cy + dy, dx == 0 && dy == 0 ? 1 : 0});
                    }
                }
                for (int[] tip : new int[][] {{2, 0}, {-2, 0}, {0, 2}, {0, -2}}) {
                    cells.add(new int[] {cx + tip[0], cy + tip[1], 0});
                }
            }
            case CHISEL -> cells.add(new int[] {cx, cy, 1});
            case PICKAXE -> {
                cells.add(new int[] {cx, cy, 1});
                for (int[] arm : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                    cells.add(new int[] {cx + arm[0], cy + arm[1], 0});
                }
            }
            case BRUSH -> {
                for (int dy = 0; dy < size; dy++) {
                    for (int dx = 0; dx < size; dx++) {
                        cells.add(new int[] {cx + dx, cy + dy, 1});
                    }
                }
            }
        }
        return cells;
    }

    public boolean bites(Layer layer) {
        return reaches.contains(layer);
    }

    public boolean matches(ItemStack stack) {
        if (this == BRUSH) {
            return stack.is(Items.BRUSH);
        }
        return !stack.isEmpty() && stack.is(tag);
    }

    /**
     * Un attrezzo migliore spreca meno sito, perche' stacca il pezzo invece di
     * sbriciolarlo. Il tier lo leggiamo dalla durabilita' massima, che in TFC
     * sale col metallo, cosi' non serve conoscere la tabella dei metalli.
     *
     * <p>Lo sconto e' fermo a due: prima era proporzionale e un martello
     * d'acciaio scendeva a uno, cioe' apriva un geode in trenta colpi.
     */
    public static final int MAX_TIER_DISCOUNT = 2;

    public int siteCost(ItemStack stack) {
        final int sconto = Math.min(MAX_TIER_DISCOUNT, stack.getMaxDamage() / 700);
        return Math.max(1, baseSiteCost - sconto);
    }
}
