# Changelog

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.0]

A full rewrite. The plugin is now a platform independent core with thin Paper and Fabric
adapters, and everything is driven from a single `/troll` menu instead of `/tool` subcommands.

### Added

- Fabric support alongside Paper, sharing the same feature set from one core.
- Module system: a folder with a `@TrollModule` class is discovered at build time by an
  annotation processor and appears in the menu on its own.
- `modes` module — chain two players together so they share inventory, health, hunger and
  death, joined by a real sagging chain built from `minecraft:iron_chain` block displays.
  Three link modes (rigid, elastic, rubber), momentum transfer so a running player drags
  the other one, and a configurable link block, thickness and length.
- `mannequin` module — drop a nameless fake player with a chosen skin in front of a victim
  while everyone else sees nothing.
- `lag` module — rubberband a player and fake a high ping in the tab list.
- `effects`, `events`, `ghost`, `bindings`, `players`, `settings` and `hub` modules.
- Three ways to stay hidden, gathered under `hide` in the config: dropping the plugin from
  the server plugin list outright, rewriting `/plugins` and `/version`, and removing the
  commands from tab completion.
- PlaceholderAPI support in menu and message text, so `%player_ping%` and friends resolve.
- Publishing to Modrinth for both loaders from a `v*` tag, plus a GitHub release.

### Changed

- All plugin settings live in one `config.yml` with `menu`, `hide` and `modules` sections,
  down from eleven separate files.
- Menu icons are configurable through `heads.yml`, accepting texture hashes, base64 values,
  materials or account names.
- Logging moved to the MagicUtils logger, with per-subsystem toggles in `logger.yml`.
- Skin resolution uses a typed HTTP client with retries instead of hand-rolled requests
  and regex JSON parsing.
- Artifacts are named `LetsTroll-<loader>-<minecraft>-<version>.jar`.

### Fixed

- Player names were interpolated into the Mojang API URL unvalidated, which could crash a
  menu click and let crafted input reach other API paths.
- Spawning a mannequin while looking straight up or down produced NaN coordinates.
- The `face-player` setting was ignored on Paper.
- Deleting the last mannequin preset silently restored all the defaults.
- Binding a troll to an item wiped that item's existing lore, and unbinding erased it.
- Chat prompts never expired, so an ignored prompt swallowed the player's next message
  whenever it came.
- The native-hide shutdown hook leaked the plugin classloader across reloads.
- Server list ping filtering no longer uses an API deprecated for removal.

## [1.0.4] - 2025-07-29

- Added new `/tool bind stun` action: applies blindness and slowness to target for 3 seconds.
- Added new `/tool bind creeper` action: spawns a fake creeper that disappears after 0.25 seconds.
- Updated command parser to recognize new actions.
- Improved player targeting accuracy and feedback messages for better UX.

## [1.0.3] - 2025-07-29

- Added automatic upload of new plugin versions to Modrinth via GitHub Actions.
- Changed `/tool` command actions to trigger on right click instead of left click.
- Expanded logger banner for improved readability and information.
- Renamed action `fall_fake` to `fake_fall` for better naming consistency.
- Improved plugin stability with async version checks and logging fixes.

## [1.0.2] - 2025-07-29

- Integrated Logger to handle version checking with stylized output.
- Automatically determines if the plugin is up-to-date based on GitHub releases.
- Displays Minecraft version and latest commit info in the startup banner.
- Added real-time `stands.yml` file watcher with reload and fancy console messages.
- Refactored and optimized main plugin structure.
- Minor cleanup and formatting adjustments.
