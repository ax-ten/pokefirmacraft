package com.kingtrapinch.tfcobblemon.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Il pulviscolo che salta via quando una zolla cede. Nella finestra non c'e' un
 * mondo in cui spawnare particelle, quindi sono quadratini disegnati a mano:
 * partono dalla cella, si aprono a raggiera e cadono.
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

    /** Una manciata di schizzi al centro di una cella. */
    public void burst(int x, int y, int colour) {
        for (int i = 0; i < 8; i++) {
            final Speck s = new Speck();
            s.x = x;
            s.y = y;
            s.vx = (RANDOM.nextDouble() - 0.5) * 2.4;
            s.vy = -RANDOM.nextDouble() * 1.6 - 0.2;
            s.colour = colour;
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
