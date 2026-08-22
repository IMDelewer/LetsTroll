<h1 align="center">LetsTroll</h1>

<h4 align="center">A trolling toolkit for Minecraft servers — one menu, every prank.</h4>

<p align="center">
  <a href="https://modrinth.com/plugin/lets-troll/versions"><img src="https://img.shields.io/modrinth/dt/lets-troll?style=for-the-badge&logo=modrinth&logoColor=white&color=00AF5C" alt="Downloads"></a>
  <a href="../../actions/workflows/build.yml"><img src="https://img.shields.io/github/actions/workflow/status/IMDelewer/LetsTroll/build.yml?style=for-the-badge&logo=githubactions&logoColor=white" alt="Build"></a>
  <img src="https://img.shields.io/badge/Minecraft-1.21.11-62B47A?style=for-the-badge&logo=minecraft&logoColor=white" alt="Minecraft">
  <a href="../../stargazers"><img src="https://img.shields.io/github/stars/IMDelewer/LetsTroll?style=for-the-badge&logo=github&logoColor=white" alt="Stars"></a>
</p>

---

### Grab it from [Modrinth](https://modrinth.com/plugin/lets-troll/version) or [GitHub Releases](https://github.com/IMDelewer/LetsTroll/releases/latest).

Everything starts from a single `/troll` menu with tabs for players, events, bindings and settings.

The project is split into a platform independent core and thin platform adapters. Paper and Fabric run the same features today; Velocity can follow with another adapter.

---

## ⌨️ Commands

| Command | Description |
| --- | --- |
| `/troll` | Open the menu |
| `/troll modules` | List loaded modules |
| `/troll reload` | Reload the configuration |
| `/ghost [on\|off\|<player>] [on\|off]` | Toggle ghost mode |

| Permission | Grants |
| --- | --- |
| `letstroll.use` | Open the menu and use bound items |
| `letstroll.admin` | Settings, reload, module list, admin-only screens |
| `letstroll.ghost` | Ghost mode for yourself |
| `letstroll.ghost.others` | Ghost mode for someone else |
| `letstroll.mannequin` | Spawn fake players in front of a victim |
| `letstroll.lag` | Rubberband a player with fake lag |
| `letstroll.effects` | Apply effects and scares to a player |
| `letstroll.immune` | Cannot be hit by automatic events (default: nobody) |

Everything except `letstroll.immune` defaults to operators.

## 🫥 Staying hidden

All three switches live in the `hide` section of `config.yml`; the last two are also in the settings menu:

```yaml
hide:
  plugin: true
  plugins-command: true
  commands: true
```

- `plugin` drops LetsTroll from the server plugin list itself, so it never appears in `/plugins` for anyone, including operators and other plugins. This one reaches into Paper internals through reflection, so it can stop working on a future Paper release; it turns itself off cleanly when that happens and the two filters below take over.
- `plugins-command` rewrites `/plugins`, `/pl`, `/version <name>` and their variants so LetsTroll is missing from the list for anyone without `letstroll.admin`.
- `commands` removes `/troll` and `/ghost` from tab completion and the client command tree for anyone without `letstroll.use`.

The last two use ordinary Bukkit events, so nothing breaks on a core update.

## ⚙️ Configuration

Everything the plugin owns lives in one `config.yml`, split into `menu`, `hide` and a `modules` section with one block per module. `heads.yml` holds the icon catalog; `logger.yml` and `lang/` belong to MagicUtils.

A module declares its settings as an ordinary class and picks it up with `context.config(ExampleConfig.class)`. To have it land in `config.yml` instead of its own file, add one field to `ModuleSettings`:

```java
@ConfigSection("example")
private ExampleConfig example = new ExampleConfig();
```

## 🎨 Icons

Menu icons come from a small catalog in `heads.yml`. Navigation arrows and the info button ship as real head textures; toggles, close and search use clean items. Every entry is overridable:

```yaml
icons:
  toggle-on: "base64:<your green head value>"
  next: "hash:<texture hash>"
  close: "item:minecraft:redstone"
```

Values accept `hash:<texture hash>`, `base64:<raw value>`, `item:<material>` or `head:<account name>`.

## 🧱 Menu layout

Every menu keeps a one slot frame of glass around its content and reserves the bottom row for navigation. Buttons are player heads with custom textures for arrows and toggles. The hub carries a ghost toggle in the lower right corner of the content area, menus play sounds on open and click, the player list has a search field backed by an anvil prompt, and an open menu redraws itself in place on a timer so ping, health and toggles stay current.

## 📦 Modules

Every feature lives in its own folder under `core/src/main/java/dev/delewer/letstroll/modules`. A folder containing a class annotated with `@TrollModule` is discovered automatically at build time and appears in the menu.

```java
@TrollModule(id = "example", name = "Example", order = 60)
public final class ExampleModule implements LetsTrollModule {

    @Override
    public void enable(ModuleContext context) {
        context.screen(new ExampleScreen());
        context.tab("example", 60, MenuIcon.of("minecraft:tnt"), "example");
        context.playerAction(new ExampleAction());
        context.command(new ExampleCommand());
        context.config(ExampleConfig.class);
    }
}
```

A module can register screens, hub entries, actions shown on a player, commands and its own configuration file. Screens are addressed by id through the router, so any screen can open any other one.

Hub entries are free form blocks, not only tabs: they carry any icon (including a player head with the real skin), hover text, a permission, an optional fixed slot and either a screen to open or custom code to run.

```java
context.hubEntry(HubEntry.of("example")
        .order(60)
        .icon(viewer -> MenuIcon.head(viewer.id(), viewer.name()))
        .title(viewer -> Text.get(viewer, "example.title"))
        .lore(viewer -> List.of(Text.get(viewer, "example.description")))
        .onClick(click -> click.open(ScreenRequest.of("example")))
        .build());
```

Shipped modules: `hub`, `players`, `events`, `modes`, `bindings`, `settings`, `ghost`, `effects`, `mannequin` and `lag`. `mannequin` drops a nameless fake player with a chosen skin in front of a victim while everyone else sees nothing; `modes` chains two players together so they share inventory, health and death, joined by a real sagging chain built from `minecraft:iron_chain` block displays; `lag` rubberbands a player and fakes a high ping in the tab list.

## 🔨 Build

Both platforms at once:

```
./gradlew build
```

The Paper plugin lands in `paper/build/libs`, the Fabric mod in `fabric/build/libs`. To build one of them alone:

```
./gradlew :paper:shadowJar
```

```
./gradlew :fabric:remapJar
```

A test server:

```
./gradlew :paper:runServer
```

The Paper jar is downloaded on first run. If you keep one in `.cache/`, it is used instead.

Artifacts are named `LetsTroll-<loader>-<minecraft>-<version>.jar`. The version comes from `build.gradle.kts` and can be overridden for a build with `-PbuildVersion=2.0.1`.

## 🚀 Releasing

Pushing a `v*` tag builds both loaders, publishes them to Modrinth and creates a GitHub release. Two repository settings are needed once:

- secret `MODRINTH_TOKEN` — a Modrinth PAT with `Create versions` scope.
- variable `MODRINTH_PROJECT_ID` — optional, defaults to the `lets-troll` slug.

```
git tag v2.0.1 && git push origin v2.0.1
```

Adding another loader is one entry in the matrix of `.github/workflows/release.yml` plus its Gradle module.

## 🔤 Placeholders

Message and menu text runs through PlaceholderAPI when it is installed, so `%player_ping%` and friends work inside `lang/` files and menu entries. Without PlaceholderAPI the text is left untouched.

## 🤝 Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for the build, the module system and the two config
gotchas that will bite you. Security reports go through [SECURITY.md](SECURITY.md), and the
release history lives in [CHANGELOG.md](CHANGELOG.md).

## 📄 License

MIT
