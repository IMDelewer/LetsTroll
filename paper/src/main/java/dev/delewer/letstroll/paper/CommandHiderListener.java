package dev.delewer.letstroll.paper;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import dev.delewer.letstroll.LetsTroll;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

public final class CommandHiderListener implements Listener {

    private static final List<String> HIDDEN = List.of("troll", "ghost");

    private final Supplier<LetsTroll> core;

    public CommandHiderListener(Supplier<LetsTroll> core) {
        this.core = core;
    }

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        LetsTroll instance = core.get();
        if (instance == null || !instance.config().hideCommands()) {
            return;
        }
        if (event.getPlayer().hasPermission(LetsTroll.PERMISSION_USE)) {
            return;
        }
        event.getCommands().removeIf(this::isHidden);
    }

    private boolean isHidden(String command) {
        String label = command.toLowerCase(Locale.ROOT);
        int colon = label.indexOf(':');
        if (colon >= 0) {
            label = label.substring(colon + 1);
        }
        return HIDDEN.contains(label);
    }
}
