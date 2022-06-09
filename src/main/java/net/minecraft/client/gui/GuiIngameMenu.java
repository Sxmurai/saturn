package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.realms.RealmsBridge;

public class GuiIngameMenu extends GuiScreen
{
    private int field_146445_a;
    private int field_146444_f;

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        field_146445_a = 0;
        buttonList.clear();
        int i = -16;
        int j = 98;
        buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 120 + i, I18n.format("menu.returnToMenu")));

        if (!mc.isIntegratedServerRunning())
        {
            buttonList.get(0).displayString = I18n.format("menu.disconnect");
        }

        buttonList.add(new GuiButton(4, width / 2 - 100, height / 4 + 24 + i, I18n.format("menu.returnToGame")));
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 96 + i, 98, 20, I18n.format("menu.options")));
        GuiButton guibutton;
        buttonList.add(guibutton = new GuiButton(7, width / 2 + 2, height / 4 + 96 + i, 98, 20, I18n.format("menu.shareToLan")));
        buttonList.add(new GuiButton(5, width / 2 - 100, height / 4 + 48 + i, 98, 20, I18n.format("gui.achievements")));
        buttonList.add(new GuiButton(6, width / 2 + 2, height / 4 + 48 + i, 98, 20, I18n.format("gui.stats")));
        guibutton.enabled = mc.isSingleplayer() && !mc.getIntegratedServer().getPublic();
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        switch (button.id)
        {
            case 0:
                mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                break;

            case 1:
                boolean flag = mc.isIntegratedServerRunning();
                boolean flag1 = mc.func_181540_al();
                button.enabled = false;
                mc.theWorld.sendQuittingDisconnectingPacket();
                mc.loadWorld(null);

                if (flag)
                {
                    mc.displayGuiScreen(new GuiMainMenu());
                }
                else if (flag1)
                {
                    RealmsBridge realmsbridge = new RealmsBridge();
                    realmsbridge.switchToRealms(new GuiMainMenu());
                }
                else
                {
                    mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
                }

            case 2:
            case 3:
            default:
                break;

            case 4:
                mc.displayGuiScreen(null);
                mc.setIngameFocus();
                break;

            case 5:
                mc.displayGuiScreen(new GuiAchievements(this, mc.thePlayer.getStatFileWriter()));
                break;

            case 6:
                mc.displayGuiScreen(new GuiStats(this, mc.thePlayer.getStatFileWriter()));
                break;

            case 7:
                mc.displayGuiScreen(new GuiShareToLan(this));
        }
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        super.updateScreen();
        ++field_146444_f;
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("menu.game"), width / 2, 40, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
