/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.commands;

import de.hardrevive.HardRevive;
import de.hardrevive.config.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * /recipe (/crafting) — shows a hologram of the Revival Totem crafting recipe.
 * Admins (hardrevive.recipe.admin): permanent hologram, toggleable.
 * Users  (hardrevive.recipe):       temporary hologram (60 s, move > 6 blocks, or sneak).
 */
public final class RecipeCommand implements CommandExecutor {

    private final @NotNull HardRevive plugin;
    private final @NotNull LanguageManager lang;

    public RecipeCommand(@NotNull HardRevive plugin) {
        this.plugin = plugin;
        this.lang   = plugin.getLanguageManager();
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!(sender instanceof Player player)) {
            lang.send(sender, "console-only");
            return true;
        }

        if (!plugin.getConfigManager().getRecipesConfig().getBoolean("recipe.enabled", true)) {
            lang.send(player, "recipe-disabled");
            return true;
        }

        if (player.hasPermission("hardrevive.recipe.admin")) {
            boolean created = plugin.getRecipeHologramManager().toggleAdmin(player);
            lang.send(player, created ? "recipe-hologram-spawned" : "recipe-hologram-removed");
        } else if (player.hasPermission("hardrevive.recipe")) {
            plugin.getRecipeHologramManager().spawnUser(player);
            lang.send(player, "recipe-hologram-user");
        } else {
            lang.send(sender, "no-permission");
        }

        return true;
    }
}
