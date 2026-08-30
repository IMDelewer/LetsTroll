package dev.delewer.letstroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import dev.delewer.letstroll.modules.lag.LagConfig;
import dev.delewer.letstroll.modules.lag.LagService;
import dev.delewer.letstroll.platform.Position;
import dev.delewer.letstroll.support.FakePlatform;
import dev.delewer.letstroll.support.FakePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LagServiceTest {

    @TempDir
    Path folder;

    private FakePlatform platform;
    private LagConfig config;
    private LagService service;
    private FakePlayer victim;

    @BeforeEach
    void setUp() {
        platform = new FakePlatform(folder);
        config = new LagConfig();
        service = new LagService(platform, config);
        victim = new FakePlayer("Victim");
        platform.addPlayers(victim);
    }

    @Test
    void registersTheSessionBeforeItsTickerRuns() {
        service.start(victim, 5, 0L);

        assertTrue(service.isLagging(victim.id()));
        assertEquals(1, platform.repeatingTasks().size());
    }

    @Test
    void stopCancelsTheTickerAndForgetsTheSession() {
        service.start(victim, 5, 0L);
        service.stop(victim.id());

        assertFalse(service.isLagging(victim.id()));
        assertTrue(platform.repeatingTasks().isEmpty());
    }

    @Test
    void restartingLeavesOnlyOneTicker() {
        service.start(victim, 5, 0L);
        service.start(victim, 8, 0L);

        assertEquals(1, platform.repeatingTasks().size());
    }

    @Test
    void holdsAndReleasesPacketsWhenThePlatformSupportsIt() {
        platform.supportPacketHold(true);

        service.start(victim, 5, 0L);
        assertTrue(service.holdsPackets(victim.id()));
        assertEquals(1, platform.heldPackets().size());

        service.stop(victim.id());
        assertTrue(platform.heldPackets().isEmpty());
    }

    @Test
    void reportsNoPacketHoldWhenThePlatformDeclines() {
        service.start(victim, 5, 0L);

        assertFalse(service.holdsPackets(victim.id()));
        assertTrue(platform.heldPackets().isEmpty());
    }

    @Test
    void fakesAJitteredPingInsteadOfAFlatValue() {
        config.setDelayMillis(1000L);
        platform.movement().teleport(victim, new Position("world", 0, 64, 0, 0f, 0f));
        service.start(victim, 5, 0L);

        for (int tick = 0; tick < 400; tick++) {
            java.util.List.copyOf(platform.repeatingTasks()).forEach(Runnable::run);
        }

        java.util.List<Integer> values = platform.pingValues();
        assertTrue(values.size() > 3, "expected several ping updates, got " + values.size());
        assertTrue(values.size() < 200, "ping should not be pushed every tick, got " + values.size());
        assertTrue(java.util.Set.copyOf(values).size() > 1, "ping should vary, got " + values);
        assertTrue(values.stream().allMatch(value -> value >= 550 && value <= 1120),
                "ping should stay around the configured delay, got " + values);
    }

    @Test
    void stopClearsTheFakePing() {
        service.start(victim, 5, 0L);
        assertTrue(platform.ping().isFaked(victim.id()));

        service.stop(victim.id());
        assertFalse(platform.ping().isFaked(victim.id()));
    }

    @Test
    void stopAllClearsEverySession() {
        FakePlayer other = new FakePlayer("Other");
        platform.addPlayers(other);

        service.start(victim, 5, 0L);
        service.start(other, 5, 0L);
        service.stopAll();

        assertFalse(service.isLagging(victim.id()));
        assertFalse(service.isLagging(other.id()));
        assertTrue(platform.repeatingTasks().isEmpty());
    }
}
