package com.kingtrapinch.tfcobblemon.belt;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingtrapinch.tfcobblemon.TFCobblemon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Dove sta la ball di un Pokemon, e chi la crea o la distrugge.
 *
 * <p>Una sola domanda sta alla base di tutto: <b>questo Pokemon ha una ball, e
 * dove?</b> Sbagliare quella risposta ha due esiti e sono entrambi cattivi — se
 * si risponde "non ce l'ha" quando invece c'e', gliene si conia una seconda e il
 * Pokemon finisce in due ball entrambe funzionanti; se si risponde "ce l'ha"
 * quando non c'e', il Pokemon resta senza modo di essere richiamato.
 *
 * <p>Per questo la ricerca passa da un posto solo, {@link #ovunque}, e guarda
 * <b>tre</b> posti: lo slot cintura (che puo' contenere una ball nuda o una
 * cintura piena di ball), l'inventario (dove a sua volta puo' esserci una
 * cintura), e <b>il cursore della schermata aperta</b>. Il cursore e' quello che
 * ci e' sfuggito piu' a lungo: non fa parte dell'inventario, e una ball tirata
 * fuori dalla cintura ci sta sopra per tutto il tempo del gesto.
 */
public final class BallHandover {
    private BallHandover() {}

    /** Quanti tick la ball resta inerte dopo che il Pokemon e' rientrato. */
    private static final int RESPIRO = 20;

    /** Un posto dove una ball puo' stare, e come riscriverlo quando cambia. */
    private record Posto(ItemStack stack, Runnable salva, Consumer<ItemStack> sostituisci) {}

    /**
     * Il contorno di un movimento nel PC: si fa quello che va fatto sulle ball,
     * poi la squadra si riallinea.
     *
     * <p>Tutto dentro un {@code try}: il deposito e' di Cobblemon e il contorno
     * e' nostro, e un nostro inciampo non deve poter far fallire il suo gesto.
     * Senza questo un'eccezione qui dentro lasciava il movimento a meta' —
     * avvenuto sul server, mai confermato al client — e a schermo sembrava un
     * Pokemon che si rifiutava di andare nel PC, o che si sdoppiava.
     */
    public static void afterPc(ServerPlayer player, Runnable cosa) {
        try {
            cosa.run();
        } catch (Exception e) {
            TFCobblemon.LOGGER.error("ball e PC", e);
        }
        try {
            BeltParty.align(player);
        } catch (Exception e) {
            TFCobblemon.LOGGER.error("allineamento dopo il PC", e);
        }
    }

    /** Tutti i posti dove una ball di questo giocatore puo' trovarsi. */
    private static List<Posto> posti(ServerPlayer player) {
        final List<Posto> tutti = new ArrayList<>();

        final ItemStack nelloSlot = Belts.inBeltSlot(player);
        aggiungi(tutti, nelloSlot, () -> Belts.store(player, nelloSlot),
                nuovo -> Belts.store(player, nuovo));

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            final int posto = i;
            aggiungi(tutti, player.getInventory().getItem(i), () -> {},
                    nuovo -> player.getInventory().setItem(posto, nuovo));
        }

        // il cursore della schermata aperta: non e' parte dell'inventario, e una
        // ball appena tirata fuori dalla cintura ci sta sopra
        if (player.containerMenu != null) {
            final ItemStack inMano = player.containerMenu.getCarried();
            aggiungi(tutti, inMano, () -> {}, player.containerMenu::setCarried);
        }

        return tutti;
    }

    /** Un posto conta per se' e, se e' una cintura, anche per quel che porta. */
    private static void aggiungi(List<Posto> tutti, ItemStack stack,
                                 Runnable salva, Consumer<ItemStack> sostituisci) {
        if (stack.isEmpty()) {
            return;
        }
        tutti.add(new Posto(stack, salva, sostituisci));
        if (!(stack.getItem() instanceof TrainerBeltItem belt)) {
            return;
        }
        final List<ItemStack> dentro = belt.posti(stack);
        for (int i = 0; i < dentro.size(); i++) {
            final int quale = i;
            tutti.add(new Posto(dentro.get(i), () -> {
                belt.salva(stack, dentro);
                salva.run();
            }, nuovo -> {
                dentro.set(quale, nuovo);
                belt.salva(stack, dentro);
                salva.run();
            }));
        }
    }

    /** Il posto dove sta la ball di questo Pokemon, o niente. */
    private static Posto ovunque(ServerPlayer player, UUID pokemon) {
        for (Posto posto : posti(player)) {
            if (punta(posto.stack(), pokemon)) {
                return posto;
            }
        }
        return null;
    }

    /** Se il giocatore ha da qualche parte la ball di questo Pokemon. */
    public static boolean anywhere(ServerPlayer player, UUID pokemon) {
        return ovunque(player, pokemon) != null;
    }

    /** Se il Pokemon sta in una ball che il giocatore ha <em>addosso</em>. */
    public static boolean onBelt(ServerPlayer player, UUID pokemon) {
        for (UUID addosso : BeltParty.wanted(player)) {
            if (pokemon.equals(addosso)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Via la ball di chi e' stato depositato, da qualunque posto. Ogni copia: se
     * ne girano due per lo stesso Pokemon, nessuna deve sopravvivere.
     */
    public static void forget(ServerPlayer player, UUID pokemon) {
        for (Posto posto : posti(player)) {
            if (punta(posto.stack(), pokemon)) {
                posto.sostituisci().accept(ItemStack.EMPTY);
            }
        }
    }

    /** Segna se il Pokemon di questa ball e' in campo, dovunque la ball sia. */
    public static void mark(ServerPlayer player, UUID pokemon, boolean fuori) {
        final Posto posto = ovunque(player, pokemon);
        if (posto == null) {
            return;
        }
        final BallLink legame = BallLink.read(posto.stack());
        if (legame != null && legame.out() != fuori) {
            posto.stack().set(ModBallData.BALL_LINK.get(), legame.withOut(fuori));
            posto.salva().run();
        }
    }

    /**
     * La ball di chi e' stato ritirato, consegnata in mano: prima la cintura se
     * c'e' posto, altrimenti l'inventario. Sopra i posti disponibili si esce
     * comunque, come oggetto — e' il modo di portarsi via piu' di quanti se ne
     * possano usare.
     */
    public static void hand(ServerPlayer player, UUID pokemon) {
        final Pokemon mon = cerca(player, pokemon);
        if (mon == null) {
            return;
        }
        // prima si fa piazza pulita: se una ball per questo Pokemon gira ancora,
        // coniarne un'altra vorrebbe dire due maniglie per una creatura
        forget(player, pokemon);
        consegna(player, mon, player.position());
    }

    /**
     * Rimette in mano la ball di un Pokemon: nell'inventario se c'e' posto,
     * altrimenti a terra dove indicato.
     */
    public static void give(ServerPlayer player, Pokemon mon, Vec3 dove) {
        if (anywhere(player, mon.getUuid())) {
            return;
        }
        consegna(player, mon, dove);
    }

    private static void consegna(ServerPlayer player, Pokemon mon, Vec3 dove) {
        // l'handle non si inventa: lo tiene il Pokemon nei suoi dati, ed e' cosi'
        // che la ball che torna e' la stessa di prima e non una gemella
        final String padrone = mon.getPersistentData().getString(BallLink.OWNER);
        final UUID handle = padrone.isEmpty() ? UUID.randomUUID() : UUID.fromString(padrone);
        if (padrone.isEmpty()) {
            mon.getPersistentData().putString(BallLink.OWNER, handle.toString());
        }
        final ItemStack ball = mon.getCaughtBall().stack(1);
        ball.set(ModBallData.BALL_LINK.get(), BallLink.of(mon, handle));
        // un secondo di respiro: la ball torna in mano nell'istante in cui il
        // Pokemon rientra, e senza pausa un doppio clic lo rispedisce fuori
        // prima che l'animazione di rientro sia finita
        player.getCooldowns().addCooldown(ball.getItem(), RESPIRO);
        if (Belts.insert(player, ball)) {
            return;
        }
        if (player.getInventory().add(ball)) {
            return;
        }
        player.serverLevel().addFreshEntity(
                new ItemEntity(player.serverLevel(), dove.x, dove.y, dove.z, ball));
    }

    private static boolean punta(ItemStack stack, UUID pokemon) {
        final BallLink legame = BallLink.read(stack);
        return legame != null && legame.pokemon().equals(pokemon);
    }

    private static Pokemon cerca(ServerPlayer player, UUID pokemon) {
        return BeltParty.trova(player, pokemon);
    }
}
