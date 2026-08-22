package dev.delewer.letstroll.platform;

import java.util.Optional;

public interface ItemBindingService {

    boolean bindHeldItem(PlayerRef holder, String actionId);

    boolean unbindHeldItem(PlayerRef holder);

    Optional<String> heldBinding(PlayerRef holder);
}
