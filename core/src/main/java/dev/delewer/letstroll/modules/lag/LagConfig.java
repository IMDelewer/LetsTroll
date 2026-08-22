package dev.delewer.letstroll.modules.lag;

import dev.ua.theroer.magicutils.config.annotations.Comment;
import dev.ua.theroer.magicutils.config.annotations.ConfigValue;
import dev.ua.theroer.magicutils.config.annotations.MaxValue;
import dev.ua.theroer.magicutils.config.annotations.MinValue;

public final class LagConfig {

    @Comment("Lag power from 1 (light stutter) to 10 (barely able to move)")
    @MinValue(1)
    @MaxValue(10)
    @ConfigValue("strength")
    private int strength = 5;

    @Comment("How long the fake lag lasts, in seconds; 0 keeps it on until you stop it in the menu")
    @ConfigValue("duration-seconds")
    private double durationSeconds = 0.0;

    public int strength() {
        return Math.max(1, Math.min(10, strength));
    }

    public void setStrength(int value) {
        this.strength = Math.max(1, Math.min(10, value));
    }

    public long durationTicks() {
        if (durationSeconds <= 0) {
            return 0L;
        }
        return Math.max(20L, Math.round(durationSeconds * 20.0));
    }
}
