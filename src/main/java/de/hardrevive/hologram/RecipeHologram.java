/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package de.hardrevive.hologram;

import de.hardrevive.HardRevive;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A floating display of the Revival Totem crafting recipe using Display entities.
 */
public final class RecipeHologram {

    private static final float ITEM_SCALE = 0.45f;
    private static final float SPACING    = 0.55f;
    private static final MiniMessage MM   = MiniMessage.miniMessage();

    private final List<Entity> entities = new ArrayList<>();
    private final @NotNull Location anchor;
    private final @NotNull Vector rightVec;

    public RecipeHologram(@NotNull HardRevive plugin, @NotNull Location anchor, @NotNull Vector rightVec) {
        this.anchor   = anchor.clone();
        this.rightVec = rightVec.clone().normalize();
        build(plugin);
    }

    private void build(@NotNull HardRevive plugin) {
        FileConfiguration recipeCfg = plugin.getConfigManager().getRecipesConfig();
        List<String> shape = recipeCfg.getStringList("recipe.shape");
        ConfigurationSection ingr = recipeCfg.getConfigurationSection("recipe.ingredients");

        Map<Character, Material> ingredientMap = new HashMap<>();
        if (ingr != null) {
            for (String key : ingr.getKeys(false)) {
                if (key.length() != 1) continue;
                String matName = ingr.getString(key, "BARRIER");
                try {
                    ingredientMap.put(key.charAt(0), Material.valueOf(matName.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    ingredientMap.put(key.charAt(0), Material.BARRIER);
                }
            }
        }

        // Title centered above grid (at rightVec * 0 + up * (SPACING + 0.55))
        String itemName = plugin.getConfigManager().getItemsConfig()
                .getString("revive-item.name", "<gold><bold>Revival Totem");
        spawnText(offset(0, SPACING + 0.55), MM.deserialize(itemName), 1.3f);

        // 3x3 crafting grid
        for (int r = 0; r < 3; r++) {
            String row = (r < shape.size()) ? shape.get(r) : "   ";
            for (int c = 0; c < 3; c++) {
                char ch = (c < row.length()) ? row.charAt(c) : ' ';
                Material mat = (ch == ' ')
                        ? Material.LIGHT_GRAY_STAINED_GLASS_PANE
                        : ingredientMap.getOrDefault(ch, Material.BARRIER);
                spawnItem(offset((c - 1) * SPACING, (1 - r) * SPACING), new ItemStack(mat));
            }
        }

        // Arrow between grid and result
        spawnText(offset(SPACING + 0.55, 0), Component.text("→"), 2.0f);

        // Result item
        int amount = recipeCfg.getInt("recipe.result-amount", 1);
        spawnItem(offset(SPACING + 1.15, 0), plugin.getReviveItemManager().createReviveItem(amount));
    }

    /** Returns a world location offset from the anchor along rightVec (x) and Y-up (y). */
    private @NotNull Location offset(double x, double y) {
        return anchor.clone()
                .add(rightVec.clone().multiply(x))
                .add(0, y, 0);
    }

    private void spawnItem(@NotNull Location loc, @NotNull ItemStack item) {
        entities.add(loc.getWorld().spawn(loc, ItemDisplay.class, d -> {
            d.setItemStack(item);
            d.setTransformation(new Transformation(
                    new Vector3f(), new AxisAngle4f(0, 0, 1, 0),
                    new Vector3f(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE),
                    new AxisAngle4f(0, 0, 1, 0)));
            d.setBillboard(Display.Billboard.CENTER);
            d.setPersistent(false);
        }));
    }

    private void spawnText(@NotNull Location loc, @NotNull Component text, float scale) {
        entities.add(loc.getWorld().spawn(loc, TextDisplay.class, d -> {
            d.text(text);
            d.setTransformation(new Transformation(
                    new Vector3f(), new AxisAngle4f(0, 0, 1, 0),
                    new Vector3f(scale, scale, scale),
                    new AxisAngle4f(0, 0, 1, 0)));
            d.setBillboard(Display.Billboard.CENTER);
            d.setPersistent(false);
            d.setDefaultBackground(false);
            d.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        }));
    }

    public @NotNull Location getAnchor()   { return anchor.clone(); }
    public @NotNull Vector  getRightVec()  { return rightVec.clone(); }

    public void remove() {
        entities.forEach(e -> { if (e != null && !e.isDead()) e.remove(); });
        entities.clear();
    }
}
