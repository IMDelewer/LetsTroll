package org.imdel.letstroll.stand;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import org.imdel.letstroll.LetsTroll;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class StandCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    public static YamlConfiguration standConfig;

    public StandCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static void reloadStands() {
        File standFile = new File(LetsTroll.getInstance().getDataFolder(), "stands.yml");
        standConfig = YamlConfiguration.loadConfiguration(standFile);
    }

    public static Set<String> getStandKeys() {
        return standConfig.getKeys(false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadStands();
            sender.sendMessage(ChatColor.GREEN + "Стенды перезагружены из stands.yml");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "/stand_conf <имя_стенда> <ник_игрока> или /stand_conf reload");
            return true;
        }

        String standName = args[0];
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден.");
            return true;
        }

        ConfigurationSection stand_conf = standConfig.getConfigurationSection(standName);
        if (stand_conf == null) {
            sender.sendMessage(ChatColor.RED + "Стенд не найден в stands.yml");
            return true;
        }

        Vector direction = target.getLocation().getDirection().normalize().multiply(1);
        Vector spawnPos = target.getLocation().toVector().add(direction);
        Location spawnLocation = spawnPos.toLocation(target.getWorld());

        Vector lookAtPlayer = target.getLocation().toVector().subtract(spawnLocation.toVector());
        spawnLocation.setDirection(lookAtPlayer);

        ArmorStand stand = target.getWorld().spawn(spawnLocation, ArmorStand.class);
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

        String skinUrl = stand_conf.getString("skin");
        if (skinUrl != null && !skinUrl.isEmpty()) {
            ItemStack head = createCustomHead(skinUrl);
            stand.setItem(EquipmentSlot.HEAD, head);
        }

        // Цветная броня
        ItemStack chest = parseArmor(stand_conf, "chestplate");
        ItemStack legs = parseArmor(stand_conf, "leggings");
        ItemStack boots = parseArmor(stand_conf, "boots");
        stand.setItem(EquipmentSlot.CHEST, chest);
        stand.setItem(EquipmentSlot.LEGS, legs);
        stand.setItem(EquipmentSlot.FEET, boots);


        new BukkitRunnable() {
            @Override
            public void run() {
                stand.remove();
            }
        }.runTaskLater(plugin, 5);

        return true;
    }

    public static ItemStack parseArmor(ConfigurationSection stand, String part) {
        String materialName = stand.getString(part);
        if (materialName == null || materialName.isEmpty()) return null;

        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            ItemStack item = new ItemStack(material);

            if (material.name().startsWith("LEATHER_")) {
                String colorHex = stand.getString("color_" + part);
                if (colorHex != null) {
                    try {
                        Color color = Color.fromRGB(Integer.parseInt(colorHex.replace("#", ""), 16));
                        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
                        meta.setColor(color);
                        item.setItemMeta(meta);
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("Неверный цвет для " + part + ": " + colorHex);
                    }
                }
            }

            return item;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private ItemStack createCustomHead(String url) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        try {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(url));
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        head.setItemMeta(meta);
        return head;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> keys = new ArrayList<>(getStandKeys());
            keys.add("reload");
            return keys;
        }
        if (args.length == 2) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                names.add(p.getName());
            }
            return names;
        }
        return Collections.emptyList();
    }
}
