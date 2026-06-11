/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.config;

import de.hardrevive.HardRevive;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Loads language files and resolves message keys with placeholder substitution.
 */
public final class LanguageManager {

    private final @NotNull HardRevive plugin;
    private final @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();
    private @NotNull YamlConfiguration messages;
    private @Nullable String prefix;

    public LanguageManager(@NotNull HardRevive plugin) {
        this.plugin = plugin;
    }

    public void load(@NotNull String lang) {
        String resourcePath = "languages/messages_" + lang + ".yml";
        File file = new File(plugin.getDataFolder(), resourcePath);

        if (!file.exists()) {
            InputStream resource = plugin.getResource(resourcePath);
            if (resource == null) {
                plugin.getLogger().warning("Language file not found: " + resourcePath + ". Falling back to English.");
                resourcePath = "languages/messages_en.yml";
                file = new File(plugin.getDataFolder(), resourcePath);
            }
            plugin.saveResource(resourcePath, false);
        }

        messages = YamlConfiguration.loadConfiguration(file);

        InputStream defaultStream = plugin.getResource(resourcePath);
        if (defaultStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            messages.setDefaults(defaults);
        }

        this.prefix = messages.getString("prefix", "");
    }

    public @NotNull Component get(@NotNull String key, @NotNull TagResolver... resolvers) {
        String raw = messages.getString(key, "<red>Missing message: " + key);
        if (prefix != null && !prefix.isBlank() && !key.equals("prefix")) {
            raw = raw.replace("<prefix>", prefix);
        }
        return miniMessage.deserialize(raw, resolvers);
    }

    public @NotNull String getRaw(@NotNull String key) {
        String raw = messages.getString(key, "");
        if (prefix != null && !prefix.isBlank()) {
            raw = raw.replace("<prefix>", prefix);
        }
        return raw;
    }

    public void send(@NotNull CommandSender sender, @NotNull String key, @NotNull TagResolver... resolvers) {
        sender.sendMessage(get(key, resolvers));
    }

    public void broadcast(@NotNull String key, @NotNull TagResolver... resolvers) {
        Component msg = get(key, resolvers);
        plugin.getServer().broadcast(msg);
    }

    public @NotNull TagResolver placeholder(@NotNull String key, @NotNull String value) {
        return Placeholder.unparsed(key, value);
    }

    public @NotNull TagResolver componentPlaceholder(@NotNull String key, @NotNull Component value) {
        return Placeholder.component(key, value);
    }
}
