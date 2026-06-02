/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.utils;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for human-readable time formatting.
 */
public final class TimeUtils {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private TimeUtils() {}

    public static @NotNull String formatDate(@NotNull Instant instant) {
        return DATE_FORMATTER.format(instant);
    }

    public static @NotNull String formatTimeAgo(@NotNull Instant instant) {
        long seconds = ChronoUnit.SECONDS.between(instant, Instant.now());

        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h " + (minutes % 60) + "m";
        long days = hours / 24;
        if (days < 7) return days + "d " + (hours % 24) + "h";
        long weeks = days / 7;
        return weeks + "w " + (days % 7) + "d";
    }
}
