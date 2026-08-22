package dev.delewer.letstroll.support;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import dev.delewer.letstroll.platform.PlayerRef;
import net.kyori.adventure.text.Component;

public final class FakePlayer implements PlayerRef {

    private final UUID id = UUID.randomUUID();
    private final String name;
    private final Set<String> permissions = new HashSet<>();
    private final List<Component> messages = new ArrayList<>();

    public FakePlayer(String name, String... nodes) {
        this.name = name;
        this.permissions.addAll(List.of(nodes));
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean online() {
        return true;
    }

    @Override
    public boolean hasPermission(String node) {
        return permissions.contains(node);
    }

    @Override
    public void send(Component message) {
        messages.add(message);
    }

    @Override
    public String world() {
        return "world";
    }

    @Override
    public int ping() {
        return 12;
    }

    @Override
    public double health() {
        return 20.0;
    }

    @Override
    public Object handle() {
        return this;
    }

    public List<Component> messages() {
        return messages;
    }
}
