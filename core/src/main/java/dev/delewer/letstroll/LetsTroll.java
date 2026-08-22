package dev.delewer.letstroll;

import java.util.ArrayList;
import java.util.List;

import dev.delewer.letstroll.config.CoreConfig;
import dev.delewer.letstroll.config.HeadsConfig;
import dev.delewer.letstroll.hub.HubRegistry;
import dev.delewer.letstroll.menu.HeadCatalog;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.menu.MenuRouter;
import dev.delewer.letstroll.module.ModuleManager;
import dev.delewer.letstroll.platform.TaskScheduler;
import dev.delewer.letstroll.platform.TrollPlatform;
import dev.delewer.letstroll.player.PlayerActionRegistry;
import dev.ua.theroer.magicutils.commands.MagicCommand;

public final class LetsTroll {

    public static final String PERMISSION_USE = "letstroll.use";
    public static final String PERMISSION_ADMIN = "letstroll.admin";

    private final TrollPlatform platform;
    private final CoreConfig config;
    private final HeadCatalog heads;
    private final MenuRouter router;
    private final HubRegistry tabs = new HubRegistry();
    private final PlayerActionRegistry playerActions = new PlayerActionRegistry();
    private final ModuleManager modules;
    private final TrollStats stats = new TrollStats();
    private final List<MagicCommand> commands = new ArrayList<>();
    private TaskScheduler.Cancellable refreshTask;

    public LetsTroll(TrollPlatform platform) {
        this.platform = platform;
        dev.delewer.letstroll.text.Text.bind(platform);
        this.config = platform.configs().load(CoreConfig.class);
        this.heads = new HeadCatalog(platform.configs().load(HeadsConfig.class).icons());
        this.router = new MenuRouter(this);
        this.modules = new ModuleManager(this);
    }

    public void start() {
        modules.loadAll(config.disabledModules());
        int interval = config.menuRefreshTicks();
        if (interval > 0) {
            refreshTask = platform.scheduler().repeating(router::refreshAll, interval);
        }
    }

    public void stop() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        modules.unloadAll();
        commands.clear();
    }

    public TrollPlatform platform() {
        return platform;
    }

    public CoreConfig config() {
        return config;
    }

    public void saveConfig() {
        platform.configs().save(config);
    }

    public MenuRouter router() {
        return router;
    }

    public HeadCatalog heads() {
        return heads;
    }

    public HubRegistry tabs() {
        return tabs;
    }

    public PlayerActionRegistry playerActions() {
        return playerActions;
    }

    public ModuleManager modules() {
        return modules;
    }

    public TrollStats stats() {
        return stats;
    }

    public MenuIcon filler() {
        return MenuIcon.of(config.menuFiller());
    }

    public void addCommand(MagicCommand command) {
        commands.add(command);
    }

    public List<MagicCommand> commands() {
        return List.copyOf(commands);
    }
}
