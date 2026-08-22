package dev.delewer.letstroll.menu;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.platform.MenuSound;
import dev.delewer.letstroll.platform.PlayerRef;

public final class MenuRouter {

    private final LetsTroll core;
    private final Map<String, Screen> screens = new LinkedHashMap<>();
    private final Map<UUID, Deque<ScreenRequest>> history = new ConcurrentHashMap<>();

    public MenuRouter(LetsTroll core) {
        this.core = core;
    }

    public void register(Screen screen) {
        screens.put(screen.id(), screen);
    }

    public boolean has(String screenId) {
        return screens.containsKey(screenId);
    }

    public Optional<Screen> screen(String screenId) {
        return Optional.ofNullable(screens.get(screenId));
    }

    public void open(PlayerRef viewer, String screenId) {
        open(viewer, ScreenRequest.of(screenId));
    }

    public void open(PlayerRef viewer, ScreenRequest request) {
        Screen screen = screens.get(request.screenId());
        if (screen == null) {
            return;
        }
        Deque<ScreenRequest> stack = history.computeIfAbsent(viewer.id(), key -> new ArrayDeque<>());
        ScreenRequest current = stack.peek();
        if (current == null || !current.screenId().equals(request.screenId()) || !current.args().equals(request.args())) {
            stack.push(request);
        } else {
            stack.pop();
            stack.push(request);
        }
        render(viewer, screen, request);
    }

    public void back(PlayerRef viewer) {
        Deque<ScreenRequest> stack = history.get(viewer.id());
        if (stack == null || stack.size() < 2) {
            close(viewer);
            return;
        }
        stack.pop();
        ScreenRequest previous = stack.peek();
        Screen screen = previous == null ? null : screens.get(previous.screenId());
        if (screen == null) {
            close(viewer);
            return;
        }
        render(viewer, screen, previous);
    }

    public void refresh(PlayerRef viewer) {
        Deque<ScreenRequest> stack = history.get(viewer.id());
        ScreenRequest current = stack == null ? null : stack.peek();
        if (current == null) {
            return;
        }
        Screen screen = screens.get(current.screenId());
        if (screen != null) {
            render(viewer, screen, current, true);
        }
    }

    public void refreshAll() {
        for (UUID id : Set.copyOf(history.keySet())) {
            core.platform().players().byId(id).ifPresentOrElse(this::refresh, () -> history.remove(id));
        }
    }

    public void close(PlayerRef viewer) {
        history.remove(viewer.id());
        core.platform().menus().close(viewer);
    }

    public void forget(UUID viewer) {
        history.remove(viewer);
    }

    public Optional<ScreenRequest> current(UUID viewer) {
        Deque<ScreenRequest> stack = history.get(viewer);
        return Optional.ofNullable(stack == null ? null : stack.peek());
    }

    private void render(PlayerRef viewer, Screen screen, ScreenRequest request) {
        render(viewer, screen, request, false);
    }

    private void render(PlayerRef viewer, Screen screen, ScreenRequest request, boolean silent) {
        Menu menu = screen.build(new ScreenContext(core, viewer, request, this));
        if (silent) {
            core.platform().menus().update(viewer, menu);
            return;
        }
        core.platform().menus().open(viewer, menu);
        core.platform().sounds().play(viewer, MenuSound.OPEN);
    }
}
