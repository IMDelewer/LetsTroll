package dev.delewer.letstroll.fabric;

import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.logging.Logger;

import dev.delewer.letstroll.platform.BossBarService;
import dev.delewer.letstroll.platform.ChainOps;
import dev.delewer.letstroll.platform.ConfigStore;
import dev.delewer.letstroll.platform.EffectsService;
import dev.delewer.letstroll.platform.FakePlayerService;
import dev.delewer.letstroll.platform.ItemBindingService;
import dev.delewer.letstroll.platform.MenuBackend;
import dev.delewer.letstroll.platform.MovementService;
import dev.delewer.letstroll.platform.PingService;
import dev.delewer.letstroll.platform.PlatformEvents;
import dev.delewer.letstroll.platform.PlayerService;
import dev.delewer.letstroll.platform.SoundService;
import dev.delewer.letstroll.platform.StealthService;
import dev.delewer.letstroll.platform.TaskScheduler;
import dev.delewer.letstroll.platform.TextInputService;
import dev.delewer.letstroll.platform.TrollPlatform;
import net.minecraft.server.MinecraftServer;

public final class FabricPlatform implements TrollPlatform {

    private final Supplier<MinecraftServer> server;
    private final Path dataFolder;
    private final Logger logger = Logger.getLogger("LetsTroll");
    private final ConfigStore configs;
    private final MenuBackend menus;
    private final FabricStealth stealth;
    private final FabricEvents events;
    private final SoundService sounds;
    private final TextInputService input;
    private final FakePlayerService fakePlayers;
    private final ItemBindingService itemBindings;
    private final TaskScheduler scheduler;
    private final PlayerService players;
    private final MovementService movement;
    private final PingService ping;
    private final EffectsService effects;
    private final BossBarService bossBars;
    private final ChainOps chain = new FabricChain();

    public FabricPlatform(Supplier<MinecraftServer> server, Path dataFolder, ConfigStore configs, MenuBackend menus,
                          FabricStealth stealth, FabricEvents events, SoundService sounds, TextInputService input,
                          FakePlayerService fakePlayers, ItemBindingService itemBindings, FabricScheduler scheduler,
                          PingService ping) {
        this.server = server;
        this.dataFolder = dataFolder;
        this.configs = configs;
        this.menus = menus;
        this.stealth = stealth;
        this.events = events;
        this.sounds = sounds;
        this.input = input;
        this.fakePlayers = fakePlayers;
        this.itemBindings = itemBindings;
        this.scheduler = scheduler;
        this.ping = ping;
        this.players = new FabricPlayers(server);
        this.movement = new FabricMovement(server);
        this.effects = new FabricEffects(server, scheduler);
        this.bossBars = new FabricBossBar(server);
    }

    @Override
    public String name() {
        return "Fabric";
    }

    @Override
    public String version() {
        MinecraftServer instance = server.get();
        return instance == null ? "unknown" : instance.getVersion();
    }

    @Override
    public Path dataFolder() {
        return dataFolder;
    }

    @Override
    public Logger logger() {
        return logger;
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
    public ChainOps chain() {
        return chain;
    }
}
