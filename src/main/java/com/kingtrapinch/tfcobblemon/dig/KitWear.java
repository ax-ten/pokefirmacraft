package com.kingtrapinch.tfcobblemon.dig;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * L'usura dei tre attrezzi del kit, contata a parte: nel minigioco ognuno ha il
 * suo modo di scavare, quindi ognuno si consuma per conto proprio e la barra che
 * finisce prima decide cosa non puoi piu' fare.
 */
public record KitWear(int hammer, int chisel, int brush) {
    public static final Codec<KitWear> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("hammer").forGetter(KitWear::hammer),
            Codec.INT.fieldOf("chisel").forGetter(KitWear::chisel),
            Codec.INT.fieldOf("brush").forGetter(KitWear::brush)
    ).apply(i, KitWear::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, KitWear> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, KitWear::hammer,
                    ByteBufCodecs.VAR_INT, KitWear::chisel,
                    ByteBufCodecs.VAR_INT, KitWear::brush,
                    KitWear::new);

    public KitWear spend(Tool tool, int amount) {
        return switch (tool) {
            case HAMMER -> new KitWear(Math.max(0, hammer - amount), chisel, brush);
            case CHISEL -> new KitWear(hammer, Math.max(0, chisel - amount), brush);
            case BRUSH -> new KitWear(hammer, chisel, Math.max(0, brush - amount));
        };
    }

    public int left(Tool tool) {
        return switch (tool) {
            case HAMMER -> hammer;
            case CHISEL -> chisel;
            case BRUSH -> brush;
        };
    }

    /** Ogni attrezzo scava a modo suo: area, strato che tocca, e quanto costa. */
    public enum Tool {
        HAMMER(3, Layer.ROCK, 7),
        CHISEL(1, Layer.ROCK, 1),
        BRUSH(2, Layer.DUST, 1);

        public final int size;
        public final Layer reaches;
        public final int siteCost;

        Tool(int size, Layer reaches, int siteCost) {
            this.size = size;
            this.reaches = reaches;
            this.siteCost = siteCost;
        }
    }

    /** I tre strati sovrapposti del sito, dal piu' duro al piu' friabile. */
    public enum Layer {
        ROCK, LIME, DUST, EMPTY
    }
}
