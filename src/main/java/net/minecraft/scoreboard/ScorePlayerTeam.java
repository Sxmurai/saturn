package net.minecraft.scoreboard;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import net.minecraft.util.EnumChatFormatting;

public class ScorePlayerTeam extends Team
{
    private final Scoreboard theScoreboard;
    private final String registeredName;
    private final Set<String> membershipSet = Sets.newHashSet();
    private String teamNameSPT;
    private String namePrefixSPT = "";
    private String colorSuffix = "";
    private boolean allowFriendlyFire = true;
    private boolean canSeeFriendlyInvisibles = true;
    private Team.EnumVisible nameTagVisibility = Team.EnumVisible.ALWAYS;
    private Team.EnumVisible deathMessageVisibility = Team.EnumVisible.ALWAYS;
    private EnumChatFormatting chatFormat = EnumChatFormatting.RESET;

    public ScorePlayerTeam(Scoreboard theScoreboardIn, String name)
    {
        theScoreboard = theScoreboardIn;
        registeredName = name;
        teamNameSPT = name;
    }

    /**
     * Retrieve the name by which this team is registered in the scoreboard
     */
    public String getRegisteredName()
    {
        return registeredName;
    }

    public String getTeamName()
    {
        return teamNameSPT;
    }

    public void setTeamName(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Name cannot be null");
        }
        else
        {
            teamNameSPT = name;
            theScoreboard.sendTeamUpdate(this);
        }
    }

    public Collection<String> getMembershipCollection()
    {
        return membershipSet;
    }

    /**
     * Returns the color prefix for the player's team name
     */
    public String getColorPrefix()
    {
        return namePrefixSPT;
    }

    public void setNamePrefix(String prefix)
    {
        if (prefix == null)
        {
            throw new IllegalArgumentException("Prefix cannot be null");
        }
        else
        {
            namePrefixSPT = prefix;
            theScoreboard.sendTeamUpdate(this);
        }
    }

    /**
     * Returns the color suffix for the player's team name
     */
    public String getColorSuffix()
    {
        return colorSuffix;
    }

    public void setNameSuffix(String suffix)
    {
        colorSuffix = suffix;
        theScoreboard.sendTeamUpdate(this);
    }

    public String formatString(String input)
    {
        return getColorPrefix() + input + getColorSuffix();
    }

    /**
     * Returns the player name including the color prefixes and suffixes
     */
    public static String formatPlayerName(Team p_96667_0_, String p_96667_1_)
    {
        return p_96667_0_ == null ? p_96667_1_ : p_96667_0_.formatString(p_96667_1_);
    }

    public boolean getAllowFriendlyFire()
    {
        return allowFriendlyFire;
    }

    public void setAllowFriendlyFire(boolean friendlyFire)
    {
        allowFriendlyFire = friendlyFire;
        theScoreboard.sendTeamUpdate(this);
    }

    public boolean getSeeFriendlyInvisiblesEnabled()
    {
        return canSeeFriendlyInvisibles;
    }

    public void setSeeFriendlyInvisiblesEnabled(boolean friendlyInvisibles)
    {
        canSeeFriendlyInvisibles = friendlyInvisibles;
        theScoreboard.sendTeamUpdate(this);
    }

    public Team.EnumVisible getNameTagVisibility()
    {
        return nameTagVisibility;
    }

    public Team.EnumVisible getDeathMessageVisibility()
    {
        return deathMessageVisibility;
    }

    public void setNameTagVisibility(Team.EnumVisible p_178772_1_)
    {
        nameTagVisibility = p_178772_1_;
        theScoreboard.sendTeamUpdate(this);
    }

    public void setDeathMessageVisibility(Team.EnumVisible p_178773_1_)
    {
        deathMessageVisibility = p_178773_1_;
        theScoreboard.sendTeamUpdate(this);
    }

    public int func_98299_i()
    {
        int i = 0;

        if (getAllowFriendlyFire())
        {
            i |= 1;
        }

        if (getSeeFriendlyInvisiblesEnabled())
        {
            i |= 2;
        }

        return i;
    }

    public void func_98298_a(int p_98298_1_)
    {
        setAllowFriendlyFire((p_98298_1_ & 1) > 0);
        setSeeFriendlyInvisiblesEnabled((p_98298_1_ & 2) > 0);
    }

    public void setChatFormat(EnumChatFormatting p_178774_1_)
    {
        chatFormat = p_178774_1_;
    }

    public EnumChatFormatting getChatFormat()
    {
        return chatFormat;
    }
}
