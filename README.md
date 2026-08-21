# LetsTroll

A trolling toolkit for Minecraft servers. Everything starts from a single `/troll` menu with tabs for players, events, bindings and settings.

The project is split into a platform independent core and thin platform adapters, so the same features can run on Paper today and on Fabric and Velocity later.

## Modules

Every feature lives in its own folder under `core/src/main/java/dev/delewer/letstroll/modules`. A folder containing a class annotated with `@TrollModule` is discovered automatically at build time and appears in the menu.

## Build

```
./gradlew build
```

## License

MIT
