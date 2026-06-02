/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for parsing MiniMessage formatted strings into Adventure components.
 */
public final class ColorUtils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private ColorUtils() {}

    public static @NotNull Component parse(@NotNull String text) {
        return MINI_MESSAGE.deserialize(text);
    }

    public static @NotNull Component parse(@NotNull String text, @NotNull TagResolver... resolvers) {
        return MINI_MESSAGE.deserialize(text, resolvers);
    }

    public static @NotNull String strip(@NotNull String text) {
        return MINI_MESSAGE.stripTags(text);
    }
}
