/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.storage;

import de.hardrevive.models.DeadPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

/**
 * Storage abstraction for persisting dead player data.
 */
public interface DataStorage {

    void load();

    void save();

    void addDeadPlayer(@NotNull DeadPlayer player);

    void removeDeadPlayer(@NotNull UUID uuid);

    @Nullable DeadPlayer getDeadPlayer(@NotNull UUID uuid);

    boolean isDeadPlayer(@NotNull UUID uuid);

    @NotNull Collection<DeadPlayer> getAllDeadPlayers();

    int getTotalRevives(@NotNull UUID uuid);

    void incrementRevives(@NotNull UUID uuid);
}
