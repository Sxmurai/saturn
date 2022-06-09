package net.minecraft.world.storage;

import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

public class WorldInfo
{
    public static final EnumDifficulty DEFAULT_DIFFICULTY = EnumDifficulty.NORMAL;

    /** Holds the seed of the currently world. */
    private long randomSeed;
    private WorldType terrainType = WorldType.DEFAULT;
    private String generatorOptions = "";

    /** The spawn zone position X coordinate. */
    private int spawnX;

    /** The spawn zone position Y coordinate. */
    private int spawnY;

    /** The spawn zone position Z coordinate. */
    private int spawnZ;

    /** Total time for this world. */
    private long totalTime;

    /** The current world time in ticks, ranging from 0 to 23999. */
    private long worldTime;

    /** The last time the player was in this world. */
    private long lastTimePlayed;

    /** The size of entire save of current world on the disk, isn't exactly. */
    private long sizeOnDisk;
    private NBTTagCompound playerTag;
    private int dimension;

    /** The name of the save defined at world creation. */
    private String levelName;

    /** Introduced in beta 1.3, is the save version for future control. */
    private int saveVersion;
    private int cleanWeatherTime;

    /** True if it's raining, false otherwise. */
    private boolean raining;

    /** Number of ticks until next rain. */
    private int rainTime;

    /** Is thunderbolts failing now? */
    private boolean thundering;

    /** Number of ticks untils next thunderbolt. */
    private int thunderTime;

    /** The Game Type. */
    private WorldSettings.GameType theGameType;

    /**
     * Whether the map features (e.g. strongholds) generation is enabled or disabled.
     */
    private boolean mapFeaturesEnabled;

    /** Hardcore mode flag */
    private boolean hardcore;
    private boolean allowCommands;
    private boolean initialized;
    private EnumDifficulty difficulty;
    private boolean difficultyLocked;
    private double borderCenterX = 0.0D;
    private double borderCenterZ = 0.0D;
    private double borderSize = 6.0E7D;
    private long borderSizeLerpTime = 0L;
    private double borderSizeLerpTarget = 0.0D;
    private double borderSafeZone = 5.0D;
    private double borderDamagePerBlock = 0.2D;
    private int borderWarningDistance = 5;
    private int borderWarningTime = 15;
    private GameRules theGameRules = new GameRules();

    protected WorldInfo()
    {
    }

    public WorldInfo(NBTTagCompound nbt)
    {
        randomSeed = nbt.getLong("RandomSeed");

        if (nbt.hasKey("generatorName", 8))
        {
            String s = nbt.getString("generatorName");
            terrainType = WorldType.parseWorldType(s);

            if (terrainType == null)
            {
                terrainType = WorldType.DEFAULT;
            }
            else if (terrainType.isVersioned())
            {
                int i = 0;

                if (nbt.hasKey("generatorVersion", 99))
                {
                    i = nbt.getInteger("generatorVersion");
                }

                terrainType = terrainType.getWorldTypeForGeneratorVersion(i);
            }

            if (nbt.hasKey("generatorOptions", 8))
            {
                generatorOptions = nbt.getString("generatorOptions");
            }
        }

        theGameType = WorldSettings.GameType.getByID(nbt.getInteger("GameType"));

        if (nbt.hasKey("MapFeatures", 99))
        {
            mapFeaturesEnabled = nbt.getBoolean("MapFeatures");
        }
        else
        {
            mapFeaturesEnabled = true;
        }

        spawnX = nbt.getInteger("SpawnX");
        spawnY = nbt.getInteger("SpawnY");
        spawnZ = nbt.getInteger("SpawnZ");
        totalTime = nbt.getLong("Time");

        if (nbt.hasKey("DayTime", 99))
        {
            worldTime = nbt.getLong("DayTime");
        }
        else
        {
            worldTime = totalTime;
        }

        lastTimePlayed = nbt.getLong("LastPlayed");
        sizeOnDisk = nbt.getLong("SizeOnDisk");
        levelName = nbt.getString("LevelName");
        saveVersion = nbt.getInteger("version");
        cleanWeatherTime = nbt.getInteger("clearWeatherTime");
        rainTime = nbt.getInteger("rainTime");
        raining = nbt.getBoolean("raining");
        thunderTime = nbt.getInteger("thunderTime");
        thundering = nbt.getBoolean("thundering");
        hardcore = nbt.getBoolean("hardcore");

        if (nbt.hasKey("initialized", 99))
        {
            initialized = nbt.getBoolean("initialized");
        }
        else
        {
            initialized = true;
        }

        if (nbt.hasKey("allowCommands", 99))
        {
            allowCommands = nbt.getBoolean("allowCommands");
        }
        else
        {
            allowCommands = theGameType == WorldSettings.GameType.CREATIVE;
        }

        if (nbt.hasKey("Player", 10))
        {
            playerTag = nbt.getCompoundTag("Player");
            dimension = playerTag.getInteger("Dimension");
        }

        if (nbt.hasKey("GameRules", 10))
        {
            theGameRules.readFromNBT(nbt.getCompoundTag("GameRules"));
        }

        if (nbt.hasKey("Difficulty", 99))
        {
            difficulty = EnumDifficulty.getDifficultyEnum(nbt.getByte("Difficulty"));
        }

        if (nbt.hasKey("DifficultyLocked", 1))
        {
            difficultyLocked = nbt.getBoolean("DifficultyLocked");
        }

        if (nbt.hasKey("BorderCenterX", 99))
        {
            borderCenterX = nbt.getDouble("BorderCenterX");
        }

        if (nbt.hasKey("BorderCenterZ", 99))
        {
            borderCenterZ = nbt.getDouble("BorderCenterZ");
        }

        if (nbt.hasKey("BorderSize", 99))
        {
            borderSize = nbt.getDouble("BorderSize");
        }

        if (nbt.hasKey("BorderSizeLerpTime", 99))
        {
            borderSizeLerpTime = nbt.getLong("BorderSizeLerpTime");
        }

        if (nbt.hasKey("BorderSizeLerpTarget", 99))
        {
            borderSizeLerpTarget = nbt.getDouble("BorderSizeLerpTarget");
        }

        if (nbt.hasKey("BorderSafeZone", 99))
        {
            borderSafeZone = nbt.getDouble("BorderSafeZone");
        }

        if (nbt.hasKey("BorderDamagePerBlock", 99))
        {
            borderDamagePerBlock = nbt.getDouble("BorderDamagePerBlock");
        }

        if (nbt.hasKey("BorderWarningBlocks", 99))
        {
            borderWarningDistance = nbt.getInteger("BorderWarningBlocks");
        }

        if (nbt.hasKey("BorderWarningTime", 99))
        {
            borderWarningTime = nbt.getInteger("BorderWarningTime");
        }
    }

    public WorldInfo(WorldSettings settings, String name)
    {
        populateFromWorldSettings(settings);
        levelName = name;
        difficulty = WorldInfo.DEFAULT_DIFFICULTY;
        initialized = false;
    }

    public void populateFromWorldSettings(WorldSettings settings)
    {
        randomSeed = settings.getSeed();
        theGameType = settings.getGameType();
        mapFeaturesEnabled = settings.isMapFeaturesEnabled();
        hardcore = settings.getHardcoreEnabled();
        terrainType = settings.getTerrainType();
        generatorOptions = settings.getWorldName();
        allowCommands = settings.areCommandsAllowed();
    }

    public WorldInfo(WorldInfo worldInformation)
    {
        randomSeed = worldInformation.randomSeed;
        terrainType = worldInformation.terrainType;
        generatorOptions = worldInformation.generatorOptions;
        theGameType = worldInformation.theGameType;
        mapFeaturesEnabled = worldInformation.mapFeaturesEnabled;
        spawnX = worldInformation.spawnX;
        spawnY = worldInformation.spawnY;
        spawnZ = worldInformation.spawnZ;
        totalTime = worldInformation.totalTime;
        worldTime = worldInformation.worldTime;
        lastTimePlayed = worldInformation.lastTimePlayed;
        sizeOnDisk = worldInformation.sizeOnDisk;
        playerTag = worldInformation.playerTag;
        dimension = worldInformation.dimension;
        levelName = worldInformation.levelName;
        saveVersion = worldInformation.saveVersion;
        rainTime = worldInformation.rainTime;
        raining = worldInformation.raining;
        thunderTime = worldInformation.thunderTime;
        thundering = worldInformation.thundering;
        hardcore = worldInformation.hardcore;
        allowCommands = worldInformation.allowCommands;
        initialized = worldInformation.initialized;
        theGameRules = worldInformation.theGameRules;
        difficulty = worldInformation.difficulty;
        difficultyLocked = worldInformation.difficultyLocked;
        borderCenterX = worldInformation.borderCenterX;
        borderCenterZ = worldInformation.borderCenterZ;
        borderSize = worldInformation.borderSize;
        borderSizeLerpTime = worldInformation.borderSizeLerpTime;
        borderSizeLerpTarget = worldInformation.borderSizeLerpTarget;
        borderSafeZone = worldInformation.borderSafeZone;
        borderDamagePerBlock = worldInformation.borderDamagePerBlock;
        borderWarningTime = worldInformation.borderWarningTime;
        borderWarningDistance = worldInformation.borderWarningDistance;
    }

    /**
     * Gets the NBTTagCompound for the worldInfo
     */
    public NBTTagCompound getNBTTagCompound()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        updateTagCompound(nbttagcompound, playerTag);
        return nbttagcompound;
    }

    /**
     * Creates a new NBTTagCompound for the world, with the given NBTTag as the "Player"
     */
    public NBTTagCompound cloneNBTCompound(NBTTagCompound nbt)
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        updateTagCompound(nbttagcompound, nbt);
        return nbttagcompound;
    }

    private void updateTagCompound(NBTTagCompound nbt, NBTTagCompound playerNbt)
    {
        nbt.setLong("RandomSeed", randomSeed);
        nbt.setString("generatorName", terrainType.getWorldTypeName());
        nbt.setInteger("generatorVersion", terrainType.getGeneratorVersion());
        nbt.setString("generatorOptions", generatorOptions);
        nbt.setInteger("GameType", theGameType.getID());
        nbt.setBoolean("MapFeatures", mapFeaturesEnabled);
        nbt.setInteger("SpawnX", spawnX);
        nbt.setInteger("SpawnY", spawnY);
        nbt.setInteger("SpawnZ", spawnZ);
        nbt.setLong("Time", totalTime);
        nbt.setLong("DayTime", worldTime);
        nbt.setLong("SizeOnDisk", sizeOnDisk);
        nbt.setLong("LastPlayed", MinecraftServer.getCurrentTimeMillis());
        nbt.setString("LevelName", levelName);
        nbt.setInteger("version", saveVersion);
        nbt.setInteger("clearWeatherTime", cleanWeatherTime);
        nbt.setInteger("rainTime", rainTime);
        nbt.setBoolean("raining", raining);
        nbt.setInteger("thunderTime", thunderTime);
        nbt.setBoolean("thundering", thundering);
        nbt.setBoolean("hardcore", hardcore);
        nbt.setBoolean("allowCommands", allowCommands);
        nbt.setBoolean("initialized", initialized);
        nbt.setDouble("BorderCenterX", borderCenterX);
        nbt.setDouble("BorderCenterZ", borderCenterZ);
        nbt.setDouble("BorderSize", borderSize);
        nbt.setLong("BorderSizeLerpTime", borderSizeLerpTime);
        nbt.setDouble("BorderSafeZone", borderSafeZone);
        nbt.setDouble("BorderDamagePerBlock", borderDamagePerBlock);
        nbt.setDouble("BorderSizeLerpTarget", borderSizeLerpTarget);
        nbt.setDouble("BorderWarningBlocks", borderWarningDistance);
        nbt.setDouble("BorderWarningTime", borderWarningTime);

        if (difficulty != null)
        {
            nbt.setByte("Difficulty", (byte) difficulty.getDifficultyId());
        }

        nbt.setBoolean("DifficultyLocked", difficultyLocked);
        nbt.setTag("GameRules", theGameRules.writeToNBT());

        if (playerNbt != null)
        {
            nbt.setTag("Player", playerNbt);
        }
    }

    /**
     * Returns the seed of current world.
     */
    public long getSeed()
    {
        return randomSeed;
    }

    /**
     * Returns the x spawn position
     */
    public int getSpawnX()
    {
        return spawnX;
    }

    /**
     * Return the Y axis spawning point of the player.
     */
    public int getSpawnY()
    {
        return spawnY;
    }

    /**
     * Returns the z spawn position
     */
    public int getSpawnZ()
    {
        return spawnZ;
    }

    public long getWorldTotalTime()
    {
        return totalTime;
    }

    /**
     * Get current world time
     */
    public long getWorldTime()
    {
        return worldTime;
    }

    public long getSizeOnDisk()
    {
        return sizeOnDisk;
    }

    /**
     * Returns the player's NBTTagCompound to be loaded
     */
    public NBTTagCompound getPlayerNBTTagCompound()
    {
        return playerTag;
    }

    /**
     * Set the x spawn position to the passed in value
     */
    public void setSpawnX(int x)
    {
        spawnX = x;
    }

    /**
     * Sets the y spawn position
     */
    public void setSpawnY(int y)
    {
        spawnY = y;
    }

    /**
     * Set the z spawn position to the passed in value
     */
    public void setSpawnZ(int z)
    {
        spawnZ = z;
    }

    public void setWorldTotalTime(long time)
    {
        totalTime = time;
    }

    /**
     * Set current world time
     */
    public void setWorldTime(long time)
    {
        worldTime = time;
    }

    public void setSpawn(BlockPos spawnPoint)
    {
        spawnX = spawnPoint.getX();
        spawnY = spawnPoint.getY();
        spawnZ = spawnPoint.getZ();
    }

    /**
     * Get current world name
     */
    public String getWorldName()
    {
        return levelName;
    }

    public void setWorldName(String worldName)
    {
        levelName = worldName;
    }

    /**
     * Returns the save version of this world
     */
    public int getSaveVersion()
    {
        return saveVersion;
    }

    /**
     * Sets the save version of the world
     */
    public void setSaveVersion(int version)
    {
        saveVersion = version;
    }

    /**
     * Return the last time the player was in this world.
     */
    public long getLastTimePlayed()
    {
        return lastTimePlayed;
    }

    public int getCleanWeatherTime()
    {
        return cleanWeatherTime;
    }

    public void setCleanWeatherTime(int cleanWeatherTimeIn)
    {
        cleanWeatherTime = cleanWeatherTimeIn;
    }

    /**
     * Returns true if it is thundering, false otherwise.
     */
    public boolean isThundering()
    {
        return thundering;
    }

    /**
     * Sets whether it is thundering or not.
     */
    public void setThundering(boolean thunderingIn)
    {
        thundering = thunderingIn;
    }

    /**
     * Returns the number of ticks until next thunderbolt.
     */
    public int getThunderTime()
    {
        return thunderTime;
    }

    /**
     * Defines the number of ticks until next thunderbolt.
     */
    public void setThunderTime(int time)
    {
        thunderTime = time;
    }

    /**
     * Returns true if it is raining, false otherwise.
     */
    public boolean isRaining()
    {
        return raining;
    }

    /**
     * Sets whether it is raining or not.
     */
    public void setRaining(boolean isRaining)
    {
        raining = isRaining;
    }

    /**
     * Return the number of ticks until rain.
     */
    public int getRainTime()
    {
        return rainTime;
    }

    /**
     * Sets the number of ticks until rain.
     */
    public void setRainTime(int time)
    {
        rainTime = time;
    }

    /**
     * Gets the GameType.
     */
    public WorldSettings.GameType getGameType()
    {
        return theGameType;
    }

    /**
     * Get whether the map features (e.g. strongholds) generation is enabled or disabled.
     */
    public boolean isMapFeaturesEnabled()
    {
        return mapFeaturesEnabled;
    }

    public void setMapFeaturesEnabled(boolean enabled)
    {
        mapFeaturesEnabled = enabled;
    }

    /**
     * Sets the GameType.
     */
    public void setGameType(WorldSettings.GameType type)
    {
        theGameType = type;
    }

    /**
     * Returns true if hardcore mode is enabled, otherwise false
     */
    public boolean isHardcoreModeEnabled()
    {
        return hardcore;
    }

    public void setHardcore(boolean hardcoreIn)
    {
        hardcore = hardcoreIn;
    }

    public WorldType getTerrainType()
    {
        return terrainType;
    }

    public void setTerrainType(WorldType type)
    {
        terrainType = type;
    }

    public String getGeneratorOptions()
    {
        return generatorOptions;
    }

    /**
     * Returns true if commands are allowed on this World.
     */
    public boolean areCommandsAllowed()
    {
        return allowCommands;
    }

    public void setAllowCommands(boolean allow)
    {
        allowCommands = allow;
    }

    /**
     * Returns true if the World is initialized.
     */
    public boolean isInitialized()
    {
        return initialized;
    }

    /**
     * Sets the initialization status of the World.
     */
    public void setServerInitialized(boolean initializedIn)
    {
        initialized = initializedIn;
    }

    /**
     * Gets the GameRules class Instance.
     */
    public GameRules getGameRulesInstance()
    {
        return theGameRules;
    }

    /**
     * Returns the border center X position
     */
    public double getBorderCenterX()
    {
        return borderCenterX;
    }

    /**
     * Returns the border center Z position
     */
    public double getBorderCenterZ()
    {
        return borderCenterZ;
    }

    public double getBorderSize()
    {
        return borderSize;
    }

    /**
     * Sets the border size
     */
    public void setBorderSize(double size)
    {
        borderSize = size;
    }

    /**
     * Returns the border lerp time
     */
    public long getBorderLerpTime()
    {
        return borderSizeLerpTime;
    }

    /**
     * Sets the border lerp time
     */
    public void setBorderLerpTime(long time)
    {
        borderSizeLerpTime = time;
    }

    /**
     * Returns the border lerp target
     */
    public double getBorderLerpTarget()
    {
        return borderSizeLerpTarget;
    }

    /**
     * Sets the border lerp target
     */
    public void setBorderLerpTarget(double lerpSize)
    {
        borderSizeLerpTarget = lerpSize;
    }

    /**
     * Sets the border center Z position
     */
    public void getBorderCenterZ(double posZ)
    {
        borderCenterZ = posZ;
    }

    /**
     * Sets the border center X position
     */
    public void getBorderCenterX(double posX)
    {
        borderCenterX = posX;
    }

    /**
     * Returns the border safe zone
     */
    public double getBorderSafeZone()
    {
        return borderSafeZone;
    }

    /**
     * Sets the border safe zone
     */
    public void setBorderSafeZone(double amount)
    {
        borderSafeZone = amount;
    }

    /**
     * Returns the border damage per block
     */
    public double getBorderDamagePerBlock()
    {
        return borderDamagePerBlock;
    }

    /**
     * Sets the border damage per block
     */
    public void setBorderDamagePerBlock(double damage)
    {
        borderDamagePerBlock = damage;
    }

    /**
     * Returns the border warning distance
     */
    public int getBorderWarningDistance()
    {
        return borderWarningDistance;
    }

    /**
     * Returns the border warning time
     */
    public int getBorderWarningTime()
    {
        return borderWarningTime;
    }

    /**
     * Sets the border warning distance
     */
    public void setBorderWarningDistance(int amountOfBlocks)
    {
        borderWarningDistance = amountOfBlocks;
    }

    /**
     * Sets the border warning time
     */
    public void setBorderWarningTime(int ticks)
    {
        borderWarningTime = ticks;
    }

    public EnumDifficulty getDifficulty()
    {
        return difficulty;
    }

    public void setDifficulty(EnumDifficulty newDifficulty)
    {
        difficulty = newDifficulty;
    }

    public boolean isDifficultyLocked()
    {
        return difficultyLocked;
    }

    public void setDifficultyLocked(boolean locked)
    {
        difficultyLocked = locked;
    }

    /**
     * Adds this WorldInfo instance to the crash report.
     */
    public void addToCrashReport(CrashReportCategory category)
    {
        category.addCrashSectionCallable("Level seed", new Callable<String>()
        {
            public String call() throws Exception
            {
                return String.valueOf(getSeed());
            }
        });
        category.addCrashSectionCallable("Level generator", new Callable<String>()
        {
            public String call() throws Exception
            {
                return String.format("ID %02d - %s, ver %d. Features enabled: %b", Integer.valueOf(terrainType.getWorldTypeID()), terrainType.getWorldTypeName(), Integer.valueOf(terrainType.getGeneratorVersion()), Boolean.valueOf(mapFeaturesEnabled));
            }
        });
        category.addCrashSectionCallable("Level generator options", new Callable<String>()
        {
            public String call() throws Exception
            {
                return generatorOptions;
            }
        });
        category.addCrashSectionCallable("Level spawn location", new Callable<String>()
        {
            public String call() throws Exception
            {
                return CrashReportCategory.getCoordinateInfo(spawnX, spawnY, spawnZ);
            }
        });
        category.addCrashSectionCallable("Level time", new Callable<String>()
        {
            public String call() throws Exception
            {
                return String.format("%d game time, %d day time", Long.valueOf(totalTime), Long.valueOf(worldTime));
            }
        });
        category.addCrashSectionCallable("Level dimension", new Callable<String>()
        {
            public String call() throws Exception
            {
                return String.valueOf(dimension);
            }
        });
        category.addCrashSectionCallable("Level storage version", new Callable<String>()
        {
            public String call() throws Exception
            {
                String s = "Unknown?";

                try
                {
                    switch (saveVersion)
                    {
                        case 19132:
                            s = "McRegion";
                            break;

                        case 19133:
                            s = "Anvil";
                    }
                }
                catch (Throwable var3)
                {
                }

                return String.format("0x%05X - %s", Integer.valueOf(saveVersion), s);
            }
        });
        category.addCrashSectionCallable("Level weather", new Callable<String>()
        {
            public String call() throws Exception
            {
                return String.format("Rain time: %d (now: %b), thunder time: %d (now: %b)", Integer.valueOf(rainTime), Boolean.valueOf(raining), Integer.valueOf(thunderTime), Boolean.valueOf(thundering));
            }
        });
        category.addCrashSectionCallable("Level game mode", new Callable<String>()
        {
            public String call() throws Exception
            {
                return String.format("Game mode: %s (ID %d). Hardcore: %b. Cheats: %b", theGameType.getName(), Integer.valueOf(theGameType.getID()), Boolean.valueOf(hardcore), Boolean.valueOf(allowCommands));
            }
        });
    }
}
