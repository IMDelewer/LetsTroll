package dev.delewer.letstroll.fabric;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import dev.delewer.letstroll.platform.TaskScheduler;
import net.minecraft.server.MinecraftServer;

public final class FabricScheduler implements TaskScheduler {

    private static final class Task {
        private final Runnable action;
        private long nextRun;
        private final long interval;
        private volatile boolean cancelled;

        private Task(Runnable action, long nextRun, long interval) {
            this.action = action;
            this.nextRun = nextRun;
            this.interval = interval;
        }
    }

    private final Supplier<MinecraftServer> server;
    private final ConcurrentLinkedQueue<Task> tasks = new ConcurrentLinkedQueue<>();
    private final ExecutorService async = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "LetsTroll-Async");
        thread.setDaemon(true);
        return thread;
    });
    private long currentTick;

    public FabricScheduler(Supplier<MinecraftServer> server) {
        this.server = server;
    }

    public void tick() {
        currentTick++;
        for (Task task : tasks) {
            if (task.cancelled) {
                tasks.remove(task);
                continue;
            }
            if (currentTick < task.nextRun) {
                continue;
            }
            try {
                task.action.run();
            } catch (RuntimeException ignored) {
            }
            if (task.interval > 0) {
                task.nextRun = currentTick + task.interval;
            } else {
                tasks.remove(task);
            }
        }
    }

    public void shutdown() {
        tasks.clear();
        async.shutdownNow();
    }

    @Override
    public void sync(Runnable task) {
        MinecraftServer instance = server.get();
        if (instance == null || instance.isOnThread()) {
            task.run();
            return;
        }
        instance.execute(task);
    }

    @Override
    public void later(Runnable task, long ticks) {
        tasks.add(new Task(task, currentTick + Math.max(1L, ticks), 0));
    }

    @Override
    public void async(Runnable task) {
        async.execute(task);
    }

    @Override
    public Cancellable repeating(Runnable task, long intervalTicks) {
        long interval = Math.max(1L, intervalTicks);
        Task scheduled = new Task(task, currentTick + interval, interval);
        tasks.add(scheduled);
        return () -> scheduled.cancelled = true;
    }
}
