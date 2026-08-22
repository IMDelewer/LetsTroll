package dev.delewer.letstroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.delewer.letstroll.modules.mannequin.MannequinConfig;
import dev.delewer.letstroll.platform.FakePlayerSpec;
import org.junit.jupiter.api.Test;

class MannequinConfigTest {

    @Test
    void removingEveryPresetLeavesTheListEmpty() {
        MannequinConfig config = new MannequinConfig();
        config.presets().keySet().stream().toList().forEach(config::removePreset);

        assertTrue(config.presets().isEmpty());
    }

    @Test
    void removingAnUnknownPresetReportsNoChange() {
        MannequinConfig config = new MannequinConfig();
        int before = config.presets().size();

        assertFalse(config.removePreset("missing"));
        assertEquals(before, config.presets().size());
    }

    @Test
    void addedPresetsSurviveAlongsideTheDefaults() {
        MannequinConfig config = new MannequinConfig();
        config.addPreset("delewer", "Delewer");

        assertEquals("Delewer", config.presets().get("delewer"));
        assertTrue(config.presets().containsKey("notch"));
    }

    @Test
    void mirrorPresetCopiesTheVictimSkin() {
        MannequinConfig config = new MannequinConfig();

        FakePlayerSpec mirror = config.spec("mirror");
        FakePlayerSpec named = config.spec("notch");

        assertTrue(mirror.copyTargetSkin());
        assertFalse(named.copyTargetSkin());
        assertEquals("Notch", named.skinOwner());
        assertTrue(named.lifetimeTicks() >= 5L);
    }
}
