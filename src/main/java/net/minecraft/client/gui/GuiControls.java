package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public class GuiControls extends GuiScreen
{
    private static final GameSettings.Options[] optionsArr = new GameSettings.Options[] {GameSettings.Options.INVERT_MOUSE, GameSettings.Options.SENSITIVITY, GameSettings.Options.TOUCHSCREEN};

    /**
     * A reference to the screen object that created this. Used for navigating between screens.
     */
    private final GuiScreen parentScreen;
    protected String screenTitle = "Controls";

    /** Reference to the GameSettings object. */
    private final GameSettings options;

    /** The ID of the button that has been pressed. */
    public KeyBinding buttonId = null;
    public long time;
    private GuiKeyBindingList keyBindingList;
    private GuiButton buttonReset;

    public GuiControls(GuiScreen screen, GameSettings settings)
    {
        parentScreen = screen;
        options = settings;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        keyBindingList = new GuiKeyBindingList(this, mc);
        buttonList.add(new GuiButton(200, width / 2 - 155, height - 29, 150, 20, I18n.format("gui.done")));
        buttonList.add(buttonReset = new GuiButton(201, width / 2 - 155 + 160, height - 29, 150, 20, I18n.format("controls.resetAll")));
        screenTitle = I18n.format("controls.title");
        int i = 0;

        for (GameSettings.Options gamesettings$options : GuiControls.optionsArr)
        {
            if (gamesettings$options.getEnumFloat())
            {
                buttonList.add(new GuiOptionSlider(gamesettings$options.returnEnumOrdinal(), width / 2 - 155 + i % 2 * 160, 18 + 24 * (i >> 1), gamesettings$options));
            }
            else
            {
                buttonList.add(new GuiOptionButton(gamesettings$options.returnEnumOrdinal(), width / 2 - 155 + i % 2 * 160, 18 + 24 * (i >> 1), gamesettings$options, options.getKeyBinding(gamesettings$options)));
            }

            ++i;
        }
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        keyBindingList.handleMouseInput();
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 200)
        {
            mc.displayGuiScreen(parentScreen);
        }
        else if (button.id == 201)
        {
            for (KeyBinding keybinding : mc.gameSettings.keyBindings)
            {
                keybinding.setKeyCode(keybinding.getKeyCodeDefault());
            }

            KeyBinding.resetKeyBindingArrayAndHash();
        }
        else if (button.id < 100 && button instanceof GuiOptionButton)
        {
            options.setOptionValue(((GuiOptionButton)button).returnEnumOptions(), 1);
            button.displayString = options.getKeyBinding(GameSettings.Options.getEnumOptions(button.id));
        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (buttonId != null)
        {
            options.setOptionKeyBinding(buttonId, -100 + mouseButton);
            buttonId = null;
            KeyBinding.resetKeyBindingArrayAndHash();
        }
        else if (mouseButton != 0 || !keyBindingList.mouseClicked(mouseX, mouseY, mouseButton))
        {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    /**
     * Called when a mouse button is released.  Args : mouseX, mouseY, releaseButton
     */
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (state != 0 || !keyBindingList.mouseReleased(mouseX, mouseY, state))
        {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (buttonId != null)
        {
            if (keyCode == 1)
            {
                options.setOptionKeyBinding(buttonId, 0);
            }
            else if (keyCode != 0)
            {
                options.setOptionKeyBinding(buttonId, keyCode);
            }
            else if (typedChar > 0)
            {
                options.setOptionKeyBinding(buttonId, typedChar + 256);
            }

            buttonId = null;
            time = Minecraft.getSystemTime();
            KeyBinding.resetKeyBindingArrayAndHash();
        }
        else
        {
            super.keyTyped(typedChar, keyCode);
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        keyBindingList.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRendererObj, screenTitle, width / 2, 8, 16777215);
        boolean flag = true;

        for (KeyBinding keybinding : options.keyBindings)
        {
            if (keybinding.getKeyCode() != keybinding.getKeyCodeDefault())
            {
                flag = false;
                break;
            }
        }

        buttonReset.enabled = !flag;
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
