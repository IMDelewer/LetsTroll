package dev.delewer.letstroll.paper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dev.delewer.letstroll.platform.PingService;
import dev.delewer.letstroll.platform.PlayerRef;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public final class PaperPing implements PingService {

    private final JavaPlugin plugin;
    private final dev.ua.theroer.magicutils.logger.PrefixedLogger log;
    private final Map<UUID, Integer> fakes = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> originals = new ConcurrentHashMap<>();

    private final boolean available;
    private Field connectionField;
    private Field latencyField;
    private Method sendMethod;
    private Constructor<?> packetConstructor;
    private Object updateLatencyAction;

    private volatile ScheduledTask task;

    public PaperPing(JavaPlugin plugin, dev.ua.theroer.magicutils.logger.PrefixedLogger log) {
        this.plugin = plugin;
        this.log = log;
        this.available = resolve();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean resolve() {
        try {
            Player any = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
            Class<?> serverPlayerClass = any != null
                    ? any.getClass().getMethod("getHandle").invoke(any).getClass()
                    : Class.forName("net.minecraft.server.level.ServerPlayer");

            connectionField = serverPlayerClass.getField("connection");

            Class<?> listener = connectionField.getType();
            latencyField = findField(listener, "latency");
            latencyField.setAccessible(true);

            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.Packet");
            sendMethod = findMethod(listener, "send", packetClass);

            Class<?> infoPacket = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
            Class<?> actionClass = Class.forName(
                    "net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action");
            updateLatencyAction = Enum.valueOf((Class) actionClass, "UPDATE_LATENCY");
            packetConstructor = infoPacket.getConstructor(EnumSet.class, java.util.Collection.class);
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            log.debug("Fake ping unavailable: %s", exception.getMessage());
            return false;
        }
    }

    @Override
    public void setFake(PlayerRef target, int milliseconds) {
        if (!available) {
            return;
        }
        Player player = (Player) target.handle();
        if (player == null) {
            return;
        }
        originals.putIfAbsent(target.id(), currentLatency(player));
        fakes.put(target.id(), Math.max(0, milliseconds));
        apply(player, Math.max(0, milliseconds));
        ensureTask();
    }

    @Override
    public void clear(PlayerRef target) {
        fakes.remove(target.id());
        Integer original = originals.remove(target.id());
        Player player = (Player) target.handle();
        if (available && player != null && original != null) {
            apply(player, original);
        }
        stopTaskIfIdle();
    }

    @Override
    public boolean isFaked(UUID id) {
        return fakes.containsKey(id);
    }

    public void forget(UUID id) {
        fakes.remove(id);
        originals.remove(id);
        stopTaskIfIdle();
    }

    @Override
    public void clearAll() {
        for (UUID id : Map.copyOf(fakes).keySet()) {
            Integer original = originals.remove(id);
            Player player = Bukkit.getPlayer(id);
            if (available && player != null && original != null) {
                apply(player, original);
            }
        }
        fakes.clear();
        originals.clear();
        stopTaskIfIdle();
    }

    private synchronized void ensureTask() {
        if (task == null) {
            task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, ignored -> pushAll(), 20L, 20L);
        }
    }

    private synchronized void stopTaskIfIdle() {
        if (fakes.isEmpty() && task != null) {
            task.cancel();
            task = null;
        }
    }

    private void pushAll() {
        for (Map.Entry<UUID, Integer> entry : fakes.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                apply(player, entry.getValue());
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void apply(Player target, int milliseconds) {
        try {
            Object handle = target.getClass().getMethod("getHandle").invoke(target);
            Object connection = connectionField.get(handle);
            latencyField.setInt(connection, milliseconds);
            EnumSet actions = EnumSet.of((Enum) updateLatencyAction);
            Object packet = packetConstructor.newInstance(actions, List.of(handle));
            broadcast(packet);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    private void broadcast(Object packet) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            try {
                Object handle = viewer.getClass().getMethod("getHandle").invoke(viewer);
                Object connection = connectionField.get(handle);
                sendMethod.invoke(connection, packet);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
    }

    private int currentLatency(Player player) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = connectionField.get(handle);
            return latencyField.getInt(connection);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return player.getPing();
        }
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static Method findMethod(Class<?> type, String name, Class<?> parameter) throws NoSuchMethodException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredMethod(name, parameter);
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchMethodException(name);
    }
}
