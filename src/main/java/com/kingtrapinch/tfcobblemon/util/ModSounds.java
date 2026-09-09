package com.kingtrapinch.tfcobblemon.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * I suoni arrivano da TFC e da vanilla, quindi si cercano per id: se la mod che
 * li registra non c'e', semplicemente non si sente niente.
 */
public final class ModSounds {
    private ModSounds() {}

    public static void playAt(Level level, Player player, String id) {
        final SoundEvent sound = find(id);
        if (sound == null) {
            return;
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Nullable
    private static SoundEvent find(String id) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(id));
    }
}
