package dev.delewer.letstroll.fabric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import dev.delewer.letstroll.platform.EffectsService;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.TaskScheduler;
import dev.ua.theroer.magicutils.platform.fabric.FabricAudience;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.text.Component;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

public final class FabricEffects implements EffectsService {

    private record Snapshot(ServerWorld world, BlockPos pos, net.minecraft.block.BlockState state) {
    }

    private static final List<Item> GIFTS = List.of(
            Items.DIAMOND, Items.EMERALD, Items.GOLDEN_APPLE, Items.NETHERITE_INGOT,
            Items.ENCHANTED_GOLDEN_APPLE, Items.EXPERIENCE_BOTTLE, Items.TOTEM_OF_UNDYING);
    private static final List<Item> JUNK = List.of(
            Items.DIRT, Items.COBBLESTONE, Items.ROTTEN_FLESH, Items.STICK,
            Items.POISONOUS_POTATO, Items.WHEAT_SEEDS, Items.GRAVEL);

    private final Supplier<MinecraftServer> server;
    private final TaskScheduler scheduler;
    private final Map<String, List<Snapshot>> undo = new ConcurrentHashMap<>();

    public FabricEffects(Supplier<MinecraftServer> server, TaskScheduler scheduler) {
        this.server = server;
        this.scheduler = scheduler;
    }

    private ServerPlayerEntity player(PlayerRef ref) {
        return ref != null && ref.handle() instanceof ServerPlayerEntity player ? player : null;
    }

    @Override
    public void potion(PlayerRef target, String effectKey, int durationTicks, int amplifier) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return;
        }
        RegistryEntry<StatusEffect> effect = effect(effectKey);
        if (effect != null) {
            player.addStatusEffect(new StatusEffectInstance(effect, durationTicks, Math.max(0, amplifier), false, false, true));
        }
    }

    @Override
    public void lightning(PlayerRef target, boolean cosmetic) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return;
        }
        ServerWorld world = player.getEntityWorld();
        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.COMMAND);
        if (bolt == null) {
            return;
        }
        bolt.refreshPositionAfterTeleport(player.getX(), player.getY(), player.getZ());
        bolt.setCosmetic(cosmetic);
        world.spawnEntity(bolt);
    }

    @Override
    public void explosion(PlayerRef target, float power, boolean breakBlocks, boolean damage) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return;
        }
        player.getEntityWorld().createExplosion(damage ? player : null, player.getX(), player.getY(), player.getZ(),
                power, breakBlocks ? World.ExplosionSourceType.TNT : World.ExplosionSourceType.NONE);
    }

    @Override
    public void sound(PlayerRef target, String sound, float volume, float pitch) {
        FabricSounds.playRaw(player(target), sound, volume, pitch);
    }

    @Override
    public void spawnMobs(PlayerRef target, String entityType, int count, double radius) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return;
        }
        EntityType<?> type = Registries.ENTITY_TYPE.get(FabricText.id(entityType));
        ServerWorld world = player.getEntityWorld();
        for (int index = 0; index < count; index++) {
            double angle = 2 * Math.PI * index / Math.max(1, count);
            BlockPos pos = BlockPos.ofFloored(player.getX() + Math.cos(angle) * radius,
                    player.getY(), player.getZ() + Math.sin(angle) * radius);
            type.spawn(world, pos, SpawnReason.COMMAND);
        }
    }

    @Override
    public void broadcast(Component message) {
        MinecraftServer instance = server.get();
        if (instance != null) {
            instance.getPlayerManager().getPlayerList().forEach(player -> new FabricAudience(player).send(message));
        }
    }

    @Override
    public void launch(PlayerRef target, double power) {
        ServerPlayerEntity player = player(target);
        if (player != null) {
            player.setVelocity(player.getVelocity().add(new Vec3d(0, power, 0)));
            player.velocityDirty = true;
        }
    }

    @Override
    public void teleportRandom(PlayerRef target, double radius) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return;
        }
        ServerWorld world = player.getEntityWorld();
        double dx = (ThreadLocalRandom.current().nextDouble() * 2 - 1) * radius;
        double dz = (ThreadLocalRandom.current().nextDouble() * 2 - 1) * radius;
        int x = (int) Math.floor(player.getX() + dx);
        int z = (int) Math.floor(player.getZ() + dz);
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        player.teleport(world, x + 0.5, y, z + 0.5, java.util.Set.of(), player.getYaw(), player.getPitch(), false);
    }

    @Override
    public void teleportTo(PlayerRef target, PlayerRef destination) {
        ServerPlayerEntity player = player(target);
        ServerPlayerEntity to = player(destination);
        if (player != null && to != null) {
            player.teleport(to.getEntityWorld(), to.getX(), to.getY(), to.getZ(), java.util.Set.of(), to.getYaw(), to.getPitch(), false);
        }
    }

    @Override
    public void giveItem(PlayerRef target, String materialKey, int amount) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return;
        }
        Item item;
        if (materialKey == null || materialKey.isBlank()) {
            item = GIFTS.get(ThreadLocalRandom.current().nextInt(GIFTS.size()));
        } else {
            item = Registries.ITEM.get(FabricText.id(materialKey));
        }
        if (item == Items.AIR) {
            return;
        }
        player.getInventory().insertStack(new ItemStack(item, Math.max(1, amount)));
    }

    @Override
    public void dropItems(PlayerRef target, int count) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return;
        }
        ServerWorld world = player.getEntityWorld();
        for (int index = 0; index < count; index++) {
            Item item = JUNK.get(ThreadLocalRandom.current().nextInt(JUNK.size()));
            ItemEntity entity = new ItemEntity(world,
                    player.getX() + ThreadLocalRandom.current().nextDouble() * 2 - 1,
                    player.getY() + 1.5,
                    player.getZ() + ThreadLocalRandom.current().nextDouble() * 2 - 1,
                    new ItemStack(item));
            world.spawnEntity(entity);
        }
    }

    @Override
    public void heal(PlayerRef target) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return;
        }
        player.setHealth(player.getMaxHealth());
        player.getHungerManager().setFoodLevel(20);
        player.getHungerManager().setSaturationLevel(20f);
        player.setFireTicks(0);
    }

    @Override
    public void firework(PlayerRef target) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return;
        }
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        FireworkExplosionComponent explosion = new FireworkExplosionComponent(
                FireworkExplosionComponent.Type.LARGE_BALL,
                IntList.of(ThreadLocalRandom.current().nextInt(0xFFFFFF)),
                IntList.of(ThreadLocalRandom.current().nextInt(0xFFFFFF)),
                true, true);
        rocket.set(DataComponentTypes.FIREWORKS, new FireworksComponent((byte) 1, List.of(explosion)));
        FireworkRocketEntity entity = new FireworkRocketEntity(player.getEntityWorld(),
                player.getX(), player.getY(), player.getZ(), rocket);
        player.getEntityWorld().spawnEntity(entity);
    }

    @Override
    public void freeze(PlayerRef target, int ticks) {
        potion(target, "minecraft:slowness", ticks, 6);
        potion(target, "minecraft:jump_boost", ticks, 128);
    }

    @Override
    public void spin(PlayerRef target, int ticks) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return;
        }
        UUID id = player.getUuid();
        int[] elapsed = {0};
        TaskScheduler.Cancellable[] handle = new TaskScheduler.Cancellable[1];
        handle[0] = scheduler.repeating(() -> {
            MinecraftServer instance = server.get();
            ServerPlayerEntity online = instance == null ? null : instance.getPlayerManager().getPlayer(id);
            if (online == null || elapsed[0] >= ticks) {
                if (handle[0] != null) {
                    handle[0].cancel();
                }
                return;
            }
            float yaw = online.getYaw() + 40f;
            float pitch = (float) (Math.sin(elapsed[0]) * 25);
            online.networkHandler.requestTeleport(online.getX(), online.getY(), online.getZ(), yaw, pitch);
            elapsed[0] += 2;
        }, 2L);
    }

    @Override
    public void swapInventory(PlayerRef first, PlayerRef second) {
        ServerPlayerEntity a = player(first);
        ServerPlayerEntity b = player(second);
        if (a == null || b == null) {
            return;
        }
        int size = Math.min(a.getInventory().size(), b.getInventory().size());
        for (int slot = 0; slot < size; slot++) {
            ItemStack temp = a.getInventory().getStack(slot);
            a.getInventory().setStack(slot, b.getInventory().getStack(slot));
            b.getInventory().setStack(slot, temp);
        }
        a.playerScreenHandler.sendContentUpdates();
        b.playerScreenHandler.sendContentUpdates();
    }

    @Override
    public void scrambleInventory(PlayerRef target) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return;
        }
        int size = player.getInventory().size();
        List<ItemStack> all = new ArrayList<>();
        for (int slot = 0; slot < size; slot++) {
            all.add(player.getInventory().getStack(slot));
        }
        Collections.shuffle(all);
        for (int slot = 0; slot < size; slot++) {
            player.getInventory().setStack(slot, all.get(slot));
        }
        player.playerScreenHandler.sendContentUpdates();
    }

    @Override
    public void weatherStorm(PlayerRef target, int ticks) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return;
        }
        ServerWorld world = player.getEntityWorld();
        world.setWeather(0, ticks, true, true);
        world.setTimeOfDay(18000);
    }

    @Override
    public void hideName(PlayerRef target, int ticks) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return;
        }
        MinecraftServer instance = server.get();
        if (instance == null) {
            return;
        }
        Scoreboard scoreboard = instance.getScoreboard();
        Team team = hiddenTeam(scoreboard);
        String name = player.getGameProfile().name();
        scoreboard.addScoreHolderToTeam(name, team);
        scheduler.later(() -> {
            Team current = scoreboard.getTeam("lt_hidden");
            if (current != null) {
                scoreboard.removeScoreHolderFromTeam(name, current);
            }
        }, ticks);
    }

    @Override
    public void anonymize(PlayerRef target, int ticks) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return;
        }
        hideName(target, ticks);
        com.mojang.authlib.properties.PropertyMap properties = player.getGameProfile().properties();
        java.util.Collection<com.mojang.authlib.properties.Property> saved =
                new ArrayList<>(properties.get("textures"));
        properties.removeAll("textures");
        refresh(player);
        UUID id = player.getUuid();
        scheduler.later(() -> {
            MinecraftServer instance = server.get();
            ServerPlayerEntity online = instance == null ? null : instance.getPlayerManager().getPlayer(id);
            if (online == null) {
                return;
            }
            com.mojang.authlib.properties.PropertyMap props = online.getGameProfile().properties();
            props.removeAll("textures");
            for (com.mojang.authlib.properties.Property property : saved) {
                props.put("textures", property);
            }
            refresh(online);
        }, ticks);
    }

    private void refresh(ServerPlayerEntity player) {
        MinecraftServer instance = server.get();
        if (instance == null) {
            return;
        }
        instance.getPlayerManager().sendToAll(new net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket(List.of(player.getUuid())));
        instance.getPlayerManager().sendToAll(net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.entryFromPlayer(List.of(player)));
    }

    @Override
    public void title(PlayerRef target, Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return;
        }
        player.networkHandler.sendPacket(new TitleFadeS2CPacket(fadeIn, stay, fadeOut));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(FabricText.toNative(subtitle)));
        player.networkHandler.sendPacket(new TitleS2CPacket(FabricText.toNative(title)));
    }

    @Override
    public String wipeColumn(PlayerRef target, int radius) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return "";
        }
        ServerWorld world = player.getEntityWorld();
        List<Snapshot> saved = new ArrayList<>();
        int baseX = player.getBlockX();
        int baseZ = player.getBlockZ();
        int topY = player.getBlockY();
        int minY = world.getBottomY() + 1;
        for (int x = baseX - radius; x <= baseX + radius; x++) {
            for (int z = baseZ - radius; z <= baseZ + radius; z++) {
                for (int y = minY; y <= topY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (world.getBlockState(pos).isAir()) {
                        continue;
                    }
                    saved.add(new Snapshot(world, pos, world.getBlockState(pos)));
                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
                }
            }
        }
        String token = UUID.randomUUID().toString();
        undo.put(token, saved);
        return token;
    }

    @Override
    public void wipeChunk(PlayerRef target) {
        ServerPlayerEntity player = player(target);
        if (player == null) {
            return;
        }
        ServerWorld world = player.getEntityWorld();
        int baseX = player.getChunkPos().getStartX();
        int baseZ = player.getChunkPos().getStartZ();
        int minY = world.getBottomY() + 1;
        for (int x = baseX; x < baseX + 16; x++) {
            for (int z = baseZ; z < baseZ + 16; z++) {
                int topY = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
                for (int y = minY; y <= topY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!world.getBlockState(pos).isAir()) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }

    @Override
    public void restore(String undoToken) {
        List<Snapshot> saved = undo.remove(undoToken);
        if (saved == null) {
            return;
        }
        for (Snapshot snapshot : saved) {
            snapshot.world().setBlockState(snapshot.pos(), snapshot.state());
        }
    }

    private RegistryEntry<StatusEffect> effect(String key) {
        return Registries.STATUS_EFFECT.getEntry(FabricText.id(key)).map(entry -> (RegistryEntry<StatusEffect>) entry).orElse(null);
    }

    private Team hiddenTeam(Scoreboard scoreboard) {
        Team team = scoreboard.getTeam("lt_hidden");
        if (team == null) {
            team = scoreboard.addTeam("lt_hidden");
        }
        team.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.NEVER);
        return team;
    }
}
