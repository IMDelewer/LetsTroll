package dev.delewer.letstroll.modules.ghost;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.TrollPlatform;

public final class GhostService {

    private final TrollPlatform platform;
    private final GhostConfig config;
    private final Set<UUID> ghosts = ConcurrentHashMap.newKeySet();
    private final Path storage;

    public GhostService(TrollPlatform platform, GhostConfig config) {
        this.platform = platform;
        this.config = config;
        this.storage = platform.dataFolder().resolve("data").resolve("ghosts.txt");
    }

    public void load() {
        if (!config.persist() || !Files.exists(storage)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(storage, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                try {
                    ghosts.add(UUID.fromString(trimmed));
                } catch (IllegalArgumentException ignored) {
                    platform.logger().warning("Skipping invalid ghost entry: " + trimmed);
                }
            }
        } catch (IOException exception) {
            platform.logger().log(Level.WARNING, "Unable to read ghost storage", exception);
        }
    }

    public void registerIntents() {
        for (UUID id : ghosts) {
            PlayerRef player = platform.players().byId(id).orElse(null);
            if (player != null) {
                platform.stealth().hide(player, config.options());
            } else {
                platform.stealth().intend(id, config.options());
            }
        }
    }

    public boolean isGhost(UUID id) {
        return ghosts.contains(id);
    }

    public boolean toggle(PlayerRef player) {
        if (isGhost(player.id())) {
            disable(player);
            return false;
        }
        enable(player);
        return true;
    }

    public void enable(PlayerRef player) {
        ghosts.add(player.id());
        platform.stealth().hide(player, config.options());
        save();
    }

    public void disable(PlayerRef player) {
        ghosts.remove(player.id());
        platform.stealth().reveal(player);
        save();
    }

    public boolean isConfigured(UUID id) {
        return ghosts.contains(id);
    }

    public void revealEverything() {
        for (UUID id : Set.copyOf(ghosts)) {
            platform.players().byId(id).ifPresent(platform.stealth()::reveal);
        }
    }

    public Set<UUID> ghosts() {
        return new LinkedHashSet<>(ghosts);
    }

    private void save() {
        if (!config.persist()) {
            return;
        }
        List<String> lines = ghosts.stream().map(UUID::toString).toList();
        platform.scheduler().async(() -> write(lines));
    }

    private synchronized void write(List<String> lines) {
        try {
            Files.createDirectories(storage.getParent());
            Files.write(storage, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            platform.logger().log(Level.WARNING, "Unable to write ghost storage", exception);
        }
    }
}
