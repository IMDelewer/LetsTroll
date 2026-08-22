package dev.delewer.letstroll.platform;

import java.util.Optional;

public interface MovementService {

    Optional<Position> positionOf(PlayerRef player);

    void teleport(PlayerRef player, Position position);

    void push(PlayerRef player, double x, double y, double z);
}
