package dev.delewer.letstroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalLong;

import dev.delewer.letstroll.text.DurationText;
import org.junit.jupiter.api.Test;

class DurationTextTest {

    @Test
    void parsesSingleUnits() {
        assertEquals(1000L, DurationText.parseMillis("1s").orElseThrow());
        assertEquals(300_000L, DurationText.parseMillis("5m").orElseThrow());
        assertEquals(21_600_000L, DurationText.parseMillis("6h").orElseThrow());
        assertEquals(86_400_000L, DurationText.parseMillis("1d").orElseThrow());
        assertEquals(1_209_600_000L, DurationText.parseMillis("2w").orElseThrow());
        assertEquals(250L, DurationText.parseMillis("250ms").orElseThrow());
    }

    @Test
    void addsUpSeveralPartsSeparatedBySpaces() {
        assertEquals(305_000L, DurationText.parseMillis("5m 5s").orElseThrow());
        assertEquals(3_661_000L, DurationText.parseMillis("1h 1m 1s").orElseThrow());
    }

    @Test
    void toleratesSpacingAndCase() {
        assertEquals(305_000L, DurationText.parseMillis("  5M   5S ").orElseThrow());
        assertEquals(305_000L, DurationText.parseMillis("5m5s").orElseThrow());
    }

    @Test
    void rejectsGarbage() {
        assertTrue(DurationText.parseMillis("").isEmpty());
        assertTrue(DurationText.parseMillis(null).isEmpty());
        assertTrue(DurationText.parseMillis("soon").isEmpty());
        assertTrue(DurationText.parseMillis("5x").isEmpty());
        assertTrue(DurationText.parseMillis("5m banana").isEmpty());
        assertTrue(DurationText.parseMillis("5").isEmpty());
    }

    @Test
    void formatsBackToTheSameShape() {
        assertEquals("5m 5s", DurationText.format(305_000L));
        assertEquals("1d 2h", DurationText.format(93_600_000L));
        assertEquals("250ms", DurationText.format(250L));
        assertEquals("0s", DurationText.format(0L));
    }

    @Test
    void roundTripsThroughTicks() {
        OptionalLong parsed = DurationText.parseMillis("5m 5s");
        long ticks = DurationText.toTicks(parsed.orElseThrow());
        assertEquals(6100L, ticks);
        assertEquals("5m 5s", DurationText.format(DurationText.fromTicks(ticks)));
    }
}
