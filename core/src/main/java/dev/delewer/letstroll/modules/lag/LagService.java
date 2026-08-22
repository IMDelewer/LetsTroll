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

    private final TrollPlatform platform;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    public LagService(TrollPlatform platform) {
        this.platform = platform;
    }

    public boolean isLagging(UUID id) {
        return sessions.containsKey(id);
    }

    public void start(PlayerRef target, int strength, long durationTicks) {
        stop(target.id());
        Session session = new Session(target, Math.max(1, Math.min(10, strength)), durationTicks);
        session.task = platform.scheduler().repeating(session::tick, 1L);
        sessions.put(target.id(), session);
    }

    public void stop(UUID id) {
        Session session = sessions.remove(id);
        if (session != null && session.task != null) {
            session.task.cancel();
        }
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
        private long remaining;
        private int cooldown;
        private TaskScheduler.Cancellable task;

        private Session(PlayerRef target, int strength, long durationTicks) {
            this.target = target;
            double spikesPerSecond = 0.4 + (strength - 1) * 0.4;
            this.chancePerTick = spikesPerSecond / 20.0;
            this.minBack = 3 + strength;
            this.maxBack = 6 + strength * 3;
            this.remaining = durationTicks <= 0 ? Long.MAX_VALUE : durationTicks;
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
            movement.teleport(target, history.get(back));
            cooldown = back + ThreadLocalRandom.current().nextInt(5, 16);
        }
    }
}
