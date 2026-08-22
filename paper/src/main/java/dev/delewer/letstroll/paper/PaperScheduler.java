package dev.delewer.letstroll.paper;

import dev.delewer.letstroll.platform.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class PaperScheduler implements TaskScheduler {

    private final JavaPlugin plugin;

    public PaperScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void sync(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
            return;
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void later(Runnable task, long ticks) {
        Bukkit.getScheduler().runTaskLater(plugin, task, Math.max(1L, ticks));
    }

    @Override
    public void async(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public Cancellable repeating(Runnable task, long intervalTicks) {
        long interval = Math.max(1L, intervalTicks);
        BukkitTask handle = Bukkit.getScheduler().runTaskTimer(plugin, task, interval, interval);
        return handle::cancel;
    }
}
