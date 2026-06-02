# HardRevive

<div align="center">


**Revive fallen players with custom craftable items, intuitive GUIs, and fully configurable death management.**

[![License: MPL-2.0](https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg)](https://opensource.org/licenses/MPL-2.0)
[![Paper 1.21+](https://img.shields.io/badge/Paper-1.21%2B-blue)](https://papermc.io)
[![Java 21](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/projects/jdk/21/)
[![Modrinth](https://img.shields.io/modrinth/dt/hardrevive?label=Modrinth&logo=modrinth)](https://modrinth.com/plugin/hardrevive)

</div>

---

## Overview

HardRevive turns death into a reversible event. When a player dies, they are automatically banned until another player uses a **Revival Totem** to bring them back. The plugin works on both Hardcore and normal Survival servers, supports unlimited revives, and is fully configurable down to the last particle effect.

---

## Features

- **Revival Totem** — craftable item with fully configurable material, name, lore, glint, and custom model data
- **Automatic ban on death** — dead players cannot rejoin until revived
- **Intuitive paginated GUI** — browse dead players with skull icons, death time, and cause
- **Search** — filter dead players by name via chat input
- **Confirmation screen** — accidental revives are prevented by a confirm/cancel dialog
- **Hardcore & Normal mode** — works on both server types
- **Broadcast system** — configurable death, ban, and revive announcements
- **Sound & particle effects** — all configurable via YAML
- **Multi-language** — English and German bundled; add your own language file
- **bStats analytics** — optional anonymous usage statistics
- **Modrinth update checker** — notifies admins of new releases on startup

---

## Requirements

| Requirement | Version |
|---|---|
| Paper (or forks) | 1.21+ |
| Java | 21+ |

---

## Installation

1. Download the latest `HardRevive-*.jar` from [Modrinth](https://modrinth.com/plugin/hardrevive) or [GitHub Releases](https://github.com/hardrevive/HardRevive/releases).
2. Drop the JAR into your server's `plugins/` folder.
3. Restart the server (not `/reload`).
4. Edit `plugins/HardRevive/config.yml` to match your server setup.
5. Restart again or run `/revive reload`.

---

## Commands

| Command | Description | Permission |
|---|---|---|
| `/revive help` | Show help menu | — |
| `/revive reload` | Reload all config files | `hardrevive.reload` |
| `/revive give <player> [amount]` | Give Revival Totems | `hardrevive.give` |
| `/revive list` | List all dead players (also opens GUI) | `hardrevive.list` |
| `/revive info <player>` | Show detailed info for a dead player | `hardrevive.info` |

Alias: `/hr`

---

## Permissions

| Permission | Description | Default |
|---|---|---|
| `hardrevive.admin` | Inherits all permissions below | OP |
| `hardrevive.reload` | Reload the plugin | OP |
| `hardrevive.give` | Give revival items | OP |
| `hardrevive.list` | List dead players | OP |
| `hardrevive.info` | View death info | OP |
| `hardrevive.update.notify` | Receive update notifications on join | OP |

---

## Configuration

### config.yml

```yaml
language: en                    # en or de (add your own messages_xx.yml)

death-system:
  mode: NORMAL                  # HARDCORE or NORMAL

broadcasts:
  player-death: true
  player-revive: true
  player-banned: true

update-checker:
  enabled: true
  notify-admins: true

bstats:
  enabled: true
```

### items.yml — Revival Totem

```yaml
revive-item:
  material: TOTEM_OF_UNDYING
  name: "<gold><bold>Revival Totem"
  lore:
    - "<gray>Use to revive a fallen player."
  glint: true
  custom-model-data: 0
  item-flags:
    - HIDE_ATTRIBUTES
    - HIDE_ENCHANTS
```

### recipes.yml — Crafting Recipe

```yaml
recipe:
  enabled: true
  shape:
    - "DED"
    - "ETE"
    - "DED"
  ingredients:
    D: DIAMOND_BLOCK
    E: ECHO_SHARD
    T: TOTEM_OF_UNDYING
  result-amount: 1
```

### gui.yml, sounds.yml, effects.yml

All GUI slots, materials, titles, sounds, and particles are individually configurable. See the generated files in `plugins/HardRevive/` for all available options.

---

## Language Files

Bundled: `messages_en.yml`, `messages_de.yml`

To add a new language:
1. Copy `messages_en.yml` to `messages_xx.yml` (replace `xx` with your ISO code).
2. Translate all values.
3. Set `language: xx` in `config.yml`.
4. Run `/revive reload`.

All messages use [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting.

---

## bStats

HardRevive collects anonymous usage data via [bStats](https://bstats.org) (plugin ID: **25000**).

Collected charts:
- Death mode in use (HARDCORE / NORMAL)
- Configured language
- Number of currently dead players
- Update checker enabled or not

You can disable collection in `config.yml`:

```yaml
bstats:
  enabled: false
```

---

## Branding

**Color Scheme:** `#8B0000` (deep red) · `#FFD700` (gold) · `#1C1C1C` (near black)

**Theme:** Hardcore · Death · Rebirth · Second Chance · Soul · Totem

**Logo concept:** A cracked totem of undying with a golden soul-flame rising from its center, set against a dark background. The name "HardRevive" is typeset in bold serif gold lettering.

**Modrinth icon concept:** 64×64 pixel art of a glowing totem with a red border gradient, matching the dark-red/gold color scheme.

---

## Contributing

Contributions are very welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

---

## License

This project is licensed under the **Mozilla Public License 2.0**.  
See [LICENSE](LICENSE) for the full license text.

> You may use and modify the source code in proprietary projects as long as modified files remain under MPL-2.0. You are **not** required to open-source the rest of your project.

---

## Links

- [Modrinth](https://modrinth.com/plugin/hardrevive)
- [GitHub Issues](https://github.com/McAffe13/HardRevive/issues)
- [bStats Dashboard](https://bstats.org/plugin/bukkit/HardRevive/31753)
