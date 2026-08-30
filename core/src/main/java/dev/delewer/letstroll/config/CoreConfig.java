package dev.delewer.letstroll.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.ua.theroer.magicutils.config.annotations.Comment;
import dev.ua.theroer.magicutils.config.annotations.ConfigFile;
import dev.ua.theroer.magicutils.config.annotations.ConfigSection;
import dev.ua.theroer.magicutils.config.annotations.ConfigValue;

@ConfigFile("config.{ext}")
public final class CoreConfig {

    @Comment("Language used for menus and messages, matches a file in lang/")
    @ConfigValue("language")
    private String language = "en";

    @Comment("Module ids that should stay disabled, for example: [ghost]")
    @ConfigValue("disabled-modules")
    private List<String> disabledModules = new ArrayList<>();

    @ConfigSection("menu")
    private MenuSettings menu = new MenuSettings();

    @ConfigSection("hide")
    private HideSettings hide = new HideSettings();

    @ConfigSection("modules")
    private ModuleSettings modules = new ModuleSettings();

    public ModuleSettings modules() {
        if (modules == null) {
            modules = new ModuleSettings();
        }
        return modules;
    }

    public String language() {
        return language == null || language.isBlank() ? "en" : language;
    }

    public MenuSettings menu() {
        if (menu == null) {
            menu = new MenuSettings();
        }
        return menu;
    }

    public HideSettings hide() {
        if (hide == null) {
            hide = new HideSettings();
        }
        return hide;
    }

    public List<String> disabledModules() {
        if (disabledModules == null) {
            return List.of();
        }
        return disabledModules.stream().map(value -> value.toLowerCase(Locale.ROOT)).toList();
    }

    public String menuFiller() {
        return menu().filler();
    }

    public int menuRows() {
        return menu().rows();
    }

    public int playersPerPage() {
        return menu().playersPerPage();
    }

    public int menuRefreshTicks() {
        return menu().refreshTicks();
    }

    public boolean soundsEnabled() {
        return menu().sounds().enabled();
    }

    public void setSoundsEnabled(boolean value) {
        menu().sounds().setEnabled(value);
    }

    public boolean hideFromPlugins() {
        return hide().pluginsCommand();
    }

    public void setHideFromPlugins(boolean value) {
        hide().setPluginsCommand(value);
    }

    public boolean hideCommands() {
        return hide().commands();
    }

    public void setHideCommands(boolean value) {
        hide().setCommands(value);
    }

    public boolean nativeHide() {
        return hide().plugin();
    }

    public void setNativeHide(boolean value) {
        hide().setPlugin(value);
    }

    public String soundOpen() {
        return menu().sounds().open();
    }

    public String soundClick() {
        return menu().sounds().click();
    }

    public String soundToggleOn() {
        return menu().sounds().toggleOn();
    }

    public String soundToggleOff() {
        return menu().sounds().toggleOff();
    }

    public String soundError() {
        return menu().sounds().error();
    }
}
