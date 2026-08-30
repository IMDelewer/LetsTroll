package dev.delewer.letstroll.modules.ghost;

import dev.delewer.letstroll.platform.StealthOptions;
import dev.ua.theroer.magicutils.config.annotations.Comment;
import dev.ua.theroer.magicutils.config.annotations.ConfigValue;

public final class GhostConfig {

    @ConfigValue("creative")
    private boolean creative = false;

    @ConfigValue("hide-entity")
    private boolean hideEntity = true;

    @ConfigValue("hide-from-tab")
    private boolean hideFromTab = true;

    @ConfigValue("silent-join-quit")
    private boolean silentJoinQuit = true;

    @ConfigValue("hide-chat")
    private boolean hideChat = true;

    @Comment("Swallow the ghost's own chat messages and private-message commands so nothing reaches other players")
    @ConfigValue("mute-chat")
    private boolean muteChat = true;

    @ConfigValue("hide-from-list")
    private boolean hideFromList = true;

    @ConfigValue("ignore-world")
    private boolean ignoreWorld = true;

    @ConfigValue("invulnerable")
    private boolean invulnerable = true;

    @ConfigValue("mobs-ignore")
    private boolean mobsIgnore = true;

    @ConfigValue("no-hunger")
    private boolean noHunger = true;

    @Comment("While ghosted, a sprint (double W or Ctrl+W) in flight slips you into spectator to phase through walls")
    @ConfigValue("noclip-on-sprint")
    private boolean noclipOnSprint = true;

    @ConfigValue("persist")
    private boolean persist = true;

    public boolean persist() {
        return persist;
    }

    public StealthOptions options() {
        return new StealthOptions(creative, hideEntity, hideFromTab, silentJoinQuit, hideChat, muteChat,
                hideFromList, ignoreWorld, invulnerable, mobsIgnore, noHunger, noclipOnSprint);
    }
}
