/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.managers;

import com.destroystokyo.paper.profile.PlayerProfile;
import de.hardrevive.HardRevive;
import de.hardrevive.config.LanguageManager;
import de.hardrevive.models.DeadPlayer;
import de.hardrevive.utils.TimeUtils;
import io.papermc.paper.ban.BanListType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.ban.ProfileBanList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Handles banning and unbanning of dead players using Paper's profile ban list.
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
        PlayerProfile profile = resolveProfile(deadPlayer.getUuid());
        if (profile == null) return;
        profileBanList().addBan(profile, banMessage, (java.util.Date) null, "HardRevive");
    }

    public void unbanPlayer(@NotNull UUID uuid) {
        PlayerProfile profile = resolveProfile(uuid);
        if (profile == null) return;
        profileBanList().pardon(profile);
    }

    public boolean isBanned(@NotNull UUID uuid) {
        PlayerProfile profile = resolveProfile(uuid);
        if (profile == null) return false;
        return profileBanList().isBanned(profile);
    }

    private @NotNull ProfileBanList profileBanList() {
        return plugin.getServer().getBanList(BanListType.PROFILE);
    }

    private PlayerProfile resolveProfile(@NotNull UUID uuid) {
        OfflinePlayer offline = plugin.getServer().getOfflinePlayer(uuid);
        return offline.getPlayerProfile();
    }

    private @NotNull String buildBanMessage(@NotNull DeadPlayer deadPlayer) {
        String raw = lang.getRaw("ban-message")
                .replace("<death_date>", TimeUtils.formatDate(deadPlayer.getDeathTime()))
                .replace("<cause>", deadPlayer.getDeathCause());
        return MiniMessage.miniMessage().stripTags(raw);
    }
}
