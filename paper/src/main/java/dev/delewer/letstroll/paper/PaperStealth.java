package dev.delewer.letstroll.paper;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.StealthOptions;
import dev.delewer.letstroll.platform.StealthService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperStealth implements StealthService {

    private record Snapshot(GameMode mode, boolean invulnerable, boolean silent, boolean collidable,
                            boolean allowFlight, boolean flying, boolean managedGameMode) {
    }

    private final JavaPlugin plugin;
    private final Map<UUID, StealthOptions> active = new ConcurrentHashMap<>();
    private final Map<UUID, Snapshot> snapshots = new ConcurrentHashMap<>();

    public PaperStealth(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void hide(PlayerRef player, StealthOptions options) {
        Player bukkit = (Player) player.handle();
        if (bukkit == null) {
            return;
        }
        snapshots.putIfAbsent(bukkit.getUniqueId(), snapshot(bukkit, options.creative()));
        active.put(bukkit.getUniqueId(), options);

        if (options.creative()) {
            bukkit.setGameMode(GameMode.CREATIVE);
            bukkit.setAllowFlight(true);
        }
        if (options.hideEntity()) {
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.getUniqueId().equals(bukkit.getUniqueId())) {
                    other.hidePlayer(plugin, bukkit);
                }
            }
            bukkit.setSilent(true);
        }
        if (options.invulnerable()) {
            bukkit.setInvulnerable(true);
        }
        if (options.noHunger()) {
            bukkit.setFoodLevel(20);
            bukkit.setSaturation(20.0f);
        }
        if (options.ignoreWorld()) {
            bukkit.setCollidable(false);
        }
        if (options.mobsIgnore()) {
            clearTargets(bukkit);
        }
    }

    @Override
    public void reveal(PlayerRef player) {
        Player bukkit = (Player) player.handle();
        UUID id = player.id();
        active.remove(id);
        noclipReturn.remove(id);
        Snapshot snapshot = snapshots.remove(id);
        if (bukkit == null) {
            return;
        }
        for (Player other : Bukkit.getOnlinePlayers()) {
            other.showPlayer(plugin, bukkit);
        }
        if (snapshot == null) {
            bukkit.setInvulnerable(false);
            bukkit.setSilent(false);
            bukkit.setCollidable(true);
            return;
        }
        bukkit.setInvulnerable(snapshot.invulnerable());
        bukkit.setSilent(snapshot.silent());
        bukkit.setCollidable(snapshot.collidable());
        if (snapshot.managedGameMode()) {
            bukkit.setGameMode(snapshot.mode());
            bukkit.setAllowFlight(snapshot.allowFlight());
            bukkit.setFlying(snapshot.flying() && bukkit.getAllowFlight());
        }
    }

    @Override
    public void intend(UUID id, StealthOptions options) {
        active.put(id, options);
    }

    public void handleQuit(UUID id) {
        snapshots.remove(id);
        noclipReturn.remove(id);
    }

    @Override
    public boolean hidden(UUID id) {
        return active.containsKey(id);
    }

    @Override
    public Set<UUID> hiddenPlayers() {
        return Set.copyOf(active.keySet());
    }

    public Optional<StealthOptions> optionsOf(UUID id) {
        return Optional.ofNullable(active.get(id));
    }

    private final Map<UUID, GameMode> noclipReturn = new ConcurrentHashMap<>();

    public boolean inNoclip(UUID id) {
        return noclipReturn.containsKey(id);
    }

    public void enterNoclip(Player player) {
        if (noclipReturn.containsKey(player.getUniqueId()) || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        noclipReturn.put(player.getUniqueId(), player.getGameMode());
        player.setGameMode(GameMode.SPECTATOR);
    }

    public void exitNoclip(Player player) {
        GameMode previous = noclipReturn.remove(player.getUniqueId());
        if (previous != null && player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(previous);
        }
    }

    public void applyExistingTo(Player viewer) {
        for (UUID id : active.keySet()) {
            Player hiddenPlayer = Bukkit.getPlayer(id);
            if (hiddenPlayer != null && !hiddenPlayer.getUniqueId().equals(viewer.getUniqueId())) {
                viewer.hidePlayer(plugin, hiddenPlayer);
            }
        }
    }

    public void forget(UUID id) {
        active.remove(id);
        snapshots.remove(id);
        noclipReturn.remove(id);
    }

    private void clearTargets(Player player) {
        for (Mob mob : player.getWorld().getEntitiesByClass(Mob.class)) {
            if (player.equals(mob.getTarget())) {
                mob.setTarget(null);
            }
        }
    }

    private Snapshot snapshot(Player player, boolean managedGameMode) {
        return new Snapshot(player.getGameMode(), player.isInvulnerable(), player.isSilent(),
                player.isCollidable(), player.getAllowFlight(), player.isFlying(), managedGameMode);
    }
}
