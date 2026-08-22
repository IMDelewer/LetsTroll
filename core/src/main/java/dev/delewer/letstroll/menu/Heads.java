package dev.delewer.letstroll.menu;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Heads {

    public static final String BACK = "back";
    public static final String PREVIOUS = "previous";
    public static final String NEXT = "next";
    public static final String UP = "up";
    public static final String CLOSE = "close";
    public static final String SEARCH = "search";
    public static final String SETTINGS = "settings";
    public static final String INFO = "info";
    public static final String WARNING = "warning";
    public static final String TOGGLE_ON = "toggle-on";
    public static final String TOGGLE_OFF = "toggle-off";

    private static final Map<String, String> DEFAULTS = new LinkedHashMap<>();

    static {
        DEFAULTS.put(BACK, hash("f7aacad193e2226971ed95302dba433438be4644fbab5ebf818054061667fbe2"));
        DEFAULTS.put(PREVIOUS, hash("f7aacad193e2226971ed95302dba433438be4644fbab5ebf818054061667fbe2"));
        DEFAULTS.put(NEXT, hash("d34ef0638537222b20f480694dadc0f85fbe0759d581aa7fcdf2e43139377158"));
        DEFAULTS.put(UP, hash("a156b31cbf8f774547dc3f9713a770ecc5c727d967cb0093f26546b920457387"));
        DEFAULTS.put(INFO, hash("d34e063cafb467a5c8de43ec78619399f369f4a52434da8017a983cdd92516a0"));
        DEFAULTS.put(WARNING, hash("40b05e699d28b3a278a92d169dca9d57c0791d07994d82de3f9ed4a48afe0e1d"));
        DEFAULTS.put(CLOSE, item("minecraft:barrier"));
        DEFAULTS.put(SEARCH, item("minecraft:spyglass"));
        DEFAULTS.put(SETTINGS, item("minecraft:comparator"));
        DEFAULTS.put(TOGGLE_ON, item("minecraft:lime_dye"));
        DEFAULTS.put(TOGGLE_OFF, item("minecraft:gray_dye"));
    }

    private Heads() {
    }

    public static Map<String, String> defaults() {
        return Map.copyOf(DEFAULTS);
    }

    public static MenuIcon resolve(String spec, MenuIcon fallback) {
        if (spec == null || spec.isBlank()) {
            return fallback;
        }
        if (spec.startsWith("hash:")) {
            return MenuIcon.textured(textureFromHash(spec.substring(5)));
        }
        if (spec.startsWith("base64:")) {
            return MenuIcon.textured(spec.substring(7));
        }
        if (spec.startsWith("item:")) {
            return MenuIcon.of(spec.substring(5));
        }
        if (spec.startsWith("head:")) {
            return MenuIcon.headOf(spec.substring(5));
        }
        return MenuIcon.of(spec);
    }

    public static String textureFromHash(String hash) {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + hash + "\"}}}";
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private static String hash(String value) {
        return "hash:" + value;
    }

    private static String item(String value) {
        return "item:" + value;
    }
}
