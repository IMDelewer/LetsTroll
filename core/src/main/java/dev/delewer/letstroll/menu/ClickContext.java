package dev.delewer.letstroll.menu;

import java.util.function.Consumer;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.platform.MenuSound;
import dev.delewer.letstroll.platform.PlayerRef;
import net.kyori.adventure.text.Component;

public record ClickContext(LetsTroll core, PlayerRef viewer, ClickKind kind, ScreenRequest request, MenuRouter router) {

    public void open(String screenId) {
        router.open(viewer, ScreenRequest.of(screenId));
    }

    public void open(ScreenRequest target) {
        router.open(viewer, target);
    }

    public void back() {
        router.back(viewer);
    }

    public void refresh() {
        router.refresh(viewer);
    }

    public void close() {
        router.close(viewer);
    }

    public void sound(MenuSound sound) {
        if (sound != MenuSound.NONE) {
            core.platform().sounds().play(viewer, sound);
        }
    }

    public void input(Component prompt, String initial, Consumer<String> onConfirm) {
        core.platform().input().request(viewer, prompt, initial, onConfirm);
    }
}
