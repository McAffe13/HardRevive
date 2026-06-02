/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.updates;

import de.hardrevive.HardRevive;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks Modrinth for new plugin versions asynchronously on startup.
 */
public final class UpdateChecker {

    private static final String MODRINTH_API = "https://api.modrinth.com/v2/project/%s/version?loaders=[%%22paper%%22]&game_versions=[]";
    private static final Pattern VERSION_PATTERN = Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"");

    private final @NotNull HardRevive plugin;
    private volatile boolean updateAvailable = false;
    private volatile @NotNull String latestVersion = "unknown";

    public UpdateChecker(@NotNull HardRevive plugin) {
        this.plugin = plugin;
    }

    public void checkAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::check);
    }

    private void check() {
        String projectId = plugin.getConfigManager().getModrinthProjectId();
        String url = String.format(MODRINTH_API, projectId);
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "HardRevive/" + plugin.getDescription().getVersion()
                    + " (https://modrinth.com/plugin/hardrevive)");

            if (conn.getResponseCode() != 200) {
                scheduleLangMessage("update-check-failed");
                return;
            }

            try (InputStream is = conn.getInputStream()) {
                String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Matcher matcher = VERSION_PATTERN.matcher(response);
                if (matcher.find()) {
                    latestVersion = matcher.group(1);
                    String current = plugin.getDescription().getVersion();
                    if (!current.equals(latestVersion)) {
                        updateAvailable = true;
                        Bukkit.getScheduler().runTask(plugin, () ->
                                plugin.getLanguageManager().broadcast("update-available",
                                        net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("current", current),
                                        net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("latest", latestVersion)));
                    } else {
                        if (plugin.getConfigManager().isDebugEnabled()) {
                            plugin.getLogger().info("HardRevive is up to date (v" + current + ").");
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().log(Level.WARNING, "Update check failed", e);
            } else {
                plugin.getLogger().warning("Could not check for updates: " + e.getMessage());
            }
        }
    }

    private void scheduleLangMessage(@NotNull String key) {
        Bukkit.getScheduler().runTask(plugin, () ->
                plugin.getLogger().info(plugin.getLanguageManager().getRaw(key)));
    }

    public boolean isUpdateAvailable() { return updateAvailable; }

    public @NotNull String getLatestVersion() { return latestVersion; }
}
