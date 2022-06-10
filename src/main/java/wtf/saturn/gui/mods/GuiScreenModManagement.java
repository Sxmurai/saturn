package wtf.saturn.gui.mods;

import net.minecraft.client.gui.GuiScreen;
import wtf.saturn.feature.cache.impl.module.ModuleCache;
import wtf.saturn.feature.impl.modules.core.ModMenu;
import wtf.saturn.gui.mods.components.ModHolder;

import java.io.IOException;

public class GuiScreenModManagement extends GuiScreen {
    private static GuiScreenModManagement instance;

    private final ModHolder holder;

    private GuiScreenModManagement() {
        holder = new ModHolder(435.0, 235.0);
    }

    @Override
    public void initGui() {
        // disable module
        ModuleCache.get().getModule(ModMenu.class).setState(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // TODO: shader background
        holder.drawComponent(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        holder.mouseClick(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        holder.mouseRelease(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        holder.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public static GuiScreenModManagement getInstance() {
        if (instance == null) {
            instance = new GuiScreenModManagement();
        }

        return instance;
    }
}
