package dev.delewer.letstroll.platform;

import net.kyori.adventure.text.Component;

public interface EffectsService {

    void potion(PlayerRef target, String effectKey, int durationTicks, int amplifier);

    void lightning(PlayerRef target, boolean cosmetic);

    void explosion(PlayerRef target, float power, boolean breakBlocks, boolean damage);

    void sound(PlayerRef target, String sound, float volume, float pitch);

    void spawnMobs(PlayerRef target, String entityType, int count, double radius);

    void broadcast(Component message);

    String wipeColumn(PlayerRef target, int radius);

    void wipeChunk(PlayerRef target);

    void restore(String undoToken);

    void launch(PlayerRef target, double power);

    void teleportRandom(PlayerRef target, double radius);

    void teleportTo(PlayerRef target, PlayerRef destination);

    void giveItem(PlayerRef target, String materialKey, int amount);

    void dropItems(PlayerRef target, int count);

    void heal(PlayerRef target);

    void firework(PlayerRef target);

    void freeze(PlayerRef target, int ticks);

    void spin(PlayerRef target, int ticks);

    void swapInventory(PlayerRef first, PlayerRef second);

    void scrambleInventory(PlayerRef target);

    void weatherStorm(PlayerRef target, int ticks);

    void hideName(PlayerRef target, int ticks);

    void anonymize(PlayerRef target, int ticks);

    void title(PlayerRef target, Component title, Component subtitle, int fadeIn, int stay, int fadeOut);
}
