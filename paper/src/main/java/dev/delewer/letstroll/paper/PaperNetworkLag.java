package dev.delewer.letstroll.paper;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.entity.Player;

public final class PaperNetworkLag {

    private static final String HANDLER = "letstroll_lag";
    private static final long SAFE_CAP_MILLIS = 4000L;

    private final dev.ua.theroer.magicutils.logger.PrefixedLogger log;
    private final Map<UUID, Channel> channels = new ConcurrentHashMap<>();

    private final boolean available;
    private Field connectionField;
    private Field networkField;
    private Field channelField;

    public PaperNetworkLag(dev.ua.theroer.magicutils.logger.PrefixedLogger log) {
        this.log = log;
        this.available = resolve();
    }

    public boolean available() {
        return available;
    }

    private boolean resolve() {
        try {
            Class<?> serverPlayer = Class.forName("net.minecraft.server.level.ServerPlayer");
            connectionField = serverPlayer.getField("connection");

            Class<?> listener = connectionField.getType();
            networkField = findByType(listener, "net.minecraft.network.Connection");
            networkField.setAccessible(true);

            channelField = findByType(networkField.getType(), "io.netty.channel.Channel");
            channelField.setAccessible(true);
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            log.debug("Network lag unavailable: %s", exception.getMessage());
            return false;
        }
    }

    public boolean start(Player player, long delayMillis, boolean dangerous) {
        if (!available) {
            return false;
        }
        long capped = dangerous ? Math.max(0L, delayMillis) : Math.min(SAFE_CAP_MILLIS, Math.max(0L, delayMillis));
        try {
            Channel channel = channelOf(player);
            if (channel == null) {
                return false;
            }
            stop(player);
            channel.pipeline().addBefore("packet_handler", HANDLER, new Delayer(capped, dangerous));
            channels.put(player.getUniqueId(), channel);
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            log.debug("Could not lag %s: %s", player.getName(), exception.getMessage());
            return false;
        }
    }

    public void stop(Player player) {
        remove(channels.remove(player.getUniqueId()));
    }

    public void stopAll() {
        for (UUID id : Map.copyOf(channels).keySet()) {
            remove(channels.remove(id));
        }
    }

    private void remove(Channel channel) {
        if (channel == null) {
            return;
        }
        try {
            if (channel.pipeline().get(HANDLER) != null) {
                channel.pipeline().remove(HANDLER);
            }
        } catch (RuntimeException ignored) {
        }
    }

    private Channel channelOf(Player player) throws ReflectiveOperationException {
        Object handle = player.getClass().getMethod("getHandle").invoke(player);
        Object listener = connectionField.get(handle);
        Object network = networkField.get(listener);
        return (Channel) channelField.get(network);
    }

    private static Field findByType(Class<?> owner, String typeName) throws NoSuchFieldException {
        Class<?> current = owner;
        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                if (field.getType().getName().equals(typeName)) {
                    return field;
                }
            }
            current = current.getSuperclass();
        }
        throw new NoSuchFieldException(typeName);
    }

    private static final class Delayer extends ChannelDuplexHandler {

        private final long delayMillis;
        private final boolean dangerous;

        private Delayer(long delayMillis, boolean dangerous) {
            this.delayMillis = delayMillis;
            this.dangerous = dangerous;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (delayMillis <= 0L || (!dangerous && isLifeline(msg))) {
                super.write(ctx, msg, promise);
                return;
            }
            ctx.executor().schedule(() -> {
                try {
                    super.write(ctx, msg, promise);
                } catch (Exception exception) {
                    ctx.fireExceptionCaught(exception);
                }
            }, delayMillis, TimeUnit.MILLISECONDS);
        }

        private boolean isLifeline(Object msg) {
            String name = msg == null ? "" : msg.getClass().getSimpleName();
            return name.contains("KeepAlive") || name.contains("Disconnect") || name.contains("Login");
        }
    }
}
