package org.imdel.letstroll.tool;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

import org.imdel.letstroll.stand.StandCommand;

public class ToolCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;

    public ToolCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private Set<String> getStandPresets() {
        return StandCommand.getStandKeys();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Использование: /tool stand <preset> | unbind | bind <action>");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        ItemStack item = player.getInventory().getItemInMainHand();

        switch (sub) {
            case "stand" -> {
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Использование: /tool stand <preset>");
                    return true;
                }
                String preset = args[1];
                if (!getStandPresets().contains(preset)) {
                    player.sendMessage(ChatColor.RED + "Стенд '" + preset + "' не найден.");
                    return true;
                }
                var meta = item.getItemMeta();
                if (meta == null) {
                    player.sendMessage(ChatColor.RED + "Нельзя привязать этот предмет.");
                    return true;
                }
                meta.getPersistentDataContainer().set(ToolListener.KEY, PersistentDataType.STRING, "stand:" + preset);
                item.setItemMeta(meta);
                player.sendMessage(ChatColor.GREEN + "Привязан стенд: " + preset);
                return true;
            }

            case "unbind" -> {
                var meta = item.getItemMeta();
                if (meta == null) {
                    player.sendMessage(ChatColor.RED + "Нет метаданных.");
                    return true;
                }
                meta.getPersistentDataContainer().remove(ToolListener.KEY);
                item.setItemMeta(meta);
                player.sendMessage(ChatColor.GREEN + "Привязка снята.");
                return true;
            }

            case "bind" -> {
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Использование: /tool bind <action>");
                    return true;
                }

                var meta = item.getItemMeta();
                if (meta == null) return false;
                String val = args[1].toLowerCase(Locale.ROOT);

                if (List.of("lightning", "explosion", "tnt", "tnt_long", "end_crystal", "fake_item", "fall_fake").contains(val)) {
                    meta.getPersistentDataContainer().set(ToolListener.KEY, PersistentDataType.STRING, "action:" + val);
                    item.setItemMeta(meta);
                    player.sendMessage(ChatColor.GREEN + "Привязано действие: " + val);
                    return true;
                }
                player.sendMessage(ChatColor.RED + "Действие '" + val + "' не распознан.");
                return true;

            }

            default -> {
                player.sendMessage(ChatColor.RED + "Неизвестная подкоманда.");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("bind", "unbind", "stand").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String arg = args[1].toLowerCase();

            if (sub.equals("stand")) {
                return getStandPresets().stream()
                        .filter(s -> s.startsWith(arg))
                        .toList();
            }

            if (sub.equals("bind")) {
                List<String> effects = List.of("lightning", "explosion", "tnt", "tnt_long", "end_crystal", "fake_item", "fall_fake");
                return effects.stream().filter(s -> s.startsWith(arg)).toList();
            }
        }

        return Collections.emptyList();
    }
}