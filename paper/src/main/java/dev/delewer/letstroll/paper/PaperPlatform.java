package dev.delewer.letstroll.paper;

import java.nio.file.Path;
import java.util.logging.Logger;

import dev.delewer.letstroll.platform.ConfigStore;
import dev.delewer.letstroll.platform.FakePlayerService;
import dev.delewer.letstroll.platform.MovementService;
import dev.delewer.letstroll.platform.BossBarService;
import dev.delewer.letstroll.platform.EffectsService;
import dev.delewer.letstroll.platform.ItemBindingService;
import dev.delewer.letstroll.platform.PingService;
import dev.delewer.letstroll.platform.SoundService;
import dev.delewer.letstroll.platform.TextInputService;
import dev.delewer.letstroll.platform.MenuBackend;
import dev.delewer.letstroll.platform.PlatformEvents;
import dev.delewer.letstroll.platform.PlayerService;
import dev.delewer.letstroll.platform.StealthService;
import dev.delewer.letstroll.platform.TaskScheduler;
import dev.delewer.letstroll.platform.TrollPlatform;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperPlatform implements TrollPlatform {

    private final JavaPlugin plugin;
    private final ConfigStore configs;
    private final PlayerService players;
    private final MenuBackend menus;
    private final PaperStealth stealth;
    private final TaskScheduler scheduler;
    private final PaperEvents events;
    private final SoundService sounds;
    private final TextInputService input;
    private final FakePlayerService fakePlayers;
    private final MovementService movement;
    private final PingService ping;
    private final EffectsService effects;
    private final BossBarService bossBars;
    private final ItemBindingService itemBindings;
    private final dev.delewer.letstroll.platform.ChainOps chain;
    private final dev.ua.theroer.magicutils.integrations.PlaceholderApiIntegration placeholders;

    public PaperPlatform(JavaPlugin plugin, ConfigStore configs, MenuBackend menus, PaperStealth stealth,
                         PaperEvents events, SoundService sounds, TextInputService input, FakePlayerService fakePlayers,
                         ItemBindingService itemBindings, dev.ua.theroer.magicutils.Logger logger) {
        this.plugin = plugin;
        this.configs = configs;
        this.menus = menus;
        this.stealth = stealth;
        this.events = events;
        this.sounds = sounds;
        this.input = input;
        this.fakePlayers = fakePlayers;
        this.movement = new PaperMovement(plugin);
        this.ping = new PaperPing(plugin, logger.create("Ping"));
        this.effects = new PaperEffects(plugin, logger.create("Effects"));
        this.bossBars = new PaperBossBar();
        this.itemBindings = itemBindings;
        this.chain = new PaperChain(plugin);
        this.players = new PaperPlayers();
        this.scheduler = new PaperScheduler(plugin);
        this.placeholders = new dev.ua.theroer.magicutils.integrations.PlaceholderApiIntegration(plugin);
    }

    @Override
    public dev.delewer.letstroll.platform.ChainOps chain() {
        return chain;
    }

    @Override
    public String name() {
        return Bukkit.getName();
    }

    @Override
    public String version() {
        return Bukkit.getMinecraftVersion();
    }

    @Override
    public Path dataFolder() {
        return plugin.getDataFolder().toPath();
    }

    @Override
    public Logger logger() {
        return plugin.getLogger();
    }

    @Override
    public ConfigStore configs() {
        return configs;
    }

    @Override
    public PlayerService players() {
        return players;
    }

    @Override
    public MenuBackend menus() {
        return menus;
    }

    @Override
    public StealthService stealth() {
        return stealth;
    }

    @Override
    public TaskScheduler scheduler() {
        return scheduler;
    }

    @Override
    public PlatformEvents events() {
        return events;
    }

    @Override
    public SoundService sounds() {
        return sounds;
    }

    @Override
    public TextInputService input() {
        return input;
    }

    @Override
    public FakePlayerService fakePlayers() {
        return fakePlayers;
    }

    @Override
    public MovementService movement() {
        return movement;
    }

    @Override
    public PingService ping() {
        return ping;
    }

    public PaperPing pingService() {
        return (PaperPing) ping;
    }

    @Override
    public EffectsService effects() {
        return effects;
    }

    @Override
    public BossBarService bossBars() {
        return bossBars;
    }

    @Override
    public ItemBindingService itemBindings() {
        return itemBindings;
    }

    @Override
    public String placeholders(dev.delewer.letstroll.platform.PlayerRef viewer, String text) {
        if (!(viewer.handle() instanceof org.bukkit.entity.Player player)) {
            return text;
        }
        return placeholders.renderPlaceholders(player, text);
    }
}
