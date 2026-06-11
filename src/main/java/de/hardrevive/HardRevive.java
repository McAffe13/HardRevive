/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive;

import de.hardrevive.commands.RecipeCommand;
import de.hardrevive.commands.ReviveCommand;
import de.hardrevive.config.ConfigManager;
import de.hardrevive.config.LanguageManager;
import de.hardrevive.effects.EffectManager;
import de.hardrevive.gui.ReviveListGUI;
import de.hardrevive.hologram.RecipeHologramManager;
import de.hardrevive.items.ReviveItemManager;
import de.hardrevive.listeners.*;
import de.hardrevive.managers.BanManager;
import de.hardrevive.managers.DeadPlayerManager;
import de.hardrevive.recipes.RecipeManager;
import de.hardrevive.services.ReviveService;
import de.hardrevive.storage.DataStorage;
import de.hardrevive.storage.YamlStorage;
import de.hardrevive.updates.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * HardRevive — main plugin class.
 * Revive fallen players with custom craftable items, intuitive GUIs,
 * and fully configurable death management.
 */
public final class HardRevive extends JavaPlugin {

    private static final int BSTATS_ID = 31753; // Replace with actual bStats ID after registration

    // Late-initialized in onEnable(); guaranteed non-null after that point
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private DataStorage dataStorage;
    private DeadPlayerManager deadPlayerManager;
    private BanManager banManager;
    private ReviveItemManager reviveItemManager;
    private RecipeManager recipeManager;
    private EffectManager effectManager;
    private ReviveService reviveService;
    private RecipeHologramManager recipeHologramManager;
    private @Nullable UpdateChecker updateChecker;

    @Override
    public void onEnable() {
        printBanner();

        configManager  = new ConfigManager(this);
        configManager.load();

        languageManager = new LanguageManager(this);
        languageManager.load(configManager.getLanguage());

        dataStorage = new YamlStorage(this);
        dataStorage.load();

        deadPlayerManager = new DeadPlayerManager(this);
        banManager        = new BanManager(this);
        reviveItemManager = new ReviveItemManager(this);
        recipeManager     = new RecipeManager(this);
        effectManager     = new EffectManager(this);
        reviveService     = new ReviveService(this);

        recipeHologramManager = new RecipeHologramManager(this);
        recipeManager.register();
        registerListeners();
        registerCommands();

        setupBStats();

        if (configManager.isUpdateCheckerEnabled()) {
            updateChecker = new UpdateChecker(this);
            updateChecker.checkAsync();
        }

        getLogger().info("HardRevive v" + getPluginMeta().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        recipeHologramManager.removeAll();
        dataStorage.save();
        recipeManager.unregister();
        getLogger().info("HardRevive disabled. Data saved.");
    }

    /**
     * Reloads all configuration files and re-initialises dependent components.
     */
    public void reload() {
        configManager.load();
        languageManager.load(configManager.getLanguage());
        dataStorage.load();
        reviveItemManager.reload();
        recipeManager.reload();
        recipeHologramManager.reloadAdminHolograms();
        getLogger().info("HardRevive reloaded.");
    }

    // -------------------------------------------------------------------------
    // Private setup helpers
    // -------------------------------------------------------------------------

    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerDeathListener(this), this);
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new InventoryClickListener(this), this);
        pm.registerEvents(new ReviveItemListener(this), this);
        pm.registerEvents(recipeHologramManager, this);
    }

    private void registerCommands() {
        ReviveCommand reviveExecutor = new ReviveCommand(this);
        var reviveCmd = getCommand("revive");
        if (reviveCmd != null) {
            reviveCmd.setExecutor(reviveExecutor);
            reviveCmd.setTabCompleter(reviveExecutor);
        }

        RecipeCommand recipeExecutor = new RecipeCommand(this);
        var recipeCmd = getCommand("recipe");
        if (recipeCmd != null) {
            recipeCmd.setExecutor(recipeExecutor);
        }
    }

    private void setupBStats() {
        Metrics metrics = new Metrics(this, BSTATS_ID);

        metrics.addCustomChart(new SimplePie("death_mode",
                () -> configManager.getDeathMode().name()));

        metrics.addCustomChart(new SimplePie("language",
                () -> configManager.getLanguage()));

        metrics.addCustomChart(new SingleLineChart("dead_players",
                () -> deadPlayerManager.getAllDead().size()));

        metrics.addCustomChart(new SimplePie("update_checker_enabled",
                () -> configManager.isUpdateCheckerEnabled() ? "enabled" : "disabled"));
    }

    private void printBanner() {
        getLogger().info("  _  _              _ ___          _        ");
        getLogger().info(" | || |__ _ _ _ __| | _ \\___ __ _(_)_ ___  ");
        getLogger().info(" | __ / _` | '_/ _` |   / -_) V / \\ V / -_)");
        getLogger().info(" |_||_\\__,_|_| \\__,_|_|_\\___|\\_/|_|\\_/\\___|");
        getLogger().info(" Version: " + getPluginMeta().getVersion());
    }

    // -------------------------------------------------------------------------
    // Public accessors
    // -------------------------------------------------------------------------

    public @NotNull ConfigManager getConfigManager()         { return java.util.Objects.requireNonNull(configManager); }
    public @NotNull LanguageManager getLanguageManager()     { return java.util.Objects.requireNonNull(languageManager); }
    public @NotNull DataStorage getDataStorage()             { return java.util.Objects.requireNonNull(dataStorage); }
    public @NotNull DeadPlayerManager getDeadPlayerManager() { return java.util.Objects.requireNonNull(deadPlayerManager); }
    public @NotNull BanManager getBanManager()               { return java.util.Objects.requireNonNull(banManager); }
    public @NotNull ReviveItemManager getReviveItemManager() { return java.util.Objects.requireNonNull(reviveItemManager); }
    public @NotNull RecipeManager getRecipeManager()                 { return java.util.Objects.requireNonNull(recipeManager); }
    public @NotNull EffectManager getEffectManager()                 { return java.util.Objects.requireNonNull(effectManager); }
    public @NotNull ReviveService getReviveService()                 { return java.util.Objects.requireNonNull(reviveService); }
    public @NotNull RecipeHologramManager getRecipeHologramManager() { return java.util.Objects.requireNonNull(recipeHologramManager); }
    public @Nullable UpdateChecker getUpdateChecker()                { return updateChecker; }
}
