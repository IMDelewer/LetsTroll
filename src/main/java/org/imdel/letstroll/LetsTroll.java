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

        GhostStorage.init(this);

        registerCommand("ghost", new GhostModeCommand(this), null);
        StandCommand standCommand = new StandCommand(this);
        registerCommand("stand", standCommand, standCommand);
        Bukkit.getPluginManager().registerEvents(new GhostModeListener(this), this);

        ToolCommand toolCommand = new ToolCommand(this);
        registerCommand("tool", toolCommand, toolCommand);
        Bukkit.getPluginManager().registerEvents(new ToolListener(this), this);

        saveResource("stands.yml", false);
        StandCommand.reloadStands();

        startWatchStandFile();
        checkVersionAsync(currentVersion);
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor, org.bukkit.command.TabCompleter completer) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
            if (completer != null) cmd.setTabCompleter(completer);
        }
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
                        if ("stands.yml".equals(changed.getFileName().toString())) {
                            Bukkit.getScheduler().runTask(this, StandCommand::reloadStands);
                            Logger.fancy("▓▒░ [stands.yml] configuration updated. ░▒▓");
                        }
                    }
                    key.reset();
                }
            } catch (InterruptedException ignored) {
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
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);

                    String json = response.toString();
                    String latestTag = json.split("\"tag_name\":\"")[1].split("\"")[0];

                    boolean isLatest = currentVersion.equals(latestTag);

                    Bukkit.getScheduler().runTask(this, () ->
                            Logger.startup(currentVersion, isLatest, latestTag)
                    );
                }
            } catch (Exception e) {
                Logger.warn("Version check failed: " + e.getMessage());
                Bukkit.getScheduler().runTask(this, () ->
                        Logger.startup(currentVersion, true, "unknown")
                );
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

        activeTasks.forEach(BukkitTask::cancel);
        activeTasks.clear();

        Logger.fancy("Plugin disabled.");
    }
}
