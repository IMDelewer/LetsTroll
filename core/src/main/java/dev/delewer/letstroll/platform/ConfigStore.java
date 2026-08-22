package dev.delewer.letstroll.platform;

public interface ConfigStore {

    <T> T load(Class<T> type);

    void save(Object config);

    void reloadAll();
}
