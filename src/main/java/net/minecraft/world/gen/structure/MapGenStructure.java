package net.minecraft.world.gen.structure;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Vec3i;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.storage.MapStorage;
import optifine.Reflector;

public abstract class MapGenStructure extends MapGenBase
{
    private MapGenStructureData structureData;

    /**
     * Used to store a list of all structures that have been recursively generated. Used so that during recursive
     * generation, the structure generator can avoid generating structures that intersect ones that have already been
     * placed.
     */
    protected Map structureMap = Maps.newHashMap();
    private static final String __OBFID = "CL_00000505";
    private final LongHashMap structureLongMap = new LongHashMap();

    public abstract String getStructureName();

    /**
     * Recursively called by generate()
     */
    protected final void recursiveGenerate(World worldIn, final int chunkX, final int chunkZ, int p_180701_4_, int p_180701_5_, ChunkPrimer chunkPrimerIn)
    {
        func_143027_a(worldIn);

        if (!structureLongMap.containsItem(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ)))
        {
            rand.nextInt();

            try
            {
                if (canSpawnStructureAtCoords(chunkX, chunkZ))
                {
                    StructureStart structurestart = getStructureStart(chunkX, chunkZ);
                    structureMap.put(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ)), structurestart);
                    structureLongMap.add(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ), structurestart);
                    func_143026_a(chunkX, chunkZ, structurestart);
                }
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception preparing structure feature");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Feature being prepared");
                crashreportcategory.addCrashSectionCallable("Is feature chunk", new Callable()
                {
                    private static final String __OBFID = "CL_00000506";
                    public String call() throws Exception
                    {
                        return canSpawnStructureAtCoords(chunkX, chunkZ) ? "True" : "False";
                    }
                });
                crashreportcategory.addCrashSection("Chunk location", String.format("%d,%d", Integer.valueOf(chunkX), Integer.valueOf(chunkZ)));
                crashreportcategory.addCrashSectionCallable("Chunk pos hash", new Callable()
                {
                    private static final String __OBFID = "CL_00000507";
                    public String call() throws Exception
                    {
                        return String.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ));
                    }
                });
                crashreportcategory.addCrashSectionCallable("Structure type", new Callable()
                {
                    private static final String __OBFID = "CL_00000508";
                    public String call() throws Exception
                    {
                        return MapGenStructure.this.getClass().getCanonicalName();
                    }
                });
                throw new ReportedException(crashreport);
            }
        }
    }

    public boolean generateStructure(World worldIn, Random randomIn, ChunkCoordIntPair chunkCoord)
    {
        func_143027_a(worldIn);
        int i = (chunkCoord.chunkXPos << 4) + 8;
        int j = (chunkCoord.chunkZPos << 4) + 8;
        boolean flag = false;

        for (Object structurestart0 : structureMap.values())
        {
            StructureStart structurestart = (StructureStart) structurestart0;

            if (structurestart.isSizeableStructure() && structurestart.func_175788_a(chunkCoord) && structurestart.getBoundingBox().intersectsWith(i, j, i + 15, j + 15))
            {
                structurestart.generateStructure(worldIn, randomIn, new StructureBoundingBox(i, j, i + 15, j + 15));
                structurestart.func_175787_b(chunkCoord);
                flag = true;
                func_143026_a(structurestart.getChunkPosX(), structurestart.getChunkPosZ(), structurestart);
            }
        }

        return flag;
    }

    public boolean func_175795_b(BlockPos pos)
    {
        func_143027_a(worldObj);
        return func_175797_c(pos) != null;
    }

    protected StructureStart func_175797_c(BlockPos pos)
    {
        label24:

        for (Object structurestart0 : structureMap.values())
        {
            StructureStart structurestart = (StructureStart)structurestart0;

            if (structurestart.isSizeableStructure() && structurestart.getBoundingBox().isVecInside(pos))
            {
                Iterator iterator = structurestart.getComponents().iterator();

                while (true)
                {
                    if (!iterator.hasNext())
                    {
                        continue label24;
                    }

                    StructureComponent structurecomponent = (StructureComponent)iterator.next();

                    if (structurecomponent.getBoundingBox().isVecInside(pos))
                    {
                        break;
                    }
                }

                return structurestart;
            }
        }

        return null;
    }

    public boolean func_175796_a(World worldIn, BlockPos pos)
    {
        func_143027_a(worldIn);

        for (Object structurestart0 : structureMap.values())
        {
            StructureStart structurestart = (StructureStart)structurestart0;

            if (structurestart.isSizeableStructure() && structurestart.getBoundingBox().isVecInside(pos))
            {
                return true;
            }
        }

        return false;
    }

    public BlockPos getClosestStrongholdPos(World worldIn, BlockPos pos)
    {
        worldObj = worldIn;
        func_143027_a(worldIn);
        rand.setSeed(worldIn.getSeed());
        long i = rand.nextLong();
        long j = rand.nextLong();
        long k = (long)(pos.getX() >> 4) * i;
        long l = (long)(pos.getZ() >> 4) * j;
        rand.setSeed(k ^ l ^ worldIn.getSeed());
        recursiveGenerate(worldIn, pos.getX() >> 4, pos.getZ() >> 4, 0, 0, null);
        double d0 = Double.MAX_VALUE;
        BlockPos blockpos = null;

        for (Object structurestart0 : structureMap.values())
        {
            StructureStart structurestart = (StructureStart)structurestart0;

            if (structurestart.isSizeableStructure())
            {
                StructureComponent structurecomponent = structurestart.getComponents().get(0);
                BlockPos blockpos1 = structurecomponent.getBoundingBoxCenter();
                double d1 = blockpos1.distanceSq(pos);

                if (d1 < d0)
                {
                    d0 = d1;
                    blockpos = blockpos1;
                }
            }
        }

        if (blockpos != null)
        {
            return blockpos;
        }
        else
        {
            List list = getCoordList();

            if (list != null)
            {
                BlockPos blockpos3 = null;

                for (Object blockpos2 : list)
                {
                    double d2 = ((BlockPos) blockpos2).distanceSq(pos);

                    if (d2 < d0)
                    {
                        d0 = d2;
                        blockpos3 = (BlockPos) blockpos2;
                    }
                }

                return blockpos3;
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * Returns a list of other locations at which the structure generation has been run, or null if not relevant to this
     * structure generator.
     */
    protected List getCoordList()
    {
        return null;
    }

    private void func_143027_a(World worldIn)
    {
        if (structureData == null)
        {
            if (Reflector.ForgeWorld_getPerWorldStorage.exists())
            {
                MapStorage mapstorage = (MapStorage)Reflector.call(worldIn, Reflector.ForgeWorld_getPerWorldStorage, new Object[0]);
                structureData = (MapGenStructureData)mapstorage.loadData(MapGenStructureData.class, getStructureName());
            }
            else
            {
                structureData = (MapGenStructureData)worldIn.loadItemData(MapGenStructureData.class, getStructureName());
            }

            if (structureData == null)
            {
                structureData = new MapGenStructureData(getStructureName());

                if (Reflector.ForgeWorld_getPerWorldStorage.exists())
                {
                    MapStorage mapstorage1 = (MapStorage)Reflector.call(worldIn, Reflector.ForgeWorld_getPerWorldStorage, new Object[0]);
                    mapstorage1.setData(getStructureName(), structureData);
                }
                else
                {
                    worldIn.setItemData(getStructureName(), structureData);
                }
            }
            else
            {
                NBTTagCompound nbttagcompound1 = structureData.getTagCompound();

                for (String s : nbttagcompound1.getKeySet())
                {
                    NBTBase nbtbase = nbttagcompound1.getTag(s);

                    if (nbtbase.getId() == 10)
                    {
                        NBTTagCompound nbttagcompound = (NBTTagCompound)nbtbase;

                        if (nbttagcompound.hasKey("ChunkX") && nbttagcompound.hasKey("ChunkZ"))
                        {
                            int i = nbttagcompound.getInteger("ChunkX");
                            int j = nbttagcompound.getInteger("ChunkZ");
                            StructureStart structurestart = MapGenStructureIO.getStructureStart(nbttagcompound, worldIn);

                            if (structurestart != null)
                            {
                                structureMap.put(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(i, j)), structurestart);
                                structureLongMap.add(ChunkCoordIntPair.chunkXZ2Int(i, j), structurestart);
                            }
                        }
                    }
                }
            }
        }
    }

    private void func_143026_a(int p_143026_1_, int p_143026_2_, StructureStart start)
    {
        structureData.writeInstance(start.writeStructureComponentsToNBT(p_143026_1_, p_143026_2_), p_143026_1_, p_143026_2_);
        structureData.markDirty();
    }

    protected abstract boolean canSpawnStructureAtCoords(int chunkX, int chunkZ);

    protected abstract StructureStart getStructureStart(int chunkX, int chunkZ);
}
