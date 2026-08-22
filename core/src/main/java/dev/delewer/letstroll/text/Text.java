package dev.delewer.letstroll.text;

import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.TrollPlatform;
import dev.ua.theroer.magicutils.lang.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Text {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private static volatile TrollPlatform platform;

    private Text() {
    }

    public static void bind(TrollPlatform value) {
        platform = value;
    }

    public static Component mini(String value) {
        return MINI.deserialize(value);
    }

    public static Component get(String key, Object... args) {
        return mini(format(raw(null, key), args));
    }

    public static Component get(PlayerRef viewer, String key, Object... args) {
        return mini(format(raw(viewer, key), args));
    }

    public static String plain(PlayerRef viewer, String key, Object... args) {
        return format(raw(viewer, key), args);
    }

    public static void send(PlayerRef target, String key, Object... args) {
        target.send(get(target, "prefix").append(get(target, key, args)));
    }

    private static String raw(PlayerRef viewer, String key) {
        String value;
        try {
            value = viewer == null ? Messages.getRaw(key) : Messages.getRaw(viewer.handle(), key);
            if (value == null || value.isBlank() || value.equals(key)) {
                value = Lang.fallback(key);
            }
        } catch (RuntimeException exception) {
            value = Lang.fallback(key);
        }
        return expand(viewer, value);
    }

    private static String expand(PlayerRef viewer, String value) {
        TrollPlatform current = platform;
        if (current == null || viewer == null || value.indexOf('%') < 0) {
            return value;
        }
        try {
            return current.placeholders(viewer, value);
        } catch (RuntimeException exception) {
            return value;
        }
    }

    private static String format(String template, Object... args) {
        String result = template;
        for (int index = 0; index < args.length; index++) {
            result = result.replace("{" + index + "}", String.valueOf(args[index]));
        }
        return result;
    }
}
