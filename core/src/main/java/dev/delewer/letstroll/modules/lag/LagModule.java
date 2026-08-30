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
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.text.DurationText;
import dev.delewer.letstroll.text.Text;
import net.kyori.adventure.text.Component;

@TrollModule(id = "lag", name = "Fake lag", order = 70)
public final class LagModule implements LetsTrollModule {

    public static final String PERMISSION = "letstroll.lag";

    private LagService service;
    private LagConfig config;

    @Override
    public void enable(ModuleContext context) {
        config = context.config(LagConfig.class);
        service = new LagService(context.platform(), config);

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
                        Text.get(target, "lag.action.delay", DurationText.format(config.delayMillis())),
                        Text.get(target, "lag.action.adjust"),
                        Text.get(target, "lag.delay.adjust"),
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
                if (click.kind() == ClickKind.SHIFT_RIGHT) {
                    click.input(Text.get(click.viewer(), "lag.delay.prompt"), "",
                            value -> applyDelay(click, target, value));
                    return;
                }
                if (click.kind().isRight()) {
                    int next = config.strength() + 1;
                    if (next > 10) {
                        next = 1;
                    }
                    config.setStrength(next);
                    click.core().saveConfig();
                    Text.send(click.viewer(), "lag.power", next);
                    restart(click, target);
                    click.refresh();
                    return;
                }
                if (service.isLagging(target.id())) {
                    service.stop(target.id());
                    Text.send(click.viewer(), "lag.stopped", target.name());
                } else {
                    service.start(target, config.strength(), config.durationTicks());
                    click.core().stats().record(target.id(), "lag");
                    Text.send(click.viewer(), "lag.started", target.name(), config.strength());
                }
                click.refresh();
            }
        });
    }

    private void restart(ClickContext click, PlayerRef target) {
        if (!service.isLagging(target.id())) {
            return;
        }
        service.start(target, config.strength(), config.durationTicks());
    }

    private void applyDelay(ClickContext click, PlayerRef target, String value) {
        java.util.OptionalLong parsed = DurationText.parseMillis(value);
        if (parsed.isEmpty()) {
            Text.send(click.viewer(), "lag.delay.bad");
        } else {
            config.setDelayMillis(parsed.getAsLong());
            click.core().saveConfig();
            Text.send(click.viewer(), "lag.delay.set", DurationText.format(config.delayMillis()));
            restart(click, target);
        }
        click.open(ScreenRequest.of("player", "target", target.id().toString()));
    }

    @Override
    public void disable() {
        if (service != null) {
            service.stopAll();
            service = null;
        }
    }
}
