package dev.delewer.letstroll.support;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

import dev.delewer.letstroll.menu.Menu;
import dev.delewer.letstroll.platform.ConfigStore;
import dev.delewer.letstroll.platform.FakePlayerService;
import dev.delewer.letstroll.platform.FakePlayerSpec;
import dev.delewer.letstroll.platform.MenuBackend;
import dev.delewer.letstroll.platform.MenuSound;
import dev.delewer.letstroll.platform.SoundService;
import dev.delewer.letstroll.platform.TextInputService;
import dev.delewer.letstroll.platform.MovementService;
import dev.delewer.letstroll.platform.BossBarService;
import dev.delewer.letstroll.platform.ChainOps;
import dev.delewer.letstroll.platform.EffectsService;
import dev.delewer.letstroll.platform.ItemBindingService;
import dev.delewer.letstroll.platform.PingService;
import dev.delewer.letstroll.platform.PlatformEvents;
import dev.delewer.letstroll.platform.Position;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.PlayerService;
import dev.delewer.letstroll.platform.StealthOptions;
import dev.delewer.letstroll.platform.StealthService;
import dev.delewer.letstroll.platform.TaskScheduler;
import dev.delewer.letstroll.platform.TrollPlatform;

public final class FakePlatform implements TrollPlatform {

    private final Path dataFolder;
    private final List<PlayerRef> players = new ArrayList<>();
    private final Map<UUID, Menu> opened = new LinkedHashMap<>();
    private final FakeStealth stealth = new FakeStealth();
    private final List<Consumer<PlayerRef>> joinListeners = new ArrayList<>();
    private final List<MenuSound> sounds = new ArrayList<>();
    private final Map<UUID, List<FakePlayerSpec>> fakes = new LinkedHashMap<>();
    private String inputAnswer = "";

    public FakePlatform(Path dataFolder) {
        this.dataFolder = dataFolder;
    }

    public void addPlayers(PlayerRef... refs) {
        players.addAll(List.of(refs));
    }

    public Menu lastMenu(PlayerRef viewer) {
        return opened.get(viewer.id());
    }

    public FakeStealth stealthService() {
        return stealth;
    }

    public List<Consumer<PlayerRef>> joinListeners() {
        return joinListeners;
    }

    public List<MenuSound> playedSounds() {
        return sounds;
    }

    public List<FakePlayerSpec> fakePlayersOf(PlayerRef target) {
        return fakes.getOrDefault(target.id(), List.of());
    }

    public void answerInputWith(String value) {
        this.inputAnswer = value;
    }

    @Override
    public String name() {
        return "Fake";
    }

    @Override
    public String version() {
        return "test";
    }

    @Override
    public Path dataFolder() {
        return dataFolder;
    }

    @Override
    public Logger logger() {
        return Logger.getLogger("letstroll-test");
    }

    @Override
    public ConfigStore configs() {
        return new ConfigStore() {
            @Override
            public <T> T load(Class<T> type) {
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (ReflectiveOperationException exception) {
                    throw new IllegalStateException(exception);
                }
            }

            @Override
            public void save(Object config) {
            }

            @Override
            public void reloadAll() {
            }
        };
    }

    @Override
    public PlayerService players() {
        return new PlayerService() {
            @Override
            public List<PlayerRef> online() {
                return List.copyOf(players);
            }

            @Override
            public Optional<PlayerRef> byId(UUID id) {
                return players.stream().filter(player -> player.id().equals(id)).findFirst();
            }

            @Override
            public Optional<PlayerRef> byName(String name) {
                return players.stream().filter(player -> player.name().equalsIgnoreCase(name)).findFirst();
            }
        };
    }

    @Override
    public MenuBackend menus() {
        return new MenuBackend() {
            @Override
            public void open(PlayerRef viewer, Menu menu) {
                opened.put(viewer.id(), menu);
            }

            @Override
            public void update(PlayerRef viewer, Menu menu) {
                opened.put(viewer.id(), menu);
            }

            @Override
            public void close(PlayerRef viewer) {
                opened.remove(viewer.id());
            }
        };
    }

    @Override
    public StealthService stealth() {
        return stealth;
    }

    @Override
    public TaskScheduler scheduler() {
        return new TaskScheduler() {
            @Override
            public void sync(Runnable task) {
                task.run();
            }

            @Override
            public void later(Runnable task, long ticks) {
                task.run();
            }

            @Override
            public void async(Runnable task) {
                task.run();
            }

            @Override
            public Cancellable repeating(Runnable task, long intervalTicks) {
                return () -> {
                };
            }
        };
    }

    @Override
    public SoundService sounds() {
        return (listener, sound) -> sounds.add(sound);
    }

    @Override
    public TextInputService input() {
        return (viewer, prompt, initial, onConfirm) -> onConfirm.accept(inputAnswer);
    }

    @Override
    public FakePlayerService fakePlayers() {
        return new FakePlayerService() {
            @Override
            public void spawn(PlayerRef target, FakePlayerSpec spec) {
                fakes.computeIfAbsent(target.id(), key -> new ArrayList<>()).add(spec);
            }

            @Override
            public void despawnAll(PlayerRef target) {
                fakes.remove(target.id());
            }
        };
    }

    private final Map<UUID, Position> positions = new LinkedHashMap<>();

    public void setPosition(PlayerRef player, Position position) {
        positions.put(player.id(), position);
    }

    @Override
    public MovementService movement() {
        return new MovementService() {
            @Override
            public java.util.Optional<Position> positionOf(PlayerRef player) {
                return java.util.Optional.ofNullable(positions.get(player.id()));
            }

            @Override
            public void teleport(PlayerRef player, Position position) {
                positions.put(player.id(), position);
            }

            @Override
            public void push(PlayerRef player, double x, double y, double z) {
            }
        };
    }

    private final java.util.Set<UUID> fakedPings = new java.util.HashSet<>();

    @Override
    public PingService ping() {
        return new PingService() {
            @Override
            public void setFake(PlayerRef target, int milliseconds) {
                fakedPings.add(target.id());
            }

            @Override
            public void clear(PlayerRef target) {
                fakedPings.remove(target.id());
            }

            @Override
            public boolean isFaked(UUID id) {
                return fakedPings.contains(id);
            }

            @Override
            public void clearAll() {
                fakedPings.clear();
            }
        };
    }

    @Override
    public EffectsService effects() {
        return new EffectsService() {
            public void potion(PlayerRef t, String e, int d, int a) {}
            public void lightning(PlayerRef t, boolean c) {}
            public void explosion(PlayerRef t, float p, boolean b, boolean d) {}
            public void sound(PlayerRef t, String s, float v, float p) {}
            public void spawnMobs(PlayerRef t, String e, int c, double r) {}
            public void broadcast(net.kyori.adventure.text.Component m) {}
            public String wipeColumn(PlayerRef t, int r) { return ""; }
            public void wipeChunk(PlayerRef t) {}
            public void restore(String token) {}
            public void launch(PlayerRef t, double p) {}
            public void teleportRandom(PlayerRef t, double r) {}
            public void teleportTo(PlayerRef t, PlayerRef d) {}
            public void giveItem(PlayerRef t, String m, int a) {}
            public void dropItems(PlayerRef t, int c) {}
            public void heal(PlayerRef t) {}
            public void firework(PlayerRef t) {}
            public void freeze(PlayerRef t, int ticks) {}
            public void spin(PlayerRef t, int ticks) {}
            public void swapInventory(PlayerRef a, PlayerRef b) {}
            public void scrambleInventory(PlayerRef t) {}
            public void weatherStorm(PlayerRef t, int ticks) {}
            public void hideName(PlayerRef t, int ticks) {}
            public void anonymize(PlayerRef t, int ticks) {}
            public void title(PlayerRef t, net.kyori.adventure.text.Component ti, net.kyori.adventure.text.Component su, int fi, int st, int fo) {}
        };
    }

    @Override
    public BossBarService bossBars() {
        return new BossBarService() {
            public Object create(net.kyori.adventure.text.Component title, String color) { return new Object(); }
            public void update(Object h, net.kyori.adventure.text.Component title, float p) {}
            public void viewers(Object h, java.util.Collection<PlayerRef> v) {}
            public void hide(Object h) {}
        };
    }

    private final Map<UUID, String> bindings = new LinkedHashMap<>();

    @Override
    public ItemBindingService itemBindings() {
        return new ItemBindingService() {
            public boolean bindHeldItem(PlayerRef holder, String actionId) { bindings.put(holder.id(), actionId); return true; }
            public boolean unbindHeldItem(PlayerRef holder) { return bindings.remove(holder.id()) != null; }
            public Optional<String> heldBinding(PlayerRef holder) { return Optional.ofNullable(bindings.get(holder.id())); }
        };
    }

    @Override
    public ChainOps chain() {
        return new ChainOps() {
            public String inventoryHash(PlayerRef p) { return ""; }
            public void copyInventory(PlayerRef from, PlayerRef to) {}
            public void setHealth(PlayerRef p, double h) {}
            public int food(PlayerRef p) { return 20; }
            public void setFood(PlayerRef p, int f) {}
            public void copyPotions(PlayerRef from, PlayerRef to) {}
            public void kill(PlayerRef p) {}
            public void drawChain(java.util.Collection<PlayerRef> viewers, java.util.List<Position> points) {}
        };
    }

    @Override
    public PlatformEvents events() {
        return new PlatformEvents() {
            @Override
            public void onJoin(Consumer<PlayerRef> listener) {
                joinListeners.add(listener);
            }

            @Override
            public void onQuit(Consumer<PlayerRef> listener) {
            }
        };
    }

    public static final class FakeStealth implements StealthService {

        private final Map<UUID, StealthOptions> hidden = new LinkedHashMap<>();

        @Override
        public void hide(PlayerRef player, StealthOptions options) {
            hidden.put(player.id(), options);
        }

        @Override
        public void reveal(PlayerRef player) {
            hidden.remove(player.id());
        }

        @Override
        public void intend(UUID id, StealthOptions options) {
            hidden.put(id, options);
        }

        @Override
        public boolean hidden(UUID id) {
            return hidden.containsKey(id);
        }

        @Override
        public java.util.Set<UUID> hiddenPlayers() {
            return java.util.Set.copyOf(hidden.keySet());
        }

        public Optional<StealthOptions> optionsOf(UUID id) {
            return Optional.ofNullable(hidden.get(id));
        }
    }
}
