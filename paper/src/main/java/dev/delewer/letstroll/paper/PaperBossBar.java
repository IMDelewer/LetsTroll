package dev.delewer.letstroll.paper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import dev.delewer.letstroll.platform.BossBarService;
import dev.delewer.letstroll.platform.PlayerRef;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PaperBossBar implements BossBarService {

    private static final class Handle {
        private final BossBar bar;
        private final Set<UUID> viewers = new HashSet<>();

        private Handle(BossBar bar) {
            this.bar = bar;
        }
    }

    @Override
    public Object create(Component title, String color) {
        BossBar.Color resolved = colorOf(color);
        return new Handle(BossBar.bossBar(title, 1.0f, resolved, BossBar.Overlay.PROGRESS));
    }

    @Override
    public void update(Object handle, Component title, float progress) {
        if (handle instanceof Handle bar) {
            bar.bar.name(title);
            bar.bar.progress(Math.max(0f, Math.min(1f, progress)));
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
            if (bar.viewers.add(viewer.id())) {
                Player player = (Player) viewer.handle();
                if (player != null) {
                    player.showBossBar(bar.bar);
                }
            }
        }
        bar.viewers.removeIf(id -> {
            if (!wanted.contains(id)) {
                Player player = Bukkit.getPlayer(id);
                if (player != null) {
                    player.hideBossBar(bar.bar);
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public void hide(Object handle) {
        if (!(handle instanceof Handle bar)) {
            return;
        }
        for (UUID id : bar.viewers) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                player.hideBossBar(bar.bar);
            }
        }
        bar.viewers.clear();
    }

    private BossBar.Color colorOf(String color) {
        try {
            return BossBar.Color.valueOf(color.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return BossBar.Color.PURPLE;
        }
    }
}
