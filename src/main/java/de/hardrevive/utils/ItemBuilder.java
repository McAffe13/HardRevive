/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Fluent builder for creating {@link ItemStack} instances.
 */
public final class ItemBuilder {

    private final @NotNull ItemStack item;
    private final @NotNull ItemMeta meta;

    public ItemBuilder(@NotNull Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(@NotNull ItemStack base) {
        this.item = base.clone();
        this.meta = this.item.getItemMeta();
    }

    public @NotNull ItemBuilder name(@NotNull Component name) {
        meta.displayName(name);
        return this;
    }

    public @NotNull ItemBuilder name(@NotNull String miniMessage) {
        meta.displayName(ColorUtils.parse(miniMessage));
        return this;
    }

    public @NotNull ItemBuilder lore(@NotNull List<Component> lore) {
        meta.lore(lore);
        return this;
    }

    public @NotNull ItemBuilder loreStrings(@NotNull List<String> lore) {
        meta.lore(lore.stream().map(ColorUtils::parse).toList());
        return this;
    }

    public @NotNull ItemBuilder flags(@NotNull ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    public @NotNull ItemBuilder glint(boolean glint) {
        meta.setEnchantmentGlintOverride(glint);
        return this;
    }

    public @NotNull ItemBuilder customModelData(int data) {
        if (data > 0) meta.setCustomModelData(data);
        return this;
    }

    public @NotNull ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public @NotNull ItemBuilder hideAll() {
        meta.addItemFlags(ItemFlag.values());
        return this;
    }

    public @NotNull ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    public static @NotNull ItemStack simple(@NotNull Material material, @Nullable String name) {
        ItemBuilder builder = new ItemBuilder(material);
        if (name != null) builder.name(name);
        return builder.build();
    }
}
