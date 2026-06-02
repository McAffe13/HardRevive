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
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * One-shot chat-input search. Opens a small instruction GUI; after the player
 * closes it the next chat message is captured as the search query and the
 * revive list is filtered accordingly.
 *
 * <p>Both the close listener and the chat listener are registered as named
 * {@link Listener} instances and unregistered immediately after use, preventing
 * memory leaks.</p>
 */
public final class SearchGUI implements InventoryHolder {

    private final @NotNull HardRevive plugin;
    private final @NotNull Player viewer;
    private final @NotNull Inventory inventory;

    /** Registered on GUI open; unregistered once the inventory closes. */
    private final @NotNull CloseListener closeListener = new CloseListener();

    public SearchGUI(@NotNull HardRevive plugin, @NotNull Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;

        String title = plugin.getLanguageManager().getRaw("gui-search-title");
        this.inventory = Bukkit.createInventory(this, 9,
                ColorUtils.parse(title.isBlank() ? "Search Player" : title));
        populate();
    }

    private void populate() {
        ItemStack paper = new ItemBuilder(Material.PAPER)
                .name("<yellow>Search")
                .loreStrings(List.of(
                        "<gray>Close this GUI and type your query in chat.",
                        "",
                        "<dark_gray>Partial names • case-insensitive"
                )).hideAll().build();
        inventory.setItem(4, paper);
    }

    public void open() {
        Bukkit.getPluginManager().registerEvents(closeListener, plugin);
        plugin.getEffectManager().playGuiSound(viewer, "gui-open");
        viewer.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }

    // -------------------------------------------------------------------------
    // Inner close listener — unregisters itself and installs the chat listener
    // -------------------------------------------------------------------------

    private final class CloseListener implements Listener {

        @EventHandler
        public void onClose(@NotNull InventoryCloseEvent event) {
            if (!event.getPlayer().equals(viewer)) return;
            if (!(event.getInventory().getHolder() instanceof SearchGUI)) return;
            HandlerList.unregisterAll(this);

            viewer.sendMessage(ColorUtils.parse("<gray>Type the player name in chat:"));

            ChatListener chatListener = new ChatListener();
            Bukkit.getPluginManager().registerEvents(chatListener, plugin);
        }
    }

    // -------------------------------------------------------------------------
    // Inner chat listener — captures one message, then unregisters itself
    // -------------------------------------------------------------------------

    private final class ChatListener implements Listener {

        @EventHandler
        public void onChat(@NotNull AsyncChatEvent event) {
            if (!event.getPlayer().getUniqueId().equals(viewer.getUniqueId())) return;
            event.setCancelled(true);
            HandlerList.unregisterAll(this);

            String query = PlainTextComponentSerializer.plainText().serialize(event.message());

            Bukkit.getScheduler().runTask(plugin, () -> {
                List<DeadPlayer> results = plugin.getDeadPlayerManager().search(query);
                new ReviveListGUI(plugin, viewer, results, 0, query).open();
            });
        }
    }
}
