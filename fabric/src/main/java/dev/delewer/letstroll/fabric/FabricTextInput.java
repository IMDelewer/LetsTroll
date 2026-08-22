package dev.delewer.letstroll.fabric;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.TaskScheduler;
import dev.delewer.letstroll.platform.TextInputService;
import dev.ua.theroer.magicutils.platform.fabric.FabricAudience;
import net.kyori.adventure.text.Component;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricTextInput implements TextInputService {

    private static final long TIMEOUT_TICKS = 20L * 60L;

    private final TaskScheduler scheduler;
    private final Map<UUID, Consumer<String>> pending = new ConcurrentHashMap<>();

    public FabricTextInput(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void request(PlayerRef viewer, Component prompt, String initial, Consumer<String> onConfirm) {
        if (!(viewer.handle() instanceof ServerPlayerEntity player)) {
            return;
        }
        UUID id = viewer.id();
        pending.put(id, onConfirm);
        player.closeHandledScreen();
        FabricAudience audience = new FabricAudience(player);
        audience.send(prompt);
        audience.send(Component.text("Type it in chat, or write cancel."));
        scheduler.later(() -> pending.remove(id, onConfirm), TIMEOUT_TICKS);
    }

    public boolean consume(UUID id, String message) {
        Consumer<String> callback = pending.remove(id);
        if (callback == null) {
            return false;
        }
        String text = message.trim();
        if (!text.equalsIgnoreCase("cancel")) {
            callback.accept(text);
        }
        return true;
    }

    public void forget(UUID id) {
        pending.remove(id);
    }
}
