package dev.delewer.letstroll.platform;

import dev.delewer.letstroll.menu.Menu;

public interface MenuBackend {

    void open(PlayerRef viewer, Menu menu);

    void update(PlayerRef viewer, Menu menu);

    void close(PlayerRef viewer);
}
