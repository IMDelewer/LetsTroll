package dev.delewer.letstroll.paper;

import java.util.function.Supplier;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.platform.MenuSound;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.SoundService;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

public final class PaperSounds implements SoundService {

    private final Supplier<LetsTroll> core;

    public PaperSounds(Supplier<LetsTroll> core) {
        this.core = core;
    }

    @Override
    public void play(PlayerRef listener, MenuSound sound) {
        LetsTroll instance = core.get();
        if (instance == null || !instance.config().soundsEnabled() || sound == MenuSound.NONE) {
            return;
        }
        Player player = (Player) listener.handle();
        if (player == null) {
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
        player.playSound(Sound.sound(Key.key(key), Sound.Source.MASTER, 0.6f, pitch));
    }
}
