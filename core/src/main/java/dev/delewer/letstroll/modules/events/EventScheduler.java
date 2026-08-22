package dev.delewer.letstroll.modules.events;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.TaskScheduler;
import dev.delewer.letstroll.text.Text;
import net.kyori.adventure.text.Component;

public final class EventScheduler {

    public static final String IMMUNE_PERMISSION = "letstroll.immune";

    private final LetsTroll core;
    private final EventsConfig config;
    private final List<TrollEvent> events;

    private TaskScheduler.Cancellable ticker;
    private Object bossBar;
    private long ticksLeft;

    public EventScheduler(LetsTroll core, EventsConfig config, List<TrollEvent> events) {
        this.core = core;
        this.config = config;
        this.events = events;
    }

    public void start() {
        stop();
        if (!config.enabled()) {
            return;
        }
        ticksLeft = config.intervalTicks();
        ticker = core.platform().scheduler().repeating(this::tick, 20L);
    }

    public void stop() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
        if (bossBar != null) {
            core.platform().bossBars().hide(bossBar);
            bossBar = null;
        }
    }

    public List<TrollEvent> enabledEvents() {
        return events.stream()
                .filter(event -> config.isEventEnabled(event.id(), event.defaultEnabled()))
                .toList();
    }

    private void tick() {
        ticksLeft -= 20L;
        updateBossBar();
        if (ticksLeft > 0) {
            return;
        }
        ticksLeft = config.intervalTicks();
        fireRandom();
    }

    public void fireRandom() {
        List<TrollEvent> pool = enabledEvents();
        if (pool.isEmpty()) {
            return;
        }
        TrollEvent event = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        fire(event);
    }

    public void fire(TrollEvent event) {
        List<PlayerRef> targets = resolveTargets(event.targetMode());
        if (targets.isEmpty()) {
            return;
        }
        playReveal(event, () -> runEvent(event, targets));
    }

    private void runEvent(TrollEvent event, List<PlayerRef> targets) {
        try {
            event.run(new EventContext(core, targets));
            targets.forEach(target -> core.stats().record(target.id(), "event:" + event.id()));
        } catch (RuntimeException exception) {
            core.platform().logger().warning("Event " + event.id() + " failed: " + exception.getMessage());
        }
    }

    private void playReveal(TrollEvent event, Runnable onComplete) {
        List<PlayerRef> viewers = core.platform().players().online();
        List<TrollEvent> pool = events.isEmpty() ? List.of(event) : events;
        TaskScheduler scheduler = core.platform().scheduler();

        scheduler.later(() -> countdown(viewers, "3", "<green>"), 0L);
        scheduler.later(() -> countdown(viewers, "2", "<yellow>"), 20L);
        scheduler.later(() -> countdown(viewers, "1", "<red>"), 40L);

        long[] delays = {2L, 2L, 2L, 3L, 3L, 4L, 5L, 6L, 8L, 10L, 13L};
        long offset = 60L;
        for (long delay : delays) {
            TrollEvent slot = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
            long at = offset;
            scheduler.later(() -> rouletteFrame(viewers, slot), at);
            offset += delay;
        }

        long revealAt = offset;
        scheduler.later(() -> {
            rouletteReveal(viewers, event);
            onComplete.run();
        }, revealAt);
    }

    private void countdown(List<PlayerRef> viewers, String number, String color) {
        Component big = Text.mini(color + "<bold>" + number);
        Component sub = Text.get("events.reveal.header");
        for (PlayerRef viewer : viewers) {
            core.platform().effects().title(viewer, big, sub, 0, 20, 4);
            core.platform().effects().sound(viewer, "minecraft:block.note_block.pling", 1.0f, 1.0f);
        }
    }

    private void rouletteFrame(List<PlayerRef> viewers, TrollEvent slot) {
        Component sub = Text.get("events.reveal.header");
        for (PlayerRef viewer : viewers) {
            Component name = Text.get(viewer, "events.entry." + slot.id());
            core.platform().effects().title(viewer, name, sub, 0, 10, 0);
            core.platform().effects().sound(viewer, "minecraft:ui.button.click", 0.6f, 1.6f);
        }
    }

    private void rouletteReveal(List<PlayerRef> viewers, TrollEvent event) {
        Component sub = Text.get("events.reveal.header");
        for (PlayerRef viewer : viewers) {
            Component name = Text.get(viewer, "events.entry." + event.id())
                    .decoration(net.kyori.adventure.text.format.TextDecoration.BOLD, true);
            core.platform().effects().title(viewer, name, sub, 2, 50, 10);
            core.platform().effects().sound(viewer, "minecraft:entity.player.levelup", 1.0f, 1.0f);
        }
    }

    private List<PlayerRef> resolveTargets(TrollEvent.TargetMode mode) {
        List<PlayerRef> online = new ArrayList<>(core.platform().players().online().stream()
                .filter(player -> !player.hasPermission(IMMUNE_PERMISSION))
                .filter(player -> !player.hasPermission(LetsTroll.PERMISSION_ADMIN))
                .toList());
        if (online.isEmpty()) {
            return online;
        }
        return switch (mode) {
            case ALL -> online;
            case RANDOM_ONE -> List.of(online.get(ThreadLocalRandom.current().nextInt(online.size())));
            case RANDOM_PAIR -> {
                if (online.size() < 2) {
                    yield List.of();
                }
                java.util.Collections.shuffle(online);
                yield List.of(online.get(0), online.get(1));
            }
        };
    }

    private void updateBossBar() {
        String mode = config.bossBar();
        if (mode.equals("OFF")) {
            if (bossBar != null) {
                core.platform().bossBars().hide(bossBar);
                bossBar = null;
            }
            return;
        }
        if (bossBar == null) {
            bossBar = core.platform().bossBars().create(Component.empty(), "PURPLE");
        }
        long total = Math.max(1L, config.intervalTicks());
        float progress = Math.max(0f, Math.min(1f, (float) ticksLeft / total));
        long seconds = Math.max(0L, ticksLeft / 20L);
        Component title = Text.get("events.bar.title", seconds);
        core.platform().bossBars().update(bossBar, title, progress);

        List<PlayerRef> viewers = mode.equals("ADMINS")
                ? core.platform().players().online().stream().filter(p -> p.hasPermission(LetsTroll.PERMISSION_ADMIN)).sorted(Comparator.comparing(PlayerRef::name)).toList()
                : core.platform().players().online();
        core.platform().bossBars().viewers(bossBar, viewers);
    }
}
