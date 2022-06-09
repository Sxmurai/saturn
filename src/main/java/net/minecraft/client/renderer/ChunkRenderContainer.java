package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;

public abstract class ChunkRenderContainer
{
    private double viewEntityX;
    private double viewEntityY;
    private double viewEntityZ;
    protected List<RenderChunk> renderChunks = Lists.newArrayListWithCapacity(17424);
    protected boolean initialized;

    public void initialize(double viewEntityXIn, double viewEntityYIn, double viewEntityZIn)
    {
        initialized = true;
        renderChunks.clear();
        viewEntityX = viewEntityXIn;
        viewEntityY = viewEntityYIn;
        viewEntityZ = viewEntityZIn;
    }

    public void preRenderChunk(RenderChunk renderChunkIn)
    {
        BlockPos blockpos = renderChunkIn.getPosition();
        GlStateManager.translate((float)((double)blockpos.getX() - viewEntityX), (float)((double)blockpos.getY() - viewEntityY), (float)((double)blockpos.getZ() - viewEntityZ));
    }

    public void addRenderChunk(RenderChunk renderChunkIn, EnumWorldBlockLayer layer)
    {
        renderChunks.add(renderChunkIn);
    }

    public abstract void renderChunkLayer(EnumWorldBlockLayer layer);
}
