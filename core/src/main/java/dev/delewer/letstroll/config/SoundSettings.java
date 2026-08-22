package dev.delewer.letstroll.config;

import dev.ua.theroer.magicutils.config.annotations.Comment;
import dev.ua.theroer.magicutils.config.annotations.ConfigValue;

public final class SoundSettings {

    @Comment("Play sounds when a menu opens and when a button is pressed")
    @ConfigValue("enabled")
    private boolean enabled = true;

    @ConfigValue("open")
    private String open = "minecraft:block.barrel.open";

    @ConfigValue("click")
    private String click = "minecraft:ui.button.click";

    @ConfigValue("toggle-on")
    private String toggleOn = "minecraft:block.note_block.pling";

    @ConfigValue("toggle-off")
    private String toggleOff = "minecraft:block.note_block.bass";

    @ConfigValue("error")
    private String error = "minecraft:entity.villager.no";

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    public String open() {
        return open;
    }

    public String click() {
        return click;
    }

    public String toggleOn() {
        return toggleOn;
    }

    public String toggleOff() {
        return toggleOff;
    }

    public String error() {
        return error;
    }
}
