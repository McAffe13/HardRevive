# HardRevive 1.0.0 — Initial Release

🎉 **HardRevive is here!** Revive fallen players with custom craftable items, intuitive GUIs, and fully configurable death management.

---

## What's New

### Core Features
- **Revival Totem** — craftable item that brings dead players back to life
- **Automatic ban system** — dead players are banned until revived; ban is lifted automatically
- **Persistent storage** — death records, timestamps, causes, and revive history survive server restarts

### GUI
- Paginated dead-player list with skull heads, death date, cause, and time-since-death
- Chat-input search to filter by player name
- Confirmation screen to prevent accidental revives

### Configuration
- `config.yml` — language, death mode (HARDCORE/NORMAL), broadcasts, update checker, bStats
- `items.yml` — Revival Totem material, name, lore, glint, CustomModelData, ItemFlags
- `recipes.yml` — fully customizable 3×3 crafting recipe
- `gui.yml` — all GUI slots, titles, materials, lore
- `sounds.yml` — per-event sounds
- `effects.yml` — per-event particle effects
- `messages_en.yml` / `messages_de.yml` — all user-facing text in MiniMessage format

### Commands & Permissions
- `/revive reload|give|list|info|help` with tab-completion
- Full permission node hierarchy under `hardrevive.*`

### Integrations
- **bStats** — anonymous usage stats with custom charts (death mode, language, dead players, update checker)
- **Modrinth update checker** — async check on startup, notifies OPs on join

---

## Requirements

- Paper 1.21+
- Java 21+

---

## Notes

- This is the initial release. Please report any bugs on [GitHub Issues](https://github.com/hardrevive/HardRevive/issues).
- The bStats plugin ID (`25000`) is a placeholder — it will be updated after registration.
