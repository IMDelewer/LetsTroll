package dev.delewer.letstroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import dev.delewer.letstroll.menu.MenuRouter;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.modules.hub.HubScreen;
import dev.delewer.letstroll.modules.players.PlayersScreen;
import dev.delewer.letstroll.support.FakePlatform;
import dev.delewer.letstroll.support.FakePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MenuRouterTest {

    @TempDir
    Path dataFolder;

    private FakePlatform platform;
    private LetsTroll core;
    private MenuRouter router;
    private FakePlayer viewer;

    @BeforeEach
    void setUp() {
        platform = new FakePlatform(dataFolder);
        viewer = new FakePlayer("Delewer", LetsTroll.PERMISSION_USE, LetsTroll.PERMISSION_ADMIN);
        platform.addPlayers(viewer);
        core = new LetsTroll(platform);
        core.start();
        router = core.router();
    }

    @Test
    void backReturnsToThePreviousScreen() {
        router.open(viewer, ScreenRequest.of(HubScreen.ID));
        router.open(viewer, ScreenRequest.of(PlayersScreen.ID));
        assertEquals(PlayersScreen.ID, router.current(viewer.id()).orElseThrow().screenId());

        router.back(viewer);

        assertEquals(HubScreen.ID, router.current(viewer.id()).orElseThrow().screenId());
    }

    @Test
    void backFromTheFirstScreenClosesTheMenu() {
        router.open(viewer, ScreenRequest.of(HubScreen.ID));

        router.back(viewer);

        assertTrue(router.current(viewer.id()).isEmpty());
        assertNull(platform.lastMenu(viewer));
    }

    @Test
    void reopeningTheSameScreenDoesNotGrowTheHistory() {
        ScreenRequest request = ScreenRequest.of(HubScreen.ID);
        router.open(viewer, request);
        router.open(viewer, request);

        router.back(viewer);

        assertTrue(router.current(viewer.id()).isEmpty());
    }

    @Test
    void openingAnUnknownScreenIsIgnored() {
        router.open(viewer, ScreenRequest.of("nope"));

        assertTrue(router.current(viewer.id()).isEmpty());
        assertNull(platform.lastMenu(viewer));
    }

    @Test
    void forgetDropsTheHistoryOfOnePlayer() {
        router.open(viewer, ScreenRequest.of(HubScreen.ID));

        router.forget(viewer.id());

        assertTrue(router.current(viewer.id()).isEmpty());
    }

    @Test
    void refreshUpdatesWithoutPlayingTheOpenSound() {
        router.open(viewer, ScreenRequest.of(HubScreen.ID));
        int afterOpen = platform.playedSounds().size();

        router.refresh(viewer);

        assertEquals(afterOpen, platform.playedSounds().size());
        assertFalse(platform.lastMenu(viewer).buttons().isEmpty());
    }
}
