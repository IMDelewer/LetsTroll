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

    @Comment("Exact packet delay in milliseconds; 0 derives it from the power above")
    @ConfigValue("delay-millis")
    @MinValue(0)
    private long delayMillis = 0L;

    @Comment("How long the fake lag lasts, in seconds; 0 keeps it on until you stop it in the menu")
    @ConfigValue("duration-seconds")
    private double durationSeconds = 0.0;

    @Comment("Real lag by holding back the victim's packets instead of only yanking them around")
    @ConfigValue("hold-packets")
    private boolean holdPackets = true;

    @Comment("Remove every safety limit: no delay cap and keep-alive packets are held too, "
            + "so a hard enough setting will time the victim out")
    @ConfigValue("dangerous")
    private boolean dangerous = false;

    public int strength() {
        return strength;
    }

    public void setStrength(int value) {
        this.strength = Math.max(1, Math.min(10, value));
        this.delayMillis = 0L;
    }

    public long delayMillis() {
        return delayMillis > 0L ? delayMillis : strength() * 200L;
    }

    public void setDelayMillis(long value) {
        this.delayMillis = Math.max(0L, value);
    }

    public boolean holdPackets() {
        return holdPackets;
    }

    public void setHoldPackets(boolean value) {
        this.holdPackets = value;
    }

    public boolean dangerous() {
        return dangerous;
    }

    public long durationTicks() {
        if (durationSeconds <= 0) {
            return 0L;
        }
        return Math.max(20L, Math.round(durationSeconds * 20.0));
    }
}
