package net.minecraft.server.integrated;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ThreadLanServerPing;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.PlayerUsageSnooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.CryptManager;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.Util;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import optifine.Reflector;
import optifine.WorldServerOF;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IntegratedServer extends MinecraftServer
{
    private static final Logger logger = LogManager.getLogger();

    /** The Minecraft instance. */
    private final Minecraft mc;
    private final WorldSettings theWorldSettings;
    private boolean isGamePaused;
    private boolean isPublic;
    private ThreadLanServerPing lanServerPing;
    private static final String __OBFID = "CL_00001129";

    public IntegratedServer(Minecraft mcIn)
    {
        super(mcIn.getProxy(), new File(mcIn.mcDataDir, MinecraftServer.USER_CACHE_FILE.getName()));
        mc = mcIn;
        theWorldSettings = null;
    }

    public IntegratedServer(Minecraft mcIn, String folderName, String worldName, WorldSettings settings)
    {
        super(new File(mcIn.mcDataDir, "saves"), mcIn.getProxy(), new File(mcIn.mcDataDir, MinecraftServer.USER_CACHE_FILE.getName()));
        setServerOwner(mcIn.getSession().getUsername());
        setFolderName(folderName);
        setWorldName(worldName);
        setDemo(mcIn.isDemo());
        canCreateBonusChest(settings.isBonusChestEnabled());
        setBuildLimit(256);
        setConfigManager(new IntegratedPlayerList(this));
        mc = mcIn;
        theWorldSettings = isDemo() ? DemoWorldServer.demoWorldSettings : settings;
    }

    protected ServerCommandManager createNewCommandManager()
    {
        return new IntegratedServerCommandManager();
    }

    protected void loadAllWorlds(String p_71247_1_, String p_71247_2_, long seed, WorldType type, String p_71247_6_)
    {
        convertMapIfNeeded(p_71247_1_);
        ISaveHandler isavehandler = getActiveAnvilConverter().getSaveLoader(p_71247_1_, true);
        setResourcePackFromWorld(getFolderName(), isavehandler);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();

        if (Reflector.DimensionManager.exists())
        {
            WorldServer worldserver = isDemo() ? (WorldServer) (new DemoWorldServer(this, isavehandler, worldinfo, 0, theProfiler)).init() : (WorldServer)(new WorldServerOF(this, isavehandler, worldinfo, 0, theProfiler)).init();
            worldserver.initialize(theWorldSettings);
            Integer[] ainteger = (Integer[]) Reflector.call(Reflector.DimensionManager_getStaticDimensionIDs, new Object[0]);
            Integer[] ainteger1 = ainteger;
            int i = ainteger.length;

            for (int j = 0; j < i; ++j)
            {
                int k = ainteger1[j].intValue();
                WorldServer worldserver1 = k == 0 ? worldserver : (WorldServer) (new WorldServerMulti(this, isavehandler, k, worldserver, theProfiler)).init();
                worldserver1.addWorldAccess(new WorldManager(this, worldserver1));

                if (!isSinglePlayer())
                {
                    worldserver1.getWorldInfo().setGameType(getGameType());
                }

                if (Reflector.EventBus.exists())
                {
                    Reflector.postForgeBusEvent(Reflector.WorldEvent_Load_Constructor, worldserver1);
                }
            }

            getConfigurationManager().setPlayerManager(new WorldServer[] {worldserver});

            if (worldserver.getWorldInfo().getDifficulty() == null)
            {
                setDifficultyForAllWorlds(mc.gameSettings.difficulty);
            }
        }
        else
        {
            worldServers = new WorldServer[3];
            timeOfLastDimensionTick = new long[worldServers.length][100];
            setResourcePackFromWorld(getFolderName(), isavehandler);

            if (worldinfo == null)
            {
                worldinfo = new WorldInfo(theWorldSettings, p_71247_2_);
            }
            else
            {
                worldinfo.setWorldName(p_71247_2_);
            }

            for (int l = 0; l < worldServers.length; ++l)
            {
                byte b0 = 0;

                if (l == 1)
                {
                    b0 = -1;
                }

                if (l == 2)
                {
                    b0 = 1;
                }

                if (l == 0)
                {
                    if (isDemo())
                    {
                        worldServers[l] = (WorldServer)(new DemoWorldServer(this, isavehandler, worldinfo, b0, theProfiler)).init();
                    }
                    else
                    {
                        worldServers[l] = (WorldServer)(new WorldServerOF(this, isavehandler, worldinfo, b0, theProfiler)).init();
                    }

                    worldServers[l].initialize(theWorldSettings);
                }
                else
                {
                    worldServers[l] = (WorldServer)(new WorldServerMulti(this, isavehandler, b0, worldServers[0], theProfiler)).init();
                }

                worldServers[l].addWorldAccess(new WorldManager(this, worldServers[l]));
            }

            getConfigurationManager().setPlayerManager(worldServers);

            if (worldServers[0].getWorldInfo().getDifficulty() == null)
            {
                setDifficultyForAllWorlds(mc.gameSettings.difficulty);
            }
        }

        initialWorldChunkLoad();
    }

    /**
     * Initialises the server and starts it.
     */
    protected boolean startServer() throws IOException
    {
        IntegratedServer.logger.info("Starting integrated minecraft server version 1.8.8");
        setOnlineMode(true);
        setCanSpawnAnimals(true);
        setCanSpawnNPCs(true);
        setAllowPvp(true);
        setAllowFlight(true);
        IntegratedServer.logger.info("Generating keypair");
        setKeyPair(CryptManager.generateKeyPair());

        if (Reflector.FMLCommonHandler_handleServerAboutToStart.exists())
        {
            Object object = Reflector.call(Reflector.FMLCommonHandler_instance);

            if (!Reflector.callBoolean(object, Reflector.FMLCommonHandler_handleServerAboutToStart, this))
            {
                return false;
            }
        }

        loadAllWorlds(getFolderName(), getWorldName(), theWorldSettings.getSeed(), theWorldSettings.getTerrainType(), theWorldSettings.getWorldName());
        setMOTD(getServerOwner() + " - " + worldServers[0].getWorldInfo().getWorldName());

        if (Reflector.FMLCommonHandler_handleServerStarting.exists())
        {
            Object object1 = Reflector.call(Reflector.FMLCommonHandler_instance);

            if (Reflector.FMLCommonHandler_handleServerStarting.getReturnType() == Boolean.TYPE)
            {
                return Reflector.callBoolean(object1, Reflector.FMLCommonHandler_handleServerStarting, this);
            }

            Reflector.callVoid(object1, Reflector.FMLCommonHandler_handleServerStarting, this);
        }

        return true;
    }

    /**
     * Main function called by run() every loop.
     */
    public void tick()
    {
        boolean flag = isGamePaused;
        isGamePaused = Minecraft.getMinecraft().getNetHandler() != null && Minecraft.getMinecraft().isGamePaused();

        if (!flag && isGamePaused)
        {
            IntegratedServer.logger.info("Saving and pausing game...");
            getConfigurationManager().saveAllPlayerData();
            saveAllWorlds(false);
        }

        if (isGamePaused)
        {
            Queue var3 = futureTaskQueue;

            synchronized (futureTaskQueue)
            {
                while (!futureTaskQueue.isEmpty())
                {
                    Util.func_181617_a((FutureTask) futureTaskQueue.poll(), IntegratedServer.logger);
                }
            }
        }
        else
        {
            super.tick();

            if (mc.gameSettings.renderDistanceChunks != getConfigurationManager().getViewDistance())
            {
                IntegratedServer.logger.info("Changing view distance to {}, from {}", new Object[] {Integer.valueOf(mc.gameSettings.renderDistanceChunks), Integer.valueOf(getConfigurationManager().getViewDistance())});
                getConfigurationManager().setViewDistance(mc.gameSettings.renderDistanceChunks);
            }

            if (mc.theWorld != null)
            {
                WorldInfo worldinfo = worldServers[0].getWorldInfo();
                WorldInfo worldinfo1 = mc.theWorld.getWorldInfo();

                if (!worldinfo.isDifficultyLocked() && worldinfo1.getDifficulty() != worldinfo.getDifficulty())
                {
                    IntegratedServer.logger.info("Changing difficulty to {}, from {}", new Object[] {worldinfo1.getDifficulty(), worldinfo.getDifficulty()});
                    setDifficultyForAllWorlds(worldinfo1.getDifficulty());
                }
                else if (worldinfo1.isDifficultyLocked() && !worldinfo.isDifficultyLocked())
                {
                    IntegratedServer.logger.info("Locking difficulty to {}", new Object[] {worldinfo1.getDifficulty()});

                    for (WorldServer worldserver : worldServers)
                    {
                        if (worldserver != null)
                        {
                            worldserver.getWorldInfo().setDifficultyLocked(true);
                        }
                    }
                }
            }
        }
    }

    public boolean canStructuresSpawn()
    {
        return false;
    }

    public WorldSettings.GameType getGameType()
    {
        return theWorldSettings.getGameType();
    }

    /**
     * Get the server's difficulty
     */
    public EnumDifficulty getDifficulty()
    {
        return mc.theWorld == null ? mc.gameSettings.difficulty : mc.theWorld.getWorldInfo().getDifficulty();
    }

    /**
     * Defaults to false.
     */
    public boolean isHardcore()
    {
        return theWorldSettings.getHardcoreEnabled();
    }

    public boolean func_181034_q()
    {
        return true;
    }

    public boolean func_183002_r()
    {
        return true;
    }

    public File getDataDirectory()
    {
        return mc.mcDataDir;
    }

    public boolean func_181035_ah()
    {
        return false;
    }

    public boolean isDedicatedServer()
    {
        return false;
    }

    /**
     * Called on exit from the main run() loop.
     */
    protected void finalTick(CrashReport report)
    {
        mc.crashed(report);
    }

    /**
     * Adds the server info, including from theWorldServer, to the crash report.
     */
    public CrashReport addServerInfoToCrashReport(CrashReport report)
    {
        report = super.addServerInfoToCrashReport(report);
        report.getCategory().addCrashSectionCallable("Type", new Callable()
        {
            private static final String __OBFID = "CL_00001130";
            public String call() throws Exception
            {
                return "Integrated Server (map_client.txt)";
            }
        });
        report.getCategory().addCrashSectionCallable("Is Modded", new Callable()
        {
            private static final String __OBFID = "CL_00001131";
            public String call() throws Exception
            {
                String s = ClientBrandRetriever.getClientModName();

                if (!s.equals("vanilla"))
                {
                    return "Definitely; Client brand changed to '" + s + "'";
                }
                else
                {
                    s = getServerModName();
                    return !s.equals("vanilla") ? "Definitely; Server brand changed to '" + s + "'" : (Minecraft.class.getSigners() == null ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and both client + server brands are untouched.");
                }
            }
        });
        return report;
    }

    public void setDifficultyForAllWorlds(EnumDifficulty difficulty)
    {
        super.setDifficultyForAllWorlds(difficulty);

        if (mc.theWorld != null)
        {
            mc.theWorld.getWorldInfo().setDifficulty(difficulty);
        }
    }

    public void addServerStatsToSnooper(PlayerUsageSnooper playerSnooper)
    {
        super.addServerStatsToSnooper(playerSnooper);
        playerSnooper.addClientStat("snooper_partner", mc.getPlayerUsageSnooper().getUniqueID());
    }

    /**
     * Returns whether snooping is enabled or not.
     */
    public boolean isSnooperEnabled()
    {
        return Minecraft.getMinecraft().isSnooperEnabled();
    }

    /**
     * On dedicated does nothing. On integrated, sets commandsAllowedForAll, gameType and allows external connections.
     */
    public String shareToLAN(WorldSettings.GameType type, boolean allowCheats)
    {
        try
        {
            int i = -1;

            try
            {
                i = HttpUtil.getSuitableLanPort();
            }
            catch (IOException var5)
            {
            }

            if (i <= 0)
            {
                i = 25564;
            }

            getNetworkSystem().addLanEndpoint(null, i);
            IntegratedServer.logger.info("Started on " + i);
            isPublic = true;
            lanServerPing = new ThreadLanServerPing(getMOTD(), i + "");
            lanServerPing.start();
            getConfigurationManager().setGameType(type);
            getConfigurationManager().setCommandsAllowedForAll(allowCheats);
            return i + "";
        }
        catch (IOException var6)
        {
            return null;
        }
    }

    /**
     * Saves all necessary data as preparation for stopping the server.
     */
    public void stopServer()
    {
        super.stopServer();

        if (lanServerPing != null)
        {
            lanServerPing.interrupt();
            lanServerPing = null;
        }
    }

    /**
     * Sets the serverRunning variable to false, in order to get the server to shut down.
     */
    public void initiateShutdown()
    {
        Futures.getUnchecked(addScheduledTask(new Runnable()
        {
            private static final String __OBFID = "CL_00002380";
            public void run()
            {
                for (EntityPlayerMP entityplayermp : Lists.newArrayList(getConfigurationManager().func_181057_v()))
                {
                    getConfigurationManager().playerLoggedOut(entityplayermp);
                }
            }
        }));
        super.initiateShutdown();

        if (lanServerPing != null)
        {
            lanServerPing.interrupt();
            lanServerPing = null;
        }
    }

    public void setStaticInstance()
    {
        setInstance();
    }

    /**
     * Returns true if this integrated server is open to LAN
     */
    public boolean getPublic()
    {
        return isPublic;
    }

    /**
     * Sets the game type for all worlds.
     */
    public void setGameType(WorldSettings.GameType gameMode)
    {
        getConfigurationManager().setGameType(gameMode);
    }

    /**
     * Return whether command blocks are enabled.
     */
    public boolean isCommandBlockEnabled()
    {
        return true;
    }

    public int getOpPermissionLevel()
    {
        return 4;
    }
}
