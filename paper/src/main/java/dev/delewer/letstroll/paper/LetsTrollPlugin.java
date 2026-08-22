package dev.delewer.letstroll.paper;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.text.Lang;
import dev.ua.theroer.magicutils.bootstrap.BukkitBootstrap;
import dev.ua.theroer.magicutils.http.MagicHttpClient;
import dev.ua.theroer.magicutils.lang.LanguageManager;
import dev.ua.theroer.magicutils.lang.Messages;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bstats.charts.AdvancedPie;


public final class LetsTrollPlugin extends JavaPlugin implements Listener {

    private static final int BSTATS_ID = 33564;

    private BukkitBootstrap.RuntimeResult magic;
    private LetsTroll core;
    private PaperFakePlayers fakePlayers;
    private PaperMenus menus;
    private PaperPlatform platformRef;
    private Metrics metrics;
    private Thread cleanupHook;

    @Override
    public void onEnable() {
        magic = BukkitBootstrap.forPlugin(this)
                .permissionPrefix("letstroll")
                .enableCommands()
                .enableDiagnostics()
                .translations(languages -> {
                    languages.registerTranslations("en", Lang.english());
                    languages.setFallbackLanguage("en");
                })
                .buildRuntime();

        PaperStealth stealth = new PaperStealth(this);
        PaperEvents events = new PaperEvents();
        PaperTextInput input = new PaperTextInput(this);
        menus = new PaperMenus(this, () -> core);
        MagicHttpClient http = magic.runtime().manage("skin-http", MagicHttpClient.builder(magic.platform())
                .userAgent("LetsTroll")
                .mapper(new com.fasterxml.jackson.databind.ObjectMapper()
                        .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false))
                .build());
        fakePlayers = new PaperFakePlayers(this, new SkinResolver(http));
        PaperSounds sounds = new PaperSounds(() -> core);
        PaperItemBindings itemBindings = new PaperItemBindings(this, () -> core);

        platformRef = new PaperPlatform(this, new MagicConfigStore(magic.configManager()), menus,
                stealth, events, sounds, input, fakePlayers, itemBindings, magic.logger());
        PaperPlatform platform = platformRef;

        core = new LetsTroll(platform);
        selectLanguage(magic.languageManager(), core.config().language());

        Bukkit.getPluginManager().registerEvents(events, this);
        Bukkit.getPluginManager().registerEvents(menus, this);
        Bukkit.getPluginManager().registerEvents(new StealthListener(stealth), this);
        Bukkit.getPluginManager().registerEvents(new NoclipListener(stealth), this);
        Bukkit.getPluginManager().registerEvents(input, this);
        Bukkit.getPluginManager().registerEvents(new PluginHiderListener(this, () -> core), this);
        Bukkit.getPluginManager().registerEvents(new CommandHiderListener(() -> core), this);
        Bukkit.getPluginManager().registerEvents(itemBindings, this);
        Bukkit.getPluginManager().registerEvents(this, this);

        core.start();
        core.commands().forEach(command -> magic.commandRegistry().registerCommand(command));

        setupMetrics();

        magic.logger().success("Loaded %d modules", core.modules().count());

        if (core.config().nativeHide()) {
            concealNatively();
        }
    }

    private void concealNatively() {
        cleanupHook = new Thread(this::cleanup, "LetsTroll-cleanup");
        Runtime.getRuntime().addShutdownHook(cleanupHook);
        boolean concealed = PluginConcealer.conceal(this, magic.logger().create("Stealth"));
        if (concealed) {
            magic.logger().info("Native hide active (removed from the server plugin list).");
        } else {
            magic.logger().warn("Native hide is not available on this server, using command filters instead.");
        }
    }

    private synchronized void cleanup() {
        if (platformRef != null) {
            try {
                platformRef.pingService().clearAll();
            } catch (RuntimeException ignored) {
            }
            platformRef = null;
        }
        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
        if (fakePlayers != null) {
            try {
                fakePlayers.despawnEverything();
            } catch (RuntimeException ignored) {
            }
            fakePlayers = null;
        }
        if (core != null) {
            try {
                core.stop();
            } catch (RuntimeException ignored) {
            }
            core = null;
        }
        if (magic != null) {
            magic.runtime().close();
            magic = null;
        }
        dropCleanupHook();
    }

    private void dropCleanupHook() {
        if (cleanupHook == null || cleanupHook == Thread.currentThread()) {
            return;
        }
        try {
            Runtime.getRuntime().removeShutdownHook(cleanupHook);
        } catch (IllegalStateException ignored) {
        }
        cleanupHook = null;
    }

    private void setupMetrics() {
        metrics = new Metrics(this, BSTATS_ID);
        metrics.addCustomChart(new SingleLineChart("trolls_total", () -> core.stats().drainTotal()));
        metrics.addCustomChart(new SingleLineChart("unique_trolled_players", () -> core.stats().drainUniqueVictims()));
        metrics.addCustomChart(new AdvancedPie("trolls_by_module", () -> core.stats().drainByModule()));
    }

    @Override
    public void onDisable() {
        cleanup();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (core != null) {
            core.router().forget(event.getPlayer().getUniqueId());
        }
        if (menus != null) {
            menus.forget(event.getPlayer().getUniqueId());
        }
    }

    private void selectLanguage(LanguageManager languages, String language) {
        if (languages == null) {
            return;
        }
        languages.setLanguage(language);
        Messages.setLanguageManager(languages);
    }
}
