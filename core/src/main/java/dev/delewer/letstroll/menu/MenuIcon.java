package dev.delewer.letstroll.menu;

import java.util.UUID;

public record MenuIcon(String material, UUID head, String headOwner, String texture) {

    public static MenuIcon of(String material) {
        return new MenuIcon(material, null, null, null);
    }

    public static MenuIcon head(UUID owner, String name) {
        return new MenuIcon("minecraft:player_head", owner, name, null);
    }

    public static MenuIcon headOf(String name) {
        return new MenuIcon("minecraft:player_head", null, name, null);
    }

    public static MenuIcon textured(String texture) {
        return new MenuIcon("minecraft:player_head", null, null, texture);
    }

    public boolean isHead() {
        return "minecraft:player_head".equals(material) && (head != null || headOwner != null || texture != null);
    }

    public boolean hasTexture() {
        return texture != null && !texture.isBlank();
    }
}
