package dev.delewer.letstroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import dev.delewer.letstroll.modules.modes.ChainConfig;
import dev.delewer.letstroll.modules.modes.ChainService;
import dev.delewer.letstroll.support.FakePlatform;
import dev.delewer.letstroll.support.FakePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ChainServiceTest {

    @TempDir
    Path dataFolder;

    private LetsTroll core;
    private ChainConfig config;
    private ChainService service;
    private FakePlayer first;
    private FakePlayer second;
    private FakePlayer third;

    @BeforeEach
    void setUp() {
        FakePlatform platform = new FakePlatform(dataFolder);
        first = new FakePlayer("One");
        second = new FakePlayer("Two");
        third = new FakePlayer("Three");
        platform.addPlayers(first, second, third);
        core = new LetsTroll(platform);
        core.start();
        config = new ChainConfig();
        service = new ChainService(core, config);
        service.start();
    }

    @Test
    void linkingPairsBothPlayers() {
        assertTrue(service.link(first.id(), second.id()));

        assertTrue(service.isLinked(first.id()));
        assertTrue(service.isLinked(second.id()));
        assertEquals(second.id(), service.partner(first.id()).orElseThrow());
        assertEquals(first.id(), service.partner(second.id()).orElseThrow());
    }

    @Test
    void aPlayerCannotBeLinkedTwiceOrToThemselves() {
        service.link(first.id(), second.id());

        assertFalse(service.link(first.id(), third.id()));
        assertFalse(service.link(first.id(), first.id()));
        assertEquals(1, service.pairs().size());
    }

    @Test
    void unlinkingFromEitherSideClearsBothPlayers() {
        service.link(first.id(), second.id());

        service.unlink(second.id());

        assertFalse(service.isLinked(first.id()));
        assertFalse(service.isLinked(second.id()));
        assertTrue(service.pairs().isEmpty());
    }

    @Test
    void linksArePersistedAndRestored() {
        service.link(first.id(), second.id());
        assertEquals(List.of(first.id() + ":" + second.id()), config.links());

        ChainService restored = new ChainService(core, config);
        restored.start();

        assertTrue(restored.isLinked(first.id()));
        assertEquals(second.id(), restored.partner(first.id()).orElseThrow());
    }

    @Test
    void malformedStoredLinksAreSkipped() {
        config.setLinks(List.of("not-a-uuid:also-not", first.id() + ":" + second.id(), "single"));

        ChainService restored = new ChainService(core, config);
        restored.start();

        assertEquals(1, restored.pairs().size());
        assertTrue(restored.isLinked(second.id()));
    }
}
