package dev.delewer.letstroll.modules.hub;

import java.util.List;

import dev.delewer.letstroll.hub.HubEntry;
import dev.delewer.letstroll.menu.Buttons;
import dev.delewer.letstroll.menu.Menu;
import dev.delewer.letstroll.menu.MenuButton;
import dev.delewer.letstroll.menu.MenuLayout;
import dev.delewer.letstroll.menu.Screen;
import dev.delewer.letstroll.menu.ScreenContext;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.text.Text;

public final class HubScreen implements Screen {

    public static final String ID = "hub";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Menu build(ScreenContext context) {
        PlayerRef viewer = context.viewer();
        int rows = context.core().config().menuRows();

        Menu.Builder builder = Menu.builder(Text.get(viewer, "hub.title"))
                .rows(rows)
                .filler(context.core().filler());

        List<HubEntry> flowing = context.core().tabs().flowingFor(viewer);
        List<Integer> slots = flowing.size() <= 4
                ? MenuLayout.spread(flowing.size(), Math.max(1, MenuLayout.lastContentRow(rows) / 2))
                : MenuLayout.content(rows);

        for (int index = 0; index < flowing.size() && index < slots.size(); index++) {
            builder.button(slots.get(index), toButton(flowing.get(index), viewer));
        }

        for (HubEntry entry : context.core().tabs().pinnedFor(viewer)) {
            builder.button(entry.slot().orElseThrow(), toButton(entry, viewer));
        }

        return builder.button(MenuLayout.serviceSlot(rows, 4), Buttons.close(context.core(), viewer)).build();
    }

    private MenuButton toButton(HubEntry entry, PlayerRef viewer) {
        return MenuButton.of(entry.icon(viewer))
                .name(entry.title(viewer))
                .lore(entry.lore(viewer))
                .glow(entry.glow(viewer))
                .onClick(entry.action())
                .build();
    }
}
