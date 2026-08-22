package dev.delewer.letstroll.module;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.hub.HubEntry;
import dev.delewer.letstroll.menu.MenuRouter;
import dev.delewer.letstroll.menu.Screen;
import dev.delewer.letstroll.platform.TrollPlatform;
import dev.delewer.letstroll.player.PlayerAction;
import dev.ua.theroer.magicutils.commands.MagicCommand;

public final class ModuleContext {

    private final LetsTroll core;
    private final ModuleInfo info;

    ModuleContext(LetsTroll core, ModuleInfo info) {
        this.core = core;
        this.info = info;
    }

    public LetsTroll core() {
        return core;
    }

    public ModuleInfo info() {
        return info;
    }

    public TrollPlatform platform() {
        return core.platform();
    }

    public MenuRouter router() {
        return core.router();
    }

    public <T> T config(Class<T> type) {
        T section = core.config().modules().of(type);
        return section != null ? section : core.platform().configs().load(type);
    }

    public void screen(Screen screen) {
        core.router().register(screen);
    }

    public void hubEntry(HubEntry entry) {
        core.tabs().add(entry);
    }

    public void tab(String id, int order, dev.delewer.letstroll.menu.MenuIcon icon, String screenId) {
        core.tabs().add(HubEntry.tab(id, order, icon, screenId));
    }

    public void playerAction(PlayerAction action) {
        core.playerActions().add(action);
    }

    public void command(MagicCommand command) {
        core.addCommand(command);
    }
}
