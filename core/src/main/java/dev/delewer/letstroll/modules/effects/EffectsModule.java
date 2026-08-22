package dev.delewer.letstroll.modules.effects;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import dev.delewer.letstroll.menu.ClickContext;
import dev.delewer.letstroll.menu.MenuIcon;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.module.LetsTrollModule;
import dev.delewer.letstroll.module.ModuleContext;
import dev.delewer.letstroll.module.TrollModule;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.player.PlayerAction;
import dev.delewer.letstroll.text.Text;
import net.kyori.adventure.text.Component;

@TrollModule(id = "effects", name = "Effects", order = 55)
public final class EffectsModule implements LetsTrollModule {

    public static final String PERMISSION = "letstroll.effects";

    private static final List<String> KILLERS = List.of("Herobrine", "Notch", "a Creeper", "the void", "lag");

    @Override
    public void enable(ModuleContext context) {
        EffectsConfig config = context.config(EffectsConfig.class);
        context.screen(new EffectPickScreen(config));

        context.playerAction(action("effects", 50, "minecraft:splash_potion",
                (click, target) -> click.open(ScreenRequest.of(EffectPickScreen.ID, "target", target.id().toString()))));

        context.playerAction(action("scare", 51, "minecraft:soul_lantern", (click, target) -> {
            click.core().platform().effects().sound(target, "minecraft:entity.enderman.scream", 1.0f, 0.5f);
            click.core().platform().effects().potion(target, "minecraft:darkness", 60, 0);
            click.core().stats().record(target.id(), "scare");
            Text.send(click.viewer(), "effects.scared", target.name());
        }));

        context.playerAction(action("fake_lightning", 52, "minecraft:lightning_rod", (click, target) -> {
            click.core().platform().effects().lightning(target, true);
            click.core().stats().record(target.id(), "fake_lightning");
            Text.send(click.viewer(), "effects.lightning", target.name());
        }));

        context.playerAction(action("fake_death", 53, "minecraft:skeleton_skull", (click, target) -> {
            String killer = KILLERS.get(ThreadLocalRandom.current().nextInt(KILLERS.size()));
            click.core().platform().effects().broadcast(Text.mini("<white>" + target.name() + " was slain by " + killer));
            click.core().stats().record(target.id(), "fake_death");
            Text.send(click.viewer(), "effects.death", target.name());
        }));
    }

    private PlayerAction action(String id, int order, String icon, java.util.function.BiConsumer<ClickContext, PlayerRef> run) {
        return new PlayerAction() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public int order() {
                return order;
            }

            @Override
            public MenuIcon icon(PlayerRef target) {
                return MenuIcon.of(icon);
            }

            @Override
            public Component name(PlayerRef target) {
                return Text.get(target, "effects.action." + id);
            }

            @Override
            public List<Component> lore(PlayerRef target) {
                return List.of(Text.get(target, "effects.action." + id + ".desc"));
            }

            @Override
            public String permission() {
                return PERMISSION;
            }

            @Override
            public void run(ClickContext click, PlayerRef target) {
                run.accept(click, target);
            }
        };
    }
}
