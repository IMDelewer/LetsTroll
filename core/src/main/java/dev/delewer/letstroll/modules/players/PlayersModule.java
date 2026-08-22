package dev.delewer.letstroll.modules.players;

import java.util.List;

import dev.delewer.letstroll.hub.HubEntry;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.module.LetsTrollModule;
import dev.delewer.letstroll.module.ModuleContext;
import dev.delewer.letstroll.module.TrollModule;
import dev.delewer.letstroll.text.Text;

@TrollModule(id = "players", name = "Players", order = 10)
public final class PlayersModule implements LetsTrollModule {

    @Override
    public void enable(ModuleContext context) {
        context.screen(new PlayersScreen());
        context.screen(new PlayerScreen());
        context.hubEntry(HubEntry.of("players")
                .order(10)
                .icon(viewer -> MenuIcon.head(viewer.id(), viewer.name()))
                .title(viewer -> Text.get(viewer, "hub.tab.players.title"))
                .lore(viewer -> List.of(
                        Text.get(viewer, "hub.tab.players.description"),
                        Text.get(viewer, "hub.tab.players.online",
                                context.platform().players().online().size())))
                .onClick(click -> click.open(ScreenRequest.of(PlayersScreen.ID)))
                .build());
    }
}
