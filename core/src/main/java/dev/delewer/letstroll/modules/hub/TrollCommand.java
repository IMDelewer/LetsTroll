package dev.delewer.letstroll.modules.hub;

import java.util.Optional;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.text.Text;
import dev.ua.theroer.magicutils.annotations.CommandInfo;
import dev.ua.theroer.magicutils.annotations.Sender;
import dev.ua.theroer.magicutils.annotations.SubCommand;
import dev.ua.theroer.magicutils.commands.CommandResult;
import dev.ua.theroer.magicutils.commands.MagicCommand;
import dev.ua.theroer.magicutils.commands.MagicSender;

@CommandInfo(name = "troll", description = "Open the LetsTroll menu", permission = LetsTroll.PERMISSION_USE)
public final class TrollCommand extends MagicCommand {

    private final LetsTroll core;

    public TrollCommand(LetsTroll core) {
        this.core = core;
    }

    public CommandResult execute(@Sender MagicSender sender) {
        Optional<PlayerRef> player = resolve(sender);
        if (player.isEmpty()) {
            return CommandResult.failure("Only a player can open the menu.");
        }
        PlayerRef viewer = player.get();
        core.router().open(viewer, ScreenRequest.of(HubScreen.ID));
        return CommandResult.success();
    }

    @SubCommand(name = "reload", description = "Reload the configuration", permission = LetsTroll.PERMISSION_ADMIN)
    public CommandResult reload(@Sender MagicSender sender) {
        core.platform().configs().reloadAll();
        return CommandResult.success("LetsTroll configuration reloaded.");
    }

    @SubCommand(name = "modules", description = "List loaded modules", permission = LetsTroll.PERMISSION_ADMIN)
    public CommandResult modules(@Sender MagicSender sender) {
        String names = String.join(", ", core.modules().infos().stream().map(info -> info.id()).toList());
        return CommandResult.success("Modules (" + core.modules().count() + "): " + names);
    }

    private Optional<PlayerRef> resolve(MagicSender sender) {
        if (sender.id() == null) {
            return Optional.empty();
        }
        return core.platform().players().byId(sender.id());
    }
}
