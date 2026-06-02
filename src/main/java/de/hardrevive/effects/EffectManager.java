/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.effects;

import de.hardrevive.HardRevive;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * Plays configured particle effects and sounds for plugin events.
 */
public final class EffectManager {

    private final @NotNull HardRevive plugin;

    public EffectManager(@NotNull HardRevive plugin) {
        this.plugin = plugin;
    }

    public void playReviveEffect(@NotNull Player target) {
        playParticle("revive", target.getLocation().add(0, 1, 0));
        playSound("revive-success", target);
    }

    public void playRevivePerformedEffect(@NotNull Player performer) {
        playSound("revive-performed", performer);
        playParticle("item-use", performer.getLocation().add(0, 1, 0));
    }

    public void playDeathEffect(@NotNull Player deceased) {
        playParticle("player-death", deceased.getLocation().add(0, 1, 0));
        playSound("player-death", deceased);
    }

    public void playGuiSound(@NotNull Player player, @NotNull String soundKey) {
        playSound(soundKey, player);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void playParticle(@NotNull String key, @NotNull Location location) {
        FileConfiguration cfg = plugin.getConfigManager().getEffectsConfig();
        String path = "effects." + key;
        if (!cfg.getBoolean(path + ".enabled", false)) return;

        String particleName = cfg.getString(path + ".particle", "");
        if (particleName.isBlank()) return;

        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            int count   = cfg.getInt(path + ".count", 10);
            double sx   = cfg.getDouble(path + ".spread-x", 0.3);
            double sy   = cfg.getDouble(path + ".spread-y", 0.5);
            double sz   = cfg.getDouble(path + ".spread-z", 0.3);
            double speed = cfg.getDouble(path + ".speed", 0.05);
            double oy   = cfg.getDouble(path + ".offset-y", 0);
            Location loc = location.clone().add(0, oy, 0);
            if (loc.getWorld() != null) {
                loc.getWorld().spawnParticle(particle, loc, count, sx, sy, sz, speed);
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unknown particle '" + particleName + "' in effects.yml at key: " + key);
        }
    }

    private void playSound(@NotNull String key, @NotNull Player player) {
        FileConfiguration cfg = plugin.getConfigManager().getSoundsConfig();
        String path = "sounds." + key;
        if (!cfg.getBoolean(path + ".enabled", false)) return;

        String soundName = cfg.getString(path + ".sound", "");
        if (soundName.isBlank()) return;

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            float volume = (float) cfg.getDouble(path + ".volume", 1.0);
            float pitch  = (float) cfg.getDouble(path + ".pitch", 1.0);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unknown sound '" + soundName + "' in sounds.yml at key: " + key);
        }
    }
}
