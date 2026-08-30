package dev.delewer.letstroll.fabric;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import dev.delewer.letstroll.platform.PingService;
import dev.delewer.letstroll.platform.PlayerRef;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricPing implements PingService {

    private final Supplier<MinecraftServer> server;
    private final Set<UUID> faked = ConcurrentHashMap.newKeySet();

    public FabricPing(Supplier<MinecraftServer> server) {
        this.server = server;
    }

    @Override
    public void setFake(PlayerRef target, int milliseconds) {
        if (!(target.handle() instanceof ServerPlayerEntity player)) {
            return;
        }
        faked.add(target.id());
        player.networkHandler.latency = Math.max(0, milliseconds);
        broadcast(player);
    }

    @Override
    public void clear(PlayerRef target) {
        faked.remove(target.id());
        if (target.handle() instanceof ServerPlayerEntity player) {
            broadcast(player);
        }
    }

    @Override
    public boolean isFaked(UUID id) {
        return faked.contains(id);
    }

    public void forget(UUID id) {
        faked.remove(id);
    }

    @Override
    public void clearAll() {
        faked.clear();
    }

    private void broadcast(ServerPlayerEntity player) {
        MinecraftServer instance = server.get();
        if (instance != null) {
            instance.getPlayerManager().sendToAll(new PlayerListS2CPacket(
                    EnumSet.of(PlayerListS2CPacket.Action.UPDATE_LATENCY), List.of(player)));
        }
    }
}
