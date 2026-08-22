package dev.delewer.letstroll.paper;

import java.util.UUID;

import dev.delewer.letstroll.platform.PlayerRef;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PaperPlayerRef implements PlayerRef {

    private final UUID id;
    private final String name;

    public PaperPlayerRef(Player player) {
        this.id = player.getUniqueId();
        this.name = player.getName();
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean online() {
        return player() != null;
    }

    @Override
    public boolean hasPermission(String node) {
        Player player = player();
        return player != null && player.hasPermission(node);
    }

    @Override
    public void send(Component message) {
        Player player = player();
        if (player != null) {
            player.sendMessage(message);
        }
    }

    @Override
    public String world() {
        Player player = player();
        return player == null ? "-" : player.getWorld().getName();
    }

    @Override
    public int ping() {
        Player player = player();
        return player == null ? 0 : player.getPing();
    }

    @Override
    public double health() {
        Player player = player();
        return player == null ? 0 : player.getHealth();
    }

    @Override
    public Object handle() {
        return player();
    }

    public Player player() {
        return Bukkit.getPlayer(id);
    }
}
