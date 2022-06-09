package net.minecraft.client.gui;

import com.google.common.base.Predicate;
import java.io.IOException;
import java.net.IDN;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

public class GuiScreenAddServer extends GuiScreen
{
    private final GuiScreen parentScreen;
    private final ServerData serverData;
    private GuiTextField serverIPField;
    private GuiTextField serverNameField;
    private GuiButton serverResourcePacks;
    private final Predicate<String> field_181032_r = new Predicate<String>()
    {
        public boolean apply(String p_apply_1_)
        {
            if (p_apply_1_.length() == 0)
            {
                return true;
            }
            else
            {
                String[] astring = p_apply_1_.split(":");

                if (astring.length == 0)
                {
                    return true;
                }
                else
                {
                    try
                    {
                        String s = IDN.toASCII(astring[0]);
                        return true;
                    }
                    catch (IllegalArgumentException var4)
                    {
                        return false;
                    }
                }
            }
        }
    };

    public GuiScreenAddServer(GuiScreen p_i1033_1_, ServerData p_i1033_2_)
    {
        parentScreen = p_i1033_1_;
        serverData = p_i1033_2_;
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        serverNameField.updateCursorCounter();
        serverIPField.updateCursorCounter();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 96 + 18, I18n.format("addServer.add")));
        buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 120 + 18, I18n.format("gui.cancel")));
        buttonList.add(serverResourcePacks = new GuiButton(2, width / 2 - 100, height / 4 + 72, I18n.format("addServer.resourcePack") + ": " + serverData.getResourceMode().getMotd().getFormattedText()));
        serverNameField = new GuiTextField(0, fontRendererObj, width / 2 - 100, 66, 200, 20);
        serverNameField.setFocused(true);
        serverNameField.setText(serverData.serverName);
        serverIPField = new GuiTextField(1, fontRendererObj, width / 2 - 100, 106, 200, 20);
        serverIPField.setMaxStringLength(128);
        serverIPField.setText(serverData.serverIP);
        serverIPField.func_175205_a(field_181032_r);
        buttonList.get(0).enabled = serverIPField.getText().length() > 0 && serverIPField.getText().split(":").length > 0 && serverNameField.getText().length() > 0;
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
            if (button.id == 2)
            {
                serverData.setResourceMode(ServerData.ServerResourceMode.values()[(serverData.getResourceMode().ordinal() + 1) % ServerData.ServerResourceMode.values().length]);
                serverResourcePacks.displayString = I18n.format("addServer.resourcePack") + ": " + serverData.getResourceMode().getMotd().getFormattedText();
            }
            else if (button.id == 1)
            {
                parentScreen.confirmClicked(false, 0);
            }
            else if (button.id == 0)
            {
                serverData.serverName = serverNameField.getText();
                serverData.serverIP = serverIPField.getText();
                parentScreen.confirmClicked(true, 0);
            }
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        serverNameField.textboxKeyTyped(typedChar, keyCode);
        serverIPField.textboxKeyTyped(typedChar, keyCode);

        if (keyCode == 15)
        {
            serverNameField.setFocused(!serverNameField.isFocused());
            serverIPField.setFocused(!serverIPField.isFocused());
        }

        if (keyCode == 28 || keyCode == 156)
        {
            actionPerformed(buttonList.get(0));
        }

        buttonList.get(0).enabled = serverIPField.getText().length() > 0 && serverIPField.getText().split(":").length > 0 && serverNameField.getText().length() > 0;
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        serverIPField.mouseClicked(mouseX, mouseY, mouseButton);
        serverNameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("addServer.title"), width / 2, 17, 16777215);
        drawString(fontRendererObj, I18n.format("addServer.enterName"), width / 2 - 100, 53, 10526880);
        drawString(fontRendererObj, I18n.format("addServer.enterIp"), width / 2 - 100, 94, 10526880);
        serverNameField.drawTextBox();
        serverIPField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
