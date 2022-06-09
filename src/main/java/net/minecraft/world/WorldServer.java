package net.minecraft.world;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.INpc;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S24PacketBlockAction;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.ScoreboardSaveData;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.village.VillageCollection;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.feature.WorldGeneratorBonusChest;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldServer extends World implements IThreadListener
{
    private static final Logger logger = LogManager.getLogger();
    private final MinecraftServer mcServer;
    private final EntityTracker theEntityTracker;
    private final PlayerManager thePlayerManager;
    private final Set<NextTickListEntry> pendingTickListEntriesHashSet = Sets.newHashSet();
    private final TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet = new TreeSet();
    private final Map<UUID, Entity> entitiesByUuid = Maps.newHashMap();
    public ChunkProviderServer theChunkProviderServer;

    /** Whether level saving is disabled or not */
    public boolean disableLevelSaving;

    /** is false if there are no players */
    private boolean allPlayersSleeping;
    private int updateEntityTick;

    /**
     * the teleporter to use when the entity is being transferred into the dimension
     */
    private final Teleporter worldTeleporter;
    private final SpawnerAnimals mobSpawner = new SpawnerAnimals();
    protected final VillageSiege villageSiege = new VillageSiege(this);
    private final WorldServer.ServerBlockEventList[] field_147490_S = new WorldServer.ServerBlockEventList[] {new WorldServer.ServerBlockEventList(), new WorldServer.ServerBlockEventList()};
    private int blockEventCacheIndex;
    private static final List<WeightedRandomChestContent> bonusChestContent = Lists.newArrayList(new WeightedRandomChestContent(Items.stick, 0, 1, 3, 10), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.planks), 0, 1, 3, 10), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.log), 0, 1, 3, 10), new WeightedRandomChestContent(Items.stone_axe, 0, 1, 1, 3), new WeightedRandomChestContent(Items.wooden_axe, 0, 1, 1, 5), new WeightedRandomChestContent(Items.stone_pickaxe, 0, 1, 1, 3), new WeightedRandomChestContent(Items.wooden_pickaxe, 0, 1, 1, 5), new WeightedRandomChestContent(Items.apple, 0, 2, 3, 5), new WeightedRandomChestContent(Items.bread, 0, 2, 3, 3), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.log2), 0, 1, 3, 10));
    private final List<NextTickListEntry> pendingTickListEntriesThisTick = Lists.newArrayList();

    public WorldServer(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn)
    {
        super(saveHandlerIn, info, WorldProvider.getProviderForDimension(dimensionId), profilerIn, false);
        mcServer = server;
        theEntityTracker = new EntityTracker(this);
        thePlayerManager = new PlayerManager(this);
        provider.registerWorld(this);
        chunkProvider = createChunkProvider();
        worldTeleporter = new Teleporter(this);
        calculateInitialSkylight();
        calculateInitialWeather();
        getWorldBorder().setSize(server.getMaxWorldSize());
    }

    public World init()
    {
        mapStorage = new MapStorage(saveHandler);
        String s = VillageCollection.fileNameForProvider(provider);
        VillageCollection villagecollection = (VillageCollection) mapStorage.loadData(VillageCollection.class, s);

        if (villagecollection == null)
        {
            villageCollectionObj = new VillageCollection(this);
            mapStorage.setData(s, villageCollectionObj);
        }
        else
        {
            villageCollectionObj = villagecollection;
            villageCollectionObj.setWorldsForAll(this);
        }

        worldScoreboard = new ServerScoreboard(mcServer);
        ScoreboardSaveData scoreboardsavedata = (ScoreboardSaveData) mapStorage.loadData(ScoreboardSaveData.class, "scoreboard");

        if (scoreboardsavedata == null)
        {
            scoreboardsavedata = new ScoreboardSaveData();
            mapStorage.setData("scoreboard", scoreboardsavedata);
        }

        scoreboardsavedata.setScoreboard(worldScoreboard);
        ((ServerScoreboard) worldScoreboard).func_96547_a(scoreboardsavedata);
        getWorldBorder().setCenter(worldInfo.getBorderCenterX(), worldInfo.getBorderCenterZ());
        getWorldBorder().setDamageAmount(worldInfo.getBorderDamagePerBlock());
        getWorldBorder().setDamageBuffer(worldInfo.getBorderSafeZone());
        getWorldBorder().setWarningDistance(worldInfo.getBorderWarningDistance());
        getWorldBorder().setWarningTime(worldInfo.getBorderWarningTime());

        if (worldInfo.getBorderLerpTime() > 0L)
        {
            getWorldBorder().setTransition(worldInfo.getBorderSize(), worldInfo.getBorderLerpTarget(), worldInfo.getBorderLerpTime());
        }
        else
        {
            getWorldBorder().setTransition(worldInfo.getBorderSize());
        }

        return this;
    }

    /**
     * Runs a single tick for the world
     */
    public void tick()
    {
        super.tick();

        if (getWorldInfo().isHardcoreModeEnabled() && getDifficulty() != EnumDifficulty.HARD)
        {
            getWorldInfo().setDifficulty(EnumDifficulty.HARD);
        }

        provider.getWorldChunkManager().cleanupCache();

        if (areAllPlayersAsleep())
        {
            if (getGameRules().getBoolean("doDaylightCycle"))
            {
                long i = worldInfo.getWorldTime() + 24000L;
                worldInfo.setWorldTime(i - i % 24000L);
            }

            wakeAllPlayers();
        }

        theProfiler.startSection("mobSpawner");

        if (getGameRules().getBoolean("doMobSpawning") && worldInfo.getTerrainType() != WorldType.DEBUG_WORLD)
        {
            mobSpawner.findChunksForSpawning(this, spawnHostileMobs, spawnPeacefulMobs, worldInfo.getWorldTotalTime() % 400L == 0L);
        }

        theProfiler.endStartSection("chunkSource");
        chunkProvider.unloadQueuedChunks();
        int j = calculateSkylightSubtracted(1.0F);

        if (j != getSkylightSubtracted())
        {
            setSkylightSubtracted(j);
        }

        worldInfo.setWorldTotalTime(worldInfo.getWorldTotalTime() + 1L);

        if (getGameRules().getBoolean("doDaylightCycle"))
        {
            worldInfo.setWorldTime(worldInfo.getWorldTime() + 1L);
        }

        theProfiler.endStartSection("tickPending");
        tickUpdates(false);
        theProfiler.endStartSection("tickBlocks");
        updateBlocks();
        theProfiler.endStartSection("chunkMap");
        thePlayerManager.updatePlayerInstances();
        theProfiler.endStartSection("village");
        villageCollectionObj.tick();
        villageSiege.tick();
        theProfiler.endStartSection("portalForcer");
        worldTeleporter.removeStalePortalLocations(getTotalWorldTime());
        theProfiler.endSection();
        sendQueuedBlockEvents();
    }

    public BiomeGenBase.SpawnListEntry getSpawnListEntryForTypeAt(EnumCreatureType creatureType, BlockPos pos)
    {
        List<BiomeGenBase.SpawnListEntry> list = getChunkProvider().getPossibleCreatures(creatureType, pos);
        return list != null && !list.isEmpty() ? WeightedRandom.getRandomItem(rand, list) : null;
    }

    public boolean canCreatureTypeSpawnHere(EnumCreatureType creatureType, BiomeGenBase.SpawnListEntry spawnListEntry, BlockPos pos)
    {
        List<BiomeGenBase.SpawnListEntry> list = getChunkProvider().getPossibleCreatures(creatureType, pos);
        return list != null && !list.isEmpty() && list.contains(spawnListEntry);
    }

    /**
     * Updates the flag that indicates whether or not all players in the world are sleeping.
     */
    public void updateAllPlayersSleepingFlag()
    {
        allPlayersSleeping = false;

        if (!playerEntities.isEmpty())
        {
            int i = 0;
            int j = 0;

            for (EntityPlayer entityplayer : playerEntities)
            {
                if (entityplayer.isSpectator())
                {
                    ++i;
                }
                else if (entityplayer.isPlayerSleeping())
                {
                    ++j;
                }
            }

            allPlayersSleeping = j > 0 && j >= playerEntities.size() - i;
        }
    }

    protected void wakeAllPlayers()
    {
        allPlayersSleeping = false;

        for (EntityPlayer entityplayer : playerEntities)
        {
            if (entityplayer.isPlayerSleeping())
            {
                entityplayer.wakeUpPlayer(false, false, true);
            }
        }

        resetRainAndThunder();
    }

    private void resetRainAndThunder()
    {
        worldInfo.setRainTime(0);
        worldInfo.setRaining(false);
        worldInfo.setThunderTime(0);
        worldInfo.setThundering(false);
    }

    public boolean areAllPlayersAsleep()
    {
        if (allPlayersSleeping && !isRemote)
        {
            for (EntityPlayer entityplayer : playerEntities)
            {
                if (entityplayer.isSpectator() || !entityplayer.isPlayerFullyAsleep())
                {
                    return false;
                }
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Sets a new spawn location by finding an uncovered block at a random (x,z) location in the chunk.
     */
    public void setInitialSpawnLocation()
    {
        if (worldInfo.getSpawnY() <= 0)
        {
            worldInfo.setSpawnY(func_181545_F() + 1);
        }

        int i = worldInfo.getSpawnX();
        int j = worldInfo.getSpawnZ();
        int k = 0;

        while (getGroundAboveSeaLevel(new BlockPos(i, 0, j)).getMaterial() == Material.air)
        {
            i += rand.nextInt(8) - rand.nextInt(8);
            j += rand.nextInt(8) - rand.nextInt(8);
            ++k;

            if (k == 10000)
            {
                break;
            }
        }

        worldInfo.setSpawnX(i);
        worldInfo.setSpawnZ(j);
    }

    protected void updateBlocks()
    {
        super.updateBlocks();

        if (worldInfo.getTerrainType() == WorldType.DEBUG_WORLD)
        {
            for (ChunkCoordIntPair chunkcoordintpair1 : activeChunkSet)
            {
                getChunkFromChunkCoords(chunkcoordintpair1.chunkXPos, chunkcoordintpair1.chunkZPos).func_150804_b(false);
            }
        }
        else
        {
            int i = 0;
            int j = 0;

            for (ChunkCoordIntPair chunkcoordintpair : activeChunkSet)
            {
                int k = chunkcoordintpair.chunkXPos * 16;
                int l = chunkcoordintpair.chunkZPos * 16;
                theProfiler.startSection("getChunk");
                Chunk chunk = getChunkFromChunkCoords(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos);
                playMoodSoundAndCheckLight(k, l, chunk);
                theProfiler.endStartSection("tickChunk");
                chunk.func_150804_b(false);
                theProfiler.endStartSection("thunder");

                if (rand.nextInt(100000) == 0 && isRaining() && isThundering())
                {
                    updateLCG = updateLCG * 3 + 1013904223;
                    int i1 = updateLCG >> 2;
                    BlockPos blockpos = adjustPosToNearbyEntity(new BlockPos(k + (i1 & 15), 0, l + (i1 >> 8 & 15)));

                    if (canLightningStrike(blockpos))
                    {
                        addWeatherEffect(new EntityLightningBolt(this, blockpos.getX(), blockpos.getY(), blockpos.getZ()));
                    }
                }

                theProfiler.endStartSection("iceandsnow");

                if (rand.nextInt(16) == 0)
                {
                    updateLCG = updateLCG * 3 + 1013904223;
                    int k2 = updateLCG >> 2;
                    BlockPos blockpos2 = getPrecipitationHeight(new BlockPos(k + (k2 & 15), 0, l + (k2 >> 8 & 15)));
                    BlockPos blockpos1 = blockpos2.down();

                    if (canBlockFreezeNoWater(blockpos1))
                    {
                        setBlockState(blockpos1, Blocks.ice.getDefaultState());
                    }

                    if (isRaining() && canSnowAt(blockpos2, true))
                    {
                        setBlockState(blockpos2, Blocks.snow_layer.getDefaultState());
                    }

                    if (isRaining() && getBiomeGenForCoords(blockpos1).canSpawnLightningBolt())
                    {
                        getBlockState(blockpos1).getBlock().fillWithRain(this, blockpos1);
                    }
                }

                theProfiler.endStartSection("tickBlocks");
                int l2 = getGameRules().getInt("randomTickSpeed");

                if (l2 > 0)
                {
                    for (ExtendedBlockStorage extendedblockstorage : chunk.getBlockStorageArray())
                    {
                        if (extendedblockstorage != null && extendedblockstorage.getNeedsRandomTick())
                        {
                            for (int j1 = 0; j1 < l2; ++j1)
                            {
                                updateLCG = updateLCG * 3 + 1013904223;
                                int k1 = updateLCG >> 2;
                                int l1 = k1 & 15;
                                int i2 = k1 >> 8 & 15;
                                int j2 = k1 >> 16 & 15;
                                ++j;
                                IBlockState iblockstate = extendedblockstorage.get(l1, j2, i2);
                                Block block = iblockstate.getBlock();

                                if (block.getTickRandomly())
                                {
                                    ++i;
                                    block.randomTick(this, new BlockPos(l1 + k, j2 + extendedblockstorage.getYLocation(), i2 + l), iblockstate, rand);
                                }
                            }
                        }
                    }
                }

                theProfiler.endSection();
            }
        }
    }

    protected BlockPos adjustPosToNearbyEntity(BlockPos pos)
    {
        BlockPos blockpos = getPrecipitationHeight(pos);
        AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockpos, new BlockPos(blockpos.getX(), getHeight(), blockpos.getZ()))).expand(3.0D, 3.0D, 3.0D);
        List<EntityLivingBase> list = getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb, new Predicate<EntityLivingBase>()
        {
            public boolean apply(EntityLivingBase p_apply_1_)
            {
                return p_apply_1_ != null && p_apply_1_.isEntityAlive() && canSeeSky(p_apply_1_.getPosition());
            }
        });
        return !list.isEmpty() ? list.get(rand.nextInt(list.size())).getPosition() : blockpos;
    }

    public boolean isBlockTickPending(BlockPos pos, Block blockType)
    {
        NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blockType);
        return pendingTickListEntriesThisTick.contains(nextticklistentry);
    }

    public void scheduleUpdate(BlockPos pos, Block blockIn, int delay)
    {
        updateBlockTick(pos, blockIn, delay, 0);
    }

    public void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority)
    {
        NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blockIn);
        int i = 0;

        if (scheduledUpdatesAreImmediate && blockIn.getMaterial() != Material.air)
        {
            if (blockIn.requiresUpdates())
            {
                i = 8;

                if (isAreaLoaded(nextticklistentry.position.add(-i, -i, -i), nextticklistentry.position.add(i, i, i)))
                {
                    IBlockState iblockstate = getBlockState(nextticklistentry.position);

                    if (iblockstate.getBlock().getMaterial() != Material.air && iblockstate.getBlock() == nextticklistentry.getBlock())
                    {
                        iblockstate.getBlock().updateTick(this, nextticklistentry.position, iblockstate, rand);
                    }
                }

                return;
            }

            delay = 1;
        }

        if (isAreaLoaded(pos.add(-i, -i, -i), pos.add(i, i, i)))
        {
            if (blockIn.getMaterial() != Material.air)
            {
                nextticklistentry.setScheduledTime((long)delay + worldInfo.getWorldTotalTime());
                nextticklistentry.setPriority(priority);
            }

            if (!pendingTickListEntriesHashSet.contains(nextticklistentry))
            {
                pendingTickListEntriesHashSet.add(nextticklistentry);
                pendingTickListEntriesTreeSet.add(nextticklistentry);
            }
        }
    }

    public void scheduleBlockUpdate(BlockPos pos, Block blockIn, int delay, int priority)
    {
        NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blockIn);
        nextticklistentry.setPriority(priority);

        if (blockIn.getMaterial() != Material.air)
        {
            nextticklistentry.setScheduledTime((long)delay + worldInfo.getWorldTotalTime());
        }

        if (!pendingTickListEntriesHashSet.contains(nextticklistentry))
        {
            pendingTickListEntriesHashSet.add(nextticklistentry);
            pendingTickListEntriesTreeSet.add(nextticklistentry);
        }
    }

    /**
     * Updates (and cleans up) entities and tile entities
     */
    public void updateEntities()
    {
        if (playerEntities.isEmpty())
        {
            if (updateEntityTick++ >= 1200)
            {
                return;
            }
        }
        else
        {
            resetUpdateEntityTick();
        }

        super.updateEntities();
    }

    /**
     * Resets the updateEntityTick field to 0
     */
    public void resetUpdateEntityTick()
    {
        updateEntityTick = 0;
    }

    /**
     * Runs through the list of updates to run and ticks them
     */
    public boolean tickUpdates(boolean p_72955_1_)
    {
        if (worldInfo.getTerrainType() == WorldType.DEBUG_WORLD)
        {
            return false;
        }
        else
        {
            int i = pendingTickListEntriesTreeSet.size();

            if (i != pendingTickListEntriesHashSet.size())
            {
                throw new IllegalStateException("TickNextTick list out of synch");
            }
            else
            {
                if (i > 1000)
                {
                    i = 1000;
                }

                theProfiler.startSection("cleaning");

                for (int j = 0; j < i; ++j)
                {
                    NextTickListEntry nextticklistentry = pendingTickListEntriesTreeSet.first();

                    if (!p_72955_1_ && nextticklistentry.scheduledTime > worldInfo.getWorldTotalTime())
                    {
                        break;
                    }

                    pendingTickListEntriesTreeSet.remove(nextticklistentry);
                    pendingTickListEntriesHashSet.remove(nextticklistentry);
                    pendingTickListEntriesThisTick.add(nextticklistentry);
                }

                theProfiler.endSection();
                theProfiler.startSection("ticking");
                Iterator<NextTickListEntry> iterator = pendingTickListEntriesThisTick.iterator();

                while (iterator.hasNext())
                {
                    NextTickListEntry nextticklistentry1 = iterator.next();
                    iterator.remove();
                    int k = 0;

                    if (isAreaLoaded(nextticklistentry1.position.add(-k, -k, -k), nextticklistentry1.position.add(k, k, k)))
                    {
                        IBlockState iblockstate = getBlockState(nextticklistentry1.position);

                        if (iblockstate.getBlock().getMaterial() != Material.air && Block.isEqualTo(iblockstate.getBlock(), nextticklistentry1.getBlock()))
                        {
                            try
                            {
                                iblockstate.getBlock().updateTick(this, nextticklistentry1.position, iblockstate, rand);
                            }
                            catch (Throwable throwable)
                            {
                                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while ticking a block");
                                CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being ticked");
                                CrashReportCategory.addBlockInfo(crashreportcategory, nextticklistentry1.position, iblockstate);
                                throw new ReportedException(crashreport);
                            }
                        }
                    }
                    else
                    {
                        scheduleUpdate(nextticklistentry1.position, nextticklistentry1.getBlock(), 0);
                    }
                }

                theProfiler.endSection();
                pendingTickListEntriesThisTick.clear();
                return !pendingTickListEntriesTreeSet.isEmpty();
            }
        }
    }

    public List<NextTickListEntry> getPendingBlockUpdates(Chunk chunkIn, boolean p_72920_2_)
    {
        ChunkCoordIntPair chunkcoordintpair = chunkIn.getChunkCoordIntPair();
        int i = (chunkcoordintpair.chunkXPos << 4) - 2;
        int j = i + 16 + 2;
        int k = (chunkcoordintpair.chunkZPos << 4) - 2;
        int l = k + 16 + 2;
        return func_175712_a(new StructureBoundingBox(i, 0, k, j, 256, l), p_72920_2_);
    }

    public List<NextTickListEntry> func_175712_a(StructureBoundingBox structureBB, boolean p_175712_2_)
    {
        List<NextTickListEntry> list = null;

        for (int i = 0; i < 2; ++i)
        {
            Iterator<NextTickListEntry> iterator;

            if (i == 0)
            {
                iterator = pendingTickListEntriesTreeSet.iterator();
            }
            else
            {
                iterator = pendingTickListEntriesThisTick.iterator();
            }

            while (iterator.hasNext())
            {
                NextTickListEntry nextticklistentry = iterator.next();
                BlockPos blockpos = nextticklistentry.position;

                if (blockpos.getX() >= structureBB.minX && blockpos.getX() < structureBB.maxX && blockpos.getZ() >= structureBB.minZ && blockpos.getZ() < structureBB.maxZ)
                {
                    if (p_175712_2_)
                    {
                        pendingTickListEntriesHashSet.remove(nextticklistentry);
                        iterator.remove();
                    }

                    if (list == null)
                    {
                        list = Lists.newArrayList();
                    }

                    list.add(nextticklistentry);
                }
            }
        }

        return list;
    }

    /**
     * Will update the entity in the world if the chunk the entity is in is currently loaded or its forced to update.
     * Args: entity, forceUpdate
     */
    public void updateEntityWithOptionalForce(Entity entityIn, boolean forceUpdate)
    {
        if (!canSpawnAnimals() && (entityIn instanceof EntityAnimal || entityIn instanceof EntityWaterMob))
        {
            entityIn.setDead();
        }

        if (!canSpawnNPCs() && entityIn instanceof INpc)
        {
            entityIn.setDead();
        }

        super.updateEntityWithOptionalForce(entityIn, forceUpdate);
    }

    private boolean canSpawnNPCs()
    {
        return mcServer.getCanSpawnNPCs();
    }

    private boolean canSpawnAnimals()
    {
        return mcServer.getCanSpawnAnimals();
    }

    /**
     * Creates the chunk provider for this world. Called in the constructor. Retrieves provider from worldProvider?
     */
    protected IChunkProvider createChunkProvider()
    {
        IChunkLoader ichunkloader = saveHandler.getChunkLoader(provider);
        theChunkProviderServer = new ChunkProviderServer(this, ichunkloader, provider.createChunkGenerator());
        return theChunkProviderServer;
    }

    public List<TileEntity> getTileEntitiesIn(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        List<TileEntity> list = Lists.newArrayList();

        for (int i = 0; i < loadedTileEntityList.size(); ++i)
        {
            TileEntity tileentity = loadedTileEntityList.get(i);
            BlockPos blockpos = tileentity.getPos();

            if (blockpos.getX() >= minX && blockpos.getY() >= minY && blockpos.getZ() >= minZ && blockpos.getX() < maxX && blockpos.getY() < maxY && blockpos.getZ() < maxZ)
            {
                list.add(tileentity);
            }
        }

        return list;
    }

    public boolean isBlockModifiable(EntityPlayer player, BlockPos pos)
    {
        return !mcServer.isBlockProtected(this, pos, player) && getWorldBorder().contains(pos);
    }

    public void initialize(WorldSettings settings)
    {
        if (!worldInfo.isInitialized())
        {
            try
            {
                createSpawnPosition(settings);

                if (worldInfo.getTerrainType() == WorldType.DEBUG_WORLD)
                {
                    setDebugWorldSettings();
                }

                super.initialize(settings);
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception initializing level");

                try
                {
                    addWorldInfoToCrashReport(crashreport);
                }
                catch (Throwable var5)
                {
                }

                throw new ReportedException(crashreport);
            }

            worldInfo.setServerInitialized(true);
        }
    }

    private void setDebugWorldSettings()
    {
        worldInfo.setMapFeaturesEnabled(false);
        worldInfo.setAllowCommands(true);
        worldInfo.setRaining(false);
        worldInfo.setThundering(false);
        worldInfo.setCleanWeatherTime(1000000000);
        worldInfo.setWorldTime(6000L);
        worldInfo.setGameType(WorldSettings.GameType.SPECTATOR);
        worldInfo.setHardcore(false);
        worldInfo.setDifficulty(EnumDifficulty.PEACEFUL);
        worldInfo.setDifficultyLocked(true);
        getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
    }

    /**
     * creates a spawn position at random within 256 blocks of 0,0
     */
    private void createSpawnPosition(WorldSettings p_73052_1_)
    {
        if (!provider.canRespawnHere())
        {
            worldInfo.setSpawn(BlockPos.ORIGIN.up(provider.getAverageGroundLevel()));
        }
        else if (worldInfo.getTerrainType() == WorldType.DEBUG_WORLD)
        {
            worldInfo.setSpawn(BlockPos.ORIGIN.up());
        }
        else
        {
            findingSpawnPoint = true;
            WorldChunkManager worldchunkmanager = provider.getWorldChunkManager();
            List<BiomeGenBase> list = worldchunkmanager.getBiomesToSpawnIn();
            Random random = new Random(getSeed());
            BlockPos blockpos = worldchunkmanager.findBiomePosition(0, 0, 256, list, random);
            int i = 0;
            int j = provider.getAverageGroundLevel();
            int k = 0;

            if (blockpos != null)
            {
                i = blockpos.getX();
                k = blockpos.getZ();
            }
            else
            {
                WorldServer.logger.warn("Unable to find spawn biome");
            }

            int l = 0;

            while (!provider.canCoordinateBeSpawn(i, k))
            {
                i += random.nextInt(64) - random.nextInt(64);
                k += random.nextInt(64) - random.nextInt(64);
                ++l;

                if (l == 1000)
                {
                    break;
                }
            }

            worldInfo.setSpawn(new BlockPos(i, j, k));
            findingSpawnPoint = false;

            if (p_73052_1_.isBonusChestEnabled())
            {
                createBonusChest();
            }
        }
    }

    /**
     * Creates the bonus chest in the world.
     */
    protected void createBonusChest()
    {
        WorldGeneratorBonusChest worldgeneratorbonuschest = new WorldGeneratorBonusChest(WorldServer.bonusChestContent, 10);

        for (int i = 0; i < 10; ++i)
        {
            int j = worldInfo.getSpawnX() + rand.nextInt(6) - rand.nextInt(6);
            int k = worldInfo.getSpawnZ() + rand.nextInt(6) - rand.nextInt(6);
            BlockPos blockpos = getTopSolidOrLiquidBlock(new BlockPos(j, 0, k)).up();

            if (worldgeneratorbonuschest.generate(this, rand, blockpos))
            {
                break;
            }
        }
    }

    /**
     * Returns null for anything other than the End
     */
    public BlockPos getSpawnCoordinate()
    {
        return provider.getSpawnCoordinate();
    }

    /**
     * Saves all chunks to disk while updating progress bar.
     */
    public void saveAllChunks(boolean p_73044_1_, IProgressUpdate progressCallback) throws MinecraftException
    {
        if (chunkProvider.canSave())
        {
            if (progressCallback != null)
            {
                progressCallback.displaySavingString("Saving level");
            }

            saveLevel();

            if (progressCallback != null)
            {
                progressCallback.displayLoadingString("Saving chunks");
            }

            chunkProvider.saveChunks(p_73044_1_, progressCallback);

            for (Chunk chunk : Lists.newArrayList(theChunkProviderServer.func_152380_a()))
            {
                if (chunk != null && !thePlayerManager.hasPlayerInstance(chunk.xPosition, chunk.zPosition))
                {
                    theChunkProviderServer.dropChunk(chunk.xPosition, chunk.zPosition);
                }
            }
        }
    }

    /**
     * saves chunk data - currently only called during execution of the Save All command
     */
    public void saveChunkData()
    {
        if (chunkProvider.canSave())
        {
            chunkProvider.saveExtraData();
        }
    }

    /**
     * Saves the chunks to disk.
     */
    protected void saveLevel() throws MinecraftException
    {
        checkSessionLock();
        worldInfo.setBorderSize(getWorldBorder().getDiameter());
        worldInfo.getBorderCenterX(getWorldBorder().getCenterX());
        worldInfo.getBorderCenterZ(getWorldBorder().getCenterZ());
        worldInfo.setBorderSafeZone(getWorldBorder().getDamageBuffer());
        worldInfo.setBorderDamagePerBlock(getWorldBorder().getDamageAmount());
        worldInfo.setBorderWarningDistance(getWorldBorder().getWarningDistance());
        worldInfo.setBorderWarningTime(getWorldBorder().getWarningTime());
        worldInfo.setBorderLerpTarget(getWorldBorder().getTargetSize());
        worldInfo.setBorderLerpTime(getWorldBorder().getTimeUntilTarget());
        saveHandler.saveWorldInfoWithPlayer(worldInfo, mcServer.getConfigurationManager().getHostPlayerData());
        mapStorage.saveAllData();
    }

    protected void onEntityAdded(Entity entityIn)
    {
        super.onEntityAdded(entityIn);
        entitiesById.addKey(entityIn.getEntityId(), entityIn);
        entitiesByUuid.put(entityIn.getUniqueID(), entityIn);
        Entity[] aentity = entityIn.getParts();

        if (aentity != null)
        {
            for (int i = 0; i < aentity.length; ++i)
            {
                entitiesById.addKey(aentity[i].getEntityId(), aentity[i]);
            }
        }
    }

    protected void onEntityRemoved(Entity entityIn)
    {
        super.onEntityRemoved(entityIn);
        entitiesById.removeObject(entityIn.getEntityId());
        entitiesByUuid.remove(entityIn.getUniqueID());
        Entity[] aentity = entityIn.getParts();

        if (aentity != null)
        {
            for (int i = 0; i < aentity.length; ++i)
            {
                entitiesById.removeObject(aentity[i].getEntityId());
            }
        }
    }

    /**
     * adds a lightning bolt to the list of lightning bolts in this world.
     */
    public boolean addWeatherEffect(Entity entityIn)
    {
        if (super.addWeatherEffect(entityIn))
        {
            mcServer.getConfigurationManager().sendToAllNear(entityIn.posX, entityIn.posY, entityIn.posZ, 512.0D, provider.getDimensionId(), new S2CPacketSpawnGlobalEntity(entityIn));
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * sends a Packet 38 (Entity Status) to all tracked players of that entity
     */
    public void setEntityState(Entity entityIn, byte state)
    {
        getEntityTracker().func_151248_b(entityIn, new S19PacketEntityStatus(entityIn, state));
    }

    /**
     * returns a new explosion. Does initiation (at time of writing Explosion is not finished)
     */
    public Explosion newExplosion(Entity entityIn, double x, double y, double z, float strength, boolean isFlaming, boolean isSmoking)
    {
        Explosion explosion = new Explosion(this, entityIn, x, y, z, strength, isFlaming, isSmoking);
        explosion.doExplosionA();
        explosion.doExplosionB(false);

        if (!isSmoking)
        {
            explosion.func_180342_d();
        }

        for (EntityPlayer entityplayer : playerEntities)
        {
            if (entityplayer.getDistanceSq(x, y, z) < 4096.0D)
            {
                ((EntityPlayerMP)entityplayer).playerNetServerHandler.sendPacket(new S27PacketExplosion(x, y, z, strength, explosion.getAffectedBlockPositions(), explosion.getPlayerKnockbackMap().get(entityplayer)));
            }
        }

        return explosion;
    }

    public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam)
    {
        BlockEventData blockeventdata = new BlockEventData(pos, blockIn, eventID, eventParam);

        for (BlockEventData blockeventdata1 : field_147490_S[blockEventCacheIndex])
        {
            if (blockeventdata1.equals(blockeventdata))
            {
                return;
            }
        }

        field_147490_S[blockEventCacheIndex].add(blockeventdata);
    }

    private void sendQueuedBlockEvents()
    {
        while (!field_147490_S[blockEventCacheIndex].isEmpty())
        {
            int i = blockEventCacheIndex;
            blockEventCacheIndex ^= 1;

            for (BlockEventData blockeventdata : field_147490_S[i])
            {
                if (fireBlockEvent(blockeventdata))
                {
                    mcServer.getConfigurationManager().sendToAllNear(blockeventdata.getPosition().getX(), blockeventdata.getPosition().getY(), blockeventdata.getPosition().getZ(), 64.0D, provider.getDimensionId(), new S24PacketBlockAction(blockeventdata.getPosition(), blockeventdata.getBlock(), blockeventdata.getEventID(), blockeventdata.getEventParameter()));
                }
            }

            field_147490_S[i].clear();
        }
    }

    private boolean fireBlockEvent(BlockEventData event)
    {
        IBlockState iblockstate = getBlockState(event.getPosition());
        return iblockstate.getBlock() == event.getBlock() && iblockstate.getBlock().onBlockEventReceived(this, event.getPosition(), iblockstate, event.getEventID(), event.getEventParameter());
    }

    /**
     * Syncs all changes to disk and wait for completion.
     */
    public void flush()
    {
        saveHandler.flush();
    }

    /**
     * Updates all weather states.
     */
    protected void updateWeather()
    {
        boolean flag = isRaining();
        super.updateWeather();

        if (prevRainingStrength != rainingStrength)
        {
            mcServer.getConfigurationManager().sendPacketToAllPlayersInDimension(new S2BPacketChangeGameState(7, rainingStrength), provider.getDimensionId());
        }

        if (prevThunderingStrength != thunderingStrength)
        {
            mcServer.getConfigurationManager().sendPacketToAllPlayersInDimension(new S2BPacketChangeGameState(8, thunderingStrength), provider.getDimensionId());
        }

        if (flag != isRaining())
        {
            if (flag)
            {
                mcServer.getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(2, 0.0F));
            }
            else
            {
                mcServer.getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(1, 0.0F));
            }

            mcServer.getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(7, rainingStrength));
            mcServer.getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(8, thunderingStrength));
        }
    }

    protected int getRenderDistanceChunks()
    {
        return mcServer.getConfigurationManager().getViewDistance();
    }

    public MinecraftServer getMinecraftServer()
    {
        return mcServer;
    }

    /**
     * Gets the EntityTracker
     */
    public EntityTracker getEntityTracker()
    {
        return theEntityTracker;
    }

    public PlayerManager getPlayerManager()
    {
        return thePlayerManager;
    }

    public Teleporter getDefaultTeleporter()
    {
        return worldTeleporter;
    }

    /**
     * Spawns the desired particle and sends the necessary packets to the relevant connected players.
     */
    public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, int numberOfParticles, double p_175739_9_, double p_175739_11_, double p_175739_13_, double p_175739_15_, int... p_175739_17_)
    {
        spawnParticle(particleType, false, xCoord, yCoord, zCoord, numberOfParticles, p_175739_9_, p_175739_11_, p_175739_13_, p_175739_15_, p_175739_17_);
    }

    /**
     * Spawns the desired particle and sends the necessary packets to the relevant connected players.
     */
    public void spawnParticle(EnumParticleTypes particleType, boolean longDistance, double xCoord, double yCoord, double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed, int... p_180505_18_)
    {
        Packet packet = new S2APacketParticles(particleType, longDistance, (float)xCoord, (float)yCoord, (float)zCoord, (float)xOffset, (float)yOffset, (float)zOffset, (float)particleSpeed, numberOfParticles, p_180505_18_);

        for (int i = 0; i < playerEntities.size(); ++i)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP) playerEntities.get(i);
            BlockPos blockpos = entityplayermp.getPosition();
            double d0 = blockpos.distanceSq(xCoord, yCoord, zCoord);

            if (d0 <= 256.0D || longDistance && d0 <= 65536.0D)
            {
                entityplayermp.playerNetServerHandler.sendPacket(packet);
            }
        }
    }

    public Entity getEntityFromUuid(UUID uuid)
    {
        return entitiesByUuid.get(uuid);
    }

    public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule)
    {
        return mcServer.addScheduledTask(runnableToSchedule);
    }

    public boolean isCallingFromMinecraftThread()
    {
        return mcServer.isCallingFromMinecraftThread();
    }

    static class ServerBlockEventList extends ArrayList<BlockEventData>
    {
        private ServerBlockEventList()
        {
        }
    }
}
