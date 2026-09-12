package com.kingtrapinch.tfcobblemon.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.Optional;

/**
 * Un geode che lascia il segno: sotto la roccia il geode vero, in superficie
 * una manciata di sassi dello stesso cristallo, sulla sua colonna.
 *
 * <p>E' il modo di TFC di dirti cosa hai sotto i piedi: i sassi sciolti
 * raccontano la roccia del posto, e qui raccontano il giacimento. Non e' una
 * coincidenza che si possa ricreare coi dati — i due pezzi devono uscire dallo
 * stesso tiro di dado, altrimenti un sasso di tumblestone non significa
 * niente e non vale la pena di scavare sotto.
 *
 * <p>Il geode lo piazza quello di vanilla, che va benissimo: qui si aggiunge
 * solo quello che si vede da sopra.
 */
public class GeodeTrailFeature extends Feature<GeodeTrailFeature.Config> {
    /**
     * @param geode  il geode da piazzare, la configurazione di vanilla intatta
     * @param rock   il sasso da spargere in superficie
     * @param tries  quanti sassi provare a mettere
     * @param spread di quanti blocchi si possono allontanare dalla colonna
     */
    public record Config(GeodeConfiguration geode, BlockStateProvider rock,
                         IntProvider tries, int spread) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(i -> i.group(
                GeodeConfiguration.CODEC.fieldOf("geode").forGetter(Config::geode),
                BlockStateProvider.CODEC.fieldOf("rock").forGetter(Config::rock),
                IntProvider.codec(0, 16).fieldOf("tries").forGetter(Config::tries),
                Codec.intRange(0, 15).fieldOf("spread").forGetter(Config::spread)
        ).apply(i, Config::new));
    }

    public GeodeTrailFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        final Config config = context.config();
        final boolean fatto = Feature.GEODE.place(new FeaturePlaceContext<>(
                Optional.empty(), context.level(), context.chunkGenerator(),
                context.random(), context.origin(), config.geode()));
        if (!fatto) {
            // il geode ha trovato troppa aria o troppa acqua e ha rinunciato:
            // niente sassi, o indicherebbero un giacimento che non c'e'
            return false;
        }
        sassi(context, config);
        return true;
    }

    /**
     * I sassi, uno per uno, con le regole degli indicatori di TFC.
     *
     * <p>TFC ha di suo il meccanismo del sasso in superficie sopra un
     * giacimento — e' il campo {@code indicator} delle sue vene — ma vive
     * dentro {@code VeinFeature.place}, impastato col ciclo che riempie la
     * vena blocco per blocco: gli serve sapere fin dove e' arrivato il
     * minerale in quella colonna, e un geode di vanilla quel numero non ce
     * l'ha. Quello che si puo' riusare sono le sue regole, e sono queste:
     * l'altezza si prende dal fondo dell'oceano e non dalla superficie, e si
     * scrive solo dove la worldgen puo' sovrascrivere. Sott'acqua invece non
     * si mette niente: TFC allagherebbe il sasso, ma un sasso in fondo a un
     * lago non lo vede nessuno, e un indizio che non si vede non e' un
     * indizio. Su un mondo di prova non ne era finito sott'acqua nemmeno uno
     * su cinquantuno, quindi non si perde niente.
     *
     * <p>L'altezza si chiede per ogni sasso e non una volta per tutti: su un
     * pendio un'altezza sola li lascerebbe meta' a mezz'aria e meta' sepolti.
     * Lo scarto dalla colonna e' la differenza di due tiri come fa TFC, che
     * li tiene raccolti intorno al centro invece di spargerli uniformi.
     */
    private void sassi(FeaturePlaceContext<Config> context, Config config) {
        final WorldGenLevel level = context.level();
        final RandomSource random = context.random();
        final BlockPos origin = context.origin();
        final int quanti = config.tries().sample(random);
        final int raggio = config.spread();
        for (int i = 0; i < quanti; i++) {
            final int x = origin.getX() + random.nextInt(raggio + 1) - random.nextInt(raggio + 1);
            final int z = origin.getZ() + random.nextInt(raggio + 1) - random.nextInt(raggio + 1);
            if (!level.hasChunk(x >> 4, z >> 4)) {
                // fuori dalla regione in generazione: non si sa che altezza
                // abbia il terreno e non ci si puo' scrivere
                continue;
            }
            final BlockPos pos = new BlockPos(x,
                    level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z), z);
            final BlockState posto = level.getBlockState(pos);
            if (!posto.getFluidState().isEmpty() || !EnvironmentHelpers.isWorldgenReplaceable(posto)) {
                continue;
            }
            final BlockState sasso = config.rock().getState(random, pos);
            if (sasso.canSurvive(level, pos)) {
                setBlock(level, pos, sasso);
            }
        }
    }
}
