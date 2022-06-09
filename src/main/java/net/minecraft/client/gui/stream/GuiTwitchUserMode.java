package net.minecraft.client.gui.stream;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.stream.IStream;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import tv.twitch.chat.ChatUserInfo;
import tv.twitch.chat.ChatUserMode;
import tv.twitch.chat.ChatUserSubscription;

public class GuiTwitchUserMode extends GuiScreen
{
    private static final EnumChatFormatting field_152331_a = EnumChatFormatting.DARK_GREEN;
    private static final EnumChatFormatting field_152335_f = EnumChatFormatting.RED;
    private static final EnumChatFormatting field_152336_g = EnumChatFormatting.DARK_PURPLE;
    private final ChatUserInfo field_152337_h;
    private final IChatComponent field_152338_i;
    private final List<IChatComponent> field_152332_r = Lists.newArrayList();
    private final IStream stream;
    private int field_152334_t;

    public GuiTwitchUserMode(IStream streamIn, ChatUserInfo p_i1064_2_)
    {
        stream = streamIn;
        field_152337_h = p_i1064_2_;
        field_152338_i = new ChatComponentText(p_i1064_2_.displayName);
        field_152332_r.addAll(GuiTwitchUserMode.func_152328_a(p_i1064_2_.modes, p_i1064_2_.subscriptions, streamIn));
    }

    public static List<IChatComponent> func_152328_a(Set<ChatUserMode> p_152328_0_, Set<ChatUserSubscription> p_152328_1_, IStream p_152328_2_)
    {
        String s = p_152328_2_ == null ? null : p_152328_2_.func_152921_C();
        boolean flag = p_152328_2_ != null && p_152328_2_.func_152927_B();
        List<IChatComponent> list = Lists.newArrayList();

        for (ChatUserMode chatusermode : p_152328_0_)
        {
            IChatComponent ichatcomponent = GuiTwitchUserMode.func_152329_a(chatusermode, s, flag);

            if (ichatcomponent != null)
            {
                IChatComponent ichatcomponent1 = new ChatComponentText("- ");
                ichatcomponent1.appendSibling(ichatcomponent);
                list.add(ichatcomponent1);
            }
        }

        for (ChatUserSubscription chatusersubscription : p_152328_1_)
        {
            IChatComponent ichatcomponent2 = GuiTwitchUserMode.func_152330_a(chatusersubscription, s, flag);

            if (ichatcomponent2 != null)
            {
                IChatComponent ichatcomponent3 = new ChatComponentText("- ");
                ichatcomponent3.appendSibling(ichatcomponent2);
                list.add(ichatcomponent3);
            }
        }

        return list;
    }

    public static IChatComponent func_152330_a(ChatUserSubscription p_152330_0_, String p_152330_1_, boolean p_152330_2_)
    {
        IChatComponent ichatcomponent = null;

        if (p_152330_0_ == ChatUserSubscription.TTV_CHAT_USERSUB_SUBSCRIBER)
        {
            if (p_152330_1_ == null)
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.subscription.subscriber");
            }
            else if (p_152330_2_)
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.subscription.subscriber.self");
            }
            else
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.subscription.subscriber.other", p_152330_1_);
            }

            ichatcomponent.getChatStyle().setColor(GuiTwitchUserMode.field_152331_a);
        }
        else if (p_152330_0_ == ChatUserSubscription.TTV_CHAT_USERSUB_TURBO)
        {
            ichatcomponent = new ChatComponentTranslation("stream.user.subscription.turbo");
            ichatcomponent.getChatStyle().setColor(GuiTwitchUserMode.field_152336_g);
        }

        return ichatcomponent;
    }

    public static IChatComponent func_152329_a(ChatUserMode p_152329_0_, String p_152329_1_, boolean p_152329_2_)
    {
        IChatComponent ichatcomponent = null;

        if (p_152329_0_ == ChatUserMode.TTV_CHAT_USERMODE_ADMINSTRATOR)
        {
            ichatcomponent = new ChatComponentTranslation("stream.user.mode.administrator");
            ichatcomponent.getChatStyle().setColor(GuiTwitchUserMode.field_152336_g);
        }
        else if (p_152329_0_ == ChatUserMode.TTV_CHAT_USERMODE_BANNED)
        {
            if (p_152329_1_ == null)
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.banned");
            }
            else if (p_152329_2_)
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.banned.self");
            }
            else
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.banned.other", p_152329_1_);
            }

            ichatcomponent.getChatStyle().setColor(GuiTwitchUserMode.field_152335_f);
        }
        else if (p_152329_0_ == ChatUserMode.TTV_CHAT_USERMODE_BROADCASTER)
        {
            if (p_152329_1_ == null)
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.broadcaster");
            }
            else if (p_152329_2_)
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.broadcaster.self");
            }
            else
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.broadcaster.other");
            }

            ichatcomponent.getChatStyle().setColor(GuiTwitchUserMode.field_152331_a);
        }
        else if (p_152329_0_ == ChatUserMode.TTV_CHAT_USERMODE_MODERATOR)
        {
            if (p_152329_1_ == null)
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.moderator");
            }
            else if (p_152329_2_)
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.moderator.self");
            }
            else
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.moderator.other", p_152329_1_);
            }

            ichatcomponent.getChatStyle().setColor(GuiTwitchUserMode.field_152331_a);
        }
        else if (p_152329_0_ == ChatUserMode.TTV_CHAT_USERMODE_STAFF)
        {
            ichatcomponent = new ChatComponentTranslation("stream.user.mode.staff");
            ichatcomponent.getChatStyle().setColor(GuiTwitchUserMode.field_152336_g);
        }

        return ichatcomponent;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        int i = width / 3;
        int j = i - 130;
        buttonList.add(new GuiButton(1, i * 0 + j / 2, height - 70, 130, 20, I18n.format("stream.userinfo.timeout")));
        buttonList.add(new GuiButton(0, i * 1 + j / 2, height - 70, 130, 20, I18n.format("stream.userinfo.ban")));
        buttonList.add(new GuiButton(2, i * 2 + j / 2, height - 70, 130, 20, I18n.format("stream.userinfo.mod")));
        buttonList.add(new GuiButton(5, i * 0 + j / 2, height - 45, 130, 20, I18n.format("gui.cancel")));
        buttonList.add(new GuiButton(3, i * 1 + j / 2, height - 45, 130, 20, I18n.format("stream.userinfo.unban")));
        buttonList.add(new GuiButton(4, i * 2 + j / 2, height - 45, 130, 20, I18n.format("stream.userinfo.unmod")));
        int k = 0;

        for (IChatComponent ichatcomponent : field_152332_r)
        {
            k = Math.max(k, fontRendererObj.getStringWidth(ichatcomponent.getFormattedText()));
        }

        field_152334_t = width / 2 - k / 2;
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 0)
            {
                stream.func_152917_b("/ban " + field_152337_h.displayName);
            }
            else if (button.id == 3)
            {
                stream.func_152917_b("/unban " + field_152337_h.displayName);
            }
            else if (button.id == 2)
            {
                stream.func_152917_b("/mod " + field_152337_h.displayName);
            }
            else if (button.id == 4)
            {
                stream.func_152917_b("/unmod " + field_152337_h.displayName);
            }
            else if (button.id == 1)
            {
                stream.func_152917_b("/timeout " + field_152337_h.displayName);
            }

            mc.displayGuiScreen(null);
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, field_152338_i.getUnformattedText(), width / 2, 70, 16777215);
        int i = 80;

        for (IChatComponent ichatcomponent : field_152332_r)
        {
            drawString(fontRendererObj, ichatcomponent.getFormattedText(), field_152334_t, i, 16777215);
            i += fontRendererObj.FONT_HEIGHT;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
