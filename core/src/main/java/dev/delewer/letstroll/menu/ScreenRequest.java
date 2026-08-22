package dev.delewer.letstroll.menu;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record ScreenRequest(String screenId, Map<String, String> args, int page) {

    public static ScreenRequest of(String screenId) {
        return new ScreenRequest(screenId, Map.of(), 0);
    }

    public static ScreenRequest of(String screenId, String key, String value) {
        return new ScreenRequest(screenId, Map.of(key, value), 0);
    }

    public ScreenRequest with(String key, String value) {
        Map<String, String> merged = new LinkedHashMap<>(args);
        merged.put(key, value);
        return new ScreenRequest(screenId, Map.copyOf(merged), page);
    }

    public ScreenRequest withPage(int newPage) {
        return new ScreenRequest(screenId, args, Math.max(0, newPage));
    }

    public Optional<String> arg(String key) {
        return Optional.ofNullable(args.get(key));
    }

    public Optional<UUID> uuidArg(String key) {
        return arg(key).map(value -> {
            try {
                return UUID.fromString(value);
            } catch (IllegalArgumentException exception) {
                return null;
            }
        });
    }
}
