package dev.delewer.letstroll.paper;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperTasks {

    private PaperTasks() {
    }

    public static void global(JavaPlugin plugin, Runnable task) {
        Bukkit.getGlobalRegionScheduler().run(plugin, ignored -> task.run());
    }

    public static void globalLater(JavaPlugin plugin, Runnable task, long ticks) {
        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, ignored -> task.run(), Math.max(1L, ticks));
    }

    public static void async(JavaPlugin plugin, Runnable task) {
        Bukkit.getAsyncScheduler().runNow(plugin, ignored -> task.run());
    }

    public static void asyncLater(JavaPlugin plugin, Runnable task, long ticks) {
        Bukkit.getAsyncScheduler().runDelayed(plugin, ignored -> task.run(),
                Math.max(1L, ticks) * 50L, TimeUnit.MILLISECONDS);
    }

    public static void onEntity(JavaPlugin plugin, Entity entity, Runnable task) {
        entity.getScheduler().run(plugin, ignored -> task.run(), null);
    }

    public static void withEntity(JavaPlugin plugin, Entity entity, Runnable task) {
        if (Bukkit.isOwnedByCurrentRegion(entity)) {
            task.run();
            return;
        }
        entity.getScheduler().run(plugin, ignored -> task.run(), null);
    }

    public static void withRegion(JavaPlugin plugin, Location location, Runnable task) {
        if (Bukkit.isOwnedByCurrentRegion(location)) {
            task.run();
            return;
        }
        Bukkit.getRegionScheduler().run(plugin, location, ignored -> task.run());
    }

    public static void onEntityLater(JavaPlugin plugin, Entity entity, Runnable task, long ticks) {
        entity.getScheduler().runDelayed(plugin, ignored -> task.run(), null, Math.max(1L, ticks));
    }

    public static void onRegion(JavaPlugin plugin, Location location, Runnable task) {
        Bukkit.getRegionScheduler().run(plugin, location, ignored -> task.run());
    }

    public static void onRegionLater(JavaPlugin plugin, Location location, Runnable task, long ticks) {
        Bukkit.getRegionScheduler().runDelayed(plugin, location, ignored -> task.run(), Math.max(1L, ticks));
    }
}
