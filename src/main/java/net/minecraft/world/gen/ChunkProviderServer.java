package net.minecraft.world.gen;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkProviderServer implements IChunkProvider
{
    private static final Logger logger = LogManager.getLogger();
    private final Set<Long> droppedChunksSet = Collections.<Long>newSetFromMap(new ConcurrentHashMap());

    /** a dummy chunk, returned in place of an actual chunk. */
    private final Chunk dummyChunk;

    /**
     * chunk generator object. Calls to load nonexistent chunks are forwarded to this object.
     */
    private final IChunkProvider serverChunkGenerator;
    private final IChunkLoader chunkLoader;

    /**
     * if set, this flag forces a request to load a chunk to load the chunk rather than defaulting to the dummy if
     * possible
     */
    public boolean chunkLoadOverride = true;
    private final LongHashMap id2ChunkMap = new LongHashMap();
    private final List<Chunk> loadedChunks = Lists.newArrayList();
    private final WorldServer worldObj;

    public ChunkProviderServer(WorldServer p_i1520_1_, IChunkLoader p_i1520_2_, IChunkProvider p_i1520_3_)
    {
        dummyChunk = new EmptyChunk(p_i1520_1_, 0, 0);
        worldObj = p_i1520_1_;
        chunkLoader = p_i1520_2_;
        serverChunkGenerator = p_i1520_3_;
    }

    /**
     * Checks to see if a chunk exists at x, z
     */
    public boolean chunkExists(int x, int z)
    {
        return id2ChunkMap.containsItem(ChunkCoordIntPair.chunkXZ2Int(x, z));
    }

    public List<Chunk> func_152380_a()
    {
        return loadedChunks;
    }

    public void dropChunk(int p_73241_1_, int p_73241_2_)
    {
        if (worldObj.provider.canRespawnHere())
        {
            if (!worldObj.isSpawnChunk(p_73241_1_, p_73241_2_))
            {
                droppedChunksSet.add(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(p_73241_1_, p_73241_2_)));
            }
        }
        else
        {
            droppedChunksSet.add(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(p_73241_1_, p_73241_2_)));
        }
    }

    /**
     * marks all chunks for unload, ignoring those near the spawn
     */
    public void unloadAllChunks()
    {
        for (Chunk chunk : loadedChunks)
        {
            dropChunk(chunk.xPosition, chunk.zPosition);
        }
    }

    /**
     * loads or generates the chunk at the chunk location specified
     */
    public Chunk loadChunk(int p_73158_1_, int p_73158_2_)
    {
        long i = ChunkCoordIntPair.chunkXZ2Int(p_73158_1_, p_73158_2_);
        droppedChunksSet.remove(Long.valueOf(i));
        Chunk chunk = (Chunk) id2ChunkMap.getValueByKey(i);

        if (chunk == null)
        {
            chunk = loadChunkFromFile(p_73158_1_, p_73158_2_);

            if (chunk == null)
            {
                if (serverChunkGenerator == null)
                {
                    chunk = dummyChunk;
                }
                else
                {
                    try
                    {
                        chunk = serverChunkGenerator.provideChunk(p_73158_1_, p_73158_2_);
                    }
                    catch (Throwable throwable)
                    {
                        CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception generating new chunk");
                        CrashReportCategory crashreportcategory = crashreport.makeCategory("Chunk to be generated");
                        crashreportcategory.addCrashSection("Location", String.format("%d,%d", Integer.valueOf(p_73158_1_), Integer.valueOf(p_73158_2_)));
                        crashreportcategory.addCrashSection("Position hash", Long.valueOf(i));
                        crashreportcategory.addCrashSection("Generator", serverChunkGenerator.makeString());
                        throw new ReportedException(crashreport);
                    }
                }
            }

            id2ChunkMap.add(i, chunk);
            loadedChunks.add(chunk);
            chunk.onChunkLoad();
            chunk.populateChunk(this, this, p_73158_1_, p_73158_2_);
        }

        return chunk;
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    public Chunk provideChunk(int x, int z)
    {
        Chunk chunk = (Chunk) id2ChunkMap.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x, z));
        return chunk == null ? (!worldObj.isFindingSpawnPoint() && !chunkLoadOverride ? dummyChunk : loadChunk(x, z)) : chunk;
    }

    private Chunk loadChunkFromFile(int x, int z)
    {
        if (chunkLoader == null)
        {
            return null;
        }
        else
        {
            try
            {
                Chunk chunk = chunkLoader.loadChunk(worldObj, x, z);

                if (chunk != null)
                {
                    chunk.setLastSaveTime(worldObj.getTotalWorldTime());

                    if (serverChunkGenerator != null)
                    {
                        serverChunkGenerator.recreateStructures(chunk, x, z);
                    }
                }

                return chunk;
            }
            catch (Exception exception)
            {
                ChunkProviderServer.logger.error("Couldn't load chunk", exception);
                return null;
            }
        }
    }

    private void saveChunkExtraData(Chunk p_73243_1_)
    {
        if (chunkLoader != null)
        {
            try
            {
                chunkLoader.saveExtraChunkData(worldObj, p_73243_1_);
            }
            catch (Exception exception)
            {
                ChunkProviderServer.logger.error("Couldn't save entities", exception);
            }
        }
    }

    private void saveChunkData(Chunk p_73242_1_)
    {
        if (chunkLoader != null)
        {
            try
            {
                p_73242_1_.setLastSaveTime(worldObj.getTotalWorldTime());
                chunkLoader.saveChunk(worldObj, p_73242_1_);
            }
            catch (IOException ioexception)
            {
                ChunkProviderServer.logger.error("Couldn't save chunk", ioexception);
            }
            catch (MinecraftException minecraftexception)
            {
                ChunkProviderServer.logger.error("Couldn't save chunk; already in use by another instance of Minecraft?", minecraftexception);
            }
        }
    }

    /**
     * Populates chunk with ores etc etc
     */
    public void populate(IChunkProvider p_73153_1_, int p_73153_2_, int p_73153_3_)
    {
        Chunk chunk = provideChunk(p_73153_2_, p_73153_3_);

        if (!chunk.isTerrainPopulated())
        {
            chunk.func_150809_p();

            if (serverChunkGenerator != null)
            {
                serverChunkGenerator.populate(p_73153_1_, p_73153_2_, p_73153_3_);
                chunk.setChunkModified();
            }
        }
    }

    public boolean func_177460_a(IChunkProvider p_177460_1_, Chunk p_177460_2_, int p_177460_3_, int p_177460_4_)
    {
        if (serverChunkGenerator != null && serverChunkGenerator.func_177460_a(p_177460_1_, p_177460_2_, p_177460_3_, p_177460_4_))
        {
            Chunk chunk = provideChunk(p_177460_3_, p_177460_4_);
            chunk.setChunkModified();
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * Return true if all chunks have been saved.
     */
    public boolean saveChunks(boolean p_73151_1_, IProgressUpdate progressCallback)
    {
        int i = 0;
        List<Chunk> list = Lists.newArrayList(loadedChunks);

        for (int j = 0; j < list.size(); ++j)
        {
            Chunk chunk = list.get(j);

            if (p_73151_1_)
            {
                saveChunkExtraData(chunk);
            }

            if (chunk.needsSaving(p_73151_1_))
            {
                saveChunkData(chunk);
                chunk.setModified(false);
                ++i;

                if (i == 24 && !p_73151_1_)
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Save extra data not associated with any Chunk.  Not saved during autosave, only during world unload.  Currently
     * unimplemented.
     */
    public void saveExtraData()
    {
        if (chunkLoader != null)
        {
            chunkLoader.saveExtraData();
        }
    }

    /**
     * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
     */
    public boolean unloadQueuedChunks()
    {
        if (!worldObj.disableLevelSaving)
        {
            for (int i = 0; i < 100; ++i)
            {
                if (!droppedChunksSet.isEmpty())
                {
                    Long olong = droppedChunksSet.iterator().next();
                    Chunk chunk = (Chunk) id2ChunkMap.getValueByKey(olong.longValue());

                    if (chunk != null)
                    {
                        chunk.onChunkUnload();
                        saveChunkData(chunk);
                        saveChunkExtraData(chunk);
                        id2ChunkMap.remove(olong.longValue());
                        loadedChunks.remove(chunk);
                    }

                    droppedChunksSet.remove(olong);
                }
            }

            if (chunkLoader != null)
            {
                chunkLoader.chunkTick();
            }
        }

        return serverChunkGenerator.unloadQueuedChunks();
    }

    /**
     * Returns if the IChunkProvider supports saving.
     */
    public boolean canSave()
    {
        return !worldObj.disableLevelSaving;
    }

    /**
     * Converts the instance data to a readable string.
     */
    public String makeString()
    {
        return "ServerChunkCache: " + id2ChunkMap.getNumHashElements() + " Drop: " + droppedChunksSet.size();
    }

    public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
        return serverChunkGenerator.getPossibleCreatures(creatureType, pos);
    }

    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position)
    {
        return serverChunkGenerator.getStrongholdGen(worldIn, structureName, position);
    }

    public int getLoadedChunkCount()
    {
        return id2ChunkMap.getNumHashElements();
    }

    public void recreateStructures(Chunk p_180514_1_, int p_180514_2_, int p_180514_3_)
    {
    }

    public Chunk provideChunk(BlockPos blockPosIn)
    {
        return provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
    }
}
