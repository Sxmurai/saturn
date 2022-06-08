package wtf.saturn.feature.impl.modules.combat;

import com.google.common.collect.Lists;
import me.bush.eventbus.annotation.EventListener;
import wtf.saturn.event.TickEvent;
import wtf.saturn.feature.cache.impl.module.impl.Module;
import wtf.saturn.feature.cache.impl.module.impl.ModuleCategory;
import wtf.saturn.feature.impl.setting.Setting;
import wtf.saturn.util.net.ServerUtil;

import java.util.List;

public class AutoPlay extends Module {
    // TODO: more servers
    private final List<String> SUPPORTED_SERVERS = Lists.newArrayList("hypixel.net", "blocksmc.com");

    public AutoPlay() {
        super("Auto Play", ModuleCategory.COMBAT, "Automatically puts you into the next game");
    }

    public final Setting<Integer> delay = new Setting<>("Delay", 2, 1, 10);

    @EventListener
    public void onTick(TickEvent event) {

    }

    private boolean isOnSupportedServer() {
        return SUPPORTED_SERVERS.stream().anyMatch(ServerUtil::isOnServer);
    }
}
