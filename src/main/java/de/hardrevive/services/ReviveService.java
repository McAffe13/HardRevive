/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.services;

import de.hardrevive.HardRevive;
import de.hardrevive.config.LanguageManager;
import de.hardrevive.effects.EffectManager;
import de.hardrevive.items.ReviveItemManager;
import de.hardrevive.managers.BanManager;
import de.hardrevive.managers.DeadPlayerManager;
import de.hardrevive.models.DeadPlayer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Orchestrates the complete revival workflow: item consumption, state updates,
 * ban removal, effect playback, and broadcasts.
 */
public final class ReviveService {

    private final @NotNull HardRevive plugin;
    private final @NotNull DeadPlayerManager deadPlayerManager;
    private final @NotNull BanManager banManager;
    private final @NotNull ReviveItemManager itemManager;
    private final @NotNull EffectManager effectManager;
    private final @NotNull LanguageManager lang;

    public ReviveService(@NotNull HardRevive plugin) {
        this.plugin = plugin;
        this.deadPlayerManager = plugin.getDeadPlayerManager();
        this.banManager = plugin.getBanManager();
        this.itemManager = plugin.getReviveItemManager();
        this.effectManager = plugin.getEffectManager();
        this.lang = plugin.getLanguageManager();
    }

    /**
     * Attempt to revive a dead player.
     *
     * @param reviver  The player performing the revival (must hold a Revival Totem)
     * @param targetId UUID of the dead player to revive
     * @return true if the revival was successful
     */
    public boolean revive(@NotNull Player reviver, @NotNull UUID targetId) {
        DeadPlayer deadPlayer = deadPlayerManager.getDeadPlayer(targetId);
        if (deadPlayer == null) {
            lang.send(reviver, "player-not-dead",
                    Placeholder.unparsed("player", plugin.getServer().getOfflinePlayer(targetId).getName() != null
                            ? plugin.getServer().getOfflinePlayer(targetId).getName() : targetId.toString()));
            return false;
        }

        if (!itemManager.hasReviveItem(reviver)) {
            lang.send(reviver, "not-enough-revival-items");
            return false;
        }

        // Consume item
        itemManager.consumeReviveItem(reviver);
        lang.send(reviver, "revive-item-consumed");

        // Update state
        deadPlayerManager.markRevived(targetId);
        banManager.unbanPlayer(targetId);

        // Notify performer
        effectManager.playRevivePerformedEffect(reviver);

        // Broadcast if configured
        if (plugin.getConfigManager().isBroadcastRevive()) {
            lang.broadcast("revive-broadcast",
                    Placeholder.unparsed("player", deadPlayer.getName()),
                    Placeholder.unparsed("reviver", reviver.getName()));
        } else {
            lang.send(reviver, "player-revived",
                    Placeholder.unparsed("player", deadPlayer.getName()),
                    Placeholder.unparsed("reviver", reviver.getName()));
        }

        // Play effect at target's last known location if online (unlikely but possible with some plugins)
        Player onlineTarget = plugin.getServer().getPlayer(targetId);
        if (onlineTarget != null) {
            effectManager.playReviveEffect(onlineTarget);
        }

        return true;
    }
}
