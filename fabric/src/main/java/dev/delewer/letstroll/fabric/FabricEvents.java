package dev.delewer.letstroll.fabric;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

import dev.delewer.letstroll.platform.PlatformEvents;
import dev.delewer.letstroll.platform.PlayerRef;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricEvents implements PlatformEvents {

    private final Supplier<MinecraftServer> server;
    private final List<Consumer<PlayerRef>> joinListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<PlayerRef>> quitListeners = new CopyOnWriteArrayList<>();

    public FabricEvents(Supplier<MinecraftServer> server) {
        this.server = server;
    }

    @Override
    public void onJoin(Consumer<PlayerRef> listener) {
        joinListeners.add(listener);
    }

    @Override
    public void onQuit(Consumer<PlayerRef> listener) {
        quitListeners.add(listener);
    }

    public void fireJoin(ServerPlayerEntity player) {
        PlayerRef ref = new FabricPlayerRef(player, server);
        joinListeners.forEach(listener -> listener.accept(ref));
    }

    public void fireQuit(ServerPlayerEntity player) {
        PlayerRef ref = new FabricPlayerRef(player, server);
        quitListeners.forEach(listener -> listener.accept(ref));
    }

    public void clear() {
        joinListeners.clear();
        quitListeners.clear();
    }
}
