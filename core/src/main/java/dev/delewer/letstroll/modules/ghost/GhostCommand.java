package dev.delewer.letstroll.modules.ghost;

import java.util.Locale;
import java.util.Optional;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.text.Text;
import dev.ua.theroer.magicutils.annotations.CommandInfo;
import dev.ua.theroer.magicutils.annotations.OptionalArgument;
import dev.ua.theroer.magicutils.annotations.Sender;
import dev.ua.theroer.magicutils.annotations.Suggest;
import dev.ua.theroer.magicutils.commands.CommandResult;
import dev.ua.theroer.magicutils.commands.MagicCommand;
import dev.ua.theroer.magicutils.commands.MagicSender;

@CommandInfo(name = "ghost", description = "Turn ghost mode on or off", permission = GhostModule.PERMISSION)
public final class GhostCommand extends MagicCommand {

    private final LetsTroll core;
    private final GhostService service;

    public GhostCommand(LetsTroll core, GhostService service) {
        this.core = core;
        this.service = service;
    }

    public CommandResult execute(@Sender MagicSender sender,
                                 @OptionalArgument @Suggest({"on", "off", "@players"}) String first,
                                 @OptionalArgument @Suggest({"on", "off"}) String second) {
        Optional<PlayerRef> actor = selfOf(sender);
        boolean firstIsState = isState(first);

        Optional<PlayerRef> target = first == null || firstIsState
                ? actor
                : core.platform().players().byName(first);

        if (target.isEmpty()) {
            if (first == null || firstIsState) {
                return CommandResult.failure("Only a player can use this command without a target.");
            }
            return CommandResult.failure("Player " + first + " was not found.");
        }

        PlayerRef subject = target.get();
        boolean self = actor.map(player -> player.id().equals(subject.id())).orElse(false);
        if (!self && !sender.hasPermission(GhostModule.PERMISSION_OTHERS)) {
            return CommandResult.failure("You are not allowed to do that.");
        }

        boolean enabled = applyState(subject, firstIsState ? first : second);

        if (actor.isEmpty()) {
            return CommandResult.success("Ghost mode " + (enabled ? "enabled" : "disabled") + " for " + subject.name() + ".");
        }
        PlayerRef viewer = actor.get();
        if (self) {
            Text.send(viewer, enabled ? "ghost.enabled.self" : "ghost.disabled.self");
        } else {
            Text.send(viewer, enabled ? "ghost.enabled.other" : "ghost.disabled.other", subject.name());
        }
        return CommandResult.success();
    }

    private boolean applyState(PlayerRef subject, String state) {
        if (state == null) {
            return service.toggle(subject);
        }
        if (state.equalsIgnoreCase("on")) {
            service.enable(subject);
            return true;
        }
        service.disable(subject);
        return false;
    }

    private boolean isState(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.equals("on") || normalized.equals("off");
    }

    private Optional<PlayerRef> selfOf(MagicSender sender) {
        if (sender.id() == null) {
            return Optional.empty();
        }
        return core.platform().players().byId(sender.id());
    }
}
