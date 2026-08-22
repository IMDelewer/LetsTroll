package dev.delewer.letstroll.modules.players;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import dev.delewer.letstroll.menu.Buttons;
import dev.delewer.letstroll.menu.Menu;
import dev.delewer.letstroll.menu.MenuButton;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.menu.MenuLayout;
import dev.delewer.letstroll.menu.Pagination;
import dev.delewer.letstroll.menu.Screen;
import dev.delewer.letstroll.menu.ScreenContext;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.text.Text;

public final class PlayersScreen implements Screen {

    public static final String ID = "players";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Menu build(ScreenContext context) {
        PlayerRef viewer = context.viewer();
        int rows = context.core().config().menuRows();
        List<Integer> slots = MenuLayout.content(rows);
        String filter = context.request().arg("filter").filter(value -> !value.isBlank()).orElse(null);

        List<PlayerRef> players = context.core().platform().players().online().stream()
                .filter(player -> matches(player, filter))
                .sorted(Comparator.comparing(PlayerRef::name, String.CASE_INSENSITIVE_ORDER))
                .toList();

        int perPage = Math.min(slots.size(), context.core().config().playersPerPage());
        Pagination<PlayerRef> pagination = new Pagination<>(players, context.page(), perPage);
        List<PlayerRef> visible = pagination.slice();

        Menu.Builder builder = Menu.builder(Text.get(viewer, "players.title"))
                .rows(rows)
                .filler(context.core().filler());

        if (visible.isEmpty()) {
            builder.button(MenuLayout.center(MenuLayout.lastContentRow(rows) / 2 + 1),
                    MenuButton.of(MenuIcon.of("minecraft:paper"))
                            .name(Text.get(viewer, filter == null ? "players.empty" : "players.empty.filtered", filter))
                            .build());
        }

        for (int index = 0; index < visible.size() && index < slots.size(); index++) {
            PlayerRef target = visible.get(index);
            builder.button(slots.get(index), MenuButton.of(MenuIcon.head(target.id(), target.name()))
                    .name(Text.mini("<white>" + target.name()))
                    .lore(Text.get(viewer, "players.entry.world", target.world()),
                            Text.get(viewer, "players.entry.ping", target.ping()),
                            Text.get(viewer, "players.entry.health", Math.round(target.health())),
                            Text.get(viewer, "players.entry.hint"))
                    .onClick(click -> click.open(ScreenRequest.of(PlayerScreen.ID, "target", target.id().toString())))
                    .build());
        }

        if (pagination.hasPrevious()) {
            builder.button(MenuLayout.serviceSlot(rows, 0),
                    Buttons.previous(context.core(), viewer, context.request(), pagination.currentPage(), pagination.totalPages()));
        }
        if (pagination.hasNext()) {
            builder.button(MenuLayout.serviceSlot(rows, 8),
                    Buttons.next(context.core(), viewer, context.request(), pagination.currentPage(), pagination.totalPages()));
        }

        return builder.button(MenuLayout.serviceSlot(rows, 2), Buttons.search(context.core(), viewer, context.request(), filter))
                .button(MenuLayout.serviceSlot(rows, 4), Buttons.back(context.core(), viewer))
                .build();
    }

    private boolean matches(PlayerRef player, String filter) {
        if (filter == null) {
            return true;
        }
        return player.name().toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
    }
}
