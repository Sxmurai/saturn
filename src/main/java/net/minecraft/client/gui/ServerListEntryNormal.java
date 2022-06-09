package net.minecraft.client.gui;

import com.google.common.base.Charsets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import java.awt.image.BufferedImage;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerListEntryNormal implements GuiListExtended.IGuiListEntry
{
    private static final Logger logger = LogManager.getLogger();
    private static final ThreadPoolExecutor field_148302_b = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());
    private static final ResourceLocation UNKNOWN_SERVER = new ResourceLocation("textures/misc/unknown_server.png");
    private static final ResourceLocation SERVER_SELECTION_BUTTONS = new ResourceLocation("textures/gui/server_selection.png");
    private final GuiMultiplayer field_148303_c;
    private final Minecraft mc;
    private final ServerData field_148301_e;
    private final ResourceLocation field_148306_i;
    private String field_148299_g;
    private DynamicTexture field_148305_h;
    private long field_148298_f;

    protected ServerListEntryNormal(GuiMultiplayer p_i45048_1_, ServerData p_i45048_2_)
    {
        field_148303_c = p_i45048_1_;
        field_148301_e = p_i45048_2_;
        mc = Minecraft.getMinecraft();
        field_148306_i = new ResourceLocation("servers/" + p_i45048_2_.serverIP + "/icon");
        field_148305_h = (DynamicTexture) mc.getTextureManager().getTexture(field_148306_i);
    }

    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        if (!field_148301_e.field_78841_f)
        {
            field_148301_e.field_78841_f = true;
            field_148301_e.pingToServer = -2L;
            field_148301_e.serverMOTD = "";
            field_148301_e.populationInfo = "";
            ServerListEntryNormal.field_148302_b.submit(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        field_148303_c.getOldServerPinger().ping(field_148301_e);
                    }
                    catch (UnknownHostException var2)
                    {
                        field_148301_e.pingToServer = -1L;
                        field_148301_e.serverMOTD = EnumChatFormatting.DARK_RED + "Can't resolve hostname";
                    }
                    catch (Exception var3)
                    {
                        field_148301_e.pingToServer = -1L;
                        field_148301_e.serverMOTD = EnumChatFormatting.DARK_RED + "Can't connect to server.";
                    }
                }
            });
        }

        boolean flag = field_148301_e.version > 47;
        boolean flag1 = field_148301_e.version < 47;
        boolean flag2 = flag || flag1;
        mc.fontRendererObj.drawString(field_148301_e.serverName, x + 32 + 3, y + 1, 16777215);
        List<String> list = mc.fontRendererObj.listFormattedStringToWidth(field_148301_e.serverMOTD, listWidth - 32 - 2);

        for (int i = 0; i < Math.min(list.size(), 2); ++i)
        {
            mc.fontRendererObj.drawString(list.get(i), x + 32 + 3, y + 12 + mc.fontRendererObj.FONT_HEIGHT * i, 8421504);
        }

        String s2 = flag2 ? EnumChatFormatting.DARK_RED + field_148301_e.gameVersion : field_148301_e.populationInfo;
        int j = mc.fontRendererObj.getStringWidth(s2);
        mc.fontRendererObj.drawString(s2, x + listWidth - j - 15 - 2, y + 1, 8421504);
        int k = 0;
        String s = null;
        int l;
        String s1;

        if (flag2)
        {
            l = 5;
            s1 = flag ? "Client out of date!" : "Server out of date!";
            s = field_148301_e.playerList;
        }
        else if (field_148301_e.field_78841_f && field_148301_e.pingToServer != -2L)
        {
            if (field_148301_e.pingToServer < 0L)
            {
                l = 5;
            }
            else if (field_148301_e.pingToServer < 150L)
            {
                l = 0;
            }
            else if (field_148301_e.pingToServer < 300L)
            {
                l = 1;
            }
            else if (field_148301_e.pingToServer < 600L)
            {
                l = 2;
            }
            else if (field_148301_e.pingToServer < 1000L)
            {
                l = 3;
            }
            else
            {
                l = 4;
            }

            if (field_148301_e.pingToServer < 0L)
            {
                s1 = "(no connection)";
            }
            else
            {
                s1 = field_148301_e.pingToServer + "ms";
                s = field_148301_e.playerList;
            }
        }
        else
        {
            k = 1;
            l = (int)(Minecraft.getSystemTime() / 100L + (long)(slotIndex * 2) & 7L);

            if (l > 4)
            {
                l = 8 - l;
            }

            s1 = "Pinging...";
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(Gui.icons);
        Gui.drawModalRectWithCustomSizedTexture(x + listWidth - 15, y, (float)(k * 10), (float)(176 + l * 8), 10, 8, 256.0F, 256.0F);

        if (field_148301_e.getBase64EncodedIconData() != null && !field_148301_e.getBase64EncodedIconData().equals(field_148299_g))
        {
            field_148299_g = field_148301_e.getBase64EncodedIconData();
            prepareServerIcon();
            field_148303_c.getServerList().saveServerList();
        }

        if (field_148305_h != null)
        {
            func_178012_a(x, y, field_148306_i);
        }
        else
        {
            func_178012_a(x, y, ServerListEntryNormal.UNKNOWN_SERVER);
        }

        int i1 = mouseX - x;
        int j1 = mouseY - y;

        if (i1 >= listWidth - 15 && i1 <= listWidth - 5 && j1 >= 0 && j1 <= 8)
        {
            field_148303_c.setHoveringText(s1);
        }
        else if (i1 >= listWidth - j - 15 - 2 && i1 <= listWidth - 15 - 2 && j1 >= 0 && j1 <= 8)
        {
            field_148303_c.setHoveringText(s);
        }

        if (mc.gameSettings.touchscreen || isSelected)
        {
            mc.getTextureManager().bindTexture(ServerListEntryNormal.SERVER_SELECTION_BUTTONS);
            Gui.drawRect(x, y, x + 32, y + 32, -1601138544);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int k1 = mouseX - x;
            int l1 = mouseY - y;

            if (func_178013_b())
            {
                if (k1 < 32 && k1 > 16)
                {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                }
                else
                {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }

            if (field_148303_c.func_175392_a(this, slotIndex))
            {
                if (k1 < 16 && l1 < 16)
                {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                }
                else
                {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }

            if (field_148303_c.func_175394_b(this, slotIndex))
            {
                if (k1 < 16 && l1 > 16)
                {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                }
                else
                {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }
        }
    }

    protected void func_178012_a(int p_178012_1_, int p_178012_2_, ResourceLocation p_178012_3_)
    {
        mc.getTextureManager().bindTexture(p_178012_3_);
        GlStateManager.enableBlend();
        Gui.drawModalRectWithCustomSizedTexture(p_178012_1_, p_178012_2_, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
        GlStateManager.disableBlend();
    }

    private boolean func_178013_b()
    {
        return true;
    }

    private void prepareServerIcon()
    {
        if (field_148301_e.getBase64EncodedIconData() == null)
        {
            mc.getTextureManager().deleteTexture(field_148306_i);
            field_148305_h = null;
        }
        else
        {
            ByteBuf bytebuf = Unpooled.copiedBuffer(field_148301_e.getBase64EncodedIconData(), Charsets.UTF_8);
            ByteBuf bytebuf1 = Base64.decode(bytebuf);
            BufferedImage bufferedimage;
            label101:
            {
                try
                {
                    bufferedimage = TextureUtil.readBufferedImage(new ByteBufInputStream(bytebuf1));
                    Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
                    Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
                    break label101;
                }
                catch (Throwable throwable)
                {
                    ServerListEntryNormal.logger.error("Invalid icon for server " + field_148301_e.serverName + " (" + field_148301_e.serverIP + ")", throwable);
                    field_148301_e.setBase64EncodedIconData(null);
                }
                finally
                {
                    bytebuf.release();
                    bytebuf1.release();
                }

                return;
            }

            if (field_148305_h == null)
            {
                field_148305_h = new DynamicTexture(bufferedimage.getWidth(), bufferedimage.getHeight());
                mc.getTextureManager().loadTexture(field_148306_i, field_148305_h);
            }

            bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), field_148305_h.getTextureData(), 0, bufferedimage.getWidth());
            field_148305_h.updateDynamicTexture();
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control.
     */
    public boolean mousePressed(int slotIndex, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_)
    {
        if (p_148278_5_ <= 32)
        {
            if (p_148278_5_ < 32 && p_148278_5_ > 16 && func_178013_b())
            {
                field_148303_c.selectServer(slotIndex);
                field_148303_c.connectToSelected();
                return true;
            }

            if (p_148278_5_ < 16 && p_148278_6_ < 16 && field_148303_c.func_175392_a(this, slotIndex))
            {
                field_148303_c.func_175391_a(this, slotIndex, GuiScreen.isShiftKeyDown());
                return true;
            }

            if (p_148278_5_ < 16 && p_148278_6_ > 16 && field_148303_c.func_175394_b(this, slotIndex))
            {
                field_148303_c.func_175393_b(this, slotIndex, GuiScreen.isShiftKeyDown());
                return true;
            }
        }

        field_148303_c.selectServer(slotIndex);

        if (Minecraft.getSystemTime() - field_148298_f < 250L)
        {
            field_148303_c.connectToSelected();
        }

        field_148298_f = Minecraft.getSystemTime();
        return false;
    }

    public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
    {
    }

    /**
     * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
     */
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
    }

    public ServerData getServerData()
    {
        return field_148301_e;
    }
}
