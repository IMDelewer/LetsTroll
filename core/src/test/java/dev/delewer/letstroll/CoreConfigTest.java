package dev.delewer.letstroll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import dev.delewer.letstroll.config.CoreConfig;
import dev.delewer.letstroll.modules.lag.LagConfig;
import org.junit.jupiter.api.Test;

class CoreConfigTest {

    private static void clear(CoreConfig config, String name) throws ReflectiveOperationException {
        Field field = CoreConfig.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(config, null);
    }

    @Test
    void keepsMenuSettingsWrittenWhileTheSectionWasMissing() throws ReflectiveOperationException {
        CoreConfig config = new CoreConfig();
        clear(config, "menu");

        config.setSoundsEnabled(false);

        assertFalse(config.soundsEnabled());
    }

    @Test
    void keepsHideSettingsWrittenWhileTheSectionWasMissing() throws ReflectiveOperationException {
        CoreConfig config = new CoreConfig();
        clear(config, "hide");

        config.setNativeHide(false);
        config.setHideCommands(false);
        config.setHideFromPlugins(false);

        assertFalse(config.nativeHide());
        assertFalse(config.hideCommands());
        assertFalse(config.hideFromPlugins());
    }

    @Test
    void handsOutTheSameModuleSectionEveryTime() throws ReflectiveOperationException {
        CoreConfig config = new CoreConfig();
        clear(config, "modules");

        LagConfig first = config.modules().of(LagConfig.class);
        first.setStrength(9);
        LagConfig second = config.modules().of(LagConfig.class);

        assertSame(first, second);
        assertTrue(second.strength() == 9);
    }
}
