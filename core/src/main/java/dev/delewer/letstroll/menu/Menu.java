package dev.delewer.letstroll.menu;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import net.kyori.adventure.text.Component;

public final class Menu {

    private final Component title;
    private final int rows;
    private final Map<Integer, MenuButton> buttons;
    private final MenuIcon filler;

    private Menu(Builder builder) {
        this.title = builder.title;
        this.rows = builder.rows;
        this.buttons = Collections.unmodifiableMap(new LinkedHashMap<>(builder.buttons));
        this.filler = builder.filler;
    }

    public static Builder builder(Component title) {
        return new Builder(title);
    }

    public Component title() {
        return title;
    }

    public int rows() {
        return rows;
    }

    public int size() {
        return rows * 9;
    }

    public Map<Integer, MenuButton> buttons() {
        return buttons;
    }

    public Optional<MenuIcon> filler() {
        return Optional.ofNullable(filler);
    }

    public static final class Builder {

        private final Component title;
        private int rows = 6;
        private final Map<Integer, MenuButton> buttons = new LinkedHashMap<>();
        private MenuIcon filler;

        private Builder(Component title) {
            this.title = title;
        }

        public Builder rows(int value) {
            this.rows = Math.max(1, Math.min(6, value));
            return this;
        }

        public Builder filler(MenuIcon icon) {
            this.filler = icon;
            return this;
        }

        public Builder button(int slot, MenuButton button) {
            if (slot >= 0 && slot < rows * 9) {
                buttons.put(slot, button);
            }
            return this;
        }

        public Menu build() {
            return new Menu(this);
        }
    }
}
