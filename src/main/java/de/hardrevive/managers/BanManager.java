/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.managers;

import de.hardrevive.HardRevive;
import de.hardrevive.config.LanguageManager;
import de.hardrevive.models.DeadPlayer;
import de.hardrevive.utils.TimeUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.BanList;
import org.bukkit.BanEntry;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.UUID;

/**
 * Handles banning and unbanning of dead players.
 */
public final class BanManager {

    private final @NotNull HardRevive plugin;
    private final @NotNull LanguageManager lang;

    public BanManager(@NotNull HardRevive plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
    }

    public void banPlayer(@NotNull DeadPlayer deadPlayer) {
        String banMessage = buildBanMessage(deadPlayer);
        BanList<OfflinePlayer> banList = plugin.getServer().getBanList(BanList.Type.PROFILE);
        OfflinePlayer offline = plugin.getServer().getOfflinePlayer(deadPlayer.getUuid());
        banList.addBan(offline.getPlayerProfile(), banMessage, (Date) null, "HardRevive");
    }

    public void unbanPlayer(@NotNull UUID uuid) {
        OfflinePlayer offline = plugin.getServer().getOfflinePlayer(uuid);
        BanList<OfflinePlayer> banList = plugin.getServer().getBanList(BanList.Type.PROFILE);
        banList.pardon(offline.getPlayerProfile());
    }

    public boolean isBanned(@NotNull UUID uuid) {
        OfflinePlayer offline = plugin.getServer().getOfflinePlayer(uuid);
        BanList<OfflinePlayer> banList = plugin.getServer().getBanList(BanList.Type.PROFILE);
        return banList.isBanned(offline.getPlayerProfile());
    }

    private @NotNull String buildBanMessage(@NotNull DeadPlayer deadPlayer) {
        String raw = lang.getRaw("ban-message")
                .replace("<death_date>", TimeUtils.formatDate(deadPlayer.getDeathTime()))
                .replace("<cause>", deadPlayer.getDeathCause());
        return MiniMessage.miniMessage().stripTags(raw);
    }
}
