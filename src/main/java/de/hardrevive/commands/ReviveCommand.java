/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.commands;

import de.hardrevive.HardRevive;
import de.hardrevive.config.LanguageManager;
import de.hardrevive.gui.ReviveListGUI;
import de.hardrevive.managers.DeadPlayerManager;
import de.hardrevive.models.DeadPlayer;
import de.hardrevive.utils.TimeUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Handles all /revive sub-commands.
 */
public final class ReviveCommand implements CommandExecutor, TabCompleter {

    private final @NotNull HardRevive plugin;
    private final @NotNull LanguageManager lang;
    private final @NotNull DeadPlayerManager deadManager;

    public ReviveCommand(@NotNull HardRevive plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
        this.deadManager = plugin.getDeadPlayerManager();
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "give"   -> handleGive(sender, args);
            case "list"   -> handleList(sender);
            case "info"   -> handleInfo(sender, args);
            case "help"   -> { showHelp(sender); yield true; }
            default       -> { showHelp(sender); yield true; }
        };
    }

    private boolean handleReload(@NotNull CommandSender sender) {
        if (!sender.hasPermission("hardrevive.reload")) {
            lang.send(sender, "no-permission");
            return true;
        }
        try {
            plugin.reload();
            lang.send(sender, "reload-success");
        } catch (Exception e) {
            lang.send(sender, "reload-failed");
            plugin.getLogger().severe("Reload failed: " + e.getMessage());
        }
        return true;
    }

    private boolean handleGive(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("hardrevive.give")) {
            lang.send(sender, "no-permission");
            return true;
        }
        if (args.length < 2) {
            lang.send(sender, "usage-give");
            return true;
        }

        String targetName = args[1];
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                lang.send(sender, "invalid-amount");
                return true;
            }
        }

        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            lang.send(sender, "player-not-found", Placeholder.unparsed("player", targetName));
            return true;
        }

        target.getInventory().addItem(plugin.getReviveItemManager().createReviveItem(amount));

        int finalAmount = amount;
        lang.send(sender, "give-success",
                Placeholder.unparsed("player", target.getName()),
                Placeholder.unparsed("amount", String.valueOf(finalAmount)));
        lang.send(target, "give-received",
                Placeholder.unparsed("amount", String.valueOf(finalAmount)));
        return true;
    }

    private boolean handleList(@NotNull CommandSender sender) {
        if (!sender.hasPermission("hardrevive.list")) {
            lang.send(sender, "no-permission");
            return true;
        }

        List<DeadPlayer> dead = deadManager.getAllDead();
        if (dead.isEmpty()) {
            lang.send(sender, "list-empty");
            return true;
        }

        lang.send(sender, "list-header", Placeholder.unparsed("count", String.valueOf(dead.size())));
        for (DeadPlayer dp : dead) {
            lang.send(sender, "list-entry",
                    Placeholder.unparsed("player", dp.getName()),
                    Placeholder.unparsed("time_ago", TimeUtils.formatTimeAgo(dp.getDeathTime())));
        }

        if (sender instanceof Player player) {
            new ReviveListGUI(plugin, player, dead, 0, null).open();
        }
        return true;
    }

    private boolean handleInfo(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("hardrevive.info")) {
            lang.send(sender, "no-permission");
            return true;
        }
        if (args.length < 2) {
            lang.send(sender, "player-not-found", Placeholder.unparsed("player", "?"));
            return true;
        }

        String targetName = args[1];
        DeadPlayer dead = findDeadByName(targetName);
        if (dead == null) {
            lang.send(sender, "player-not-dead", Placeholder.unparsed("player", targetName));
            return true;
        }

        lang.send(sender, "info-header", Placeholder.unparsed("player", dead.getName()));
        lang.send(sender, "info-death-date",
                Placeholder.unparsed("death_date", TimeUtils.formatDate(dead.getDeathTime())));
        lang.send(sender, "info-cause", Placeholder.unparsed("cause", dead.getDeathCause()));
        lang.send(sender, "info-time-ago",
                Placeholder.unparsed("time_ago", TimeUtils.formatTimeAgo(dead.getDeathTime())));
        lang.send(sender, "info-revive-count",
                Placeholder.unparsed("count", String.valueOf(
                        plugin.getDataStorage().getTotalRevives(dead.getUuid()))));
        return true;
    }

    private void showHelp(@NotNull CommandSender sender) {
        String version = plugin.getDescription().getVersion();
        lang.send(sender, "help-header");
        lang.send(sender, "help-title", Placeholder.unparsed("version", version));
        lang.send(sender, "help-reload");
        lang.send(sender, "help-give");
        lang.send(sender, "help-list");
        lang.send(sender, "help-info");
        lang.send(sender, "help-help");
        lang.send(sender, "help-footer");
    }

    private @Nullable DeadPlayer findDeadByName(@NotNull String name) {
        return deadManager.getAllDead().stream()
                .filter(dp -> dp.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (args.length == 1) {
            return List.of("reload", "give", "list", "info", "help").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            return deadManager.getAllDead().stream()
                    .map(DeadPlayer::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
