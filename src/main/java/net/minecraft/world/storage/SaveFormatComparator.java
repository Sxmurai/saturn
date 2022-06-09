package net.minecraft.world.storage;

import net.minecraft.world.WorldSettings;

public class SaveFormatComparator implements Comparable<SaveFormatComparator>
{
    /** the file name of this save */
    private final String fileName;

    /** the displayed name of this save file */
    private final String displayName;
    private final long lastTimePlayed;
    private final long sizeOnDisk;
    private final boolean requiresConversion;

    /** Instance of EnumGameType. */
    private final WorldSettings.GameType theEnumGameType;
    private final boolean hardcore;
    private final boolean cheatsEnabled;

    public SaveFormatComparator(String fileNameIn, String displayNameIn, long lastTimePlayedIn, long sizeOnDiskIn, WorldSettings.GameType theEnumGameTypeIn, boolean requiresConversionIn, boolean hardcoreIn, boolean cheatsEnabledIn)
    {
        fileName = fileNameIn;
        displayName = displayNameIn;
        lastTimePlayed = lastTimePlayedIn;
        sizeOnDisk = sizeOnDiskIn;
        theEnumGameType = theEnumGameTypeIn;
        requiresConversion = requiresConversionIn;
        hardcore = hardcoreIn;
        cheatsEnabled = cheatsEnabledIn;
    }

    /**
     * return the file name
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * return the display name of the save
     */
    public String getDisplayName()
    {
        return displayName;
    }

    public long getSizeOnDisk()
    {
        return sizeOnDisk;
    }

    public boolean requiresConversion()
    {
        return requiresConversion;
    }

    public long getLastTimePlayed()
    {
        return lastTimePlayed;
    }

    public int compareTo(SaveFormatComparator p_compareTo_1_)
    {
        return lastTimePlayed < p_compareTo_1_.lastTimePlayed ? 1 : (lastTimePlayed > p_compareTo_1_.lastTimePlayed ? -1 : fileName.compareTo(p_compareTo_1_.fileName));
    }

    /**
     * Gets the EnumGameType.
     */
    public WorldSettings.GameType getEnumGameType()
    {
        return theEnumGameType;
    }

    public boolean isHardcoreModeEnabled()
    {
        return hardcore;
    }

    /**
     * @return {@code true} if cheats are enabled for this world
     */
    public boolean getCheatsEnabled()
    {
        return cheatsEnabled;
    }
}
