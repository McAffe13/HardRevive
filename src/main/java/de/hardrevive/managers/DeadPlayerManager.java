/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.managers;

import de.hardrevive.HardRevive;
import de.hardrevive.models.DeadPlayer;
import de.hardrevive.storage.DataStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Central manager for querying and modifying the set of dead players.
 */
public final class DeadPlayerManager {

    private final @NotNull DataStorage storage;

    public DeadPlayerManager(@NotNull HardRevive plugin) {
        this.storage = plugin.getDataStorage();
    }

    public void markDead(@NotNull UUID uuid, @NotNull String name, @NotNull String cause) {
        int previousRevives = storage.getTotalRevives(uuid);
        DeadPlayer dp = new DeadPlayer(uuid, name, Instant.now(), cause, previousRevives);
        storage.addDeadPlayer(dp);
    }

    public void markRevived(@NotNull UUID uuid) {
        storage.incrementRevives(uuid);
        storage.removeDeadPlayer(uuid);
    }

    public boolean isDead(@NotNull UUID uuid) {
        return storage.isDeadPlayer(uuid);
    }

    public @Nullable DeadPlayer getDeadPlayer(@NotNull UUID uuid) {
        return storage.getDeadPlayer(uuid);
    }

    public @NotNull List<DeadPlayer> getAllDead() {
        return new ArrayList<>(storage.getAllDeadPlayers());
    }

    public @NotNull List<DeadPlayer> search(@NotNull String query) {
        String lower = query.toLowerCase();
        return storage.getAllDeadPlayers().stream()
                .filter(dp -> dp.getName().toLowerCase().contains(lower))
                .toList();
    }

    public int getTotalRevives(@NotNull UUID uuid) {
        return storage.getTotalRevives(uuid);
    }
}
