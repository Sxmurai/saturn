package net.minecraft.client.multiplayer;

import com.google.common.collect.Sets;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSoundMinecart;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EntityFirework;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveDataMemoryStorage;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import optifine.BlockPosM;
import optifine.Config;
import optifine.DynamicLights;
import optifine.PlayerControllerOF;
import optifine.Reflector;

public class WorldClient extends World
{
    /** The packets that need to be sent to the server. */
    private final NetHandlerPlayClient sendQueue;

    /** The ChunkProviderClient instance */
    private ChunkProviderClient clientChunkProvider;

    /** Contains all entities for this client, both spawned and non-spawned. */
    private final Set entityList = Sets.newHashSet();

    /**
     * Contains all entities for this client that were not spawned due to a non-present chunk. The game will attempt to
     * spawn up to 10 pending entities with each subsequent tick until the spawn queue is empty.
     */
    private final Set entitySpawnQueue = Sets.newHashSet();
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Set previousActiveChunkSet = Sets.newHashSet();
    private static final String __OBFID = "CL_00000882";
    private final BlockPosM randomTickPosM = new BlockPosM(0, 0, 0, 3);
    private boolean playerUpdate = false;

    public WorldClient(NetHandlerPlayClient p_i45063_1_, WorldSettings p_i45063_2_, int p_i45063_3_, EnumDifficulty p_i45063_4_, Profiler p_i45063_5_)
    {
        super(new SaveHandlerMP(), new WorldInfo(p_i45063_2_, "MpServer"), WorldProvider.getProviderForDimension(p_i45063_3_), p_i45063_5_, true);
        sendQueue = p_i45063_1_;
        getWorldInfo().setDifficulty(p_i45063_4_);
        provider.registerWorld(this);
        setSpawnPoint(new BlockPos(8, 64, 8));
        chunkProvider = createChunkProvider();
        mapStorage = new SaveDataMemoryStorage();
        calculateInitialSkylight();
        calculateInitialWeather();
        Reflector.postForgeBusEvent(Reflector.WorldEvent_Load_Constructor, this);

        if (mc.playerController != null && mc.playerController.getClass() == PlayerControllerMP.class)
        {
            mc.playerController = new PlayerControllerOF(mc, p_i45063_1_);
        }
    }

    /**
     * Runs a single tick for the world
     */
    public void tick()
    {
        super.tick();
        setTotalWorldTime(getTotalWorldTime() + 1L);

        if (getGameRules().getBoolean("doDaylightCycle"))
        {
            setWorldTime(getWorldTime() + 1L);
        }

        theProfiler.startSection("reEntryProcessing");

        for (int i = 0; i < 10 && !entitySpawnQueue.isEmpty(); ++i)
        {
            Entity entity = (Entity) entitySpawnQueue.iterator().next();
            entitySpawnQueue.remove(entity);

            if (!loadedEntityList.contains(entity))
            {
                spawnEntityInWorld(entity);
            }
        }

        theProfiler.endStartSection("chunkCache");
        clientChunkProvider.unloadQueuedChunks();
        theProfiler.endStartSection("blocks");
        updateBlocks();
        theProfiler.endSection();
    }

    /**
     * Invalidates an AABB region of blocks from the receive queue, in the event that the block has been modified
     * client-side in the intervening 80 receive ticks.
     */
    public void invalidateBlockReceiveRegion(int p_73031_1_, int p_73031_2_, int p_73031_3_, int p_73031_4_, int p_73031_5_, int p_73031_6_)
    {
    }

    /**
     * Creates the chunk provider for this world. Called in the constructor. Retrieves provider from worldProvider?
     */
    protected IChunkProvider createChunkProvider()
    {
        clientChunkProvider = new ChunkProviderClient(this);
        return clientChunkProvider;
    }

    protected void updateBlocks()
    {
        super.updateBlocks();
        previousActiveChunkSet.retainAll(activeChunkSet);

        if (previousActiveChunkSet.size() == activeChunkSet.size())
        {
            previousActiveChunkSet.clear();
        }

        int i = 0;

        for (ChunkCoordIntPair chunkcoordintpair : activeChunkSet)
        {
            if (!previousActiveChunkSet.contains(chunkcoordintpair))
            {
                int j = chunkcoordintpair.chunkXPos * 16;
                int k = chunkcoordintpair.chunkZPos * 16;
                theProfiler.startSection("getChunk");
                Chunk chunk = getChunkFromChunkCoords(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos);
                playMoodSoundAndCheckLight(j, k, chunk);
                theProfiler.endSection();
                previousActiveChunkSet.add(chunkcoordintpair);
                ++i;

                if (i >= 10)
                {
                    return;
                }
            }
        }
    }

    public void doPreChunk(int p_73025_1_, int p_73025_2_, boolean p_73025_3_)
    {
        if (p_73025_3_)
        {
            clientChunkProvider.loadChunk(p_73025_1_, p_73025_2_);
        }
        else
        {
            clientChunkProvider.unloadChunk(p_73025_1_, p_73025_2_);
        }

        if (!p_73025_3_)
        {
            markBlockRangeForRenderUpdate(p_73025_1_ * 16, 0, p_73025_2_ * 16, p_73025_1_ * 16 + 15, 256, p_73025_2_ * 16 + 15);
        }
    }

    /**
     * Called when an entity is spawned in the world. This includes players.
     */
    public boolean spawnEntityInWorld(Entity entityIn)
    {
        boolean flag = super.spawnEntityInWorld(entityIn);
        entityList.add(entityIn);

        if (!flag)
        {
            entitySpawnQueue.add(entityIn);
        }
        else if (entityIn instanceof EntityMinecart)
        {
            mc.getSoundHandler().playSound(new MovingSoundMinecart((EntityMinecart)entityIn));
        }

        return flag;
    }

    /**
     * Schedule the entity for removal during the next tick. Marks the entity dead in anticipation.
     */
    public void removeEntity(Entity entityIn)
    {
        super.removeEntity(entityIn);
        entityList.remove(entityIn);
    }

    protected void onEntityAdded(Entity entityIn)
    {
        super.onEntityAdded(entityIn);

        entitySpawnQueue.remove(entityIn);
    }

    protected void onEntityRemoved(Entity entityIn)
    {
        super.onEntityRemoved(entityIn);
        boolean flag = false;

        if (entityList.contains(entityIn))
        {
            if (entityIn.isEntityAlive())
            {
                entitySpawnQueue.add(entityIn);
                flag = true;
            }
            else
            {
                entityList.remove(entityIn);
            }
        }
    }

    /**
     * Add an ID to Entity mapping to entityHashSet
     */
    public void addEntityToWorld(int p_73027_1_, Entity p_73027_2_)
    {
        Entity entity = getEntityByID(p_73027_1_);

        if (entity != null)
        {
            removeEntity(entity);
        }

        entityList.add(p_73027_2_);
        p_73027_2_.setEntityId(p_73027_1_);

        if (!spawnEntityInWorld(p_73027_2_))
        {
            entitySpawnQueue.add(p_73027_2_);
        }

        entitiesById.addKey(p_73027_1_, p_73027_2_);
    }

    /**
     * Returns the Entity with the given ID, or null if it doesn't exist in this World.
     */
    public Entity getEntityByID(int id)
    {
        return id == mc.thePlayer.getEntityId() ? mc.thePlayer : super.getEntityByID(id);
    }

    public Entity removeEntityFromWorld(int p_73028_1_)
    {
        Entity entity = entitiesById.removeObject(p_73028_1_);

        if (entity != null)
        {
            entityList.remove(entity);
            removeEntity(entity);
        }

        return entity;
    }

    public boolean invalidateRegionAndSetBlock(BlockPos p_180503_1_, IBlockState p_180503_2_)
    {
        int i = p_180503_1_.getX();
        int j = p_180503_1_.getY();
        int k = p_180503_1_.getZ();
        invalidateBlockReceiveRegion(i, j, k, i, j, k);
        return super.setBlockState(p_180503_1_, p_180503_2_, 3);
    }

    /**
     * If on MP, sends a quitting packet.
     */
    public void sendQuittingDisconnectingPacket()
    {
        sendQueue.getNetworkManager().closeChannel(new ChatComponentText("Quitting"));
    }

    /**
     * Updates all weather states.
     */
    protected void updateWeather()
    {
    }

    protected int getRenderDistanceChunks()
    {
        return mc.gameSettings.renderDistanceChunks;
    }

    public void doVoidFogParticles(int p_73029_1_, int p_73029_2_, int p_73029_3_)
    {
        byte b0 = 16;
        Random random = new Random();
        ItemStack itemstack = mc.thePlayer.getHeldItem();
        boolean flag = mc.playerController.getCurrentGameType() == WorldSettings.GameType.CREATIVE && itemstack != null && Block.getBlockFromItem(itemstack.getItem()) == Blocks.barrier;
        BlockPosM blockposm = randomTickPosM;

        for (int i = 0; i < 1000; ++i)
        {
            int j = p_73029_1_ + rand.nextInt(b0) - rand.nextInt(b0);
            int k = p_73029_2_ + rand.nextInt(b0) - rand.nextInt(b0);
            int l = p_73029_3_ + rand.nextInt(b0) - rand.nextInt(b0);
            blockposm.setXyz(j, k, l);
            IBlockState iblockstate = getBlockState(blockposm);
            iblockstate.getBlock().randomDisplayTick(this, blockposm, iblockstate, random);

            if (flag && iblockstate.getBlock() == Blocks.barrier)
            {
                spawnParticle(EnumParticleTypes.BARRIER, (float)j + 0.5F, (float)k + 0.5F, (float)l + 0.5F, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    /**
     * also releases skins.
     */
    public void removeAllEntities()
    {
        loadedEntityList.removeAll(unloadedEntityList);

        for (int i = 0; i < unloadedEntityList.size(); ++i)
        {
            Entity entity = unloadedEntityList.get(i);
            int j = entity.chunkCoordX;
            int k = entity.chunkCoordZ;

            if (entity.addedToChunk && isChunkLoaded(j, k, true))
            {
                getChunkFromChunkCoords(j, k).removeEntity(entity);
            }
        }

        for (int l = 0; l < unloadedEntityList.size(); ++l)
        {
            onEntityRemoved(unloadedEntityList.get(l));
        }

        unloadedEntityList.clear();

        for (int i1 = 0; i1 < loadedEntityList.size(); ++i1)
        {
            Entity entity1 = loadedEntityList.get(i1);

            if (entity1.ridingEntity != null)
            {
                if (!entity1.ridingEntity.isDead && entity1.ridingEntity.riddenByEntity == entity1)
                {
                    continue;
                }

                entity1.ridingEntity.riddenByEntity = null;
                entity1.ridingEntity = null;
            }

            if (entity1.isDead)
            {
                int j1 = entity1.chunkCoordX;
                int k1 = entity1.chunkCoordZ;

                if (entity1.addedToChunk && isChunkLoaded(j1, k1, true))
                {
                    getChunkFromChunkCoords(j1, k1).removeEntity(entity1);
                }

                loadedEntityList.remove(i1--);
                onEntityRemoved(entity1);
            }
        }
    }

    /**
     * Adds some basic stats of the world to the given crash report.
     */
    public CrashReportCategory addWorldInfoToCrashReport(CrashReport report)
    {
        CrashReportCategory crashreportcategory = super.addWorldInfoToCrashReport(report);
        crashreportcategory.addCrashSectionCallable("Forced entities", new Callable()
        {
            private static final String __OBFID = "CL_00000883";
            public String call()
            {
                return entityList.size() + " total; " + entityList;
            }
        });
        crashreportcategory.addCrashSectionCallable("Retry entities", new Callable()
        {
            private static final String __OBFID = "CL_00000884";
            public String call()
            {
                return entitySpawnQueue.size() + " total; " + entitySpawnQueue;
            }
        });
        crashreportcategory.addCrashSectionCallable("Server brand", new Callable()
        {
            private static final String __OBFID = "CL_00000885";
            public String call() throws Exception
            {
                return mc.thePlayer.getClientBrand();
            }
        });
        crashreportcategory.addCrashSectionCallable("Server type", new Callable()
        {
            private static final String __OBFID = "CL_00000886";
            public String call() throws Exception
            {
                return mc.getIntegratedServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server";
            }
        });
        return crashreportcategory;
    }

    /**
     * Plays a sound at the specified position.
     */
    public void playSoundAtPos(BlockPos p_175731_1_, String p_175731_2_, float p_175731_3_, float p_175731_4_, boolean p_175731_5_)
    {
        playSound((double)p_175731_1_.getX() + 0.5D, (double)p_175731_1_.getY() + 0.5D, (double)p_175731_1_.getZ() + 0.5D, p_175731_2_, p_175731_3_, p_175731_4_, p_175731_5_);
    }

    /**
     * par8 is loudness, all pars passed to minecraftInstance.sndManager.playSound
     */
    public void playSound(double x, double y, double z, String soundName, float volume, float pitch, boolean distanceDelay)
    {
        double d0 = mc.getRenderViewEntity().getDistanceSq(x, y, z);
        PositionedSoundRecord positionedsoundrecord = new PositionedSoundRecord(new ResourceLocation(soundName), volume, pitch, (float)x, (float)y, (float)z);

        if (distanceDelay && d0 > 100.0D)
        {
            double d1 = Math.sqrt(d0) / 40.0D;
            mc.getSoundHandler().playDelayedSound(positionedsoundrecord, (int)(d1 * 20.0D));
        }
        else
        {
            mc.getSoundHandler().playSound(positionedsoundrecord);
        }
    }

    public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, NBTTagCompound compund)
    {
        mc.effectRenderer.addEffect(new EntityFirework.StarterFX(this, x, y, z, motionX, motionY, motionZ, mc.effectRenderer, compund));
    }

    public void setWorldScoreboard(Scoreboard p_96443_1_)
    {
        worldScoreboard = p_96443_1_;
    }

    /**
     * Sets the world time.
     */
    public void setWorldTime(long time)
    {
        if (time < 0L)
        {
            time = -time;
            getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
        }
        else
        {
            getGameRules().setOrCreateGameRule("doDaylightCycle", "true");
        }

        super.setWorldTime(time);
    }

    public int getCombinedLight(BlockPos pos, int lightValue)
    {
        int i = super.getCombinedLight(pos, lightValue);

        if (Config.isDynamicLights())
        {
            i = DynamicLights.getCombinedLight(pos, i);
        }

        return i;
    }

    /**
     * Sets the block state at a given location. Flag 1 will cause a block update. Flag 2 will send the change to
     * clients (you almost always want this). Flag 4 prevents the block from being re-rendered, if this is a client
     * world. Flags can be added together.
     */
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags)
    {
        playerUpdate = isPlayerActing();
        boolean flag = super.setBlockState(pos, newState, flags);
        playerUpdate = false;
        return flag;
    }

    private boolean isPlayerActing()
    {
        if (mc.playerController instanceof PlayerControllerOF)
        {
            PlayerControllerOF playercontrollerof = (PlayerControllerOF) mc.playerController;
            return playercontrollerof.isActing();
        }
        else
        {
            return false;
        }
    }

    public boolean isPlayerUpdate()
    {
        return playerUpdate;
    }
}
