package dev.delewer.letstroll.modules.settings;

import java.util.List;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.hub.HubEntry;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.module.LetsTrollModule;
import dev.delewer.letstroll.module.ModuleContext;
import dev.delewer.letstroll.module.TrollModule;
import dev.delewer.letstroll.text.Text;

@TrollModule(id = "settings", name = "Settings", order = 40)
public final class SettingsModule implements LetsTrollModule {

    @Override
    public void enable(ModuleContext context) {
        context.screen(new SettingsScreen());
        context.hubEntry(HubEntry.of("settings")
                .order(40)
                .icon(viewer -> MenuIcon.of("minecraft:comparator"))
                .title(viewer -> Text.get(viewer, "hub.tab.settings.title"))
                .lore(viewer -> List.of(Text.get(viewer, "hub.tab.settings.description")))
                .permission(LetsTroll.PERMISSION_ADMIN)
                .onClick(click -> click.open(ScreenRequest.of(SettingsScreen.ID)))
                .build());
    }
}
