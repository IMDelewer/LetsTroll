package dev.delewer.letstroll.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;

import dev.delewer.letstroll.LetsTroll;

public final class ModuleManager {

    private final LetsTroll core;
    private final Map<String, LetsTrollModule> loaded = new LinkedHashMap<>();
    private final List<ModuleInfo> infos = new ArrayList<>();

    public ModuleManager(LetsTroll core) {
        this.core = core;
    }

    public void loadAll(Collection<String> disabled) {
        List<LetsTrollModule> discovered = new ArrayList<>();
        ServiceLoader.load(LetsTrollModule.class, getClass().getClassLoader()).forEach(discovered::add);
        discovered.sort(Comparator.comparingInt(module -> ModuleInfo.of(module).order()));

        for (LetsTrollModule module : discovered) {
            ModuleInfo info = ModuleInfo.of(module);
            if (disabled.contains(info.id().toLowerCase(Locale.ROOT))) {
                continue;
            }
            try {
                module.enable(new ModuleContext(core, info));
                loaded.put(info.id(), module);
                infos.add(info);
            } catch (RuntimeException exception) {
                core.platform().logger().log(Level.SEVERE, "Module " + info.id() + " failed to enable", exception);
            }
        }
    }

    public void unloadAll() {
        List<LetsTrollModule> reversed = new ArrayList<>(loaded.values());
        java.util.Collections.reverse(reversed);
        for (LetsTrollModule module : reversed) {
            try {
                module.disable();
            } catch (RuntimeException exception) {
                core.platform().logger().log(Level.WARNING, "Module failed to disable", exception);
            }
        }
        loaded.clear();
        infos.clear();
    }

    public List<ModuleInfo> infos() {
        return List.copyOf(infos);
    }

    public int count() {
        return loaded.size();
    }
}
