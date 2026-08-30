package dev.delewer.letstroll.paper;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import dev.delewer.letstroll.platform.StealthOptions;
import dev.delewer.letstroll.text.Text;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.block.Action;

public final class StealthListener implements Listener {

    private static final List<String> LIST_COMMANDS = List.of("/list", "/who", "/playerlist", "/plist");
    private static final Set<String> MESSAGE_COMMANDS = Set.of(
            "msg", "tell", "w", "whisper", "pm", "m", "t", "r", "reply", "me", "say", "teammsg", "tm");

    private final PaperStealth stealth;

    public StealthListener(PaperStealth stealth) {
        this.stealth = stealth;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        stealth.applyExistingTo(player);
        stealth.optionsOf(player.getUniqueId()).ifPresent(options -> {
            stealth.hide(new PaperPlayerRef(player), options);
            if (options.silentJoinQuit()) {
                event.joinMessage(null);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        options(event.getPlayer().getUniqueId())
                .filter(StealthOptions::silentJoinQuit)
                .ifPresent(options -> event.quitMessage(null));
        stealth.handleQuit(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        StealthOptions options = options(event.getPlayer().getUniqueId()).orElse(null);
        if (options == null) {
            return;
        }
        if (options.muteChat()) {
            event.setCancelled(true);
            Text.send(new PaperPlayerRef(event.getPlayer()), "ghost.muted");
            return;
        }
        if (options.hideChat()) {
            UUID speaker = event.getPlayer().getUniqueId();
            event.viewers().removeIf(audience -> audience instanceof Player player
                    && !player.getUniqueId().equals(speaker));
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMessageCommand(PlayerCommandPreprocessEvent event) {
        if (options(event.getPlayer().getUniqueId()).filter(StealthOptions::muteChat).isEmpty()) {
            return;
        }
        if (!MESSAGE_COMMANDS.contains(label(event.getMessage()))) {
            return;
        }
        event.setCancelled(true);
        Text.send(new PaperPlayerRef(event.getPlayer()), "ghost.muted");
    }

    private String label(String message) {
        String command = message.substring(1).trim().split("\\s+")[0].toLowerCase(Locale.ROOT);
        int colon = command.indexOf(':');
        return colon < 0 ? command : command.substring(colon + 1);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        options(event.getEntity().getUniqueId())
                .filter(StealthOptions::hideChat)
                .ifPresent(options -> event.deathMessage(null));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        options(event.getPlayer().getUniqueId())
                .filter(StealthOptions::hideChat)
                .ifPresent(options -> event.message(null));
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player
                && options(player.getUniqueId()).filter(StealthOptions::mobsIgnore).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player
                && options(player.getUniqueId()).filter(StealthOptions::invulnerable).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player
                && options(player.getUniqueId()).filter(StealthOptions::noHunger).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player
                && options(player.getUniqueId()).filter(StealthOptions::ignoreWorld).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPhysical(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL
                && options(event.getPlayer().getUniqueId()).filter(StealthOptions::ignoreWorld).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBed(PlayerBedEnterEvent event) {
        if (options(event.getPlayer().getUniqueId()).filter(StealthOptions::ignoreWorld).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onListCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().trim().toLowerCase(Locale.ROOT).split(" ")[0];
        if (!LIST_COMMANDS.contains(command) || stealth.hiddenPlayers().stream().noneMatch(this::hiddenFromLists)) {
            return;
        }
        List<String> names = Bukkit.getOnlinePlayers().stream()
                .filter(player -> !hiddenFromLists(player.getUniqueId()))
                .map(Player::getName)
                .toList();
        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("There are " + names.size() + " of a max of "
                        + Bukkit.getMaxPlayers() + " players online: " + String.join(", ", names))
                .color(NamedTextColor.GRAY));
    }

    @EventHandler
    public void onPing(PaperServerListPingEvent event) {
        Set<UUID> hidden = stealth.hiddenPlayers().stream()
                .filter(this::hiddenFromLists)
                .collect(Collectors.toSet());
        if (hidden.isEmpty()) {
            return;
        }
        event.getListedPlayers().removeIf(entry -> hidden.contains(entry.id()));
        long online = Bukkit.getOnlinePlayers().stream()
                .filter(player -> hidden.contains(player.getUniqueId()))
                .count();
        event.setNumPlayers(Math.max(0, event.getNumPlayers() - (int) online));
    }

    private Optional<StealthOptions> options(UUID id) {
        return stealth.optionsOf(id);
    }

    private boolean hiddenFromLists(UUID id) {
        return options(id).filter(StealthOptions::hideFromList).isPresent();
    }
}
