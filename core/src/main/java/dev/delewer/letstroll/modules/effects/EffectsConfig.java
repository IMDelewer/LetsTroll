package dev.delewer.letstroll.modules.effects;

import java.util.ArrayList;
import java.util.List;

import dev.ua.theroer.magicutils.config.annotations.Comment;
import dev.ua.theroer.magicutils.config.annotations.ConfigValue;

public final class EffectsConfig {

    @Comment("How long a picked effect lasts, in seconds")
    @ConfigValue("duration-seconds")
    private int durationSeconds = 6;

    @Comment("Effects offered in the pick menu")
    @ConfigValue("effects")
    private List<String> effects = new ArrayList<>(List.of(
            "minecraft:blindness", "minecraft:nausea", "minecraft:levitation",
            "minecraft:darkness", "minecraft:slowness", "minecraft:glowing"));

    public int durationTicks() {
        return Math.max(20, durationSeconds * 20);
    }

    public List<String> effects() {
        return effects == null || effects.isEmpty()
                ? List.of("minecraft:blindness", "minecraft:nausea", "minecraft:levitation")
                : effects;
    }
}
