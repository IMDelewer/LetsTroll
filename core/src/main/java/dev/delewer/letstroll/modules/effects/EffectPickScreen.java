package dev.delewer.letstroll.modules.effects;

import java.util.List;
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

public final class EffectPickScreen implements Screen {

    public static final String ID = "effect_pick";

    private final EffectsConfig config;

    public EffectPickScreen(EffectsConfig config) {
        this.config = config;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Menu build(ScreenContext context) {
        LetsTroll core = context.core();
        PlayerRef viewer = context.viewer();
        int rows = core.config().menuRows();
        Optional<PlayerRef> target = context.request().uuidArg("target")
                .flatMap(id -> core.platform().players().byId(id));

        Menu.Builder builder = Menu.builder(Text.get(viewer, "effects.pick.title"))
                .rows(rows)
                .filler(core.filler());

        if (target.isEmpty()) {
            return builder.button(MenuLayout.center(MenuLayout.lastContentRow(rows) / 2 + 1),
                            MenuButton.of(MenuIcon.of("minecraft:paper"))
                                    .name(Text.get(viewer, "common.player-not-found", "?"))
                                    .build())
                    .button(MenuLayout.serviceSlot(rows, 4), Buttons.back(core, viewer))
                    .build();
        }

        PlayerRef subject = target.get();
        List<String> effects = config.effects();
        List<Integer> slots = MenuLayout.content(rows);
        for (int index = 0; index < effects.size() && index < slots.size(); index++) {
            String effect = effects.get(index);
            builder.button(slots.get(index), MenuButton.of(MenuIcon.of("minecraft:splash_potion"))
                    .name(Text.mini("<white>" + prettyName(effect)))
                    .lore(Text.get(viewer, "effects.pick.hint", subject.name()))
                    .sound(MenuSound.TOGGLE_ON)
                    .onClick(click -> {
                        click.core().platform().effects().potion(subject, effect, config.durationTicks(), 0);
                        click.core().stats().record(subject.id(), "effects");
                        Text.send(click.viewer(), "effects.applied", prettyName(effect), subject.name());
                    })
                    .build());
        }

        return builder.button(MenuLayout.serviceSlot(rows, 4), Buttons.back(core, viewer)).build();
    }

    private String prettyName(String effectKey) {
        String name = effectKey.contains(":") ? effectKey.substring(effectKey.indexOf(':') + 1) : effectKey;
        name = name.replace('_', ' ');
        return name.isEmpty() ? effectKey : Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
