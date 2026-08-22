package dev.delewer.letstroll.hub;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Function;

import dev.delewer.letstroll.menu.ClickContext;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.text.Text;
import net.kyori.adventure.text.Component;

public final class HubEntry {

    private final String id;
    private final int order;
    private final Function<PlayerRef, MenuIcon> icon;
    private final Function<PlayerRef, Component> title;
    private final Function<PlayerRef, List<Component>> lore;
    private final Function<PlayerRef, Boolean> glow;
    private final String permission;
    private final Consumer<ClickContext> action;
    private final Integer slot;

    private HubEntry(Builder builder) {
        this.id = builder.id;
        this.order = builder.order;
        this.icon = builder.icon;
        this.title = builder.title;
        this.lore = builder.lore;
        this.glow = builder.glow;
        this.permission = builder.permission;
        this.action = builder.action;
        this.slot = builder.slot;
    }

    public static Builder of(String id) {
        return new Builder(id);
    }

    public static HubEntry tab(String id, int order, MenuIcon icon, String screenId) {
        return of(id)
                .order(order)
                .icon(viewer -> icon)
                .title(viewer -> Text.get(viewer, "hub.tab." + id + ".title"))
                .lore(viewer -> List.of(Text.get(viewer, "hub.tab." + id + ".description")))
                .opens(screenId)
                .build();
    }

    public String id() {
        return id;
    }

    public int order() {
        return order;
    }

    public MenuIcon icon(PlayerRef viewer) {
        return icon.apply(viewer);
    }

    public Component title(PlayerRef viewer) {
        return title.apply(viewer);
    }

    public List<Component> lore(PlayerRef viewer) {
        return lore.apply(viewer);
    }

    public boolean glow(PlayerRef viewer) {
        return glow.apply(viewer);
    }

    public String permission() {
        return permission;
    }

    public Consumer<ClickContext> action() {
        return action;
    }

    public OptionalInt slot() {
        return slot == null ? OptionalInt.empty() : OptionalInt.of(slot);
    }

    public static final class Builder {

        private final String id;
        private int order = 100;
        private Function<PlayerRef, MenuIcon> icon = viewer -> MenuIcon.of("minecraft:paper");
        private Function<PlayerRef, Component> title = viewer -> Component.text("");
        private Function<PlayerRef, List<Component>> lore = viewer -> List.of();
        private Function<PlayerRef, Boolean> glow = viewer -> false;
        private String permission;
        private Consumer<ClickContext> action;
        private Integer slot;

        private Builder(String id) {
            this.id = id;
        }

        public Builder order(int value) {
            this.order = value;
            return this;
        }

        public Builder icon(Function<PlayerRef, MenuIcon> value) {
            this.icon = value;
            return this;
        }

        public Builder title(Function<PlayerRef, Component> value) {
            this.title = value;
            return this;
        }

        public Builder lore(Function<PlayerRef, List<Component>> value) {
            this.lore = value;
            return this;
        }

        public Builder glow(Function<PlayerRef, Boolean> value) {
            this.glow = value;
            return this;
        }

        public Builder permission(String value) {
            this.permission = value;
            return this;
        }

        public Builder opens(String screenId) {
            this.action = click -> click.open(ScreenRequest.of(screenId));
            return this;
        }

        public Builder onClick(Consumer<ClickContext> value) {
            this.action = value;
            return this;
        }

        public Builder slot(int value) {
            this.slot = value;
            return this;
        }

        public HubEntry build() {
            return new HubEntry(this);
        }
    }
}
