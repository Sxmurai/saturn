package net.minecraft.world;

import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.world.storage.WorldInfo;

public final class WorldSettings
{
    /** The seed for the map. */
    private final long seed;

    /** The EnumGameType. */
    private final WorldSettings.GameType theGameType;

    /**
     * Switch for the map features. 'true' for enabled, 'false' for disabled.
     */
    private final boolean mapFeaturesEnabled;

    /** True if hardcore mode is enabled */
    private final boolean hardcoreEnabled;
    private final WorldType terrainType;

    /** True if Commands (cheats) are allowed. */
    private boolean commandsAllowed;

    /** True if the Bonus Chest is enabled. */
    private boolean bonusChestEnabled;
    private String worldName;

    public WorldSettings(long seedIn, WorldSettings.GameType gameType, boolean enableMapFeatures, boolean hardcoreMode, WorldType worldTypeIn)
    {
        worldName = "";
        seed = seedIn;
        theGameType = gameType;
        mapFeaturesEnabled = enableMapFeatures;
        hardcoreEnabled = hardcoreMode;
        terrainType = worldTypeIn;
    }

    public WorldSettings(WorldInfo info)
    {
        this(info.getSeed(), info.getGameType(), info.isMapFeaturesEnabled(), info.isHardcoreModeEnabled(), info.getTerrainType());
    }

    /**
     * Enables the bonus chest.
     */
    public WorldSettings enableBonusChest()
    {
        bonusChestEnabled = true;
        return this;
    }

    /**
     * Enables Commands (cheats).
     */
    public WorldSettings enableCommands()
    {
        commandsAllowed = true;
        return this;
    }

    public WorldSettings setWorldName(String name)
    {
        worldName = name;
        return this;
    }

    /**
     * Returns true if the Bonus Chest is enabled.
     */
    public boolean isBonusChestEnabled()
    {
        return bonusChestEnabled;
    }

    /**
     * Returns the seed for the world.
     */
    public long getSeed()
    {
        return seed;
    }

    /**
     * Gets the game type.
     */
    public WorldSettings.GameType getGameType()
    {
        return theGameType;
    }

    /**
     * Returns true if hardcore mode is enabled, otherwise false
     */
    public boolean getHardcoreEnabled()
    {
        return hardcoreEnabled;
    }

    /**
     * Get whether the map features (e.g. strongholds) generation is enabled or disabled.
     */
    public boolean isMapFeaturesEnabled()
    {
        return mapFeaturesEnabled;
    }

    public WorldType getTerrainType()
    {
        return terrainType;
    }

    /**
     * Returns true if Commands (cheats) are allowed.
     */
    public boolean areCommandsAllowed()
    {
        return commandsAllowed;
    }

    /**
     * Gets the GameType by ID
     */
    public static WorldSettings.GameType getGameTypeById(int id)
    {
        return WorldSettings.GameType.getByID(id);
    }

    public String getWorldName()
    {
        return worldName;
    }

    public static enum GameType
    {
        NOT_SET(-1, ""),
        SURVIVAL(0, "survival"),
        CREATIVE(1, "creative"),
        ADVENTURE(2, "adventure"),
        SPECTATOR(3, "spectator");

        int id;
        String name;

        private GameType(int typeId, String nameIn)
        {
            id = typeId;
            name = nameIn;
        }

        public int getID()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public void configurePlayerCapabilities(PlayerCapabilities capabilities)
        {
            if (this == GameType.CREATIVE)
            {
                capabilities.allowFlying = true;
                capabilities.isCreativeMode = true;
                capabilities.disableDamage = true;
            }
            else if (this == GameType.SPECTATOR)
            {
                capabilities.allowFlying = true;
                capabilities.isCreativeMode = false;
                capabilities.disableDamage = true;
                capabilities.isFlying = true;
            }
            else
            {
                capabilities.allowFlying = false;
                capabilities.isCreativeMode = false;
                capabilities.disableDamage = false;
                capabilities.isFlying = false;
            }

            capabilities.allowEdit = !isAdventure();
        }

        public boolean isAdventure()
        {
            return this == GameType.ADVENTURE || this == GameType.SPECTATOR;
        }

        public boolean isCreative()
        {
            return this == GameType.CREATIVE;
        }

        public boolean isSurvivalOrAdventure()
        {
            return this == GameType.SURVIVAL || this == GameType.ADVENTURE;
        }

        public static WorldSettings.GameType getByID(int idIn)
        {
            for (WorldSettings.GameType worldsettings$gametype : GameType.values())
            {
                if (worldsettings$gametype.id == idIn)
                {
                    return worldsettings$gametype;
                }
            }

            return GameType.SURVIVAL;
        }

        public static WorldSettings.GameType getByName(String p_77142_0_)
        {
            for (WorldSettings.GameType worldsettings$gametype : GameType.values())
            {
                if (worldsettings$gametype.name.equals(p_77142_0_))
                {
                    return worldsettings$gametype;
                }
            }

            return GameType.SURVIVAL;
        }
    }
}
