package dev.delewer.letstroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import dev.delewer.letstroll.menu.ClickContext;
import dev.delewer.letstroll.menu.ClickKind;
import dev.delewer.letstroll.menu.MenuButton;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.modules.mannequin.MannequinModule;
import dev.delewer.letstroll.platform.FakePlayerSpec;
import dev.delewer.letstroll.support.FakePlatform;
import dev.delewer.letstroll.support.FakePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MannequinModuleTest {

    @TempDir
    Path dataFolder;

    private FakePlatform platform;
    private LetsTroll core;
    private FakePlayer viewer;
    private FakePlayer target;

    @BeforeEach
    void setUp() {
        platform = new FakePlatform(dataFolder);
        viewer = new FakePlayer("Delewer", LetsTroll.PERMISSION_USE, MannequinModule.PERMISSION);
        target = new FakePlayer("Victim");
        platform.addPlayers(viewer, target);
        core = new LetsTroll(platform);
        core.start();
    }

    @Test
    void presetSpawnsAFakePlayerOnlyForTheVictim() {
        ScreenRequest request = ScreenRequest.of("mannequin", "target", target.id().toString());
        core.router().open(viewer, request);

        MenuButton mirror = presetButtons().getFirst();
        mirror.click(new ClickContext(core, viewer, ClickKind.LEFT, request, core.router()));

        List<FakePlayerSpec> spawned = platform.fakePlayersOf(target);
        assertEquals(1, spawned.size());
        assertTrue(spawned.getFirst().visibleOnlyToTarget());
        assertTrue(spawned.getFirst().copyTargetSkin());
    }

    @Test
    void clearRemovesEverySpawnedCopy() {
        ScreenRequest request = ScreenRequest.of("mannequin", "target", target.id().toString());
        core.router().open(viewer, request);
        presetButtons().getFirst().click(new ClickContext(core, viewer, ClickKind.LEFT, request, core.router()));

        MenuButton clear = platform.lastMenu(viewer).buttons().values().stream()
                .filter(button -> button.icon().material().contains("bone"))
                .findFirst()
                .orElseThrow();
        clear.click(new ClickContext(core, viewer, ClickKind.LEFT, request, core.router()));

        assertTrue(platform.fakePlayersOf(target).isEmpty());
    }

    @Test
    void actionIsHiddenWithoutPermission() {
        FakePlayer guest = new FakePlayer("Guest", LetsTroll.PERMISSION_USE);
        List<String> actions = core.playerActions().visibleFor(guest).stream().map(action -> action.id()).toList();

        assertTrue(actions.isEmpty());
    }

    private List<MenuButton> presetButtons() {
        return platform.lastMenu(viewer).buttons().values().stream()
                .filter(MenuButton::clickable)
                .filter(button -> button.icon().isHead())
                .toList();
    }
}
