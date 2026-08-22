package dev.delewer.letstroll.platform;

public interface TaskScheduler {

    void sync(Runnable task);

    void later(Runnable task, long ticks);

    void async(Runnable task);

    Cancellable repeating(Runnable task, long intervalTicks);

    interface Cancellable {

        void cancel();
    }
}
