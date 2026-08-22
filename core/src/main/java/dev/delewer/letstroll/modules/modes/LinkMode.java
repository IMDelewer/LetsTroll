package dev.delewer.letstroll.modules.modes;

import java.util.Locale;

public enum LinkMode {

    RIGID,
    ELASTIC,
    RUBBER;

    public LinkMode next() {
        LinkMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static LinkMode of(String value) {
        if (value == null) {
            return ELASTIC;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return ELASTIC;
        }
    }
}
