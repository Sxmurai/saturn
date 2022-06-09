package net.minecraft.scoreboard;

public class ScoreObjective
{
    private final Scoreboard theScoreboard;
    private final String name;

    /** The ScoreObjectiveCriteria for this objetive */
    private final IScoreObjectiveCriteria objectiveCriteria;
    private IScoreObjectiveCriteria.EnumRenderType renderType;
    private String displayName;

    public ScoreObjective(Scoreboard theScoreboardIn, String nameIn, IScoreObjectiveCriteria objectiveCriteriaIn)
    {
        theScoreboard = theScoreboardIn;
        name = nameIn;
        objectiveCriteria = objectiveCriteriaIn;
        displayName = nameIn;
        renderType = objectiveCriteriaIn.getRenderType();
    }

    public Scoreboard getScoreboard()
    {
        return theScoreboard;
    }

    public String getName()
    {
        return name;
    }

    public IScoreObjectiveCriteria getCriteria()
    {
        return objectiveCriteria;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String nameIn)
    {
        displayName = nameIn;
        theScoreboard.func_96532_b(this);
    }

    public IScoreObjectiveCriteria.EnumRenderType getRenderType()
    {
        return renderType;
    }

    public void setRenderType(IScoreObjectiveCriteria.EnumRenderType type)
    {
        renderType = type;
        theScoreboard.func_96532_b(this);
    }
}
