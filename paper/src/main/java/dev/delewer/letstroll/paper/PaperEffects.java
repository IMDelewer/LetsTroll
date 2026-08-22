package dev.delewer.letstroll.paper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import dev.delewer.letstroll.platform.EffectsService;
import dev.delewer.letstroll.platform.PlayerRef;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

public final class PaperEffects implements EffectsService {

    private record Snapshot(World world, int x, int y, int z, BlockData data) {
    }

    private static final List<Material> GIFTS = List.of(
            Material.DIAMOND, Material.EMERALD, Material.GOLDEN_APPLE, Material.NETHERITE_INGOT,
            Material.ENCHANTED_GOLDEN_APPLE, Material.EXPERIENCE_BOTTLE, Material.TOTEM_OF_UNDYING);
    private static final List<Material> JUNK = List.of(
            Material.DIRT, Material.COBBLESTONE, Material.ROTTEN_FLESH, Material.STICK,
            Material.POISONOUS_POTATO, Material.WHEAT_SEEDS, Material.GRAVEL);

    private final JavaPlugin plugin;
    private final dev.ua.theroer.magicutils.logger.PrefixedLogger log;
    private final Map<String, List<Snapshot>> undo = new ConcurrentHashMap<>();

    public PaperEffects(JavaPlugin plugin, dev.ua.theroer.magicutils.logger.PrefixedLogger log) {
        this.plugin = plugin;
        this.log = log;
    }

    @Override
    public void potion(PlayerRef target, String effectKey, int durationTicks, int amplifier) {
        Player player = player(target);
        if (player == null) {
            return;
        }
        PotionEffectType type = Registry.EFFECT.get(key(effectKey));
        if (type != null) {
            player.addPotionEffect(new PotionEffect(type, durationTicks, Math.max(0, amplifier), false, false, true));
        }
    }

    @Override
    public void lightning(PlayerRef target, boolean cosmetic) {
        Player player = player(target);
        if (player == null) {
            return;
        }
        if (cosmetic) {
            player.getWorld().strikeLightningEffect(player.getLocation());
        } else {
            player.getWorld().strikeLightning(player.getLocation());
        }
    }

    @Override
    public void explosion(PlayerRef target, float power, boolean breakBlocks, boolean damage) {
        Player player = player(target);
        if (player != null) {
            player.getWorld().createExplosion(player.getLocation(), power, false, breakBlocks, player);
        }
    }

    @Override
    public void sound(PlayerRef target, String sound, float volume, float pitch) {
        Player player = player(target);
        if (player != null) {
            player.playSound(Sound.sound(Key.key(sound), Sound.Source.MASTER, volume, pitch));
        }
    }

    @Override
    public void spawnMobs(PlayerRef target, String entityType, int count, double radius) {
        Player player = player(target);
        if (player == null) {
            return;
        }
        EntityType type = Registry.ENTITY_TYPE.get(key(entityType));
        if (type == null) {
            return;
        }
        World world = player.getWorld();
        for (int index = 0; index < count; index++) {
            double angle = 2 * Math.PI * index / count;
            Location spot = player.getLocation().clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            world.spawnEntity(spot, type);
        }
    }

    @Override
    public void broadcast(Component message) {
        Bukkit.getServer().sendMessage(message);
    }

    @Override
    public void launch(PlayerRef target, double power) {
        Player player = player(target);
        if (player != null) {
            player.setVelocity(player.getVelocity().add(new Vector(0, power, 0)));
        }
    }

    @Override
    public void teleportRandom(PlayerRef target, double radius) {
        Player player = player(target);
        if (player == null) {
            return;
        }
        double dx = (ThreadLocalRandom.current().nextDouble() * 2 - 1) * radius;
        double dz = (ThreadLocalRandom.current().nextDouble() * 2 - 1) * radius;
        Location spot = player.getLocation().clone().add(dx, 0, dz);
        spot.setY(player.getWorld().getHighestBlockYAt(spot) + 1);
        player.teleportAsync(spot);
    }

    @Override
    public void teleportTo(PlayerRef target, PlayerRef destination) {
        Player player = player(target);
        Player to = player(destination);
        if (player != null && to != null) {
            player.teleportAsync(to.getLocation());
        }
    }

    @Override
    public void giveItem(PlayerRef target, String materialKey, int amount) {
        Player player = player(target);
        if (player == null) {
            return;
        }
        Material material = materialKey == null || materialKey.isBlank()
                ? GIFTS.get(ThreadLocalRandom.current().nextInt(GIFTS.size()))
                : Material.matchMaterial(materialKey);
        if (material == null) {
            return;
        }
        player.getInventory().addItem(new ItemStack(material, Math.max(1, amount)));
    }

    @Override
    public void dropItems(PlayerRef target, int count) {
        Player player = player(target);
        if (player == null) {
            return;
        }
        World world = player.getWorld();
        for (int index = 0; index < count; index++) {
            Material material = JUNK.get(ThreadLocalRandom.current().nextInt(JUNK.size()));
            Location spot = player.getLocation().clone().add(
                    ThreadLocalRandom.current().nextDouble() * 2 - 1, 1.5,
                    ThreadLocalRandom.current().nextDouble() * 2 - 1);
            world.dropItemNaturally(spot, new ItemStack(material));
        }
    }

    @Override
    public void heal(PlayerRef target) {
        Player player = player(target);
        if (player == null) {
            return;
        }
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setFireTicks(0);
    }

    @Override
    public void firework(PlayerRef target) {
        Player player = player(target);
        if (player == null) {
            return;
        }
        Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .withColor(Color.fromRGB(ThreadLocalRandom.current().nextInt(0xFFFFFF)))
                .withFade(Color.fromRGB(ThreadLocalRandom.current().nextInt(0xFFFFFF)))
                .with(FireworkEffect.Type.BALL_LARGE)
                .flicker(true)
                .trail(true)
                .build());
        meta.setPower(1);
        firework.setFireworkMeta(meta);
    }

    @Override
    public void freeze(PlayerRef target, int ticks) {
        Player player = player(target);
        if (player == null) {
            return;
        }
        float walk = player.getWalkSpeed();
        player.setWalkSpeed(0f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, 6, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, ticks, 128, false, false, false));
        UUID id = player.getUniqueId();
        PaperTasks.onEntityLater(plugin, player, () -> {
            Player online = Bukkit.getPlayer(id);
            if (online != null) {
                online.setWalkSpeed(walk == 0f ? 0.2f : walk);
            }
        }, ticks);
    }

    @Override
    public void spin(PlayerRef target, int ticks) {
        Player player = player(target);
        if (player == null) {
            return;
        }
        UUID id = player.getUniqueId();
        int[] elapsed = {0};
        player.getScheduler().runAtFixedRate(plugin, task -> {
            Player online = Bukkit.getPlayer(id);
            if (online == null || elapsed[0] >= ticks) {
                task.cancel();
                return;
            }
            Location location = online.getLocation();
            location.setYaw(location.getYaw() + 40f);
            location.setPitch((float) (Math.sin(elapsed[0]) * 25));
            online.setRotation(location.getYaw(), location.getPitch());
            elapsed[0] += 2;
        }, null, 1L, 2L);
    }

    @Override
    public void swapInventory(PlayerRef first, PlayerRef second) {
        Player a = player(first);
        Player b = player(second);
        if (a == null || b == null) {
            return;
        }
        ItemStack[] contentsA = a.getInventory().getContents().clone();
        ItemStack[] contentsB = b.getInventory().getContents().clone();
        a.getInventory().setContents(contentsB);
        b.getInventory().setContents(contentsA);
    }

    @Override
    public void scrambleInventory(PlayerRef target) {
        Player player = player(target);
        if (player == null) {
            return;
        }
        org.bukkit.inventory.PlayerInventory inventory = player.getInventory();
        List<ItemStack> all = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            all.add(inventory.getItem(slot));
        }
        java.util.Collections.shuffle(all);
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, all.get(slot));
        }
        player.updateInventory();
    }

    @Override
    public void weatherStorm(PlayerRef target, int ticks) {
        Player player = player(target);
        if (player == null) {
            return;
        }
        World world = player.getWorld();
        long previousTime = world.getTime();
        world.setStorm(true);
        world.setThundering(true);
        world.setTime(18000);
        PaperTasks.globalLater(plugin, () -> {
            world.setStorm(false);
            world.setThundering(false);
            world.setTime(previousTime);
        }, ticks);
    }

    @Override
    public void hideName(PlayerRef target, int ticks) {
        Player player = player(target);
        if (player == null) {
            return;
        }
        Team team = hiddenTeam();
        team.addEntry(player.getName());
        String name = player.getName();
        PaperTasks.globalLater(plugin, () -> {
            Team current = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("lt_hidden");
            if (current != null) {
                current.removeEntry(name);
            }
        }, ticks);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void anonymize(PlayerRef target, int ticks) {
        Player player = player(target);
        if (player == null) {
            return;
        }
        hideName(target, ticks);
        UUID id = player.getUniqueId();
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object profile = handle.getClass().getMethod("getGameProfile").invoke(handle);
            Object properties = profile.getClass().getMethod("getProperties").invoke(profile);
            java.lang.reflect.Method get = properties.getClass().getMethod("get", Object.class);
            java.lang.reflect.Method removeAll = properties.getClass().getMethod("removeAll", Object.class);
            java.lang.reflect.Method put = properties.getClass().getMethod("put", Object.class, Object.class);

            List<Object> saved = new ArrayList<>((java.util.Collection<Object>) get.invoke(properties, "textures"));
            removeAll.invoke(properties, "textures");
            refreshEntity(player);

            PaperTasks.onEntityLater(plugin, player, () -> {
                Player online = Bukkit.getPlayer(id);
                if (online == null) {
                    return;
                }
                try {
                    Object handle2 = online.getClass().getMethod("getHandle").invoke(online);
                    Object profile2 = handle2.getClass().getMethod("getGameProfile").invoke(handle2);
                    Object properties2 = profile2.getClass().getMethod("getProperties").invoke(profile2);
                    properties2.getClass().getMethod("removeAll", Object.class).invoke(properties2, "textures");
                    for (Object property : saved) {
                        properties2.getClass().getMethod("put", Object.class, Object.class).invoke(properties2, "textures", property);
                    }
                    refreshEntity(online);
                } catch (ReflectiveOperationException | RuntimeException ignored) {
                }
            }, ticks);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            log.debug("Anonymize unavailable: %s", exception.getMessage());
        }
    }

    private void refreshEntity(Player target) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.getUniqueId().equals(target.getUniqueId())) {
                viewer.hidePlayer(plugin, target);
            }
        }
        UUID id = target.getUniqueId();
        PaperTasks.globalLater(plugin, () -> {
            Player online = Bukkit.getPlayer(id);
            if (online == null) {
                return;
            }
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (!viewer.getUniqueId().equals(id)) {
                    viewer.showPlayer(plugin, online);
                }
            }
        }, 2L);
    }

    @Override
    public void title(PlayerRef target, Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        Player player = player(target);
        if (player != null) {
            player.showTitle(Title.title(title, subtitle, Title.Times.times(
                    Duration.ofMillis(fadeIn * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeOut * 50L))));
        }
    }

    @Override
    public String wipeColumn(PlayerRef target, int radius) {
        Player player = player(target);
        if (player == null) {
            return "";
        }
        World world = player.getWorld();
        Location base = player.getLocation();
        List<Snapshot> saved = new ArrayList<>();
        int minY = world.getMinHeight() + 1;
        int baseX = base.getBlockX();
        int baseZ = base.getBlockZ();
        int topY = base.getBlockY();
        for (int x = baseX - radius; x <= baseX + radius; x++) {
            for (int z = baseZ - radius; z <= baseZ + radius; z++) {
                for (int y = minY; y <= topY; y++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType().isAir()) {
                        continue;
                    }
                    saved.add(new Snapshot(world, x, y, z, block.getBlockData()));
                    block.setType(Material.AIR, false);
                }
            }
        }
        String token = UUID.randomUUID().toString();
        undo.put(token, saved);
        return token;
    }

    @Override
    public void wipeChunk(PlayerRef target) {
        Player player = player(target);
        if (player == null) {
            return;
        }
        World world = player.getWorld();
        Chunk chunk = player.getLocation().getChunk();
        int minY = world.getMinHeight() + 1;
        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;
        for (int x = baseX; x < baseX + 16; x++) {
            for (int z = baseZ; z < baseZ + 16; z++) {
                int topY = world.getHighestBlockYAt(x, z);
                for (int y = minY; y <= topY; y++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!block.getType().isAir()) {
                        block.setType(Material.AIR, false);
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
            snapshot.world().getBlockAt(snapshot.x(), snapshot.y(), snapshot.z()).setBlockData(snapshot.data(), false);
        }
    }

    private Team hiddenTeam() {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("lt_hidden");
        if (team == null) {
            team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("lt_hidden");
        }
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        return team;
    }

    private Player player(PlayerRef ref) {
        return ref == null ? null : (Player) ref.handle();
    }

    private NamespacedKey key(String value) {
        NamespacedKey namespaced = NamespacedKey.fromString(value);
        return namespaced == null ? NamespacedKey.minecraft(value) : namespaced;
    }
}
