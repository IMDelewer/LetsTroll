package dev.delewer.letstroll.modules.ghost;

import java.util.List;

import dev.delewer.letstroll.hub.HubEntry;
import dev.delewer.letstroll.menu.Heads;
import dev.delewer.letstroll.menu.MenuLayout;
import dev.delewer.letstroll.module.LetsTrollModule;
import dev.delewer.letstroll.module.ModuleContext;
import dev.delewer.letstroll.module.TrollModule;
import dev.delewer.letstroll.platform.MenuSound;
import dev.delewer.letstroll.text.Text;

@TrollModule(id = "ghost", name = "Ghost", order = 50)
public final class GhostModule implements LetsTrollModule {

    public static final String PERMISSION = "letstroll.ghost";
    public static final String PERMISSION_OTHERS = "letstroll.ghost.others";

    private GhostService service;

    @Override
    public void enable(ModuleContext context) {
        GhostConfig config = context.config(GhostConfig.class);
        service = new GhostService(context.platform(), config);
        service.load();
        service.registerIntents();

        context.playerAction(new GhostAction(service));
        context.command(new GhostCommand(context.core(), service));
        context.hubEntry(hubToggle(context));
    }

    @Override
    public void disable() {
        if (service != null) {
            service.revealEverything();
            service = null;
        }
    }

    private HubEntry hubToggle(ModuleContext context) {
        int corner = MenuLayout.contentCorner(context.core().config().menuRows());
        return HubEntry.of("ghost")
                .order(90)
                .slot(corner)
                .permission(PERMISSION)
                .icon(viewer -> context.core().heads().icon(service.isGhost(viewer.id()) ? Heads.TOGGLE_ON : Heads.TOGGLE_OFF))
                .title(viewer -> Text.get(viewer, "ghost.action.name"))
                .lore(viewer -> List.of(
                        Text.get(viewer, service.isGhost(viewer.id()) ? "ghost.action.on" : "ghost.action.off"),
                        Text.get(viewer, "ghost.action.hint")))
                .glow(viewer -> service.isGhost(viewer.id()))
                .onClick(click -> {
                    boolean enabled = service.toggle(click.viewer());
                    click.sound(enabled ? MenuSound.TOGGLE_ON : MenuSound.TOGGLE_OFF);
                    Text.send(click.viewer(), enabled ? "ghost.enabled.self" : "ghost.disabled.self");
                    click.refresh();
                })
                .build();
    }
}
