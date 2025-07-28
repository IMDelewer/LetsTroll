# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),  
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.4] - 2025-07-29

### Added

- Support for activating troll actions by **right-clicking with specific items** in hand.
- New troll triggers: `lightning`, `fake_explosion`, `blindness`, `swap_position`, and more — all configurable per item.
- `/tool bind <action>` now links a troll effect to a right-clickable item in the player’s main hand.

### Changed

- Improved event system to support both block and air interactions with items.
- Internal refactoring of item action logic for better modularity and future expansion.

### Fixed

- Fixed rare cases where right-clicking with an item failed to trigger the bound action.
- Corrected misleading feedback message when binding an invalid action to an item.

## [1.0.3] - 2025-07-29

### Added

- Automatic upload of new plugin versions to Modrinth via GitHub Actions.
- `/tool` command actions now trigger on **right click** instead of left click.

### Changed

- Expanded logger banner for better readability and information density.
- Renamed action `fall_fake` to `fake_fall` for improved naming consistency.

### Fixed

- Improved plugin stability related to asynchronous version checks and logging.

## [1.0.2] - 2025-07-28

### Added

- Asynchronous plugin version check from GitHub using `Bukkit.getScheduler().runTaskAsynchronously`.
- Centralized logging of version check results via `Logger.startup` on the main server thread.
- Error handling for GitHub requests with warnings logged through `Logger.warn` and fallback default version message.

### Changed

- Moved version info output exclusively to `Logger.startup`.
- Removed direct calls to `Logger.startup` from `onEnable()`; version logging now depends solely on async version check results.
- Ensured thread-safe logging by scheduling log calls on Bukkit’s main thread (`runTask`).
- Optimized `onEnable()` method for cleaner and safer plugin startup.

### Fixed

- Fixed duplicated and premature version messages during plugin startup.

## [1.0.1] - 2025-07-28

### Added

- Centralized `Logger` class for colorful and structured console logging.
- Logging levels: INFO, WARN, ERROR with a stylish `[LetsTroll]` prefix.
- Fancy startup banner displayed via `Logger.startup()` on plugin enable.

### Changed

- Refactored main `LetsTroll` class:
  - Replaced direct console messages with the new `Logger`.
  - Improved `WatchService` usage for monitoring `stands.yml` changes asynchronously.
  - Proper shutdown and cancellation of all Bukkit async tasks on plugin disable.
  - Better error handling and logging in GitHub version check task.
  - Improved code readability and resource management.

### Fixed

- Fixed potential thread hanging issues in the configuration watcher.
- Prevented errors related to version fetching failures or missing data.
