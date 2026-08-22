package dev.delewer.letstroll.fabric;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.menu.Menu;
import dev.delewer.letstroll.menu.MenuButton;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.platform.MenuBackend;
import dev.delewer.letstroll.platform.PlayerRef;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class FabricMenus implements MenuBackend {

    private final Supplier<LetsTroll> core;
    private final Map<UUID, FabricMenuScreenHandler> open = new ConcurrentHashMap<>();

    public FabricMenus(Supplier<LetsTroll> core) {
        this.core = core;
    }

    @Override
    public void open(PlayerRef viewer, Menu menu) {
        if (!(viewer.handle() instanceof ServerPlayerEntity player)) {
            return;
        }
        LetsTroll instance = core.get();
        int rows = menu.rows();
        SimpleInventory inventory = new SimpleInventory(rows * 9);
        render(inventory, menu);
        Text title = FabricText.toNative(menu.title());
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, owner) -> {
            FabricMenuScreenHandler handler = new FabricMenuScreenHandler(
                    syncId, playerInventory, inventory, rows, this, instance, viewer, menu);
            open.put(viewer.id(), handler);
            return handler;
        }, title));
    }

    @Override
    public void update(PlayerRef viewer, Menu menu) {
        FabricMenuScreenHandler handler = open.get(viewer.id());
        if (handler == null || handler.rowCount() != menu.rows()) {
            open(viewer, menu);
            return;
        }
        handler.apply(menu);
        render(handler.inventory(), menu);
        handler.sendContentUpdates();
    }

    @Override
    public void close(PlayerRef viewer) {
        open.remove(viewer.id());
        if (viewer.handle() instanceof ServerPlayerEntity player) {
            player.closeHandledScreen();
        }
    }

    public void forget(UUID viewer) {
        open.remove(viewer);
    }

    public void handleClosed(UUID viewer) {
        open.remove(viewer);
        LetsTroll instance = core.get();
        if (instance != null) {
            instance.router().forget(viewer);
        }
    }

    private void render(SimpleInventory inventory, Menu menu) {
        MenuIcon filler = menu.filler().orElse(null);
        ItemStack fillerStack = filler == null ? ItemStack.EMPTY : fillerStack(filler);
        for (int slot = 0; slot < inventory.size(); slot++) {
            inventory.setStack(slot, fillerStack.copy());
        }
        for (Map.Entry<Integer, MenuButton> entry : menu.buttons().entrySet()) {
            if (entry.getKey() >= 0 && entry.getKey() < inventory.size()) {
                inventory.setStack(entry.getKey(), toStack(entry.getValue()));
            }
        }
    }

    private ItemStack fillerStack(MenuIcon icon) {
        ItemStack stack = iconStack(icon);
        stack.set(DataComponentTypes.CUSTOM_NAME, FabricText.toNative(Component.text(" ")));
        return stack;
    }

    private ItemStack toStack(MenuButton button) {
        ItemStack stack = iconStack(button.icon());
        stack.set(DataComponentTypes.CUSTOM_NAME, FabricText.toNative(clean(button.name())));
        if (!button.lore().isEmpty()) {
            List<Text> lore = button.lore().stream().map(line -> FabricText.toNative(clean(line))).toList();
            stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        }
        if (button.glow()) {
            stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        return stack;
    }

    private ItemStack iconStack(MenuIcon icon) {
        if (icon.isHead()) {
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            ProfileComponent profile = null;
            if (icon.hasTexture()) {
                GameProfile gameProfile = new GameProfile(new UUID(icon.texture().hashCode(), 0L), "lt");
                gameProfile.properties().put("textures", new Property("textures", icon.texture()));
                profile = ProfileComponent.ofStatic(gameProfile);
            } else if (icon.head() != null) {
                profile = ProfileComponent.ofDynamic(icon.head());
            } else if (icon.headOwner() != null) {
                profile = ProfileComponent.ofDynamic(icon.headOwner());
            }
            if (profile != null) {
                head.set(DataComponentTypes.PROFILE, profile);
            }
            return head;
        }
        Item item = Registries.ITEM.get(FabricText.id(icon.material()));
        if (item == Items.AIR) {
            item = Items.STONE;
        }
        return new ItemStack(item);
    }

    private Component clean(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }
}
