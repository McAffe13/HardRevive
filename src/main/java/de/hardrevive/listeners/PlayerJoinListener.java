/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.listeners;

import de.hardrevive.HardRevive;
import de.hardrevive.config.LanguageManager;
import de.hardrevive.managers.DeadPlayerManager;
import de.hardrevive.models.DeadPlayer;
import de.hardrevive.updates.UpdateChecker;
import de.hardrevive.utils.TimeUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Blocks dead players from joining and notifies admins of updates.
 */
public final class PlayerJoinListener implements Listener {

    private final @NotNull HardRevive plugin;
    private final @NotNull DeadPlayerManager deadPlayerManager;
    private final @NotNull LanguageManager lang;

    public PlayerJoinListener(@NotNull HardRevive plugin) {
        this.plugin = plugin;
        this.deadPlayerManager = plugin.getDeadPlayerManager();
        this.lang = plugin.getLanguageManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(@NotNull PlayerLoginEvent event) {
        Player player = event.getPlayer();
        DeadPlayer dead = deadPlayerManager.getDeadPlayer(player.getUniqueId());
        if (dead == null) return;

        String message = lang.getRaw("ban-message")
                .replace("<death_date>", TimeUtils.formatDate(dead.getDeathTime()))
                .replace("<cause>", dead.getDeathCause());

        net.kyori.adventure.text.Component kickMsg =
                net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(message);
        String plainMsg = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(kickMsg);

        event.disallow(PlayerLoginEvent.Result.KICK_BANNED, kickMsg);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("hardrevive.update.notify")) return;
        if (!plugin.getConfigManager().isUpdateCheckerEnabled()) return;
        if (!plugin.getConfigManager().isNotifyAdmins()) return;

        UpdateChecker checker = plugin.getUpdateChecker();
        if (checker != null && checker.isUpdateAvailable()) {
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                lang.send(player, "update-available",
                        Placeholder.unparsed("current", plugin.getPluginMeta().getVersion()),
                        Placeholder.unparsed("latest", checker.getLatestVersion()));
                lang.send(player, "update-url");
            }, 40L);
        }
    }
}
