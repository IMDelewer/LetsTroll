package dev.delewer.letstroll.platform;

import java.util.Collection;
import java.util.List;

public interface ChainOps {

    String inventoryHash(PlayerRef player);

    void copyInventory(PlayerRef from, PlayerRef to);

    void setHealth(PlayerRef player, double health);

    int food(PlayerRef player);

    void setFood(PlayerRef player, int food);

    void copyPotions(PlayerRef from, PlayerRef to);

    void kill(PlayerRef player);

    void drawChain(Collection<PlayerRef> viewers, List<Position> points);

    default boolean supportsChainLinks() {
        return false;
    }

    default void drawChainLinks(String linkId, List<Position> points, double thickness, String block) {
    }

    default void clearChainLinks(String linkId) {
    }

    default void clearAllChainLinks() {
    }
}
