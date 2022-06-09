package net.minecraft.client.renderer;

import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ViewFrustum
{
    protected final RenderGlobal renderGlobal;
    protected final World world;
    protected int countChunksY;
    protected int countChunksX;
    protected int countChunksZ;
    public RenderChunk[] renderChunks;
    private static final String __OBFID = "CL_00002531";

    public ViewFrustum(World worldIn, int renderDistanceChunks, RenderGlobal p_i46246_3_, IRenderChunkFactory renderChunkFactory)
    {
        renderGlobal = p_i46246_3_;
        world = worldIn;
        setCountChunksXYZ(renderDistanceChunks);
        createRenderChunks(renderChunkFactory);
    }

    protected void createRenderChunks(IRenderChunkFactory renderChunkFactory)
    {
        int i = countChunksX * countChunksY * countChunksZ;
        renderChunks = new RenderChunk[i];
        int j = 0;

        for (int k = 0; k < countChunksX; ++k)
        {
            for (int l = 0; l < countChunksY; ++l)
            {
                for (int i1 = 0; i1 < countChunksZ; ++i1)
                {
                    int j1 = (i1 * countChunksY + l) * countChunksX + k;
                    BlockPos blockpos = new BlockPos(k * 16, l * 16, i1 * 16);
                    renderChunks[j1] = renderChunkFactory.makeRenderChunk(world, renderGlobal, blockpos, j++);
                }
            }
        }
    }

    public void deleteGlResources()
    {
        for (RenderChunk renderchunk : renderChunks)
        {
            renderchunk.deleteGlResources();
        }
    }

    protected void setCountChunksXYZ(int renderDistanceChunks)
    {
        int i = renderDistanceChunks * 2 + 1;
        countChunksX = i;
        countChunksY = 16;
        countChunksZ = i;
    }

    public void updateChunkPositions(double viewEntityX, double viewEntityZ)
    {
        int i = MathHelper.floor_double(viewEntityX) - 8;
        int j = MathHelper.floor_double(viewEntityZ) - 8;
        int k = countChunksX * 16;

        for (int l = 0; l < countChunksX; ++l)
        {
            int i1 = func_178157_a(i, k, l);

            for (int j1 = 0; j1 < countChunksZ; ++j1)
            {
                int k1 = func_178157_a(j, k, j1);

                for (int l1 = 0; l1 < countChunksY; ++l1)
                {
                    int i2 = l1 * 16;
                    RenderChunk renderchunk = renderChunks[(j1 * countChunksY + l1) * countChunksX + l];
                    BlockPos blockpos = renderchunk.getPosition();

                    if (blockpos.getX() != i1 || blockpos.getY() != i2 || blockpos.getZ() != k1)
                    {
                        BlockPos blockpos1 = new BlockPos(i1, i2, k1);

                        if (!blockpos1.equals(renderchunk.getPosition()))
                        {
                            renderchunk.setPosition(blockpos1);
                        }
                    }
                }
            }
        }
    }

    private int func_178157_a(int p_178157_1_, int p_178157_2_, int p_178157_3_)
    {
        int i = p_178157_3_ * 16;
        int j = i - p_178157_1_ + p_178157_2_ / 2;

        if (j < 0)
        {
            j -= p_178157_2_ - 1;
        }

        return i - j / p_178157_2_ * p_178157_2_;
    }

    public void markBlocksForUpdate(int fromX, int fromY, int fromZ, int toX, int toY, int toZ)
    {
        int i = MathHelper.bucketInt(fromX, 16);
        int j = MathHelper.bucketInt(fromY, 16);
        int k = MathHelper.bucketInt(fromZ, 16);
        int l = MathHelper.bucketInt(toX, 16);
        int i1 = MathHelper.bucketInt(toY, 16);
        int j1 = MathHelper.bucketInt(toZ, 16);

        for (int k1 = i; k1 <= l; ++k1)
        {
            int l1 = k1 % countChunksX;

            if (l1 < 0)
            {
                l1 += countChunksX;
            }

            for (int i2 = j; i2 <= i1; ++i2)
            {
                int j2 = i2 % countChunksY;

                if (j2 < 0)
                {
                    j2 += countChunksY;
                }

                for (int k2 = k; k2 <= j1; ++k2)
                {
                    int l2 = k2 % countChunksZ;

                    if (l2 < 0)
                    {
                        l2 += countChunksZ;
                    }

                    int i3 = (l2 * countChunksY + j2) * countChunksX + l1;
                    RenderChunk renderchunk = renderChunks[i3];
                    renderchunk.setNeedsUpdate(true);
                }
            }
        }
    }

    public RenderChunk getRenderChunk(BlockPos pos)
    {
        int i = pos.getX() >> 4;
        int j = pos.getY() >> 4;
        int k = pos.getZ() >> 4;

        if (j >= 0 && j < countChunksY)
        {
            i = i % countChunksX;

            if (i < 0)
            {
                i += countChunksX;
            }

            k = k % countChunksZ;

            if (k < 0)
            {
                k += countChunksZ;
            }

            int l = (k * countChunksY + j) * countChunksX + i;
            return renderChunks[l];
        }
        else
        {
            return null;
        }
    }
}
