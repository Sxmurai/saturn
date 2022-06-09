package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;

public class ChunkCompileTaskGenerator
{
    private final RenderChunk renderChunk;
    private final ReentrantLock lock = new ReentrantLock();
    private final List<Runnable> listFinishRunnables = Lists.newArrayList();
    private final ChunkCompileTaskGenerator.Type type;
    private RegionRenderCacheBuilder regionRenderCacheBuilder;
    private CompiledChunk compiledChunk;
    private ChunkCompileTaskGenerator.Status status = ChunkCompileTaskGenerator.Status.PENDING;
    private boolean finished;

    public ChunkCompileTaskGenerator(RenderChunk renderChunkIn, ChunkCompileTaskGenerator.Type typeIn)
    {
        renderChunk = renderChunkIn;
        type = typeIn;
    }

    public ChunkCompileTaskGenerator.Status getStatus()
    {
        return status;
    }

    public RenderChunk getRenderChunk()
    {
        return renderChunk;
    }

    public CompiledChunk getCompiledChunk()
    {
        return compiledChunk;
    }

    public void setCompiledChunk(CompiledChunk compiledChunkIn)
    {
        compiledChunk = compiledChunkIn;
    }

    public RegionRenderCacheBuilder getRegionRenderCacheBuilder()
    {
        return regionRenderCacheBuilder;
    }

    public void setRegionRenderCacheBuilder(RegionRenderCacheBuilder regionRenderCacheBuilderIn)
    {
        regionRenderCacheBuilder = regionRenderCacheBuilderIn;
    }

    public void setStatus(ChunkCompileTaskGenerator.Status statusIn)
    {
        lock.lock();

        try
        {
            status = statusIn;
        }
        finally
        {
            lock.unlock();
        }
    }

    public void finish()
    {
        lock.lock();

        try
        {
            if (type == ChunkCompileTaskGenerator.Type.REBUILD_CHUNK && status != ChunkCompileTaskGenerator.Status.DONE)
            {
                renderChunk.setNeedsUpdate(true);
            }

            finished = true;
            status = ChunkCompileTaskGenerator.Status.DONE;

            for (Runnable runnable : listFinishRunnables)
            {
                runnable.run();
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void addFinishRunnable(Runnable p_178539_1_)
    {
        lock.lock();

        try
        {
            listFinishRunnables.add(p_178539_1_);

            if (finished)
            {
                p_178539_1_.run();
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public ReentrantLock getLock()
    {
        return lock;
    }

    public ChunkCompileTaskGenerator.Type getType()
    {
        return type;
    }

    public boolean isFinished()
    {
        return finished;
    }

    public static enum Status
    {
        PENDING,
        COMPILING,
        UPLOADING,
        DONE
    }

    public static enum Type
    {
        REBUILD_CHUNK,
        RESORT_TRANSPARENCY
    }
}
