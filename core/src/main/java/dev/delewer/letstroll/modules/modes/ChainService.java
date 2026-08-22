package dev.delewer.letstroll.modules.modes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.platform.ChainOps;
import dev.delewer.letstroll.platform.MovementService;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.Position;
import dev.delewer.letstroll.platform.TaskScheduler;

public final class ChainService {

    static final class Link {
        final UUID a;
        final UUID b;
        String hashA;
        String hashB;
        double healthA = -1;
        double healthB = -1;
        int foodA = -1;
        int foodB = -1;
        boolean seeded;
        long tick;
        Position lastA;
        Position lastB;
        boolean taut;

        Link(UUID a, UUID b) {
            this.a = a;
            this.b = b;
        }
    }

    private record Velocity(double x, double y, double z) {

        static final Velocity ZERO = new Velocity(0, 0, 0);

        static Velocity between(Position from, Position to) {
            if (from == null || to == null || !from.world().equals(to.world())) {
                return ZERO;
            }
            return new Velocity(to.x() - from.x(), to.y() - from.y(), to.z() - from.z());
        }

        double speed() {
            return Math.sqrt(x * x + y * y + z * z);
        }
    }

    private final LetsTroll core;
    private final ChainConfig config;
    private final Map<UUID, Link> byPlayer = new LinkedHashMap<>();
    private final List<Link> links = new ArrayList<>();
    private TaskScheduler.Cancellable ticker;

    public ChainService(LetsTroll core, ChainConfig config) {
        this.core = core;
        this.config = config;
    }

    public void start() {
        stop();
        loadFromConfig();
        ticker = core.platform().scheduler().repeating(this::tick, 1L);
    }

    public void stop() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
        core.platform().chain().clearAllChainLinks();
    }

    public boolean isLinked(UUID id) {
        return byPlayer.containsKey(id);
    }

    public Optional<UUID> partner(UUID id) {
        Link link = byPlayer.get(id);
        return link == null ? Optional.empty() : Optional.of(id.equals(link.a) ? link.b : link.a);
    }

    public List<UUID[]> pairs() {
        List<UUID[]> result = new ArrayList<>();
        for (Link link : links) {
            result.add(new UUID[]{link.a, link.b});
        }
        return result;
    }

    public boolean link(UUID a, UUID b) {
        if (a.equals(b) || byPlayer.containsKey(a) || byPlayer.containsKey(b)) {
            return false;
        }
        Link link = new Link(a, b);
        links.add(link);
        byPlayer.put(a, link);
        byPlayer.put(b, link);
        persist();
        return true;
    }

    public void unlink(UUID id) {
        Link link = byPlayer.remove(id);
        if (link == null) {
            return;
        }
        byPlayer.remove(link.a);
        byPlayer.remove(link.b);
        links.remove(link);
        core.platform().chain().clearChainLinks(idOf(link));
        persist();
    }

    private void loadFromConfig() {
        byPlayer.clear();
        links.clear();
        for (String pair : config.links()) {
            String[] parts = pair.split(":");
            if (parts.length != 2) {
                continue;
            }
            try {
                UUID a = UUID.fromString(parts[0]);
                UUID b = UUID.fromString(parts[1]);
                if (!byPlayer.containsKey(a) && !byPlayer.containsKey(b)) {
                    Link link = new Link(a, b);
                    links.add(link);
                    byPlayer.put(a, link);
                    byPlayer.put(b, link);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void persist() {
        List<String> stored = new ArrayList<>();
        for (Link link : links) {
            stored.add(link.a + ":" + link.b);
        }
        config.setLinks(stored);
        core.saveConfig();
    }

    private void tick() {
        if (links.isEmpty()) {
            return;
        }
        ChainOps ops = core.platform().chain();
        MovementService movement = core.platform().movement();
        for (Link link : List.copyOf(links)) {
            PlayerRef a = core.platform().players().byId(link.a).orElse(null);
            PlayerRef b = core.platform().players().byId(link.b).orElse(null);
            if (a == null || b == null) {
                ops.clearChainLinks(idOf(link));
                continue;
            }
            if (!link.seeded) {
                seed(ops, link, a, b);
                continue;
            }
            syncInventory(ops, link, a, b);
            syncHealth(ops, link, a, b);
            syncFood(ops, link, a, b);
            if (link.tick % 20 == 0) {
                ops.copyPotions(a, b);
            }
            tether(ops, movement, link, a, b);
            link.tick++;
        }
    }

    private void seed(ChainOps ops, Link link, PlayerRef a, PlayerRef b) {
        ops.copyInventory(a, b);
        ops.copyPotions(a, b);
        ops.setHealth(b, a.health());
        ops.setFood(b, ops.food(a));
        link.hashA = ops.inventoryHash(a);
        link.hashB = ops.inventoryHash(b);
        link.healthA = a.health();
        link.healthB = b.health();
        link.foodA = ops.food(a);
        link.foodB = ops.food(b);
        link.seeded = true;
    }

    private void syncInventory(ChainOps ops, Link link, PlayerRef a, PlayerRef b) {
        String ha = ops.inventoryHash(a);
        String hb = ops.inventoryHash(b);
        boolean ca = !ha.equals(link.hashA);
        boolean cb = !hb.equals(link.hashB);
        if (ca && !cb) {
            ops.copyInventory(a, b);
        } else if (cb && !ca) {
            ops.copyInventory(b, a);
        } else if (ca) {
            ops.copyInventory(a, b);
        }
        if (ca || cb) {
            link.hashA = ops.inventoryHash(a);
            link.hashB = ops.inventoryHash(b);
        }
    }

    private void syncHealth(ChainOps ops, Link link, PlayerRef a, PlayerRef b) {
        double ha = a.health();
        double hb = b.health();
        if (ha <= 0 || hb <= 0) {
            if (ha <= 0 && hb > 0) {
                ops.kill(b);
            } else if (hb <= 0 && ha > 0) {
                ops.kill(a);
            }
        } else {
            boolean ca = link.healthA < 0 || ha != link.healthA;
            boolean cb = link.healthB < 0 || hb != link.healthB;
            if (ca && !cb) {
                ops.setHealth(b, ha);
            } else if (cb && !ca) {
                ops.setHealth(a, hb);
            } else if (ca) {
                double min = Math.min(ha, hb);
                ops.setHealth(a, min);
                ops.setHealth(b, min);
            }
        }
        link.healthA = a.health();
        link.healthB = b.health();
    }

    private void syncFood(ChainOps ops, Link link, PlayerRef a, PlayerRef b) {
        int fa = ops.food(a);
        int fb = ops.food(b);
        boolean ca = link.foodA < 0 || fa != link.foodA;
        boolean cb = link.foodB < 0 || fb != link.foodB;
        if (ca && !cb) {
            ops.setFood(b, fa);
        } else if (cb && !ca) {
            ops.setFood(a, fb);
        } else if (ca) {
            int min = Math.min(fa, fb);
            ops.setFood(a, min);
            ops.setFood(b, min);
        }
        link.foodA = ops.food(a);
        link.foodB = ops.food(b);
    }

    private void tether(ChainOps ops, MovementService movement, Link link, PlayerRef a, PlayerRef b) {
        Position posA = movement.positionOf(a).orElse(null);
        Position posB = movement.positionOf(b).orElse(null);
        if (posA == null || posB == null) {
            return;
        }
        if (!posA.world().equals(posB.world())) {
            movement.teleport(b, posA);
            link.lastA = null;
            link.lastB = null;
            return;
        }

        Velocity velA = Velocity.between(link.lastA, posA);
        Velocity velB = Velocity.between(link.lastB, posB);
        link.lastA = posA;
        link.lastB = posB;

        double dx = posB.x() - posA.x();
        double dy = posB.y() - posA.y();
        double dz = posB.z() - posA.z();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double max = config.chainLength();

        if (distance > max * 2.0) {
            movement.teleport(b, posA);
            link.lastB = null;
            return;
        }

        boolean taut = distance > max;
        if (taut && distance > 0.0001) {
            double ux = dx / distance;
            double uy = dy / distance;
            double uz = dz / distance;
            applyTension(movement, a, b, ux, uy, uz, distance - max, velA, velB);
            if (!link.taut && config.tensionSound()) {
                core.platform().effects().sound(a, "minecraft:block.chain.break", 0.7f, 0.7f);
                core.platform().effects().sound(b, "minecraft:block.chain.break", 0.7f, 0.7f);
            }
        }
        link.taut = taut;

        if (!config.visualChain()) {
            ops.clearChainLinks(idOf(link));
            return;
        }
        List<Position> points = sample(posA, posB, distance, max);
        if (config.useLinks() && ops.supportsChainLinks()) {
            ops.drawChainLinks(idOf(link), points, config.linkThickness(), config.linkBlock());
        } else {
            ops.drawChain(List.of(a, b), points);
        }
    }

    private String idOf(Link link) {
        return link.a + ":" + link.b;
    }

    private void applyTension(MovementService movement, PlayerRef a, PlayerRef b,
                              double ux, double uy, double uz, double over,
                              Velocity velA, Velocity velB) {
        LinkMode mode = config.linkMode();
        double force = switch (mode) {
            case RIGID -> Math.min(1.4, 0.35 + over * 0.9);
            case ELASTIC -> Math.min(0.6, over * 0.18);
            case RUBBER -> Math.min(2.2, over * over * 0.12 + 0.25);
        };
        double vertical = mode == LinkMode.RUBBER ? 0.75 : 0.4;

        movement.push(a, ux * force, uy * force * vertical, uz * force);
        movement.push(b, -ux * force, -uy * force * vertical, -uz * force);

        if (!config.inertia()) {
            return;
        }
        double share = config.inertiaStrength();
        if (share <= 0.0) {
            return;
        }
        double alongA = velA.x() * ux + velA.y() * uy + velA.z() * uz;
        double alongB = velB.x() * ux + velB.y() * uy + velB.z() * uz;
        if (alongA < 0 && velA.speed() > velB.speed()) {
            double drag = Math.min(1.2, -alongA * share);
            movement.push(b, -ux * drag, -uy * drag * 0.3, -uz * drag);
        } else if (alongB > 0 && velB.speed() > velA.speed()) {
            double drag = Math.min(1.2, alongB * share);
            movement.push(a, ux * drag, uy * drag * 0.3, uz * drag);
        }
    }

    private List<Position> sample(Position from, Position to, double distance, double max) {
        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double dz = to.z() - from.z();
        int steps = Math.max(4, Math.min(64, (int) Math.ceil(distance / config.linkLength())));
        double slack = Math.max(0.0, 1.0 - distance / Math.max(0.0001, max));
        double drop = config.sag() * slack;
        List<Position> points = new ArrayList<>(steps);
        for (int index = 1; index < steps; index++) {
            double factor = (double) index / steps;
            double curve = 4.0 * factor * (1.0 - factor);
            points.add(new Position(from.world(),
                    from.x() + dx * factor,
                    from.y() + dy * factor + 1.0 - drop * curve,
                    from.z() + dz * factor,
                    0f, 0f));
        }
        return points;
    }
}
