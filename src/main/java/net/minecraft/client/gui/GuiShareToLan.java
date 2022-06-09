package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldSettings;

public class GuiShareToLan extends GuiScreen
{
    private final GuiScreen field_146598_a;
    private GuiButton field_146596_f;
    private GuiButton field_146597_g;
    private String field_146599_h = "survival";
    private boolean field_146600_i;

    public GuiShareToLan(GuiScreen p_i1055_1_)
    {
        field_146598_a = p_i1055_1_;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        buttonList.clear();
        buttonList.add(new GuiButton(101, width / 2 - 155, height - 28, 150, 20, I18n.format("lanServer.start")));
        buttonList.add(new GuiButton(102, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
        buttonList.add(field_146597_g = new GuiButton(104, width / 2 - 155, 100, 150, 20, I18n.format("selectWorld.gameMode")));
        buttonList.add(field_146596_f = new GuiButton(103, width / 2 + 5, 100, 150, 20, I18n.format("selectWorld.allowCommands")));
        func_146595_g();
    }

    private void func_146595_g()
    {
        field_146597_g.displayString = I18n.format("selectWorld.gameMode") + " " + I18n.format("selectWorld.gameMode." + field_146599_h);
        field_146596_f.displayString = I18n.format("selectWorld.allowCommands") + " ";

        if (field_146600_i)
        {
            field_146596_f.displayString = field_146596_f.displayString + I18n.format("options.on");
        }
        else
        {
            field_146596_f.displayString = field_146596_f.displayString + I18n.format("options.off");
        }
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 102)
        {
            mc.displayGuiScreen(field_146598_a);
        }
        else if (button.id == 104)
        {
            if (field_146599_h.equals("spectator"))
            {
                field_146599_h = "creative";
            }
            else if (field_146599_h.equals("creative"))
            {
                field_146599_h = "adventure";
            }
            else if (field_146599_h.equals("adventure"))
            {
                field_146599_h = "survival";
            }
            else
            {
                field_146599_h = "spectator";
            }

            func_146595_g();
        }
        else if (button.id == 103)
        {
            field_146600_i = !field_146600_i;
            func_146595_g();
        }
        else if (button.id == 101)
        {
            mc.displayGuiScreen(null);
            String s = mc.getIntegratedServer().shareToLAN(WorldSettings.GameType.getByName(field_146599_h), field_146600_i);
            IChatComponent ichatcomponent;

            if (s != null)
            {
                ichatcomponent = new ChatComponentTranslation("commands.publish.started", s);
            }
            else
            {
                ichatcomponent = new ChatComponentText("commands.publish.failed");
            }

            mc.ingameGUI.getChatGUI().printChatMessage(ichatcomponent);
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("lanServer.title"), width / 2, 50, 16777215);
        drawCenteredString(fontRendererObj, I18n.format("lanServer.otherPlayers"), width / 2, 82, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
