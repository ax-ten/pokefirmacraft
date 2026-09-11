package com.kingtrapinch.tfcobblemon.client;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.interaction.PokemonInteractionGUICreationEvent;
import com.cobblemon.mod.common.client.gui.interact.wheel.InteractWheelOption;
import com.kingtrapinch.tfcobblemon.TFCobblemon;
import com.kingtrapinch.tfcobblemon.belt.RecallPayload;
import kotlin.Unit;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

/**
 * "Richiama" nella ruota delle interazioni, cioe' shift + tasto destro su un
 * Pokemon in campo.
 *
 * <p>Serve perche' la ball non e' sempre la strada: un compagno di viaggio lo si
 * fa rientrare dalla sua ball, ma se quella e' in fondo allo zaino il gesto
 * naturale e' chiederlo a lui. E per i Pokemon di squadra la ruota e' comunque
 * il posto dove uno va a cercare le azioni.
 *
 * <p>La ruota e' di Cobblemon e vive sul client, quindi l'opzione si aggiunge
 * qui e la pressione manda un pacchetto: il rientro lo fa il server.
 */
@EventBusSubscriber(modid = TFCobblemon.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class WheelRecall {
    private WheelRecall() {}

    private static final ResourceLocation ICONA = ResourceLocation.fromNamespaceAndPath(
            TFCobblemon.MODID, "textures/gui/interact/interact_wheel_icon_recall.png");

    /** Il colore dell'icona, come le altre della ruota. */
    private static final Vector3f COLORE = new Vector3f(1.0F, 1.0F, 1.0F);

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        CobblemonEvents.POKEMON_INTERACTION_GUI_CREATION.subscribe(WheelRecall::aggiungi);
    }

    private static void aggiungi(PokemonInteractionGUICreationEvent event) {
        // getGiveHeld e' vero solo sui propri Pokemon: e' il modo piu' vicino a
        // "questo e' tuo" che l'evento offra, e richiamare quello di un altro non
        // ha senso
        if (!event.getGiveHeld()) {
            return;
        }
        event.addFillingOption(new InteractWheelOption(
                ICONA, null, true, "tfcobblemon.ui.interact.recall",
                () -> COLORE,
                () -> {
                    PacketDistributor.sendToServer(new RecallPayload(event.getPokemonID()));
                    return Unit.INSTANCE;
                }));
    }
}
