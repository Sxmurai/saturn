package wtf.saturn.feature.impl.setting;

import wtf.saturn.feature.impl.Feature;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Represents a setting that will dictate how a feature may function
 *
 * @param <T> The setting type
 * @author aesthetical
 * @since 6/7/22
 */
public class Setting<T> extends Feature {
    // For number settings
    private final Number min, max;

    // Hierarchy in settings
    private final Setting parent;
    private final Set<Setting> children = new HashSet<>();
    private T value;

    private final Supplier<Boolean> visibility;
    private Feature feature;

    public Setting(Supplier<Boolean> visibility, Setting parentIn, String name, T value, Number min, Number max) {
        super(name);

        parent = parentIn;
        if (parent != null) {
            parent.children.add(this);
        }

        this.value = value;
        this.min = min;
        this.max = max;

        this.visibility = visibility;
    }

    public Setting(Setting parentIn, String name, T value, Number min, Number max) {
        this(null, parentIn, name, value, min, max);
    }

    public Setting(Supplier<Boolean> visibility, String name, T value, Number min, Number max) {
        this(visibility, null, name, value, min, max);
    }

    public Setting(String name, T value, Number min, Number max) {
        this(null, null, name, value, min, max);
    }

    public Setting(Setting parent, String name, T value) {
        this(parent, name, value, null, null);
    }

    public Setting(Supplier<Boolean> visibility, String name, T value) {
        this(visibility, name, value, null, null);
    }

    public Setting(String name, T value) {
        this(null, null, name, value, null, null);
    }

    public static int current(Enum clazz) {
        for (int i = 0; i < clazz.getClass().getEnumConstants().length; ++i) {
            Enum e = ((Enum[]) clazz.getClass().getEnumConstants())[i];
            if (e.toString().equalsIgnoreCase(clazz.toString())) {
                return i;
            }
        }

        return -1;
    }

    public static Enum increase(Enum clazz) {
        int index = current(clazz);
        int next = index + 1;
        Enum[] constants = clazz.getClass().getEnumConstants();

        if (next > constants.length - 1) {
            return constants[0];
        }

        return constants[next];
    }

    public static Enum decrease(Enum clazz) {
        int index = current(clazz);
        int last = index - 1;

        Enum[] constants = clazz.getClass().getEnumConstants();

        if (last < 0) {
            return constants[constants.length - 1];
        }

        return constants[last];
    }

    public static String formatEnum(Enum clazz) {
        String name = clazz.toString().replaceAll("_", " ");
        return Character.toString(name.charAt(0)).toUpperCase() + name.substring(1).toLowerCase();
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Number getMin() {
        return min;
    }

    public Number getMax() {
        return max;
    }

    /**
     * If this setting is a number setting
     *
     * @return if value is a number, and min & max != null
     */
    public boolean isNumberSetting() {
        return value instanceof Number && (min != null && max != null);
    }

    public boolean getVisibility() {
        return visibility == null || visibility.get();
    }

    public Setting getParent() {
        return parent;
    }

    public Set<Setting> getChildren() {
        return children;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Feature getFeature() {
        return feature;
    }
}