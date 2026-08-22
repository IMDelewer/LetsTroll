package dev.delewer.letstroll.modules.mannequin;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.menu.Buttons;
import dev.delewer.letstroll.menu.Menu;
import dev.delewer.letstroll.menu.MenuButton;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.menu.MenuLayout;
import dev.delewer.letstroll.menu.Screen;
import dev.delewer.letstroll.menu.ScreenContext;
import dev.delewer.letstroll.platform.MenuSound;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.text.Text;

public final class MannequinScreen implements Screen {

    public static final String ID = "mannequin";

    private final MannequinConfig config;

    public MannequinScreen(MannequinConfig config) {
        this.config = config;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Menu build(ScreenContext context) {
        PlayerRef viewer = context.viewer();
        int rows = context.core().config().menuRows();
        Optional<PlayerRef> target = context.request().uuidArg("target")
                .flatMap(id -> context.core().platform().players().byId(id));

        Menu.Builder builder = Menu.builder(Text.get(viewer, "mannequin.title"))
                .rows(rows)
                .filler(context.core().filler());

        if (target.isEmpty()) {
            return builder.button(MenuLayout.center(MenuLayout.lastContentRow(rows) / 2 + 1),
                            MenuButton.of(MenuIcon.of("minecraft:paper"))
                                    .name(Text.get(viewer, "common.player-not-found", "?"))
                                    .build())
                    .button(MenuLayout.serviceSlot(rows, 4), Buttons.back(context.core(), viewer))
                    .build();
        }

        PlayerRef subject = target.get();
        List<Integer> slots = MenuLayout.content(rows);
        List<Map.Entry<String, String>> presets = List.copyOf(config.presets().entrySet());

        for (int index = 0; index < presets.size() && index < slots.size(); index++) {
            Map.Entry<String, String> preset = presets.get(index);
            builder.button(slots.get(index), MenuButton.of(iconFor(preset.getValue(), subject))
                    .name(Text.mini("<white>" + preset.getKey()))
                    .lore(Text.get(viewer, "mannequin.preset.skin", skinLabel(viewer, preset.getValue())),
                            Text.get(viewer, "mannequin.preset.hint", subject.name()),
                            Text.get(viewer, "mannequin.preset.remove"))
                    .sound(MenuSound.TOGGLE_ON)
                    .onClick(click -> {
                        if (click.kind().isRight() && click.viewer().hasPermission(LetsTroll.PERMISSION_ADMIN)) {
                            if (config.removePreset(preset.getKey())) {
                                click.core().saveConfig();
                                Text.send(click.viewer(), "mannequin.removed", preset.getKey());
                            }
                            click.refresh();
                            return;
                        }
                        click.core().platform().fakePlayers().spawn(subject, config.spec(preset.getKey()));
                        click.core().stats().record(subject.id(), "mannequin");
                        Text.send(click.viewer(), "mannequin.spawned", preset.getKey(), subject.name());
                        click.refresh();
                    })
                    .build());
        }

        builder.button(MenuLayout.serviceSlot(rows, 2), MenuButton.of(MenuIcon.of("minecraft:writable_book"))
                .name(Text.get(viewer, "mannequin.add.name"))
                .lore(Text.get(viewer, "mannequin.add.hint"))
                .onClick(click -> {
                    if (!click.viewer().hasPermission(LetsTroll.PERMISSION_ADMIN)) {
                        Text.send(click.viewer(), "common.no-permission");
                        return;
                    }
                    click.input(Text.get(viewer, "mannequin.add.prompt"), "", value -> addPreset(click, value, subject));
                })
                .build());

        return builder.button(MenuLayout.serviceSlot(rows, 4), Buttons.back(context.core(), viewer))
                .button(MenuLayout.serviceSlot(rows, 6), MenuButton.of(MenuIcon.of("minecraft:bone"))
                        .name(Text.get(viewer, "mannequin.clear"))
                        .sound(MenuSound.TOGGLE_OFF)
                        .onClick(click -> {
                            click.core().platform().fakePlayers().despawnAll(subject);
                            Text.send(click.viewer(), "mannequin.cleared", subject.name());
                            click.refresh();
                        })
                        .build())
                .build();
    }

    private void addPreset(dev.delewer.letstroll.menu.ClickContext click, String value, PlayerRef subject) {
        String[] parts = value.trim().split("\\s+", 2);
        if (parts.length < 2 || parts[0].isBlank()) {
            Text.send(click.viewer(), "mannequin.add.usage");
            click.open(dev.delewer.letstroll.menu.ScreenRequest.of(ID, "target", subject.id().toString()));
            return;
        }
        config.addPreset(parts[0], parts[1]);
        click.core().saveConfig();
        Text.send(click.viewer(), "mannequin.added", parts[0]);
        click.open(dev.delewer.letstroll.menu.ScreenRequest.of(ID, "target", subject.id().toString()));
    }

    private MenuIcon iconFor(String skin, PlayerRef subject) {
        if (MannequinConfig.MIRROR.equals(skin)) {
            return MenuIcon.head(subject.id(), subject.name());
        }
        if (skin.matches("[A-Za-z0-9_]{1,16}")) {
            return MenuIcon.headOf(skin);
        }
        return MenuIcon.of("minecraft:armor_stand");
    }

    private String skinLabel(PlayerRef viewer, String skin) {
        if (MannequinConfig.MIRROR.equals(skin)) {
            return Text.plain(viewer, "mannequin.preset.mirror");
        }
        if (skin.length() > 24) {
            return skin.substring(0, 21) + "...";
        }
        return skin;
    }
}
