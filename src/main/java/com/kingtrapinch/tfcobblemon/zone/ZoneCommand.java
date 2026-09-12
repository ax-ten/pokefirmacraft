package com.kingtrapinch.tfcobblemon.zone;

import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Il comando di prova delle zone: manda avanti l'orologio piu' in fretta.
 *
 * <p>Serve perche' i tempi veri sono lunghi di proposito — tre giorni di gioco
 * per portare una statistica al tetto, cioe' un'ora vera — e per sapere se il
 * giro gira non si puo' stare un'ora a guardare un sacco. Con
 * {@code /tfcobblemon zona fretta 100} un'ora diventa mezzo minuto.
 *
 * <p>Non e' una config: si perde al riavvio, perche' non deve finire in una
 * partita per sbaglio.
 */
@EventBusSubscriber(modid = TFCobblemon.MODID)
public final class ZoneCommand {
    private ZoneCommand() {}

    @SubscribeEvent
    public static void registra(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(TFCobblemon.MODID)
                .requires(chi -> chi.hasPermission(2))
                .then(Commands.literal("zona")
                        .then(Commands.literal("fretta")
                                .executes(contesto -> {
                                    contesto.getSource().sendSuccess(() -> Component.literal(
                                            "fretta " + ZoneClock.prova() + "x, un punto ogni "
                                                    + ZoneClock.passo() + " tick"), false);
                                    return 1;
                                })
                                .then(Commands.argument("quante", IntegerArgumentType.integer(1, 10000))
                                        .executes(contesto -> {
                                            final int quante = IntegerArgumentType
                                                    .getInteger(contesto, "quante");
                                            ZoneClock.prova(quante);
                                            contesto.getSource().sendSuccess(() -> Component.literal(
                                                    "fretta " + quante + "x, un punto ogni "
                                                            + ZoneClock.passo() + " tick"), true);
                                            return 1;
                                        })))));
    }
}
