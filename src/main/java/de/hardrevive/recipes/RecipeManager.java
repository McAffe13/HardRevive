/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.recipes;

import de.hardrevive.HardRevive;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registers and unregisters the Revival Totem shaped crafting recipe.
 */
public final class RecipeManager {

    private static final NamespacedKey RECIPE_KEY = new NamespacedKey("hardrevive", "revival_totem");

    private final @NotNull HardRevive plugin;
    private boolean registered = false;

    public RecipeManager(@NotNull HardRevive plugin) {
        this.plugin = plugin;
    }

    public void register() {
        FileConfiguration cfg = plugin.getConfigManager().getRecipesConfig();
        if (!cfg.getBoolean("recipe.enabled", true)) return;

        List<String> shapeList = cfg.getStringList("recipe.shape");
        if (shapeList.size() != 3) {
            plugin.getLogger().warning("recipes.yml: shape must have exactly 3 rows. Recipe not registered.");
            return;
        }

        ConfigurationSection ingredientsSection = cfg.getConfigurationSection("recipe.ingredients");
        if (ingredientsSection == null) {
            plugin.getLogger().warning("recipes.yml: no ingredients defined. Recipe not registered.");
            return;
        }

        int resultAmount = cfg.getInt("recipe.result-amount", 1);
        ShapedRecipe recipe = new ShapedRecipe(RECIPE_KEY, plugin.getReviveItemManager().createReviveItem(resultAmount));
        recipe.shape(shapeList.get(0), shapeList.get(1), shapeList.get(2));

        for (String keyChar : ingredientsSection.getKeys(false)) {
            if (keyChar.length() != 1) continue;
            String materialName = ingredientsSection.getString(keyChar, "AIR");
            try {
                Material mat = Material.valueOf(materialName.toUpperCase());
                recipe.setIngredient(keyChar.charAt(0), mat);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown material '" + materialName + "' for ingredient '" + keyChar + "' in recipes.yml.");
            }
        }

        plugin.getServer().addRecipe(recipe);
        registered = true;
        plugin.getLogger().info("Revival Totem crafting recipe registered.");
    }

    public void unregister() {
        if (registered) {
            plugin.getServer().removeRecipe(RECIPE_KEY);
            registered = false;
        }
    }

    public void reload() {
        unregister();
        register();
    }
}
