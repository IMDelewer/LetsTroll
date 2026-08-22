package dev.delewer.letstroll.modules.bindings;

import java.util.List;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.hub.HubEntry;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.module.LetsTrollModule;
import dev.delewer.letstroll.module.ModuleContext;
import dev.delewer.letstroll.module.TrollModule;
import dev.delewer.letstroll.text.Text;

@TrollModule(id = "bindings", name = "Bindings", order = 30)
public final class BindingsModule implements LetsTrollModule {

    @Override
    public void enable(ModuleContext context) {
        context.screen(new BindingsScreen());
        context.hubEntry(HubEntry.of("bindings")
                .order(30)
                .icon(viewer -> MenuIcon.of("minecraft:blaze_rod"))
                .title(viewer -> Text.get(viewer, "hub.tab.bindings.title"))
                .lore(viewer -> List.of(Text.get(viewer, "hub.tab.bindings.description")))
                .permission(LetsTroll.PERMISSION_USE)
                .onClick(click -> click.open(ScreenRequest.of(BindingsScreen.ID)))
                .build());
    }
}
