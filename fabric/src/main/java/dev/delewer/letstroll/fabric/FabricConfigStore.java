package dev.delewer.letstroll.fabric;

import dev.delewer.letstroll.platform.ConfigStore;
import dev.ua.theroer.magicutils.config.ConfigManager;

public final class FabricConfigStore implements ConfigStore {

    private final ConfigManager manager;

    public FabricConfigStore(ConfigManager manager) {
        this.manager = manager;
    }

    @Override
    public <T> T load(Class<T> type) {
        return manager.register(type);
    }

    @Override
    public void save(Object config) {
        manager.save(config);
    }

    @Override
    public void reloadAll() {
        manager.reloadAll();
    }
}
