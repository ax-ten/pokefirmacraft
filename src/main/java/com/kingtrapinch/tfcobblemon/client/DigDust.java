package com.kingtrapinch.tfcobblemon.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Il pulviscolo che salta via quando una zolla cede. Nella finestra non c'e' un
 * mondo in cui spawnare particelle, quindi sono quadratini disegnati a mano:
 * partono sparsi sulla zolla, si aprono a raggiera e cadono. Il colore lo
 * pescano dai pixel veri della texture che ha ceduto, come fa vanilla.
 */
public final class DigDust {
    private static final RandomSource RANDOM = RandomSource.create();
    private static final int LIFE = 14;

    private static final class Speck {
        double x;
        double y;
        double vx;
        double vy;
        int age;
        int colour;
        int size;
    }

    private final List<Speck> specks = new ArrayList<>();

    /**
     * Una manciata di schizzi sparsi su una zolla. Non partono dal centro: una
     * zolla che cede si sbriciola per intero, e otto granelli dallo stesso punto
     * si leggono come uno scoppio invece che come un crollo. Il margine tiene i
     * granelli dentro la cella, cosi' non sembrano nascere dal vicino.
     */
    public void burst(int x, int y, int span, int[] palette) {
        final int margine = Math.max(1, span / 6);
        for (int i = 0; i < 8; i++) {
            final Speck s = new Speck();
            s.x = x + margine + RANDOM.nextDouble() * (span - 2 * margine);
            s.y = y + margine + RANDOM.nextDouble() * (span - 2 * margine);
            s.vx = (RANDOM.nextDouble() - 0.5) * 2.4;
            s.vy = -RANDOM.nextDouble() * 1.6 - 0.2;
            s.colour = palette[RANDOM.nextInt(palette.length)];
            s.size = 1 + RANDOM.nextInt(2);
            specks.add(s);
        }
    }

    public void render(GuiGraphics graphics) {
        final Iterator<Speck> it = specks.iterator();
        while (it.hasNext()) {
            final Speck s = it.next();
            s.x += s.vx;
            s.y += s.vy;
            s.vy += 0.22;
            if (++s.age > LIFE) {
                it.remove();
                continue;
            }
            final int alpha = 0xFF - (s.age * 0xFF / LIFE);
            graphics.fill((int) s.x, (int) s.y, (int) s.x + s.size, (int) s.y + s.size,
                    (alpha << 24) | (s.colour & 0xFFFFFF));
        }
    }

    public void clear() {
        specks.clear();
    }
}
