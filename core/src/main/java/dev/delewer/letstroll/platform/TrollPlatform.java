package dev.delewer.letstroll.platform;

import java.nio.file.Path;
import java.util.logging.Logger;


public interface TrollPlatform {

    String name();

    String version();

    Path dataFolder();

    Logger logger();

    ConfigStore configs();

    PlayerService players();

    MenuBackend menus();

    StealthService stealth();

    TaskScheduler scheduler();

    PlatformEvents events();

    SoundService sounds();

    TextInputService input();

    FakePlayerService fakePlayers();

    MovementService movement();

    PingService ping();

    EffectsService effects();

    BossBarService bossBars();

    ItemBindingService itemBindings();

    ChainOps chain();

    default String placeholders(PlayerRef viewer, String text) {
        return text;
    }

    default boolean holdPackets(PlayerRef target, long delayMillis, boolean dangerous) {
        return false;
    }

    default void releasePackets(PlayerRef target) {
    }
}
