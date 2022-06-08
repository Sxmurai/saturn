package wtf.saturn.feature.impl;

import lombok.Getter;
import lombok.Setter;

import static wtf.saturn.launch.Launcher.BUS;

/**
 * Represents a feature that can be toggled
 *
 * @author aesthetical
 * @since 6/7/22
 */
@Getter @Setter
public class ToggleableFeature extends Feature {
    private boolean toggled = false;

    public ToggleableFeature(String name) {
        super(name);
    }

    protected void onActivated() {
        BUS.subscribe(this);
    }

    protected void onDeactivated() {
        BUS.unsubscribe(this);
    }

    public void setState(boolean toggled) {
        setToggled(toggled);

        if (toggled) {
            onActivated();
        } else {
            onDeactivated();
        }
    }
}
