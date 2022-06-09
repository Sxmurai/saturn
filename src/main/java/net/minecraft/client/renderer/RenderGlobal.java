package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.client.renderer.chunk.ListChunkFactory;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.VboChunkFactory;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemRecord;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Matrix4f;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vector3d;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import optifine.ChunkUtils;
import optifine.CloudRenderer;
import optifine.Config;
import optifine.CustomColors;
import optifine.CustomSky;
import optifine.DynamicLights;
import optifine.Lagometer;
import optifine.RandomMobs;
import optifine.Reflector;
import optifine.RenderInfoLazy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import shadersmod.client.Shaders;
import shadersmod.client.ShadersRender;
import shadersmod.client.ShadowUtils;

public class RenderGlobal implements IWorldAccess, IResourceManagerReloadListener
{
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation locationMoonPhasesPng = new ResourceLocation("textures/environment/moon_phases.png");
    private static final ResourceLocation locationSunPng = new ResourceLocation("textures/environment/sun.png");
    private static final ResourceLocation locationCloudsPng = new ResourceLocation("textures/environment/clouds.png");
    private static final ResourceLocation locationEndSkyPng = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation locationForcefieldPng = new ResourceLocation("textures/misc/forcefield.png");

    /** A reference to the Minecraft object. */
    public final Minecraft mc;

    /** The RenderEngine instance used by RenderGlobal */
    private final TextureManager renderEngine;
    private final RenderManager renderManager;
    private WorldClient theWorld;
    private Set chunksToUpdate = Sets.newLinkedHashSet();

    /** List of OpenGL lists for the current render pass */
    private List renderInfos = Lists.newArrayListWithCapacity(69696);
    private final Set field_181024_n = Sets.newHashSet();
    private ViewFrustum viewFrustum;

    /** The star GL Call list */
    private int starGLCallList = -1;

    /** OpenGL sky list */
    private int glSkyList = -1;

    /** OpenGL sky list 2 */
    private int glSkyList2 = -1;
    private final VertexFormat vertexBufferFormat;
    private VertexBuffer starVBO;
    private VertexBuffer skyVBO;
    private VertexBuffer sky2VBO;

    /**
     * counts the cloud render updates. Used with mod to stagger some updates
     */
    private int cloudTickCounter;

    /**
     * Stores blocks currently being broken. Key is entity ID of the thing doing the breaking. Value is a
     * DestroyBlockProgress
     */
    public final Map damagedBlocks = Maps.newHashMap();

    /** Currently playing sounds.  Type:  HashMap<ChunkCoordinates, ISound> */
    private final Map mapSoundPositions = Maps.newHashMap();
    private final TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];
    private Framebuffer entityOutlineFramebuffer;

    /** Stores the shader group for the entity_outline shader */
    private ShaderGroup entityOutlineShader;
    private double frustumUpdatePosX = Double.MIN_VALUE;
    private double frustumUpdatePosY = Double.MIN_VALUE;
    private double frustumUpdatePosZ = Double.MIN_VALUE;
    private int frustumUpdatePosChunkX = Integer.MIN_VALUE;
    private int frustumUpdatePosChunkY = Integer.MIN_VALUE;
    private int frustumUpdatePosChunkZ = Integer.MIN_VALUE;
    private double lastViewEntityX = Double.MIN_VALUE;
    private double lastViewEntityY = Double.MIN_VALUE;
    private double lastViewEntityZ = Double.MIN_VALUE;
    private double lastViewEntityPitch = Double.MIN_VALUE;
    private double lastViewEntityYaw = Double.MIN_VALUE;
    private final ChunkRenderDispatcher renderDispatcher = new ChunkRenderDispatcher();
    private ChunkRenderContainer renderContainer;
    private int renderDistanceChunks = -1;

    /** Render entities startup counter (init value=2) */
    private int renderEntitiesStartupCounter = 2;

    /** Count entities total */
    private int countEntitiesTotal;

    /** Count entities rendered */
    private int countEntitiesRendered;

    /** Count entities hidden */
    private int countEntitiesHidden;
    private boolean debugFixTerrainFrustum = false;
    private ClippingHelper debugFixedClippingHelper;
    private final Vector4f[] debugTerrainMatrix = new Vector4f[8];
    private final Vector3d debugTerrainFrustumPosition = new Vector3d();
    private boolean vboEnabled = false;
    IRenderChunkFactory renderChunkFactory;
    private double prevRenderSortX;
    private double prevRenderSortY;
    private double prevRenderSortZ;
    public boolean displayListEntitiesDirty = true;
    private static final String __OBFID = "CL_00000954";
    private final CloudRenderer cloudRenderer;
    public Entity renderedEntity;
    public Set chunksToResortTransparency = new LinkedHashSet();
    public Set chunksToUpdateForced = new LinkedHashSet();
    private final Deque visibilityDeque = new ArrayDeque();
    private List renderInfosEntities = new ArrayList(1024);
    private List renderInfosTileEntities = new ArrayList(1024);
    private final List renderInfosNormal = new ArrayList(1024);
    private final List renderInfosEntitiesNormal = new ArrayList(1024);
    private final List renderInfosTileEntitiesNormal = new ArrayList(1024);
    private final List renderInfosShadow = new ArrayList(1024);
    private final List renderInfosEntitiesShadow = new ArrayList(1024);
    private final List renderInfosTileEntitiesShadow = new ArrayList(1024);
    private int renderDistance = 0;
    private int renderDistanceSq = 0;
    private static final Set SET_ALL_FACINGS = Collections.unmodifiableSet(new HashSet(Arrays.asList(EnumFacing.VALUES)));
    private int countTileEntitiesRendered;

    public RenderGlobal(Minecraft mcIn)
    {
        cloudRenderer = new CloudRenderer(mcIn);
        mc = mcIn;
        renderManager = mcIn.getRenderManager();
        renderEngine = mcIn.getTextureManager();
        renderEngine.bindTexture(RenderGlobal.locationForcefieldPng);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.bindTexture(0);
        updateDestroyBlockIcons();
        vboEnabled = OpenGlHelper.useVbo();

        if (vboEnabled)
        {
            renderContainer = new VboRenderList();
            renderChunkFactory = new VboChunkFactory();
        }
        else
        {
            renderContainer = new RenderList();
            renderChunkFactory = new ListChunkFactory();
        }

        vertexBufferFormat = new VertexFormat();
        vertexBufferFormat.func_181721_a(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
        generateStars();
        generateSky();
        generateSky2();
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        updateDestroyBlockIcons();
    }

    private void updateDestroyBlockIcons()
    {
        TextureMap texturemap = mc.getTextureMapBlocks();

        for (int i = 0; i < destroyBlockIcons.length; ++i)
        {
            destroyBlockIcons[i] = texturemap.getAtlasSprite("minecraft:blocks/destroy_stage_" + i);
        }
    }

    /**
     * Creates the entity outline shader to be stored in RenderGlobal.entityOutlineShader
     */
    public void makeEntityOutlineShader()
    {
        if (OpenGlHelper.shadersSupported)
        {
            if (ShaderLinkHelper.getStaticShaderLinkHelper() == null)
            {
                ShaderLinkHelper.setNewStaticShaderLinkHelper();
            }

            ResourceLocation resourcelocation = new ResourceLocation("shaders/post/entity_outline.json");

            try
            {
                entityOutlineShader = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), resourcelocation);
                entityOutlineShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
                entityOutlineFramebuffer = entityOutlineShader.getFramebufferRaw("final");
            }
            catch (IOException ioexception)
            {
                RenderGlobal.logger.warn("Failed to load shader: " + resourcelocation, ioexception);
                entityOutlineShader = null;
                entityOutlineFramebuffer = null;
            }
            catch (JsonSyntaxException jsonsyntaxexception)
            {
                RenderGlobal.logger.warn("Failed to load shader: " + resourcelocation, jsonsyntaxexception);
                entityOutlineShader = null;
                entityOutlineFramebuffer = null;
            }
        }
        else
        {
            entityOutlineShader = null;
            entityOutlineFramebuffer = null;
        }
    }

    public void renderEntityOutlineFramebuffer()
    {
        if (isRenderEntityOutlines())
        {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            entityOutlineFramebuffer.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false);
            GlStateManager.disableBlend();
        }
    }

    protected boolean isRenderEntityOutlines()
    {
        return !Config.isFastRender() && !Config.isShaders() && !Config.isAntialiasing() && entityOutlineFramebuffer != null && entityOutlineShader != null && mc.thePlayer != null && mc.thePlayer.isSpectator() && mc.gameSettings.keyBindSpectatorOutlines.isKeyDown();
    }

    private void generateSky2()
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        if (sky2VBO != null)
        {
            sky2VBO.deleteGlBuffers();
        }

        if (glSkyList2 >= 0)
        {
            GLAllocation.deleteDisplayLists(glSkyList2);
            glSkyList2 = -1;
        }

        if (vboEnabled)
        {
            sky2VBO = new VertexBuffer(vertexBufferFormat);
            renderSky(worldrenderer, -16.0F, true);
            worldrenderer.finishDrawing();
            worldrenderer.reset();
            sky2VBO.func_181722_a(worldrenderer.getByteBuffer());
        }
        else
        {
            glSkyList2 = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(glSkyList2, GL11.GL_COMPILE);
            renderSky(worldrenderer, -16.0F, true);
            tessellator.draw();
            GL11.glEndList();
        }
    }

    private void generateSky()
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        if (skyVBO != null)
        {
            skyVBO.deleteGlBuffers();
        }

        if (glSkyList >= 0)
        {
            GLAllocation.deleteDisplayLists(glSkyList);
            glSkyList = -1;
        }

        if (vboEnabled)
        {
            skyVBO = new VertexBuffer(vertexBufferFormat);
            renderSky(worldrenderer, 16.0F, false);
            worldrenderer.finishDrawing();
            worldrenderer.reset();
            skyVBO.func_181722_a(worldrenderer.getByteBuffer());
        }
        else
        {
            glSkyList = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(glSkyList, GL11.GL_COMPILE);
            renderSky(worldrenderer, 16.0F, false);
            tessellator.draw();
            GL11.glEndList();
        }
    }

    private void renderSky(WorldRenderer worldRendererIn, float p_174968_2_, boolean p_174968_3_)
    {
        boolean flag = true;
        boolean flag1 = true;
        worldRendererIn.begin(7, DefaultVertexFormats.POSITION);

        for (int i = -384; i <= 384; i += 64)
        {
            for (int j = -384; j <= 384; j += 64)
            {
                float f = (float)i;
                float f1 = (float)(i + 64);

                if (p_174968_3_)
                {
                    f1 = (float)i;
                    f = (float)(i + 64);
                }

                worldRendererIn.pos(f, p_174968_2_, j).endVertex();
                worldRendererIn.pos(f1, p_174968_2_, j).endVertex();
                worldRendererIn.pos(f1, p_174968_2_, j + 64).endVertex();
                worldRendererIn.pos(f, p_174968_2_, j + 64).endVertex();
            }
        }
    }

    private void generateStars()
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        if (starVBO != null)
        {
            starVBO.deleteGlBuffers();
        }

        if (starGLCallList >= 0)
        {
            GLAllocation.deleteDisplayLists(starGLCallList);
            starGLCallList = -1;
        }

        if (vboEnabled)
        {
            starVBO = new VertexBuffer(vertexBufferFormat);
            renderStars(worldrenderer);
            worldrenderer.finishDrawing();
            worldrenderer.reset();
            starVBO.func_181722_a(worldrenderer.getByteBuffer());
        }
        else
        {
            starGLCallList = GLAllocation.generateDisplayLists(1);
            GlStateManager.pushMatrix();
            GL11.glNewList(starGLCallList, GL11.GL_COMPILE);
            renderStars(worldrenderer);
            tessellator.draw();
            GL11.glEndList();
            GlStateManager.popMatrix();
        }
    }

    private void renderStars(WorldRenderer worldRendererIn)
    {
        Random random = new Random(10842L);
        worldRendererIn.begin(7, DefaultVertexFormats.POSITION);

        for (int i = 0; i < 1500; ++i)
        {
            double d0 = random.nextFloat() * 2.0F - 1.0F;
            double d1 = random.nextFloat() * 2.0F - 1.0F;
            double d2 = random.nextFloat() * 2.0F - 1.0F;
            double d3 = 0.15F + random.nextFloat() * 0.1F;
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d4 < 1.0D && d4 > 0.01D)
            {
                d4 = 1.0D / Math.sqrt(d4);
                d0 = d0 * d4;
                d1 = d1 * d4;
                d2 = d2 * d4;
                double d5 = d0 * 100.0D;
                double d6 = d1 * 100.0D;
                double d7 = d2 * 100.0D;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);

                for (int j = 0; j < 4; ++j)
                {
                    double d17 = 0.0D;
                    double d18 = (double)((j & 2) - 1) * d3;
                    double d19 = (double)((j + 1 & 2) - 1) * d3;
                    double d20 = 0.0D;
                    double d21 = d18 * d16 - d19 * d15;
                    double d22 = d19 * d16 + d18 * d15;
                    double d23 = d21 * d12 + 0.0D * d13;
                    double d24 = 0.0D * d12 - d21 * d13;
                    double d25 = d24 * d9 - d22 * d10;
                    double d26 = d22 * d9 + d24 * d10;
                    worldRendererIn.pos(d5 + d25, d6 + d23, d7 + d26).endVertex();
                }
            }
        }
    }

    /**
     * set null to clear
     */
    public void setWorldAndLoadRenderers(WorldClient worldClientIn)
    {
        if (theWorld != null)
        {
            theWorld.removeWorldAccess(this);
        }

        frustumUpdatePosX = Double.MIN_VALUE;
        frustumUpdatePosY = Double.MIN_VALUE;
        frustumUpdatePosZ = Double.MIN_VALUE;
        frustumUpdatePosChunkX = Integer.MIN_VALUE;
        frustumUpdatePosChunkY = Integer.MIN_VALUE;
        frustumUpdatePosChunkZ = Integer.MIN_VALUE;
        renderManager.set(worldClientIn);
        theWorld = worldClientIn;

        if (Config.isDynamicLights())
        {
            DynamicLights.clear();
        }

        if (worldClientIn != null)
        {
            worldClientIn.addWorldAccess(this);
            loadRenderers();
        }
    }

    /**
     * Loads all the renderers and sets up the basic settings usage
     */
    public void loadRenderers()
    {
        if (theWorld != null)
        {
            displayListEntitiesDirty = true;
            Blocks.leaves.setGraphicsLevel(Config.isTreesFancy());
            Blocks.leaves2.setGraphicsLevel(Config.isTreesFancy());
            BlockModelRenderer.updateAoLightValue();

            if (Config.isDynamicLights())
            {
                DynamicLights.clear();
            }

            renderDistanceChunks = mc.gameSettings.renderDistanceChunks;
            renderDistance = renderDistanceChunks * 16;
            renderDistanceSq = renderDistance * renderDistance;
            boolean flag = vboEnabled;
            vboEnabled = OpenGlHelper.useVbo();

            if (flag && !vboEnabled)
            {
                renderContainer = new RenderList();
                renderChunkFactory = new ListChunkFactory();
            }
            else if (!flag && vboEnabled)
            {
                renderContainer = new VboRenderList();
                renderChunkFactory = new VboChunkFactory();
            }

            if (flag != vboEnabled)
            {
                generateStars();
                generateSky();
                generateSky2();
            }

            if (viewFrustum != null)
            {
                viewFrustum.deleteGlResources();
            }

            stopChunkUpdates();
            Set var5 = field_181024_n;

            synchronized (field_181024_n)
            {
                field_181024_n.clear();
            }

            viewFrustum = new ViewFrustum(theWorld, mc.gameSettings.renderDistanceChunks, this, renderChunkFactory);

            if (theWorld != null)
            {
                Entity entity = mc.getRenderViewEntity();

                if (entity != null)
                {
                    viewFrustum.updateChunkPositions(entity.posX, entity.posZ);
                }
            }

            renderEntitiesStartupCounter = 2;
        }
    }

    protected void stopChunkUpdates()
    {
        chunksToUpdate.clear();
        renderDispatcher.stopChunkUpdates();
    }

    public void createBindEntityOutlineFbs(int p_72720_1_, int p_72720_2_)
    {
        if (OpenGlHelper.shadersSupported && entityOutlineShader != null)
        {
            entityOutlineShader.createBindFramebuffers(p_72720_1_, p_72720_2_);
        }
    }

    public void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks)
    {
        int i = 0;

        if (Reflector.MinecraftForgeClient_getRenderPass.exists())
        {
            i = Reflector.callInt(Reflector.MinecraftForgeClient_getRenderPass);
        }

        if (renderEntitiesStartupCounter > 0)
        {
            if (i > 0)
            {
                return;
            }

            --renderEntitiesStartupCounter;
        }
        else
        {
            double d0 = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * (double)partialTicks;
            double d1 = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * (double)partialTicks;
            double d2 = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * (double)partialTicks;
            theWorld.theProfiler.startSection("prepare");
            TileEntityRendererDispatcher.instance.cacheActiveRenderInfo(theWorld, mc.getTextureManager(), mc.fontRendererObj, mc.getRenderViewEntity(), partialTicks);
            renderManager.cacheActiveRenderInfo(theWorld, mc.fontRendererObj, mc.getRenderViewEntity(), mc.pointedEntity, mc.gameSettings, partialTicks);

            if (i == 0)
            {
                countEntitiesTotal = 0;
                countEntitiesRendered = 0;
                countEntitiesHidden = 0;
                countTileEntitiesRendered = 0;
            }

            Entity entity = mc.getRenderViewEntity();
            double d3 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
            double d4 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
            double d5 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
            TileEntityRendererDispatcher.staticPlayerX = d3;
            TileEntityRendererDispatcher.staticPlayerY = d4;
            TileEntityRendererDispatcher.staticPlayerZ = d5;
            renderManager.setRenderPosition(d3, d4, d5);
            mc.entityRenderer.enableLightmap();
            theWorld.theProfiler.endStartSection("global");
            List list = theWorld.getLoadedEntityList();

            if (i == 0)
            {
                countEntitiesTotal = list.size();
            }

            if (Config.isFogOff() && mc.entityRenderer.fogStandard)
            {
                GlStateManager.disableFog();
            }

            boolean flag = Reflector.ForgeEntity_shouldRenderInPass.exists();
            boolean flag1 = Reflector.ForgeTileEntity_shouldRenderInPass.exists();

            for (int j = 0; j < theWorld.weatherEffects.size(); ++j)
            {
                Entity entity1 = theWorld.weatherEffects.get(j);

                if (!flag || Reflector.callBoolean(entity1, Reflector.ForgeEntity_shouldRenderInPass, Integer.valueOf(i)))
                {
                    ++countEntitiesRendered;

                    if (entity1.isInRangeToRender3d(d0, d1, d2))
                    {
                        renderManager.renderEntitySimple(entity1, partialTicks);
                    }
                }
            }

            if (isRenderEntityOutlines())
            {
                GlStateManager.depthFunc(519);
                GlStateManager.disableFog();
                entityOutlineFramebuffer.framebufferClear();
                entityOutlineFramebuffer.bindFramebuffer(false);
                theWorld.theProfiler.endStartSection("entityOutlines");
                RenderHelper.disableStandardItemLighting();
                renderManager.setRenderOutlines(true);

                for (int k = 0; k < list.size(); ++k)
                {
                    Entity entity3 = (Entity)list.get(k);

                    if (!flag || Reflector.callBoolean(entity3, Reflector.ForgeEntity_shouldRenderInPass, Integer.valueOf(i)))
                    {
                        boolean flag2 = mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping();
                        boolean flag3 = entity3.isInRangeToRender3d(d0, d1, d2) && (entity3.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(entity3.getEntityBoundingBox()) || entity3.riddenByEntity == mc.thePlayer) && entity3 instanceof EntityPlayer;

                        if ((entity3 != mc.getRenderViewEntity() || mc.gameSettings.thirdPersonView != 0 || flag2) && flag3)
                        {
                            renderManager.renderEntitySimple(entity3, partialTicks);
                        }
                    }
                }

                renderManager.setRenderOutlines(false);
                RenderHelper.enableStandardItemLighting();
                GlStateManager.depthMask(false);
                entityOutlineShader.loadShaderGroup(partialTicks);
                GlStateManager.enableLighting();
                GlStateManager.depthMask(true);
                mc.getFramebuffer().bindFramebuffer(false);
                GlStateManager.enableFog();
                GlStateManager.enableBlend();
                GlStateManager.enableColorMaterial();
                GlStateManager.depthFunc(515);
                GlStateManager.enableDepth();
                GlStateManager.enableAlpha();
            }

            theWorld.theProfiler.endStartSection("entities");
            boolean flag7 = Config.isShaders();

            if (flag7)
            {
                Shaders.beginEntities();
            }

            Iterator iterator1 = renderInfosEntities.iterator();
            boolean flag4 = mc.gameSettings.fancyGraphics;
            mc.gameSettings.fancyGraphics = Config.isDroppedItemsFancy();
            label920:

            while (iterator1.hasNext())
            {
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation = (RenderGlobal.ContainerLocalRenderInformation)iterator1.next();
                Chunk chunk = theWorld.getChunkFromBlockCoords(renderglobal$containerlocalrenderinformation.renderChunk.getPosition());
                ClassInheritanceMultiMap classinheritancemultimap = chunk.getEntityLists()[renderglobal$containerlocalrenderinformation.renderChunk.getPosition().getY() / 16];

                if (!classinheritancemultimap.isEmpty())
                {
                    Iterator iterator = classinheritancemultimap.iterator();

                    while (true)
                    {
                        Entity entity2;
                        boolean flag5;

                        while (true)
                        {
                            if (!iterator.hasNext())
                            {
                                continue label920;
                            }

                            entity2 = (Entity)iterator.next();

                            if (!flag || Reflector.callBoolean(entity2, Reflector.ForgeEntity_shouldRenderInPass, Integer.valueOf(i)))
                            {
                                flag5 = renderManager.shouldRender(entity2, camera, d0, d1, d2) || entity2.riddenByEntity == mc.thePlayer;

                                if (!flag5)
                                {
                                    break;
                                }

                                boolean flag6 = mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping();

                                if ((entity2 != mc.getRenderViewEntity() || mc.gameSettings.thirdPersonView != 0 || flag6) && (entity2.posY < 0.0D || entity2.posY >= 256.0D || theWorld.isBlockLoaded(new BlockPos(entity2))))
                                {
                                    ++countEntitiesRendered;

                                    if (entity2.getClass() == EntityItemFrame.class)
                                    {
                                        entity2.renderDistanceWeight = 0.06D;
                                    }

                                    renderedEntity = entity2;

                                    if (flag7)
                                    {
                                        Shaders.nextEntity(entity2);
                                    }

                                    renderManager.renderEntitySimple(entity2, partialTicks);
                                    renderedEntity = null;
                                    break;
                                }
                            }
                        }

                        if (!flag5 && entity2 instanceof EntityWitherSkull)
                        {
                            if (flag7)
                            {
                                Shaders.nextEntity(entity2);
                            }

                            mc.getRenderManager().renderWitherSkull(entity2, partialTicks);
                        }
                    }
                }
            }

            mc.gameSettings.fancyGraphics = flag4;
            FontRenderer fontrenderer = TileEntityRendererDispatcher.instance.getFontRenderer();

            if (flag7)
            {
                Shaders.endEntities();
                Shaders.beginBlockEntities();
            }

            theWorld.theProfiler.endStartSection("blockentities");
            RenderHelper.enableStandardItemLighting();

            if (Reflector.ForgeTileEntityRendererDispatcher_preDrawBatch.exists())
            {
                Reflector.call(TileEntityRendererDispatcher.instance, Reflector.ForgeTileEntityRendererDispatcher_preDrawBatch);
            }

            label1385:

            for (Object renderglobal$containerlocalrenderinformation10 : renderInfosTileEntities)
            {
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation1 = (RenderGlobal.ContainerLocalRenderInformation) renderglobal$containerlocalrenderinformation10;
                List list1 = renderglobal$containerlocalrenderinformation1.renderChunk.getCompiledChunk()
                             .getTileEntities();

                if (!list1.isEmpty())
                {
                    Iterator iterator2 = list1.iterator();

                    while (true)
                    {
                        TileEntity tileentity;

                        while (true)
                        {
                            if (!iterator2.hasNext())
                            {
                                continue label1385;
                            }

                            tileentity = (TileEntity)iterator2.next();

                            if (!flag1)
                            {
                                break;
                            }

                            if (Reflector.callBoolean(tileentity, Reflector.ForgeTileEntity_shouldRenderInPass, Integer.valueOf(i)))
                            {
                                AxisAlignedBB axisalignedbb = (AxisAlignedBB)Reflector.call(tileentity, Reflector.ForgeTileEntity_getRenderBoundingBox, new Object[0]);

                                if (axisalignedbb == null || camera.isBoundingBoxInFrustum(axisalignedbb))
                                {
                                    break;
                                }
                            }
                        }

                        Class oclass = tileentity.getClass();

                        if (oclass == TileEntitySign.class && !Config.zoomMode)
                        {
                            EntityPlayer entityplayer = mc.thePlayer;
                            double d6 = tileentity.getDistanceSq(entityplayer.posX, entityplayer.posY, entityplayer.posZ);

                            if (d6 > 256.0D)
                            {
                                fontrenderer.enabled = false;
                            }
                        }

                        if (flag7)
                        {
                            Shaders.nextBlockEntity(tileentity);
                        }

                        TileEntityRendererDispatcher.instance.renderTileEntity(tileentity, partialTicks, -1);
                        ++countTileEntitiesRendered;
                        fontrenderer.enabled = true;
                    }
                }
            }

            Set var32 = field_181024_n;

            synchronized (field_181024_n)
            {
                for (Object tileentity1 : field_181024_n)
                {
                    if (flag1)
                    {
                        if (!Reflector.callBoolean(tileentity1, Reflector.ForgeTileEntity_shouldRenderInPass, Integer.valueOf(i)))
                        {
                            continue;
                        }
                        AxisAlignedBB axisalignedbb1 = (AxisAlignedBB)Reflector.call(tileentity1, Reflector.ForgeTileEntity_getRenderBoundingBox, new Object[0]);

                        if (axisalignedbb1 != null && !camera.isBoundingBoxInFrustum(axisalignedbb1))
                        {
                            continue;
                        }
                    }

                    Class oclass1 = tileentity1.getClass();

                    if (oclass1 == TileEntitySign.class && !Config.zoomMode)
                    {
                        EntityPlayer entityplayer1 = mc.thePlayer;
                        double d7 = ((TileEntity) tileentity1).getDistanceSq(entityplayer1.posX, entityplayer1.posY, entityplayer1.posZ);

                        if (d7 > 256.0D)
                        {
                            fontrenderer.enabled = false;
                        }
                    }

                    if (flag7)
                    {
                        Shaders.nextBlockEntity((TileEntity) tileentity1);
                    }

                    TileEntityRendererDispatcher.instance.renderTileEntity((TileEntity) tileentity1, partialTicks, -1);
                    fontrenderer.enabled = true;
                }
            }

            if (Reflector.ForgeTileEntityRendererDispatcher_drawBatch.exists())
            {
                Reflector.call(TileEntityRendererDispatcher.instance, Reflector.ForgeTileEntityRendererDispatcher_drawBatch, Integer.valueOf(i));
            }

            preRenderDamagedBlocks();

            for (Object destroyblockprogress : damagedBlocks.values())
            {
                BlockPos blockpos = ((DestroyBlockProgress) destroyblockprogress).getPosition();
                TileEntity tileentity2 = theWorld.getTileEntity(blockpos);

                if (tileentity2 instanceof TileEntityChest)
                {
                    TileEntityChest tileentitychest = (TileEntityChest)tileentity2;

                    if (tileentitychest.adjacentChestXNeg != null)
                    {
                        blockpos = blockpos.offset(EnumFacing.WEST);
                        tileentity2 = theWorld.getTileEntity(blockpos);
                    }
                    else if (tileentitychest.adjacentChestZNeg != null)
                    {
                        blockpos = blockpos.offset(EnumFacing.NORTH);
                        tileentity2 = theWorld.getTileEntity(blockpos);
                    }
                }

                Block block = theWorld.getBlockState(blockpos).getBlock();
                boolean flag8;

                if (flag1)
                {
                    flag8 = false;

                    if (tileentity2 != null && Reflector.callBoolean(tileentity2, Reflector.ForgeTileEntity_shouldRenderInPass, Integer.valueOf(i)) && Reflector.callBoolean(tileentity2, Reflector.ForgeTileEntity_canRenderBreaking))
                    {
                        AxisAlignedBB axisalignedbb2 = (AxisAlignedBB)Reflector.call(tileentity2, Reflector.ForgeTileEntity_getRenderBoundingBox, new Object[0]);

                        if (axisalignedbb2 != null)
                        {
                            flag8 = camera.isBoundingBoxInFrustum(axisalignedbb2);
                        }
                    }
                }
                else
                {
                    flag8 = tileentity2 != null && (block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockSign || block instanceof BlockSkull);
                }

                if (flag8)
                {
                    if (flag7)
                    {
                        Shaders.nextBlockEntity(tileentity2);
                    }

                    TileEntityRendererDispatcher.instance.renderTileEntity(tileentity2, partialTicks, ((DestroyBlockProgress) destroyblockprogress).getPartialBlockDamage());
                }
            }

            postRenderDamagedBlocks();
            mc.entityRenderer.disableLightmap();
            mc.mcProfiler.endSection();
        }
    }

    /**
     * Gets the render info for use on the Debug screen
     */
    public String getDebugInfoRenders()
    {
        int i = viewFrustum.renderChunks.length;
        int j = 0;

        for (Object renderglobal$containerlocalrenderinformation0 : renderInfos)
        {
            RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation = (RenderGlobal.ContainerLocalRenderInformation) renderglobal$containerlocalrenderinformation0;
            CompiledChunk compiledchunk = renderglobal$containerlocalrenderinformation.renderChunk.compiledChunk;

            if (compiledchunk != CompiledChunk.DUMMY && !compiledchunk.isEmpty())
            {
                ++j;
            }
        }

        return String.format("C: %d/%d %sD: %d, %s", Integer.valueOf(j), Integer.valueOf(i), mc.renderChunksMany ? "(s) " : "", Integer.valueOf(renderDistanceChunks), renderDispatcher.getDebugInfo());
    }

    /**
     * Gets the entities info for use on the Debug screen
     */
    public String getDebugInfoEntities()
    {
        return "E: " + countEntitiesRendered + "/" + countEntitiesTotal + ", B: " + countEntitiesHidden + ", I: " + (countEntitiesTotal - countEntitiesHidden - countEntitiesRendered) + ", " + Config.getVersionDebug();
    }

    public void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator)
    {
        if (mc.gameSettings.renderDistanceChunks != renderDistanceChunks)
        {
            loadRenderers();
        }

        theWorld.theProfiler.startSection("camera");
        double d0 = viewEntity.posX - frustumUpdatePosX;
        double d1 = viewEntity.posY - frustumUpdatePosY;
        double d2 = viewEntity.posZ - frustumUpdatePosZ;

        if (frustumUpdatePosChunkX != viewEntity.chunkCoordX || frustumUpdatePosChunkY != viewEntity.chunkCoordY || frustumUpdatePosChunkZ != viewEntity.chunkCoordZ || d0 * d0 + d1 * d1 + d2 * d2 > 16.0D)
        {
            frustumUpdatePosX = viewEntity.posX;
            frustumUpdatePosY = viewEntity.posY;
            frustumUpdatePosZ = viewEntity.posZ;
            frustumUpdatePosChunkX = viewEntity.chunkCoordX;
            frustumUpdatePosChunkY = viewEntity.chunkCoordY;
            frustumUpdatePosChunkZ = viewEntity.chunkCoordZ;
            viewFrustum.updateChunkPositions(viewEntity.posX, viewEntity.posZ);
        }

        if (Config.isDynamicLights())
        {
            DynamicLights.update(this);
        }

        theWorld.theProfiler.endStartSection("renderlistcamera");
        double d3 = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
        double d4 = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks;
        double d5 = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;
        renderContainer.initialize(d3, d4, d5);
        theWorld.theProfiler.endStartSection("cull");

        if (debugFixedClippingHelper != null)
        {
            Frustum frustum = new Frustum(debugFixedClippingHelper);
            frustum.setPosition(debugTerrainFrustumPosition.field_181059_a, debugTerrainFrustumPosition.field_181060_b, debugTerrainFrustumPosition.field_181061_c);
            camera = frustum;
        }

        mc.mcProfiler.endStartSection("culling");
        BlockPos blockpos2 = new BlockPos(d3, d4 + (double)viewEntity.getEyeHeight(), d5);
        RenderChunk renderchunk = viewFrustum.getRenderChunk(blockpos2);
        BlockPos blockpos = new BlockPos(MathHelper.floor_double(d3 / 16.0D) * 16, MathHelper.floor_double(d4 / 16.0D) * 16, MathHelper.floor_double(d5 / 16.0D) * 16);
        displayListEntitiesDirty = displayListEntitiesDirty || !chunksToUpdate.isEmpty() || viewEntity.posX != lastViewEntityX || viewEntity.posY != lastViewEntityY || viewEntity.posZ != lastViewEntityZ || (double)viewEntity.rotationPitch != lastViewEntityPitch || (double)viewEntity.rotationYaw != lastViewEntityYaw;
        lastViewEntityX = viewEntity.posX;
        lastViewEntityY = viewEntity.posY;
        lastViewEntityZ = viewEntity.posZ;
        lastViewEntityPitch = viewEntity.rotationPitch;
        lastViewEntityYaw = viewEntity.rotationYaw;
        boolean flag = debugFixedClippingHelper != null;
        Lagometer.timerVisibility.start();

        if (Shaders.isShadowPass)
        {
            renderInfos = renderInfosShadow;
            renderInfosEntities = renderInfosEntitiesShadow;
            renderInfosTileEntities = renderInfosTileEntitiesShadow;

            if (!flag && displayListEntitiesDirty)
            {
                renderInfos.clear();
                renderInfosEntities.clear();
                renderInfosTileEntities.clear();
                RenderInfoLazy renderinfolazy = new RenderInfoLazy();
                Iterator<RenderChunk> iterator = ShadowUtils.makeShadowChunkIterator(theWorld, partialTicks, viewEntity, renderDistanceChunks, viewFrustum);

                while (iterator.hasNext())
                {
                    RenderChunk renderchunk1 = iterator.next();

                    if (renderchunk1 != null)
                    {
                        renderinfolazy.setRenderChunk(renderchunk1);

                        if (!renderchunk1.compiledChunk.isEmpty() || renderchunk1.isNeedsUpdate())
                        {
                            renderInfos.add(renderinfolazy.getRenderInfo());
                        }

                        BlockPos blockpos1 = renderchunk1.getPosition();

                        if (ChunkUtils.hasEntities(theWorld.getChunkFromBlockCoords(blockpos1)))
                        {
                            renderInfosEntities.add(renderinfolazy.getRenderInfo());
                        }

                        if (renderchunk1.getCompiledChunk().getTileEntities().size() > 0)
                        {
                            renderInfosTileEntities.add(renderinfolazy.getRenderInfo());
                        }
                    }
                }
            }
        }
        else
        {
            renderInfos = renderInfosNormal;
            renderInfosEntities = renderInfosEntitiesNormal;
            renderInfosTileEntities = renderInfosTileEntitiesNormal;
        }

        if (!flag && displayListEntitiesDirty && !Shaders.isShadowPass)
        {
            displayListEntitiesDirty = false;
            renderInfos.clear();
            renderInfosEntities.clear();
            renderInfosTileEntities.clear();
            visibilityDeque.clear();
            Deque deque = visibilityDeque;
            boolean flag1 = mc.renderChunksMany;

            if (renderchunk != null)
            {
                boolean flag2 = false;
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation3 = new RenderGlobal.ContainerLocalRenderInformation(renderchunk, null, 0, null);
                Set set1 = RenderGlobal.SET_ALL_FACINGS;

                if (set1.size() == 1)
                {
                    Vector3f vector3f = getViewVector(viewEntity, partialTicks);
                    EnumFacing enumfacing = EnumFacing.getFacingFromVector(vector3f.x, vector3f.y, vector3f.z).getOpposite();
                    set1.remove(enumfacing);
                }

                if (set1.isEmpty())
                {
                    flag2 = true;
                }

                if (flag2 && !playerSpectator)
                {
                    renderInfos.add(renderglobal$containerlocalrenderinformation3);
                }
                else
                {
                    if (playerSpectator && theWorld.getBlockState(blockpos2).getBlock().isOpaqueCube())
                    {
                        flag1 = false;
                    }

                    renderchunk.setFrameIndex(frameCount);
                    deque.add(renderglobal$containerlocalrenderinformation3);
                }
            }
            else
            {
                int i = blockpos2.getY() > 0 ? 248 : 8;

                for (int j = -renderDistanceChunks; j <= renderDistanceChunks; ++j)
                {
                    for (int k = -renderDistanceChunks; k <= renderDistanceChunks; ++k)
                    {
                        RenderChunk renderchunk2 = viewFrustum.getRenderChunk(new BlockPos((j << 4) + 8, i, (k << 4) + 8));

                        if (renderchunk2 != null && camera.isBoundingBoxInFrustum(renderchunk2.boundingBox))
                        {
                            renderchunk2.setFrameIndex(frameCount);
                            deque.add(new RenderGlobal.ContainerLocalRenderInformation(renderchunk2, null, 0, null));
                        }
                    }
                }
            }

            EnumFacing[] aenumfacing = EnumFacing.VALUES;
            int l = aenumfacing.length;

            while (!deque.isEmpty())
            {
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation1 = (RenderGlobal.ContainerLocalRenderInformation)deque.poll();
                RenderChunk renderchunk4 = renderglobal$containerlocalrenderinformation1.renderChunk;
                EnumFacing enumfacing2 = renderglobal$containerlocalrenderinformation1.facing;
                BlockPos blockpos3 = renderchunk4.getPosition();

                if (!renderchunk4.compiledChunk.isEmpty() || renderchunk4.isNeedsUpdate())
                {
                    renderInfos.add(renderglobal$containerlocalrenderinformation1);
                }

                if (ChunkUtils.hasEntities(theWorld.getChunkFromBlockCoords(blockpos3)))
                {
                    renderInfosEntities.add(renderglobal$containerlocalrenderinformation1);
                }

                if (renderchunk4.getCompiledChunk().getTileEntities().size() > 0)
                {
                    renderInfosTileEntities.add(renderglobal$containerlocalrenderinformation1);
                }

                for (int i1 = 0; i1 < l; ++i1)
                {
                    EnumFacing enumfacing1 = aenumfacing[i1];

                    if ((!flag1 || !renderglobal$containerlocalrenderinformation1.setFacing.contains(enumfacing1.getOpposite())) && (!flag1 || enumfacing2 == null || renderchunk4.getCompiledChunk().isVisible(enumfacing2.getOpposite(), enumfacing1)))
                    {
                        RenderChunk renderchunk3 = func_181562_a(blockpos2, renderchunk4, enumfacing1);

                        if (renderchunk3 != null && renderchunk3.setFrameIndex(frameCount) && camera.isBoundingBoxInFrustum(renderchunk3.boundingBox))
                        {
                            RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation = new RenderGlobal.ContainerLocalRenderInformation(renderchunk3, enumfacing1, renderglobal$containerlocalrenderinformation1.counter + 1, null);
                            renderglobal$containerlocalrenderinformation.setFacing.addAll(renderglobal$containerlocalrenderinformation1.setFacing);
                            renderglobal$containerlocalrenderinformation.setFacing.add(enumfacing1);
                            deque.add(renderglobal$containerlocalrenderinformation);
                        }
                    }
                }
            }
        }

        if (debugFixTerrainFrustum)
        {
            fixTerrainFrustum(d3, d4, d5);
            debugFixTerrainFrustum = false;
        }

        Lagometer.timerVisibility.end();

        if (Shaders.isShadowPass)
        {
            Shaders.mcProfilerEndSection();
        }
        else
        {
            renderDispatcher.clearChunkUpdates();
            Set set = chunksToUpdate;
            chunksToUpdate = Sets.newLinkedHashSet();
            Iterator iterator1 = renderInfos.iterator();
            Lagometer.timerChunkUpdate.start();

            while (iterator1.hasNext())
            {
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation2 = (RenderGlobal.ContainerLocalRenderInformation)iterator1.next();
                RenderChunk renderchunk5 = renderglobal$containerlocalrenderinformation2.renderChunk;

                if (renderchunk5.isNeedsUpdate() || set.contains(renderchunk5))
                {
                    displayListEntitiesDirty = true;

                    if (isPositionInRenderChunk(blockpos, renderglobal$containerlocalrenderinformation2.renderChunk))
                    {
                        if (!renderchunk5.isPlayerUpdate())
                        {
                            chunksToUpdateForced.add(renderchunk5);
                        }
                        else
                        {
                            mc.mcProfiler.startSection("build near");
                            renderDispatcher.updateChunkNow(renderchunk5);
                            renderchunk5.setNeedsUpdate(false);
                            mc.mcProfiler.endSection();
                        }
                    }
                    else
                    {
                        chunksToUpdate.add(renderchunk5);
                    }
                }
            }

            Lagometer.timerChunkUpdate.end();
            chunksToUpdate.addAll(set);
            mc.mcProfiler.endSection();
        }
    }

    private boolean isPositionInRenderChunk(BlockPos pos, RenderChunk renderChunkIn)
    {
        BlockPos blockpos = renderChunkIn.getPosition();
        return MathHelper.abs_int(pos.getX() - blockpos.getX()) <= 16 && (MathHelper.abs_int(pos.getY() - blockpos.getY()) <= 16 && MathHelper.abs_int(pos.getZ() - blockpos.getZ()) <= 16);
    }

    private Set getVisibleFacings(BlockPos pos)
    {
        VisGraph visgraph = new VisGraph();
        BlockPos blockpos = new BlockPos(pos.getX() >> 4 << 4, pos.getY() >> 4 << 4, pos.getZ() >> 4 << 4);
        Chunk chunk = theWorld.getChunkFromBlockCoords(blockpos);

        for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(blockpos, blockpos.add(15, 15, 15)))
        {
            if (chunk.getBlock(blockpos$mutableblockpos).isOpaqueCube())
            {
                visgraph.func_178606_a(blockpos$mutableblockpos);
            }
        }
        return visgraph.func_178609_b(pos);
    }

    private RenderChunk func_181562_a(BlockPos p_181562_1_, RenderChunk p_181562_2_, EnumFacing p_181562_3_)
    {
        BlockPos blockpos = p_181562_2_.getPositionOffset16(p_181562_3_);

        if (blockpos.getY() >= 0 && blockpos.getY() < 256)
        {
            int i = MathHelper.abs_int(p_181562_1_.getX() - blockpos.getX());
            int j = MathHelper.abs_int(p_181562_1_.getZ() - blockpos.getZ());

            if (Config.isFogOff())
            {
                if (i > renderDistance || j > renderDistance)
                {
                    return null;
                }
            }
            else
            {
                int k = i * i + j * j;

                if (k > renderDistanceSq)
                {
                    return null;
                }
            }

            return viewFrustum.getRenderChunk(blockpos);
        }
        else
        {
            return null;
        }
    }

    private void fixTerrainFrustum(double x, double y, double z)
    {
        debugFixedClippingHelper = new ClippingHelperImpl();
        ((ClippingHelperImpl) debugFixedClippingHelper).init();
        Matrix4f matrix4f = new Matrix4f(debugFixedClippingHelper.modelviewMatrix);
        matrix4f.transpose();
        Matrix4f matrix4f1 = new Matrix4f(debugFixedClippingHelper.projectionMatrix);
        matrix4f1.transpose();
        Matrix4f matrix4f2 = new Matrix4f();
        org.lwjgl.util.vector.Matrix4f.mul(matrix4f1, matrix4f, matrix4f2);
        matrix4f2.invert();
        debugTerrainFrustumPosition.field_181059_a = x;
        debugTerrainFrustumPosition.field_181060_b = y;
        debugTerrainFrustumPosition.field_181061_c = z;
        debugTerrainMatrix[0] = new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F);
        debugTerrainMatrix[1] = new Vector4f(1.0F, -1.0F, -1.0F, 1.0F);
        debugTerrainMatrix[2] = new Vector4f(1.0F, 1.0F, -1.0F, 1.0F);
        debugTerrainMatrix[3] = new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F);
        debugTerrainMatrix[4] = new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F);
        debugTerrainMatrix[5] = new Vector4f(1.0F, -1.0F, 1.0F, 1.0F);
        debugTerrainMatrix[6] = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
        debugTerrainMatrix[7] = new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F);

        for (int i = 0; i < 8; ++i)
        {
            org.lwjgl.util.vector.Matrix4f.transform(matrix4f2, debugTerrainMatrix[i], debugTerrainMatrix[i]);
            debugTerrainMatrix[i].x /= debugTerrainMatrix[i].w;
            debugTerrainMatrix[i].y /= debugTerrainMatrix[i].w;
            debugTerrainMatrix[i].z /= debugTerrainMatrix[i].w;
            debugTerrainMatrix[i].w = 1.0F;
        }
    }

    protected Vector3f getViewVector(Entity entityIn, double partialTicks)
    {
        float f = (float)((double)entityIn.prevRotationPitch + (double)(entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks);
        float f1 = (float)((double)entityIn.prevRotationYaw + (double)(entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks);

        if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2)
        {
            f += 180.0F;
        }

        float f2 = MathHelper.cos(-f1 * 0.017453292F - (float)Math.PI);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - (float)Math.PI);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        return new Vector3f(f3 * f4, f5, f2 * f4);
    }

    public int renderBlockLayer(EnumWorldBlockLayer blockLayerIn, double partialTicks, int pass, Entity entityIn)
    {
        RenderHelper.disableStandardItemLighting();

        if (blockLayerIn == EnumWorldBlockLayer.TRANSLUCENT)
        {
            mc.mcProfiler.startSection("translucent_sort");
            double d0 = entityIn.posX - prevRenderSortX;
            double d1 = entityIn.posY - prevRenderSortY;
            double d2 = entityIn.posZ - prevRenderSortZ;

            if (d0 * d0 + d1 * d1 + d2 * d2 > 1.0D)
            {
                prevRenderSortX = entityIn.posX;
                prevRenderSortY = entityIn.posY;
                prevRenderSortZ = entityIn.posZ;
                int k = 0;
                Iterator iterator = renderInfos.iterator();
                chunksToResortTransparency.clear();

                while (iterator.hasNext())
                {
                    RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation = (RenderGlobal.ContainerLocalRenderInformation)iterator.next();

                    if (renderglobal$containerlocalrenderinformation.renderChunk.compiledChunk.isLayerStarted(blockLayerIn) && k++ < 15)
                    {
                        chunksToResortTransparency.add(renderglobal$containerlocalrenderinformation.renderChunk);
                    }
                }
            }

            mc.mcProfiler.endSection();
        }

        mc.mcProfiler.startSection("filterempty");
        int l = 0;
        boolean flag = blockLayerIn == EnumWorldBlockLayer.TRANSLUCENT;
        int i1 = flag ? renderInfos.size() - 1 : 0;
        int i = flag ? -1 : renderInfos.size();
        int j1 = flag ? -1 : 1;

        for (int j = i1; j != i; j += j1)
        {
            RenderChunk renderchunk = ((RenderGlobal.ContainerLocalRenderInformation) renderInfos.get(j)).renderChunk;

            if (!renderchunk.getCompiledChunk().isLayerEmpty(blockLayerIn))
            {
                ++l;
                renderContainer.addRenderChunk(renderchunk, blockLayerIn);
            }
        }

        if (l == 0)
        {
            mc.mcProfiler.endSection();
            return l;
        }
        else
        {
            if (Config.isFogOff() && mc.entityRenderer.fogStandard)
            {
                GlStateManager.disableFog();
            }

            mc.mcProfiler.endStartSection("render_" + blockLayerIn);
            renderBlockLayer(blockLayerIn);
            mc.mcProfiler.endSection();
            return l;
        }
    }

    @SuppressWarnings("incomplete-switch")
    private void renderBlockLayer(EnumWorldBlockLayer blockLayerIn)
    {
        mc.entityRenderer.enableLightmap();

        if (OpenGlHelper.useVbo())
        {
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        }

        if (Config.isShaders())
        {
            ShadersRender.preRenderChunkLayer(blockLayerIn);
        }

        renderContainer.renderChunkLayer(blockLayerIn);

        if (Config.isShaders())
        {
            ShadersRender.postRenderChunkLayer(blockLayerIn);
        }

        if (OpenGlHelper.useVbo())
        {
            for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements())
            {
                VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
                int i = vertexformatelement.getIndex();

                switch (RenderGlobal.RenderGlobal$2.field_178037_a[vertexformatelement$enumusage.ordinal()])
                {
                    case 1:
                        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                        break;

                    case 2:
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + i);
                        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                        break;

                    case 3:
                        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
                        GlStateManager.resetColor();
                }
            }
        }

        mc.entityRenderer.disableLightmap();
    }

    private void cleanupDamagedBlocks(Iterator iteratorIn)
    {
        while (iteratorIn.hasNext())
        {
            DestroyBlockProgress destroyblockprogress = (DestroyBlockProgress)iteratorIn.next();
            int i = destroyblockprogress.getCreationCloudUpdateTick();

            if (cloudTickCounter - i > 400)
            {
                iteratorIn.remove();
            }
        }
    }

    public void updateClouds()
    {
        if (Config.isShaders() && Keyboard.isKeyDown(61) && Keyboard.isKeyDown(19))
        {
            Shaders.uninit();
            Shaders.loadShaderPack();
        }

        ++cloudTickCounter;

        if (cloudTickCounter % 20 == 0)
        {
            cleanupDamagedBlocks(damagedBlocks.values().iterator());
        }
    }

    private void renderSkyEnd()
    {
        if (Config.isSkyEnabled())
        {
            GlStateManager.disableFog();
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.depthMask(false);
            renderEngine.bindTexture(RenderGlobal.locationEndSkyPng);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();

            for (int i = 0; i < 6; ++i)
            {
                GlStateManager.pushMatrix();

                if (i == 1)
                {
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 2)
                {
                    GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 3)
                {
                    GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 4)
                {
                    GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                }

                if (i == 5)
                {
                    GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
                }

                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                worldrenderer.pos(-100.0D, -100.0D, -100.0D).tex(0.0D, 0.0D).color(40, 40, 40, 255).endVertex();
                worldrenderer.pos(-100.0D, -100.0D, 100.0D).tex(0.0D, 16.0D).color(40, 40, 40, 255).endVertex();
                worldrenderer.pos(100.0D, -100.0D, 100.0D).tex(16.0D, 16.0D).color(40, 40, 40, 255).endVertex();
                worldrenderer.pos(100.0D, -100.0D, -100.0D).tex(16.0D, 0.0D).color(40, 40, 40, 255).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.enableAlpha();
        }
    }

    public void renderSky(float partialTicks, int pass)
    {
        if (Reflector.ForgeWorldProvider_getSkyRenderer.exists())
        {
            WorldProvider worldprovider = mc.theWorld.provider;
            Object object = Reflector.call(worldprovider, Reflector.ForgeWorldProvider_getSkyRenderer);

            if (object != null)
            {
                Reflector.callVoid(object, Reflector.IRenderHandler_render, Float.valueOf(partialTicks), theWorld, mc);
                return;
            }
        }

        if (mc.theWorld.provider.getDimensionId() == 1)
        {
            renderSkyEnd();
        }
        else if (mc.theWorld.provider.isSurfaceWorld())
        {
            GlStateManager.disableTexture2D();
            boolean flag1 = Config.isShaders();

            if (flag1)
            {
                Shaders.disableTexture2D();
            }

            Vec3 vec3 = theWorld.getSkyColor(mc.getRenderViewEntity(), partialTicks);
            vec3 = CustomColors.getSkyColor(vec3, mc.theWorld, mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY + 1.0D, mc.getRenderViewEntity().posZ);

            if (flag1)
            {
                Shaders.setSkyColor(vec3);
            }

            float f = (float)vec3.xCoord;
            float f1 = (float)vec3.yCoord;
            float f2 = (float)vec3.zCoord;

            if (pass != 2)
            {
                float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
                float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
                float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
                f = f3;
                f1 = f4;
                f2 = f5;
            }

            GlStateManager.color(f, f1, f2);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            GlStateManager.depthMask(false);
            GlStateManager.enableFog();

            if (flag1)
            {
                Shaders.enableFog();
            }

            GlStateManager.color(f, f1, f2);

            if (flag1)
            {
                Shaders.preSkyList();
            }

            if (Config.isSkyEnabled())
            {
                if (vboEnabled)
                {
                    skyVBO.bindBuffer();
                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
                    skyVBO.drawArrays(7);
                    skyVBO.unbindBuffer();
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                }
                else
                {
                    GlStateManager.callList(glSkyList);
                }
            }

            GlStateManager.disableFog();

            if (flag1)
            {
                Shaders.disableFog();
            }

            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.disableStandardItemLighting();
            float[] afloat = theWorld.provider.calcSunriseSunsetColors(theWorld.getCelestialAngle(partialTicks), partialTicks);

            if (afloat != null && Config.isSunMoonEnabled())
            {
                GlStateManager.disableTexture2D();

                if (flag1)
                {
                    Shaders.disableTexture2D();
                }

                GlStateManager.shadeModel(7425);
                GlStateManager.pushMatrix();
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(MathHelper.sin(theWorld.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                float f6 = afloat[0];
                float f7 = afloat[1];
                float f8 = afloat[2];

                if (pass != 2)
                {
                    float f9 = (f6 * 30.0F + f7 * 59.0F + f8 * 11.0F) / 100.0F;
                    float f10 = (f6 * 30.0F + f7 * 70.0F) / 100.0F;
                    float f11 = (f6 * 30.0F + f8 * 70.0F) / 100.0F;
                    f6 = f9;
                    f7 = f10;
                    f8 = f11;
                }

                worldrenderer.begin(6, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos(0.0D, 100.0D, 0.0D).color(f6, f7, f8, afloat[3]).endVertex();
                boolean flag = true;

                for (int i = 0; i <= 16; ++i)
                {
                    float f20 = (float)i * (float)Math.PI * 2.0F / 16.0F;
                    float f12 = MathHelper.sin(f20);
                    float f13 = MathHelper.cos(f20);
                    worldrenderer.pos(f12 * 120.0F, f13 * 120.0F, -f13 * 40.0F * afloat[3]).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
                }

                tessellator.draw();
                GlStateManager.popMatrix();
                GlStateManager.shadeModel(7424);
            }

            GlStateManager.enableTexture2D();

            if (flag1)
            {
                Shaders.enableTexture2D();
            }

            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
            GlStateManager.pushMatrix();
            float f15 = 1.0F - theWorld.getRainStrength(partialTicks);
            GlStateManager.color(1.0F, 1.0F, 1.0F, f15);
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            CustomSky.renderSky(theWorld, renderEngine, theWorld.getCelestialAngle(partialTicks), f15);

            if (flag1)
            {
                Shaders.preCelestialRotate();
            }

            GlStateManager.rotate(theWorld.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);

            if (flag1)
            {
                Shaders.postCelestialRotate();
            }

            float f16 = 30.0F;

            if (Config.isSunTexture())
            {
                renderEngine.bindTexture(RenderGlobal.locationSunPng);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos(-f16, 100.0D, -f16).tex(0.0D, 0.0D).endVertex();
                worldrenderer.pos(f16, 100.0D, -f16).tex(1.0D, 0.0D).endVertex();
                worldrenderer.pos(f16, 100.0D, f16).tex(1.0D, 1.0D).endVertex();
                worldrenderer.pos(-f16, 100.0D, f16).tex(0.0D, 1.0D).endVertex();
                tessellator.draw();
            }

            f16 = 20.0F;

            if (Config.isMoonTexture())
            {
                renderEngine.bindTexture(RenderGlobal.locationMoonPhasesPng);
                int l = theWorld.getMoonPhase();
                int j = l % 4;
                int k = l / 4 % 2;
                float f21 = (float)(j + 0) / 4.0F;
                float f22 = (float)(k + 0) / 2.0F;
                float f23 = (float)(j + 1) / 4.0F;
                float f14 = (float)(k + 1) / 2.0F;
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos(-f16, -100.0D, f16).tex(f23, f14).endVertex();
                worldrenderer.pos(f16, -100.0D, f16).tex(f21, f14).endVertex();
                worldrenderer.pos(f16, -100.0D, -f16).tex(f21, f22).endVertex();
                worldrenderer.pos(-f16, -100.0D, -f16).tex(f23, f22).endVertex();
                tessellator.draw();
            }

            GlStateManager.disableTexture2D();

            if (flag1)
            {
                Shaders.disableTexture2D();
            }

            float f24 = theWorld.getStarBrightness(partialTicks) * f15;

            if (f24 > 0.0F && Config.isStarsEnabled() && !CustomSky.hasSkyLayers(theWorld))
            {
                GlStateManager.color(f24, f24, f24, f24);

                if (vboEnabled)
                {
                    starVBO.bindBuffer();
                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
                    starVBO.drawArrays(7);
                    starVBO.unbindBuffer();
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                }
                else
                {
                    GlStateManager.callList(starGLCallList);
                }
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableFog();

            if (flag1)
            {
                Shaders.enableFog();
            }

            GlStateManager.popMatrix();
            GlStateManager.disableTexture2D();

            if (flag1)
            {
                Shaders.disableTexture2D();
            }

            GlStateManager.color(0.0F, 0.0F, 0.0F);
            double d0 = mc.thePlayer.getPositionEyes(partialTicks).yCoord - theWorld.getHorizon();

            if (d0 < 0.0D)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, 12.0F, 0.0F);

                if (vboEnabled)
                {
                    sky2VBO.bindBuffer();
                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
                    sky2VBO.drawArrays(7);
                    sky2VBO.unbindBuffer();
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                }
                else
                {
                    GlStateManager.callList(glSkyList2);
                }

                GlStateManager.popMatrix();
                float f17 = 1.0F;
                float f18 = -((float)(d0 + 65.0D));
                float f19 = -1.0F;
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos(-1.0D, f18, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, f18, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, f18, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, f18, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, f18, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, f18, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, f18, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, f18, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
            }

            if (theWorld.provider.isSkyColored())
            {
                GlStateManager.color(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F);
            }
            else
            {
                GlStateManager.color(f, f1, f2);
            }

            if (mc.gameSettings.renderDistanceChunks <= 4)
            {
                GlStateManager.color(mc.entityRenderer.fogColorRed, mc.entityRenderer.fogColorGreen, mc.entityRenderer.fogColorBlue);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, -((float)(d0 - 16.0D)), 0.0F);

            if (Config.isSkyEnabled())
            {
                GlStateManager.callList(glSkyList2);
            }

            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();

            if (flag1)
            {
                Shaders.enableTexture2D();
            }

            GlStateManager.depthMask(true);
        }
    }

    public void renderClouds(float partialTicks, int pass)
    {
        if (!Config.isCloudsOff())
        {
            if (Reflector.ForgeWorldProvider_getCloudRenderer.exists())
            {
                WorldProvider worldprovider = mc.theWorld.provider;
                Object object = Reflector.call(worldprovider, Reflector.ForgeWorldProvider_getCloudRenderer);

                if (object != null)
                {
                    Reflector.callVoid(object, Reflector.IRenderHandler_render, Float.valueOf(partialTicks), theWorld, mc);
                    return;
                }
            }

            if (mc.theWorld.provider.isSurfaceWorld())
            {
                if (Config.isShaders())
                {
                    Shaders.beginClouds();
                }

                if (Config.isCloudsFancy())
                {
                    renderCloudsFancy(partialTicks, pass);
                }
                else
                {
                    cloudRenderer.prepareToRender(false, cloudTickCounter, partialTicks);
                    partialTicks = 0.0F;
                    GlStateManager.disableCull();
                    float f9 = (float)(mc.getRenderViewEntity().lastTickPosY + (mc.getRenderViewEntity().posY - mc.getRenderViewEntity().lastTickPosY) * (double)partialTicks);
                    boolean flag = true;
                    boolean flag1 = true;
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                    renderEngine.bindTexture(RenderGlobal.locationCloudsPng);
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

                    if (cloudRenderer.shouldUpdateGlList())
                    {
                        cloudRenderer.startUpdateGlList();
                        Vec3 vec3 = theWorld.getCloudColour(partialTicks);
                        float f = (float)vec3.xCoord;
                        float f1 = (float)vec3.yCoord;
                        float f2 = (float)vec3.zCoord;

                        if (pass != 2)
                        {
                            float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
                            float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
                            float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
                            f = f3;
                            f1 = f4;
                            f2 = f5;
                        }

                        float f10 = 4.8828125E-4F;
                        double d2 = (float) cloudTickCounter + partialTicks;
                        double d0 = mc.getRenderViewEntity().prevPosX + (mc.getRenderViewEntity().posX - mc.getRenderViewEntity().prevPosX) * (double)partialTicks + d2 * 0.029999999329447746D;
                        double d1 = mc.getRenderViewEntity().prevPosZ + (mc.getRenderViewEntity().posZ - mc.getRenderViewEntity().prevPosZ) * (double)partialTicks;
                        int i = MathHelper.floor_double(d0 / 2048.0D);
                        int j = MathHelper.floor_double(d1 / 2048.0D);
                        d0 = d0 - (double)(i * 2048);
                        d1 = d1 - (double)(j * 2048);
                        float f6 = theWorld.provider.getCloudHeight() - f9 + 0.33F;
                        f6 = f6 + mc.gameSettings.ofCloudsHeight * 128.0F;
                        float f7 = (float)(d0 * 4.8828125E-4D);
                        float f8 = (float)(d1 * 4.8828125E-4D);
                        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

                        for (int k = -256; k < 256; k += 32)
                        {
                            for (int l = -256; l < 256; l += 32)
                            {
                                worldrenderer.pos(k + 0, f6, l + 32).tex((float)(k + 0) * 4.8828125E-4F + f7, (float)(l + 32) * 4.8828125E-4F + f8).color(f, f1, f2, 0.8F).endVertex();
                                worldrenderer.pos(k + 32, f6, l + 32).tex((float)(k + 32) * 4.8828125E-4F + f7, (float)(l + 32) * 4.8828125E-4F + f8).color(f, f1, f2, 0.8F).endVertex();
                                worldrenderer.pos(k + 32, f6, l + 0).tex((float)(k + 32) * 4.8828125E-4F + f7, (float)(l + 0) * 4.8828125E-4F + f8).color(f, f1, f2, 0.8F).endVertex();
                                worldrenderer.pos(k + 0, f6, l + 0).tex((float)(k + 0) * 4.8828125E-4F + f7, (float)(l + 0) * 4.8828125E-4F + f8).color(f, f1, f2, 0.8F).endVertex();
                            }
                        }

                        tessellator.draw();
                        cloudRenderer.endUpdateGlList();
                    }

                    cloudRenderer.renderGlList();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.disableBlend();
                    GlStateManager.enableCull();
                }

                if (Config.isShaders())
                {
                    Shaders.endClouds();
                }
            }
        }
    }

    /**
     * Checks if the given position is to be rendered with cloud fog
     */
    public boolean hasCloudFog(double x, double y, double z, float partialTicks)
    {
        return false;
    }

    private void renderCloudsFancy(float partialTicks, int pass)
    {
        cloudRenderer.prepareToRender(true, cloudTickCounter, partialTicks);
        partialTicks = 0.0F;
        GlStateManager.disableCull();
        float f = (float)(mc.getRenderViewEntity().lastTickPosY + (mc.getRenderViewEntity().posY - mc.getRenderViewEntity().lastTickPosY) * (double)partialTicks);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        float f1 = 12.0F;
        float f2 = 4.0F;
        double d0 = (float) cloudTickCounter + partialTicks;
        double d1 = (mc.getRenderViewEntity().prevPosX + (mc.getRenderViewEntity().posX - mc.getRenderViewEntity().prevPosX) * (double)partialTicks + d0 * 0.029999999329447746D) / 12.0D;
        double d2 = (mc.getRenderViewEntity().prevPosZ + (mc.getRenderViewEntity().posZ - mc.getRenderViewEntity().prevPosZ) * (double)partialTicks) / 12.0D + 0.33000001311302185D;
        float f3 = theWorld.provider.getCloudHeight() - f + 0.33F;
        f3 = f3 + mc.gameSettings.ofCloudsHeight * 128.0F;
        int i = MathHelper.floor_double(d1 / 2048.0D);
        int j = MathHelper.floor_double(d2 / 2048.0D);
        d1 = d1 - (double)(i * 2048);
        d2 = d2 - (double)(j * 2048);
        renderEngine.bindTexture(RenderGlobal.locationCloudsPng);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Vec3 vec3 = theWorld.getCloudColour(partialTicks);
        float f4 = (float)vec3.xCoord;
        float f5 = (float)vec3.yCoord;
        float f6 = (float)vec3.zCoord;

        if (pass != 2)
        {
            float f7 = (f4 * 30.0F + f5 * 59.0F + f6 * 11.0F) / 100.0F;
            float f8 = (f4 * 30.0F + f5 * 70.0F) / 100.0F;
            float f9 = (f4 * 30.0F + f6 * 70.0F) / 100.0F;
            f4 = f7;
            f5 = f8;
            f6 = f9;
        }

        float f26 = f4 * 0.9F;
        float f27 = f5 * 0.9F;
        float f28 = f6 * 0.9F;
        float f10 = f4 * 0.7F;
        float f11 = f5 * 0.7F;
        float f12 = f6 * 0.7F;
        float f13 = f4 * 0.8F;
        float f14 = f5 * 0.8F;
        float f15 = f6 * 0.8F;
        float f16 = 0.00390625F;
        float f17 = (float)MathHelper.floor_double(d1) * 0.00390625F;
        float f18 = (float)MathHelper.floor_double(d2) * 0.00390625F;
        float f19 = (float)(d1 - (double)MathHelper.floor_double(d1));
        float f20 = (float)(d2 - (double)MathHelper.floor_double(d2));
        boolean flag = true;
        boolean flag1 = true;
        float f21 = 9.765625E-4F;
        GlStateManager.scale(12.0F, 1.0F, 12.0F);

        for (int k = 0; k < 2; ++k)
        {
            if (k == 0)
            {
                GlStateManager.colorMask(false, false, false, false);
            }
            else
            {
                switch (pass)
                {
                    case 0:
                        GlStateManager.colorMask(false, true, true, true);
                        break;

                    case 1:
                        GlStateManager.colorMask(true, false, false, true);
                        break;

                    case 2:
                        GlStateManager.colorMask(true, true, true, true);
                }
            }

            cloudRenderer.renderGlList();
        }

        if (cloudRenderer.shouldUpdateGlList())
        {
            cloudRenderer.startUpdateGlList();

            for (int j1 = -3; j1 <= 4; ++j1)
            {
                for (int l = -3; l <= 4; ++l)
                {
                    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
                    float f22 = (float)(j1 * 8);
                    float f23 = (float)(l * 8);
                    float f24 = f22 - f19;
                    float f25 = f23 - f20;

                    if (f3 > -5.0F)
                    {
                        worldrenderer.pos(f24 + 0.0F, f3 + 0.0F, f25 + 8.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 8.0F, f3 + 0.0F, f25 + 8.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 8.0F, f3 + 0.0F, f25 + 0.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 0.0F, f3 + 0.0F, f25 + 0.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    }

                    if (f3 <= 5.0F)
                    {
                        worldrenderer.pos(f24 + 0.0F, f3 + 4.0F - 9.765625E-4F, f25 + 8.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 8.0F, f3 + 4.0F - 9.765625E-4F, f25 + 8.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 8.0F, f3 + 4.0F - 9.765625E-4F, f25 + 0.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 0.0F, f3 + 4.0F - 9.765625E-4F, f25 + 0.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                    }

                    if (j1 > -1)
                    {
                        for (int i1 = 0; i1 < 8; ++i1)
                        {
                            worldrenderer.pos(f24 + (float)i1 + 0.0F, f3 + 0.0F, f25 + 8.0F).tex((f22 + (float)i1 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float)i1 + 0.0F, f3 + 4.0F, f25 + 8.0F).tex((f22 + (float)i1 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float)i1 + 0.0F, f3 + 4.0F, f25 + 0.0F).tex((f22 + (float)i1 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float)i1 + 0.0F, f3 + 0.0F, f25 + 0.0F).tex((f22 + (float)i1 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                        }
                    }

                    if (j1 <= 1)
                    {
                        for (int k1 = 0; k1 < 8; ++k1)
                        {
                            worldrenderer.pos(f24 + (float)k1 + 1.0F - 9.765625E-4F, f3 + 0.0F, f25 + 8.0F).tex((f22 + (float)k1 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float)k1 + 1.0F - 9.765625E-4F, f3 + 4.0F, f25 + 8.0F).tex((f22 + (float)k1 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float)k1 + 1.0F - 9.765625E-4F, f3 + 4.0F, f25 + 0.0F).tex((f22 + (float)k1 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float)k1 + 1.0F - 9.765625E-4F, f3 + 0.0F, f25 + 0.0F).tex((f22 + (float)k1 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                        }
                    }

                    if (l > -1)
                    {
                        for (int l1 = 0; l1 < 8; ++l1)
                        {
                            worldrenderer.pos(f24 + 0.0F, f3 + 4.0F, f25 + (float)l1 + 0.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + (float)l1 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            worldrenderer.pos(f24 + 8.0F, f3 + 4.0F, f25 + (float)l1 + 0.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + (float)l1 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            worldrenderer.pos(f24 + 8.0F, f3 + 0.0F, f25 + (float)l1 + 0.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + (float)l1 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            worldrenderer.pos(f24 + 0.0F, f3 + 0.0F, f25 + (float)l1 + 0.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + (float)l1 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                        }
                    }

                    if (l <= 1)
                    {
                        for (int i2 = 0; i2 < 8; ++i2)
                        {
                            worldrenderer.pos(f24 + 0.0F, f3 + 4.0F, f25 + (float)i2 + 1.0F - 9.765625E-4F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + (float)i2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            worldrenderer.pos(f24 + 8.0F, f3 + 4.0F, f25 + (float)i2 + 1.0F - 9.765625E-4F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + (float)i2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            worldrenderer.pos(f24 + 8.0F, f3 + 0.0F, f25 + (float)i2 + 1.0F - 9.765625E-4F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + (float)i2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            worldrenderer.pos(f24 + 0.0F, f3 + 0.0F, f25 + (float)i2 + 1.0F - 9.765625E-4F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + (float)i2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                        }
                    }

                    tessellator.draw();
                }
            }

            cloudRenderer.endUpdateGlList();
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
    }

    public void updateChunks(long finishTimeNano)
    {
        finishTimeNano = (long)((double)finishTimeNano + 1.0E8D);
        displayListEntitiesDirty |= renderDispatcher.runChunkUploads(finishTimeNano);

        if (chunksToUpdateForced.size() > 0)
        {
            Iterator iterator = chunksToUpdateForced.iterator();

            while (iterator.hasNext())
            {
                RenderChunk renderchunk = (RenderChunk)iterator.next();

                if (!renderDispatcher.updateChunkLater(renderchunk))
                {
                    break;
                }

                renderchunk.setNeedsUpdate(false);
                iterator.remove();
                chunksToUpdate.remove(renderchunk);
                chunksToResortTransparency.remove(renderchunk);
            }
        }

        if (chunksToResortTransparency.size() > 0)
        {
            Iterator iterator2 = chunksToResortTransparency.iterator();

            if (iterator2.hasNext())
            {
                RenderChunk renderchunk2 = (RenderChunk)iterator2.next();

                if (renderDispatcher.updateTransparencyLater(renderchunk2))
                {
                    iterator2.remove();
                }
            }
        }

        int j = 0;
        int k = Config.getUpdatesPerFrame();
        int i = k * 2;
        Iterator iterator1 = chunksToUpdate.iterator();

        while (iterator1.hasNext())
        {
            RenderChunk renderchunk1 = (RenderChunk)iterator1.next();

            if (!renderDispatcher.updateChunkLater(renderchunk1))
            {
                break;
            }

            renderchunk1.setNeedsUpdate(false);
            iterator1.remove();

            if (renderchunk1.getCompiledChunk().isEmpty() && k < i)
            {
                ++k;
            }

            ++j;

            if (j >= k)
            {
                break;
            }
        }
    }

    public void renderWorldBorder(Entity p_180449_1_, float partialTicks)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        WorldBorder worldborder = theWorld.getWorldBorder();
        double d0 = mc.gameSettings.renderDistanceChunks * 16;

        if (p_180449_1_.posX >= worldborder.maxX() - d0 || p_180449_1_.posX <= worldborder.minX() + d0 || p_180449_1_.posZ >= worldborder.maxZ() - d0 || p_180449_1_.posZ <= worldborder.minZ() + d0)
        {
            double d1 = 1.0D - worldborder.getClosestDistance(p_180449_1_) / d0;
            d1 = Math.pow(d1, 4.0D);
            double d2 = p_180449_1_.lastTickPosX + (p_180449_1_.posX - p_180449_1_.lastTickPosX) * (double)partialTicks;
            double d3 = p_180449_1_.lastTickPosY + (p_180449_1_.posY - p_180449_1_.lastTickPosY) * (double)partialTicks;
            double d4 = p_180449_1_.lastTickPosZ + (p_180449_1_.posZ - p_180449_1_.lastTickPosZ) * (double)partialTicks;
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
            renderEngine.bindTexture(RenderGlobal.locationForcefieldPng);
            GlStateManager.depthMask(false);
            GlStateManager.pushMatrix();
            int i = worldborder.getStatus().getID();
            float f = (float)(i >> 16 & 255) / 255.0F;
            float f1 = (float)(i >> 8 & 255) / 255.0F;
            float f2 = (float)(i & 255) / 255.0F;
            GlStateManager.color(f, f1, f2, (float)d1);
            GlStateManager.doPolygonOffset(-3.0F, -3.0F);
            GlStateManager.enablePolygonOffset();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableAlpha();
            GlStateManager.disableCull();
            float f3 = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F;
            float f4 = 0.0F;
            float f5 = 0.0F;
            float f6 = 128.0F;
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.setTranslation(-d2, -d3, -d4);
            double d5 = Math.max(MathHelper.floor_double(d4 - d0), worldborder.minZ());
            double d6 = Math.min(MathHelper.ceiling_double_int(d4 + d0), worldborder.maxZ());

            if (d2 > worldborder.maxX() - d0)
            {
                float f7 = 0.0F;

                for (double d7 = d5; d7 < d6; f7 += 0.5F)
                {
                    double d8 = Math.min(1.0D, d6 - d7);
                    float f8 = (float)d8 * 0.5F;
                    worldrenderer.pos(worldborder.maxX(), 256.0D, d7).tex(f3 + f7, f3 + 0.0F).endVertex();
                    worldrenderer.pos(worldborder.maxX(), 256.0D, d7 + d8).tex(f3 + f8 + f7, f3 + 0.0F).endVertex();
                    worldrenderer.pos(worldborder.maxX(), 0.0D, d7 + d8).tex(f3 + f8 + f7, f3 + 128.0F).endVertex();
                    worldrenderer.pos(worldborder.maxX(), 0.0D, d7).tex(f3 + f7, f3 + 128.0F).endVertex();
                    ++d7;
                }
            }

            if (d2 < worldborder.minX() + d0)
            {
                float f9 = 0.0F;

                for (double d9 = d5; d9 < d6; f9 += 0.5F)
                {
                    double d12 = Math.min(1.0D, d6 - d9);
                    float f12 = (float)d12 * 0.5F;
                    worldrenderer.pos(worldborder.minX(), 256.0D, d9).tex(f3 + f9, f3 + 0.0F).endVertex();
                    worldrenderer.pos(worldborder.minX(), 256.0D, d9 + d12).tex(f3 + f12 + f9, f3 + 0.0F).endVertex();
                    worldrenderer.pos(worldborder.minX(), 0.0D, d9 + d12).tex(f3 + f12 + f9, f3 + 128.0F).endVertex();
                    worldrenderer.pos(worldborder.minX(), 0.0D, d9).tex(f3 + f9, f3 + 128.0F).endVertex();
                    ++d9;
                }
            }

            d5 = Math.max(MathHelper.floor_double(d2 - d0), worldborder.minX());
            d6 = Math.min(MathHelper.ceiling_double_int(d2 + d0), worldborder.maxX());

            if (d4 > worldborder.maxZ() - d0)
            {
                float f10 = 0.0F;

                for (double d10 = d5; d10 < d6; f10 += 0.5F)
                {
                    double d13 = Math.min(1.0D, d6 - d10);
                    float f13 = (float)d13 * 0.5F;
                    worldrenderer.pos(d10, 256.0D, worldborder.maxZ()).tex(f3 + f10, f3 + 0.0F).endVertex();
                    worldrenderer.pos(d10 + d13, 256.0D, worldborder.maxZ()).tex(f3 + f13 + f10, f3 + 0.0F).endVertex();
                    worldrenderer.pos(d10 + d13, 0.0D, worldborder.maxZ()).tex(f3 + f13 + f10, f3 + 128.0F).endVertex();
                    worldrenderer.pos(d10, 0.0D, worldborder.maxZ()).tex(f3 + f10, f3 + 128.0F).endVertex();
                    ++d10;
                }
            }

            if (d4 < worldborder.minZ() + d0)
            {
                float f11 = 0.0F;

                for (double d11 = d5; d11 < d6; f11 += 0.5F)
                {
                    double d14 = Math.min(1.0D, d6 - d11);
                    float f14 = (float)d14 * 0.5F;
                    worldrenderer.pos(d11, 256.0D, worldborder.minZ()).tex(f3 + f11, f3 + 0.0F).endVertex();
                    worldrenderer.pos(d11 + d14, 256.0D, worldborder.minZ()).tex(f3 + f14 + f11, f3 + 0.0F).endVertex();
                    worldrenderer.pos(d11 + d14, 0.0D, worldborder.minZ()).tex(f3 + f14 + f11, f3 + 128.0F).endVertex();
                    worldrenderer.pos(d11, 0.0D, worldborder.minZ()).tex(f3 + f11, f3 + 128.0F).endVertex();
                    ++d11;
                }
            }

            tessellator.draw();
            worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
            GlStateManager.enableCull();
            GlStateManager.disableAlpha();
            GlStateManager.doPolygonOffset(0.0F, 0.0F);
            GlStateManager.disablePolygonOffset();
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);
        }
    }

    private void preRenderDamagedBlocks()
    {
        GlStateManager.tryBlendFuncSeparate(774, 768, 1, 0);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.doPolygonOffset(-3.0F, -3.0F);
        GlStateManager.enablePolygonOffset();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();

        if (Config.isShaders())
        {
            ShadersRender.beginBlockDamage();
        }
    }

    private void postRenderDamagedBlocks()
    {
        GlStateManager.disableAlpha();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();

        if (Config.isShaders())
        {
            ShadersRender.endBlockDamage();
        }
    }

    public void drawBlockDamageTexture(Tessellator tessellatorIn, WorldRenderer worldRendererIn, Entity entityIn, float partialTicks)
    {
        double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;

        if (!damagedBlocks.isEmpty())
        {
            renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            preRenderDamagedBlocks();
            worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
            worldRendererIn.setTranslation(-d0, -d1, -d2);
            worldRendererIn.markDirty();
            Iterator iterator = damagedBlocks.values().iterator();

            while (iterator.hasNext())
            {
                DestroyBlockProgress destroyblockprogress = (DestroyBlockProgress)iterator.next();
                BlockPos blockpos = destroyblockprogress.getPosition();
                double d3 = (double)blockpos.getX() - d0;
                double d4 = (double)blockpos.getY() - d1;
                double d5 = (double)blockpos.getZ() - d2;
                Block block = theWorld.getBlockState(blockpos).getBlock();
                boolean flag;

                if (Reflector.ForgeTileEntity_canRenderBreaking.exists())
                {
                    boolean flag1 = block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockSign || block instanceof BlockSkull;

                    if (!flag1)
                    {
                        TileEntity tileentity = theWorld.getTileEntity(blockpos);

                        if (tileentity != null)
                        {
                            flag1 = Reflector.callBoolean(tileentity, Reflector.ForgeTileEntity_canRenderBreaking);
                        }
                    }

                    flag = !flag1;
                }
                else
                {
                    flag = !(block instanceof BlockChest) && !(block instanceof BlockEnderChest) && !(block instanceof BlockSign) && !(block instanceof BlockSkull);
                }

                if (flag)
                {
                    if (d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D)
                    {
                        iterator.remove();
                    }
                    else
                    {
                        IBlockState iblockstate = theWorld.getBlockState(blockpos);

                        if (iblockstate.getBlock().getMaterial() != Material.air)
                        {
                            int i = destroyblockprogress.getPartialBlockDamage();
                            TextureAtlasSprite textureatlassprite = destroyBlockIcons[i];
                            BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
                            blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, theWorld);
                        }
                    }
                }
            }

            tessellatorIn.draw();
            worldRendererIn.setTranslation(0.0D, 0.0D, 0.0D);
            postRenderDamagedBlocks();
        }
    }

    /**
     * Draws the selection box for the player. Args: entityPlayer, rayTraceHit, i, itemStack, partialTickTime
     */
    public void drawSelectionBox(EntityPlayer player, MovingObjectPosition movingObjectPositionIn, int p_72731_3_, float partialTicks)
    {
        if (p_72731_3_ == 0 && movingObjectPositionIn.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
        {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
            GL11.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();

            if (Config.isShaders())
            {
                Shaders.disableTexture2D();
            }

            GlStateManager.depthMask(false);
            float f = 0.002F;
            BlockPos blockpos = movingObjectPositionIn.getBlockPos();
            Block block = theWorld.getBlockState(blockpos).getBlock();

            if (block.getMaterial() != Material.air && theWorld.getWorldBorder().contains(blockpos))
            {
                block.setBlockBoundsBasedOnState(theWorld, blockpos);
                double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
                double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
                double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
                RenderGlobal.func_181561_a(block.getSelectedBoundingBox(theWorld, blockpos).expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D).offset(-d0, -d1, -d2));
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();

            if (Config.isShaders())
            {
                Shaders.enableTexture2D();
            }

            GlStateManager.disableBlend();
        }
    }

    public static void func_181561_a(AxisAlignedBB p_181561_0_)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(1, DefaultVertexFormats.POSITION);
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
        tessellator.draw();
    }

    public static void func_181563_a(AxisAlignedBB p_181563_0_, int p_181563_1_, int p_181563_2_, int p_181563_3_, int p_181563_4_)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.minY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        tessellator.draw();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.maxY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        tessellator.draw();
        worldrenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.minY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.maxY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        tessellator.draw();
    }

    /**
     * Marks the blocks in the given range for update
     */
    private void markBlocksForUpdate(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        viewFrustum.markBlocksForUpdate(x1, y1, z1, x2, y2, z2);
    }

    public void markBlockForUpdate(BlockPos pos)
    {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1);
    }

    public void notifyLightSet(BlockPos pos)
    {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1);
    }

    /**
     * On the client, re-renders all blocks in this range, inclusive. On the server, does nothing. Args: min x, min y,
     * min z, max x, max y, max z
     */
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        markBlocksForUpdate(x1 - 1, y1 - 1, z1 - 1, x2 + 1, y2 + 1, z2 + 1);
    }

    public void playRecord(String recordName, BlockPos blockPosIn)
    {
        ISound isound = (ISound) mapSoundPositions.get(blockPosIn);

        if (isound != null)
        {
            mc.getSoundHandler().stopSound(isound);
            mapSoundPositions.remove(blockPosIn);
        }

        if (recordName != null)
        {
            ItemRecord itemrecord = ItemRecord.getRecord(recordName);

            if (itemrecord != null)
            {
                mc.ingameGUI.setRecordPlayingMessage(itemrecord.getRecordNameLocal());
            }

            ResourceLocation resourcelocation = null;

            if (Reflector.ForgeItemRecord_getRecordResource.exists() && itemrecord != null)
            {
                resourcelocation = (ResourceLocation)Reflector.call(itemrecord, Reflector.ForgeItemRecord_getRecordResource, new Object[] {recordName});
            }

            if (resourcelocation == null)
            {
                resourcelocation = new ResourceLocation(recordName);
            }

            PositionedSoundRecord positionedsoundrecord = PositionedSoundRecord.create(resourcelocation, (float)blockPosIn.getX(), (float)blockPosIn.getY(), (float)blockPosIn.getZ());
            mapSoundPositions.put(blockPosIn, positionedsoundrecord);
            mc.getSoundHandler().playSound(positionedsoundrecord);
        }
    }

    /**
     * Plays the specified sound. Arg: soundName, x, y, z, volume, pitch
     */
    public void playSound(String soundName, double x, double y, double z, float volume, float pitch)
    {
    }

    /**
     * Plays sound to all near players except the player reference given
     */
    public void playSoundToNearExcept(EntityPlayer except, String soundName, double x, double y, double z, float volume, float pitch)
    {
    }

    public void spawnParticle(int particleID, boolean ignoreRange, final double xCoord, final double yCoord, final double zCoord, double xOffset, double yOffset, double zOffset, int... p_180442_15_)
    {
        try
        {
            spawnEntityFX(particleID, ignoreRange, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, p_180442_15_);
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while adding particle");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being added");
            crashreportcategory.addCrashSection("ID", Integer.valueOf(particleID));

            if (p_180442_15_ != null)
            {
                crashreportcategory.addCrashSection("Parameters", p_180442_15_);
            }

            crashreportcategory.addCrashSectionCallable("Position", new Callable()
            {
                private static final String __OBFID = "CL_00000955";
                public String call() throws Exception
                {
                    return CrashReportCategory.getCoordinateInfo(xCoord, yCoord, zCoord);
                }
            });
            throw new ReportedException(crashreport);
        }
    }

    private void spawnParticle(EnumParticleTypes particleIn, double p_174972_2_, double p_174972_4_, double p_174972_6_, double p_174972_8_, double p_174972_10_, double p_174972_12_, int... p_174972_14_)
    {
        spawnParticle(particleIn.getParticleID(), particleIn.getShouldIgnoreRange(), p_174972_2_, p_174972_4_, p_174972_6_, p_174972_8_, p_174972_10_, p_174972_12_, p_174972_14_);
    }

    private EntityFX spawnEntityFX(int p_174974_1_, boolean ignoreRange, double p_174974_3_, double p_174974_5_, double p_174974_7_, double p_174974_9_, double p_174974_11_, double p_174974_13_, int... p_174974_15_)
    {
        if (mc != null && mc.getRenderViewEntity() != null && mc.effectRenderer != null)
        {
            int i = mc.gameSettings.particleSetting;

            if (i == 1 && theWorld.rand.nextInt(3) == 0)
            {
                i = 2;
            }

            double d0 = mc.getRenderViewEntity().posX - p_174974_3_;
            double d1 = mc.getRenderViewEntity().posY - p_174974_5_;
            double d2 = mc.getRenderViewEntity().posZ - p_174974_7_;

            if (p_174974_1_ == EnumParticleTypes.EXPLOSION_HUGE.getParticleID() && !Config.isAnimatedExplosion())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.EXPLOSION_LARGE.getParticleID() && !Config.isAnimatedExplosion())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.EXPLOSION_NORMAL.getParticleID() && !Config.isAnimatedExplosion())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.SUSPENDED.getParticleID() && !Config.isWaterParticles())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.SUSPENDED_DEPTH.getParticleID() && !Config.isVoidParticles())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.SMOKE_NORMAL.getParticleID() && !Config.isAnimatedSmoke())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.SMOKE_LARGE.getParticleID() && !Config.isAnimatedSmoke())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.SPELL_MOB.getParticleID() && !Config.isPotionParticles())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.SPELL_MOB_AMBIENT.getParticleID() && !Config.isPotionParticles())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.SPELL.getParticleID() && !Config.isPotionParticles())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.SPELL_INSTANT.getParticleID() && !Config.isPotionParticles())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.SPELL_WITCH.getParticleID() && !Config.isPotionParticles())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.PORTAL.getParticleID() && !Config.isAnimatedPortal())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.FLAME.getParticleID() && !Config.isAnimatedFlame())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.REDSTONE.getParticleID() && !Config.isAnimatedRedstone())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.DRIP_WATER.getParticleID() && !Config.isDrippingWaterLava())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.DRIP_LAVA.getParticleID() && !Config.isDrippingWaterLava())
            {
                return null;
            }
            else if (p_174974_1_ == EnumParticleTypes.FIREWORKS_SPARK.getParticleID() && !Config.isFireworkParticles())
            {
                return null;
            }
            else if (ignoreRange)
            {
                return mc.effectRenderer.spawnEffectParticle(p_174974_1_, p_174974_3_, p_174974_5_, p_174974_7_, p_174974_9_, p_174974_11_, p_174974_13_, p_174974_15_);
            }
            else
            {
                double d3 = 16.0D;
                double d4 = 256.0D;

                if (p_174974_1_ == EnumParticleTypes.CRIT.getParticleID())
                {
                    d4 = 38416.0D;
                }

                if (d0 * d0 + d1 * d1 + d2 * d2 > d4)
                {
                    return null;
                }
                else if (i > 1)
                {
                    return null;
                }
                else
                {
                    EntityFX entityfx = mc.effectRenderer.spawnEffectParticle(p_174974_1_, p_174974_3_, p_174974_5_, p_174974_7_, p_174974_9_, p_174974_11_, p_174974_13_, p_174974_15_);

                    if (p_174974_1_ == EnumParticleTypes.WATER_BUBBLE.getParticleID())
                    {
                        CustomColors.updateWaterFX(entityfx, theWorld, p_174974_3_, p_174974_5_, p_174974_7_);
                    }

                    if (p_174974_1_ == EnumParticleTypes.WATER_SPLASH.getParticleID())
                    {
                        CustomColors.updateWaterFX(entityfx, theWorld, p_174974_3_, p_174974_5_, p_174974_7_);
                    }

                    if (p_174974_1_ == EnumParticleTypes.WATER_DROP.getParticleID())
                    {
                        CustomColors.updateWaterFX(entityfx, theWorld, p_174974_3_, p_174974_5_, p_174974_7_);
                    }

                    if (p_174974_1_ == EnumParticleTypes.TOWN_AURA.getParticleID())
                    {
                        CustomColors.updateMyceliumFX(entityfx);
                    }

                    if (p_174974_1_ == EnumParticleTypes.PORTAL.getParticleID())
                    {
                        CustomColors.updatePortalFX(entityfx);
                    }

                    if (p_174974_1_ == EnumParticleTypes.REDSTONE.getParticleID())
                    {
                        CustomColors.updateReddustFX(entityfx, theWorld, p_174974_3_, p_174974_5_, p_174974_7_);
                    }

                    return entityfx;
                }
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * Called on all IWorldAccesses when an entity is created or loaded. On client worlds, starts downloading any
     * necessary textures. On server worlds, adds the entity to the entity tracker.
     */
    public void onEntityAdded(Entity entityIn)
    {
        RandomMobs.entityLoaded(entityIn, theWorld);

        if (Config.isDynamicLights())
        {
            DynamicLights.entityAdded(entityIn, this);
        }
    }

    /**
     * Called on all IWorldAccesses when an entity is unloaded or destroyed. On client worlds, releases any downloaded
     * textures. On server worlds, removes the entity from the entity tracker.
     */
    public void onEntityRemoved(Entity entityIn)
    {
        if (Config.isDynamicLights())
        {
            DynamicLights.entityRemoved(entityIn, this);
        }
    }

    /**
     * Deletes all display lists
     */
    public void deleteAllDisplayLists()
    {
    }

    public void broadcastSound(int p_180440_1_, BlockPos p_180440_2_, int p_180440_3_)
    {
        switch (p_180440_1_)
        {
            case 1013:
            case 1018:
                if (mc.getRenderViewEntity() != null)
                {
                    double d0 = (double)p_180440_2_.getX() - mc.getRenderViewEntity().posX;
                    double d1 = (double)p_180440_2_.getY() - mc.getRenderViewEntity().posY;
                    double d2 = (double)p_180440_2_.getZ() - mc.getRenderViewEntity().posZ;
                    double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    double d4 = mc.getRenderViewEntity().posX;
                    double d5 = mc.getRenderViewEntity().posY;
                    double d6 = mc.getRenderViewEntity().posZ;

                    if (d3 > 0.0D)
                    {
                        d4 += d0 / d3 * 2.0D;
                        d5 += d1 / d3 * 2.0D;
                        d6 += d2 / d3 * 2.0D;
                    }

                    if (p_180440_1_ == 1013)
                    {
                        theWorld.playSound(d4, d5, d6, "mob.wither.spawn", 1.0F, 1.0F, false);
                    }
                    else
                    {
                        theWorld.playSound(d4, d5, d6, "mob.enderdragon.end", 5.0F, 1.0F, false);
                    }
                }

            default:
        }
    }

    public void playAuxSFX(EntityPlayer player, int sfxType, BlockPos blockPosIn, int p_180439_4_)
    {
        Random random = theWorld.rand;

        switch (sfxType)
        {
            case 1000:
                theWorld.playSoundAtPos(blockPosIn, "random.click", 1.0F, 1.0F, false);
                break;

            case 1001:
                theWorld.playSoundAtPos(blockPosIn, "random.click", 1.0F, 1.2F, false);
                break;

            case 1002:
                theWorld.playSoundAtPos(blockPosIn, "random.bow", 1.0F, 1.2F, false);
                break;

            case 1003:
                theWorld.playSoundAtPos(blockPosIn, "random.door_open", 1.0F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1004:
                theWorld.playSoundAtPos(blockPosIn, "random.fizz", 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
                break;

            case 1005:
                if (Item.getItemById(p_180439_4_) instanceof ItemRecord)
                {
                    theWorld.playRecord(blockPosIn, "records." + ((ItemRecord)Item.getItemById(p_180439_4_)).recordName);
                }
                else
                {
                    theWorld.playRecord(blockPosIn, null);
                }

                break;

            case 1006:
                theWorld.playSoundAtPos(blockPosIn, "random.door_close", 1.0F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1007:
                theWorld.playSoundAtPos(blockPosIn, "mob.ghast.charge", 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1008:
                theWorld.playSoundAtPos(blockPosIn, "mob.ghast.fireball", 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1009:
                theWorld.playSoundAtPos(blockPosIn, "mob.ghast.fireball", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1010:
                theWorld.playSoundAtPos(blockPosIn, "mob.zombie.wood", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1011:
                theWorld.playSoundAtPos(blockPosIn, "mob.zombie.metal", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1012:
                theWorld.playSoundAtPos(blockPosIn, "mob.zombie.woodbreak", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1014:
                theWorld.playSoundAtPos(blockPosIn, "mob.wither.shoot", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1015:
                theWorld.playSoundAtPos(blockPosIn, "mob.bat.takeoff", 0.05F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1016:
                theWorld.playSoundAtPos(blockPosIn, "mob.zombie.infect", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1017:
                theWorld.playSoundAtPos(blockPosIn, "mob.zombie.unfect", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1020:
                theWorld.playSoundAtPos(blockPosIn, "random.anvil_break", 1.0F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1021:
                theWorld.playSoundAtPos(blockPosIn, "random.anvil_use", 1.0F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1022:
                theWorld.playSoundAtPos(blockPosIn, "random.anvil_land", 0.3F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 2000:
                int k = p_180439_4_ % 3 - 1;
                int l = p_180439_4_ / 3 % 3 - 1;
                double d13 = (double)blockPosIn.getX() + (double)k * 0.6D + 0.5D;
                double d15 = (double)blockPosIn.getY() + 0.5D;
                double d19 = (double)blockPosIn.getZ() + (double)l * 0.6D + 0.5D;

                for (int l1 = 0; l1 < 10; ++l1)
                {
                    double d20 = random.nextDouble() * 0.2D + 0.01D;
                    double d21 = d13 + (double)k * 0.01D + (random.nextDouble() - 0.5D) * (double)l * 0.5D;
                    double d22 = d15 + (random.nextDouble() - 0.5D) * 0.5D;
                    double d23 = d19 + (double)l * 0.01D + (random.nextDouble() - 0.5D) * (double)k * 0.5D;
                    double d24 = (double)k * d20 + random.nextGaussian() * 0.01D;
                    double d9 = -0.03D + random.nextGaussian() * 0.01D;
                    double d10 = (double)l * d20 + random.nextGaussian() * 0.01D;
                    spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d21, d22, d23, d24, d9, d10);
                }

                return;

            case 2001:
                Block block = Block.getBlockById(p_180439_4_ & 4095);

                if (block.getMaterial() != Material.air)
                {
                    mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(block.stepSound.getBreakSound()), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getFrequency() * 0.8F, (float)blockPosIn.getX() + 0.5F, (float)blockPosIn.getY() + 0.5F, (float)blockPosIn.getZ() + 0.5F));
                }

                mc.effectRenderer.addBlockDestroyEffects(blockPosIn, block.getStateFromMeta(p_180439_4_ >> 12 & 255));
                break;

            case 2002:
                double d11 = blockPosIn.getX();
                double d12 = blockPosIn.getY();
                double d14 = blockPosIn.getZ();

                for (int i1 = 0; i1 < 8; ++i1)
                {
                    spawnParticle(EnumParticleTypes.ITEM_CRACK, d11, d12, d14, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D, Item.getIdFromItem(Items.potionitem), p_180439_4_);
                }

                int j1 = Items.potionitem.getColorFromDamage(p_180439_4_);
                float f = (float)(j1 >> 16 & 255) / 255.0F;
                float f1 = (float)(j1 >> 8 & 255) / 255.0F;
                float f2 = (float)(j1 >> 0 & 255) / 255.0F;
                EnumParticleTypes enumparticletypes = EnumParticleTypes.SPELL;

                if (Items.potionitem.isEffectInstant(p_180439_4_))
                {
                    enumparticletypes = EnumParticleTypes.SPELL_INSTANT;
                }

                for (int k1 = 0; k1 < 100; ++k1)
                {
                    double d16 = random.nextDouble() * 4.0D;
                    double d17 = random.nextDouble() * Math.PI * 2.0D;
                    double d18 = Math.cos(d17) * d16;
                    double d7 = 0.01D + random.nextDouble() * 0.5D;
                    double d8 = Math.sin(d17) * d16;
                    EntityFX entityfx = spawnEntityFX(enumparticletypes.getParticleID(), enumparticletypes.getShouldIgnoreRange(), d11 + d18 * 0.1D, d12 + 0.3D, d14 + d8 * 0.1D, d18, d7, d8);

                    if (entityfx != null)
                    {
                        float f3 = 0.75F + random.nextFloat() * 0.25F;
                        entityfx.setRBGColorF(f * f3, f1 * f3, f2 * f3);
                        entityfx.multiplyVelocity((float)d16);
                    }
                }

                theWorld.playSoundAtPos(blockPosIn, "game.potion.smash", 1.0F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 2003:
                double var7 = (double)blockPosIn.getX() + 0.5D;
                double var9 = blockPosIn.getY();
                double var11 = (double)blockPosIn.getZ() + 0.5D;

                for (int var13 = 0; var13 < 8; ++var13)
                {
                    spawnParticle(EnumParticleTypes.ITEM_CRACK, var7, var9, var11, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D, Item.getIdFromItem(Items.ender_eye));
                }

                for (double var32 = 0.0D; var32 < (Math.PI * 2D); var32 += 0.15707963267948966D)
                {
                    spawnParticle(EnumParticleTypes.PORTAL, var7 + Math.cos(var32) * 5.0D, var9 - 0.4D, var11 + Math.sin(var32) * 5.0D, Math.cos(var32) * -5.0D, 0.0D, Math.sin(var32) * -5.0D);
                    spawnParticle(EnumParticleTypes.PORTAL, var7 + Math.cos(var32) * 5.0D, var9 - 0.4D, var11 + Math.sin(var32) * 5.0D, Math.cos(var32) * -7.0D, 0.0D, Math.sin(var32) * -7.0D);
                }

                return;

            case 2004:
                for (int var18 = 0; var18 < 20; ++var18)
                {
                    double d3 = (double)blockPosIn.getX() + 0.5D + ((double) theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    double d4 = (double)blockPosIn.getY() + 0.5D + ((double) theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    double d5 = (double)blockPosIn.getZ() + 0.5D + ((double) theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    theWorld.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d3, d4, d5, 0.0D, 0.0D, 0.0D);
                    theWorld.spawnParticle(EnumParticleTypes.FLAME, d3, d4, d5, 0.0D, 0.0D, 0.0D);
                }

                return;

            case 2005:
                ItemDye.spawnBonemealParticles(theWorld, blockPosIn, p_180439_4_);
        }
    }

    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress)
    {
        if (progress >= 0 && progress < 10)
        {
            DestroyBlockProgress destroyblockprogress = (DestroyBlockProgress) damagedBlocks.get(Integer.valueOf(breakerId));

            if (destroyblockprogress == null || destroyblockprogress.getPosition().getX() != pos.getX() || destroyblockprogress.getPosition().getY() != pos.getY() || destroyblockprogress.getPosition().getZ() != pos.getZ())
            {
                destroyblockprogress = new DestroyBlockProgress(breakerId, pos);
                damagedBlocks.put(Integer.valueOf(breakerId), destroyblockprogress);
            }

            destroyblockprogress.setPartialBlockDamage(progress);
            destroyblockprogress.setCloudUpdateTick(cloudTickCounter);
        }
        else
        {
            damagedBlocks.remove(Integer.valueOf(breakerId));
        }
    }

    public void setDisplayListEntitiesDirty()
    {
        displayListEntitiesDirty = true;
    }

    public void resetClouds()
    {
        cloudRenderer.reset();
    }

    public int getCountRenderers()
    {
        return viewFrustum.renderChunks.length;
    }

    public int getCountActiveRenderers()
    {
        return renderInfos.size();
    }

    public int getCountEntitiesRendered()
    {
        return countEntitiesRendered;
    }

    public int getCountTileEntitiesRendered()
    {
        return countTileEntitiesRendered;
    }

    public RenderChunk getRenderChunk(BlockPos p_getRenderChunk_1_)
    {
        return viewFrustum.getRenderChunk(p_getRenderChunk_1_);
    }

    public RenderChunk getRenderChunk(RenderChunk p_getRenderChunk_1_, EnumFacing p_getRenderChunk_2_)
    {
        if (p_getRenderChunk_1_ == null)
        {
            return null;
        }
        else
        {
            BlockPos blockpos = p_getRenderChunk_1_.func_181701_a(p_getRenderChunk_2_);
            return viewFrustum.getRenderChunk(blockpos);
        }
    }

    public WorldClient getWorld()
    {
        return theWorld;
    }

    public void func_181023_a(Collection p_181023_1_, Collection p_181023_2_)
    {
        Set set = field_181024_n;

        synchronized (field_181024_n)
        {
            field_181024_n.removeAll(p_181023_1_);
            field_181024_n.addAll(p_181023_2_);
        }
    }

    static final class RenderGlobal$2
    {
        static final int[] field_178037_a = new int[VertexFormatElement.EnumUsage.values().length];
        private static final String __OBFID = "CL_00002535";

        static
        {
            try
            {
                RenderGlobal$2.field_178037_a[VertexFormatElement.EnumUsage.POSITION.ordinal()] = 1;
            }
            catch (NoSuchFieldError var3)
            {
            }

            try
            {
                RenderGlobal$2.field_178037_a[VertexFormatElement.EnumUsage.UV.ordinal()] = 2;
            }
            catch (NoSuchFieldError var2)
            {
            }

            try
            {
                RenderGlobal$2.field_178037_a[VertexFormatElement.EnumUsage.COLOR.ordinal()] = 3;
            }
            catch (NoSuchFieldError var1)
            {
            }
        }
    }

    public static class ContainerLocalRenderInformation
    {
        final RenderChunk renderChunk;
        final EnumFacing facing;
        final Set setFacing;
        final int counter;
        private static final String __OBFID = "CL_00002534";

        public ContainerLocalRenderInformation(RenderChunk p_i4_1_, EnumFacing p_i4_2_, int p_i4_3_)
        {
            setFacing = EnumSet.noneOf(EnumFacing.class);
            renderChunk = p_i4_1_;
            facing = p_i4_2_;
            counter = p_i4_3_;
        }

        ContainerLocalRenderInformation(RenderChunk p_i5_1_, EnumFacing p_i5_2_, int p_i5_3_, Object p_i5_4_)
        {
            this(p_i5_1_, p_i5_2_, p_i5_3_);
        }
    }
}
