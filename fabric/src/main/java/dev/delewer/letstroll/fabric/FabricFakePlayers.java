package dev.delewer.letstroll.fabric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import dev.delewer.letstroll.platform.FakePlayerService;
import dev.delewer.letstroll.platform.FakePlayerSpec;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.TaskScheduler;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public final class FabricFakePlayers implements FakePlayerService {

    private record Fake(int entityId, UUID uuid) {
    }

    private final Supplier<MinecraftServer> server;
    private final TaskScheduler scheduler;
    private final FabricSkinResolver skins;
    private final Map<UUID, List<Fake>> spawned = new ConcurrentHashMap<>();
    private final AtomicInteger ids = new AtomicInteger(Integer.MAX_VALUE - 1000);

    public FabricFakePlayers(Supplier<MinecraftServer> server, TaskScheduler scheduler, FabricSkinResolver skins) {
        this.server = server;
        this.scheduler = scheduler;
        this.skins = skins;
    }

    @Override
    public void spawn(PlayerRef target, FakePlayerSpec spec) {
        if (!(target.handle() instanceof ServerPlayerEntity player)) {
            return;
        }
        if (spec.copyTargetSkin()) {
            doSpawn(target, player, spec, textureOf(player.getGameProfile()));
            return;
        }

        String immediate = FabricSkinResolver.immediateTexture(spec.skinOwner()).orElse(null);
        if (immediate != null) {
            doSpawn(target, player, spec, immediate);
            return;
        }

        String name = FabricSkinResolver.nameFrom(spec.skinOwner());
        Optional<String> hit = skins.cached(name);
        if (hit.isPresent()) {
            doSpawn(target, player, spec, hit.get());
            return;
        }

        skins.byName(name).whenComplete((resolved, error) -> scheduler.sync(
                () -> doSpawn(target, player, spec, resolved == null ? null : resolved.orElse(null))));
    }

    private Fake doSpawn(PlayerRef target, ServerPlayerEntity player, FakePlayerSpec spec, String texture) {
        double yaw = player.getYaw();
        double rad = Math.toRadians(yaw);
        double distance = Math.max(1.0, spec.distance());
        double x = player.getX() - Math.sin(rad) * distance;
        double z = player.getZ() + Math.cos(rad) * distance;
        float faceYaw = spec.facePlayer() ? (float) (yaw + 180.0) : (float) yaw;

        int entityId = ids.getAndDecrement();
        UUID uuid = UUID.randomUUID();
        GameProfile profile = new GameProfile(uuid, "Mannequin");
        if (texture != null && !texture.isBlank()) {
            profile.properties().put("textures", new Property("textures", texture));
        }

        PlayerListS2CPacket listPacket = new PlayerListS2CPacket(
                EnumSet.of(PlayerListS2CPacket.Action.ADD_PLAYER, PlayerListS2CPacket.Action.UPDATE_LISTED),
                List.<ServerPlayerEntity>of());
        listPacket.entries = List.of(new PlayerListS2CPacket.Entry(
                uuid, profile, false, 0, GameMode.SURVIVAL, null, true, 0, null));
        EntitySpawnS2CPacket spawnPacket = new EntitySpawnS2CPacket(entityId, uuid, x, player.getY(), z,
                0f, faceYaw, EntityType.PLAYER, 0, Vec3d.ZERO, faceYaw);

        broadcast(player, spec.visibleOnlyToTarget(), listPacket, spawnPacket);

        Fake fake = new Fake(entityId, uuid);
        spawned.computeIfAbsent(target.id(), key -> new ArrayList<>()).add(fake);
        if (spec.lifetimeTicks() > 0) {
            scheduler.later(() -> despawn(target, spec.visibleOnlyToTarget(), fake), spec.lifetimeTicks());
        }
        return fake;
    }

    @Override
    public void despawnAll(PlayerRef target) {
        List<Fake> list = spawned.remove(target.id());
        if (list == null || list.isEmpty()) {
            return;
        }
        if (target.handle() instanceof ServerPlayerEntity player) {
            list.forEach(fake -> sendRemoval(player, true, fake));
        }
    }

    public void despawnEverything() {
        MinecraftServer instance = server.get();
        if (instance != null) {
            spawned.forEach((id, list) -> {
                ServerPlayerEntity player = instance.getPlayerManager().getPlayer(id);
                if (player != null) {
                    list.forEach(fake -> sendRemoval(player, true, fake));
                }
            });
        }
        spawned.clear();
    }

    private void despawn(PlayerRef target, boolean onlyTarget, Fake fake) {
        List<Fake> list = spawned.get(target.id());
        if (list != null) {
            list.remove(fake);
        }
        if (target.handle() instanceof ServerPlayerEntity player) {
            sendRemoval(player, onlyTarget, fake);
        }
    }

    private void sendRemoval(ServerPlayerEntity target, boolean onlyTarget, Fake fake) {
        broadcast(target, onlyTarget,
                new EntitiesDestroyS2CPacket(fake.entityId()),
                new PlayerRemoveS2CPacket(List.of(fake.uuid())));
    }

    private void broadcast(ServerPlayerEntity target, boolean onlyTarget, Packet<?>... packets) {
        MinecraftServer instance = server.get();
        if (instance == null) {
            return;
        }
        Collection<ServerPlayerEntity> viewers = onlyTarget
                ? List.of(target)
                : instance.getPlayerManager().getPlayerList();
        for (ServerPlayerEntity viewer : viewers) {
            for (Packet<?> packet : packets) {
                viewer.networkHandler.sendPacket(packet);
            }
        }
    }

    private String textureOf(GameProfile profile) {
        Collection<Property> textures = profile.properties().get("textures");
        for (Property property : textures) {
            return property.value();
        }
        return null;
    }
}
