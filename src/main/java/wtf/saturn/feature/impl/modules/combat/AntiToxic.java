package wtf.saturn.feature.impl.modules.combat;

import com.google.common.collect.Lists;
import me.bush.eventbus.annotation.EventListener;
import wtf.saturn.event.AddChatMessageEvent;
import wtf.saturn.feature.cache.impl.module.impl.Module;
import wtf.saturn.feature.cache.impl.module.impl.ModuleCategory;
import wtf.saturn.feature.impl.setting.Setting;

import java.util.List;

public class AntiToxic extends Module {
    // TODO: slurs etc
    private final List<String> TOXIC_MESSAGES = Lists.newArrayList(
            "ez",
            "kys",
            "kill yourself",
            "kill your self",
            "horrible",
            "trash"
    );

    public AntiToxic() {
        super("Anti Toxic", ModuleCategory.COMBAT, "Removes any toxic messages from the chat");
    }

    public final Setting<Boolean> report = new Setting<>("Report", false);

    @EventListener
    public void onAddChatMessage(AddChatMessageEvent event) {
        String raw = event.getComponent().getUnformattedText().toLowerCase();
        if (TOXIC_MESSAGES.stream().anyMatch(raw::contains)) {
            event.setCancelled(true);

            if (report.getValue()) {
                // TODO: snitch lel
            }
        }
    }
}
