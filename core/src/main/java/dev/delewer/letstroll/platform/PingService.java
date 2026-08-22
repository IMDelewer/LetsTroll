package dev.delewer.letstroll.platform;

import java.util.UUID;

public interface PingService {

    void setFake(PlayerRef target, int milliseconds);

    void clear(PlayerRef target);

    boolean isFaked(UUID id);

    void clearAll();
}
