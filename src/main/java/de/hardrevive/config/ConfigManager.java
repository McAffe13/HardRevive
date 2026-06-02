/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.config;

import de.hardrevive.HardRevive;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Manages all plugin configuration files, providing type-safe access.
 */
public final class ConfigManager {

    public enum DeathMode { HARDCORE, NORMAL }

    private final @NotNull HardRevive plugin;

    private FileConfiguration mainConfig;
    private FileConfiguration guiConfig;
    private FileConfiguration itemsConfig;
    private FileConfiguration recipesConfig;
    private FileConfiguration soundsConfig;
    private FileConfiguration effectsConfig;

    public ConfigManager(@NotNull HardRevive plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.mainConfig = plugin.getConfig();

        this.guiConfig     = loadOrCreate("gui.yml");
        this.itemsConfig   = loadOrCreate("items.yml");
        this.recipesConfig = loadOrCreate("recipes.yml");
        this.soundsConfig  = loadOrCreate("sounds.yml");
        this.effectsConfig = loadOrCreate("effects.yml");
    }

    private @NotNull FileConfiguration loadOrCreate(@NotNull String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        InputStream defaultStream = plugin.getResource(name);
        if (defaultStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaults);
        }
        return config;
    }

    public void save(@NotNull String name, @NotNull FileConfiguration config) {
        File file = new File(plugin.getDataFolder(), name);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + name, e);
        }
    }

    // -------------------------------------------------------------------------
    // Typed accessors — main config
    // -------------------------------------------------------------------------

    public @NotNull String getLanguage() {
        return mainConfig.getString("language", "en");
    }

    public @NotNull DeathMode getDeathMode() {
        String raw = mainConfig.getString("death-system.mode", "NORMAL");
        try {
            return DeathMode.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid death-system.mode '" + raw + "', defaulting to NORMAL.");
            return DeathMode.NORMAL;
        }
    }

    public boolean isBroadcastDeath()   { return mainConfig.getBoolean("broadcasts.player-death", true); }
    public boolean isBroadcastRevive()  { return mainConfig.getBoolean("broadcasts.player-revive", true); }
    public boolean isBroadcastBanned()  { return mainConfig.getBoolean("broadcasts.player-banned", true); }

    public boolean isUpdateCheckerEnabled()  { return mainConfig.getBoolean("update-checker.enabled", true); }
    public boolean isNotifyAdmins()          { return mainConfig.getBoolean("update-checker.notify-admins", true); }
    public @NotNull String getModrinthProjectId() {
        return mainConfig.getString("update-checker.modrinth-project-id", "hardrevive");
    }

    public boolean isBStatsEnabled()   { return mainConfig.getBoolean("bstats.enabled", true); }
    public boolean isDebugEnabled()    { return mainConfig.getBoolean("debug", false); }

    // -------------------------------------------------------------------------
    // Config file accessors
    // -------------------------------------------------------------------------

    public @NotNull FileConfiguration getMainConfig()    { return mainConfig; }
    public @NotNull FileConfiguration getGuiConfig()     { return guiConfig; }
    public @NotNull FileConfiguration getItemsConfig()   { return itemsConfig; }
    public @NotNull FileConfiguration getRecipesConfig() { return recipesConfig; }
    public @NotNull FileConfiguration getSoundsConfig()  { return soundsConfig; }
    public @NotNull FileConfiguration getEffectsConfig() { return effectsConfig; }
}
