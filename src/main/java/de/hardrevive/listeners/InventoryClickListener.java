/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.listeners;

import de.hardrevive.HardRevive;
import de.hardrevive.gui.ConfirmGUI;
import de.hardrevive.gui.ReviveListGUI;
import de.hardrevive.gui.SearchGUI;
import de.hardrevive.models.DeadPlayer;
import de.hardrevive.services.ReviveService;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Routes clicks within HardRevive GUIs to the appropriate action.
 */
public final class InventoryClickListener implements Listener {

    private final @NotNull HardRevive plugin;
    private final @NotNull ReviveService reviveService;

    public InventoryClickListener(@NotNull HardRevive plugin) {
        this.plugin = plugin;
        this.reviveService = plugin.getReviveService();
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof ReviveListGUI gui) {
            event.setCancelled(true);
            handleReviveList(event, player, gui);
        } else if (holder instanceof ConfirmGUI gui) {
            event.setCancelled(true);
            handleConfirm(event, player, gui);
        } else if (holder instanceof SearchGUI) {
            event.setCancelled(true);
        }
    }

    private void handleReviveList(
            @NotNull InventoryClickEvent event,
            @NotNull Player player,
            @NotNull ReviveListGUI gui
    ) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        FileConfiguration cfg = plugin.getConfigManager().getGuiConfig();
        int slot = event.getRawSlot();

        int prevSlot = cfg.getInt("revive-list.previous-page.slot", 45);
        int nextSlot = cfg.getInt("revive-list.next-page.slot", 53);
        int searchSlot = cfg.getInt("revive-list.search-button.slot", 48);

        if (slot == searchSlot) {
            plugin.getEffectManager().playGuiSound(player, "gui-click");
            player.closeInventory();
            new SearchGUI(plugin, player).open();
            return;
        }

        if (slot == prevSlot && gui.getPage() > 0) {
            plugin.getEffectManager().playGuiSound(player, "gui-click");
            player.closeInventory();
            new ReviveListGUI(plugin, player, gui.getPlayers(), gui.getPage() - 1, gui.getSearchQuery()).open();
            return;
        }

        if (slot == nextSlot) {
            plugin.getEffectManager().playGuiSound(player, "gui-click");
            player.closeInventory();
            new ReviveListGUI(plugin, player, gui.getPlayers(), gui.getPage() + 1, gui.getSearchQuery()).open();
            return;
        }

        // Dead player head
        UUID targetUuid = ReviveListGUI.getDeadPlayerUuid(clicked);
        if (targetUuid != null) {
            DeadPlayer dead = plugin.getDeadPlayerManager().getDeadPlayer(targetUuid);
            if (dead != null) {
                plugin.getEffectManager().playGuiSound(player, "gui-click");
                player.closeInventory();
                new ConfirmGUI(plugin, player, dead, gui.getPage(), gui.getSearchQuery()).open();
            }
        }
    }

    private void handleConfirm(
            @NotNull InventoryClickEvent event,
            @NotNull Player player,
            @NotNull ConfirmGUI gui
    ) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        FileConfiguration cfg = plugin.getConfigManager().getGuiConfig();
        int slot = event.getRawSlot();
        int confirmSlot = cfg.getInt("confirm.confirm-button.slot", 11);
        int cancelSlot  = cfg.getInt("confirm.cancel-button.slot", 15);

        if (slot == confirmSlot) {
            plugin.getEffectManager().playGuiSound(player, "gui-confirm");
            player.closeInventory();
            reviveService.revive(player, gui.getTarget().getUuid());
        } else if (slot == cancelSlot) {
            plugin.getEffectManager().playGuiSound(player, "gui-cancel");
            player.closeInventory();
            String query = gui.getSearchQuery();
            java.util.List<DeadPlayer> players = query != null
                    ? plugin.getDeadPlayerManager().search(query)
                    : plugin.getDeadPlayerManager().getAllDead();
            new ReviveListGUI(plugin, player, players, gui.getListPage(), query).open();
        }
    }
}
