package dev.delewer.letstroll.modules.events;

import java.util.List;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.hub.HubEntry;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.module.LetsTrollModule;
import dev.delewer.letstroll.module.ModuleContext;
import dev.delewer.letstroll.module.TrollModule;
import dev.delewer.letstroll.text.Text;

@TrollModule(id = "events", name = "Events", order = 20)
public final class EventsModule implements LetsTrollModule {

    private EventScheduler scheduler;

    @Override
    public void enable(ModuleContext context) {
        EventsConfig config = context.config(EventsConfig.class);
        List<TrollEvent> events = EventLibrary.all();
        scheduler = new EventScheduler(context.core(), config, events);

        context.screen(new EventsScreen(config, scheduler, events));
        context.hubEntry(HubEntry.of("events")
                .order(20)
                .icon(viewer -> MenuIcon.of("minecraft:clock"))
                .title(viewer -> Text.get(viewer, "hub.tab.events.title"))
                .lore(viewer -> List.of(
                        Text.get(viewer, "hub.tab.events.description"),
                        Text.get(viewer, config.enabled() ? "common.toggle.on" : "common.toggle.off")))
                .permission(LetsTroll.PERMISSION_ADMIN)
                .onClick(click -> click.open(ScreenRequest.of(EventsScreen.ID)))
                .build());

        scheduler.start();
    }

    @Override
    public void disable() {
        if (scheduler != null) {
            scheduler.stop();
            scheduler = null;
        }
    }
}
