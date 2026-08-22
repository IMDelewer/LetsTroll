package dev.delewer.letstroll.config;

import java.lang.reflect.Field;

import dev.delewer.letstroll.modules.effects.EffectsConfig;
import dev.delewer.letstroll.modules.events.EventsConfig;
import dev.delewer.letstroll.modules.ghost.GhostConfig;
import dev.delewer.letstroll.modules.lag.LagConfig;
import dev.delewer.letstroll.modules.mannequin.MannequinConfig;
import dev.delewer.letstroll.modules.modes.ChainConfig;
import dev.ua.theroer.magicutils.config.annotations.ConfigSection;

public final class ModuleSettings {

    @ConfigSection("ghost")
    private GhostConfig ghost = new GhostConfig();

    @ConfigSection("chain")
    private ChainConfig chain = new ChainConfig();

    @ConfigSection("events")
    private EventsConfig events = new EventsConfig();

    @ConfigSection("effects")
    private EffectsConfig effects = new EffectsConfig();

    @ConfigSection("lag")
    private LagConfig lag = new LagConfig();

    @ConfigSection("mannequin")
    private MannequinConfig mannequin = new MannequinConfig();

    @SuppressWarnings("unchecked")
    public <T> T of(Class<T> type) {
        for (Field field : ModuleSettings.class.getDeclaredFields()) {
            if (!field.getType().equals(type)) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(this);
                if (value == null) {
                    value = type.getDeclaredConstructor().newInstance();
                    field.set(this, value);
                }
                return (T) value;
            } catch (ReflectiveOperationException exception) {
                return null;
            }
        }
        return null;
    }
}
