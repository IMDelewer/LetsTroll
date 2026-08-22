package dev.delewer.letstroll.text;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationText {

    private static final Pattern PART = Pattern.compile("(\\d+)\\s*(ms|s|m|h|d|w)");
    private static final Map<String, Long> UNITS = new LinkedHashMap<>();

    static {
        UNITS.put("w", 7L * 24L * 60L * 60L * 1000L);
        UNITS.put("d", 24L * 60L * 60L * 1000L);
        UNITS.put("h", 60L * 60L * 1000L);
        UNITS.put("m", 60L * 1000L);
        UNITS.put("s", 1000L);
        UNITS.put("ms", 1L);
    }

    private DurationText() {
    }

    public static OptionalLong parseMillis(String input) {
        if (input == null || input.isBlank()) {
            return OptionalLong.empty();
        }
        String cleaned = input.trim().toLowerCase(Locale.ROOT);
        Matcher matcher = PART.matcher(cleaned);
        long total = 0L;
        int covered = 0;
        boolean found = false;
        while (matcher.find()) {
            Long unit = UNITS.get(matcher.group(2));
            if (unit == null) {
                return OptionalLong.empty();
            }
            try {
                total = Math.addExact(total, Math.multiplyExact(Long.parseLong(matcher.group(1)), unit));
            } catch (ArithmeticException overflow) {
                return OptionalLong.empty();
            }
            covered += matcher.group().length();
            found = true;
        }
        if (!found) {
            return OptionalLong.empty();
        }
        if (covered < cleaned.replace(" ", "").length()) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(total);
    }

    public static String format(long millis) {
        if (millis <= 0L) {
            return "0s";
        }
        StringBuilder out = new StringBuilder();
        long left = millis;
        for (Map.Entry<String, Long> unit : UNITS.entrySet()) {
            if (unit.getKey().equals("ms") && out.length() > 0) {
                break;
            }
            long count = left / unit.getValue();
            if (count <= 0L) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(count).append(unit.getKey());
            left -= count * unit.getValue();
        }
        return out.length() == 0 ? "0s" : out.toString();
    }

    public static long toTicks(long millis) {
        return Math.max(0L, millis / 50L);
    }

    public static long fromTicks(long ticks) {
        return Math.max(0L, ticks) * 50L;
    }
}
