package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.resources.I18n;

public class GuiYesNo extends GuiScreen
{
    /**
     * A reference to the screen object that created this. Used for navigating between screens.
     */
    protected GuiYesNoCallback parentScreen;
    protected String messageLine1;
    private final String messageLine2;
    private final List<String> field_175298_s = Lists.newArrayList();

    /** The text shown for the first button in GuiYesNo */
    protected String confirmButtonText;

    /** The text shown for the second button in GuiYesNo */
    protected String cancelButtonText;
    protected int parentButtonClickedId;
    private int ticksUntilEnable;

    public GuiYesNo(GuiYesNoCallback p_i1082_1_, String p_i1082_2_, String p_i1082_3_, int p_i1082_4_)
    {
        parentScreen = p_i1082_1_;
        messageLine1 = p_i1082_2_;
        messageLine2 = p_i1082_3_;
        parentButtonClickedId = p_i1082_4_;
        confirmButtonText = I18n.format("gui.yes");
        cancelButtonText = I18n.format("gui.no");
    }

    public GuiYesNo(GuiYesNoCallback p_i1083_1_, String p_i1083_2_, String p_i1083_3_, String p_i1083_4_, String p_i1083_5_, int p_i1083_6_)
    {
        parentScreen = p_i1083_1_;
        messageLine1 = p_i1083_2_;
        messageLine2 = p_i1083_3_;
        confirmButtonText = p_i1083_4_;
        cancelButtonText = p_i1083_5_;
        parentButtonClickedId = p_i1083_6_;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        buttonList.add(new GuiOptionButton(0, width / 2 - 155, height / 6 + 96, confirmButtonText));
        buttonList.add(new GuiOptionButton(1, width / 2 - 155 + 160, height / 6 + 96, cancelButtonText));
        field_175298_s.clear();
        field_175298_s.addAll(fontRendererObj.listFormattedStringToWidth(messageLine2, width - 50));
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        parentScreen.confirmClicked(button.id == 0, parentButtonClickedId);
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, messageLine1, width / 2, 70, 16777215);
        int i = 90;

        for (String s : field_175298_s)
        {
            drawCenteredString(fontRendererObj, s, width / 2, i, 16777215);
            i += fontRendererObj.FONT_HEIGHT;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Sets the number of ticks to wait before enabling the buttons.
     */
    public void setButtonDelay(int p_146350_1_)
    {
        ticksUntilEnable = p_146350_1_;

        for (GuiButton guibutton : buttonList)
        {
            guibutton.enabled = false;
        }
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        super.updateScreen();

        if (--ticksUntilEnable == 0)
        {
            for (GuiButton guibutton : buttonList)
            {
                guibutton.enabled = true;
            }
        }
    }
}
