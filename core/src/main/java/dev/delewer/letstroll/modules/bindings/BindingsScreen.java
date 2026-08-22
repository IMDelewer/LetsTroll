package dev.delewer.letstroll.modules.bindings;

import java.util.List;
import java.util.Optional;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.menu.Buttons;
import dev.delewer.letstroll.menu.Menu;
import dev.delewer.letstroll.menu.MenuButton;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.menu.MenuLayout;
import dev.delewer.letstroll.menu.Screen;
import dev.delewer.letstroll.platform.MenuSound;
import dev.delewer.letstroll.menu.ScreenContext;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.player.PlayerAction;
import dev.delewer.letstroll.text.Text;

public final class BindingsScreen implements Screen {

    public static final String ID = "bindings";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Menu build(ScreenContext context) {
        LetsTroll core = context.core();
        PlayerRef viewer = context.viewer();
        int rows = core.config().menuRows();

        Menu.Builder builder = Menu.builder(Text.get(viewer, "hub.tab.bindings.title"))
                .rows(rows)
                .filler(core.filler());

        Optional<String> current = core.platform().itemBindings().heldBinding(viewer);
        builder.button(MenuLayout.serviceSlot(rows, 2), MenuButton.of(MenuIcon.of("minecraft:paper"))
                .name(Text.get(viewer, "bindings.held", current.orElse("-")))
                .lore(Text.get(viewer, "bindings.held.hint"))
                .build());

        builder.button(MenuLayout.serviceSlot(rows, 6), MenuButton.of(MenuIcon.of("minecraft:barrier"))
                .name(Text.get(viewer, "bindings.clear"))
                .sound(MenuSound.TOGGLE_OFF)
                .onClick(click -> {
                    if (core.platform().itemBindings().unbindHeldItem(click.viewer())) {
                        Text.send(click.viewer(), "bindings.cleared");
                    } else {
                        Text.send(click.viewer(), "bindings.none");
                    }
                    click.refresh();
                })
                .build());

        List<PlayerAction> actions = core.playerActions().visibleFor(viewer);
        List<Integer> slots = MenuLayout.content(rows);
        for (int index = 0; index < actions.size() && index < slots.size(); index++) {
            PlayerAction action = actions.get(index);
            boolean bound = current.filter(id -> id.equals(action.id())).isPresent();
            builder.button(slots.get(index), MenuButton.of(action.icon(viewer))
                    .name(action.name(viewer))
                    .lore(Text.get(viewer, "bindings.bind.hint"),
                            Text.get(viewer, bound ? "bindings.bound" : "bindings.not-bound"))
                    .glow(bound)
                    .sound(MenuSound.TOGGLE_ON)
                    .onClick(click -> {
                        if (core.platform().itemBindings().bindHeldItem(click.viewer(), action.id())) {
                            Text.send(click.viewer(), "bindings.saved", action.id());
                        } else {
                            Text.send(click.viewer(), "bindings.hold-item");
                        }
                        click.refresh();
                    })
                    .build());
        }

        return builder.button(MenuLayout.serviceSlot(rows, 4), Buttons.back(core, viewer)).build();
    }
}
