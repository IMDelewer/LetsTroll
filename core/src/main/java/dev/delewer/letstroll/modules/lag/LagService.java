package dev.delewer.letstroll.modules.lag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import dev.delewer.letstroll.platform.MovementService;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.Position;
import dev.delewer.letstroll.platform.TaskScheduler;
import dev.delewer.letstroll.platform.TrollPlatform;

public final class LagService {

    private static final int HISTORY_CAP = 50;
    private static final int PING_MIN_TICKS = 14;
    private static final int PING_SPREAD_TICKS = 50;
    private static final double PING_FLOOR = 0.55;
    private static final double PING_CEILING = 1.12;

    private final TrollPlatform platform;
    private final LagConfig config;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    public LagService(TrollPlatform platform, LagConfig config) {
        this.platform = platform;
        this.config = config;
    }

    public boolean isLagging(UUID id) {
        return sessions.containsKey(id);
    }

    public void start(PlayerRef target, int strength, long durationTicks) {
        stop(target.id());
        boolean held = config.holdPackets()
                && platform.holdPackets(target, config.delayMillis(), config.dangerous());
        Session session = new Session(target, Math.max(1, Math.min(10, strength)), durationTicks, held,
                config.delayMillis());
        sessions.put(target.id(), session);
        session.pushPing();
        session.task = platform.scheduler().repeating(session::tick, 1L);
        if (!sessions.containsKey(target.id())) {
            session.task.cancel();
        }
    }

    public boolean holdsPackets(UUID id) {
        Session session = sessions.get(id);
        return session != null && session.held;
    }

    public void stop(UUID id) {
        Session session = sessions.remove(id);
        if (session == null) {
            return;
        }
        if (session.task != null) {
            session.task.cancel();
        }
        if (session.held) {
            platform.releasePackets(session.target);
        }
        platform.ping().clear(session.target);
    }

    public void stopAll() {
        for (UUID id : Map.copyOf(sessions).keySet()) {
            stop(id);
        }
    }

    private final class Session {

        private final PlayerRef target;
        private final List<Position> history = new ArrayList<>();
        private final double chancePerTick;
        private final int minBack;
        private final int maxBack;
        private final boolean held;
        private final long pingBase;
        private long remaining;
        private int cooldown;
        private int pingCountdown;
        private TaskScheduler.Cancellable task;

        private Session(PlayerRef target, int strength, long durationTicks, boolean held, long pingBase) {
            this.target = target;
            this.held = held;
            this.pingBase = Math.max(1L, pingBase);
            double spikesPerSecond = (held ? 0.12 : 0.4) + (strength - 1) * (held ? 0.06 : 0.4);
            this.chancePerTick = spikesPerSecond / 20.0;
            this.minBack = 3 + strength;
            this.maxBack = 6 + strength * 3;
            this.remaining = durationTicks <= 0 ? Long.MAX_VALUE : durationTicks;
        }

        private void pushPing() {
            double factor = ThreadLocalRandom.current().nextDouble(PING_FLOOR, PING_CEILING);
            platform.ping().setFake(target, (int) Math.max(1L, Math.round(pingBase * factor)));
            pingCountdown = PING_MIN_TICKS + ThreadLocalRandom.current().nextInt(PING_SPREAD_TICKS);
        }

        private void tick() {
            MovementService movement = platform.movement();
            Position now = movement.positionOf(target).orElse(null);
            if (now == null || !target.online()) {
                stop(target.id());
                return;
            }

            history.add(0, now);
            while (history.size() > HISTORY_CAP) {
                history.remove(history.size() - 1);
            }

            if (--pingCountdown <= 0) {
                pushPing();
            }

            if (--remaining <= 0) {
                stop(target.id());
                return;
            }

            if (cooldown > 0) {
                cooldown--;
                return;
            }

            if (ThreadLocalRandom.current().nextDouble() >= chancePerTick) {
                return;
            }

            int back = ThreadLocalRandom.current().nextInt(minBack, maxBack + 1);
            back = Math.min(back, history.size() - 1);
            if (back <= 0) {
                return;
            }
            Position past = history.get(back);
            movement.teleport(target, new Position(past.world(), past.x(), past.y(), past.z(),
                    now.yaw(), now.pitch()));
            cooldown = back + ThreadLocalRandom.current().nextInt(5, 16);
        }
    }
}
