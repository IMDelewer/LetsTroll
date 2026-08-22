package dev.delewer.letstroll.menu;

import java.util.List;

import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.text.Text;
import net.kyori.adventure.text.Component;

public final class InfoScreen implements Screen {

    private final String id;
    private final String titleKey;
    private final String iconMaterial;
    private final String headlineKey;
    private final List<String> loreKeys;

    public InfoScreen(String id, String titleKey, String iconMaterial, String headlineKey, List<String> loreKeys) {
        this.id = id;
        this.titleKey = titleKey;
        this.iconMaterial = iconMaterial;
        this.headlineKey = headlineKey;
        this.loreKeys = List.copyOf(loreKeys);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Menu build(ScreenContext context) {
        PlayerRef viewer = context.viewer();
        int rows = context.core().config().menuRows();
        List<Component> lore = loreKeys.stream().map(key -> Text.get(viewer, key)).toList();
        return Menu.builder(Text.get(viewer, titleKey))
                .rows(rows)
                .filler(context.core().filler())
                .button(MenuLayout.center(MenuLayout.lastContentRow(rows) / 2 + 1), MenuButton.of(MenuIcon.of(iconMaterial))
                        .name(Text.get(viewer, headlineKey))
                        .lore(lore)
                        .build())
                .button(MenuLayout.serviceSlot(rows, 4), Buttons.back(context.core(), viewer))
                .build();
    }
}
