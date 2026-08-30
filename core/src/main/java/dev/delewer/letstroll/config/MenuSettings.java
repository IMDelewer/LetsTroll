package dev.delewer.letstroll.config;

import dev.ua.theroer.magicutils.config.annotations.Comment;
import dev.ua.theroer.magicutils.config.annotations.ConfigSection;
import dev.ua.theroer.magicutils.config.annotations.ConfigValue;
import dev.ua.theroer.magicutils.config.annotations.MaxValue;
import dev.ua.theroer.magicutils.config.annotations.MinValue;

public final class MenuSettings {

    @Comment("Item that fills the empty slots of every menu")
    @ConfigValue("filler")
    private String filler = "minecraft:gray_stained_glass_pane";

    @Comment("Height of every menu, from 3 to 6 rows")
    @ConfigValue("rows")
    @MinValue(3)
    @MaxValue(6)
    private int rows = 6;

    @Comment("How many player heads fit on one page")
    @ConfigValue("players-per-page")
    @MinValue(1)
    @MaxValue(45)
    private int playersPerPage = 28;

    @Comment("How often an open menu redraws itself, in ticks, 0 turns it off")
    @ConfigValue("refresh-ticks")
    @MinValue(0)
    private int refreshTicks = 40;

    @ConfigSection("sounds")
    private SoundSettings sounds = new SoundSettings();

    public String filler() {
        return filler == null || filler.isBlank() ? "minecraft:gray_stained_glass_pane" : filler;
    }

    public int rows() {
        return rows;
    }

    public int playersPerPage() {
        return playersPerPage;
    }

    public int refreshTicks() {
        return refreshTicks;
    }

    public SoundSettings sounds() {
        if (sounds == null) {
            sounds = new SoundSettings();
        }
        return sounds;
    }
}
