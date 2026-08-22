package dev.delewer.letstroll.config;

import dev.ua.theroer.magicutils.config.annotations.Comment;
import dev.ua.theroer.magicutils.config.annotations.ConfigValue;

public final class HideSettings {

    @Comment("Drop LetsTroll from the server plugin list so it never appears in /plugins for anyone (may break on future Paper versions)")
    @ConfigValue("plugin")
    private boolean plugin = true;

    @Comment("Rewrite /plugins and /version for players without letstroll.admin")
    @ConfigValue("plugins-command")
    private boolean pluginsCommand = true;

    @Comment("Hide /troll and /ghost from tab completion for players without letstroll.use")
    @ConfigValue("commands")
    private boolean commands = true;

    public boolean plugin() {
        return plugin;
    }

    public void setPlugin(boolean value) {
        this.plugin = value;
    }

    public boolean pluginsCommand() {
        return pluginsCommand;
    }

    public void setPluginsCommand(boolean value) {
        this.pluginsCommand = value;
    }

    public boolean commands() {
        return commands;
    }

    public void setCommands(boolean value) {
        this.commands = value;
    }
}
