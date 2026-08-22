package dev.delewer.letstroll.fabric;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.PlayerService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricPlayers implements PlayerService {

    private final Supplier<MinecraftServer> server;

    public FabricPlayers(Supplier<MinecraftServer> server) {
        this.server = server;
    }

    @Override
    public List<PlayerRef> online() {
        MinecraftServer instance = server.get();
        if (instance == null) {
            return List.of();
        }
        return instance.getPlayerManager().getPlayerList().stream()
                .map(player -> (PlayerRef) new FabricPlayerRef(player, server))
                .toList();
    }

    @Override
    public Optional<PlayerRef> byId(UUID id) {
        MinecraftServer instance = server.get();
        if (instance == null) {
            return Optional.empty();
        }
        ServerPlayerEntity player = instance.getPlayerManager().getPlayer(id);
        return player == null ? Optional.empty() : Optional.of(new FabricPlayerRef(player, server));
    }

    @Override
    public Optional<PlayerRef> byName(String name) {
        MinecraftServer instance = server.get();
        if (instance == null) {
            return Optional.empty();
        }
        ServerPlayerEntity player = instance.getPlayerManager().getPlayer(name);
        return player == null ? Optional.empty() : Optional.of(new FabricPlayerRef(player, server));
    }
}
