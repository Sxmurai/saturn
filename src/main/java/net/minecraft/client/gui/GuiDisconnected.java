package net.minecraft.client.gui;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IChatComponent;

public class GuiDisconnected extends GuiScreen
{
    private final String reason;
    private final IChatComponent message;
    private List<String> multilineMessage;
    private final GuiScreen parentScreen;
    private int field_175353_i;

    public GuiDisconnected(GuiScreen screen, String reasonLocalizationKey, IChatComponent chatComp)
    {
        parentScreen = screen;
        reason = I18n.format(reasonLocalizationKey);
        message = chatComp;
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        buttonList.clear();
        multilineMessage = fontRendererObj.listFormattedStringToWidth(message.getFormattedText(), width - 50);
        field_175353_i = multilineMessage.size() * fontRendererObj.FONT_HEIGHT;
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 2 + field_175353_i / 2 + fontRendererObj.FONT_HEIGHT, I18n.format("gui.toMenu")));
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            mc.displayGuiScreen(parentScreen);
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, reason, width / 2, height / 2 - field_175353_i / 2 - fontRendererObj.FONT_HEIGHT * 2, 11184810);
        int i = height / 2 - field_175353_i / 2;

        if (multilineMessage != null)
        {
            for (String s : multilineMessage)
            {
                drawCenteredString(fontRendererObj, s, width / 2, i, 16777215);
                i += fontRendererObj.FONT_HEIGHT;
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
