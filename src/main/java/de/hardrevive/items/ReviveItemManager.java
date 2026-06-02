/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.items;

import de.hardrevive.HardRevive;
import de.hardrevive.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Creates and identifies Revival Totem item stacks from items.yml configuration.
 */
public final class ReviveItemManager {

    private static final String REVIVE_PDC_KEY = "hardrevive:revive_item";

    private final @NotNull HardRevive plugin;
    private @NotNull ItemStack cachedItem;

    public ReviveItemManager(@NotNull HardRevive plugin) {
        this.plugin = plugin;
        this.cachedItem = buildItem();
    }

    public void reload() {
        this.cachedItem = buildItem();
    }

    public @NotNull ItemStack createReviveItem() {
        return cachedItem.clone();
    }

    public @NotNull ItemStack createReviveItem(int amount) {
        ItemStack item = cachedItem.clone();
        item.setAmount(Math.max(1, Math.min(amount, 64)));
        return item;
    }

    public boolean isReviveItem(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "revive_item");
        org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(key, org.bukkit.persistence.PersistentDataType.BYTE);
    }

    public boolean hasReviveItem(@NotNull org.bukkit.entity.Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isReviveItem(item)) return true;
        }
        return false;
    }

    public void consumeReviveItem(@NotNull org.bukkit.entity.Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && isReviveItem(item)) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().setItem(i, null);
                }
                return;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private @NotNull ItemStack buildItem() {
        FileConfiguration cfg = plugin.getConfigManager().getItemsConfig();

        String materialName = cfg.getString("revive-item.material", "TOTEM_OF_UNDYING");
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material '" + materialName + "' in items.yml, using TOTEM_OF_UNDYING.");
            material = Material.TOTEM_OF_UNDYING;
        }

        String name = cfg.getString("revive-item.name", "<gold>Revival Totem");
        List<String> lore = cfg.getStringList("revive-item.lore");
        boolean glint = cfg.getBoolean("revive-item.glint", true);
        int customModelData = cfg.getInt("revive-item.custom-model-data", 0);
        List<String> flagNames = cfg.getStringList("revive-item.item-flags");

        ItemBuilder builder = new ItemBuilder(material)
                .name(name)
                .loreStrings(lore)
                .glint(glint)
                .customModelData(customModelData);

        for (String flagName : flagNames) {
            try {
                builder.flags(ItemFlag.valueOf(flagName.toUpperCase()));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown ItemFlag '" + flagName + "' in items.yml.");
            }
        }

        ItemStack item = builder.build();

        // Tag with PDC so we can reliably identify the item
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "revive_item");
            meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }

        return item;
    }
}
