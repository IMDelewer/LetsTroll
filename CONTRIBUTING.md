# Contributing

Thanks for taking the time. This project is small, so the rules are short.

## Building

Java 21 and nothing else — the Gradle wrapper pulls the rest.

```
./gradlew build
```

That compiles both loaders and runs the tests. A green build is the bar for any pull
request. To try changes on a real server:

```
./gradlew :paper:runServer
```

The Paper jar is downloaded on first run. If you keep one in `.cache/`, it is used instead.

## Project layout

| Module | What lives there |
| --- | --- |
| `core` | Everything platform independent: modules, menus, config, text |
| `paper` | Bukkit/Paper adapter |
| `fabric` | Fabric adapter |
| `processor` | Annotation processor that indexes `@TrollModule` classes at build time |

`core` must never import Bukkit or Minecraft classes. Anything platform specific goes
behind an interface in `core/.../platform` and is implemented in both adapters.

## Adding a module

Create a folder under `core/src/main/java/dev/delewer/letstroll/modules/` with a class
annotated `@TrollModule`. The processor finds it at build time and it appears in the menu —
no registration list to edit.

```java
@TrollModule(id = "example", name = "Example", order = 60)
public final class ExampleModule implements LetsTrollModule {

    @Override
    public void enable(ModuleContext context) {
        ExampleConfig config = context.config(ExampleConfig.class);
        context.screen(new ExampleScreen(config));
        context.playerAction(new ExampleAction());
    }
}
```

For its settings to land in the shared `config.yml` rather than a file of its own, add one
field to `ModuleSettings`:

```java
@ConfigSection("example")
private ExampleConfig example = new ExampleConfig();
```

Two things that will bite you if you skip them:

- Config sections are **object fields**. A dotted path like `@ConfigValue("example.enabled")`
  silently produces an empty section and drops the value.
- Two classes cannot both declare `@ConfigFile("config.{ext}")`. One of them will win and
  the other will vanish.

## Style

- Match the file you are editing. Four spaces, UTF-8, LF — `.editorconfig` covers it.
- No comments unless the behaviour is genuinely non-obvious; the code should read plainly.
- User facing strings go through `Text.get(viewer, "key")` and into `Lang`, never inline.
- Handle errors deliberately. An empty catch block needs a reason to exist.

## Tests

Tests live in `core/src/test` and run against `FakePlatform`, so they need no server.
Anything with real logic — navigation, config behaviour, service state — should get one.

## Commits

Conventional Commits, no body needed:

```
fix(chain): keep links when a player rejoins
feat(menu): add a search field to the player list
```
