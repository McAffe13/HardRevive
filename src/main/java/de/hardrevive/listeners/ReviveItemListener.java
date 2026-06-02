/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.listeners;

import de.hardrevive.HardRevive;
import de.hardrevive.gui.ReviveListGUI;
import de.hardrevive.models.DeadPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Opens the revive list GUI when a player right-clicks with a Revival Totem.
 */
public final class ReviveItemListener implements Listener {

    private final @NotNull HardRevive plugin;

    public ReviveItemListener(@NotNull HardRevive plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        if (item == null) return;
        if (!plugin.getReviveItemManager().isReviveItem(item)) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        List<DeadPlayer> dead = plugin.getDeadPlayerManager().getAllDead();
        new ReviveListGUI(plugin, player, dead, 0, null).open();
    }
}
