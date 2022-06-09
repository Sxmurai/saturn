package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import org.lwjgl.input.Keyboard;

public class GuiRenameWorld extends GuiScreen
{
    private final GuiScreen parentScreen;
    private GuiTextField field_146583_f;
    private final String saveName;

    public GuiRenameWorld(GuiScreen parentScreenIn, String saveNameIn)
    {
        parentScreen = parentScreenIn;
        saveName = saveNameIn;
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        field_146583_f.updateCursorCounter();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 96 + 12, I18n.format("selectWorld.renameButton")));
        buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 120 + 12, I18n.format("gui.cancel")));
        ISaveFormat isaveformat = mc.getSaveLoader();
        WorldInfo worldinfo = isaveformat.getWorldInfo(saveName);
        String s = worldinfo.getWorldName();
        field_146583_f = new GuiTextField(2, fontRendererObj, width / 2 - 100, 60, 200, 20);
        field_146583_f.setFocused(true);
        field_146583_f.setText(s);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 1)
            {
                mc.displayGuiScreen(parentScreen);
            }
            else if (button.id == 0)
            {
                ISaveFormat isaveformat = mc.getSaveLoader();
                isaveformat.renameWorld(saveName, field_146583_f.getText().trim());
                mc.displayGuiScreen(parentScreen);
            }
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        field_146583_f.textboxKeyTyped(typedChar, keyCode);
        buttonList.get(0).enabled = field_146583_f.getText().trim().length() > 0;

        if (keyCode == 28 || keyCode == 156)
        {
            actionPerformed(buttonList.get(0));
        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        field_146583_f.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("selectWorld.renameTitle"), width / 2, 20, 16777215);
        drawString(fontRendererObj, I18n.format("selectWorld.enterName"), width / 2 - 100, 47, 10526880);
        field_146583_f.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
