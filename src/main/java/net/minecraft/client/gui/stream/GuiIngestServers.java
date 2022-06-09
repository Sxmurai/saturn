package net.minecraft.client.gui.stream;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.stream.IngestServerTester;
import net.minecraft.util.EnumChatFormatting;
import tv.twitch.broadcast.IngestServer;

public class GuiIngestServers extends GuiScreen
{
    private final GuiScreen field_152309_a;
    private String field_152310_f;
    private GuiIngestServers.ServerList field_152311_g;

    public GuiIngestServers(GuiScreen p_i46312_1_)
    {
        field_152309_a = p_i46312_1_;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        field_152310_f = I18n.format("options.stream.ingest.title");
        field_152311_g = new GuiIngestServers.ServerList(mc);

        if (!mc.getTwitchStream().func_152908_z())
        {
            mc.getTwitchStream().func_152909_x();
        }

        buttonList.add(new GuiButton(1, width / 2 - 155, height - 24 - 6, 150, 20, I18n.format("gui.done")));
        buttonList.add(new GuiButton(2, width / 2 + 5, height - 24 - 6, 150, 20, I18n.format("options.stream.ingest.reset")));
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        field_152311_g.handleMouseInput();
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        if (mc.getTwitchStream().func_152908_z())
        {
            mc.getTwitchStream().func_152932_y().func_153039_l();
        }
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
                mc.displayGuiScreen(field_152309_a);
            }
            else
            {
                mc.gameSettings.streamPreferredServer = "";
                mc.gameSettings.saveOptions();
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        field_152311_g.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRendererObj, field_152310_f, width / 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class ServerList extends GuiSlot
    {
        public ServerList(Minecraft mcIn)
        {
            super(mcIn, GuiIngestServers.this.width, GuiIngestServers.this.height, 32, GuiIngestServers.this.height - 35, (int)((double)mcIn.fontRendererObj.FONT_HEIGHT * 3.5D));
            setShowSelectionBox(false);
        }

        protected int getSize()
        {
            return mc.getTwitchStream().func_152925_v().length;
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
            mc.gameSettings.streamPreferredServer = mc.getTwitchStream().func_152925_v()[slotIndex].serverUrl;
            mc.gameSettings.saveOptions();
        }

        protected boolean isSelected(int slotIndex)
        {
            return mc.getTwitchStream().func_152925_v()[slotIndex].serverUrl.equals(mc.gameSettings.streamPreferredServer);
        }

        protected void drawBackground()
        {
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn)
        {
            IngestServer ingestserver = mc.getTwitchStream().func_152925_v()[entryID];
            String s = ingestserver.serverUrl.replaceAll("\\{stream_key\\}", "");
            String s1 = (int)ingestserver.bitrateKbps + " kbps";
            String s2 = null;
            IngestServerTester ingestservertester = mc.getTwitchStream().func_152932_y();

            if (ingestservertester != null)
            {
                if (ingestserver == ingestservertester.func_153040_c())
                {
                    s = EnumChatFormatting.GREEN + s;
                    s1 = (int)(ingestservertester.func_153030_h() * 100.0F) + "%";
                }
                else if (entryID < ingestservertester.func_153028_p())
                {
                    if (ingestserver.bitrateKbps == 0.0F)
                    {
                        s1 = EnumChatFormatting.RED + "Down!";
                    }
                }
                else
                {
                    s1 = EnumChatFormatting.OBFUSCATED + "1234" + EnumChatFormatting.RESET + " kbps";
                }
            }
            else if (ingestserver.bitrateKbps == 0.0F)
            {
                s1 = EnumChatFormatting.RED + "Down!";
            }

            p_180791_2_ = p_180791_2_ - 15;

            if (isSelected(entryID))
            {
                s2 = EnumChatFormatting.BLUE + "(Preferred)";
            }
            else if (ingestserver.defaultServer)
            {
                s2 = EnumChatFormatting.GREEN + "(Default)";
            }

            drawString(fontRendererObj, ingestserver.serverName, p_180791_2_ + 2, p_180791_3_ + 5, 16777215);
            drawString(fontRendererObj, s, p_180791_2_ + 2, p_180791_3_ + fontRendererObj.FONT_HEIGHT + 5 + 3, 3158064);
            drawString(fontRendererObj, s1, getScrollBarX() - 5 - fontRendererObj.getStringWidth(s1), p_180791_3_ + 5, 8421504);

            if (s2 != null)
            {
                drawString(fontRendererObj, s2, getScrollBarX() - 5 - fontRendererObj.getStringWidth(s2), p_180791_3_ + 5 + 3 + fontRendererObj.FONT_HEIGHT, 8421504);
            }
        }

        protected int getScrollBarX()
        {
            return super.getScrollBarX() + 15;
        }
    }
}
