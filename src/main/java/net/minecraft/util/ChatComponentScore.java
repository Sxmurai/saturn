package net.minecraft.util;

import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;

public class ChatComponentScore extends ChatComponentStyle
{
    private final String name;
    private final String objective;

    /** The value displayed instead of the real score (may be null) */
    private String value = "";

    public ChatComponentScore(String nameIn, String objectiveIn)
    {
        name = nameIn;
        objective = objectiveIn;
    }

    public String getName()
    {
        return name;
    }

    public String getObjective()
    {
        return objective;
    }

    /**
     * Sets the value displayed instead of the real score.
     */
    public void setValue(String valueIn)
    {
        value = valueIn;
    }

    /**
     * Gets the text of this component, without any special formatting codes added, for chat.  TODO: why is this two
     * different methods?
     */
    public String getUnformattedTextForChat()
    {
        MinecraftServer minecraftserver = MinecraftServer.getServer();

        if (minecraftserver != null && minecraftserver.isAnvilFileSet() && StringUtils.isNullOrEmpty(value))
        {
            Scoreboard scoreboard = minecraftserver.worldServerForDimension(0).getScoreboard();
            ScoreObjective scoreobjective = scoreboard.getObjective(objective);

            if (scoreboard.entityHasObjective(name, scoreobjective))
            {
                Score score = scoreboard.getValueFromObjective(name, scoreobjective);
                setValue(String.format("%d", Integer.valueOf(score.getScorePoints())));
            }
            else
            {
                value = "";
            }
        }

        return value;
    }

    /**
     * Creates a copy of this component.  Almost a deep copy, except the style is shallow-copied.
     */
    public ChatComponentScore createCopy()
    {
        ChatComponentScore chatcomponentscore = new ChatComponentScore(name, objective);
        chatcomponentscore.setValue(value);
        chatcomponentscore.setChatStyle(getChatStyle().createShallowCopy());

        for (IChatComponent ichatcomponent : getSiblings())
        {
            chatcomponentscore.appendSibling(ichatcomponent.createCopy());
        }

        return chatcomponentscore;
    }

    public boolean equals(Object p_equals_1_)
    {
        if (this == p_equals_1_)
        {
            return true;
        }
        else if (!(p_equals_1_ instanceof ChatComponentScore))
        {
            return false;
        }
        else
        {
            ChatComponentScore chatcomponentscore = (ChatComponentScore)p_equals_1_;
            return name.equals(chatcomponentscore.name) && objective.equals(chatcomponentscore.objective) && super.equals(p_equals_1_);
        }
    }

    public String toString()
    {
        return "ScoreComponent{name='" + name + '\'' + "objective='" + objective + '\'' + ", siblings=" + siblings + ", style=" + getChatStyle() + '}';
    }
}
