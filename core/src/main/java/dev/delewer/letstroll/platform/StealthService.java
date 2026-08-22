package dev.delewer.letstroll.platform;

import java.util.Set;
import java.util.UUID;

public interface StealthService {

    void hide(PlayerRef player, StealthOptions options);

    void reveal(PlayerRef player);

    void intend(UUID id, StealthOptions options);

    boolean hidden(UUID id);

    Set<UUID> hiddenPlayers();
}
