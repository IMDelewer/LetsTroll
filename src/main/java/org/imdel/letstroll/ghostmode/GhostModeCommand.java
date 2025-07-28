
package org.imdel.letstroll.ghostmode;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GhostModeCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public GhostModeCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            return true;
        }

        if (!player.hasPermission("ghost.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (GhostStorage.isGhost(player.getUniqueId())) {
            disableGhostMode(player);
            GhostStorage.removeGhost(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "Ghost mode disabled.");
        } else {
            enableGhostMode(player);
            GhostStorage.addGhost(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Ghost mode enabled.");
        }


        return true;
    }

    static void enableGhostMode(Player player) {
        player.setGameMode(GameMode.CREATIVE);
        player.setInvisible(true);
        player.setSilent(true);
        player.setCollidable(false);
        player.setCanPickupItems(false);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
    }

    public static void disableGhostMode(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setInvisible(false);
        player.setSilent(false);
        player.setCollidable(true);
        player.setCanPickupItems(true);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }
}
