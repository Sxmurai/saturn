package net.minecraft.scoreboard;

import java.util.Comparator;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;

public class Score
{
    public static final Comparator<Score> scoreComparator = new Comparator<Score>()
    {
        public int compare(Score p_compare_1_, Score p_compare_2_)
        {
            return p_compare_1_.getScorePoints() > p_compare_2_.getScorePoints() ? 1 : (p_compare_1_.getScorePoints() < p_compare_2_.getScorePoints() ? -1 : p_compare_2_.getPlayerName().compareToIgnoreCase(p_compare_1_.getPlayerName()));
        }
    };
    private final Scoreboard theScoreboard;
    private final ScoreObjective theScoreObjective;
    private final String scorePlayerName;
    private int scorePoints;
    private boolean locked;
    private boolean field_178818_g;

    public Score(Scoreboard theScoreboardIn, ScoreObjective theScoreObjectiveIn, String scorePlayerNameIn)
    {
        theScoreboard = theScoreboardIn;
        theScoreObjective = theScoreObjectiveIn;
        scorePlayerName = scorePlayerNameIn;
        field_178818_g = true;
    }

    public void increseScore(int amount)
    {
        if (theScoreObjective.getCriteria().isReadOnly())
        {
            throw new IllegalStateException("Cannot modify read-only score");
        }
        else
        {
            setScorePoints(getScorePoints() + amount);
        }
    }

    public void decreaseScore(int amount)
    {
        if (theScoreObjective.getCriteria().isReadOnly())
        {
            throw new IllegalStateException("Cannot modify read-only score");
        }
        else
        {
            setScorePoints(getScorePoints() - amount);
        }
    }

    public void func_96648_a()
    {
        if (theScoreObjective.getCriteria().isReadOnly())
        {
            throw new IllegalStateException("Cannot modify read-only score");
        }
        else
        {
            increseScore(1);
        }
    }

    public int getScorePoints()
    {
        return scorePoints;
    }

    public void setScorePoints(int points)
    {
        int i = scorePoints;
        scorePoints = points;

        if (i != points || field_178818_g)
        {
            field_178818_g = false;
            getScoreScoreboard().func_96536_a(this);
        }
    }

    public ScoreObjective getObjective()
    {
        return theScoreObjective;
    }

    /**
     * Returns the name of the player this score belongs to
     */
    public String getPlayerName()
    {
        return scorePlayerName;
    }

    public Scoreboard getScoreScoreboard()
    {
        return theScoreboard;
    }

    public boolean isLocked()
    {
        return locked;
    }

    public void setLocked(boolean locked)
    {
        this.locked = locked;
    }

    public void func_96651_a(List<EntityPlayer> p_96651_1_)
    {
        setScorePoints(theScoreObjective.getCriteria().func_96635_a(p_96651_1_));
    }
}
