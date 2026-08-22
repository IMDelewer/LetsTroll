package dev.delewer.letstroll.paper;

import dev.delewer.letstroll.platform.TaskScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperScheduler implements TaskScheduler {

    private final JavaPlugin plugin;

    public PaperScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void sync(Runnable task) {
        if (Bukkit.isGlobalTickThread()) {
            task.run();
            return;
        }
        PaperTasks.global(plugin, task);
    }

    @Override
    public void later(Runnable task, long ticks) {
        PaperTasks.globalLater(plugin, task, ticks);
    }

    @Override
    public void async(Runnable task) {
        PaperTasks.async(plugin, task);
    }

    @Override
    public Cancellable repeating(Runnable task, long intervalTicks) {
        long interval = Math.max(1L, intervalTicks);
        ScheduledTask handle = Bukkit.getGlobalRegionScheduler()
                .runAtFixedRate(plugin, ignored -> task.run(), interval, interval);
        return handle::cancel;
    }
}
