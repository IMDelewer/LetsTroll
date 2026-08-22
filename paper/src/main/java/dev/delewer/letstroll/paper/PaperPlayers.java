package dev.delewer.letstroll.paper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.PlayerService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PaperPlayers implements PlayerService {

    @Override
    public List<PlayerRef> online() {
        return Bukkit.getOnlinePlayers().stream()
                .map(player -> (PlayerRef) new PaperPlayerRef(player))
                .toList();
    }

    @Override
    public Optional<PlayerRef> byId(UUID id) {
        Player player = Bukkit.getPlayer(id);
        return player == null ? Optional.empty() : Optional.of(new PaperPlayerRef(player));
    }

    @Override
    public Optional<PlayerRef> byName(String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player == null) {
            player = Bukkit.getPlayer(name);
        }
        return player == null ? Optional.empty() : Optional.of(new PaperPlayerRef(player));
    }
}
