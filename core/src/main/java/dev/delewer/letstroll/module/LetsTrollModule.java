package dev.delewer.letstroll.module;

public interface LetsTrollModule {

    void enable(ModuleContext context);

    default void disable() {
    }
}
