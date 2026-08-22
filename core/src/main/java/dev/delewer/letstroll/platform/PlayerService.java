package dev.delewer.letstroll.platform;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerService {

    List<PlayerRef> online();

    Optional<PlayerRef> byId(UUID id);

    Optional<PlayerRef> byName(String name);
}
