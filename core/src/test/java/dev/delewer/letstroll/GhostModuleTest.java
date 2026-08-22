package dev.delewer.letstroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import dev.delewer.letstroll.menu.ClickContext;
import dev.delewer.letstroll.menu.ClickKind;
import dev.delewer.letstroll.menu.Menu;
import dev.delewer.letstroll.menu.MenuButton;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.modules.ghost.GhostConfig;
import dev.delewer.letstroll.modules.ghost.GhostService;
import dev.delewer.letstroll.platform.StealthOptions;
import dev.delewer.letstroll.support.FakePlatform;
import dev.delewer.letstroll.support.FakePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GhostModuleTest {

    @TempDir
    Path dataFolder;

    private FakePlatform platform;
    private LetsTroll core;
    private FakePlayer viewer;
    private FakePlayer target;

    @BeforeEach
    void setUp() {
        platform = new FakePlatform(dataFolder);
        viewer = new FakePlayer("Delewer", LetsTroll.PERMISSION_USE, "letstroll.ghost", "letstroll.ghost.others");
        target = new FakePlayer("Victim");
        platform.addPlayers(viewer, target);
        core = new LetsTroll(platform);
        core.start();
    }

    @Test
    void ghostActionHidesTheTargetWithEveryOption() {
        core.router().open(viewer, ScreenRequest.of("player", "target", target.id().toString()));
        MenuButton ghostButton = ghostButton();

        ghostButton.click(new ClickContext(core, viewer, ClickKind.LEFT,
                ScreenRequest.of("player", "target", target.id().toString()), core.router()));

        assertTrue(platform.stealthService().hidden(target.id()));
        StealthOptions options = platform.stealthService().optionsOf(target.id()).orElseThrow();
        assertTrue(options.hideEntity());
        assertTrue(options.hideFromTab());
        assertTrue(options.silentJoinQuit());
        assertFalse(options.creative());
    }

    @Test
    void secondClickRevealsTheTarget() {
        ScreenRequest request = ScreenRequest.of("player", "target", target.id().toString());
        core.router().open(viewer, request);
        ghostButton().click(new ClickContext(core, viewer, ClickKind.LEFT, request, core.router()));
        ghostButton().click(new ClickContext(core, viewer, ClickKind.LEFT, request, core.router()));

        assertFalse(platform.stealthService().hidden(target.id()));
    }

    @Test
    void ghostStateSurvivesRestartAndRejoin() throws Exception {
        GhostService service = new GhostService(platform, new GhostConfig());
        service.enable(target);

        Path storage = dataFolder.resolve("data").resolve("ghosts.txt");
        assertTrue(Files.exists(storage));
        assertEquals(target.id().toString(), Files.readString(storage).trim());

        platform.stealthService().reveal(target);
        GhostService restarted = new GhostService(platform, new GhostConfig());
        restarted.load();
        restarted.registerIntents();

        assertTrue(platform.stealthService().hidden(target.id()));
    }

    @Test
    void ghostActionIsHiddenWithoutTheGhostPermission() {
        FakePlayer stranger = new FakePlayer("Stranger", LetsTroll.PERMISSION_USE);
        assertTrue(core.playerActions().visibleFor(stranger).stream().noneMatch(action -> action.id().equals("ghost")));
    }

    @Test
    void playersWithoutPermissionCannotTrollOthers() {
        FakePlayer guest = new FakePlayer("Guest", LetsTroll.PERMISSION_USE, "letstroll.ghost");
        platform.addPlayers(guest);
        ScreenRequest request = ScreenRequest.of("player", "target", target.id().toString());
        core.router().open(guest, request);

        ghostButtonFor(guest).click(new ClickContext(core, guest, ClickKind.LEFT, request, core.router()));

        assertFalse(platform.stealthService().hidden(target.id()));
        assertEquals(1, guest.messages().size());
    }

    private MenuButton ghostButton() {
        return ghostButtonFor(viewer);
    }

    private MenuButton ghostButtonFor(FakePlayer player) {
        Menu menu = platform.lastMenu(player);
        return menu.buttons().values().stream()
                .filter(MenuButton::clickable)
                .filter(button -> button.icon().material().contains("ender"))
                .findFirst()
                .orElseThrow();
    }
}
