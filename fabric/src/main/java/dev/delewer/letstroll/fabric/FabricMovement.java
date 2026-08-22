package dev.delewer.letstroll.fabric;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import dev.delewer.letstroll.platform.MovementService;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.Position;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class FabricMovement implements MovementService {

    private final Supplier<MinecraftServer> server;

    public FabricMovement(Supplier<MinecraftServer> server) {
        this.server = server;
    }

    @Override
    public Optional<Position> positionOf(PlayerRef player) {
        if (!(player.handle() instanceof ServerPlayerEntity handle)) {
            return Optional.empty();
        }
        return Optional.of(new Position(handle.getEntityWorld().getRegistryKey().getValue().toString(),
                handle.getX(), handle.getY(), handle.getZ(), handle.getYaw(), handle.getPitch()));
    }

    @Override
    public void teleport(PlayerRef player, Position position) {
        if (!(player.handle() instanceof ServerPlayerEntity handle)) {
            return;
        }
        MinecraftServer instance = server.get();
        if (instance == null) {
            return;
        }
        RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, FabricText.id(position.world()));
        ServerWorld world = instance.getWorld(key);
        if (world == null) {
            world = handle.getEntityWorld();
        }
        handle.teleport(world, position.x(), position.y(), position.z(), Set.of(), position.yaw(), position.pitch(), false);
    }

    @Override
    public void push(PlayerRef player, double x, double y, double z) {
        if (player.handle() instanceof ServerPlayerEntity handle) {
            handle.setVelocity(handle.getVelocity().add(new Vec3d(x, y, z)));
            handle.velocityDirty = true;
        }
    }
}
