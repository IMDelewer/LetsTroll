package dev.delewer.letstroll.modules.lag;

import java.util.List;

import dev.delewer.letstroll.menu.ClickContext;
import dev.delewer.letstroll.menu.ClickKind;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.module.LetsTrollModule;
import dev.delewer.letstroll.module.ModuleContext;
import dev.delewer.letstroll.module.TrollModule;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.player.PlayerAction;
import dev.delewer.letstroll.text.Text;
import net.kyori.adventure.text.Component;

@TrollModule(id = "lag", name = "Fake lag", order = 70)
public final class LagModule implements LetsTrollModule {

    public static final String PERMISSION = "letstroll.lag";

    private LagService service;

    @Override
    public void enable(ModuleContext context) {
        LagConfig config = context.config(LagConfig.class);
        service = new LagService(context.platform());

        context.playerAction(new PlayerAction() {

            @Override
            public String id() {
                return "lag";
            }

            @Override
            public int order() {
                return 30;
            }

            @Override
            public MenuIcon icon(PlayerRef target) {
                return MenuIcon.of("minecraft:cobweb");
            }

            @Override
            public Component name(PlayerRef target) {
                return Text.get(target, "lag.action.name");
            }

            @Override
            public List<Component> lore(PlayerRef target) {
                return List.of(
                        Text.get(target, "lag.action.description"),
                        Text.get(target, "lag.action.power", config.strength()),
                        Text.get(target, "lag.action.adjust"),
                        Text.get(target, service.isLagging(target.id()) ? "lag.action.on" : "lag.action.off"));
            }

            @Override
            public boolean glow(PlayerRef target) {
                return service.isLagging(target.id());
            }

            @Override
            public String permission() {
                return PERMISSION;
            }

            @Override
            public void run(ClickContext click, PlayerRef target) {
                if (click.kind().isRight()) {
                    int next = config.strength() + 1;
                    if (next > 10) {
                        next = 1;
                    }
                    config.setStrength(next);
                    click.core().saveConfig();
                    Text.send(click.viewer(), "lag.power", next);
                    if (service.isLagging(target.id())) {
                        service.start(target, config.strength(), config.durationTicks());
                        click.core().platform().ping().setFake(target, fakePing(config.strength()));
                    }
                    click.refresh();
                    return;
                }
                if (service.isLagging(target.id())) {
                    service.stop(target.id());
                    click.core().platform().ping().clear(target);
                    Text.send(click.viewer(), "lag.stopped", target.name());
                } else {
                    service.start(target, config.strength(), config.durationTicks());
                    click.core().platform().ping().setFake(target, fakePing(config.strength()));
                    click.core().stats().record(target.id(), "lag");
                    Text.send(click.viewer(), "lag.started", target.name(), config.strength());
                }
                click.refresh();
            }
        });
    }

    private int fakePing(int strength) {
        return 250 + strength * 180;
    }

    @Override
    public void disable() {
        if (service != null) {
            service.stopAll();
            service = null;
        }
    }
}
