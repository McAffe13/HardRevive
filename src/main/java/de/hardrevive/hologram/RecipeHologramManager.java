/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.hologram;

import de.hardrevive.HardRevive;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages recipe holograms for all online players.
 * Admin holograms are permanent (toggle) and rebuild on recipe reload.
 * User holograms expire after 60 s, on movement > 6 blocks, or on sneak.
 */
public final class RecipeHologramManager implements Listener {

    private static final double MAX_DISTANCE_SQ = 6.0 * 6.0;
    private static final long   USER_DURATION_MS = 60_000L;
    private static final double SPAWN_DISTANCE   = 2.5;

    private final @NotNull HardRevive plugin;
    private final Map<UUID, RecipeHologram> adminHolograms = new HashMap<>();
    private final Map<UUID, RecipeHologram> userHolograms  = new HashMap<>();
    private final Map<UUID, Long>           userExpiry     = new HashMap<>();

    public RecipeHologramManager(@NotNull HardRevive plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }

    /** Toggle admin hologram on/off. Returns true if created, false if removed. */
    public boolean toggleAdmin(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        if (adminHolograms.containsKey(uuid)) {
            adminHolograms.remove(uuid).remove();
            return false;
        }
        adminHolograms.put(uuid, spawnHologram(player));
        return true;
    }

    /** Spawn a temporary user hologram, replacing any previous one. */
    public void spawnUser(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        RecipeHologram old = userHolograms.remove(uuid);
        if (old != null) old.remove();
        userHolograms.put(uuid, spawnHologram(player));
        userExpiry.put(uuid, System.currentTimeMillis() + USER_DURATION_MS);
    }

    /** Rebuild all admin holograms after the recipe was reloaded. */
    public void reloadAdminHolograms() {
        Map<UUID, RecipeHologram> snapshot = new HashMap<>(adminHolograms);
        adminHolograms.clear();
        snapshot.forEach((uuid, holo) -> {
            holo.remove();
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null && p.isOnline() && holo.getAnchor().getWorld() != null) {
                adminHolograms.put(uuid,
                        new RecipeHologram(plugin, holo.getAnchor(), holo.getRightVec()));
            }
        });
    }

    /** Remove all holograms (called on plugin disable). */
    public void removeAll() {
        adminHolograms.values().forEach(RecipeHologram::remove);
        adminHolograms.clear();
        userHolograms.values().forEach(RecipeHologram::remove);
        userHolograms.clear();
        userExpiry.clear();
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        RecipeHologram admin = adminHolograms.remove(uuid);
        if (admin != null) admin.remove();
        RecipeHologram user = userHolograms.remove(uuid);
        if (user != null) user.remove();
        userExpiry.remove(uuid);
    }

    // -------------------------------------------------------------------------

    private @NotNull RecipeHologram spawnHologram(@NotNull Player player) {
        Vector forward = horizontalForward(player);
        Vector right   = forward.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        Location center = player.getEyeLocation().add(forward.clone().multiply(SPAWN_DISTANCE));
        return new RecipeHologram(plugin, center, right);
    }

    private @NotNull Vector horizontalForward(@NotNull Player player) {
        Vector dir = player.getLocation().getDirection().clone().setY(0);
        if (dir.lengthSquared() < 0.001) dir = new Vector(0, 0, 1);
        return dir.normalize();
    }

    private void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                userHolograms.entrySet().removeIf(entry -> {
                    UUID   uuid   = entry.getKey();
                    Player player = plugin.getServer().getPlayer(uuid);

                    if (player == null || !player.isOnline()) {
                        entry.getValue().remove();
                        userExpiry.remove(uuid);
                        return true;
                    }
                    if (now >= userExpiry.getOrDefault(uuid, 0L)) {
                        entry.getValue().remove();
                        userExpiry.remove(uuid);
                        return true;
                    }
                    if (player.isSneaking()) {
                        entry.getValue().remove();
                        userExpiry.remove(uuid);
                        return true;
                    }
                    Location holo = entry.getValue().getAnchor();
                    if (!player.getWorld().equals(holo.getWorld())
                            || player.getLocation().distanceSquared(holo) > MAX_DISTANCE_SQ) {
                        entry.getValue().remove();
                        userExpiry.remove(uuid);
                        return true;
                    }
                    return false;
                });
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }
}
