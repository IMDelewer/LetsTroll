package org.imdel.letstroll.ghostmode;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import org.imdel.letstroll.LetsTroll;

public class GhostModeListener implements Listener {
    private final LetsTroll plugin;

    public GhostModeListener(LetsTroll plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (GhostStorage.isGhost(event.getPlayer().getUniqueId())) {
            event.setJoinMessage(null);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (GhostStorage.isGhost(event.getPlayer().getUniqueId())) {
            event.setQuitMessage(null);
        }
    }
}