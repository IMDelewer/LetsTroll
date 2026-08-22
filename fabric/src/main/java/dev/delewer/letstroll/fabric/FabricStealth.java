package dev.delewer.letstroll.fabric;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.StealthOptions;
import dev.delewer.letstroll.platform.StealthService;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricStealth implements StealthService {

    private final Supplier<MinecraftServer> server;
    private final Map<UUID, StealthOptions> hidden = new ConcurrentHashMap<>();

    public FabricStealth(Supplier<MinecraftServer> server) {
        this.server = server;
    }

    @Override
    public void hide(PlayerRef player, StealthOptions options) {
        hidden.put(player.id(), options);
        if (player.handle() instanceof ServerPlayerEntity handle) {
            apply(handle, options);
        }
    }

    public void apply(ServerPlayerEntity handle, StealthOptions options) {
        MinecraftServer instance = server.get();
        if (instance == null) {
            return;
        }
        handle.setInvisible(true);
        handle.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, -1, 0, false, false, false));
        if (options.hideFromTab() || options.hideEntity()) {
            PlayerRemoveS2CPacket remove = new PlayerRemoveS2CPacket(List.of(handle.getUuid()));
            for (ServerPlayerEntity other : instance.getPlayerManager().getPlayerList()) {
                if (!other.getUuid().equals(handle.getUuid())) {
                    other.networkHandler.sendPacket(remove);
                }
            }
        }
    }

    @Override
    public void reveal(PlayerRef player) {
        hidden.remove(player.id());
        MinecraftServer instance = server.get();
        if (instance == null || !(player.handle() instanceof ServerPlayerEntity handle)) {
            return;
        }
        handle.setInvisible(false);
        handle.removeStatusEffect(StatusEffects.INVISIBILITY);
        instance.getPlayerManager().sendToAll(PlayerListS2CPacket.entryFromPlayer(List.of(handle)));
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
    public Set<UUID> hiddenPlayers() {
        return Set.copyOf(hidden.keySet());
    }

    public StealthOptions optionsOf(UUID id) {
        return hidden.get(id);
    }
}
