package dev.delewer.letstroll.modules.events;

import java.util.List;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.TrollPlatform;

public record EventContext(LetsTroll core, List<PlayerRef> targets) {

    public TrollPlatform platform() {
        return core.platform();
    }
}
