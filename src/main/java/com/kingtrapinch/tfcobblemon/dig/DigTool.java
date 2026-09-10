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
     */
    public int siteCost(ItemStack stack) {
        final int sconto = Math.min(baseSiteCost - 1, stack.getMaxDamage() / 250);
        return Math.max(1, baseSiteCost - sconto);
    }
}
