package dev.delewer.letstroll.fabric;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.menu.ClickContext;
import dev.delewer.letstroll.menu.ClickKind;
import dev.delewer.letstroll.menu.Menu;
import dev.delewer.letstroll.menu.MenuButton;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.platform.PlayerRef;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;

public final class FabricMenuScreenHandler extends GenericContainerScreenHandler {

    private final FabricMenus owner;
    private final LetsTroll core;
    private final PlayerRef viewer;
    private final SimpleInventory inventory;
    private final int rows;
    private Menu menu;

    public FabricMenuScreenHandler(int syncId, PlayerInventory playerInventory, SimpleInventory inventory, int rows,
                                   FabricMenus owner, LetsTroll core, PlayerRef viewer, Menu menu) {
        super(type(rows), syncId, playerInventory, inventory, rows);
        this.inventory = inventory;
        this.rows = rows;
        this.owner = owner;
        this.core = core;
        this.viewer = viewer;
        this.menu = menu;
    }

    public SimpleInventory inventory() {
        return inventory;
    }

    public int rowCount() {
        return rows;
    }

    public void apply(Menu updated) {
        this.menu = updated;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex >= 0 && slotIndex < rows * 9 && menu != null && core != null) {
            MenuButton clicked = menu.buttons().get(slotIndex);
            if (clicked != null && clicked.clickable()) {
                ScreenRequest request = core.router().current(viewer.id()).orElse(null);
                if (request != null) {
                    clicked.click(new ClickContext(core, viewer, kind(button, actionType), request, core.router()));
                }
            }
        }
        sendContentUpdates();
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        owner.handleClosed(viewer.id());
    }

    private static ClickKind kind(int button, SlotActionType actionType) {
        if (actionType == SlotActionType.QUICK_MOVE) {
            return button == 0 ? ClickKind.SHIFT_LEFT : ClickKind.SHIFT_RIGHT;
        }
        if (actionType == SlotActionType.CLONE) {
            return ClickKind.MIDDLE;
        }
        if (actionType == SlotActionType.PICKUP) {
            return button == 0 ? ClickKind.LEFT : ClickKind.RIGHT;
        }
        return ClickKind.OTHER;
    }

    private static ScreenHandlerType<GenericContainerScreenHandler> type(int rows) {
        return switch (rows) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            default -> ScreenHandlerType.GENERIC_9X6;
        };
    }
}
