package com.kingtrapinch.tfcobblemon.dig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * I tre modi di scavare. Gli attrezzi non stanno in un kit: si prendono
 * dall'inventario, quindi il tier del metallo e la durabilita' sono quelli veri
 * dell'attrezzo di TFC e non serve inventare niente.
 */
public enum DigTool {
    /** Sfonda la roccia in area, ma spreca sito. */
    HAMMER(3, Layer.ROCK, 7, ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "tools/hammer"))),
    /** Un colpo per volta, il piu' parsimonioso. */
    CHISEL(1, Layer.ROCK, 1, ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "tools/chisel"))),
    /** Tocca solo il pulviscolo: sulla roccia si consuma e non combina niente. */
    BRUSH(2, Layer.DUST, 1, null);

    /** Il lato dell'area, in celle. */
    public final int size;
    /** Lo strato piu' duro che riesce a togliere. */
    public final Layer reaches;
    /** Quanto sito consuma un colpo, prima dello sconto del tier. */
    public final int baseSiteCost;

    private final TagKey<Item> tag;

    DigTool(int size, Layer reaches, int baseSiteCost, TagKey<Item> tag) {
        this.size = size;
        this.reaches = reaches;
        this.baseSiteCost = baseSiteCost;
        this.tag = tag;
    }

    public boolean matches(ItemStack stack) {
        if (this == BRUSH) {
            return stack.is(net.minecraft.world.item.Items.BRUSH);
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
