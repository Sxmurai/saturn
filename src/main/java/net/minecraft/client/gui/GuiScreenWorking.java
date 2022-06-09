package net.minecraft.client.gui;

import net.minecraft.util.IProgressUpdate;

public class GuiScreenWorking extends GuiScreen implements IProgressUpdate
{
    private String field_146591_a = "";
    private String field_146589_f = "";
    private int progress;
    private boolean doneWorking;

    /**
     * Shows the 'Saving level' string.
     */
    public void displaySavingString(String message)
    {
        resetProgressAndMessage(message);
    }

    /**
     * this string, followed by "working..." and then the "% complete" are the 3 lines shown. This resets progress to 0,
     * and the WorkingString to "working...".
     */
    public void resetProgressAndMessage(String message)
    {
        field_146591_a = message;
        displayLoadingString("Working...");
    }

    /**
     * Displays a string on the loading screen supposed to indicate what is being done currently.
     */
    public void displayLoadingString(String message)
    {
        field_146589_f = message;
        setLoadingProgress(0);
    }

    /**
     * Updates the progress bar on the loading screen to the specified amount. Args: loadProgress
     */
    public void setLoadingProgress(int progress)
    {
        this.progress = progress;
    }

    public void setDoneWorking()
    {
        doneWorking = true;
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        if (doneWorking)
        {
            if (!mc.func_181540_al())
            {
                mc.displayGuiScreen(null);
            }
        }
        else
        {
            drawDefaultBackground();
            drawCenteredString(fontRendererObj, field_146591_a, width / 2, 70, 16777215);
            drawCenteredString(fontRendererObj, field_146589_f + " " + progress + "%", width / 2, 90, 16777215);
            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }
}
