package dev.delewer.letstroll.menu;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.platform.PlayerRef;

public record ScreenContext(LetsTroll core, PlayerRef viewer, ScreenRequest request, MenuRouter router) {

    public int page() {
        return request.page();
    }
}
