/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.storage;

import de.hardrevive.HardRevive;
import de.hardrevive.models.DeadPlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * YAML-based implementation of {@link DataStorage}.
 * Thread-safe read access; writes are dispatched to the main thread via Bukkit scheduler.
 */
public final class YamlStorage implements DataStorage {

    private static final String DATA_FILE = "data.yml";

    private final @NotNull HardRevive plugin;
    private final @NotNull File dataFile;
    private final @NotNull Map<UUID, DeadPlayer> deadPlayers = new ConcurrentHashMap<>();
    private final @NotNull Map<UUID, Integer> reviveCounts = new ConcurrentHashMap<>();
    private @NotNull YamlConfiguration yaml;

    public YamlStorage(@NotNull HardRevive plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), DATA_FILE);
        this.yaml = new YamlConfiguration();
    }

    @Override
    public void load() {
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create data.yml", e);
                return;
            }
        }

        yaml = YamlConfiguration.loadConfiguration(dataFile);
        deadPlayers.clear();
        reviveCounts.clear();

        if (yaml.isConfigurationSection("dead-players")) {
            var section = yaml.getConfigurationSection("dead-players");
            if (section == null) return;
            for (String uuidStr : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String name        = section.getString(uuidStr + ".name", "Unknown");
                    long deathMillis   = section.getLong(uuidStr + ".death-time", 0L);
                    String cause       = section.getString(uuidStr + ".cause", "Unknown");
                    int revives        = section.getInt(uuidStr + ".revive-count", 0);
                    Instant deathTime  = Instant.ofEpochMilli(deathMillis);
                    deadPlayers.put(uuid, new DeadPlayer(uuid, name, deathTime, cause, revives));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Skipping invalid UUID in data.yml: " + uuidStr);
                }
            }
        }

        if (yaml.isConfigurationSection("revive-history")) {
            var section = yaml.getConfigurationSection("revive-history");
            if (section == null) return;
            for (String uuidStr : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    reviveCounts.put(uuid, section.getInt(uuidStr, 0));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        plugin.getLogger().info("Loaded " + deadPlayers.size() + " dead player(s) from storage.");
    }

    @Override
    public void save() {
        yaml.set("dead-players", null);

        for (Map.Entry<UUID, DeadPlayer> entry : deadPlayers.entrySet()) {
            String path = "dead-players." + entry.getKey();
            DeadPlayer dp = entry.getValue();
            yaml.set(path + ".name", dp.getName());
            yaml.set(path + ".death-time", dp.getDeathTime().toEpochMilli());
            yaml.set(path + ".cause", dp.getDeathCause());
            yaml.set(path + ".revive-count", dp.getReviveCount());
        }

        for (Map.Entry<UUID, Integer> entry : reviveCounts.entrySet()) {
            yaml.set("revive-history." + entry.getKey(), entry.getValue());
        }

        try {
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save data.yml", e);
        }
    }

    @Override
    public void addDeadPlayer(@NotNull DeadPlayer player) {
        deadPlayers.put(player.getUuid(), player);
        save();
    }

    @Override
    public void removeDeadPlayer(@NotNull UUID uuid) {
        deadPlayers.remove(uuid);
        save();
    }

    @Override
    public @Nullable DeadPlayer getDeadPlayer(@NotNull UUID uuid) {
        return deadPlayers.get(uuid);
    }

    @Override
    public boolean isDeadPlayer(@NotNull UUID uuid) {
        return deadPlayers.containsKey(uuid);
    }

    @Override
    public @NotNull Collection<DeadPlayer> getAllDeadPlayers() {
        return Collections.unmodifiableCollection(deadPlayers.values());
    }

    @Override
    public int getTotalRevives(@NotNull UUID uuid) {
        return reviveCounts.getOrDefault(uuid, 0);
    }

    @Override
    public void incrementRevives(@NotNull UUID uuid) {
        reviveCounts.merge(uuid, 1, Integer::sum);
        save();
    }
}
