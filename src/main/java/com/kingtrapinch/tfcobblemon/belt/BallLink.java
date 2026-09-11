package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Il legame tra una ball e il suo Pokemon.
 *
 * <p>La ball non lo contiene: ne tiene l'UUID. Se il Pokemon vivesse dentro
 * l'item, perdere l'item sarebbe perderlo, ed e' il motivo per cui Cobblemon
 * tiene squadra e PC fuori dal mondo. Quello che la ball si porta oltre
 * all'UUID e' solo il necessario per disegnare il tooltip senza chiedere niente
 * al server: specie, nome, sesso, shiny. Il livello invece cambia mentre il
 * Pokemon e' fuori, quindi quello in cache e' l'ultimo visto e viene riletto
 * dal vivo quando ci passi sopra il cursore.
 *
 * <p>{@code handle} e' l'identita' della ball stessa, e serve contro i doppioni:
 * il Pokemon si ricorda quale ball lo possiede (vedi {@link BallLink#OWNER}), e
 * una copia della ball non risponde perche' il suo handle non combacia piu'.
 */
public record BallLink(UUID pokemon, UUID handle, ResourceLocation species,
                       Optional<String> nickname, String gender, boolean shiny, int level) {

    /** La chiave nei dati persistenti del Pokemon dove scriviamo l'handle padrone. */
    public static final String OWNER = "tfcobblemon:ball";

    public static final Codec<BallLink> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("pokemon").forGetter(BallLink::pokemon),
            UUIDUtil.CODEC.fieldOf("handle").forGetter(BallLink::handle),
            ResourceLocation.CODEC.fieldOf("species").forGetter(BallLink::species),
            Codec.STRING.optionalFieldOf("nickname").forGetter(BallLink::nickname),
            Codec.STRING.fieldOf("gender").forGetter(BallLink::gender),
            Codec.BOOL.optionalFieldOf("shiny", false).forGetter(BallLink::shiny),
            Codec.INT.fieldOf("level").forGetter(BallLink::level)
    ).apply(instance, BallLink::new));

    /** Prende la fotografia di un Pokemon, e gli assegna una ball padrona nuova. */
    public static BallLink of(Pokemon mon, UUID handle) {
        final String nick = mon.getNickname() == null ? null : mon.getNickname().getString();
        return new BallLink(
                mon.getUuid(),
                handle,
                mon.getSpecies().getResourceIdentifier(),
                nick == null || nick.isBlank() ? Optional.empty() : Optional.of(nick),
                mon.getGender().getSerializedName(),
                mon.getShiny(),
                mon.getLevel());
    }

    public static @Nullable BallLink read(ItemStack stack) {
        return stack.get(ModBallData.BALL_LINK.get());
    }

    /** Una ball con un Pokemon dentro: e' quella che non si rompe e non scade. */
    public static boolean filled(ItemStack stack) {
        return read(stack) != null;
    }

    public BallLink withLevel(int nuovo) {
        return new BallLink(pokemon, handle, species, nickname, gender, shiny, nuovo);
    }

    /** Il nome da mostrare: il soprannome se c'e', altrimenti quello della specie. */
    public Component label() {
        return nickname.<Component>map(Component::literal)
                .orElseGet(() -> Component.translatable("cobblemon.species." + species.getPath() + ".name"));
    }

    /** Il simbolo del sesso, col colore che gli spetta. Niente per gli asessuati. */
    public @Nullable Component genderMark() {
        return switch (gender.toLowerCase()) {
            case "male" -> Component.literal("♂").withStyle(ChatFormatting.AQUA);
            case "female" -> Component.literal("♀").withStyle(ChatFormatting.LIGHT_PURPLE);
            default -> null;
        };
    }
}
