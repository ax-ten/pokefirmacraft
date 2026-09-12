package com.kingtrapinch.tfcobblemon.mixin;

import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingtrapinch.tfcobblemon.pasture.BallBasket;
import com.kingtrapinch.tfcobblemon.pasture.Pastures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


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
    private final NonNullList<ItemStack> tfcobblemon$cesta =
            NonNullList.withSize(BallBasket.POSTI, ItemStack.EMPTY);

    @Override
    public NonNullList<ItemStack> tfcobblemon$balls() {
        return tfcobblemon$cesta;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void tfcobblemon$salva(CompoundTag tag, HolderLookup.Provider registri, CallbackInfo ci) {
        final CompoundTag mio = new CompoundTag();
        ContainerHelper.saveAllItems(mio, tfcobblemon$cesta, true, registri);
        tag.put(CHIAVE, mio);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void tfcobblemon$carica(CompoundTag tag, HolderLookup.Provider registri, CallbackInfo ci) {
        tfcobblemon$cesta.clear();
        ContainerHelper.loadAllItems(tag.getCompound(CHIAVE), tfcobblemon$cesta, registri);
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
        if (tfcobblemon$cesta.stream().anyMatch(ball -> !ball.isEmpty())) {
            callback.setReturnValue(Math.max(1, callback.getReturnValue()));
        }
    }

    /**
     * Rotto il pascolo, le ball cadono dove stava il blocco e i Pokemon
     * rientrano. Il rientro e' quello del pascolo — lo stesso effetto con cui
     * ne escono — e non il richiamo verso il giocatore: il Pokemon non e'
     * tornato in tasca a nessuno, il suo recinto e' solo sparito.
     *
     * <p>Si fa prima che Cobblemon sciolga i legami: dopo, dal legame non si
     * risalirebbe piu' al Pokemon. E il padrone e' quello del legame, non
     * quello del blocco: a un pascolo ci puo' appendere una ball chiunque.
     */
    @Inject(method = "onBroken", at = @At("HEAD"))
    private void tfcobblemon$leBallCadono(CallbackInfo ci) {
        final PokemonPastureBlockEntity pascolo = (PokemonPastureBlockEntity) (Object) this;
        if (!(pascolo.getLevel() instanceof ServerLevel mondo)) {
            return;
        }
        final RegistryAccess registri = mondo.registryAccess();
        for (PokemonPastureBlockEntity.Tethering legame : pascolo.getTetheredPokemon()) {
            final Pokemon mon = legame.getPokemon();
            if (mon == null) {
                continue;
            }
            Pastures.rientra(mon, legame.getPlayerId(), registri);
        }
        final BlockPos pos = pascolo.getBlockPos();
        for (ItemStack ball : tfcobblemon$cesta) {
            if (!ball.isEmpty()) {
                Containers.dropItemStack(mondo, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ball);
            }
        }
        tfcobblemon$cesta.clear();
    }
}
