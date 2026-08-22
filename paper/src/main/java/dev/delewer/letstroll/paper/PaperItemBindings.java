package dev.delewer.letstroll.paper;

import java.util.Optional;
import java.util.function.Supplier;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.menu.ClickContext;
import dev.delewer.letstroll.menu.ClickKind;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.platform.ItemBindingService;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.player.PlayerAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperItemBindings implements ItemBindingService, Listener {

    private static final String LORE_PREFIX = "LetsTroll: ";

    private final NamespacedKey key;
    private final Supplier<LetsTroll> core;

    public PaperItemBindings(JavaPlugin plugin, Supplier<LetsTroll> core) {
        this.key = new NamespacedKey(plugin, "bound_action");
        this.core = core;
    }

    @Override
    public boolean bindHeldItem(PlayerRef holder, String actionId) {
        Player player = (Player) holder.handle();
        if (player == null) {
            return false;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, actionId);
        java.util.List<Component> lore = withoutMarker(meta);
        lore.add(Component.text(LORE_PREFIX + actionId, NamedTextColor.LIGHT_PURPLE));
        meta.lore(lore);
        item.setItemMeta(meta);
        return true;
    }

    @Override
    public boolean unbindHeldItem(PlayerRef holder) {
        Player player = (Player) holder.handle();
        if (player == null) {
            return false;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return false;
        }
        meta.getPersistentDataContainer().remove(key);
        java.util.List<Component> lore = withoutMarker(meta);
        meta.lore(lore.isEmpty() ? null : lore);
        item.setItemMeta(meta);
        return true;
    }

    private java.util.List<Component> withoutMarker(ItemMeta meta) {
        java.util.List<Component> lore = meta.lore();
        if (lore == null) {
            return new java.util.ArrayList<>();
        }
        java.util.List<Component> kept = new java.util.ArrayList<>(lore);
        kept.removeIf(line -> PlainTextComponentSerializer.plainText().serialize(line).startsWith(LORE_PREFIX));
        return kept;
    }

    @Override
    public Optional<String> heldBinding(PlayerRef holder) {
        Player player = (Player) holder.handle();
        if (player == null) {
            return Optional.empty();
        }
        return readBinding(player.getInventory().getItemInMainHand());
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player target)) {
            return;
        }
        Player holder = event.getPlayer();
        ItemStack item = holder.getInventory().getItem(event.getHand());
        Optional<String> binding = readBinding(item);
        if (binding.isEmpty()) {
            return;
        }
        LetsTroll instance = core.get();
        if (instance == null || !holder.hasPermission(LetsTroll.PERMISSION_USE)) {
            return;
        }
        PlayerAction action = instance.playerActions().all().stream()
                .filter(candidate -> candidate.id().equals(binding.get()))
                .findFirst()
                .orElse(null);
        if (action == null) {
            return;
        }
        event.setCancelled(true);
        PlayerRef holderRef = new PaperPlayerRef(holder);
        PlayerRef targetRef = new PaperPlayerRef(target);
        ClickContext context = new ClickContext(instance, holderRef, ClickKind.RIGHT,
                ScreenRequest.of("bindings"), instance.router());
        action.run(context, targetRef);
    }

    private Optional<String> readBinding(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return Optional.empty();
        }
        String value = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return Optional.ofNullable(value);
    }
}
