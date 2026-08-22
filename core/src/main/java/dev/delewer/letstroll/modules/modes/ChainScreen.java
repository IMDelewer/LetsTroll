package dev.delewer.letstroll.modules.modes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.menu.Buttons;
import dev.delewer.letstroll.menu.Menu;
import dev.delewer.letstroll.menu.MenuButton;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.menu.MenuLayout;
import dev.delewer.letstroll.menu.Pagination;
import dev.delewer.letstroll.menu.Screen;
import dev.delewer.letstroll.menu.ScreenContext;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.platform.MenuSound;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.text.Text;

public final class ChainScreen implements Screen {

    public static final String ID = "chain";

    private final ChainConfig config;
    private final ChainService service;

    public ChainScreen(ChainConfig config, ChainService service) {
        this.config = config;
        this.service = service;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Menu build(ScreenContext context) {
        LetsTroll core = context.core();
        PlayerRef viewer = context.viewer();
        int rows = core.config().menuRows();
        Set<UUID> selection = parse(context.request().arg("sel").orElse(""));

        Menu.Builder builder = Menu.builder(Text.get(viewer, "modes.chain.name"))
                .rows(rows)
                .filler(core.filler());

        builder.button(MenuLayout.serviceSlot(rows, 1), MenuButton.of(MenuIcon.of("minecraft:lead"))
                .name(Text.get(viewer, "modes.chain.length", trim(config.chainLength())))
                .lore(Text.get(viewer, "modes.chain.length.adjust"))
                .onClick(click -> {
                    config.setChainLength(nextLength(config.chainLength()));
                    click.core().saveConfig();
                    click.refresh();
                })
                .build());

        builder.button(MenuLayout.serviceSlot(rows, 2), MenuButton.of(MenuIcon.of("minecraft:chain"))
                .name(Text.get(viewer, "modes.chain.mode", Text.plain(viewer, "modes.chain.mode." + config.linkMode().key())))
                .lore(Text.get(viewer, "modes.chain.mode." + config.linkMode().key() + ".hint"),
                        Text.get(viewer, "modes.chain.mode.adjust"))
                .onClick(click -> {
                    config.setLinkMode(config.linkMode().next());
                    click.core().saveConfig();
                    click.refresh();
                })
                .build());

        builder.button(MenuLayout.serviceSlot(rows, 3), Buttons.toggle(core, viewer, config.visualChain(),
                Text.get(viewer, "modes.chain.visual"), Text.get(viewer, "modes.chain.visual.hint"),
                click -> {
                    config.setVisualChain(!config.visualChain());
                    click.core().saveConfig();
                    click.refresh();
                }));

        builder.button(MenuLayout.serviceSlot(rows, 4), MenuButton.of(MenuIcon.of(config.useLinks() ? config.linkBlock() : "minecraft:blaze_powder"))
                .name(Text.get(viewer, "modes.chain.style",
                        Text.plain(viewer, config.useLinks() ? "modes.chain.style.links" : "modes.chain.style.particles")))
                .lore(Text.get(viewer, "modes.chain.style.hint"))
                .onClick(click -> {
                    config.setUseLinks(!config.useLinks());
                    click.core().platform().chain().clearAllChainLinks();
                    click.core().saveConfig();
                    click.refresh();
                })
                .build());

        builder.button(MenuLayout.serviceSlot(rows, 5), Buttons.toggle(core, viewer, config.inertia(),
                Text.get(viewer, "modes.chain.inertia"), Text.get(viewer, "modes.chain.inertia.hint"),
                click -> {
                    config.setInertia(!config.inertia());
                    click.core().saveConfig();
                    click.refresh();
                }));

        List<UUID> chosen = new ArrayList<>(selection);
        if (chosen.size() == 2) {
            builder.button(MenuLayout.serviceSlot(rows, 7), MenuButton.of(MenuIcon.of("minecraft:chain"))
                    .name(Text.get(viewer, "modes.chain.link"))
                    .lore(Text.get(viewer, "modes.chain.link.hint"))
                    .glow(true)
                    .sound(MenuSound.TOGGLE_ON)
                    .onClick(click -> {
                        if (service.link(chosen.get(0), chosen.get(1))) {
                            Text.send(click.viewer(), "modes.chain.linked");
                        }
                        click.open(ScreenRequest.of(ID));
                    })
                    .build());
        }

        List<Integer> slots = MenuLayout.content(rows);
        List<PlayerRef> players = core.platform().players().online().stream()
                .sorted(Comparator.comparing(PlayerRef::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        Pagination<PlayerRef> pagination = new Pagination<>(players, context.page(),
                Math.min(slots.size(), core.config().playersPerPage()));
        List<PlayerRef> visible = pagination.slice();

        for (int index = 0; index < visible.size() && index < slots.size(); index++) {
            PlayerRef target = visible.get(index);
            boolean linked = service.isLinked(target.id());
            boolean selected = selection.contains(target.id());
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            if (linked) {
                String partner = service.partner(target.id())
                        .flatMap(id -> core.platform().players().byId(id))
                        .map(PlayerRef::name)
                        .orElse("?");
                lore.add(Text.get(viewer, "modes.chain.entry.linked", partner));
                lore.add(Text.get(viewer, "modes.chain.entry.unlink"));
            } else {
                lore.add(Text.get(viewer, selected ? "modes.chain.entry.selected" : "modes.chain.entry.select"));
            }
            builder.button(slots.get(index), MenuButton.of(MenuIcon.head(target.id(), target.name()))
                    .name(Text.mini((selected ? "<aqua>" : "<white>") + target.name()))
                    .lore(lore)
                    .glow(linked || selected)
                    .onClick(click -> {
                        if (linked) {
                            if (click.kind().isRight()) {
                                service.unlink(target.id());
                                Text.send(click.viewer(), "modes.chain.unlinked");
                                click.refresh();
                            }
                            return;
                        }
                        Set<UUID> next = new LinkedHashSet<>(selection);
                        if (!next.remove(target.id()) && next.size() < 2) {
                            next.add(target.id());
                        }
                        click.open(ScreenRequest.of(ID).with("sel", serialize(next)));
                    })
                    .build());
        }

        if (pagination.hasPrevious()) {
            builder.button(MenuLayout.serviceSlot(rows, 0),
                    Buttons.previous(core, viewer, context.request(), pagination.currentPage(), pagination.totalPages()));
        }
        if (pagination.hasNext()) {
            builder.button(MenuLayout.serviceSlot(rows, 8),
                    Buttons.next(core, viewer, context.request(), pagination.currentPage(), pagination.totalPages()));
        }

        return builder.button(MenuLayout.serviceSlot(rows, 4), Buttons.back(core, viewer)).build();
    }

    private Set<UUID> parse(String value) {
        Set<UUID> result = new LinkedHashSet<>();
        if (value == null || value.isBlank()) {
            return result;
        }
        for (String part : value.split(";")) {
            try {
                result.add(UUID.fromString(part));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return result;
    }

    private String serialize(Set<UUID> ids) {
        StringBuilder builder = new StringBuilder();
        for (UUID id : ids) {
            if (builder.length() > 0) {
                builder.append(';');
            }
            builder.append(id);
        }
        return builder.toString();
    }

    private double nextLength(double current) {
        if (current < 20) {
            return 20;
        }
        if (current < 25) {
            return 25;
        }
        if (current < 30) {
            return 30;
        }
        return 15;
    }

    private String trim(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
