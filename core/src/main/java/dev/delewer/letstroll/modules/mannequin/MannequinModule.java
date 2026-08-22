package dev.delewer.letstroll.modules.mannequin;

import java.util.List;

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

@TrollModule(id = "mannequin", name = "Fake player", order = 60)
public final class MannequinModule implements LetsTrollModule {

    public static final String PERMISSION = "letstroll.mannequin";

    @Override
    public void enable(ModuleContext context) {
        MannequinConfig config = context.config(MannequinConfig.class);
        context.screen(new MannequinScreen(config));
        context.playerAction(new PlayerAction() {

            @Override
            public String id() {
                return "mannequin";
            }

            @Override
            public int order() {
                return 20;
            }

            @Override
            public MenuIcon icon(PlayerRef target) {
                return MenuIcon.of("minecraft:armor_stand");
            }

            @Override
            public Component name(PlayerRef target) {
                return Text.get(target, "mannequin.action.name");
            }

            @Override
            public List<Component> lore(PlayerRef target) {
                return List.of(Text.get(target, "mannequin.action.description"),
                        Text.get(target, "mannequin.action.presets", config.presets().size()));
            }

            @Override
            public String permission() {
                return PERMISSION;
            }

            @Override
            public void run(ClickContext click, PlayerRef target) {
                click.open(ScreenRequest.of(MannequinScreen.ID, "target", target.id().toString()));
            }
        });
    }
}
