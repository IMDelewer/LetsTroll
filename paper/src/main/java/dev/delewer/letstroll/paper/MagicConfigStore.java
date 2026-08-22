package dev.delewer.letstroll.paper;

import dev.delewer.letstroll.platform.ConfigStore;
import dev.ua.theroer.magicutils.config.ConfigManager;

public final class MagicConfigStore implements ConfigStore {

    private final ConfigManager manager;

    public MagicConfigStore(ConfigManager manager) {
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
