package dev.delewer.letstroll.menu;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HeadCatalog {

    private final Map<String, MenuIcon> icons = new LinkedHashMap<>();

    public HeadCatalog(Map<String, String> overrides) {
        Map<String, String> merged = new LinkedHashMap<>(Heads.defaults());
        if (overrides != null) {
            overrides.forEach((key, value) -> {
                if (value != null && !value.isBlank()) {
                    merged.put(key, value);
                }
            });
        }
        merged.forEach((key, spec) -> icons.put(key, Heads.resolve(spec, MenuIcon.of("minecraft:paper"))));
    }

    public MenuIcon icon(String key) {
        return icons.getOrDefault(key, MenuIcon.of("minecraft:paper"));
    }
}
