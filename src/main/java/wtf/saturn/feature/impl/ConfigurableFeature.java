package wtf.saturn.feature.impl;

import lombok.Getter;
import org.lwjgl.input.Keyboard;
import wtf.saturn.feature.impl.setting.Bind;
import wtf.saturn.feature.impl.setting.Setting;
import wtf.saturn.util.reflect.ReflectionUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A feature that can be configured
 *
 * @author aesthetical
 * @since 6/7/22
 */
@Getter
public class ConfigurableFeature extends ToggleableFeature {
    private final Map<String, Setting> settingMap = new HashMap<>();
    private final List<Setting> settings = new CopyOnWriteArrayList<>();
    private final Bind bind = new Bind("Keybind", Keyboard.KEY_NONE);

    public ConfigurableFeature(String name) {
        super(name);
    }

    /**
     * Uses reflections to automatically register settings defined as fields in the class
     */
    public void reflectSettings() {
        ReflectionUtil.allFieldsWithType(this, Setting.class)
                .forEach((field) -> {
                    field.setAccessible(true);

                    Setting setting;
                    try {
                        setting = (Setting) field.get(this);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return;
                    }

                    if (setting != null) {
                        settingMap.put(setting.getName(), setting);
                        settings.add(setting);
                    }
                });
    }

    public void setBind(int bindIn) {
        bind.setValue(bindIn);
    }
}
