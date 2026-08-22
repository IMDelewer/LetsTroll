package dev.delewer.letstroll.paper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginConcealer {

    private PluginConcealer() {
    }

    @SuppressWarnings("unchecked")
    public static boolean conceal(JavaPlugin plugin, dev.ua.theroer.magicutils.logger.PrefixedLogger log) {
        try {
            Class<?> impl = Class.forName("io.papermc.paper.plugin.manager.PaperPluginManagerImpl");
            Object manager = impl.getMethod("getInstance").invoke(null);

            Field instanceField = impl.getDeclaredField("instanceManager");
            instanceField.setAccessible(true);
            Object instanceManager = instanceField.get(manager);

            Field pluginsField = instanceManager.getClass().getDeclaredField("plugins");
            pluginsField.setAccessible(true);
            List<Plugin> plugins = (List<Plugin>) pluginsField.get(instanceManager);
            plugins.removeIf(entry -> entry == plugin);

            Field lookupField = instanceManager.getClass().getDeclaredField("lookupNames");
            lookupField.setAccessible(true);
            Map<String, Plugin> lookup = (Map<String, Plugin>) lookupField.get(instanceManager);
            lookup.values().removeIf(entry -> entry == plugin);

            return isConcealed(plugin);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            log.debug("Native hide unavailable: %s", exception.getMessage());
            return false;
        }
    }

    public static boolean isConcealed(JavaPlugin plugin) {
        for (Plugin entry : plugin.getServer().getPluginManager().getPlugins()) {
            if (entry == plugin) {
                return false;
            }
        }
        return plugin.getServer().getPluginManager().getPlugin(plugin.getName()) == null;
    }
}
