package dev.delewer.letstroll.paper;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import dev.delewer.letstroll.LetsTroll;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginHiderListener implements Listener {

    private static final List<String> PLUGIN_COMMANDS = List.of(
            "plugins", "pl", "bukkit:plugins", "bukkit:pl", "paper:plugins", "spigot:plugins");
    private static final List<String> VERSION_COMMANDS = List.of(
            "version", "ver", "about", "icanhasbukkit", "bukkit:version", "bukkit:ver", "bukkit:about", "paper:version");

    private final JavaPlugin plugin;
    private final Supplier<LetsTroll> core;

    public PluginHiderListener(JavaPlugin plugin, Supplier<LetsTroll> core) {
        this.plugin = plugin;
        this.core = core;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        LetsTroll instance = core.get();
        if (instance == null || !instance.config().hideFromPlugins()) {
            return;
        }
        Player player = event.getPlayer();
        if (player.hasPermission(LetsTroll.PERMISSION_ADMIN)) {
            return;
        }

        String message = event.getMessage().substring(1).trim();
        String[] parts = message.split("\\s+");
        String label = parts[0].toLowerCase(Locale.ROOT);

        if (PLUGIN_COMMANDS.contains(label)) {
            event.setCancelled(true);
            player.sendMessage(pluginListWithout(plugin.getName()));
            return;
        }

        if (VERSION_COMMANDS.contains(label) && parts.length > 1
                && parts[1].equalsIgnoreCase(plugin.getName())) {
            event.setCancelled(true);
            player.sendMessage(Component.text("This server is running Paper version "
                            + Bukkit.getMinecraftVersion() + ".", NamedTextColor.WHITE));
        }
    }

    private Component pluginListWithout(String hidden) {
        List<Plugin> plugins = List.of(Bukkit.getPluginManager().getPlugins());
        List<Component> names = plugins.stream()
                .filter(other -> !other.getName().equalsIgnoreCase(hidden))
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(this::coloredName)
                .collect(Collectors.toList());

        Component result = Component.text("Server Plugins (" + names.size() + "): ", NamedTextColor.WHITE);
        for (int index = 0; index < names.size(); index++) {
            if (index > 0) {
                result = result.append(Component.text(", ", NamedTextColor.WHITE));
            }
            result = result.append(names.get(index));
        }
        return result;
    }

    private Component coloredName(Plugin other) {
        NamedTextColor color = other.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED;
        return Component.text(other.getName(), color);
    }
}
