package dev.delewer.letstroll.paper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import dev.delewer.letstroll.platform.ChainOps;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.Position;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class PaperChain implements ChainOps {

    private final Map<String, List<BlockDisplay>> chains = new ConcurrentHashMap<>();
    private final Map<String, BlockData> blocks = new ConcurrentHashMap<>();

    @Override
    public String inventoryHash(PlayerRef player) {
        Player bukkit = player(player);
        if (bukkit == null) {
            return "";
        }
        return Arrays.deepHashCode(bukkit.getInventory().getContents()) + ":" + bukkit.getInventory().getHeldItemSlot();
    }

    @Override
    public void copyInventory(PlayerRef from, PlayerRef to) {
        Player source = player(from);
        Player target = player(to);
        if (source == null || target == null) {
            return;
        }
        ItemStack[] contents = source.getInventory().getContents();
        ItemStack[] copy = new ItemStack[contents.length];
        for (int index = 0; index < contents.length; index++) {
            copy[index] = contents[index] == null ? null : contents[index].clone();
        }
        target.getInventory().setContents(copy);
        target.getInventory().setHeldItemSlot(source.getInventory().getHeldItemSlot());
    }

    @Override
    public void setHealth(PlayerRef player, double health) {
        Player bukkit = player(player);
        if (bukkit != null) {
            bukkit.setHealth(Math.max(0.0, Math.min(bukkit.getMaxHealth(), health)));
        }
    }

    @Override
    public int food(PlayerRef player) {
        Player bukkit = player(player);
        return bukkit == null ? 20 : bukkit.getFoodLevel();
    }

    @Override
    public void setFood(PlayerRef player, int food) {
        Player bukkit = player(player);
        if (bukkit != null) {
            int clamped = Math.max(0, Math.min(20, food));
            bukkit.setFoodLevel(clamped);
            bukkit.setSaturation(Math.min(bukkit.getSaturation(), clamped));
        }
    }

    @Override
    public void copyPotions(PlayerRef from, PlayerRef to) {
        Player source = player(from);
        Player target = player(to);
        if (source == null || target == null) {
            return;
        }
        for (PotionEffect effect : List.copyOf(target.getActivePotionEffects())) {
            target.removePotionEffect(effect.getType());
        }
        for (PotionEffect effect : source.getActivePotionEffects()) {
            target.addPotionEffect(effect);
        }
    }

    @Override
    public void kill(PlayerRef player) {
        Player bukkit = player(player);
        if (bukkit != null) {
            bukkit.setHealth(0.0);
        }
    }

    @Override
    public void drawChain(Collection<PlayerRef> viewers, List<Position> points) {
        for (PlayerRef viewer : viewers) {
            Player bukkit = player(viewer);
            if (bukkit == null) {
                continue;
            }
            for (Position point : points) {
                Location location = new Location(bukkit.getWorld(), point.x(), point.y(), point.z());
                bukkit.spawnParticle(Particle.CRIT, location, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public boolean supportsChainLinks() {
        return true;
    }

    @Override
    public void drawChainLinks(String linkId, List<Position> points, double thickness, String block) {
        if (points.size() < 2) {
            clearChainLinks(linkId);
            return;
        }
        World world = Bukkit.getWorld(points.get(0).world());
        if (world == null) {
            clearChainLinks(linkId);
            return;
        }

        List<BlockDisplay> links = chains.computeIfAbsent(linkId, key -> new ArrayList<>());
        int wanted = points.size() - 1;
        links.removeIf(display -> !display.isValid());

        while (links.size() > wanted) {
            links.remove(links.size() - 1).remove();
        }
        BlockData data = blockOf(block);
        while (links.size() < wanted) {
            links.add(spawnLink(world, points.get(0), data));
        }

        float width = (float) thickness;
        for (int index = 0; index < wanted; index++) {
            Position from = points.get(index);
            Position to = points.get(index + 1);
            shapeLink(links.get(index), world, from, to, width);
        }
    }

    @Override
    public void clearChainLinks(String linkId) {
        List<BlockDisplay> links = chains.remove(linkId);
        if (links == null) {
            return;
        }
        links.forEach(display -> {
            if (display.isValid()) {
                display.remove();
            }
        });
    }

    @Override
    public void clearAllChainLinks() {
        for (String key : Set.copyOf(chains.keySet())) {
            clearChainLinks(key);
        }
    }

    private BlockData blockOf(String key) {
        return blocks.computeIfAbsent(key, name -> {
            Material material = Material.matchMaterial(name);
            if (material == null || !material.isBlock()) {
                material = Material.matchMaterial("minecraft:iron_chain");
            }
            return (material == null ? Material.IRON_BARS : material).createBlockData();
        });
    }

    private BlockDisplay spawnLink(World world, Position at, BlockData data) {
        return world.spawn(new Location(world, at.x(), at.y(), at.z()), BlockDisplay.class, display -> {
            display.setBlock(data);
            display.setPersistent(false);
            display.setViewRange(2.0f);
            display.setInterpolationDuration(1);
            display.setBrightness(new Display.Brightness(15, 15));
        });
    }

    private void shapeLink(BlockDisplay display, World world, Position from, Position to, float width) {
        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double dz = to.z() - from.z();
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0e-4) {
            return;
        }

        Quaternionf rotation = new Quaternionf().rotationTo(
                new Vector3f(0.0f, 1.0f, 0.0f),
                new Vector3f((float) (dx / length), (float) (dy / length), (float) (dz / length)));
        Vector3f offset = rotation.transform(new Vector3f(-width / 2.0f, 0.0f, -width / 2.0f));

        display.teleport(new Location(world, from.x(), from.y(), from.z()));
        display.setInterpolationDelay(0);
        display.setTransformation(new Transformation(
                offset, rotation, new Vector3f(width, (float) length, width), new Quaternionf()));
    }

    private Player player(PlayerRef ref) {
        return ref == null ? null : (Player) ref.handle();
    }
}
