/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a player who has died and is awaiting revival.
 */
public final class DeadPlayer {

    private final @NotNull UUID uuid;
    private final @NotNull String name;
    private final @NotNull Instant deathTime;
    private final @NotNull String deathCause;
    private int reviveCount;

    public DeadPlayer(
            @NotNull UUID uuid,
            @NotNull String name,
            @NotNull Instant deathTime,
            @NotNull String deathCause,
            int reviveCount
    ) {
        this.uuid = uuid;
        this.name = name;
        this.deathTime = deathTime;
        this.deathCause = deathCause;
        this.reviveCount = reviveCount;
    }

    public @NotNull UUID getUuid() {
        return uuid;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull Instant getDeathTime() {
        return deathTime;
    }

    public @NotNull String getDeathCause() {
        return deathCause;
    }

    public int getReviveCount() {
        return reviveCount;
    }

    public void incrementReviveCount() {
        this.reviveCount++;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DeadPlayer other)) return false;
        return uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public @NotNull String toString() {
        return "DeadPlayer{uuid=" + uuid + ", name='" + name + "', deathTime=" + deathTime + "}";
    }
}
