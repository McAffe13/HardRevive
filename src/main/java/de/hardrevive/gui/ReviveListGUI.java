/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.gui;

import de.hardrevive.HardRevive;
import de.hardrevive.effects.EffectManager;
import de.hardrevive.models.DeadPlayer;
import de.hardrevive.utils.ColorUtils;
import de.hardrevive.utils.ItemBuilder;
import de.hardrevive.utils.TimeUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Paginated GUI showing all dead players. Clicking a player head opens the confirmation GUI.
 */
public final class ReviveListGUI implements InventoryHolder {

    private final @NotNull HardRevive plugin;
    private final @NotNull Player viewer;
    private final @NotNull List<DeadPlayer> players;
    private final int page;
    private final @Nullable String searchQuery;
    private final @NotNull Inventory inventory;

    public static final String GUI_ID = "hardrevive:revive_list";

    public ReviveListGUI(
            @NotNull HardRevive plugin,
            @NotNull Player viewer,
            @NotNull List<DeadPlayer> players,
            int page,
            @Nullable String searchQuery
    ) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.players = players;
        this.page = page;
        this.searchQuery = searchQuery;

        FileConfiguration cfg = plugin.getConfigManager().getGuiConfig();
        int rows = cfg.getInt("revive-list.rows", 6);
        String title = cfg.getString("revive-list.title", "Dead Players");

        this.inventory = Bukkit.createInventory(this, rows * 9, ColorUtils.parse(title));
        populate();
    }

    private void populate() {
        FileConfiguration cfg = plugin.getConfigManager().getGuiConfig();
        int rows = cfg.getInt("revive-list.rows", 6);
        int size = rows * 9;

        List<Integer> playerSlots = cfg.getIntegerList("revive-list.player-slots");
        int playersPerPage = playerSlots.isEmpty() ? 28 : playerSlots.size();

        int totalPages = Math.max(1, (int) Math.ceil((double) players.size() / playersPerPage));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));
        int start = safePage * playersPerPage;
        int end = Math.min(start + playersPerPage, players.size());

        // Fill
        String fillMat = cfg.getString("revive-list.fill-item.material", "BLACK_STAINED_GLASS_PANE");
        String fillName = cfg.getString("revive-list.fill-item.name", " ");
        List<Integer> fillSlots = cfg.getIntegerList("revive-list.fill-item.slots");
        ItemStack fillItem = new ItemBuilder(parseMaterial(fillMat, Material.BLACK_STAINED_GLASS_PANE))
                .name(fillName).hideAll().build();

        for (int slot : fillSlots) {
            if (slot >= 0 && slot < size) inventory.setItem(slot, fillItem);
        }

        // Player heads
        List<DeadPlayer> pageEntries = players.subList(start, end);
        for (int i = 0; i < pageEntries.size() && i < playerSlots.size(); i++) {
            DeadPlayer dp = pageEntries.get(i);
            int slot = playerSlots.get(i);
            if (slot >= 0 && slot < size) {
                inventory.setItem(slot, buildPlayerHead(dp, cfg));
            }
        }

        // Search button
        int searchSlot = cfg.getInt("revive-list.search-button.slot", 48);
        String searchMat = cfg.getString("revive-list.search-button.material", "COMPASS");
        String searchName = cfg.getString("revive-list.search-button.name", "<yellow>Search Player");
        List<String> searchLore = cfg.getStringList("revive-list.search-button.lore");
        inventory.setItem(searchSlot, new ItemBuilder(parseMaterial(searchMat, Material.COMPASS))
                .name(searchName).loreStrings(searchLore).hideAll().build());

        // Previous page button
        int prevSlot = cfg.getInt("revive-list.previous-page.slot", 45);
        boolean hasPrev = safePage > 0;
        String prevMat = hasPrev
                ? cfg.getString("revive-list.previous-page.material", "ARROW")
                : cfg.getString("revive-list.previous-page.disabled-material", "GRAY_DYE");
        String prevName = hasPrev
                ? cfg.getString("revive-list.previous-page.name", "<yellow>Previous Page")
                : cfg.getString("revive-list.previous-page.disabled-name", "<gray>Previous Page");
        List<String> prevLore = hasPrev ? cfg.getStringList("revive-list.previous-page.lore") : List.of();
        String resolvedPrevName = prevName.replace("<current>", String.valueOf(safePage + 1))
                .replace("<total>", String.valueOf(totalPages));
        inventory.setItem(prevSlot, new ItemBuilder(parseMaterial(prevMat, Material.ARROW))
                .name(resolvedPrevName).loreStrings(prevLore).hideAll().build());

        // Next page button
        int nextSlot = cfg.getInt("revive-list.next-page.slot", 53);
        boolean hasNext = safePage < totalPages - 1;
        String nextMat = hasNext
                ? cfg.getString("revive-list.next-page.material", "ARROW")
                : cfg.getString("revive-list.next-page.disabled-material", "GRAY_DYE");
        String nextName = hasNext
                ? cfg.getString("revive-list.next-page.name", "<yellow>Next Page")
                : cfg.getString("revive-list.next-page.disabled-name", "<gray>Next Page");
        List<String> nextLore = hasNext ? cfg.getStringList("revive-list.next-page.lore") : List.of();
        String resolvedNextName = nextName.replace("<current>", String.valueOf(safePage + 1))
                .replace("<total>", String.valueOf(totalPages));
        inventory.setItem(nextSlot, new ItemBuilder(parseMaterial(nextMat, Material.ARROW))
                .name(resolvedNextName).loreStrings(nextLore).hideAll().build());

        // Page info
        int infoSlot = cfg.getInt("revive-list.page-info.slot", 49);
        String infoMat = cfg.getString("revive-list.page-info.material", "PAPER");
        String infoName = cfg.getString("revive-list.page-info.name", "<gray>Page <white><current> / <total>")
                .replace("<current>", String.valueOf(safePage + 1))
                .replace("<total>", String.valueOf(totalPages));
        inventory.setItem(infoSlot, new ItemBuilder(parseMaterial(infoMat, Material.PAPER))
                .name(infoName).hideAll().build());
    }

    private @NotNull ItemStack buildPlayerHead(@NotNull DeadPlayer dp, @NotNull FileConfiguration cfg) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(dp.getUuid()));
        }

        String nameTemplate = cfg.getString("player-entry.name", "<yellow><bold><player>")
                .replace("<player>", dp.getName());
        List<String> loreTemplate = cfg.getStringList("player-entry.lore");
        List<Component> lore = loreTemplate.stream()
                .map(line -> line
                        .replace("<player>", dp.getName())
                        .replace("<death_date>", TimeUtils.formatDate(dp.getDeathTime()))
                        .replace("<cause>", dp.getDeathCause())
                        .replace("<time_ago>", TimeUtils.formatTimeAgo(dp.getDeathTime())))
                .map(ColorUtils::parse)
                .toList();

        if (meta != null) {
            meta.displayName(ColorUtils.parse(nameTemplate));
            meta.lore(lore);

            // Store UUID in PDC for click handling
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "dead_player_uuid");
            meta.getPersistentDataContainer().set(key,
                    org.bukkit.persistence.PersistentDataType.STRING, dp.getUuid().toString());
            skull.setItemMeta(meta);
        }
        return skull;
    }

    public void open() {
        plugin.getEffectManager().playGuiSound(viewer, "gui-open");
        viewer.openInventory(inventory);
    }

    public int getPage() { return page; }
    public @Nullable String getSearchQuery() { return searchQuery; }
    public @NotNull List<DeadPlayer> getPlayers() { return players; }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }

    private @NotNull Material parseMaterial(@Nullable String name, @NotNull Material fallback) {
        if (name == null) return fallback;
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    public static @Nullable UUID getDeadPlayerUuid(@NotNull ItemStack item) {
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("hardrevive", "dead_player_uuid");
        String uuidStr = meta.getPersistentDataContainer().get(key,
                org.bukkit.persistence.PersistentDataType.STRING);
        if (uuidStr == null) return null;
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
