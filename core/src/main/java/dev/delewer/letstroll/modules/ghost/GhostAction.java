package dev.delewer.letstroll.modules.ghost;

import java.util.List;

import dev.delewer.letstroll.menu.ClickContext;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.player.PlayerAction;
import dev.delewer.letstroll.text.Text;
import net.kyori.adventure.text.Component;

public final class GhostAction implements PlayerAction {

    private final GhostService service;

    public GhostAction(GhostService service) {
        this.service = service;
    }

    @Override
    public String id() {
        return "ghost";
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public MenuIcon icon(PlayerRef target) {
        return MenuIcon.of(service.isGhost(target.id()) ? "minecraft:ender_eye" : "minecraft:ender_pearl");
    }

    @Override
    public Component name(PlayerRef target) {
        return Text.get(target, "ghost.action.name");
    }

    @Override
    public List<Component> lore(PlayerRef target) {
        return List.of(Text.get(target, service.isGhost(target.id()) ? "ghost.action.on" : "ghost.action.off"));
    }

    @Override
    public String permission() {
        return GhostModule.PERMISSION;
    }

    @Override
    public boolean glow(PlayerRef target) {
        return service.isGhost(target.id());
    }

    @Override
    public void run(ClickContext click, PlayerRef target) {
        PlayerRef viewer = click.viewer();
        boolean self = viewer.id().equals(target.id());
        String required = self ? GhostModule.PERMISSION : GhostModule.PERMISSION_OTHERS;
        if (!viewer.hasPermission(required)) {
            Text.send(viewer, "common.no-permission");
            return;
        }
        boolean enabled = service.toggle(target);
        if (self) {
            Text.send(viewer, enabled ? "ghost.enabled.self" : "ghost.disabled.self");
        } else {
            Text.send(viewer, enabled ? "ghost.enabled.other" : "ghost.disabled.other", target.name());
        }
        click.refresh();
    }
}
