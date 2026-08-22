package dev.delewer.letstroll.paper;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.delewer.letstroll.menu.Heads;
import dev.delewer.letstroll.platform.MojangProfile;
import dev.ua.theroer.magicutils.http.MagicHttpClient;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

public final class SkinResolver {

    private static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SESSION_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    private static final Pattern HASH = Pattern.compile("([0-9a-fA-F]{48,})");
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");

    private final MagicHttpClient http;
    private final ConcurrentHashMap<String, Optional<String>> cache = new ConcurrentHashMap<>();

    public SkinResolver(MagicHttpClient http) {
        this.http = http;
    }

    public static Optional<String> immediateTexture(String spec) {
        if (spec == null || spec.isBlank()) {
            return Optional.empty();
        }
        String trimmed = spec.trim();
        if (trimmed.startsWith("base64:")) {
            return Optional.of(trimmed.substring(7));
        }
        if (trimmed.contains("textures.minecraft.net/texture/") || trimmed.matches("[0-9a-fA-F]{48,}")) {
            Matcher matcher = HASH.matcher(trimmed);
            if (matcher.find()) {
                return Optional.of(Heads.textureFromHash(matcher.group(1)));
            }
        }
        return Optional.empty();
    }

    public static String nameFrom(String spec) {
        if (spec == null) {
            return "";
        }
        String trimmed = spec.trim();
        int marker = trimmed.indexOf("namemc.com/profile/");
        if (marker >= 0) {
            return tail(trimmed.substring(marker + "namemc.com/profile/".length()));
        }
        marker = trimmed.indexOf("namemc.com/");
        if (marker >= 0) {
            return tail(trimmed.substring(marker + "namemc.com/".length()));
        }
        return trimmed;
    }

    private static String tail(String value) {
        String cleaned = value;
        int slash = cleaned.indexOf('/');
        if (slash >= 0) {
            cleaned = cleaned.substring(0, slash);
        }
        int query = cleaned.indexOf('?');
        if (query >= 0) {
            cleaned = cleaned.substring(0, query);
        }
        return cleaned;
    }

    public Optional<String> fromProfile(PlayerProfile profile) {
        if (profile == null) {
            return Optional.empty();
        }
        return profile.getProperties().stream()
                .filter(property -> property.getName().equals("textures"))
                .map(ProfileProperty::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    public Optional<String> cached(String name) {
        if (name == null || !NAME.matcher(name).matches()) {
            return Optional.empty();
        }
        Optional<String> hit = cache.get(name.toLowerCase(Locale.ROOT));
        return hit == null ? Optional.empty() : hit;
    }

    public CompletableFuture<Optional<String>> byName(String name) {
        if (name == null || !NAME.matcher(name).matches()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        String key = name.toLowerCase(Locale.ROOT);
        Optional<String> cached = cache.get(key);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return http.getJsonSmart(PROFILE_URL + name, MojangProfile.class)
                .thenCompose(profile -> profile == null
                        ? CompletableFuture.completedFuture(Optional.<String>empty())
                        : profile.uuid()
                                .map(this::textureOf)
                                .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty())))
                .thenApply(value -> {
                    cache.put(key, value);
                    return value;
                })
                .exceptionally(error -> {
                    cache.put(key, Optional.empty());
                    return Optional.empty();
                });
    }

    private CompletableFuture<Optional<String>> textureOf(String uuid) {
        return http.getJsonSmart(SESSION_URL + uuid, MojangProfile.class)
                .thenApply(profile -> profile == null ? Optional.<String>empty() : profile.texture());
    }
}
