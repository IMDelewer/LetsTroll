package org.imdel.letstroll;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import org.imdel.letstroll.ghostmode.*;
import org.imdel.letstroll.stand.*;
import org.imdel.letstroll.tool.*;

import java.nio.file.*;

public class LetsTroll extends JavaPlugin {
    private static LetsTroll instance;

    public static LetsTroll getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        GhostStorage.init(this);

        Bukkit.getPluginManager().registerEvents(new GhostModeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ToolListener(this), this);

        PluginCommand ghostCmd = this.getCommand("ghostmode");
        if (ghostCmd != null) ghostCmd.setExecutor(new GhostModeCommand(this));

        PluginCommand standCmd = this.getCommand("stand");
        if (standCmd != null) standCmd.setExecutor(new StandCommand(this));

        PluginCommand toolCmd = this.getCommand("tool");
        if (toolCmd != null) {
            ToolCommand toolCommand = new ToolCommand(this);
            toolCmd.setExecutor(toolCommand);
            toolCmd.setTabCompleter(toolCommand);
        }

        saveResource("stands.yml", false);
        StandCommand.reloadStands();
        watchStandFile();

        getLogger().info("Let's Troll enabled");
        getLogger().info("Let's start trolling) Hahaha");
    }

    private void watchStandFile() {
        Path folder = getDataFolder().toPath();
        Path file = folder.resolve("stands.yml");

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                folder.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path changed = (Path) event.context();
                        if (changed.getFileName().toString().equals("stands.yml")) {
                            Bukkit.getScheduler().runTask(this, StandCommand::reloadStands);
                            getLogger().info("stands.yml изменён — конфигурация обновлена.");
                        }
                    }
                    key.reset();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onDisable() {
        GhostStorage.save();
        getLogger().info("Well, let's stop trolling (");
        getLogger().info("Let's Troll disabled");
    }
}