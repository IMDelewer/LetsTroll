package dev.delewer.letstroll.menu;

import java.util.function.Consumer;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.platform.MenuSound;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.text.Text;
import net.kyori.adventure.text.Component;

public final class Buttons {

    private Buttons() {
    }

    public static MenuButton back(LetsTroll core, PlayerRef viewer) {
        return MenuButton.of(core.heads().icon(Heads.BACK))
                .name(Text.get(viewer, "common.back"))
                .onClick(ClickContext::back)
                .build();
    }

    public static MenuButton close(LetsTroll core, PlayerRef viewer) {
        return MenuButton.of(core.heads().icon(Heads.CLOSE))
                .name(Text.get(viewer, "common.close"))
                .sound(MenuSound.TOGGLE_OFF)
                .onClick(ClickContext::close)
                .build();
    }

    public static MenuButton previous(LetsTroll core, PlayerRef viewer, ScreenRequest request, int page, int total) {
        return MenuButton.of(core.heads().icon(Heads.PREVIOUS))
                .name(Text.get(viewer, "common.previous"))
                .lore(Text.get(viewer, "common.page", page + 1, total))
                .onClick(click -> click.open(request.withPage(page - 1)))
                .build();
    }

    public static MenuButton next(LetsTroll core, PlayerRef viewer, ScreenRequest request, int page, int total) {
        return MenuButton.of(core.heads().icon(Heads.NEXT))
                .name(Text.get(viewer, "common.next"))
                .lore(Text.get(viewer, "common.page", page + 1, total))
                .onClick(click -> click.open(request.withPage(page + 1)))
                .build();
    }

    public static MenuButton search(LetsTroll core, PlayerRef viewer, ScreenRequest request, String filter) {
        MenuButton.Builder builder = MenuButton.of(core.heads().icon(Heads.SEARCH))
                .name(Text.get(viewer, "common.search"))
                .onClick(click -> click.input(Text.get(viewer, "common.search.prompt"), filter == null ? "" : filter,
                        value -> click.open(request.with("filter", value).withPage(0))));
        if (filter != null && !filter.isBlank()) {
            builder.lore(Text.get(viewer, "common.search.active", filter), Text.get(viewer, "common.search.clear"))
                    .glow(true);
        }
        return builder.build();
    }

    public static MenuButton toggle(LetsTroll core, PlayerRef viewer, boolean on, Component name,
                                    Component description, Consumer<ClickContext> onToggle) {
        return MenuButton.of(core.heads().icon(on ? Heads.TOGGLE_ON : Heads.TOGGLE_OFF))
                .name(name)
                .lore(description, Text.get(viewer, on ? "common.toggle.on" : "common.toggle.off"))
                .glow(on)
                .sound(on ? MenuSound.TOGGLE_OFF : MenuSound.TOGGLE_ON)
                .onClick(onToggle)
                .build();
    }
}
