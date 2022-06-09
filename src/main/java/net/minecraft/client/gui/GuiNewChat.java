package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wtf.saturn.event.AddChatMessageEvent;

import static wtf.saturn.launch.Launcher.BUS;

public class GuiNewChat extends Gui
{
    private static final Logger logger = LogManager.getLogger();
    private final Minecraft mc;
    private final List<String> sentMessages = Lists.newArrayList();
    private final List<ChatLine> chatLines = Lists.newArrayList();
    private final List<ChatLine> field_146253_i = Lists.newArrayList();
    private int scrollPos;
    private boolean isScrolled;

    public GuiNewChat(Minecraft mcIn)
    {
        mc = mcIn;
    }

    public void drawChat(int p_146230_1_)
    {
        if (mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN)
        {
            int i = getLineCount();
            boolean flag = false;
            int j = 0;
            int k = field_146253_i.size();
            float f = mc.gameSettings.chatOpacity * 0.9F + 0.1F;

            if (k > 0)
            {
                if (getChatOpen())
                {
                    flag = true;
                }

                float f1 = getChatScale();
                int l = MathHelper.ceiling_float_int((float) getChatWidth() / f1);
                GlStateManager.pushMatrix();
                GlStateManager.translate(2.0F, 20.0F, 0.0F);
                GlStateManager.scale(f1, f1, 1.0F);

                for (int i1 = 0; i1 + scrollPos < field_146253_i.size() && i1 < i; ++i1)
                {
                    ChatLine chatline = field_146253_i.get(i1 + scrollPos);

                    if (chatline != null)
                    {
                        int j1 = p_146230_1_ - chatline.getUpdatedCounter();

                        if (j1 < 200 || flag)
                        {
                            double d0 = (double)j1 / 200.0D;
                            d0 = 1.0D - d0;
                            d0 = d0 * 10.0D;
                            d0 = MathHelper.clamp_double(d0, 0.0D, 1.0D);
                            d0 = d0 * d0;
                            int l1 = (int)(255.0D * d0);

                            if (flag)
                            {
                                l1 = 255;
                            }

                            l1 = (int)((float)l1 * f);
                            ++j;

                            if (l1 > 3)
                            {
                                int i2 = 0;
                                int j2 = -i1 * 9;
                                Gui.drawRect(i2, j2 - 9, i2 + l + 4, j2, l1 / 2 << 24);
                                String s = chatline.getChatComponent().getFormattedText();
                                GlStateManager.enableBlend();
                                mc.fontRendererObj.drawStringWithShadow(s, (float)i2, (float)(j2 - 8), 16777215 + (l1 << 24));
                                GlStateManager.disableAlpha();
                                GlStateManager.disableBlend();
                            }
                        }
                    }
                }

                if (flag)
                {
                    int k2 = mc.fontRendererObj.FONT_HEIGHT;
                    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                    int l2 = k * k2 + k;
                    int i3 = j * k2 + j;
                    int j3 = scrollPos * i3 / k;
                    int k1 = i3 * i3 / l2;

                    if (l2 != i3)
                    {
                        int k3 = j3 > 0 ? 170 : 96;
                        int l3 = isScrolled ? 13382451 : 3355562;
                        Gui.drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
                        Gui.drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
                    }
                }

                GlStateManager.popMatrix();
            }
        }
    }

    /**
     * Clears the chat.
     */
    public void clearChatMessages()
    {
        field_146253_i.clear();
        chatLines.clear();
        sentMessages.clear();
    }

    public void printChatMessage(IChatComponent p_146227_1_)
    {
        printChatMessageWithOptionalDeletion(p_146227_1_, 0);
    }

    /**
     * prints the ChatComponent to Chat. If the ID is not 0, deletes an existing Chat Line of that ID from the GUI
     */
    public void printChatMessageWithOptionalDeletion(IChatComponent p_146234_1_, int p_146234_2_)
    {
        if (BUS.post(new AddChatMessageEvent(p_146234_1_))) {
            return;
        }

        setChatLine(p_146234_1_, p_146234_2_, mc.ingameGUI.getUpdateCounter(), false);
        GuiNewChat.logger.info("[CHAT] " + p_146234_1_.getUnformattedText());
    }

    private void setChatLine(IChatComponent p_146237_1_, int p_146237_2_, int p_146237_3_, boolean p_146237_4_)
    {
        if (p_146237_2_ != 0)
        {
            deleteChatLine(p_146237_2_);
        }

        int i = MathHelper.floor_float((float) getChatWidth() / getChatScale());
        List<IChatComponent> list = GuiUtilRenderComponents.func_178908_a(p_146237_1_, i, mc.fontRendererObj, false, false);
        boolean flag = getChatOpen();

        for (IChatComponent ichatcomponent : list)
        {
            if (flag && scrollPos > 0)
            {
                isScrolled = true;
                scroll(1);
            }

            field_146253_i.add(0, new ChatLine(p_146237_3_, ichatcomponent, p_146237_2_));
        }

        while (field_146253_i.size() > 100)
        {
            field_146253_i.remove(field_146253_i.size() - 1);
        }

        if (!p_146237_4_)
        {
            chatLines.add(0, new ChatLine(p_146237_3_, p_146237_1_, p_146237_2_));

            while (chatLines.size() > 100)
            {
                chatLines.remove(chatLines.size() - 1);
            }
        }
    }

    public void refreshChat()
    {
        field_146253_i.clear();
        resetScroll();

        for (int i = chatLines.size() - 1; i >= 0; --i)
        {
            ChatLine chatline = chatLines.get(i);
            setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
        }
    }

    public List<String> getSentMessages()
    {
        return sentMessages;
    }

    /**
     * Adds this string to the list of sent messages, for recall using the up/down arrow keys
     */
    public void addToSentMessages(String p_146239_1_)
    {
        if (sentMessages.isEmpty() || !sentMessages.get(sentMessages.size() - 1).equals(p_146239_1_))
        {
            sentMessages.add(p_146239_1_);
        }
    }

    /**
     * Resets the chat scroll (executed when the GUI is closed, among others)
     */
    public void resetScroll()
    {
        scrollPos = 0;
        isScrolled = false;
    }

    /**
     * Scrolls the chat by the given number of lines.
     */
    public void scroll(int p_146229_1_)
    {
        scrollPos += p_146229_1_;
        int i = field_146253_i.size();

        if (scrollPos > i - getLineCount())
        {
            scrollPos = i - getLineCount();
        }

        if (scrollPos <= 0)
        {
            scrollPos = 0;
            isScrolled = false;
        }
    }

    /**
     * Gets the chat component under the mouse
     */
    public IChatComponent getChatComponent(int p_146236_1_, int p_146236_2_)
    {
        if (!getChatOpen())
        {
            return null;
        }
        else
        {
            ScaledResolution scaledresolution = new ScaledResolution(mc);
            int i = scaledresolution.getScaleFactor();
            float f = getChatScale();
            int j = p_146236_1_ / i - 3;
            int k = p_146236_2_ / i - 27;
            j = MathHelper.floor_float((float)j / f);
            k = MathHelper.floor_float((float)k / f);

            if (j >= 0 && k >= 0)
            {
                int l = Math.min(getLineCount(), field_146253_i.size());

                if (j <= MathHelper.floor_float((float) getChatWidth() / getChatScale()) && k < mc.fontRendererObj.FONT_HEIGHT * l + l)
                {
                    int i1 = k / mc.fontRendererObj.FONT_HEIGHT + scrollPos;

                    if (i1 >= 0 && i1 < field_146253_i.size())
                    {
                        ChatLine chatline = field_146253_i.get(i1);
                        int j1 = 0;

                        for (IChatComponent ichatcomponent : chatline.getChatComponent())
                        {
                            if (ichatcomponent instanceof ChatComponentText)
                            {
                                j1 += mc.fontRendererObj.getStringWidth(GuiUtilRenderComponents.func_178909_a(((ChatComponentText)ichatcomponent).getChatComponentText_TextValue(), false));

                                if (j1 > j)
                                {
                                    return ichatcomponent;
                                }
                            }
                        }
                    }

                    return null;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * Returns true if the chat GUI is open
     */
    public boolean getChatOpen()
    {
        return mc.currentScreen instanceof GuiChat;
    }

    /**
     * finds and deletes a Chat line by ID
     */
    public void deleteChatLine(int p_146242_1_)
    {
        Iterator<ChatLine> iterator = field_146253_i.iterator();

        while (iterator.hasNext())
        {
            ChatLine chatline = iterator.next();

            if (chatline.getChatLineID() == p_146242_1_)
            {
                iterator.remove();
            }
        }

        iterator = chatLines.iterator();

        while (iterator.hasNext())
        {
            ChatLine chatline1 = iterator.next();

            if (chatline1.getChatLineID() == p_146242_1_)
            {
                iterator.remove();
                break;
            }
        }
    }

    public int getChatWidth()
    {
        return GuiNewChat.calculateChatboxWidth(mc.gameSettings.chatWidth);
    }

    public int getChatHeight()
    {
        return GuiNewChat.calculateChatboxHeight(getChatOpen() ? mc.gameSettings.chatHeightFocused : mc.gameSettings.chatHeightUnfocused);
    }

    /**
     * Returns the chatscale from mc.gameSettings.chatScale
     */
    public float getChatScale()
    {
        return mc.gameSettings.chatScale;
    }

    public static int calculateChatboxWidth(float p_146233_0_)
    {
        int i = 320;
        int j = 40;
        return MathHelper.floor_float(p_146233_0_ * (float)(i - j) + (float)j);
    }

    public static int calculateChatboxHeight(float p_146243_0_)
    {
        int i = 180;
        int j = 20;
        return MathHelper.floor_float(p_146243_0_ * (float)(i - j) + (float)j);
    }

    public int getLineCount()
    {
        return getChatHeight() / 9;
    }
}
