package com.kingtrapinch.tfcobblemon.client;

import com.kingtrapinch.tfcobblemon.belt.BallLevelPayload;
import com.kingtrapinch.tfcobblemon.belt.BallProbePayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * I livelli letti dal vivo, tenuti da parte il tempo che basta a disegnare un
 * tooltip. La domanda parte quando il cursore si posa su una ball e non si
 * ripete per due secondi: e' un intero per Pokemon, non vale precauzioni
 * maggiori.
 */
public final class ClientBallLevels {
    private ClientBallLevels() {}

    private static final long RESPIRO = 2000L;

    private static final Map<UUID, Integer> livelli = new HashMap<>();
    private static final Map<UUID, Long> chiesti = new HashMap<>();

    /** Il livello vero se e' arrivato, altrimenti quello in cache sulla ball. */
    public static int level(UUID pokemon, int cache) {
        final long ora = System.currentTimeMillis();
        if (ora - chiesti.getOrDefault(pokemon, 0L) > RESPIRO) {
            chiesti.put(pokemon, ora);
            if (Minecraft.getInstance().getConnection() != null) {
                PacketDistributor.sendToServer(new BallProbePayload(pokemon));
            }
        }
        return livelli.getOrDefault(pokemon, cache);
    }

    public static void accept(BallLevelPayload payload) {
        livelli.put(payload.pokemon(), payload.level());
    }

    /** Uscendo dal mondo la cache non serve piu', e gli UUID non valgono altrove. */
    public static void forget() {
        livelli.clear();
        chiesti.clear();
    }
}
