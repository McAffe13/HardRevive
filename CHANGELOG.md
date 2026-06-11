# Changelog


---

## [1.1.0] — 2026-06-11

### Added

- `/recipe` command (alias `/crafting`) — spawns a floating 3-D hologram of the Revival Totem crafting recipe using Display entities
  - Admins (`hardrevive.recipe.admin`): permanent hologram, toggle off by running the command again; rebuilt automatically on `/revive reload`
  - Players (`hardrevive.recipe`): temporary hologram that disappears after 60 seconds, when moving more than 6 blocks away, or when sneaking
- New permission nodes: `hardrevive.recipe` (default: true), `hardrevive.recipe.admin` (default: op, child of `hardrevive.admin`)
- New language keys: `recipe-disabled`, `recipe-hologram-spawned`, `recipe-hologram-removed`, `recipe-hologram-user` (all 8 bundled languages updated)

---

## [1.0.0] — 2026-06-02

### Added

- Revival Totem item with fully configurable material, name, lore, glint, CustomModelData, and ItemFlags
- Configurable shaped crafting recipe for the Revival Totem (`recipes.yml`)
- Automatic player ban on death; ban is lifted on revival
- Persistent storage of dead players, death time, death cause, and revive history (`data.yml`)
- Paginated revive list GUI with player skull, name, death date, cause, and time-since-death
- Chat-input search: filter dead players by partial name (case-insensitive)
- Confirmation GUI before committing a revival
- Support for both HARDCORE and NORMAL death modes
- Configurable broadcasts for death, ban, and revive events
- Full multi-language support (`messages_en.yml`, `messages_de.yml`)
- All sounds configurable via `sounds.yml`
- All particle effects configurable via `effects.yml`
- GUI fully configurable via `gui.yml` (titles, slots, materials, lore)
- Commands: `/revive reload|give|list|info|help` with tab-completion
- Permission nodes: `hardrevive.admin`, `.reload`, `.give`, `.list`, `.info`, `.update.notify`
- bStats integration with custom charts (death mode, language, dead-player count, update-checker state)
- Modrinth update checker — async check on startup, notifies admins with `hardrevive.update.notify`
- MPL-2.0 license

---

<!-- Add future versions above this line -->
