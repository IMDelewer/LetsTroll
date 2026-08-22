package dev.delewer.letstroll.player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import dev.delewer.letstroll.platform.PlayerRef;

public final class PlayerActionRegistry {

    private final List<PlayerAction> actions = new ArrayList<>();

    public void add(PlayerAction action) {
        actions.removeIf(existing -> existing.id().equals(action.id()));
        actions.add(action);
        actions.sort(Comparator.comparingInt(PlayerAction::order).thenComparing(PlayerAction::id));
    }

    public List<PlayerAction> all() {
        return List.copyOf(actions);
    }

    public List<PlayerAction> visibleFor(PlayerRef viewer) {
        return actions.stream()
                .filter(action -> action.permission() == null || viewer.hasPermission(action.permission()))
                .toList();
    }
}
