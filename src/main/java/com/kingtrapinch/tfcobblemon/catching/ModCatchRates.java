package com.kingtrapinch.tfcobblemon.catching;

import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier;
import com.cobblemon.mod.common.api.pokeball.catching.modifiers.MultiplierModifier;
import com.cobblemon.mod.common.api.pokeball.catching.modifiers.WorldStateModifier;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * I moltiplicatori delle ball rustic. Le tre linee si comportano diversamente:
 * quelle base e di metallo hanno un valore fisso, la linea heavy premia chi si
 * muove di nascosto e quella sky chi coglie il Pokemon in volo.
 */
public final class ModCatchRates {
    private ModCatchRates() {}

    /** Sotto questa luce il giocatore e' nell'ombra. */
    private static final int DARK = 3;

    private static final List<String> BASE = List.of(
            "ancient_poke_ball", "ancient_azure_ball", "ancient_citrine_ball", "ancient_verdant_ball",
            "ancient_slate_ball", "ancient_ivory_ball", "ancient_roseate_ball");

    private static final Map<ResourceLocation, CatchRateModifier> MODIFIERS = build();

    @Nullable
    public static CatchRateModifier get(ResourceLocation ball) {
        return MODIFIERS.get(ball);
    }

    private static Map<ResourceLocation, CatchRateModifier> build() {
        final Map<ResourceLocation, CatchRateModifier> map = new HashMap<>();
        for (String ball : BASE) {
            map.put(cobblemon(ball), fixed(0.9F));
        }
        map.put(cobblemon("ancient_great_ball"), fixed(1.4F));
        map.put(cobblemon("ancient_ultra_ball"), fixed(1.9F));

        map.put(cobblemon("ancient_heavy_ball"), stealth(1.0F, 1.25F));
        map.put(cobblemon("ancient_leaden_ball"), stealth(1.75F, 2.0F));
        map.put(cobblemon("ancient_gigaton_ball"), stealth(2.5F, 2.75F));

        map.put(cobblemon("ancient_feather_ball"), inFlight(1.0F, 1.25F));
        map.put(cobblemon("ancient_wing_ball"), inFlight(1.75F, 2.0F));
        map.put(cobblemon("ancient_jet_ball"), inFlight(2.5F, 2.75F));
        return map;
    }

    private static CatchRateModifier fixed(float value) {
        return new MultiplierModifier(value, (thrower, pokemon) -> true);
    }

    private static CatchRateModifier stealth(float seen, float hidden) {
        return new WorldStateModifier((thrower, pokemon) -> unnoticed(thrower, pokemon) ? hidden : seen);
    }

    private static CatchRateModifier inFlight(float grounded, float flying) {
        return new WorldStateModifier((thrower, pokemon) -> pokemon.isPokemonFlying() ? flying : grounded);
    }

    /**
     * L'invisibilita' basta da sola; restare fuori dalla vista o nell'ombra vale
     * solo se il giocatore si muove accucciato.
     */
    private static boolean unnoticed(LivingEntity thrower, PokemonEntity pokemon) {
        if (thrower.isInvisible()) {
            return true;
        }
        if (!thrower.isShiftKeyDown()) {
            return false;
        }
        return !pokemon.hasLineOfSight(thrower)
                || thrower.level().getMaxLocalRawBrightness(thrower.blockPosition()) <= DARK;
    }

    private static ResourceLocation cobblemon(String path) {
        return ResourceLocation.fromNamespaceAndPath("cobblemon", path);
    }
}
