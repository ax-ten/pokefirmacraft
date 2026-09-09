package com.kingtrapinch.tfcobblemon.event;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.item.custom.LifeOrbItem;
import com.kingtrapinch.tfcobblemon.util.ModSounds;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Set;

@EventBusSubscriber(modid = TFCobblemon.MODID)
public final class PokemonEvents {
    private PokemonEvents() {}

    /** Entro questo raggio la morte di un Pokemon carica l'orb del giocatore. */
    private static final double CHARGE_RANGE = 25.0D;

    /**
     * Ogni Pokemon abbattuto vicino toglie un punto di danno all'orb in carica:
     * a zero e' pronto per diventare la Life Orb.
     */
    @SubscribeEvent
    public static void onPokemonDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof PokemonEntity pokemon) || pokemon.level().isClientSide) {
            return;
        }

        final Player player = pokemon.level().getNearestPlayer(pokemon, CHARGE_RANGE);
        if (player == null) {
            return;
        }

        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(LifeOrbItem.LIFE_ORB_CHARGING.get()) && stack.getDamageValue() >= 1) {
                stack.setDamageValue(stack.getDamageValue() - 1);
                ModSounds.playAt(player.level(), player, "minecraft:block.amethyst_cluster.hit");
                return;
            }
        }
    }

    /**
     * I Pokemon d'acqua salata non annegano: TFC ha oceani veri e senza questo
     * finirebbero per soffocare.
     */
    @SubscribeEvent
    public static void onPokemonSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof PokemonEntity pokemon)) {
            return;
        }

        final String species = pokemon.getPokemon().getSpecies().getResourceIdentifier().getPath();
        if (!SALT_WATER_BREATHERS.contains(species) || pokemon.hasEffect(MobEffects.WATER_BREATHING)) {
            return;
        }

        pokemon.addEffect(new MobEffectInstance(
                MobEffects.WATER_BREATHING, MobEffectInstance.INFINITE_DURATION, 0, true, false));
    }

    private static final Set<String> SALT_WATER_BREATHERS = Set.of(
            "squirtle", "wartortle", "blastoise", "psyduck", "golduck", "tentacool",
            "tentacruel", "slowpoke", "slowbro", "seel", "dewgong", "shellder",
            "cloyster", "krabby", "kingler", "horsea", "seadra", "staryu",
            "starmie", "magikarp", "gyarados", "lapras", "vaporeon", "omanyte",
            "omastar", "kabuto", "kabutops", "dratini", "dragonair", "dragonite",
            "totodile", "croconaw", "feraligatr", "chinchou", "lanturn", "marill",
            "azumarill", "slowking", "qwilfish", "corsola", "remoraid", "octillery",
            "mantine", "kingdra", "suicune", "lugia", "azurill", "carvanha",
            "sharpedo", "wailmer", "wailord", "lileep", "cradily", "anorith",
            "armaldo", "feebas", "milotic", "spheal", "sealeo", "walrein",
            "clamperl", "huntail", "gorebyss", "relicanth", "luvdisc", "kyogre",
            "piplup", "prinplup", "empoleon", "bidoof", "bibarel", "buizel",
            "floatzel", "shellos", "gastrodon", "finneon", "lumineon", "mantyke",
            "dialga", "palkia", "giratina", "phione", "manaphy", "arceus",
            "oshawott", "dewott", "samurott", "basculin", "tirtouga", "carracosta",
            "frillish", "jellicent", "alomomola", "tynamo", "eelektrik", "eelektross",
            "keldeo", "inkay", "malamar", "binacle", "barbaracle", "skrelp",
            "dragalge", "clauncher", "clawitzer", "bergmite", "avalugg", "volcanion",
            "popplio", "brionne", "primarina", "wishiwashi", "mareanie", "toxapex",
            "wimpod", "golisopod", "sandygast", "palossand", "pyukumuku", "bruxish",
            "dhelmise", "tapufini", "chewtle", "drednaw", "arrokuda", "barraskewda",
            "clobbopus", "grapploct", "cursola", "pincurchin", "eiscue", "dracovish",
            "arctovish", "dreepy", "drakloak", "dragapult", "basculegion", "overqwil",
            "wiglett", "wugtrio", "finizen", "palafin", "veluza", "walkingwake"
    );
}
