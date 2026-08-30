package dev.delewer.letstroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import dev.delewer.letstroll.modules.lag.LagConfig;
import dev.delewer.letstroll.modules.lag.LagService;
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
