package net.minecraft.util;

public class ChatComponentText extends ChatComponentStyle
{
    private final String text;

    public ChatComponentText(String msg)
    {
        text = msg;
    }

    /**
     * Gets the text value of this ChatComponentText.  TODO: what are getUnformattedText and getUnformattedTextForChat
     * missing that made someone decide to create a third equivalent method that only ChatComponentText can implement?
     */
    public String getChatComponentText_TextValue()
    {
        return text;
    }

    /**
     * Gets the text of this component, without any special formatting codes added, for chat.  TODO: why is this two
     * different methods?
     */
    public String getUnformattedTextForChat()
    {
        return text;
    }

    /**
     * Creates a copy of this component.  Almost a deep copy, except the style is shallow-copied.
     */
    public ChatComponentText createCopy()
    {
        ChatComponentText chatcomponenttext = new ChatComponentText(text);
        chatcomponenttext.setChatStyle(getChatStyle().createShallowCopy());

        for (IChatComponent ichatcomponent : getSiblings())
        {
            chatcomponenttext.appendSibling(ichatcomponent.createCopy());
        }

        return chatcomponenttext;
    }

    public boolean equals(Object p_equals_1_)
    {
        if (this == p_equals_1_)
        {
            return true;
        }
        else if (!(p_equals_1_ instanceof ChatComponentText))
        {
            return false;
        }
        else
        {
            ChatComponentText chatcomponenttext = (ChatComponentText)p_equals_1_;
            return text.equals(chatcomponenttext.getChatComponentText_TextValue()) && super.equals(p_equals_1_);
        }
    }

    public String toString()
    {
        return "TextComponent{text='" + text + '\'' + ", siblings=" + siblings + ", style=" + getChatStyle() + '}';
    }
}
