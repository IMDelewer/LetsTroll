package dev.delewer.letstroll.fabric;

import java.util.UUID;
import java.util.function.Supplier;

import dev.delewer.letstroll.platform.PlayerRef;
import dev.ua.theroer.magicutils.platform.fabric.FabricAudience;
import dev.ua.theroer.magicutils.platform.fabric.FabricPermissionBridge;
import net.kyori.adventure.text.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricPlayerRef implements PlayerRef {

    private final UUID id;
    private final String name;
    private final Supplier<MinecraftServer> server;

    public FabricPlayerRef(ServerPlayerEntity player, Supplier<MinecraftServer> server) {
        this.id = player.getUuid();
        this.name = player.getGameProfile().name();
        this.server = server;
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean online() {
        return player() != null;
    }

    @Override
    public boolean hasPermission(String node) {
        ServerPlayerEntity player = player();
        return player != null && FabricPermissionBridge.hasPermission(player, node, 2);
    }

    @Override
    public void send(Component message) {
        ServerPlayerEntity player = player();
        if (player != null) {
            new FabricAudience(player).send(message);
        }
    }

    @Override
    public String world() {
        ServerPlayerEntity player = player();
        if (player == null) {
            return "-";
        }
        return player.getEntityWorld().getRegistryKey().getValue().toString();
    }

    @Override
    public int ping() {
        ServerPlayerEntity player = player();
        return player == null ? 0 : player.networkHandler.getLatency();
    }

    @Override
    public double health() {
        ServerPlayerEntity player = player();
        return player == null ? 0 : player.getHealth();
    }

    @Override
    public Object handle() {
        return player();
    }

    public ServerPlayerEntity player() {
        MinecraftServer instance = server.get();
        if (instance == null) {
            return null;
        }
        return instance.getPlayerManager().getPlayer(id);
    }
}
