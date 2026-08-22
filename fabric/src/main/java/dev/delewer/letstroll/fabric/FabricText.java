package dev.delewer.letstroll.fabric;

import dev.ua.theroer.magicutils.platform.fabric.FabricComponentSerializer;
import net.kyori.adventure.text.Component;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class FabricText {

    private FabricText() {
    }

    public static Text toNative(Component component) {
        return FabricComponentSerializer.toNative(component);
    }

    public static Identifier id(String value) {
        Identifier parsed = Identifier.tryParse(value);
        return parsed == null ? Identifier.of("minecraft", value) : parsed;
    }
}
