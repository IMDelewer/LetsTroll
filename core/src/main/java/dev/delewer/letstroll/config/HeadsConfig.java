package dev.delewer.letstroll.config;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.ua.theroer.magicutils.config.annotations.Comment;
import dev.ua.theroer.magicutils.config.annotations.ConfigFile;
import dev.ua.theroer.magicutils.config.annotations.ConfigValue;

@ConfigFile("heads.{ext}")
public final class HeadsConfig {

    @Comment("Icons used across the menus. Values: hash:<texture hash>, base64:<value>, item:<material>, head:<name>")
    @ConfigValue("icons")
    private Map<String, String> icons = new LinkedHashMap<>();

    public Map<String, String> icons() {
        return icons == null ? Map.of() : icons;
    }
}
