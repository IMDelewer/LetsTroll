package dev.delewer.letstroll;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class TrollStats {

    private final AtomicInteger total = new AtomicInteger();
    private final Set<UUID> victims = new HashSet<>();
    private final Map<String, Integer> byModule = new HashMap<>();

    public void record(UUID victim, String module) {
        total.incrementAndGet();
        synchronized (victims) {
            victims.add(victim);
        }
        synchronized (byModule) {
            byModule.merge(module, 1, Integer::sum);
        }
    }

    public int drainTotal() {
        return total.getAndSet(0);
    }

    public int drainUniqueVictims() {
        synchronized (victims) {
            int count = victims.size();
            victims.clear();
            return count;
        }
    }

    public Map<String, Integer> drainByModule() {
        synchronized (byModule) {
            Map<String, Integer> snapshot = new HashMap<>(byModule);
            byModule.clear();
            return snapshot;
        }
    }
}
