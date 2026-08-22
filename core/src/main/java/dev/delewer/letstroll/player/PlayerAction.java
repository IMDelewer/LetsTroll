package dev.delewer.letstroll.player;

import java.util.List;

import dev.delewer.letstroll.menu.ClickContext;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.platform.PlayerRef;
import net.kyori.adventure.text.Component;

public interface PlayerAction {

    String id();

    MenuIcon icon(PlayerRef target);

    Component name(PlayerRef target);

    void run(ClickContext click, PlayerRef target);

    default int order() {
        return 100;
    }

    default List<Component> lore(PlayerRef target) {
        return List.of();
    }

    default String permission() {
        return null;
    }

    default boolean glow(PlayerRef target) {
        return false;
    }
}
