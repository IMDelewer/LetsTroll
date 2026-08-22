package dev.delewer.letstroll.platform;

import java.util.Collection;

import net.kyori.adventure.text.Component;

public interface BossBarService {

    Object create(Component title, String color);

    void update(Object handle, Component title, float progress);

    void viewers(Object handle, Collection<PlayerRef> viewers);

    void hide(Object handle);
}
