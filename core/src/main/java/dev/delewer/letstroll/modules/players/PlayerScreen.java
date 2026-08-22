package dev.delewer.letstroll.modules.players;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.delewer.letstroll.menu.Buttons;
import dev.delewer.letstroll.menu.Menu;
import dev.delewer.letstroll.menu.MenuButton;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.menu.MenuLayout;
import dev.delewer.letstroll.menu.Screen;
import dev.delewer.letstroll.menu.ScreenContext;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.player.PlayerAction;
import dev.delewer.letstroll.text.Text;

public final class PlayerScreen implements Screen {

    public static final String ID = "player";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Menu build(ScreenContext context) {
        PlayerRef viewer = context.viewer();
        int rows = context.core().config().menuRows();
        Optional<UUID> targetId = context.request().uuidArg("target");
        Optional<PlayerRef> target = targetId.flatMap(id -> context.core().platform().players().byId(id));

        if (target.isEmpty()) {
            return Menu.builder(Text.get(viewer, "players.title"))
                    .rows(rows)
                    .filler(context.core().filler())
                    .button(MenuLayout.center(MenuLayout.lastContentRow(rows) / 2 + 1),
                            MenuButton.of(MenuIcon.of("minecraft:paper"))
                                    .name(Text.get(viewer, "common.player-not-found", targetId.map(UUID::toString).orElse("?")))
                                    .onClick(click -> click.open(ScreenRequest.of(PlayersScreen.ID)))
                                    .build())
                    .button(MenuLayout.serviceSlot(rows, 4), Buttons.back(context.core(), viewer))
                    .build();
        }

        PlayerRef subject = target.get();
        List<PlayerAction> actions = context.core().playerActions().visibleFor(viewer);
        List<Integer> slots = MenuLayout.content(rows);

        Menu.Builder builder = Menu.builder(Text.get(viewer, "player.title", subject.name()))
                .rows(rows)
                .filler(context.core().filler());

        if (actions.isEmpty()) {
            builder.button(MenuLayout.center(MenuLayout.lastContentRow(rows) / 2 + 1),
                    MenuButton.of(MenuIcon.of("minecraft:paper"))
                            .name(Text.get(viewer, "player.actions.empty"))
                            .lore(Text.get(viewer, "player.actions.hint"))
                            .build());
        }

        for (int index = 0; index < actions.size() && index < slots.size(); index++) {
            PlayerAction action = actions.get(index);
            builder.button(slots.get(index), MenuButton.of(action.icon(subject))
                    .name(action.name(subject))
                    .lore(action.lore(subject))
                    .glow(action.glow(subject))
                    .onClick(click -> action.run(click, subject))
                    .build());
        }

        return builder.button(MenuLayout.serviceSlot(rows, 4), Buttons.back(context.core(), viewer))
                .button(MenuLayout.serviceSlot(rows, 8), MenuButton.of(MenuIcon.head(subject.id(), subject.name()))
                        .name(Text.mini("<white>" + subject.name()))
                        .lore(Text.get(viewer, "players.entry.world", subject.world()),
                                Text.get(viewer, "players.entry.ping", subject.ping()),
                                Text.get(viewer, "players.entry.health", Math.round(subject.health())))
                        .build())
                .build();
    }
}
