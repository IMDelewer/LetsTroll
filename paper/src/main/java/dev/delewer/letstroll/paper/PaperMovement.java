package dev.delewer.letstroll.paper;

import java.util.Optional;

import dev.delewer.letstroll.platform.MovementService;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.Position;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class PaperMovement implements MovementService {

    @Override
    public Optional<Position> positionOf(PlayerRef player) {
        Player bukkit = (Player) player.handle();
        if (bukkit == null) {
            return Optional.empty();
        }
        Location location = bukkit.getLocation();
        return Optional.of(new Position(location.getWorld().getName(), location.getX(), location.getY(),
                location.getZ(), location.getYaw(), location.getPitch()));
    }

    @Override
    public void teleport(PlayerRef player, Position position) {
        Player bukkit = (Player) player.handle();
        if (bukkit == null) {
            return;
        }
        World world = Bukkit.getWorld(position.world());
        if (world == null) {
            return;
        }
        bukkit.teleport(new Location(world, position.x(), position.y(), position.z(), position.yaw(), position.pitch()));
    }

    @Override
    public void push(PlayerRef player, double x, double y, double z) {
        Player bukkit = (Player) player.handle();
        if (bukkit != null) {
            bukkit.setVelocity(bukkit.getVelocity().add(new Vector(x, y, z)));
        }
    }
}
