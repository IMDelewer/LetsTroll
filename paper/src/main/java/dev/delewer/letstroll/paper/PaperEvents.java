package dev.delewer.letstroll.paper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import dev.delewer.letstroll.platform.PlatformEvents;
import dev.delewer.letstroll.platform.PlayerRef;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PaperEvents implements PlatformEvents, Listener {

    private final List<Consumer<PlayerRef>> joinListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<PlayerRef>> quitListeners = new CopyOnWriteArrayList<>();

    @Override
    public void onJoin(Consumer<PlayerRef> listener) {
        joinListeners.add(listener);
    }

    @Override
    public void onQuit(Consumer<PlayerRef> listener) {
        quitListeners.add(listener);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleJoin(PlayerJoinEvent event) {
        PlayerRef player = new PaperPlayerRef(event.getPlayer());
        joinListeners.forEach(listener -> listener.accept(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleQuit(PlayerQuitEvent event) {
        PlayerRef player = new PaperPlayerRef(event.getPlayer());
        quitListeners.forEach(listener -> listener.accept(player));
    }

    public void clear() {
        joinListeners.clear();
        quitListeners.clear();
    }
}
