package dev.delewer.letstroll.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import dev.delewer.letstroll.platform.MenuSound;
import net.kyori.adventure.text.Component;

public final class MenuButton {

    private final MenuIcon icon;
    private final Component name;
    private final List<Component> lore;
    private final boolean glow;
    private final MenuSound sound;
    private final Consumer<ClickContext> action;

    private MenuButton(Builder builder) {
        this.icon = builder.icon;
        this.name = builder.name;
        this.lore = List.copyOf(builder.lore);
        this.glow = builder.glow;
        this.sound = builder.sound;
        this.action = builder.action;
    }

    public static Builder of(MenuIcon icon) {
        return new Builder(icon);
    }

    public MenuIcon icon() {
        return icon;
    }

    public Component name() {
        return name;
    }

    public List<Component> lore() {
        return lore;
    }

    public boolean glow() {
        return glow;
    }

    public MenuSound sound() {
        return sound;
    }

    public void click(ClickContext context) {
        if (action == null) {
            return;
        }
        context.sound(sound);
        action.accept(context);
    }

    public boolean clickable() {
        return action != null;
    }

    public static final class Builder {

        private final MenuIcon icon;
        private Component name = Component.empty();
        private final List<Component> lore = new ArrayList<>();
        private boolean glow;
        private MenuSound sound = MenuSound.CLICK;
        private Consumer<ClickContext> action;

        private Builder(MenuIcon icon) {
            this.icon = icon;
        }

        public Builder name(Component value) {
            this.name = value;
            return this;
        }

        public Builder lore(Component... lines) {
            this.lore.addAll(List.of(lines));
            return this;
        }

        public Builder lore(List<Component> lines) {
            this.lore.addAll(lines);
            return this;
        }

        public Builder glow(boolean value) {
            this.glow = value;
            return this;
        }

        public Builder sound(MenuSound value) {
            this.sound = value;
            return this;
        }

        public Builder onClick(Consumer<ClickContext> value) {
            this.action = value;
            return this;
        }

        public MenuButton build() {
            return new MenuButton(this);
        }
    }
}
