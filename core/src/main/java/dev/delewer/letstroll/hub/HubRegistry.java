package dev.delewer.letstroll.hub;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import dev.delewer.letstroll.platform.PlayerRef;

public final class HubRegistry {

    private final List<HubEntry> entries = new ArrayList<>();

    public void add(HubEntry entry) {
        entries.removeIf(existing -> existing.id().equals(entry.id()));
        entries.add(entry);
        entries.sort(Comparator.comparingInt(HubEntry::order).thenComparing(HubEntry::id));
    }

    public List<HubEntry> all() {
        return List.copyOf(entries);
    }

    public List<HubEntry> visibleFor(PlayerRef viewer) {
        return entries.stream()
                .filter(entry -> entry.permission() == null || viewer.hasPermission(entry.permission()))
                .toList();
    }

    public List<HubEntry> flowingFor(PlayerRef viewer) {
        return visibleFor(viewer).stream().filter(entry -> entry.slot().isEmpty()).toList();
    }

    public List<HubEntry> pinnedFor(PlayerRef viewer) {
        return visibleFor(viewer).stream().filter(entry -> entry.slot().isPresent()).toList();
    }
}
