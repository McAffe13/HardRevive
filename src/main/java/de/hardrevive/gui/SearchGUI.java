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
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Anvil-style search GUI implemented using a plain inventory with a paper icon.
 * Because AnvilGUI requires a third-party library, this provides equivalent
 * functionality using a sign/chat-based input via a book + sign prompt approach,
 * but in practice we implement a simple chat-input search here for compatibility.
 *
 * The player sees an instruction item and their next chat message is used as query.
 */
public final class SearchGUI implements InventoryHolder, Listener {

    private final @NotNull HardRevive plugin;
    private final @NotNull Player viewer;
    private final @NotNull Inventory inventory;
    private boolean waitingForInput = false;

    public SearchGUI(@NotNull HardRevive plugin, @NotNull Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;

        String title = plugin.getLanguageManager().getRaw("gui-search-title");
        this.inventory = Bukkit.createInventory(this, 9, ColorUtils.parse(title.isBlank() ? "Search Player" : title));
        populate();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void populate() {
        String placeholder = plugin.getLanguageManager().getRaw("gui-search-placeholder");
        ItemStack paper = new ItemBuilder(Material.PAPER)
                .name("<yellow>Search")
                .loreStrings(List.of(
                        "<gray>Type your search query in chat",
                        "<gray>after closing this GUI.",
                        "",
                        "<dark_gray>Partial names supported."
                )).hideAll().build();
        inventory.setItem(4, paper);
    }

    public void open() {
        viewer.openInventory(inventory);
        waitingForInput = true;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!e.getPlayer().equals(viewer)) return;
        if (!(e.getInventory().getHolder() instanceof SearchGUI)) return;
        HandlerList.unregisterAll(this);

        if (waitingForInput) {
            viewer.sendMessage(ColorUtils.parse("<gray>Type the player name in chat:"));
            // Register chat listener for one message
            Listener chatListener = new Listener() {};
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onChat(io.papermc.paper.event.player.AsyncChatEvent event) {
                    if (!event.getPlayer().equals(viewer)) return;
                    event.setCancelled(true);
                    HandlerList.unregisterAll(this);

                    String query = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                            .plainText().serialize(event.message());

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        List<DeadPlayer> results = plugin.getDeadPlayerManager().search(query);
                        new ReviveListGUI(plugin, viewer, results, 0, query).open();
                    });
                }
            }, plugin);
        }
    }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }
}
