package wtf.saturn.feature.impl.modules.core;

import org.lwjgl.input.Keyboard;
import wtf.saturn.feature.cache.impl.module.impl.Module;
import wtf.saturn.feature.cache.impl.module.impl.ModuleCategory;
import wtf.saturn.gui.mods.GuiScreenModManagement;

public class ModMenu extends Module {
    public ModMenu() {
        super("Mod Menu", ModuleCategory.CORE, "Manages all of the client's modifications");
        setBind(Keyboard.KEY_RSHIFT);
    }

    @Override
    protected void onActivated() {
        super.onActivated();

        if (nullCheck()) {
            setState(false);
            return;
        }

        mc.displayGuiScreen(GuiScreenModManagement.getInstance());
    }
}
