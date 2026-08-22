package dev.delewer.letstroll.paper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.menu.ClickContext;
import dev.delewer.letstroll.menu.ClickKind;
import dev.delewer.letstroll.menu.Menu;
import dev.delewer.letstroll.menu.MenuButton;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.platform.MenuBackend;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.ua.theroer.magicutils.gui.MagicGui;
import dev.ua.theroer.magicutils.gui.MagicItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperMenus implements MenuBackend, Listener {

    private record OpenMenu(MagicGui gui, Component title, int size, Set<Integer> slots) {
    }

    private final JavaPlugin plugin;
    private final Supplier<LetsTroll> core;
    private final Map<UUID, OpenMenu> open = new ConcurrentHashMap<>();

    public PaperMenus(JavaPlugin plugin, Supplier<LetsTroll> core) {
        this.plugin = plugin;
        this.core = core;
    }

    @Override
    public void open(PlayerRef viewer, Menu menu) {
        Player player = (Player) viewer.handle();
        if (player == null) {
            return;
        }
        OpenMenu current = open.get(viewer.id());
        if (canUpdateInPlace(current, menu, player)) {
            fill(current.gui(), viewer, menu);
            current.gui().refresh();
            return;
        }

        MagicGui gui = new MagicGui(plugin, player, menu.size(), menu.title());
        fill(gui, viewer, menu);
        open.put(viewer.id(), new OpenMenu(gui, menu.title(), menu.size(), Set.copyOf(menu.buttons().keySet())));
        gui.open();
    }

    @Override
    public void update(PlayerRef viewer, Menu menu) {
        Player player = (Player) viewer.handle();
        if (player == null) {
            return;
        }
        OpenMenu current = open.get(viewer.id());
        if (!canUpdateInPlace(current, menu, player)) {
            return;
        }
        fill(current.gui(), viewer, menu);
        current.gui().refresh();
    }

    @Override
    public void close(PlayerRef viewer) {
        open.remove(viewer.id());
        Player player = (Player) viewer.handle();
        if (player != null) {
            player.closeInventory();
        }
    }

    public void forget(UUID viewer) {
        open.remove(viewer);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        OpenMenu tracked = open.get(id);
        if (tracked == null) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(id);
            OpenMenu still = open.get(id);
            if (player == null || still == null) {
                dropSession(id);
                return;
            }
            if (!player.getOpenInventory().getTopInventory().equals(still.gui().getInventory())) {
                dropSession(id);
            }
        });
    }

    private void dropSession(UUID id) {
        open.remove(id);
        LetsTroll instance = core.get();
        if (instance != null) {
            instance.router().forget(id);
        }
    }

    private boolean canUpdateInPlace(OpenMenu current, Menu menu, Player player) {
        return current != null
                && current.size() == menu.size()
                && Objects.equals(current.title(), menu.title())
                && current.slots().equals(menu.buttons().keySet())
                && player.getOpenInventory().getTopInventory().equals(current.gui().getInventory());
    }

    private void fill(MagicGui gui, PlayerRef viewer, Menu menu) {
        for (Map.Entry<Integer, MenuButton> entry : menu.buttons().entrySet()) {
            MenuButton button = entry.getValue();
            ItemStack item = toItem(button);
            if (button.clickable()) {
                gui.setItem(entry.getKey(), item, event -> handleClick(viewer, button, event));
            } else {
                gui.setItem(entry.getKey(), item);
            }
        }
        menu.filler().ifPresent(icon -> gui.fillEmpty(toFiller(icon)));
    }

    private void handleClick(PlayerRef viewer, MenuButton button, InventoryClickEvent event) {
        event.setCancelled(true);
        LetsTroll instance = core.get();
        if (instance == null) {
            return;
        }
        ScreenRequest request = instance.router().current(viewer.id()).orElse(null);
        if (request == null) {
            return;
        }
        button.click(new ClickContext(instance, viewer, kindOf(event.getClick()), request, instance.router()));
    }

    private ClickKind kindOf(ClickType type) {
        return switch (type) {
            case LEFT, DOUBLE_CLICK -> ClickKind.LEFT;
            case RIGHT -> ClickKind.RIGHT;
            case SHIFT_LEFT -> ClickKind.SHIFT_LEFT;
            case SHIFT_RIGHT -> ClickKind.SHIFT_RIGHT;
            case MIDDLE -> ClickKind.MIDDLE;
            default -> ClickKind.OTHER;
        };
    }

    private ItemStack toFiller(MenuIcon icon) {
        return MagicItem.of(material(icon.material()))
                .name(Component.text(" "))
                .hideAttributes()
                .build();
    }

    private ItemStack toItem(MenuButton button) {
        MenuIcon icon = button.icon();
        MagicItem item = MagicItem.of(material(icon.material()))
                .name(clean(button.name()))
                .loreComponents(button.lore().stream().map(this::clean).toList())
                .hideAttributes();
        if (button.glow()) {
            item.glow();
        }
        ItemStack stack = item.build();
        if (icon.isHead()) {
            applyHead(stack, icon);
        }
        return stack;
    }

    private void applyHead(ItemStack stack, MenuIcon icon) {
        ItemMeta meta = stack.getItemMeta();
        if (!(meta instanceof SkullMeta skull)) {
            return;
        }
        if (icon.hasTexture()) {
            skull.setPlayerProfile(texturedProfile(icon.texture()));
        } else if (icon.head() != null) {
            skull.setOwningPlayer(Bukkit.getOfflinePlayer(icon.head()));
        } else {
            skull.setOwnerProfile(Bukkit.createPlayerProfile(icon.headOwner()));
        }
        stack.setItemMeta(skull);
    }

    private com.destroystokyo.paper.profile.PlayerProfile texturedProfile(String texture) {
        UUID id = UUID.nameUUIDFromBytes(texture.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        com.destroystokyo.paper.profile.PlayerProfile profile = Bukkit.createProfile(id, null);
        profile.setProperty(new com.destroystokyo.paper.profile.ProfileProperty("textures", texture));
        return profile;
    }

    private Component clean(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }

    private Material material(String key) {
        Material material = Material.matchMaterial(key);
        return material == null ? Material.STONE : material;
    }
}
