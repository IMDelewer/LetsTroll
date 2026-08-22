package dev.delewer.letstroll.fabric;

import java.util.function.Supplier;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.platform.MenuSound;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.SoundService;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public final class FabricSounds implements SoundService {

    private final Supplier<LetsTroll> core;

    public FabricSounds(Supplier<LetsTroll> core) {
        this.core = core;
    }

    public static void playRaw(ServerPlayerEntity player, String key, float volume, float pitch) {
        if (player == null || key == null || key.isBlank()) {
            return;
        }
        SoundEvent event = Registries.SOUND_EVENT.get(FabricText.id(key));
        if (event == null) {
            return;
        }
        RegistryEntry<SoundEvent> entry = RegistryEntry.of(event);
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(entry, SoundCategory.MASTER,
                player.getX(), player.getY(), player.getZ(), volume, pitch, player.getEntityWorld().getRandom().nextLong()));
    }

    @Override
    public void play(PlayerRef listener, MenuSound sound) {
        LetsTroll instance = core.get();
        if (instance == null || !instance.config().soundsEnabled() || sound == MenuSound.NONE) {
            return;
        }
        if (!(listener.handle() instanceof ServerPlayerEntity player)) {
            return;
        }
        String key = switch (sound) {
            case OPEN -> instance.config().soundOpen();
            case CLICK -> instance.config().soundClick();
            case TOGGLE_ON -> instance.config().soundToggleOn();
            case TOGGLE_OFF -> instance.config().soundToggleOff();
            case ERROR -> instance.config().soundError();
            case NONE -> null;
        };
        if (key == null || key.isBlank()) {
            return;
        }
        float pitch = switch (sound) {
            case TOGGLE_ON -> 1.4f;
            case TOGGLE_OFF -> 0.8f;
            default -> 1.0f;
        };
        playRaw(player, key, 0.6f, pitch);
    }
}
