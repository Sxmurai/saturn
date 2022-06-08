package wtf.saturn.launch;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.bush.eventbus.bus.EventBus;
import org.lwjgl.opengl.Display;
import wtf.saturn.feature.cache.Caches;
import wtf.saturn.feature.cache.impl.account.AccountCache;
import wtf.saturn.feature.cache.impl.module.ModuleCache;
import wtf.saturn.util.versioning.BuildConfig;

/**
 * The main class of the client, initializing all client variables
 *
 * @author aesthetical
 * @since 6/7/22
 */
@Slf4j
public class Launcher {
    private static boolean started = false;

    public static final ClientEnvironment ENV = ClientEnvironment.DEVELOPER;
    public static final String NAME = "Saturn";
    public static final String VERSION = "1.0.0";
    public static final String FULL = NAME + " v" + VERSION + "-" + BuildConfig.HASH + "/" + BuildConfig.BRANCH;

    public static final EventBus BUS = new EventBus(log::error);

    @SneakyThrows
    public static void init() {
        if (started) {
            throw new IllegalAccessException("Should not be accessing while client is already started!");
        }

        started = true;

        log.info("Loading {}", FULL);
        Display.setTitle(FULL);

        Caches.registerCache(new ModuleCache());
        Caches.registerCache(new AccountCache());
    }


}
