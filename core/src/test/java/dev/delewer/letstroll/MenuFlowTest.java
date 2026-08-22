package dev.delewer.letstroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import dev.delewer.letstroll.menu.ClickContext;
import dev.delewer.letstroll.menu.ClickKind;
import dev.delewer.letstroll.menu.Menu;
import dev.delewer.letstroll.menu.MenuButton;
import dev.delewer.letstroll.menu.MenuLayout;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.platform.MenuSound;
import dev.delewer.letstroll.support.FakePlatform;
import dev.delewer.letstroll.support.FakePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MenuFlowTest {

    @TempDir
    Path dataFolder;

    private FakePlatform platform;
    private LetsTroll core;
    private FakePlayer viewer;

    @BeforeEach
    void setUp() {
        platform = new FakePlatform(dataFolder);
        viewer = new FakePlayer("Delewer", LetsTroll.PERMISSION_USE, LetsTroll.PERMISSION_ADMIN,
                "letstroll.ghost", "letstroll.ghost.others");
        platform.addPlayers(viewer);
        core = new LetsTroll(platform);
        core.start();
    }

    @Test
    void discoversModulesFromTheIndex() {
        assertEquals(10, core.modules().count());
        assertTrue(core.router().has("hub"));
        assertTrue(core.router().has("players"));
        assertTrue(core.router().has("player"));
        assertTrue(core.router().has("settings"));
        assertTrue(core.router().has("modes"));
        assertTrue(core.router().has("chain"));
    }

    @Test
    void hubShowsEveryTabAndFillsTheRest() {
        core.router().open(viewer, ScreenRequest.of("hub"));
        Menu menu = platform.lastMenu(viewer);

        assertNotNull(menu);
        assertEquals(54, menu.size());
        assertTrue(menu.filler().isPresent());
        assertEquals(6, core.tabs().visibleFor(viewer).size());
        assertEquals(7, menu.buttons().size());
        assertTrue(menu.buttons().containsKey(MenuLayout.contentCorner(6)));
    }

    @Test
    void tabClickOpensTheTargetScreen() {
        core.router().open(viewer, ScreenRequest.of("hub"));
        Menu hub = platform.lastMenu(viewer);
        MenuButton playersTab = hub.buttons().values().stream()
                .filter(MenuButton::clickable)
                .filter(button -> button.icon().material().contains("player_head"))
                .findFirst()
                .orElseThrow();

        playersTab.click(new ClickContext(core, viewer, ClickKind.LEFT, ScreenRequest.of("hub"), core.router()));

        assertEquals("players", core.router().current(viewer.id()).orElseThrow().screenId());
        assertTrue(platform.lastMenu(viewer).buttons().values().stream()
                .anyMatch(button -> button.icon().isHead()));
    }

    @Test
    void playerListPaginatesAndKeepsHeads() {
        for (int index = 0; index < 40; index++) {
            platform.addPlayers(new FakePlayer("Player" + index));
        }

        core.router().open(viewer, ScreenRequest.of("players"));
        Menu first = platform.lastMenu(viewer);
        long headsOnFirstPage = first.buttons().values().stream().filter(button -> button.icon().head() != null).count();

        core.router().open(viewer, ScreenRequest.of("players").withPage(1));
        Menu second = platform.lastMenu(viewer);
        long headsOnSecondPage = second.buttons().values().stream().filter(button -> button.icon().head() != null).count();

        assertEquals(28, headsOnFirstPage);
        assertEquals(13, headsOnSecondPage);
    }

    @Test
    void backReturnsToThePreviousScreen() {
        core.router().open(viewer, ScreenRequest.of("hub"));
        core.router().open(viewer, ScreenRequest.of("players"));
        core.router().back(viewer);

        assertEquals("hub", core.router().current(viewer.id()).orElseThrow().screenId());
    }

    @Test
    void everyButtonStaysInsideTheFrame() {
        for (int index = 0; index < 40; index++) {
            platform.addPlayers(new FakePlayer("Player" + index));
        }
        core.router().open(viewer, ScreenRequest.of("players"));

        for (int slot : platform.lastMenu(viewer).buttons().keySet()) {
            int row = slot / 9;
            int column = slot % 9;
            boolean serviceRow = row == 5;
            assertTrue(serviceRow || (row > 0 && column > 0 && column < 8),
                    "button at slot " + slot + " touches the frame");
        }
    }

    @Test
    void searchFiltersTheList() {
        platform.addPlayers(new FakePlayer("Notch"), new FakePlayer("Herobrine"), new FakePlayer("Delewer2"));
        core.router().open(viewer, ScreenRequest.of("players"));
        platform.answerInputWith("dele");

        MenuButton search = platform.lastMenu(viewer).buttons().values().stream()
                .filter(button -> button.icon().material().contains("spyglass"))
                .findFirst()
                .orElseThrow();
        search.click(new ClickContext(core, viewer, ClickKind.LEFT, ScreenRequest.of("players"), core.router()));

        long heads = platform.lastMenu(viewer).buttons().values().stream()
                .filter(button -> button.icon().head() != null)
                .count();
        assertEquals(2, heads);
        assertEquals("dele", core.router().current(viewer.id()).orElseThrow().arg("filter").orElseThrow());
    }

    @Test
    void navigationArrowsUseCustomHeadTextures() {
        for (int index = 0; index < 40; index++) {
            platform.addPlayers(new FakePlayer("Player" + index));
        }
        core.router().open(viewer, ScreenRequest.of("players").withPage(1));

        boolean anyTexturedArrow = platform.lastMenu(viewer).buttons().values().stream()
                .anyMatch(button -> button.icon().hasTexture());
        assertTrue(anyTexturedArrow);
    }

    @Test
    void clicksPlaySounds() {
        core.router().open(viewer, ScreenRequest.of("hub"));
        assertTrue(platform.playedSounds().contains(MenuSound.OPEN));
    }

    @Test
    void settingsTabIsHiddenWithoutAdmin() {
        FakePlayer guest = new FakePlayer("Guest", LetsTroll.PERMISSION_USE);
        List<String> tabs = core.tabs().visibleFor(guest).stream().map(tab -> tab.id()).toList();

        assertTrue(tabs.contains("players"));
        assertFalse(tabs.contains("settings"));
    }
}
