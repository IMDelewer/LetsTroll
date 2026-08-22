package dev.delewer.letstroll.modules.mannequin;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.delewer.letstroll.platform.FakePlayerSpec;
import dev.ua.theroer.magicutils.config.annotations.Comment;
import dev.ua.theroer.magicutils.config.annotations.ConfigValue;

public final class MannequinConfig {

    public static final String MIRROR = "%target%";

    @Comment("Presets shown in the menu: preset id to the skin it wears, %target% copies the victim skin")
    @ConfigValue("presets")
    private Map<String, String> presets = defaults();

    @Comment("How long the fake player stays, a short value works like a jumpscare")
    @ConfigValue("lifetime-seconds")
    private double lifetimeSeconds = 1.5;

    @Comment("How far in front of the victim it appears, in blocks")
    @ConfigValue("distance")
    private double distance = 2.0;

    @Comment("Turn the fake player towards the victim")
    @ConfigValue("face-player")
    private boolean facePlayer = true;

    @Comment("Only the victim sees it, everyone else sees nothing")
    @ConfigValue("visible-only-to-target")
    private boolean visibleOnlyToTarget = true;

    private static Map<String, String> defaults() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("mirror", MIRROR);
        values.put("notch", "Notch");
        values.put("jeb", "jeb_");
        values.put("dinnerbone", "Dinnerbone");
        values.put("herobrine", "Herobrine");
        return values;
    }

    public Map<String, String> presets() {
        return presets == null ? defaults() : presets;
    }

    public void addPreset(String id, String skin) {
        if (presets == null) {
            presets = defaults();
        }
        presets.put(id, skin);
    }

    public boolean removePreset(String id) {
        if (presets == null) {
            return false;
        }
        return presets.remove(id) != null;
    }

    public FakePlayerSpec spec(String presetId) {
        String skin = presets().get(presetId);
        boolean mirror = MIRROR.equals(skin);
        return new FakePlayerSpec(mirror ? null : skin, mirror, Math.max(0.5, distance), facePlayer,
                visibleOnlyToTarget, Math.max(5L, Math.round(lifetimeSeconds * 20.0)));
    }
}
