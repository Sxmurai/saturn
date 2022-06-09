package net.minecraft.world;

public enum EnumDifficulty
{
    PEACEFUL(0, "options.difficulty.peaceful"),
    EASY(1, "options.difficulty.easy"),
    NORMAL(2, "options.difficulty.normal"),
    HARD(3, "options.difficulty.hard");

    private static final EnumDifficulty[] difficultyEnums = new EnumDifficulty[EnumDifficulty.values().length];
    private final int difficultyId;
    private final String difficultyResourceKey;

    private EnumDifficulty(int difficultyIdIn, String difficultyResourceKeyIn)
    {
        difficultyId = difficultyIdIn;
        difficultyResourceKey = difficultyResourceKeyIn;
    }

    public int getDifficultyId()
    {
        return difficultyId;
    }

    public static EnumDifficulty getDifficultyEnum(int p_151523_0_)
    {
        return EnumDifficulty.difficultyEnums[p_151523_0_ % EnumDifficulty.difficultyEnums.length];
    }

    public String getDifficultyResourceKey()
    {
        return difficultyResourceKey;
    }

    static {
        for (EnumDifficulty enumdifficulty : EnumDifficulty.values())
        {
            EnumDifficulty.difficultyEnums[enumdifficulty.difficultyId] = enumdifficulty;
        }
    }
}
