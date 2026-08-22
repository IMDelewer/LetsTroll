package dev.delewer.letstroll.paper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.destroystokyo.paper.profile.ProfileProperty;
import dev.delewer.letstroll.platform.FakePlayerService;
import dev.delewer.letstroll.platform.FakePlayerSpec;
import dev.delewer.letstroll.platform.PlayerRef;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public final class PaperFakePlayers implements FakePlayerService {

    private final JavaPlugin plugin;
    private final SkinResolver skins;
    private final Map<UUID, List<UUID>> spawned = new ConcurrentHashMap<>();

    public PaperFakePlayers(JavaPlugin plugin, SkinResolver skins) {
        this.plugin = plugin;
        this.skins = skins;
    }

    @Override
    public void spawn(PlayerRef target, FakePlayerSpec spec) {
        Player player = (Player) target.handle();
        if (player == null) {
            return;
        }

        Optional<String> known = spec.copyTargetSkin()
                ? skins.fromProfile(player.getPlayerProfile())
                : SkinResolver.immediateTexture(spec.skinOwner());

        Mannequin mannequin = doSpawn(target, spec, known.orElse(null));
        if (mannequin == null || known.isPresent()) {
            return;
        }

        String name = spec.copyTargetSkin() ? player.getName() : SkinResolver.nameFrom(spec.skinOwner());
        UUID entityId = mannequin.getUniqueId();
        skins.byName(name).whenComplete((texture, error) -> {
            if (texture == null || texture.isEmpty()) {
                return;
            }
            PaperTasks.onEntity(plugin, mannequin, () -> {
                if (Bukkit.getEntity(entityId) instanceof Mannequin live) {
                    live.setProfile(texturedProfile(texture.get()));
                }
            });
        });
    }

    private Mannequin doSpawn(PlayerRef target, FakePlayerSpec spec, String texture) {
        Player player = (Player) target.handle();
        if (player == null || !player.isOnline()) {
            return null;
        }
        Location spot = inFrontOf(player, spec.distance(), spec.facePlayer());
        ResolvableProfile profile = texture != null && !texture.isBlank()
                ? texturedProfile(texture)
                : ResolvableProfile.resolvableProfile(player.getPlayerProfile());

        Mannequin mannequin = player.getWorld().spawn(spot, Mannequin.class, entity -> {
            entity.setProfile(profile);
            entity.setDescription(Component.empty());
            entity.setCustomNameVisible(false);
            entity.setImmovable(true);
            entity.setInvulnerable(true);
            entity.setSilent(true);
            entity.setPersistent(false);
        });

        if (spec.visibleOnlyToTarget()) {
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.getUniqueId().equals(player.getUniqueId())) {
                    other.hideEntity(plugin, mannequin);
                }
            }
        }

        spawned.computeIfAbsent(target.id(), key -> new ArrayList<>()).add(mannequin.getUniqueId());
        PaperTasks.onEntityLater(plugin, mannequin, () -> remove(target.id(), mannequin.getUniqueId()), spec.lifetimeTicks());
        return mannequin;
    }

    @Override
    public void despawnAll(PlayerRef target) {
        List<UUID> ids = spawned.remove(target.id());
        if (ids == null) {
            return;
        }
        ids.forEach(this::removeEntity);
    }

    public void despawnEverything() {
        for (UUID owner : Map.copyOf(spawned).keySet()) {
            List<UUID> ids = spawned.remove(owner);
            if (ids != null) {
                ids.forEach(this::removeEntity);
            }
        }
    }

    private void remove(UUID owner, UUID entityId) {
        List<UUID> ids = spawned.get(owner);
        if (ids != null) {
            ids.remove(entityId);
            if (ids.isEmpty()) {
                spawned.remove(owner);
            }
        }
        removeEntity(entityId);
    }

    private void removeEntity(UUID entityId) {
        if (Bukkit.getEntity(entityId) instanceof Mannequin mannequin) {
            mannequin.remove();
        }
    }

    private Location inFrontOf(Player player, double distance, boolean facePlayer) {
        Location eye = player.getLocation();
        double radians = Math.toRadians(eye.getYaw());
        Vector direction = new Vector(-Math.sin(radians), 0.0, Math.cos(radians)).multiply(distance);
        Location spot = eye.clone().add(direction);
        spot.setYaw(facePlayer ? eye.getYaw() + 180.0f : eye.getYaw());
        spot.setPitch(0.0f);
        return spot;
    }

    private ResolvableProfile texturedProfile(String texture) {
        return ResolvableProfile.resolvableProfile()
                .uuid(UUID.randomUUID())
                .addProperty(new ProfileProperty("textures", texture))
                .build();
    }
}
