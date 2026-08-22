package dev.delewer.letstroll.fabric;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import dev.delewer.letstroll.platform.BossBarService;
import dev.delewer.letstroll.platform.PlayerRef;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricBossBar implements BossBarService {

    private static final class Handle {
        private final ServerBossBar bar;
        private final Set<UUID> viewers = new HashSet<>();

        private Handle(ServerBossBar bar) {
            this.bar = bar;
        }
    }

    private final Supplier<MinecraftServer> server;

    public FabricBossBar(Supplier<MinecraftServer> server) {
        this.server = server;
    }

    @Override
    public Object create(Component title, String color) {
        ServerBossBar bar = new ServerBossBar(FabricText.toNative(title), colorOf(color), BossBar.Style.PROGRESS);
        bar.setPercent(1.0f);
        return new Handle(bar);
    }

    @Override
    public void update(Object handle, Component title, float progress) {
        if (handle instanceof Handle bar) {
            bar.bar.setName(FabricText.toNative(title));
            bar.bar.setPercent(Math.max(0f, Math.min(1f, progress)));
        }
    }

    @Override
    public void viewers(Object handle, Collection<PlayerRef> viewers) {
        if (!(handle instanceof Handle bar)) {
            return;
        }
        Set<UUID> wanted = new HashSet<>();
        for (PlayerRef viewer : viewers) {
            wanted.add(viewer.id());
            if (bar.viewers.add(viewer.id()) && viewer.handle() instanceof ServerPlayerEntity player) {
                bar.bar.addPlayer(player);
            }
        }
        MinecraftServer instance = server.get();
        bar.viewers.removeIf(id -> {
            if (!wanted.contains(id)) {
                if (instance != null) {
                    ServerPlayerEntity player = instance.getPlayerManager().getPlayer(id);
                    if (player != null) {
                        bar.bar.removePlayer(player);
                    }
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public void hide(Object handle) {
        if (handle instanceof Handle bar) {
            bar.bar.clearPlayers();
            bar.bar.setVisible(false);
            bar.viewers.clear();
        }
    }

    private BossBar.Color colorOf(String color) {
        try {
            return BossBar.Color.valueOf(color.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return BossBar.Color.PURPLE;
        }
    }
}
