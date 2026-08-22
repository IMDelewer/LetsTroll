package dev.delewer.letstroll.paper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dev.delewer.letstroll.platform.StealthOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public final class NoclipListener implements Listener {

    private static final long COOLDOWN_MS = 200L;

    private final PaperStealth stealth;
    private final Map<UUID, Long> lastToggle = new ConcurrentHashMap<>();

    public NoclipListener(PaperStealth stealth) {
        this.stealth = stealth;
    }

    @EventHandler
    public void onSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        if (!noclipAllowed(player)) {
            return;
        }
        if (onCooldown(player.getUniqueId())) {
            return;
        }

        if (event.isSprinting()) {
            if (player.isFlying()) {
                stealth.enterNoclip(player);
            }
        } else {
            stealth.exitNoclip(player);
        }
    }

    @EventHandler
    public void onFlightToggle(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (!event.isFlying() && stealth.inNoclip(player.getUniqueId())) {
            stealth.exitNoclip(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastToggle.remove(event.getPlayer().getUniqueId());
    }

    private boolean noclipAllowed(Player player) {
        return stealth.optionsOf(player.getUniqueId())
                .filter(StealthOptions::noclipOnSprint)
                .isPresent();
    }

    private boolean onCooldown(UUID id) {
        long now = System.currentTimeMillis();
        Long previous = lastToggle.get(id);
        if (previous != null && now - previous < COOLDOWN_MS) {
            return true;
        }
        lastToggle.put(id, now);
        return false;
    }
}
