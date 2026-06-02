/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.listeners;

import de.hardrevive.HardRevive;
import de.hardrevive.config.ConfigManager;
import de.hardrevive.config.LanguageManager;
import de.hardrevive.effects.EffectManager;
import de.hardrevive.managers.BanManager;
import de.hardrevive.managers.DeadPlayerManager;
import de.hardrevive.models.DeadPlayer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handles player death events: records the death, bans the player, broadcasts.
 */
public final class PlayerDeathListener implements Listener {

    private final @NotNull HardRevive plugin;
    private final @NotNull DeadPlayerManager deadPlayerManager;
    private final @NotNull BanManager banManager;
    private final @NotNull LanguageManager lang;
    private final @NotNull ConfigManager config;
    private final @NotNull EffectManager effectManager;

    public PlayerDeathListener(@NotNull HardRevive plugin) {
        this.plugin = plugin;
        this.deadPlayerManager = plugin.getDeadPlayerManager();
        this.banManager = plugin.getBanManager();
        this.lang = plugin.getLanguageManager();
        this.config = plugin.getConfigManager();
        this.effectManager = plugin.getEffectManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Skip if player is already recorded as dead (edge-case: double-fire)
        if (deadPlayerManager.isDead(player.getUniqueId())) return;

        String cause = resolveCause(event);

        deadPlayerManager.markDead(player.getUniqueId(), player.getName(), cause);

        DeadPlayer deadPlayer = deadPlayerManager.getDeadPlayer(player.getUniqueId());
        if (deadPlayer == null) return;

        // Schedule ban on next tick so the death event fully completes first
        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
            banManager.banPlayer(deadPlayer);

            if (config.isBroadcastDeath()) {
                lang.broadcast("player-died",
                        Placeholder.unparsed("player", player.getName()),
                        Placeholder.unparsed("cause", cause));
            }
            if (config.isBroadcastBanned()) {
                lang.broadcast("player-banned",
                        Placeholder.unparsed("player", player.getName()));
            }

            effectManager.playDeathEffect(player);
        });
    }

    private @NotNull String resolveCause(@NotNull PlayerDeathEvent event) {
        net.kyori.adventure.text.Component deathMsg = event.deathMessage();
        if (deathMsg != null) {
            String raw = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                    .plainText().serialize(deathMsg);
            if (!raw.isBlank()) return raw;
        }
        if (event.getEntity().getKiller() != null) {
            return "Killed by " + event.getEntity().getKiller().getName();
        }
        return "Unknown";
    }
}
