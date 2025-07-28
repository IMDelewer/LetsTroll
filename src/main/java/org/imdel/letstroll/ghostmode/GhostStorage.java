package org.imdel.letstroll.ghostmode;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GhostStorage {
    private static final Set<UUID> ghostPlayers = new HashSet<>();
    private static File file;
    private static YamlConfiguration config;

    public static void init(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "ghost-players.yml");
        if (!file.exists()) {
            plugin.saveResource("ghost-players.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        List<String> uuids = config.getStringList("players");
        for (String uuid : uuids) {
            ghostPlayers.add(UUID.fromString(uuid));
        }
    }

    public static void save() {
        List<String> uuids = ghostPlayers.stream().map(UUID::toString).toList();
        config.set("players", uuids);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isGhost(UUID uuid) {
        return ghostPlayers.contains(uuid);
    }

    public static void addGhost(UUID uuid) {
        ghostPlayers.add(uuid);
        save();
    }

    public static void removeGhost(UUID uuid) {
        ghostPlayers.remove(uuid);
        save();
    }
}
