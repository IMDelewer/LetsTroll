package org.imdel.letstroll.tool;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.imdel.letstroll.stand.StandCommand;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ToolListener implements Listener {
    public static final NamespacedKey KEY = new NamespacedKey("letstroll", "bound_stand");
    private final JavaPlugin plugin;

    // Добавил Set для отслеживания "падающих" игроков
    private final Set<UUID> fallingPlayers = new HashSet<>();

    public ToolListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || !event.getAction().toString().contains("LEFT_CLICK")) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) return;

        var meta = item.getItemMeta();
        if (meta == null) return;

        String bound = meta.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
        if (bound == null || bound.isEmpty()) return;

        event.setCancelled(true);

        if (bound.startsWith("stand:")) {
            String preset = bound.substring("stand:".length());
            spawnStandAtPlayerView(player, preset);
        } else if (bound.startsWith("action:")) {
            String action = bound.substring("action:".length());
            switch (action) {
                case "lightning" -> strikeLightning(player);
                case "explosion" -> spawnFakeExplosion(player);
                case "tnt" -> spawnFakeTNT(player, 40);
                case "tnt_long" -> spawnFakeTNT(player, 200);
                case "end_crystal" -> spawnEndCrystal(player);
                case "fake_item" -> {
                    Entity target = getTargetEntity(player, 50);
                    if (target instanceof Player targetPlayer) {
                        spawnFakeItem(targetPlayer);
                    } else {
                        player.sendMessage(ChatColor.RED + "No player in cursor");
                    }
                }
                case "fall_fake" -> {
                    Entity target = getTargetEntity(player, 50);
                    if (target instanceof Player targetPlayer) {
                        simulateFall(targetPlayer);
                    } else {
                        player.sendMessage(ChatColor.RED + "No player in cursor");
                    }
                }
            }
        }
    }

    private Entity getTargetEntity(Player player, int maxDistance) {
        List<Entity> nearbyEntities = player.getNearbyEntities(maxDistance, maxDistance, maxDistance);
        Vector direction = player.getEyeLocation().getDirection().normalize();
        Location eyeLoc = player.getEyeLocation();

        Entity closest = null;
        double closestDistance = maxDistance + 1;

        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Player)) continue;

            Location entityLoc = entity.getLocation().clone().add(0, entity.getHeight() / 2.0, 0);
            Vector toEntity = entityLoc.toVector().subtract(eyeLoc.toVector());
            double dot = toEntity.normalize().dot(direction);
            if (dot < 0.95) continue;

            double distance = eyeLoc.distance(entityLoc);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = entity;
            }
        }

        return closest;
    }

    public void spawnStandAtPlayerView(Player player, String preset) {
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize().multiply(1.2);
        Location spawnLoc = eyeLoc.clone().add(direction).subtract(0, 0.5, 0);
        Location facingLoc = spawnLoc.clone();
        facingLoc.setDirection(player.getEyeLocation().toVector().subtract(spawnLoc.toVector()).normalize());

        var standConfig = StandCommand.standConfig;
        var stand_conf = standConfig.getConfigurationSection(preset);
        if (stand_conf == null) {
            player.sendMessage(ChatColor.RED + "Stand not found.");
            return;
        }

        ArmorStand stand = player.getWorld().spawn(facingLoc, ArmorStand.class);
        stand.setVisible(false);
        stand.setCustomName(stand_conf.getString("name", null));
        stand.setCustomNameVisible(true);
        stand.setArms(true);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setCanPickupItems(false);
        stand.setInvulnerable(true);
        stand.setPersistent(true);
        stand.setRemoveWhenFarAway(false);
        stand.setCollidable(false);
        stand.setSilent(true);
        stand.setMarker(false);

        stand.setItem(EquipmentSlot.HEAD, new ItemStack(Material.PLAYER_HEAD));
        stand.setItem(EquipmentSlot.CHEST, StandCommand.parseArmor(stand_conf, "chestplate"));
        stand.setItem(EquipmentSlot.LEGS, StandCommand.parseArmor(stand_conf, "leggings"));
        stand.setItem(EquipmentSlot.FEET, StandCommand.parseArmor(stand_conf, "boots"));
    }

    private void strikeLightning(Player player) {
        Location loc = player.getTargetBlockExact(50) != null
                ? player.getTargetBlockExact(50).getLocation()
                : player.getLocation().add(player.getLocation().getDirection().multiply(5));
        player.getWorld().strikeLightningEffect(loc);
    }

    private void spawnFakeExplosion(Player player) {
        Block targetBlock = player.getTargetBlockExact(30);
        if (targetBlock == null) {
            player.sendMessage(ChatColor.RED + "No block on cursor.");
            return;
        }

        Location loc = targetBlock.getLocation().add(0.5, 1, 0.5);

        World world = player.getWorld();
        world.spawnParticle(Particle.EXPLOSION, loc, 1);
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
    }


    private void spawnEndCrystal(Player player) {
        Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(2)).add(0, 1, 0);
        EnderCrystal crystal = (EnderCrystal) player.getWorld().spawnEntity(loc, EntityType.END_CRYSTAL);
        crystal.setShowingBottom(false);
        crystal.setInvulnerable(true);
        crystal.setBeamTarget(null);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!crystal.isDead()) {
                    crystal.remove();
                }
            }
        }.runTaskLater(plugin, 20L);
    }

    private void spawnFakeTNT(Player player, int ticks) {
        Block targetBlock = player.getTargetBlockExact(30);
        if (targetBlock == null) {
            player.sendMessage(ChatColor.RED + "No block on cursor.");
            return;
        }

        Location spawnLoc = targetBlock.getLocation().add(0.5, 1, 0.5);
        var world = player.getWorld();

        TNTPrimed tnt = (TNTPrimed) world.spawnEntity(spawnLoc, EntityType.TNT);
        tnt.setFuseTicks(ticks);
        tnt.setYield(0);
        tnt.setIsIncendiary(false);
        tnt.setMetadata("letstroll_fake", new FixedMetadataValue(plugin, true));
    }

    private void spawnFakeItem(Player player) {
        Location loc = player.getLocation().add(player.getLocation().getDirection().normalize().multiply(2));
        World world = player.getWorld();

        Item item = world.dropItem(loc, new ItemStack(Material.DIAMOND));
        item.setPickupDelay(Integer.MAX_VALUE); // Запретить подбор
        item.setGlowing(true);
        item.setInvulnerable(true);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!item.isDead()) {
                    item.remove();
                    world.spawnParticle(Particle.EXPLOSION, item.getLocation(), 1);
                    world.playSound(item.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
                }
            }
        }.runTaskLater(plugin, 40L);
    }

    private void simulateFall(Player player) {
        Location start = player.getLocation().clone();
        Location up = start.clone().add(0, 10, 0);

        fallingPlayers.add(player.getUniqueId());

        player.teleport(up);
        player.setFallDistance(0);

        new BukkitRunnable() {
            @Override
            public void run() {
                player.setVelocity(new Vector(0, -0.1, 0));
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.6f);
            }
        }.runTaskLater(plugin, 5L);

        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(start);
                player.getWorld().playSound(start, Sound.BLOCK_SLIME_BLOCK_PLACE, 1f, 1.2f);
                player.spawnParticle(Particle.CLOUD, start, 10, 0.2, 0.2, 0.2, 0.01);

                fallingPlayers.remove(player.getUniqueId());
            }
        }.runTaskLater(plugin, 40L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (fallingPlayers.contains(player.getUniqueId())) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
