package dev.delewer.letstroll.modules.events;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import dev.delewer.letstroll.platform.EffectsService;
import dev.delewer.letstroll.platform.MovementService;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.Position;

public final class EventLibrary {

    private static final List<String> EFFECTS = List.of(
            "minecraft:blindness", "minecraft:nausea", "minecraft:levitation",
            "minecraft:slowness", "minecraft:darkness");
    private static final List<String> MOBS = List.of(
            "minecraft:zombie", "minecraft:spider", "minecraft:silverfish", "minecraft:bee");
    private static final List<String> FAKE_NAMES = List.of(
            "Notch", "Herobrine", "Steve", "Alex", "Dinnerbone");

    private EventLibrary() {
    }

    public static List<TrollEvent> all() {
        return List.of(
                event("jumpscare", false, true, TrollEvent.TargetMode.ALL, context -> {
                    for (PlayerRef target : context.targets()) {
                        EffectsService effects = context.platform().effects();
                        effects.sound(target, "minecraft:entity.enderman.scream", 1.0f, 0.6f);
                        effects.potion(target, "minecraft:darkness", 60, 0);
                    }
                }),
                event("random_effects", false, true, TrollEvent.TargetMode.ALL, context -> {
                    for (PlayerRef target : context.targets()) {
                        String effect = EFFECTS.get(ThreadLocalRandom.current().nextInt(EFFECTS.size()));
                        context.platform().effects().potion(target, effect, 100, 0);
                    }
                }),
                event("fake_chat", false, true, TrollEvent.TargetMode.ALL, context -> {
                    String name = FAKE_NAMES.get(ThreadLocalRandom.current().nextInt(FAKE_NAMES.size()));
                    context.platform().effects().broadcast(dev.delewer.letstroll.text.Text.mini(
                            "<yellow>" + name + " joined the game"));
                }),
                event("fake_lag", false, true, TrollEvent.TargetMode.ALL, context -> {
                    for (PlayerRef target : context.targets()) {
                        context.platform().ping().setFake(target, 900 + ThreadLocalRandom.current().nextInt(400));
                    }
                }),

                event("mob_crowd", true, false, TrollEvent.TargetMode.ALL, context -> {
                    String mob = MOBS.get(ThreadLocalRandom.current().nextInt(MOBS.size()));
                    for (PlayerRef target : context.targets()) {
                        context.platform().effects().spawnMobs(target, mob, 6, 2.0);
                    }
                }),
                event("fall_to_bedrock", true, false, TrollEvent.TargetMode.ALL, context -> {
                    for (PlayerRef target : context.targets()) {
                        String token = context.platform().effects().wipeColumn(target, 1);
                        context.core().platform().scheduler().later(
                                () -> context.platform().effects().restore(token), 200L);
                    }
                }),
                event("chunk_wipe", true, false, TrollEvent.TargetMode.ALL, context ->
                        context.targets().forEach(target -> context.platform().effects().wipeChunk(target))),
                event("real_boom", true, false, TrollEvent.TargetMode.ALL, context -> {
                    for (PlayerRef target : context.targets()) {
                        context.platform().effects().lightning(target, false);
                        context.platform().effects().explosion(target, 4.0f, true, true);
                    }
                }),
                event("swap", true, false, TrollEvent.TargetMode.RANDOM_PAIR, context -> {
                    List<PlayerRef> targets = context.targets();
                    if (targets.size() < 2) {
                        return;
                    }
                    MovementService movement = context.platform().movement();
                    PlayerRef first = targets.get(0);
                    PlayerRef second = targets.get(1);
                    Position firstPos = movement.positionOf(first).orElse(null);
                    Position secondPos = movement.positionOf(second).orElse(null);
                    if (firstPos != null && secondPos != null) {
                        movement.teleport(first, secondPos);
                        movement.teleport(second, firstPos);
                    }
                }),

                event("launch_up", false, true, TrollEvent.TargetMode.ALL, context ->
                        context.targets().forEach(t -> context.platform().effects().launch(t, 1.6))),
                event("random_tp", false, true, TrollEvent.TargetMode.ALL, context ->
                        context.targets().forEach(t -> context.platform().effects().teleportRandom(t, 8.0))),
                event("gather", false, true, TrollEvent.TargetMode.ALL, context -> {
                    List<PlayerRef> all = context.targets();
                    if (all.isEmpty()) {
                        return;
                    }
                    PlayerRef anchor = all.get(ThreadLocalRandom.current().nextInt(all.size()));
                    all.forEach(t -> context.platform().effects().teleportTo(t, anchor));
                }),
                event("scatter", false, true, TrollEvent.TargetMode.ALL, context ->
                        context.targets().forEach(t -> context.platform().effects().teleportRandom(t, 20.0))),
                event("gift", false, true, TrollEvent.TargetMode.ALL, context ->
                        context.targets().forEach(t -> context.platform().effects().giveItem(t, "", 1))),
                event("item_rain", false, true, TrollEvent.TargetMode.ALL, context ->
                        context.targets().forEach(t -> context.platform().effects().dropItems(t, 12))),
                event("heal_feed", false, true, TrollEvent.TargetMode.ALL, context ->
                        context.targets().forEach(t -> context.platform().effects().heal(t))),
                event("fireworks", false, true, TrollEvent.TargetMode.ALL, context ->
                        context.targets().forEach(t -> context.platform().effects().firework(t))),
                event("freeze_all", false, true, TrollEvent.TargetMode.ALL, context ->
                        context.targets().forEach(t -> context.platform().effects().freeze(t, 60))),
                event("speed_chaos", false, true, TrollEvent.TargetMode.ALL, context -> {
                    for (PlayerRef t : context.targets()) {
                        context.platform().effects().potion(t, "minecraft:speed", 200, 3);
                        context.platform().effects().potion(t, "minecraft:jump_boost", 200, 3);
                    }
                }),
                event("anonymous", false, true, TrollEvent.TargetMode.ALL, context ->
                        context.targets().forEach(t -> context.platform().effects().anonymize(t, 400))),
                event("spin", false, true, TrollEvent.TargetMode.ALL, context ->
                        context.targets().forEach(t -> context.platform().effects().spin(t, 40))),
                event("fake_creeper", false, true, TrollEvent.TargetMode.ALL, context -> {
                    for (PlayerRef t : context.targets()) {
                        context.platform().effects().sound(t, "minecraft:entity.creeper.primed", 1.0f, 1.0f);
                        context.platform().effects().sound(t, "minecraft:entity.generic.explode", 1.0f, 1.0f);
                    }
                }),
                event("swap_inventory", true, false, TrollEvent.TargetMode.RANDOM_PAIR, context -> {
                    List<PlayerRef> targets = context.targets();
                    if (targets.size() >= 2) {
                        context.platform().effects().swapInventory(targets.get(0), targets.get(1));
                    }
                }),
                event("storm_night", false, true, TrollEvent.TargetMode.RANDOM_ONE, context ->
                        context.targets().forEach(t -> context.platform().effects().weatherStorm(t, 1200))),
                event("scramble_inventory", false, true, TrollEvent.TargetMode.ALL, context ->
                        context.targets().forEach(t -> context.platform().effects().scrambleInventory(t))),
                event("glowing", false, true, TrollEvent.TargetMode.ALL, context ->
                        context.targets().forEach(t -> context.platform().effects().potion(t, "minecraft:glowing", 300, 0)))
        );
    }

    private static TrollEvent event(String id, boolean dangerous, boolean defaultEnabled,
                                    TrollEvent.TargetMode mode, java.util.function.Consumer<EventContext> action) {
        return new TrollEvent() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public boolean dangerous() {
                return dangerous;
            }

            @Override
            public boolean defaultEnabled() {
                return defaultEnabled;
            }

            @Override
            public TargetMode targetMode() {
                return mode;
            }

            @Override
            public void run(EventContext context) {
                action.accept(context);
            }
        };
    }
}
