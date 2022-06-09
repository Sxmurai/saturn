package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.resources.I18n;

public class GuiMemoryErrorScreen extends GuiScreen
{
    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        buttonList.clear();
        buttonList.add(new GuiOptionButton(0, width / 2 - 155, height / 4 + 120 + 12, I18n.format("gui.toTitle")));
        buttonList.add(new GuiOptionButton(1, width / 2 - 155 + 160, height / 4 + 120 + 12, I18n.format("menu.quit")));
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            mc.displayGuiScreen(new GuiMainMenu());
        }
        else if (button.id == 1)
        {
            mc.shutdown();
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Out of memory!", width / 2, height / 4 - 60 + 20, 16777215);
        drawString(fontRendererObj, "Minecraft has run out of memory.", width / 2 - 140, height / 4 - 60 + 60 + 0, 10526880);
        drawString(fontRendererObj, "This could be caused by a bug in the game or by the", width / 2 - 140, height / 4 - 60 + 60 + 18, 10526880);
        drawString(fontRendererObj, "Java Virtual Machine not being allocated enough", width / 2 - 140, height / 4 - 60 + 60 + 27, 10526880);
        drawString(fontRendererObj, "memory.", width / 2 - 140, height / 4 - 60 + 60 + 36, 10526880);
        drawString(fontRendererObj, "To prevent level corruption, the current game has quit.", width / 2 - 140, height / 4 - 60 + 60 + 54, 10526880);
        drawString(fontRendererObj, "We've tried to free up enough memory to let you go back to", width / 2 - 140, height / 4 - 60 + 60 + 63, 10526880);
        drawString(fontRendererObj, "the main menu and back to playing, but this may not have worked.", width / 2 - 140, height / 4 - 60 + 60 + 72, 10526880);
        drawString(fontRendererObj, "Please restart the game if you see this message again.", width / 2 - 140, height / 4 - 60 + 60 + 81, 10526880);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
