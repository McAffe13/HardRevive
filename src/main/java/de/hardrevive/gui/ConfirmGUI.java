/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.gui;

import de.hardrevive.HardRevive;
import de.hardrevive.models.DeadPlayer;
import de.hardrevive.utils.ColorUtils;
import de.hardrevive.utils.ItemBuilder;
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

/**
 * Confirmation dialog shown before reviving a specific player.
 */
public final class ConfirmGUI implements InventoryHolder {

    private final @NotNull HardRevive plugin;
    private final @NotNull Player viewer;
    private final @NotNull DeadPlayer target;
    private final int listPage;
    private final @Nullable String searchQuery;
    private final @NotNull Inventory inventory;

    public ConfirmGUI(
            @NotNull HardRevive plugin,
            @NotNull Player viewer,
            @NotNull DeadPlayer target,
            int listPage,
            @Nullable String searchQuery
    ) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.target = target;
        this.listPage = listPage;
        this.searchQuery = searchQuery;

        FileConfiguration cfg = plugin.getConfigManager().getGuiConfig();
        int rows = cfg.getInt("confirm.rows", 3);
        String title = cfg.getString("confirm.title", "<dark_red>Confirm Revival");

        this.inventory = Bukkit.createInventory(this, rows * 9, ColorUtils.parse(title));
        populate();
    }

    private void populate() {
        FileConfiguration cfg = plugin.getConfigManager().getGuiConfig();
        int rows = cfg.getInt("confirm.rows", 3);
        int size = rows * 9;

        // Fill
        String fillMat = cfg.getString("confirm.fill-item.material", "BLACK_STAINED_GLASS_PANE");
        String fillName = cfg.getString("confirm.fill-item.name", " ");
        List<Integer> fillSlots = cfg.getIntegerList("confirm.fill-item.slots");
        ItemStack fillItem = new ItemBuilder(parseMaterial(fillMat, Material.BLACK_STAINED_GLASS_PANE))
                .name(fillName).hideAll().build();
        for (int slot : fillSlots) {
            if (slot >= 0 && slot < size) inventory.setItem(slot, fillItem);
        }

        // Player head
        int headSlot = cfg.getInt("confirm.player-head.slot", 13);
        inventory.setItem(headSlot, buildHead());

        // Confirm button
        int confirmSlot = cfg.getInt("confirm.confirm-button.slot", 11);
        String confirmMat = cfg.getString("confirm.confirm-button.material", "LIME_STAINED_GLASS_PANE");
        String confirmName = cfg.getString("confirm.confirm-button.name", "<green><bold>✔ Confirm");
        List<String> confirmLore = cfg.getStringList("confirm.confirm-button.lore").stream()
                .map(l -> l.replace("<player>", target.getName())).toList();
        inventory.setItem(confirmSlot, new ItemBuilder(parseMaterial(confirmMat, Material.LIME_STAINED_GLASS_PANE))
                .name(confirmName).loreStrings(confirmLore).hideAll().build());

        // Cancel button
        int cancelSlot = cfg.getInt("confirm.cancel-button.slot", 15);
        String cancelMat = cfg.getString("confirm.cancel-button.material", "RED_STAINED_GLASS_PANE");
        String cancelName = cfg.getString("confirm.cancel-button.name", "<red><bold>✖ Cancel");
        List<String> cancelLore = cfg.getStringList("confirm.cancel-button.lore");
        inventory.setItem(cancelSlot, new ItemBuilder(parseMaterial(cancelMat, Material.RED_STAINED_GLASS_PANE))
                .name(cancelName).loreStrings(cancelLore).hideAll().build());
    }

    private @NotNull ItemStack buildHead() {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(target.getUuid()));
            meta.displayName(ColorUtils.parse("<yellow><bold>" + target.getName()));
            skull.setItemMeta(meta);
        }
        return skull;
    }

    public void open() {
        plugin.getEffectManager().playGuiSound(viewer, "gui-open");
        viewer.openInventory(inventory);
    }

    public @NotNull DeadPlayer getTarget() { return target; }
    public int getListPage() { return listPage; }
    public @Nullable String getSearchQuery() { return searchQuery; }

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
}
