package dev.delewer.letstroll.modules.settings;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.config.CoreConfig;
import dev.delewer.letstroll.menu.Buttons;
import dev.delewer.letstroll.menu.HeadCatalog;
import dev.delewer.letstroll.menu.Heads;
import dev.delewer.letstroll.menu.Menu;
import dev.delewer.letstroll.menu.MenuButton;
import dev.delewer.letstroll.menu.MenuLayout;
import dev.delewer.letstroll.menu.Screen;
import dev.delewer.letstroll.menu.ScreenContext;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.text.Text;

public final class SettingsScreen implements Screen {

    public static final String ID = "settings";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Menu build(ScreenContext context) {
        LetsTroll core = context.core();
        PlayerRef viewer = context.viewer();
        CoreConfig config = core.config();
        HeadCatalog heads = core.heads();
        int rows = config.menuRows();
        int row = MenuLayout.lastContentRow(rows) / 2 + 1;
        int center = MenuLayout.center(row);

        Menu.Builder builder = Menu.builder(Text.get(viewer, "settings.title"))
                .rows(rows)
                .filler(core.filler());

        builder.button(center - 4, MenuButton.of(heads.icon(Heads.INFO))
                .name(Text.get(viewer, "settings.modules", core.modules().count()))
                .lore(core.modules().infos().stream().map(info -> Text.mini("<gray>- <white>" + info.name())).toList())
                .build());

        builder.button(center - 2, Buttons.toggle(core, viewer, config.soundsEnabled(),
                Text.get(viewer, "settings.sounds"), Text.get(viewer, "settings.sounds.description"),
                click -> {
                    config.setSoundsEnabled(!config.soundsEnabled());
                    core.saveConfig();
                    click.refresh();
                }));

        builder.button(center, Buttons.toggle(core, viewer, config.hideFromPlugins(),
                Text.get(viewer, "settings.hide-plugins"), Text.get(viewer, "settings.hide-plugins.description"),
                click -> {
                    config.setHideFromPlugins(!config.hideFromPlugins());
                    core.saveConfig();
                    click.refresh();
                }));

        builder.button(center + 2, Buttons.toggle(core, viewer, config.hideCommands(),
                Text.get(viewer, "settings.hide-commands"), Text.get(viewer, "settings.hide-commands.description"),
                click -> {
                    config.setHideCommands(!config.hideCommands());
                    core.saveConfig();
                    click.refresh();
                }));

        builder.button(center + 4, MenuButton.of(heads.icon(Heads.SETTINGS))
                .name(Text.get(viewer, "settings.reload"))
                .lore(Text.get(viewer, "settings.language", config.language()))
                .onClick(click -> {
                    click.core().platform().configs().reloadAll();
                    Text.send(click.viewer(), "settings.reload.done");
                    click.refresh();
                })
                .build());

        return builder.button(MenuLayout.serviceSlot(rows, 4), Buttons.back(core, viewer)).build();
    }
}
