package dev.delewer.letstroll.modules.events;

public interface TrollEvent {

    String id();

    boolean dangerous();

    boolean defaultEnabled();

    TargetMode targetMode();

    void run(EventContext context);

    enum TargetMode {
        ALL,
        RANDOM_ONE,
        RANDOM_PAIR
    }
}
