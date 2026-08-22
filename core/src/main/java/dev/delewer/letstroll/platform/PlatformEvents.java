package dev.delewer.letstroll.platform;

import java.util.function.Consumer;

public interface PlatformEvents {

    void onJoin(Consumer<PlayerRef> listener);

    void onQuit(Consumer<PlayerRef> listener);
}
