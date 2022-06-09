package net.minecraft.scoreboard;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldSavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScoreboardSaveData extends WorldSavedData
{
    private static final Logger logger = LogManager.getLogger();
    private Scoreboard theScoreboard;
    private NBTTagCompound delayedInitNbt;

    public ScoreboardSaveData()
    {
        this("scoreboard");
    }

    public ScoreboardSaveData(String name)
    {
        super(name);
    }

    public void setScoreboard(Scoreboard scoreboardIn)
    {
        theScoreboard = scoreboardIn;

        if (delayedInitNbt != null)
        {
            readFromNBT(delayedInitNbt);
        }
    }

    /**
     * reads in data from the NBTTagCompound into this MapDataBase
     */
    public void readFromNBT(NBTTagCompound nbt)
    {
        if (theScoreboard == null)
        {
            delayedInitNbt = nbt;
        }
        else
        {
            readObjectives(nbt.getTagList("Objectives", 10));
            readScores(nbt.getTagList("PlayerScores", 10));

            if (nbt.hasKey("DisplaySlots", 10))
            {
                readDisplayConfig(nbt.getCompoundTag("DisplaySlots"));
            }

            if (nbt.hasKey("Teams", 9))
            {
                readTeams(nbt.getTagList("Teams", 10));
            }
        }
    }

    protected void readTeams(NBTTagList p_96498_1_)
    {
        for (int i = 0; i < p_96498_1_.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = p_96498_1_.getCompoundTagAt(i);
            String s = nbttagcompound.getString("Name");

            if (s.length() > 16)
            {
                s = s.substring(0, 16);
            }

            ScorePlayerTeam scoreplayerteam = theScoreboard.createTeam(s);
            String s1 = nbttagcompound.getString("DisplayName");

            if (s1.length() > 32)
            {
                s1 = s1.substring(0, 32);
            }

            scoreplayerteam.setTeamName(s1);

            if (nbttagcompound.hasKey("TeamColor", 8))
            {
                scoreplayerteam.setChatFormat(EnumChatFormatting.getValueByName(nbttagcompound.getString("TeamColor")));
            }

            scoreplayerteam.setNamePrefix(nbttagcompound.getString("Prefix"));
            scoreplayerteam.setNameSuffix(nbttagcompound.getString("Suffix"));

            if (nbttagcompound.hasKey("AllowFriendlyFire", 99))
            {
                scoreplayerteam.setAllowFriendlyFire(nbttagcompound.getBoolean("AllowFriendlyFire"));
            }

            if (nbttagcompound.hasKey("SeeFriendlyInvisibles", 99))
            {
                scoreplayerteam.setSeeFriendlyInvisiblesEnabled(nbttagcompound.getBoolean("SeeFriendlyInvisibles"));
            }

            if (nbttagcompound.hasKey("NameTagVisibility", 8))
            {
                Team.EnumVisible team$enumvisible = Team.EnumVisible.func_178824_a(nbttagcompound.getString("NameTagVisibility"));

                if (team$enumvisible != null)
                {
                    scoreplayerteam.setNameTagVisibility(team$enumvisible);
                }
            }

            if (nbttagcompound.hasKey("DeathMessageVisibility", 8))
            {
                Team.EnumVisible team$enumvisible1 = Team.EnumVisible.func_178824_a(nbttagcompound.getString("DeathMessageVisibility"));

                if (team$enumvisible1 != null)
                {
                    scoreplayerteam.setDeathMessageVisibility(team$enumvisible1);
                }
            }

            func_96502_a(scoreplayerteam, nbttagcompound.getTagList("Players", 8));
        }
    }

    protected void func_96502_a(ScorePlayerTeam p_96502_1_, NBTTagList p_96502_2_)
    {
        for (int i = 0; i < p_96502_2_.tagCount(); ++i)
        {
            theScoreboard.addPlayerToTeam(p_96502_2_.getStringTagAt(i), p_96502_1_.getRegisteredName());
        }
    }

    protected void readDisplayConfig(NBTTagCompound p_96504_1_)
    {
        for (int i = 0; i < 19; ++i)
        {
            if (p_96504_1_.hasKey("slot_" + i, 8))
            {
                String s = p_96504_1_.getString("slot_" + i);
                ScoreObjective scoreobjective = theScoreboard.getObjective(s);
                theScoreboard.setObjectiveInDisplaySlot(i, scoreobjective);
            }
        }
    }

    protected void readObjectives(NBTTagList nbt)
    {
        for (int i = 0; i < nbt.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbt.getCompoundTagAt(i);
            IScoreObjectiveCriteria iscoreobjectivecriteria = IScoreObjectiveCriteria.INSTANCES.get(nbttagcompound.getString("CriteriaName"));

            if (iscoreobjectivecriteria != null)
            {
                String s = nbttagcompound.getString("Name");

                if (s.length() > 16)
                {
                    s = s.substring(0, 16);
                }

                ScoreObjective scoreobjective = theScoreboard.addScoreObjective(s, iscoreobjectivecriteria);
                scoreobjective.setDisplayName(nbttagcompound.getString("DisplayName"));
                scoreobjective.setRenderType(IScoreObjectiveCriteria.EnumRenderType.func_178795_a(nbttagcompound.getString("RenderType")));
            }
        }
    }

    protected void readScores(NBTTagList nbt)
    {
        for (int i = 0; i < nbt.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbt.getCompoundTagAt(i);
            ScoreObjective scoreobjective = theScoreboard.getObjective(nbttagcompound.getString("Objective"));
            String s = nbttagcompound.getString("Name");

            if (s.length() > 40)
            {
                s = s.substring(0, 40);
            }

            Score score = theScoreboard.getValueFromObjective(s, scoreobjective);
            score.setScorePoints(nbttagcompound.getInteger("Score"));

            if (nbttagcompound.hasKey("Locked"))
            {
                score.setLocked(nbttagcompound.getBoolean("Locked"));
            }
        }
    }

    /**
     * write data to NBTTagCompound from this MapDataBase, similar to Entities and TileEntities
     */
    public void writeToNBT(NBTTagCompound nbt)
    {
        if (theScoreboard == null)
        {
            ScoreboardSaveData.logger.warn("Tried to save scoreboard without having a scoreboard...");
        }
        else
        {
            nbt.setTag("Objectives", objectivesToNbt());
            nbt.setTag("PlayerScores", scoresToNbt());
            nbt.setTag("Teams", func_96496_a());
            func_96497_d(nbt);
        }
    }

    protected NBTTagList func_96496_a()
    {
        NBTTagList nbttaglist = new NBTTagList();

        for (ScorePlayerTeam scoreplayerteam : theScoreboard.getTeams())
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setString("Name", scoreplayerteam.getRegisteredName());
            nbttagcompound.setString("DisplayName", scoreplayerteam.getTeamName());

            if (scoreplayerteam.getChatFormat().getColorIndex() >= 0)
            {
                nbttagcompound.setString("TeamColor", scoreplayerteam.getChatFormat().getFriendlyName());
            }

            nbttagcompound.setString("Prefix", scoreplayerteam.getColorPrefix());
            nbttagcompound.setString("Suffix", scoreplayerteam.getColorSuffix());
            nbttagcompound.setBoolean("AllowFriendlyFire", scoreplayerteam.getAllowFriendlyFire());
            nbttagcompound.setBoolean("SeeFriendlyInvisibles", scoreplayerteam.getSeeFriendlyInvisiblesEnabled());
            nbttagcompound.setString("NameTagVisibility", scoreplayerteam.getNameTagVisibility().field_178830_e);
            nbttagcompound.setString("DeathMessageVisibility", scoreplayerteam.getDeathMessageVisibility().field_178830_e);
            NBTTagList nbttaglist1 = new NBTTagList();

            for (String s : scoreplayerteam.getMembershipCollection())
            {
                nbttaglist1.appendTag(new NBTTagString(s));
            }

            nbttagcompound.setTag("Players", nbttaglist1);
            nbttaglist.appendTag(nbttagcompound);
        }

        return nbttaglist;
    }

    protected void func_96497_d(NBTTagCompound p_96497_1_)
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        boolean flag = false;

        for (int i = 0; i < 19; ++i)
        {
            ScoreObjective scoreobjective = theScoreboard.getObjectiveInDisplaySlot(i);

            if (scoreobjective != null)
            {
                nbttagcompound.setString("slot_" + i, scoreobjective.getName());
                flag = true;
            }
        }

        if (flag)
        {
            p_96497_1_.setTag("DisplaySlots", nbttagcompound);
        }
    }

    protected NBTTagList objectivesToNbt()
    {
        NBTTagList nbttaglist = new NBTTagList();

        for (ScoreObjective scoreobjective : theScoreboard.getScoreObjectives())
        {
            if (scoreobjective.getCriteria() != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setString("Name", scoreobjective.getName());
                nbttagcompound.setString("CriteriaName", scoreobjective.getCriteria().getName());
                nbttagcompound.setString("DisplayName", scoreobjective.getDisplayName());
                nbttagcompound.setString("RenderType", scoreobjective.getRenderType().func_178796_a());
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        return nbttaglist;
    }

    protected NBTTagList scoresToNbt()
    {
        NBTTagList nbttaglist = new NBTTagList();

        for (Score score : theScoreboard.getScores())
        {
            if (score.getObjective() != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setString("Name", score.getPlayerName());
                nbttagcompound.setString("Objective", score.getObjective().getName());
                nbttagcompound.setInteger("Score", score.getScorePoints());
                nbttagcompound.setBoolean("Locked", score.isLocked());
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        return nbttaglist;
    }
}
