package org.imdel.letstroll;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.imdel.letstroll.ghostmode.GhostModeCommand;
import org.imdel.letstroll.ghostmode.GhostModeListener;
import org.imdel.letstroll.ghostmode.GhostStorage;
import org.imdel.letstroll.stand.StandCommand;
import org.imdel.letstroll.tool.ToolCommand;
import org.imdel.letstroll.tool.ToolListener;
import org.imdel.letstroll.util.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

public class LetsTroll extends JavaPlugin {
    private static LetsTroll instance;
    private static final String VERSION_API = "https://api.github.com/repos/imdelewer/LetsTroll/releases/latest";
    private final Set<BukkitTask> activeTasks = new HashSet<>();
    private Thread watchThread;

    public static LetsTroll getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        String currentVersion = getDescription().getVersion();
        Logger.startup(currentVersion);

        GhostStorage.init(this);

        // Регистрация команд и слушателей
        Bukkit.getPluginManager().registerEvents(new GhostModeListener(this), this);
        PluginCommand ghostCmd = this.getCommand("ghost");
        if (ghostCmd != null) ghostCmd.setExecutor(new GhostModeCommand(this));

        PluginCommand standCmd = this.getCommand("stand");
        if (standCmd != null) {
            StandCommand standCommand = new StandCommand(this);
            standCmd.setExecutor(standCommand);
            standCmd.setTabCompleter(standCommand);
        }

        Bukkit.getPluginManager().registerEvents(new ToolListener(this), this);
        PluginCommand toolCmd = this.getCommand("tool");
        if (toolCmd != null) {
            ToolCommand toolCommand = new ToolCommand(this);
            toolCmd.setExecutor(toolCommand);
            toolCmd.setTabCompleter(toolCommand);
        }

        saveResource("stands.yml", false);
        StandCommand.reloadStands();

        startWatchStandFile();
        checkVersionAsync(currentVersion);
    }

    private void startWatchStandFile() {
        Path folder = getDataFolder().toPath();

        watchThread = new Thread(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                folder.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path changed = (Path) event.context();
                        if (changed.getFileName().toString().equals("stands.yml")) {
                            Bukkit.getScheduler().runTask(this, StandCommand::reloadStands);
                            Logger.fancy("▓▒░ [stands.yml] configuration updated. ░▒▓");
                        }
                    }
                    key.reset();
                }
            } catch (InterruptedException ignored) {
                // Expected when thread is interrupted on shutdown
            } catch (Exception e) {
                Logger.error("Error watching stands.yml: " + e.getMessage());
            }
        }, "LetsTroll-StandWatcher");

        watchThread.start();
    }

    private void checkVersionAsync(String currentVersion) {
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(VERSION_API).openConnection();
                con.setRequestProperty("Accept", "application/vnd.github.v3+json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String json = response.toString();
                String latestTag = json.split("\"tag_name\":\"")[1].split("\"")[0];

                if (!currentVersion.equals(latestTag)) {
                    Logger.fancy("░ New version available: " + latestTag);
                    Logger.fancy("▒ Please update: https://github.com/imdelewer/LetsTroll/releases");
                } else {
                    Logger.fancy("▓ You are running the latest version.");
                }
            } catch (Exception e) {
                Logger.warn("Version check failed: " + e.getMessage());
            }
        });

        activeTasks.add(task);
    }

    @Override
    public void onDisable() {
        GhostStorage.save();

        if (watchThread != null && watchThread.isAlive()) {
            watchThread.interrupt();
            try {
                watchThread.join(2000);
            } catch (InterruptedException ignored) {}
        }

        for (BukkitTask task : activeTasks) {
            task.cancel();
        }
        activeTasks.clear();

        Logger.fancy("Plugin disabled.");
    }
}
