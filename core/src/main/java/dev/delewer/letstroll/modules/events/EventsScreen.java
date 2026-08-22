package dev.delewer.letstroll.modules.events;

import java.util.List;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.menu.Buttons;
import dev.delewer.letstroll.menu.Menu;
import dev.delewer.letstroll.menu.MenuButton;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.menu.MenuLayout;
import dev.delewer.letstroll.menu.Screen;
import dev.delewer.letstroll.platform.MenuSound;
import dev.delewer.letstroll.menu.ScreenContext;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.text.Text;

public final class EventsScreen implements Screen {

    public static final String ID = "events";

    private final EventsConfig config;
    private final EventScheduler scheduler;
    private final List<TrollEvent> events;

    public EventsScreen(EventsConfig config, EventScheduler scheduler, List<TrollEvent> events) {
        this.config = config;
        this.scheduler = scheduler;
        this.events = events;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Menu build(ScreenContext context) {
        LetsTroll core = context.core();
        PlayerRef viewer = context.viewer();
        int rows = core.config().menuRows();

        Menu.Builder builder = Menu.builder(Text.get(viewer, "hub.tab.events.title"))
                .rows(rows)
                .filler(core.filler());

        builder.button(MenuLayout.serviceSlot(rows, 1), MenuButton.of(core.heads().icon(
                        config.enabled() ? dev.delewer.letstroll.menu.Heads.TOGGLE_ON : dev.delewer.letstroll.menu.Heads.TOGGLE_OFF))
                .name(Text.get(viewer, "events.toggle"))
                .lore(Text.get(viewer, config.enabled() ? "common.toggle.on" : "common.toggle.off"))
                .glow(config.enabled())
                .sound(config.enabled() ? MenuSound.TOGGLE_OFF : MenuSound.TOGGLE_ON)
                .onClick(click -> {
                    config.setEnabled(!config.enabled());
                    save(click);
                    scheduler.start();
                    click.refresh();
                })
                .build());

        builder.button(MenuLayout.serviceSlot(rows, 3), MenuButton.of(MenuIcon.of("minecraft:clock"))
                .name(Text.get(viewer, "events.interval", trim(config.intervalMinutes())))
                .lore(Text.get(viewer, "events.interval.adjust"))
                .onClick(click -> {
                    double next = Math.floor(config.intervalMinutes()) + 1;
                    if (next > 30) {
                        next = 1;
                    }
                    config.setIntervalMinutes(next);
                    save(click);
                    scheduler.start();
                    click.refresh();
                })
                .build());

        builder.button(MenuLayout.serviceSlot(rows, 5), MenuButton.of(MenuIcon.of("minecraft:beacon"))
                .name(Text.get(viewer, "events.bossbar", config.bossBar()))
                .lore(Text.get(viewer, "events.bossbar.adjust"))
                .onClick(click -> {
                    config.setBossBar(nextBossBar(config.bossBar()));
                    save(click);
                    scheduler.start();
                    click.refresh();
                })
                .build());

        builder.button(MenuLayout.serviceSlot(rows, 7), MenuButton.of(MenuIcon.of("minecraft:tnt"))
                .name(Text.get(viewer, "events.fire-now"))
                .lore(Text.get(viewer, "events.fire-now.hint"))
                .sound(MenuSound.TOGGLE_ON)
                .onClick(click -> {
                    scheduler.fireRandom();
                    Text.send(click.viewer(), "events.fired");
                })
                .build());

        List<Integer> slots = MenuLayout.content(rows);
        for (int index = 0; index < events.size() && index < slots.size(); index++) {
            TrollEvent event = events.get(index);
            boolean on = config.isEventEnabled(event.id(), event.defaultEnabled());
            builder.button(slots.get(index), MenuButton.of(core.heads().icon(
                            on ? dev.delewer.letstroll.menu.Heads.TOGGLE_ON : dev.delewer.letstroll.menu.Heads.TOGGLE_OFF))
                    .name(Text.get(viewer, "events.entry." + event.id()))
                    .lore(Text.get(viewer, on ? "common.toggle.on" : "common.toggle.off"),
                            Text.get(viewer, event.dangerous() ? "events.entry.dangerous" : "events.entry.safe"),
                            Text.get(viewer, "events.entry.fire"))
                    .glow(on)
                    .sound(on ? MenuSound.TOGGLE_OFF : MenuSound.TOGGLE_ON)
                    .onClick(click -> {
                        if (click.kind().isRight()) {
                            scheduler.fire(event);
                            Text.send(click.viewer(), "events.fired");
                            return;
                        }
                        config.setEventEnabled(event.id(), !on);
                        save(click);
                        click.refresh();
                    })
                    .build());
        }

        return builder.button(MenuLayout.serviceSlot(rows, 4), Buttons.back(core, viewer)).build();
    }

    private void save(dev.delewer.letstroll.menu.ClickContext click) {
        click.core().saveConfig();
    }

    private String nextBossBar(String current) {
        return switch (current) {
            case "OFF" -> "ADMINS";
            case "ADMINS" -> "ALL";
            default -> "OFF";
        };
    }

    private String trim(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
