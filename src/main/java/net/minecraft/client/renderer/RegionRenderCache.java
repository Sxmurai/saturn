package net.minecraft.client.renderer;

import java.util.ArrayDeque;
import java.util.Arrays;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import optifine.Config;
import optifine.DynamicLights;

public class RegionRenderCache extends ChunkCache
{
    private static final IBlockState DEFAULT_STATE = Blocks.air.getDefaultState();
    private final BlockPos position;
    private final int[] combinedLights;
    private final IBlockState[] blockStates;
    private static final String __OBFID = "CL_00002565";
    private static final ArrayDeque<int[]> cacheLights = new ArrayDeque();
    private static final ArrayDeque<IBlockState[]> cacheStates = new ArrayDeque();
    private static final int maxCacheSize = Config.limit(Runtime.getRuntime().availableProcessors(), 1, 32);

    public RegionRenderCache(World worldIn, BlockPos posFromIn, BlockPos posToIn, int subIn)
    {
        super(worldIn, posFromIn, posToIn, subIn);
        position = posFromIn.subtract(new Vec3i(subIn, subIn, subIn));
        boolean flag = true;
        combinedLights = RegionRenderCache.allocateLights(8000);
        Arrays.fill(combinedLights, - 1);
        blockStates = RegionRenderCache.allocateStates(8000);
    }

    public TileEntity getTileEntity(BlockPos pos)
    {
        int i = (pos.getX() >> 4) - chunkX;
        int j = (pos.getZ() >> 4) - chunkZ;
        return chunkArray[i][j].getTileEntity(pos, Chunk.EnumCreateEntityType.QUEUED);
    }

    public int getCombinedLight(BlockPos pos, int lightValue)
    {
        int i = getPositionIndex(pos);
        int j = combinedLights[i];

        if (j == -1)
        {
            j = super.getCombinedLight(pos, lightValue);

            if (Config.isDynamicLights() && !getBlockState(pos).getBlock().isOpaqueCube())
            {
                j = DynamicLights.getCombinedLight(pos, j);
            }

            combinedLights[i] = j;
        }

        return j;
    }

    public IBlockState getBlockState(BlockPos pos)
    {
        int i = getPositionIndex(pos);
        IBlockState iblockstate = blockStates[i];

        if (iblockstate == null)
        {
            iblockstate = getBlockStateRaw(pos);
            blockStates[i] = iblockstate;
        }

        return iblockstate;
    }

    private IBlockState getBlockStateRaw(BlockPos pos)
    {
        if (pos.getY() >= 0 && pos.getY() < 256)
        {
            int i = (pos.getX() >> 4) - chunkX;
            int j = (pos.getZ() >> 4) - chunkZ;
            return chunkArray[i][j].getBlockState(pos);
        }
        else
        {
            return RegionRenderCache.DEFAULT_STATE;
        }
    }

    private int getPositionIndex(BlockPos p_175630_1_)
    {
        int i = p_175630_1_.getX() - position.getX();
        int j = p_175630_1_.getY() - position.getY();
        int k = p_175630_1_.getZ() - position.getZ();
        return i * 400 + k * 20 + j;
    }

    public void freeBuffers()
    {
        RegionRenderCache.freeLights(combinedLights);
        RegionRenderCache.freeStates(blockStates);
    }

    private static int[] allocateLights(int p_allocateLights_0_)
    {
        synchronized (RegionRenderCache.cacheLights)
        {
            int[] aint = RegionRenderCache.cacheLights.pollLast();

            if (aint == null || aint.length < p_allocateLights_0_)
            {
                aint = new int[p_allocateLights_0_];
            }

            return aint;
        }
    }

    public static void freeLights(int[] p_freeLights_0_)
    {
        synchronized (RegionRenderCache.cacheLights)
        {
            if (RegionRenderCache.cacheLights.size() < RegionRenderCache.maxCacheSize)
            {
                RegionRenderCache.cacheLights.add(p_freeLights_0_);
            }
        }
    }

    private static IBlockState[] allocateStates(int p_allocateStates_0_)
    {
        synchronized (RegionRenderCache.cacheStates)
        {
            IBlockState[] aiblockstate = RegionRenderCache.cacheStates.pollLast();

            if (aiblockstate != null && aiblockstate.length >= p_allocateStates_0_)
            {
                Arrays.fill(aiblockstate, null);
            }
            else
            {
                aiblockstate = new IBlockState[p_allocateStates_0_];
            }

            return aiblockstate;
        }
    }

    public static void freeStates(IBlockState[] p_freeStates_0_)
    {
        synchronized (RegionRenderCache.cacheStates)
        {
            if (RegionRenderCache.cacheStates.size() < RegionRenderCache.maxCacheSize)
            {
                RegionRenderCache.cacheStates.add(p_freeStates_0_);
            }
        }
    }
}
