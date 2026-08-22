package dev.delewer.letstroll.paper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.TextInputService;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperTextInput implements TextInputService, Listener {

    private static final long TIMEOUT_TICKS = 20L * 60L;

    private final JavaPlugin plugin;
    private final Map<UUID, Consumer<String>> pending = new ConcurrentHashMap<>();

    public PaperTextInput(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void request(PlayerRef viewer, Component prompt, String initial, Consumer<String> onConfirm) {
        Player player = (Player) viewer.handle();
        if (player == null) {
            return;
        }
        UUID id = player.getUniqueId();
        pending.put(id, onConfirm);
        player.closeInventory();
        player.sendMessage(prompt);
        player.sendMessage(Component.text("Type it in chat, or write cancel."));
        PaperTasks.asyncLater(plugin, () -> pending.remove(id, onConfirm), TIMEOUT_TICKS);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Consumer<String> callback = pending.remove(event.getPlayer().getUniqueId());
        if (callback == null) {
            return;
        }
        event.setCancelled(true);
        String text = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        if (text.equalsIgnoreCase("cancel")) {
            return;
        }
        PaperTasks.onEntity(plugin, event.getPlayer(), () -> callback.accept(text));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pending.remove(event.getPlayer().getUniqueId());
    }
}
