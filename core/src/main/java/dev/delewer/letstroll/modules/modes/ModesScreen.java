package dev.delewer.letstroll.modules.modes;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.menu.Buttons;
import dev.delewer.letstroll.menu.Menu;
import dev.delewer.letstroll.menu.MenuButton;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.menu.MenuLayout;
import dev.delewer.letstroll.menu.Screen;
import dev.delewer.letstroll.menu.ScreenContext;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.text.Text;

public final class ModesScreen implements Screen {

    public static final String ID = "modes";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Menu build(ScreenContext context) {
        LetsTroll core = context.core();
        PlayerRef viewer = context.viewer();
        int rows = core.config().menuRows();

        Menu.Builder builder = Menu.builder(Text.get(viewer, "hub.tab.modes.title"))
                .rows(rows)
                .filler(core.filler());

        builder.button(MenuLayout.center(2), MenuButton.of(MenuIcon.of("minecraft:chain"))
                .name(Text.get(viewer, "modes.chain.name"))
                .lore(Text.get(viewer, "modes.chain.description"))
                .glow(true)
                .onClick(click -> click.open(ScreenRequest.of(ChainScreen.ID)))
                .build());

        return builder.button(MenuLayout.serviceSlot(rows, 4), Buttons.back(core, viewer)).build();
    }
}
