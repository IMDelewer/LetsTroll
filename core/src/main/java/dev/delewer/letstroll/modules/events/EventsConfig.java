package dev.delewer.letstroll.modules.events;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.ua.theroer.magicutils.config.annotations.Comment;
import dev.ua.theroer.magicutils.config.annotations.ConfigValue;

public final class EventsConfig {

    @Comment("Master switch for automatic events")
    @ConfigValue("enabled")
    private boolean enabled = false;

    @Comment("Minutes between events")
    @ConfigValue("interval-minutes")
    private double intervalMinutes = 5.0;

    @Comment("Countdown boss bar: OFF, ADMINS or ALL")
    @ConfigValue("boss-bar")
    private String bossBar = "OFF";

    @Comment("Which events may fire, event id to on/off")
    @ConfigValue("events")
    private Map<String, Boolean> events = new LinkedHashMap<>();

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    public double intervalMinutes() {
        return Math.max(0.25, intervalMinutes);
    }

    public void setIntervalMinutes(double value) {
        this.intervalMinutes = Math.max(0.25, value);
    }

    public long intervalTicks() {
        return Math.round(intervalMinutes() * 60.0 * 20.0);
    }

    public String bossBar() {
        return bossBar == null ? "OFF" : bossBar.toUpperCase(java.util.Locale.ROOT);
    }

    public void setBossBar(String value) {
        this.bossBar = value;
    }

    public Map<String, Boolean> events() {
        if (events == null) {
            events = new LinkedHashMap<>();
        }
        return events;
    }

    public boolean isEventEnabled(String id, boolean fallback) {
        return events().getOrDefault(id, fallback);
    }

    public void setEventEnabled(String id, boolean value) {
        events().put(id, value);
    }
}
