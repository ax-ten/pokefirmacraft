package com.kingtrapinch.tfcobblemon.client;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * I colori veri di una texture, per farci il pulviscolo.
 *
 * <p>Una media non basterebbe: la ghiaia di granito mediata e' un grigio piatto,
 * mentre quello che salta via da una zolla di ghiaia sono granelli chiari e
 * scuri mescolati. Quindi si tengono i pixel come sono e ogni granello ne
 * pesca uno, che e' anche il trucco con cui vanilla fa le particelle di rottura.
 *
 * <p>La lettura passa dal resource manager e non dall'atlante: cosi' funziona
 * per qualunque png, anche quelli di Cobblemon o nostri, senza dover sapere in
 * che atlante siano finiti. Si legge una volta per texture e si tiene.
 */
public final class DigPalette {
    private DigPalette() {}

    /** Se il png non si legge: un grigio sabbia che non stona da nessuna parte. */
    private static final int[] RIPIEGO = {0xC8B090};

    /** Quanti pixel al massimo si tengono per texture. */
    private static final int MAX = 256;

    private static final Map<ResourceLocation, int[]> cache = new HashMap<>();

    public static int[] of(ResourceLocation texture) {
        return cache.computeIfAbsent(texture, DigPalette::leggi);
    }

    /** Dopo un reload le texture possono essere cambiate sotto i piedi. */
    public static void forget() {
        cache.clear();
    }

    private static int[] leggi(ResourceLocation texture) {
        try (InputStream stream = Minecraft.getInstance().getResourceManager()
                .open(texture); NativeImage image = NativeImage.read(stream)) {
            // le texture animate sono strisce verticali di fotogrammi: si
            // guarda solo il primo, che e' alto quanto e' larga la texture
            final int altezza = Math.min(image.getHeight(), image.getWidth());
            final int passo = Math.max(1, (image.getWidth() * altezza) / MAX);
            final java.util.List<Integer> colori = new java.util.ArrayList<>();
            for (int i = 0; i < image.getWidth() * altezza; i += passo) {
                final int argb = image.getPixelRGBA(i % image.getWidth(), i / image.getWidth());
                // NativeImage tiene ABGR: rosso e blu vanno scambiati
                if ((argb >>> 24) < 0x80) {
                    continue;
                }
                colori.add(((argb & 0xFF) << 16) | (argb & 0xFF00) | ((argb >> 16) & 0xFF));
            }
            if (colori.isEmpty()) {
                return RIPIEGO;
            }
            final int[] tavolozza = new int[colori.size()];
            for (int i = 0; i < tavolozza.length; i++) {
                tavolozza[i] = colori.get(i);
            }
            return tavolozza;
        } catch (Exception e) {
            TFCobblemon.LOGGER.debug("Niente tavolozza da {}: {}", texture, e.toString());
            return RIPIEGO;
        }
    }
}
