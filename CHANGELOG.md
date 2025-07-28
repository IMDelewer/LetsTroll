# Changelog

## [1.0.2] - 2025-07-28

### Added

* Asynchronous plugin version check from GitHub using `Bukkit.getScheduler().runTaskAsynchronously`.
* Centralized logging of version check results via `Logger.startup` on the main server thread.
* Error handling for GitHub requests with warnings logged through `Logger.warn` and fallback default version message.

### Changed

* Moved version info output exclusively to `Logger.startup`.
* Removed direct calls to `Logger.startup` from `onEnable()`, so version logging depends solely on async version check results.
* Ensured thread-safe logging by scheduling log calls on Bukkit’s main thread (`runTask`).
* Optimized `onEnable()` method for cleaner and safer plugin startup.

### Fixed

* Fixed duplicated and premature version messages during plugin startup.

## [1.0.1] - 2025-07-28

### Added

* New centralized `Logger` class for colorful and structured console logging.
* Logging levels: INFO, WARN, ERROR with a stylish `[LetsTroll]` prefix.
* Fancy startup banner displayed via `Logger.startup()` on plugin enable.

### Changed

* Refactored main `LetsTroll` class:

    * Replaced direct console messages with the new `Logger`.
    * Improved `WatchService` usage for monitoring `stands.yml` changes asynchronously.
    * Proper shutdown and cancellation of all Bukkit async tasks on plugin disable.
    * Better error handling and logging in GitHub version check task.
    * Improved code readability and resource management.

### Fixed

* Fixed potential thread hanging issues in the configuration watcher.
* Prevented errors related to version fetching failures or missing data.