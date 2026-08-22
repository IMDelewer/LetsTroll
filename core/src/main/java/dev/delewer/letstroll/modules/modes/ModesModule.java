package dev.delewer.letstroll.modules.modes;

import java.util.List;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.hub.HubEntry;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.module.LetsTrollModule;
import dev.delewer.letstroll.module.ModuleContext;
import dev.delewer.letstroll.module.TrollModule;
import dev.delewer.letstroll.text.Text;

@TrollModule(id = "modes", name = "Modes", order = 25)
public final class ModesModule implements LetsTrollModule {

    private ChainService service;

    @Override
    public void enable(ModuleContext context) {
        ChainConfig config = context.config(ChainConfig.class);
        service = new ChainService(context.core(), config);

        context.screen(new ModesScreen());
        context.screen(new ChainScreen(config, service));

        context.hubEntry(HubEntry.of("modes")
                .order(25)
                .icon(viewer -> MenuIcon.of("minecraft:chain"))
                .title(viewer -> Text.get(viewer, "hub.tab.modes.title"))
                .lore(viewer -> List.of(Text.get(viewer, "hub.tab.modes.description")))
                .permission(LetsTroll.PERMISSION_ADMIN)
                .onClick(click -> click.open(ScreenRequest.of(ModesScreen.ID)))
                .build());

        service.start();
    }

    @Override
    public void disable() {
        if (service != null) {
            service.stop();
            service = null;
        }
    }
}
