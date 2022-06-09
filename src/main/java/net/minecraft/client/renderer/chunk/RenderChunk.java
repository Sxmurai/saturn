package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Sets;
import java.nio.FloatBuffer;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RegionRenderCache;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import optifine.BlockPosM;
import optifine.Config;
import optifine.Reflector;
import optifine.ReflectorForge;
import shadersmod.client.SVertexBuilder;

public class RenderChunk
{
    private World world;
    private final RenderGlobal renderGlobal;
    public static int renderChunksUpdated;
    private BlockPos position;
    public CompiledChunk compiledChunk = CompiledChunk.DUMMY;
    private final ReentrantLock lockCompileTask = new ReentrantLock();
    private final ReentrantLock lockCompiledChunk = new ReentrantLock();
    private ChunkCompileTaskGenerator compileTask = null;
    private final Set field_181056_j = Sets.newHashSet();
    private final int index;
    private final FloatBuffer modelviewMatrix = GLAllocation.createDirectFloatBuffer(16);
    private final VertexBuffer[] vertexBuffers = new VertexBuffer[EnumWorldBlockLayer.values().length];
    public AxisAlignedBB boundingBox;
    private int frameIndex = -1;
    private boolean needsUpdate = true;
    private EnumMap field_181702_p;
    private static final String __OBFID = "CL_00002452";
    private final BlockPos[] positionOffsets16 = new BlockPos[EnumFacing.VALUES.length];
    private static final EnumWorldBlockLayer[] ENUM_WORLD_BLOCK_LAYERS = EnumWorldBlockLayer.values();
    private final EnumWorldBlockLayer[] blockLayersSingle = new EnumWorldBlockLayer[1];
    private final boolean isMipmaps = Config.isMipmaps();
    private final boolean fixBlockLayer = !Reflector.BetterFoliageClient.exists();
    private boolean playerUpdate = false;

    public RenderChunk(World worldIn, RenderGlobal renderGlobalIn, BlockPos blockPosIn, int indexIn)
    {
        world = worldIn;
        renderGlobal = renderGlobalIn;
        index = indexIn;

        if (!blockPosIn.equals(getPosition()))
        {
            setPosition(blockPosIn);
        }

        if (OpenGlHelper.useVbo())
        {
            for (int i = 0; i < EnumWorldBlockLayer.values().length; ++i)
            {
                vertexBuffers[i] = new VertexBuffer(DefaultVertexFormats.BLOCK);
            }
        }
    }

    public boolean setFrameIndex(int frameIndexIn)
    {
        if (frameIndex == frameIndexIn)
        {
            return false;
        }
        else
        {
            frameIndex = frameIndexIn;
            return true;
        }
    }

    public VertexBuffer getVertexBufferByLayer(int layer)
    {
        return vertexBuffers[layer];
    }

    public void setPosition(BlockPos pos)
    {
        stopCompileTask();
        position = pos;
        boundingBox = new AxisAlignedBB(pos, pos.add(16, 16, 16));
        initModelviewMatrix();

        for (int i = 0; i < positionOffsets16.length; ++i)
        {
            positionOffsets16[i] = null;
        }
    }

    public void resortTransparency(float x, float y, float z, ChunkCompileTaskGenerator generator)
    {
        CompiledChunk compiledchunk = generator.getCompiledChunk();

        if (compiledchunk.getState() != null && !compiledchunk.isLayerEmpty(EnumWorldBlockLayer.TRANSLUCENT))
        {
            WorldRenderer worldrenderer = generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(EnumWorldBlockLayer.TRANSLUCENT);
            preRenderBlocks(worldrenderer, position);
            worldrenderer.setVertexState(compiledchunk.getState());
            postRenderBlocks(EnumWorldBlockLayer.TRANSLUCENT, x, y, z, worldrenderer, compiledchunk);
        }
    }

    public void rebuildChunk(float x, float y, float z, ChunkCompileTaskGenerator generator)
    {
        CompiledChunk compiledchunk = new CompiledChunk();
        boolean flag = true;
        BlockPos blockpos = position;
        BlockPos blockpos1 = blockpos.add(15, 15, 15);
        generator.getLock().lock();
        RegionRenderCache regionrendercache;

        try
        {
            if (generator.getStatus() != ChunkCompileTaskGenerator.Status.COMPILING)
            {
                return;
            }

            if (world == null)
            {
                return;
            }

            regionrendercache = createRegionRenderCache(world, blockpos.add(-1, -1, -1), blockpos1.add(1, 1, 1), 1);

            if (Reflector.MinecraftForgeClient_onRebuildChunk.exists())
            {
                Reflector.call(Reflector.MinecraftForgeClient_onRebuildChunk, world, position, regionrendercache);
            }

            generator.setCompiledChunk(compiledchunk);
        }
        finally
        {
            generator.getLock().unlock();
        }

        VisGraph var10 = new VisGraph();
        HashSet var11 = Sets.newHashSet();

        if (!regionrendercache.extendedLevelsInChunkCache())
        {
            ++RenderChunk.renderChunksUpdated;
            boolean[] aboolean = new boolean[RenderChunk.ENUM_WORLD_BLOCK_LAYERS.length];
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            Iterator iterator = BlockPosM.getAllInBoxMutable(blockpos, blockpos1).iterator();
            boolean flag1 = Reflector.ForgeBlock_hasTileEntity.exists();
            boolean flag2 = Reflector.ForgeBlock_canRenderInLayer.exists();
            boolean flag3 = Reflector.ForgeHooksClient_setRenderLayer.exists();

            while (iterator.hasNext())
            {
                BlockPosM blockposm = (BlockPosM)iterator.next();
                IBlockState iblockstate = regionrendercache.getBlockState(blockposm);
                Block block = iblockstate.getBlock();

                if (block.isOpaqueCube())
                {
                    var10.func_178606_a(blockposm);
                }

                if (ReflectorForge.blockHasTileEntity(iblockstate))
                {
                    TileEntity tileentity = regionrendercache.getTileEntity(new BlockPos(blockposm));
                    TileEntitySpecialRenderer tileentityspecialrenderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tileentity);

                    if (tileentity != null && tileentityspecialrenderer != null)
                    {
                        compiledchunk.addTileEntity(tileentity);

                        if (tileentityspecialrenderer.func_181055_a())
                        {
                            var11.add(tileentity);
                        }
                    }
                }

                EnumWorldBlockLayer[] aenumworldblocklayer;

                if (flag2)
                {
                    aenumworldblocklayer = RenderChunk.ENUM_WORLD_BLOCK_LAYERS;
                }
                else
                {
                    aenumworldblocklayer = blockLayersSingle;
                    aenumworldblocklayer[0] = block.getBlockLayer();
                }

                for (int i = 0; i < aenumworldblocklayer.length; ++i)
                {
                    EnumWorldBlockLayer enumworldblocklayer = aenumworldblocklayer[i];

                    if (flag2)
                    {
                        boolean flag4 = Reflector.callBoolean(block, Reflector.ForgeBlock_canRenderInLayer, enumworldblocklayer);

                        if (!flag4)
                        {
                            continue;
                        }
                    }

                    if (flag3)
                    {
                        Reflector.callVoid(Reflector.ForgeHooksClient_setRenderLayer, enumworldblocklayer);
                    }

                    if (fixBlockLayer)
                    {
                        enumworldblocklayer = fixBlockLayer(block, enumworldblocklayer);
                    }

                    int j = enumworldblocklayer.ordinal();

                    if (block.getRenderType() != -1)
                    {
                        WorldRenderer worldrenderer = generator.getRegionRenderCacheBuilder().getWorldRendererByLayerId(j);
                        worldrenderer.setBlockLayer(enumworldblocklayer);

                        if (!compiledchunk.isLayerStarted(enumworldblocklayer))
                        {
                            compiledchunk.setLayerStarted(enumworldblocklayer);
                            preRenderBlocks(worldrenderer, blockpos);
                        }

                        aboolean[j] |= blockrendererdispatcher.renderBlock(iblockstate, blockposm, regionrendercache, worldrenderer);
                    }
                }
            }

            for (EnumWorldBlockLayer enumworldblocklayer1 : RenderChunk.ENUM_WORLD_BLOCK_LAYERS)
            {
                if (aboolean[enumworldblocklayer1.ordinal()])
                {
                    compiledchunk.setLayerUsed(enumworldblocklayer1);
                }

                if (compiledchunk.isLayerStarted(enumworldblocklayer1))
                {
                    if (Config.isShaders())
                    {
                        SVertexBuilder.calcNormalChunkLayer(generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(enumworldblocklayer1));
                    }

                    postRenderBlocks(enumworldblocklayer1, x, y, z, generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(enumworldblocklayer1), compiledchunk);
                }
            }
        }

        compiledchunk.setVisibility(var10.computeVisibility());
        lockCompileTask.lock();

        try
        {
            HashSet hashset1 = Sets.newHashSet(var11);
            HashSet hashset2 = Sets.newHashSet(field_181056_j);
            hashset1.removeAll(field_181056_j);
            hashset2.removeAll(var11);
            field_181056_j.clear();
            field_181056_j.addAll(var11);
            renderGlobal.func_181023_a(hashset2, hashset1);
        }
        finally
        {
            lockCompileTask.unlock();
        }
    }

    protected void finishCompileTask()
    {
        lockCompileTask.lock();

        try
        {
            if (compileTask != null && compileTask.getStatus() != ChunkCompileTaskGenerator.Status.DONE)
            {
                compileTask.finish();
                compileTask = null;
            }
        }
        finally
        {
            lockCompileTask.unlock();
        }
    }

    public ReentrantLock getLockCompileTask()
    {
        return lockCompileTask;
    }

    public ChunkCompileTaskGenerator makeCompileTaskChunk()
    {
        lockCompileTask.lock();
        ChunkCompileTaskGenerator chunkcompiletaskgenerator;

        try
        {
            finishCompileTask();
            compileTask = new ChunkCompileTaskGenerator(this, ChunkCompileTaskGenerator.Type.REBUILD_CHUNK);
            chunkcompiletaskgenerator = compileTask;
        }
        finally
        {
            lockCompileTask.unlock();
        }

        return chunkcompiletaskgenerator;
    }

    public ChunkCompileTaskGenerator makeCompileTaskTransparency()
    {
        lockCompileTask.lock();
        ChunkCompileTaskGenerator chunkcompiletaskgenerator1;

        try
        {
            if (compileTask != null && compileTask.getStatus() == ChunkCompileTaskGenerator.Status.PENDING)
            {
                ChunkCompileTaskGenerator chunkcompiletaskgenerator2 = null;
                return chunkcompiletaskgenerator2;
            }

            if (compileTask != null && compileTask.getStatus() != ChunkCompileTaskGenerator.Status.DONE)
            {
                compileTask.finish();
                compileTask = null;
            }

            compileTask = new ChunkCompileTaskGenerator(this, ChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY);
            compileTask.setCompiledChunk(compiledChunk);
            ChunkCompileTaskGenerator chunkcompiletaskgenerator = compileTask;
            chunkcompiletaskgenerator1 = chunkcompiletaskgenerator;
        }
        finally
        {
            lockCompileTask.unlock();
        }

        return chunkcompiletaskgenerator1;
    }

    private void preRenderBlocks(WorldRenderer worldRendererIn, BlockPos pos)
    {
        worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
        worldRendererIn.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
    }

    private void postRenderBlocks(EnumWorldBlockLayer layer, float x, float y, float z, WorldRenderer worldRendererIn, CompiledChunk compiledChunkIn)
    {
        if (layer == EnumWorldBlockLayer.TRANSLUCENT && !compiledChunkIn.isLayerEmpty(layer))
        {
            worldRendererIn.func_181674_a(x, y, z);
            compiledChunkIn.setState(worldRendererIn.func_181672_a());
        }

        worldRendererIn.finishDrawing();
    }

    private void initModelviewMatrix()
    {
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        float f = 1.000001F;
        GlStateManager.translate(-8.0F, -8.0F, -8.0F);
        GlStateManager.scale(f, f, f);
        GlStateManager.translate(8.0F, 8.0F, 8.0F);
        GlStateManager.getFloat(2982, modelviewMatrix);
        GlStateManager.popMatrix();
    }

    public void multModelviewMatrix()
    {
        GlStateManager.multMatrix(modelviewMatrix);
    }

    public CompiledChunk getCompiledChunk()
    {
        return compiledChunk;
    }

    public void setCompiledChunk(CompiledChunk compiledChunkIn)
    {
        lockCompiledChunk.lock();

        try
        {
            compiledChunk = compiledChunkIn;
        }
        finally
        {
            lockCompiledChunk.unlock();
        }
    }

    public void stopCompileTask()
    {
        finishCompileTask();
        compiledChunk = CompiledChunk.DUMMY;
    }

    public void deleteGlResources()
    {
        stopCompileTask();
        world = null;

        for (int i = 0; i < EnumWorldBlockLayer.values().length; ++i)
        {
            if (vertexBuffers[i] != null)
            {
                vertexBuffers[i].deleteGlBuffers();
            }
        }
    }

    public BlockPos getPosition()
    {
        return position;
    }

    public void setNeedsUpdate(boolean needsUpdateIn)
    {
        needsUpdate = needsUpdateIn;

        if (needsUpdate)
        {
            if (isWorldPlayerUpdate())
            {
                playerUpdate = true;
            }
        }
        else
        {
            playerUpdate = false;
        }
    }

    public boolean isNeedsUpdate()
    {
        return needsUpdate;
    }

    public BlockPos func_181701_a(EnumFacing p_181701_1_)
    {
        return getPositionOffset16(p_181701_1_);
    }

    public BlockPos getPositionOffset16(EnumFacing p_getPositionOffset16_1_)
    {
        int i = p_getPositionOffset16_1_.getIndex();
        BlockPos blockpos = positionOffsets16[i];

        if (blockpos == null)
        {
            blockpos = getPosition().offset(p_getPositionOffset16_1_, 16);
            positionOffsets16[i] = blockpos;
        }

        return blockpos;
    }

    private boolean isWorldPlayerUpdate()
    {
        if (world instanceof WorldClient)
        {
            WorldClient worldclient = (WorldClient) world;
            return worldclient.isPlayerUpdate();
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

    protected RegionRenderCache createRegionRenderCache(World p_createRegionRenderCache_1_, BlockPos p_createRegionRenderCache_2_, BlockPos p_createRegionRenderCache_3_, int p_createRegionRenderCache_4_)
    {
        return new RegionRenderCache(p_createRegionRenderCache_1_, p_createRegionRenderCache_2_, p_createRegionRenderCache_3_, p_createRegionRenderCache_4_);
    }

    private EnumWorldBlockLayer fixBlockLayer(Block p_fixBlockLayer_1_, EnumWorldBlockLayer p_fixBlockLayer_2_)
    {
        if (isMipmaps)
        {
            if (p_fixBlockLayer_2_ == EnumWorldBlockLayer.CUTOUT)
            {
                if (p_fixBlockLayer_1_ instanceof BlockRedstoneWire)
                {
                    return p_fixBlockLayer_2_;
                }

                if (p_fixBlockLayer_1_ instanceof BlockCactus)
                {
                    return p_fixBlockLayer_2_;
                }

                return EnumWorldBlockLayer.CUTOUT_MIPPED;
            }
        }
        else if (p_fixBlockLayer_2_ == EnumWorldBlockLayer.CUTOUT_MIPPED)
        {
            return EnumWorldBlockLayer.CUTOUT;
        }

        return p_fixBlockLayer_2_;
    }
}
