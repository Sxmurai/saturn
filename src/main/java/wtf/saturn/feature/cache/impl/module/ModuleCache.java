package wtf.saturn.feature.cache.impl.module;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import me.bush.eventbus.annotation.EventListener;
import wtf.saturn.event.KeyPressEvent;
import wtf.saturn.feature.cache.BaseCache;
import wtf.saturn.feature.cache.Caches;
import wtf.saturn.feature.cache.impl.module.impl.Module;
import wtf.saturn.feature.cache.impl.module.impl.ModuleCategory;
import wtf.saturn.util.reflect.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Where modules are automatically registered
 *
 * @author aesthetical
 * @since 6/7/22
 */
@Getter
@Log4j2
public class ModuleCache extends BaseCache<Module> {
    private final Map<ModuleCategory, CopyOnWriteArrayList<Module>> modulesByCategory = new HashMap<>();
    private final List<Module> modules = new CopyOnWriteArrayList<>();

    @SneakyThrows
    @Override
    public void init() {
        ReflectionUtil.getClasses("wtf.saturn.feature.impl.modules", Module.class)
                .forEach((clazz) -> {
                    try {
                        Module module = clazz.getConstructor().newInstance();
                        addModule(module);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                });

        log.info("Loaded {} modules!", modules.size());
    }

    @EventListener
    public void onKeyPress(KeyPressEvent event) {
        if (!event.isState() && mc.currentScreen == null) {
            for (Module module : modules) {
                if (event.getKeyCode() == module.getBind().getValue()) {
                    module.setState(!module.isToggled());
                }
            }
        }
    }

    public <T extends Module> T getModule(Class<? extends Module> clazz) {
        return (T) objects.getOrDefault(clazz, null);
    }

    private void addModule(Module module) {
        module.reflectSettings();

        objects.put(module.getClass(), module);

        CopyOnWriteArrayList<Module> category = modulesByCategory.computeIfAbsent(module.getCategory(),
                (s) -> new CopyOnWriteArrayList<>());

        category.add(module);
        modulesByCategory.put(module.getCategory(), category);

        modules.add(module);
    }

    public static ModuleCache get() {
        return Caches.getCache(ModuleCache.class);
    }
}
