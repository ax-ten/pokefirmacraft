package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.api.storage.StoreCoordinates;
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingtrapinch.tfcobblemon.belt.BeltParty;
import com.kingtrapinch.tfcobblemon.pasture.BallBasket;
import com.kingtrapinch.tfcobblemon.pasture.PastureStore;
import com.kingtrapinch.tfcobblemon.pasture.Pastures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Le ball appese al pascolo, e cosa succede quando lo si rompe.
 *
 * <p>Il campo lo si aggiunge qui perche' la block entity di Cobblemon e' final:
 * non si estende, e un blocco nostro che ospitasse la loro entita' inciamperebbe
 * comunque su {@code togglePastureOn}, che fa un cast secco a {@code PastureBlock}
 * ad ogni tick. Tanto vale cambiare il pascolo che c'e'.
 */
@Mixin(PokemonPastureBlockEntity.class)
public abstract class PastureBasketMixin implements BallBasket {

    @Unique
    private static final String CHIAVE = "TfcobblemonBalls";

    @Unique
    private final List<ItemStack> tfcobblemon$cesta = new ArrayList<>();

    @Override
    public List<ItemStack> tfcobblemon$balls() {
        return tfcobblemon$cesta;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void tfcobblemon$salva(CompoundTag tag, HolderLookup.Provider registri, CallbackInfo ci) {
        final ListTag lista = new ListTag();
        for (ItemStack ball : tfcobblemon$cesta) {
            lista.add(ball.save(registri));
        }
        tag.put(CHIAVE, lista);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void tfcobblemon$carica(CompoundTag tag, HolderLookup.Provider registri, CallbackInfo ci) {
        tfcobblemon$cesta.clear();
        for (Tag voce : tag.getList(CHIAVE, Tag.TAG_COMPOUND)) {
            ItemStack.parse(registri, voce).ifPresent(tfcobblemon$cesta::add);
        }
    }

    /**
     * Un pascolo con qualcuno dentro si vede da fuori.
     *
     * <p>Cobblemon accende il blocco mentre qualcuno ha aperto la schermata, e
     * la schermata da noi non si apre mai: quello stato resterebbe spento per
     * sempre. Senza schermata serve pero' un segno, e questo e' l'unico che il
     * modello ha gia': acceso vuol dire che ci sono ball appese.
     */
    @Inject(method = "getInRangeViewerCount", at = @At("RETURN"), cancellable = true)
    private void tfcobblemon$accesoSeHaBall(Level level, BlockPos pos, double range,
                                            CallbackInfoReturnable<Integer> callback) {
        if (!tfcobblemon$cesta.isEmpty()) {
            callback.setReturnValue(Math.max(1, callback.getReturnValue()));
        }
    }

    /**
     * Rotto il pascolo, le ball cadono a terra e i Pokemon tornano nel box del
     * loro padrone. Si fa prima che Cobblemon sciolga i legami: dopo, dal
     * legame non si risalirebbe piu' al Pokemon.
     *
     * <p>Il padrone e' quello del legame e non quello del blocco: a un pascolo
     * ci puo' appendere una ball chiunque passi.
     */
    @Inject(method = "onBroken", at = @At("HEAD"))
    private void tfcobblemon$leBallCadono(CallbackInfo ci) {
        final PokemonPastureBlockEntity pascolo = (PokemonPastureBlockEntity) (Object) this;
        if (!(pascolo.getLevel() instanceof ServerLevel mondo)) {
            return;
        }
        for (PokemonPastureBlockEntity.Tethering legame : pascolo.getTetheredPokemon()) {
            final Pokemon mon = legame.getPokemon();
            if (mon == null) {
                continue;
            }
            Pastures.rientra(mon);
            // si riporta a casa solo chi stava nel deposito del pascolo: un
            // Pokemon messo al pascolo dal PC — cioe' alla maniera di
            // Cobblemon, prima di noi — nel PC ci resta
            final StoreCoordinates<?> dove = mon.getStoreCoordinates().get();
            if (dove != null && dove.getStore() instanceof PastureStore) {
                Pastures.trasloca(mon, BeltParty.deposito(legame.getPlayerId(), mondo.registryAccess()));
            }
        }
        final BlockPos pos = pascolo.getBlockPos();
        for (ItemStack ball : tfcobblemon$cesta) {
            Containers.dropItemStack(mondo, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ball);
        }
        tfcobblemon$cesta.clear();
    }
}
