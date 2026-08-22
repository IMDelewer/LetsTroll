package dev.delewer.letstroll.platform;

import java.util.UUID;

import net.kyori.adventure.text.Component;

public interface PlayerRef {

    UUID id();

    String name();

    boolean online();

    boolean hasPermission(String node);

    void send(Component message);

    String world();

    int ping();

    double health();

    Object handle();
}
