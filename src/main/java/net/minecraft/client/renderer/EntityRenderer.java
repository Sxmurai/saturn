package net.minecraft.client.renderer;

import com.google.common.base.Predicates;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MouseFilter;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeGenBase;
import optifine.Config;
import optifine.CustomColors;
import optifine.Lagometer;
import optifine.RandomMobs;
import optifine.Reflector;
import optifine.ReflectorForge;
import optifine.TextureUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Project;
import shadersmod.client.Shaders;
import shadersmod.client.ShadersRender;
import wtf.saturn.feature.cache.impl.module.ModuleCache;
import wtf.saturn.feature.impl.modules.visuals.SmoothZoom;

public class EntityRenderer implements IResourceManagerReloadListener
{
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation locationRainPng = new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation locationSnowPng = new ResourceLocation("textures/environment/snow.png");
    public static boolean anaglyphEnable;

    /** Anaglyph field (0=R, 1=GB) */
    public static int anaglyphField;

    /** A reference to the Minecraft object. */
    private final Minecraft mc;
    private final IResourceManager resourceManager;
    private final Random random = new Random();
    private float farPlaneDistance;
    public ItemRenderer itemRenderer;
    private final MapItemRenderer theMapItemRenderer;

    /** Entity renderer update count */
    private int rendererUpdateCount;

    /** Pointed entity */
    private Entity pointedEntity;
    private MouseFilter mouseFilterXAxis = new MouseFilter();
    private MouseFilter mouseFilterYAxis = new MouseFilter();
    private final float thirdPersonDistance = 4.0F;

    /** Third person distance temp */
    private float thirdPersonDistanceTemp = 4.0F;

    /** Smooth cam yaw */
    private float smoothCamYaw;

    /** Smooth cam pitch */
    private float smoothCamPitch;

    /** Smooth cam filter X */
    private float smoothCamFilterX;

    /** Smooth cam filter Y */
    private float smoothCamFilterY;

    /** Smooth cam partial ticks */
    private float smoothCamPartialTicks;

    /** FOV modifier hand */
    private float fovModifierHand;

    /** FOV modifier hand prev */
    private float fovModifierHandPrev;
    private float bossColorModifier;
    private float bossColorModifierPrev;

    /** Cloud fog mode */
    private boolean cloudFog;
    private final boolean renderHand = true;
    private final boolean drawBlockOutline = true;

    /** Previous frame time in milliseconds */
    private long prevFrameTime = Minecraft.getSystemTime();

    /** End time of last render (ns) */
    private long renderEndNanoTime;

    /**
     * The texture id of the blocklight/skylight texture used for lighting effects
     */
    private final DynamicTexture lightmapTexture;

    /**
     * Colors computed in updateLightmap() and loaded into the lightmap emptyTexture
     */
    private final int[] lightmapColors;
    private final ResourceLocation locationLightMap;

    /**
     * Is set, updateCameraAndRender() calls updateLightmap(); set by updateTorchFlicker()
     */
    private boolean lightmapUpdateNeeded;

    /** Torch flicker X */
    private float torchFlickerX;
    private float torchFlickerDX;

    /** Rain sound counter */
    private int rainSoundCounter;
    private final float[] rainXCoords = new float[1024];
    private final float[] rainYCoords = new float[1024];

    /** Fog color buffer */
    private final FloatBuffer fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);
    public float fogColorRed;
    public float fogColorGreen;
    public float fogColorBlue;

    /** Fog color 2 */
    private float fogColor2;

    /** Fog color 1 */
    private float fogColor1;
    private final int debugViewDirection = 0;
    private final boolean debugView = false;
    private final double cameraZoom = 1.0D;
    private double cameraYaw;
    private double cameraPitch;
    private ShaderGroup theShaderGroup;
    private static final ResourceLocation[] shaderResourceLocations = new ResourceLocation[] {new ResourceLocation("shaders/post/notch.json"), new ResourceLocation("shaders/post/fxaa.json"), new ResourceLocation("shaders/post/art.json"), new ResourceLocation("shaders/post/bumpy.json"), new ResourceLocation("shaders/post/blobs2.json"), new ResourceLocation("shaders/post/pencil.json"), new ResourceLocation("shaders/post/color_convolve.json"), new ResourceLocation("shaders/post/deconverge.json"), new ResourceLocation("shaders/post/flip.json"), new ResourceLocation("shaders/post/invert.json"), new ResourceLocation("shaders/post/ntsc.json"), new ResourceLocation("shaders/post/outline.json"), new ResourceLocation("shaders/post/phosphor.json"), new ResourceLocation("shaders/post/scan_pincushion.json"), new ResourceLocation("shaders/post/sobel.json"), new ResourceLocation("shaders/post/bits.json"), new ResourceLocation("shaders/post/desaturate.json"), new ResourceLocation("shaders/post/green.json"), new ResourceLocation("shaders/post/blur.json"), new ResourceLocation("shaders/post/wobble.json"), new ResourceLocation("shaders/post/blobs.json"), new ResourceLocation("shaders/post/antialias.json"), new ResourceLocation("shaders/post/creeper.json"), new ResourceLocation("shaders/post/spider.json")};
    public static final int shaderCount = EntityRenderer.shaderResourceLocations.length;
    private int shaderIndex;
    private boolean useShader;
    public int frameCount;
    private static final String __OBFID = "CL_00000947";
    private boolean initialized = false;
    private World updatedWorld = null;
    private final boolean showDebugInfo = false;
    public boolean fogStandard = false;
    private float clipDistance = 128.0F;
    private long lastServerTime = 0L;
    private int lastServerTicks = 0;
    private int serverWaitTime = 0;
    private int serverWaitTimeCurrent = 0;
    private float avgServerTimeDiff = 0.0F;
    private float avgServerTickDiff = 0.0F;
    private long lastErrorCheckTimeMs = 0L;
    private final ShaderGroup[] fxaaShaders = new ShaderGroup[10];

    public EntityRenderer(Minecraft mcIn, IResourceManager resourceManagerIn)
    {
        shaderIndex = EntityRenderer.shaderCount;
        useShader = false;
        frameCount = 0;
        mc = mcIn;
        resourceManager = resourceManagerIn;
        itemRenderer = mcIn.getItemRenderer();
        theMapItemRenderer = new MapItemRenderer(mcIn.getTextureManager());
        lightmapTexture = new DynamicTexture(16, 16);
        locationLightMap = mcIn.getTextureManager().getDynamicTextureLocation("lightMap", lightmapTexture);
        lightmapColors = lightmapTexture.getTextureData();
        theShaderGroup = null;

        for (int i = 0; i < 32; ++i)
        {
            for (int j = 0; j < 32; ++j)
            {
                float f = (float)(j - 16);
                float f1 = (float)(i - 16);
                float f2 = MathHelper.sqrt_float(f * f + f1 * f1);
                rainXCoords[i << 5 | j] = -f1 / f2;
                rainYCoords[i << 5 | j] = f / f2;
            }
        }
    }

    public boolean isShaderActive()
    {
        return OpenGlHelper.shadersSupported && theShaderGroup != null;
    }

    public void func_181022_b()
    {
        if (theShaderGroup != null)
        {
            theShaderGroup.deleteShaderGroup();
        }

        theShaderGroup = null;
        shaderIndex = EntityRenderer.shaderCount;
    }

    public void switchUseShader()
    {
        useShader = !useShader;
    }

    /**
     * What shader to use when spectating this entity
     */
    public void loadEntityShader(Entity entityIn)
    {
        if (OpenGlHelper.shadersSupported)
        {
            if (theShaderGroup != null)
            {
                theShaderGroup.deleteShaderGroup();
            }

            theShaderGroup = null;

            if (entityIn instanceof EntityCreeper)
            {
                loadShader(new ResourceLocation("shaders/post/creeper.json"));
            }
            else if (entityIn instanceof EntitySpider)
            {
                loadShader(new ResourceLocation("shaders/post/spider.json"));
            }
            else if (entityIn instanceof EntityEnderman)
            {
                loadShader(new ResourceLocation("shaders/post/invert.json"));
            }
            else if (Reflector.ForgeHooksClient_loadEntityShader.exists())
            {
                Reflector.call(Reflector.ForgeHooksClient_loadEntityShader, entityIn, this);
            }
        }
    }

    public void activateNextShader()
    {
        if (OpenGlHelper.shadersSupported && mc.getRenderViewEntity() instanceof EntityPlayer)
        {
            if (theShaderGroup != null)
            {
                theShaderGroup.deleteShaderGroup();
            }

            shaderIndex = (shaderIndex + 1) % (EntityRenderer.shaderResourceLocations.length + 1);

            if (shaderIndex != EntityRenderer.shaderCount)
            {
                loadShader(EntityRenderer.shaderResourceLocations[shaderIndex]);
            }
            else
            {
                theShaderGroup = null;
            }
        }
    }

    private void loadShader(ResourceLocation resourceLocationIn)
    {
        if (OpenGlHelper.isFramebufferEnabled())
        {
            try
            {
                theShaderGroup = new ShaderGroup(mc.getTextureManager(), resourceManager, mc.getFramebuffer(), resourceLocationIn);
                theShaderGroup.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
                useShader = true;
            }
            catch (IOException ioexception)
            {
                EntityRenderer.logger.warn("Failed to load shader: " + resourceLocationIn, ioexception);
                shaderIndex = EntityRenderer.shaderCount;
                useShader = false;
            }
            catch (JsonSyntaxException jsonsyntaxexception)
            {
                EntityRenderer.logger.warn("Failed to load shader: " + resourceLocationIn, jsonsyntaxexception);
                shaderIndex = EntityRenderer.shaderCount;
                useShader = false;
            }
        }
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        if (theShaderGroup != null)
        {
            theShaderGroup.deleteShaderGroup();
        }

        theShaderGroup = null;

        if (shaderIndex != EntityRenderer.shaderCount)
        {
            loadShader(EntityRenderer.shaderResourceLocations[shaderIndex]);
        }
        else
        {
            loadEntityShader(mc.getRenderViewEntity());
        }
    }

    /**
     * Updates the entity renderer
     */
    public void updateRenderer()
    {
        if (OpenGlHelper.shadersSupported && ShaderLinkHelper.getStaticShaderLinkHelper() == null)
        {
            ShaderLinkHelper.setNewStaticShaderLinkHelper();
        }

        updateFovModifierHand();
        updateTorchFlicker();
        fogColor2 = fogColor1;
        thirdPersonDistanceTemp = thirdPersonDistance;

        if (mc.gameSettings.smoothCamera)
        {
            float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float f1 = f * f * f * 8.0F;
            smoothCamFilterX = mouseFilterXAxis.smooth(smoothCamYaw, 0.05F * f1);
            smoothCamFilterY = mouseFilterYAxis.smooth(smoothCamPitch, 0.05F * f1);
            smoothCamPartialTicks = 0.0F;
            smoothCamYaw = 0.0F;
            smoothCamPitch = 0.0F;
        }
        else
        {
            smoothCamFilterX = 0.0F;
            smoothCamFilterY = 0.0F;
            mouseFilterXAxis.reset();
            mouseFilterYAxis.reset();
        }

        if (mc.getRenderViewEntity() == null)
        {
            mc.setRenderViewEntity(mc.thePlayer);
        }

        Entity entity = mc.getRenderViewEntity();
        double d0 = entity.posX;
        double d1 = entity.posY + (double)entity.getEyeHeight();
        double d2 = entity.posZ;
        float f3 = mc.theWorld.getLightBrightness(new BlockPos(d0, d1, d2));
        float f4 = (float) mc.gameSettings.renderDistanceChunks / 16.0F;
        f4 = MathHelper.clamp_float(f4, 0.0F, 1.0F);
        float f2 = f3 * (1.0F - f4) + f4;
        fogColor1 += (f2 - fogColor1) * 0.1F;
        ++rendererUpdateCount;
        itemRenderer.updateEquippedItem();
        addRainParticles();
        bossColorModifierPrev = bossColorModifier;

        if (BossStatus.hasColorModifier)
        {
            bossColorModifier += 0.05F;

            if (bossColorModifier > 1.0F)
            {
                bossColorModifier = 1.0F;
            }

            BossStatus.hasColorModifier = false;
        }
        else if (bossColorModifier > 0.0F)
        {
            bossColorModifier -= 0.0125F;
        }
    }

    public ShaderGroup getShaderGroup()
    {
        return theShaderGroup;
    }

    public void updateShaderGroupSize(int width, int height)
    {
        if (OpenGlHelper.shadersSupported)
        {
            if (theShaderGroup != null)
            {
                theShaderGroup.createBindFramebuffers(width, height);
            }

            mc.renderGlobal.createBindEntityOutlineFbs(width, height);
        }
    }

    /**
     * Finds what block or object the mouse is over at the specified partial tick time. Args: partialTickTime
     */
    public void getMouseOver(float partialTicks)
    {
        Entity entity = mc.getRenderViewEntity();

        if (entity != null && mc.theWorld != null)
        {
            mc.mcProfiler.startSection("pick");
            mc.pointedEntity = null;
            double d0 = mc.playerController.getBlockReachDistance();
            mc.objectMouseOver = entity.rayTrace(d0, partialTicks);
            double d1 = d0;
            Vec3 vec3 = entity.getPositionEyes(partialTicks);
            boolean flag = false;
            boolean flag1 = true;

            if (mc.playerController.extendedReach())
            {
                d0 = 6.0D;
                d1 = 6.0D;
            }
            else
            {
                if (d0 > 3.0D)
                {
                    flag = true;
                }

                d0 = d0;
            }

            if (mc.objectMouseOver != null)
            {
                d1 = mc.objectMouseOver.hitVec.distanceTo(vec3);
            }

            Vec3 vec31 = entity.getLook(partialTicks);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            pointedEntity = null;
            Vec3 vec33 = null;
            float f = 1.0F;
            List list = mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, new EntityRenderer$1(this)));
            double d2 = d1;

            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity1 = (Entity)list.get(i);
                float f1 = entity1.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                if (axisalignedbb.isVecInside(vec3))
                {
                    if (d2 >= 0.0D)
                    {
                        pointedEntity = entity1;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                }
                else if (movingobjectposition != null)
                {
                    double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                    if (d3 < d2 || d2 == 0.0D)
                    {
                        boolean flag2 = false;

                        if (Reflector.ForgeEntity_canRiderInteract.exists())
                        {
                            flag2 = Reflector.callBoolean(entity1, Reflector.ForgeEntity_canRiderInteract);
                        }

                        if (entity1 == entity.ridingEntity && !flag2)
                        {
                            if (d2 == 0.0D)
                            {
                                pointedEntity = entity1;
                                vec33 = movingobjectposition.hitVec;
                            }
                        }
                        else
                        {
                            pointedEntity = entity1;
                            vec33 = movingobjectposition.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }

            if (pointedEntity != null && flag && vec3.distanceTo(vec33) > 3.0D)
            {
                pointedEntity = null;
                mc.objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, null, new BlockPos(vec33));
            }

            if (pointedEntity != null && (d2 < d1 || mc.objectMouseOver == null))
            {
                mc.objectMouseOver = new MovingObjectPosition(pointedEntity, vec33);

                if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame)
                {
                    mc.pointedEntity = pointedEntity;
                }
            }

            mc.mcProfiler.endSection();
        }
    }

    /**
     * Update FOV modifier hand
     */
    private void updateFovModifierHand()
    {
        float f = 1.0F;

        if (mc.getRenderViewEntity() instanceof AbstractClientPlayer)
        {
            AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer) mc.getRenderViewEntity();
            f = abstractclientplayer.getFovModifier();
        }

        fovModifierHandPrev = fovModifierHand;
        fovModifierHand += (f - fovModifierHand) * 0.5F;

        if (fovModifierHand > 1.5F)
        {
            fovModifierHand = 1.5F;
        }

        if (fovModifierHand < 0.1F)
        {
            fovModifierHand = 0.1F;
        }
    }

    /**
     * Changes the field of view of the player depending on if they are underwater or not
     */
    private float getFOVModifier(float partialTicks, boolean p_78481_2_)
    {
        if (debugView)
        {
            return 90.0F;
        }
        else
        {
            Entity entity = mc.getRenderViewEntity();
            float f = 70.0F;

            if (p_78481_2_)
            {
                f = mc.gameSettings.fovSetting;

                if (Config.isDynamicFov())
                {
                    f *= fovModifierHandPrev + (fovModifierHand - fovModifierHandPrev) * partialTicks;
                }
            }

            boolean flag = false;

            if (mc.currentScreen == null)
            {
                GameSettings gamesettings = mc.gameSettings;
                flag = GameSettings.isKeyDown(mc.gameSettings.ofKeyBindZoom);
            }

            if (flag)
            {
                if (!Config.zoomMode)
                {
                    Config.zoomMode = true;
                    mc.gameSettings.smoothCamera = true;
                }

                if (Config.zoomMode)
                {
                    SmoothZoom mod = ModuleCache.get().getModule(SmoothZoom.class);
                    if (mod.isToggled()) {
                        f = mod.hookZoom(f);
                    } else {
                        f /= 4.0f;
                    }
                }
            }
            else if (Config.zoomMode)
            {
                Config.zoomMode = false;
                mc.gameSettings.smoothCamera = false;
                mouseFilterXAxis = new MouseFilter();
                mouseFilterYAxis = new MouseFilter();
                mc.renderGlobal.displayListEntitiesDirty = true;
            }

            if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getHealth() <= 0.0F)
            {
                float f1 = (float)((EntityLivingBase)entity).deathTime + partialTicks;
                f /= (1.0F - 500.0F / (f1 + 500.0F)) * 2.0F + 1.0F;
            }

            Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(mc.theWorld, entity, partialTicks);

            if (block.getMaterial() == Material.water)
            {
                f = f * 60.0F / 70.0F;
            }

            return f;
        }
    }

    private void hurtCameraEffect(float partialTicks)
    {
        if (mc.getRenderViewEntity() instanceof EntityLivingBase)
        {
            EntityLivingBase entitylivingbase = (EntityLivingBase) mc.getRenderViewEntity();
            float f = (float)entitylivingbase.hurtTime - partialTicks;

            if (entitylivingbase.getHealth() <= 0.0F)
            {
                float f1 = (float)entitylivingbase.deathTime + partialTicks;
                GlStateManager.rotate(40.0F - 8000.0F / (f1 + 200.0F), 0.0F, 0.0F, 1.0F);
            }

            if (f < 0.0F)
            {
                return;
            }

            f = f / (float)entitylivingbase.maxHurtTime;
            f = MathHelper.sin(f * f * f * f * (float)Math.PI);
            float f2 = entitylivingbase.attackedAtYaw;
            GlStateManager.rotate(-f2, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-f * 14.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(f2, 0.0F, 1.0F, 0.0F);
        }
    }

    /**
     * Setups all the GL settings for view bobbing. Args: partialTickTime
     */
    private void setupViewBobbing(float partialTicks)
    {
        if (mc.getRenderViewEntity() instanceof EntityPlayer)
        {
            EntityPlayer entityplayer = (EntityPlayer) mc.getRenderViewEntity();
            float f = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
            float f1 = -(entityplayer.distanceWalkedModified + f * partialTicks);
            float f2 = entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * partialTicks;
            float f3 = entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * partialTicks;
            GlStateManager.translate(MathHelper.sin(f1 * (float)Math.PI) * f2 * 0.5F, -Math.abs(MathHelper.cos(f1 * (float)Math.PI) * f2), 0.0F);
            GlStateManager.rotate(MathHelper.sin(f1 * (float)Math.PI) * f2 * 3.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(Math.abs(MathHelper.cos(f1 * (float)Math.PI - 0.2F) * f2) * 5.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(f3, 1.0F, 0.0F, 0.0F);
        }
    }

    /**
     * sets up player's eye (or camera in third person mode)
     */
    private void orientCamera(float partialTicks)
    {
        Entity entity = mc.getRenderViewEntity();
        float f = entity.getEyeHeight();
        double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
        double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + (double)f;
        double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;

        if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPlayerSleeping())
        {
            f = (float)((double)f + 1.0D);
            GlStateManager.translate(0.0F, 0.3F, 0.0F);

            if (!mc.gameSettings.debugCamEnable)
            {
                BlockPos blockpos = new BlockPos(entity);
                IBlockState iblockstate = mc.theWorld.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if (Reflector.ForgeHooksClient_orientBedCamera.exists())
                {
                    Reflector.callVoid(Reflector.ForgeHooksClient_orientBedCamera, mc.theWorld, blockpos, iblockstate, entity);
                }
                else if (block == Blocks.bed)
                {
                    int j = iblockstate.getValue(BlockDirectional.FACING).getHorizontalIndex();
                    GlStateManager.rotate((float)(j * 90), 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
                GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
            }
        }
        else if (mc.gameSettings.thirdPersonView > 0)
        {
            double d3 = thirdPersonDistanceTemp + (thirdPersonDistance - thirdPersonDistanceTemp) * partialTicks;

            if (mc.gameSettings.debugCamEnable)
            {
                GlStateManager.translate(0.0F, 0.0F, (float)(-d3));
            }
            else
            {
                float f1 = entity.rotationYaw;
                float f2 = entity.rotationPitch;

                if (mc.gameSettings.thirdPersonView == 2)
                {
                    f2 += 180.0F;
                }

                double d4 = (double)(-MathHelper.sin(f1 / 180.0F * (float)Math.PI) * MathHelper.cos(f2 / 180.0F * (float)Math.PI)) * d3;
                double d5 = (double)(MathHelper.cos(f1 / 180.0F * (float)Math.PI) * MathHelper.cos(f2 / 180.0F * (float)Math.PI)) * d3;
                double d6 = (double)(-MathHelper.sin(f2 / 180.0F * (float)Math.PI)) * d3;

                for (int i = 0; i < 8; ++i)
                {
                    float f3 = (float)((i & 1) * 2 - 1);
                    float f4 = (float)((i >> 1 & 1) * 2 - 1);
                    float f5 = (float)((i >> 2 & 1) * 2 - 1);
                    f3 = f3 * 0.1F;
                    f4 = f4 * 0.1F;
                    f5 = f5 * 0.1F;
                    MovingObjectPosition movingobjectposition = mc.theWorld.rayTraceBlocks(new Vec3(d0 + (double)f3, d1 + (double)f4, d2 + (double)f5), new Vec3(d0 - d4 + (double)f3 + (double)f5, d1 - d6 + (double)f4, d2 - d5 + (double)f5));

                    if (movingobjectposition != null)
                    {
                        double d7 = movingobjectposition.hitVec.distanceTo(new Vec3(d0, d1, d2));

                        if (d7 < d3)
                        {
                            d3 = d7;
                        }
                    }
                }

                if (mc.gameSettings.thirdPersonView == 2)
                {
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.rotate(entity.rotationPitch - f2, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(entity.rotationYaw - f1, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(0.0F, 0.0F, (float)(-d3));
                GlStateManager.rotate(f1 - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(f2 - entity.rotationPitch, 1.0F, 0.0F, 0.0F);
            }
        }
        else
        {
            GlStateManager.translate(0.0F, 0.0F, -0.1F);
        }

        if (Reflector.EntityViewRenderEvent_CameraSetup_Constructor.exists())
        {
            if (!mc.gameSettings.debugCamEnable)
            {
                float f6 = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F;
                float f7 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                float f8 = 0.0F;

                if (entity instanceof EntityAnimal)
                {
                    EntityAnimal entityanimal = (EntityAnimal)entity;
                    f6 = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F;
                }

                Block block1 = ActiveRenderInfo.getBlockAtEntityViewpoint(mc.theWorld, entity, partialTicks);
                Object object = Reflector.newInstance(Reflector.EntityViewRenderEvent_CameraSetup_Constructor, this, entity, block1, Float.valueOf(partialTicks), Float.valueOf(f6), Float.valueOf(f7), Float.valueOf(f8));
                Reflector.postForgeBusEvent(object);
                f8 = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_CameraSetup_roll, f8);
                f7 = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_CameraSetup_pitch, f7);
                f6 = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_CameraSetup_yaw, f6);
                GlStateManager.rotate(f8, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(f7, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(f6, 0.0F, 1.0F, 0.0F);
            }
        }
        else if (!mc.gameSettings.debugCamEnable)
        {
            GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 1.0F, 0.0F, 0.0F);

            if (entity instanceof EntityAnimal)
            {
                EntityAnimal entityanimal1 = (EntityAnimal)entity;
                GlStateManager.rotate(entityanimal1.prevRotationYawHead + (entityanimal1.rotationYawHead - entityanimal1.prevRotationYawHead) * partialTicks + 180.0F, 0.0F, 1.0F, 0.0F);
            }
            else
            {
                GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, 1.0F, 0.0F);
            }
        }

        GlStateManager.translate(0.0F, -f, 0.0F);
        d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
        d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + (double)f;
        d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;
        cloudFog = mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks);
    }

    /**
     * sets up projection, view effects, camera position/rotation
     */
    public void setupCameraTransform(float partialTicks, int pass)
    {
        farPlaneDistance = (float)(mc.gameSettings.renderDistanceChunks * 16);

        if (Config.isFogFancy())
        {
            farPlaneDistance *= 0.95F;
        }

        if (Config.isFogFast())
        {
            farPlaneDistance *= 0.83F;
        }

        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        float f = 0.07F;

        if (mc.gameSettings.anaglyph)
        {
            GlStateManager.translate((float)(-(pass * 2 - 1)) * f, 0.0F, 0.0F);
        }

        clipDistance = farPlaneDistance * 2.0F;

        if (clipDistance < 173.0F)
        {
            clipDistance = 173.0F;
        }

        if (mc.theWorld.provider.getDimensionId() == 1)
        {
            clipDistance = 256.0F;
        }

        if (cameraZoom != 1.0D)
        {
            GlStateManager.translate((float) cameraYaw, (float)(-cameraPitch), 0.0F);
            GlStateManager.scale(cameraZoom, cameraZoom, 1.0D);
        }

        Project.gluPerspective(getFOVModifier(partialTicks, true), (float) mc.displayWidth / (float) mc.displayHeight, 0.05F, clipDistance);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();

        if (mc.gameSettings.anaglyph)
        {
            GlStateManager.translate((float)(pass * 2 - 1) * 0.1F, 0.0F, 0.0F);
        }

        hurtCameraEffect(partialTicks);

        if (mc.gameSettings.viewBobbing)
        {
            setupViewBobbing(partialTicks);
        }

        float f1 = mc.thePlayer.prevTimeInPortal + (mc.thePlayer.timeInPortal - mc.thePlayer.prevTimeInPortal) * partialTicks;

        if (f1 > 0.0F)
        {
            byte b0 = 20;

            if (mc.thePlayer.isPotionActive(Potion.confusion))
            {
                b0 = 7;
            }

            float f2 = 5.0F / (f1 * f1 + 5.0F) - f1 * 0.04F;
            f2 = f2 * f2;
            GlStateManager.rotate(((float) rendererUpdateCount + partialTicks) * (float)b0, 0.0F, 1.0F, 1.0F);
            GlStateManager.scale(1.0F / f2, 1.0F, 1.0F);
            GlStateManager.rotate(-((float) rendererUpdateCount + partialTicks) * (float)b0, 0.0F, 1.0F, 1.0F);
        }

        orientCamera(partialTicks);

        if (debugView)
        {
            switch (debugViewDirection)
            {
                case 0:
                    GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                    break;

                case 1:
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                    break;

                case 2:
                    GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                    break;

                case 3:
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                    break;

                case 4:
                    GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            }
        }
    }

    /**
     * Render player hand
     */
    private void renderHand(float partialTicks, int xOffset)
    {
        renderHand(partialTicks, xOffset, true, true, false);
    }

    public void renderHand(float p_renderHand_1_, int p_renderHand_2_, boolean p_renderHand_3_, boolean p_renderHand_4_, boolean p_renderHand_5_)
    {
        if (!debugView)
        {
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            float f = 0.07F;

            if (mc.gameSettings.anaglyph)
            {
                GlStateManager.translate((float)(-(p_renderHand_2_ * 2 - 1)) * f, 0.0F, 0.0F);
            }

            if (Config.isShaders())
            {
                Shaders.applyHandDepth();
            }

            Project.gluPerspective(getFOVModifier(p_renderHand_1_, false), (float) mc.displayWidth / (float) mc.displayHeight, 0.05F, farPlaneDistance * 2.0F);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();

            if (mc.gameSettings.anaglyph)
            {
                GlStateManager.translate((float)(p_renderHand_2_ * 2 - 1) * 0.1F, 0.0F, 0.0F);
            }

            boolean flag = false;

            if (p_renderHand_3_)
            {
                GlStateManager.pushMatrix();
                hurtCameraEffect(p_renderHand_1_);

                if (mc.gameSettings.viewBobbing)
                {
                    setupViewBobbing(p_renderHand_1_);
                }

                flag = mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping();
                boolean flag1 = !ReflectorForge.renderFirstPersonHand(mc.renderGlobal, p_renderHand_1_, p_renderHand_2_);

                if (flag1 && mc.gameSettings.thirdPersonView == 0 && !flag && !mc.gameSettings.hideGUI && !mc.playerController.isSpectator())
                {
                    enableLightmap();

                    if (Config.isShaders())
                    {
                        ShadersRender.renderItemFP(itemRenderer, p_renderHand_1_, p_renderHand_5_);
                    }
                    else
                    {
                        itemRenderer.renderItemInFirstPerson(p_renderHand_1_);
                    }

                    disableLightmap();
                }

                GlStateManager.popMatrix();
            }

            if (!p_renderHand_4_)
            {
                return;
            }

            disableLightmap();

            if (mc.gameSettings.thirdPersonView == 0 && !flag)
            {
                itemRenderer.renderOverlays(p_renderHand_1_);
                hurtCameraEffect(p_renderHand_1_);
            }

            if (mc.gameSettings.viewBobbing)
            {
                setupViewBobbing(p_renderHand_1_);
            }
        }
    }

    public void disableLightmap()
    {
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        if (Config.isShaders())
        {
            Shaders.disableLightmap();
        }
    }

    public void enableLightmap()
    {
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        float f = 0.00390625F;
        GlStateManager.scale(f, f, f);
        GlStateManager.translate(8.0F, 8.0F, 8.0F);
        GlStateManager.matrixMode(5888);
        mc.getTextureManager().bindTexture(locationLightMap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        if (Config.isShaders())
        {
            Shaders.enableLightmap();
        }
    }

    /**
     * Recompute a random value that is applied to block color in updateLightmap()
     */
    private void updateTorchFlicker()
    {
        torchFlickerDX = (float)((double) torchFlickerDX + (Math.random() - Math.random()) * Math.random() * Math.random());
        torchFlickerDX = (float)((double) torchFlickerDX * 0.9D);
        torchFlickerX += (torchFlickerDX - torchFlickerX) * 1.0F;
        lightmapUpdateNeeded = true;
    }

    private void updateLightmap(float partialTicks)
    {
        if (lightmapUpdateNeeded)
        {
            mc.mcProfiler.startSection("lightTex");
            WorldClient worldclient = mc.theWorld;

            if (worldclient != null)
            {
                if (Config.isCustomColors() && CustomColors.updateLightmap(worldclient, torchFlickerX, lightmapColors, mc.thePlayer.isPotionActive(Potion.nightVision)))
                {
                    lightmapTexture.updateDynamicTexture();
                    lightmapUpdateNeeded = false;
                    mc.mcProfiler.endSection();
                    return;
                }

                float f = worldclient.getSunBrightness(1.0F);
                float f1 = f * 0.95F + 0.05F;

                for (int i = 0; i < 256; ++i)
                {
                    float f2 = worldclient.provider.getLightBrightnessTable()[i / 16] * f1;
                    float f3 = worldclient.provider.getLightBrightnessTable()[i % 16] * (torchFlickerX * 0.1F + 1.5F);

                    if (worldclient.getLastLightningBolt() > 0)
                    {
                        f2 = worldclient.provider.getLightBrightnessTable()[i / 16];
                    }

                    float f4 = f2 * (f * 0.65F + 0.35F);
                    float f5 = f2 * (f * 0.65F + 0.35F);
                    float f6 = f3 * ((f3 * 0.6F + 0.4F) * 0.6F + 0.4F);
                    float f7 = f3 * (f3 * f3 * 0.6F + 0.4F);
                    float f8 = f4 + f3;
                    float f9 = f5 + f6;
                    float f10 = f2 + f7;
                    f8 = f8 * 0.96F + 0.03F;
                    f9 = f9 * 0.96F + 0.03F;
                    f10 = f10 * 0.96F + 0.03F;

                    if (bossColorModifier > 0.0F)
                    {
                        float f11 = bossColorModifierPrev + (bossColorModifier - bossColorModifierPrev) * partialTicks;
                        f8 = f8 * (1.0F - f11) + f8 * 0.7F * f11;
                        f9 = f9 * (1.0F - f11) + f9 * 0.6F * f11;
                        f10 = f10 * (1.0F - f11) + f10 * 0.6F * f11;
                    }

                    if (worldclient.provider.getDimensionId() == 1)
                    {
                        f8 = 0.22F + f3 * 0.75F;
                        f9 = 0.28F + f6 * 0.75F;
                        f10 = 0.25F + f7 * 0.75F;
                    }

                    if (mc.thePlayer.isPotionActive(Potion.nightVision))
                    {
                        float f15 = getNightVisionBrightness(mc.thePlayer, partialTicks);
                        float f12 = 1.0F / f8;

                        if (f12 > 1.0F / f9)
                        {
                            f12 = 1.0F / f9;
                        }

                        if (f12 > 1.0F / f10)
                        {
                            f12 = 1.0F / f10;
                        }

                        f8 = f8 * (1.0F - f15) + f8 * f12 * f15;
                        f9 = f9 * (1.0F - f15) + f9 * f12 * f15;
                        f10 = f10 * (1.0F - f15) + f10 * f12 * f15;
                    }

                    if (f8 > 1.0F)
                    {
                        f8 = 1.0F;
                    }

                    if (f9 > 1.0F)
                    {
                        f9 = 1.0F;
                    }

                    if (f10 > 1.0F)
                    {
                        f10 = 1.0F;
                    }

                    float f16 = mc.gameSettings.gammaSetting;
                    float f17 = 1.0F - f8;
                    float f13 = 1.0F - f9;
                    float f14 = 1.0F - f10;
                    f17 = 1.0F - f17 * f17 * f17 * f17;
                    f13 = 1.0F - f13 * f13 * f13 * f13;
                    f14 = 1.0F - f14 * f14 * f14 * f14;
                    f8 = f8 * (1.0F - f16) + f17 * f16;
                    f9 = f9 * (1.0F - f16) + f13 * f16;
                    f10 = f10 * (1.0F - f16) + f14 * f16;
                    f8 = f8 * 0.96F + 0.03F;
                    f9 = f9 * 0.96F + 0.03F;
                    f10 = f10 * 0.96F + 0.03F;

                    if (f8 > 1.0F)
                    {
                        f8 = 1.0F;
                    }

                    if (f9 > 1.0F)
                    {
                        f9 = 1.0F;
                    }

                    if (f10 > 1.0F)
                    {
                        f10 = 1.0F;
                    }

                    if (f8 < 0.0F)
                    {
                        f8 = 0.0F;
                    }

                    if (f9 < 0.0F)
                    {
                        f9 = 0.0F;
                    }

                    if (f10 < 0.0F)
                    {
                        f10 = 0.0F;
                    }

                    short short1 = 255;
                    int j = (int)(f8 * 255.0F);
                    int k = (int)(f9 * 255.0F);
                    int l = (int)(f10 * 255.0F);
                    lightmapColors[i] = short1 << 24 | j << 16 | k << 8 | l;
                }

                lightmapTexture.updateDynamicTexture();
                lightmapUpdateNeeded = false;
                mc.mcProfiler.endSection();
            }
        }
    }

    public float getNightVisionBrightness(EntityLivingBase entitylivingbaseIn, float partialTicks)
    {
        int i = entitylivingbaseIn.getActivePotionEffect(Potion.nightVision).getDuration();
        return i > 200 ? 1.0F : 0.7F + MathHelper.sin(((float)i - partialTicks) * (float)Math.PI * 0.2F) * 0.3F;
    }

    public void func_181560_a(float p_181560_1_, long p_181560_2_)
    {
        frameInit();
        boolean flag = Display.isActive();

        if (!flag && mc.gameSettings.pauseOnLostFocus && (!mc.gameSettings.touchscreen || !Mouse.isButtonDown(1)))
        {
            if (Minecraft.getSystemTime() - prevFrameTime > 500L)
            {
                mc.displayInGameMenu();
            }
        }
        else
        {
            prevFrameTime = Minecraft.getSystemTime();
        }

        mc.mcProfiler.startSection("mouse");

        if (flag && Minecraft.isRunningOnMac && mc.inGameHasFocus && !Mouse.isInsideWindow())
        {
            Mouse.setGrabbed(false);
            Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
            Mouse.setGrabbed(true);
        }

        if (mc.inGameHasFocus && flag)
        {
            mc.mouseHelper.mouseXYChange();
            float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float f1 = f * f * f * 8.0F;
            float f2 = (float) mc.mouseHelper.deltaX * f1;
            float f3 = (float) mc.mouseHelper.deltaY * f1;
            byte b0 = 1;

            if (mc.gameSettings.invertMouse)
            {
                b0 = -1;
            }

            if (mc.gameSettings.smoothCamera)
            {
                smoothCamYaw += f2;
                smoothCamPitch += f3;
                float f4 = p_181560_1_ - smoothCamPartialTicks;
                smoothCamPartialTicks = p_181560_1_;
                f2 = smoothCamFilterX * f4;
                f3 = smoothCamFilterY * f4;
                mc.thePlayer.setAngles(f2, f3 * (float)b0);
            }
            else
            {
                smoothCamYaw = 0.0F;
                smoothCamPitch = 0.0F;
                mc.thePlayer.setAngles(f2, f3 * (float)b0);
            }
        }

        mc.mcProfiler.endSection();

        if (!mc.skipRenderWorld)
        {
            EntityRenderer.anaglyphEnable = mc.gameSettings.anaglyph;
            final ScaledResolution scaledresolution = new ScaledResolution(mc);
            int l = scaledresolution.getScaledWidth();
            int i1 = scaledresolution.getScaledHeight();
            final int j1 = Mouse.getX() * l / mc.displayWidth;
            final int k1 = i1 - Mouse.getY() * i1 / mc.displayHeight - 1;
            int l1 = mc.gameSettings.limitFramerate;

            if (mc.theWorld != null)
            {
                mc.mcProfiler.startSection("level");
                int i = Math.min(Minecraft.getDebugFPS(), l1);
                i = Math.max(i, 60);
                long j = System.nanoTime() - p_181560_2_;
                long k = Math.max((long)(1000000000 / i / 4) - j, 0L);
                renderWorld(p_181560_1_, System.nanoTime() + k);

                if (OpenGlHelper.shadersSupported)
                {
                    mc.renderGlobal.renderEntityOutlineFramebuffer();

                    if (theShaderGroup != null && useShader)
                    {
                        GlStateManager.matrixMode(5890);
                        GlStateManager.pushMatrix();
                        GlStateManager.loadIdentity();
                        theShaderGroup.loadShaderGroup(p_181560_1_);
                        GlStateManager.popMatrix();
                    }

                    mc.getFramebuffer().bindFramebuffer(true);
                }

                renderEndNanoTime = System.nanoTime();
                mc.mcProfiler.endStartSection("gui");

                if (!mc.gameSettings.hideGUI || mc.currentScreen != null)
                {
                    GlStateManager.alphaFunc(516, 0.1F);
                    mc.ingameGUI.renderGameOverlay(p_181560_1_);

                    if (mc.gameSettings.ofShowFps && !mc.gameSettings.showDebugInfo)
                    {
                        Config.drawFps();
                    }

                    if (mc.gameSettings.showDebugInfo)
                    {
                        Lagometer.showLagometer(scaledresolution);
                    }
                }

                mc.mcProfiler.endSection();
            }
            else
            {
                GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
                GlStateManager.matrixMode(5889);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(5888);
                GlStateManager.loadIdentity();
                setupOverlayRendering();
                renderEndNanoTime = System.nanoTime();
                TileEntityRendererDispatcher.instance.renderEngine = mc.getTextureManager();
            }

            if (mc.currentScreen != null)
            {
                GlStateManager.clear(256);

                try
                {
                    if (Reflector.ForgeHooksClient_drawScreen.exists())
                    {
                        Reflector.callVoid(Reflector.ForgeHooksClient_drawScreen, mc.currentScreen, j1, k1, p_181560_1_);
                    }
                    else
                    {
                        mc.currentScreen.drawScreen(j1, k1, p_181560_1_);
                    }
                }
                catch (Throwable throwable)
                {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering screen");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Screen render details");
                    crashreportcategory.addCrashSectionCallable("Screen name", () -> Minecraft.getMinecraft().currentScreen.getClass().getCanonicalName());
                    crashreportcategory.addCrashSectionCallable("Mouse location", () -> String.format("Scaled: (%d, %d). Absolute: (%d, %d)", j1, k1, Mouse.getX(), Mouse.getY()));
                    crashreportcategory.addCrashSectionCallable("Screen size", () -> String.format("Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %d", scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), mc.displayWidth, mc.displayHeight, scaledresolution.getScaleFactor()));
                    throw new ReportedException(crashreport);
                }
            }
        }

        frameFinish();
        waitForServerThread();
        Lagometer.updateLagometer();

        if (mc.gameSettings.ofProfiler)
        {
            mc.gameSettings.showDebugProfilerChart = true;
        }
    }

    public void renderStreamIndicator(float partialTicks)
    {
        setupOverlayRendering();
        mc.ingameGUI.renderStreamIndicator(new ScaledResolution(mc));
    }

    private boolean isDrawBlockOutline()
    {
        if (!drawBlockOutline)
        {
            return false;
        }
        else
        {
            Entity entity = mc.getRenderViewEntity();
            boolean flag = entity instanceof EntityPlayer && !mc.gameSettings.hideGUI;

            if (flag && !((EntityPlayer)entity).capabilities.allowEdit)
            {
                ItemStack itemstack = ((EntityPlayer)entity).getCurrentEquippedItem();

                if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
                {
                    BlockPos blockpos = mc.objectMouseOver.getBlockPos();
                    IBlockState iblockstate = mc.theWorld.getBlockState(blockpos);
                    Block block = iblockstate.getBlock();

                    if (mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR)
                    {
                        flag = ReflectorForge.blockHasTileEntity(iblockstate) && mc.theWorld.getTileEntity(blockpos) instanceof IInventory;
                    }
                    else
                    {
                        flag = itemstack != null && (itemstack.canDestroy(block) || itemstack.canPlaceOn(block));
                    }
                }
            }

            return flag;
        }
    }

    private void renderWorldDirections(float partialTicks)
    {
        if (mc.gameSettings.showDebugInfo && !mc.gameSettings.hideGUI && !mc.thePlayer.hasReducedDebug() && !mc.gameSettings.reducedDebugInfo)
        {
            Entity entity = mc.getRenderViewEntity();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GL11.glLineWidth(1.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.pushMatrix();
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            orientCamera(partialTicks);
            GlStateManager.translate(0.0F, entity.getEyeHeight(), 0.0F);
            RenderGlobal.func_181563_a(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.005D, 1.0E-4D, 1.0E-4D), 255, 0, 0, 255);
            RenderGlobal.func_181563_a(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0E-4D, 1.0E-4D, 0.005D), 0, 0, 255, 255);
            RenderGlobal.func_181563_a(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0E-4D, 0.0033D, 1.0E-4D), 0, 255, 0, 255);
            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }
    }

    public void renderWorld(float partialTicks, long finishTimeNano)
    {
        updateLightmap(partialTicks);

        if (mc.getRenderViewEntity() == null)
        {
            mc.setRenderViewEntity(mc.thePlayer);
        }

        getMouseOver(partialTicks);

        if (Config.isShaders())
        {
            Shaders.beginRender(mc, partialTicks, finishTimeNano);
        }

        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        mc.mcProfiler.startSection("center");

        if (mc.gameSettings.anaglyph)
        {
            EntityRenderer.anaglyphField = 0;
            GlStateManager.colorMask(false, true, true, false);
            renderWorldPass(0, partialTicks, finishTimeNano);
            EntityRenderer.anaglyphField = 1;
            GlStateManager.colorMask(true, false, false, false);
            renderWorldPass(1, partialTicks, finishTimeNano);
            GlStateManager.colorMask(true, true, true, false);
        }
        else
        {
            renderWorldPass(2, partialTicks, finishTimeNano);
        }

        mc.mcProfiler.endSection();
    }

    private void renderWorldPass(int pass, float partialTicks, long finishTimeNano)
    {
        boolean flag = Config.isShaders();

        if (flag)
        {
            Shaders.beginRenderPass(pass, partialTicks, finishTimeNano);
        }

        RenderGlobal renderglobal = mc.renderGlobal;
        EffectRenderer effectrenderer = mc.effectRenderer;
        boolean flag1 = isDrawBlockOutline();
        GlStateManager.enableCull();
        mc.mcProfiler.endStartSection("clear");

        if (flag)
        {
            Shaders.setViewport(0, 0, mc.displayWidth, mc.displayHeight);
        }
        else
        {
            GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
        }

        updateFogColor(partialTicks);
        GlStateManager.clear(16640);

        if (flag)
        {
            Shaders.clearRenderBuffer();
        }

        mc.mcProfiler.endStartSection("camera");
        setupCameraTransform(partialTicks, pass);

        if (flag)
        {
            Shaders.setCamera(partialTicks);
        }

        ActiveRenderInfo.updateRenderInfo(mc.thePlayer, mc.gameSettings.thirdPersonView == 2);
        mc.mcProfiler.endStartSection("frustum");
        ClippingHelperImpl.getInstance();
        mc.mcProfiler.endStartSection("culling");
        Frustum frustum = new Frustum();
        Entity entity = mc.getRenderViewEntity();
        double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
        double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
        double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;

        if (flag)
        {
            ShadersRender.setFrustrumPosition(frustum, d0, d1, d2);
        }
        else
        {
            frustum.setPosition(d0, d1, d2);
        }

        if ((Config.isSkyEnabled() || Config.isSunMoonEnabled() || Config.isStarsEnabled()) && !Shaders.isShadowPass)
        {
            setupFog(-1, partialTicks);
            mc.mcProfiler.endStartSection("sky");
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(getFOVModifier(partialTicks, true), (float) mc.displayWidth / (float) mc.displayHeight, 0.05F, clipDistance);
            GlStateManager.matrixMode(5888);

            if (flag)
            {
                Shaders.beginSky();
            }

            renderglobal.renderSky(partialTicks, pass);

            if (flag)
            {
                Shaders.endSky();
            }

            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(getFOVModifier(partialTicks, true), (float) mc.displayWidth / (float) mc.displayHeight, 0.05F, clipDistance);
            GlStateManager.matrixMode(5888);
        }
        else
        {
            GlStateManager.disableBlend();
        }

        setupFog(0, partialTicks);
        GlStateManager.shadeModel(7425);

        if (entity.posY + (double)entity.getEyeHeight() < 128.0D + (double)(mc.gameSettings.ofCloudsHeight * 128.0F))
        {
            renderCloudsCheck(renderglobal, partialTicks, pass);
        }

        mc.mcProfiler.endStartSection("prepareterrain");
        setupFog(0, partialTicks);
        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        RenderHelper.disableStandardItemLighting();
        mc.mcProfiler.endStartSection("terrain_setup");

        if (flag)
        {
            ShadersRender.setupTerrain(renderglobal, entity, partialTicks, frustum, frameCount++, mc.thePlayer.isSpectator());
        }
        else
        {
            renderglobal.setupTerrain(entity, partialTicks, frustum, frameCount++, mc.thePlayer.isSpectator());
        }

        if (pass == 0 || pass == 2)
        {
            mc.mcProfiler.endStartSection("updatechunks");
            Lagometer.timerChunkUpload.start();
            mc.renderGlobal.updateChunks(finishTimeNano);
            Lagometer.timerChunkUpload.end();
        }

        mc.mcProfiler.endStartSection("terrain");
        Lagometer.timerTerrain.start();

        if (mc.gameSettings.ofSmoothFps && pass > 0)
        {
            mc.mcProfiler.endStartSection("finish");
            GL11.glFinish();
            mc.mcProfiler.endStartSection("terrain");
        }

        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();

        if (flag)
        {
            ShadersRender.beginTerrainSolid();
        }

        renderglobal.renderBlockLayer(EnumWorldBlockLayer.SOLID, partialTicks, pass, entity);
        GlStateManager.enableAlpha();

        if (flag)
        {
            ShadersRender.beginTerrainCutoutMipped();
        }

        renderglobal.renderBlockLayer(EnumWorldBlockLayer.CUTOUT_MIPPED, partialTicks, pass, entity);
        mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);

        if (flag)
        {
            ShadersRender.beginTerrainCutout();
        }

        renderglobal.renderBlockLayer(EnumWorldBlockLayer.CUTOUT, partialTicks, pass, entity);
        mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();

        if (flag)
        {
            ShadersRender.endTerrain();
        }

        Lagometer.timerTerrain.end();
        GlStateManager.shadeModel(7424);
        GlStateManager.alphaFunc(516, 0.1F);

        if (!debugView)
        {
            GlStateManager.matrixMode(5888);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            RenderHelper.enableStandardItemLighting();
            mc.mcProfiler.endStartSection("entities");

            if (Reflector.ForgeHooksClient_setRenderPass.exists())
            {
                Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, Integer.valueOf(0));
            }

            renderglobal.renderEntities(entity, frustum, partialTicks);

            if (Reflector.ForgeHooksClient_setRenderPass.exists())
            {
                Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, Integer.valueOf(-1));
            }

            RenderHelper.disableStandardItemLighting();
            disableLightmap();
            GlStateManager.matrixMode(5888);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();

            if (mc.objectMouseOver != null && entity.isInsideOfMaterial(Material.water) && flag1)
            {
                EntityPlayer entityplayer = (EntityPlayer)entity;
                GlStateManager.disableAlpha();
                mc.mcProfiler.endStartSection("outline");

                if ((!Reflector.ForgeHooksClient_onDrawBlockHighlight.exists() || !Reflector.callBoolean(Reflector.ForgeHooksClient_onDrawBlockHighlight, renderglobal, entityplayer, mc.objectMouseOver, Integer.valueOf(0), entityplayer.getHeldItem(), Float.valueOf(partialTicks))) && !mc.gameSettings.hideGUI)
                {
                    renderglobal.drawSelectionBox(entityplayer, mc.objectMouseOver, 0, partialTicks);
                }
                GlStateManager.enableAlpha();
            }
        }

        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();

        if (flag1 && mc.objectMouseOver != null && !entity.isInsideOfMaterial(Material.water))
        {
            EntityPlayer entityplayer1 = (EntityPlayer)entity;
            GlStateManager.disableAlpha();
            mc.mcProfiler.endStartSection("outline");

            if ((!Reflector.ForgeHooksClient_onDrawBlockHighlight.exists() || !Reflector.callBoolean(Reflector.ForgeHooksClient_onDrawBlockHighlight, renderglobal, entityplayer1, mc.objectMouseOver, Integer.valueOf(0), entityplayer1.getHeldItem(), Float.valueOf(partialTicks))) && !mc.gameSettings.hideGUI)
            {
                renderglobal.drawSelectionBox(entityplayer1, mc.objectMouseOver, 0, partialTicks);
            }
            GlStateManager.enableAlpha();
        }

        if (!renderglobal.damagedBlocks.isEmpty())
        {
            mc.mcProfiler.endStartSection("destroyProgress");
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
            mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
            renderglobal.drawBlockDamageTexture(Tessellator.getInstance(), Tessellator.getInstance().getWorldRenderer(), entity, partialTicks);
            mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
            GlStateManager.disableBlend();
        }

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableBlend();

        if (!debugView)
        {
            enableLightmap();
            mc.mcProfiler.endStartSection("litParticles");

            if (flag)
            {
                Shaders.beginLitParticles();
            }

            effectrenderer.renderLitParticles(entity, partialTicks);
            RenderHelper.disableStandardItemLighting();
            setupFog(0, partialTicks);
            mc.mcProfiler.endStartSection("particles");

            if (flag)
            {
                Shaders.beginParticles();
            }

            effectrenderer.renderParticles(entity, partialTicks);

            if (flag)
            {
                Shaders.endParticles();
            }

            disableLightmap();
        }

        GlStateManager.depthMask(false);
        GlStateManager.enableCull();
        mc.mcProfiler.endStartSection("weather");

        if (flag)
        {
            Shaders.beginWeather();
        }

        renderRainSnow(partialTicks);

        if (flag)
        {
            Shaders.endWeather();
        }

        GlStateManager.depthMask(true);
        renderglobal.renderWorldBorder(entity, partialTicks);

        if (flag)
        {
            ShadersRender.renderHand0(this, partialTicks, pass);
            Shaders.preWater();
        }

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.alphaFunc(516, 0.1F);
        setupFog(0, partialTicks);
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        GlStateManager.shadeModel(7425);
        mc.mcProfiler.endStartSection("translucent");

        if (flag)
        {
            Shaders.beginWater();
        }

        renderglobal.renderBlockLayer(EnumWorldBlockLayer.TRANSLUCENT, partialTicks, pass, entity);

        if (flag)
        {
            Shaders.endWater();
        }

        if (Reflector.ForgeHooksClient_setRenderPass.exists() && !debugView)
        {
            RenderHelper.enableStandardItemLighting();
            mc.mcProfiler.endStartSection("entities");
            Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, Integer.valueOf(1));
            mc.renderGlobal.renderEntities(entity, frustum, partialTicks);
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, Integer.valueOf(-1));
            RenderHelper.disableStandardItemLighting();
        }

        GlStateManager.shadeModel(7424);
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.disableFog();

        if (entity.posY + (double)entity.getEyeHeight() >= 128.0D + (double)(mc.gameSettings.ofCloudsHeight * 128.0F))
        {
            mc.mcProfiler.endStartSection("aboveClouds");
            renderCloudsCheck(renderglobal, partialTicks, pass);
        }

        if (Reflector.ForgeHooksClient_dispatchRenderLast.exists())
        {
            mc.mcProfiler.endStartSection("forge_render_last");
            Reflector.callVoid(Reflector.ForgeHooksClient_dispatchRenderLast, renderglobal, Float.valueOf(partialTicks));
        }

        mc.mcProfiler.endStartSection("hand");
        boolean flag2 = ReflectorForge.renderFirstPersonHand(mc.renderGlobal, partialTicks, pass);

        if (!flag2 && renderHand && !Shaders.isShadowPass)
        {
            if (flag)
            {
                ShadersRender.renderHand1(this, partialTicks, pass);
                Shaders.renderCompositeFinal();
            }

            GlStateManager.clear(256);

            if (flag)
            {
                ShadersRender.renderFPOverlay(this, partialTicks, pass);
            }
            else
            {
                renderHand(partialTicks, pass);
            }

            renderWorldDirections(partialTicks);
        }

        if (flag)
        {
            Shaders.endRender();
        }
    }

    private void renderCloudsCheck(RenderGlobal renderGlobalIn, float partialTicks, int pass)
    {
        if (mc.gameSettings.renderDistanceChunks >= 4 && !Config.isCloudsOff() && Shaders.shouldRenderClouds(mc.gameSettings))
        {
            mc.mcProfiler.endStartSection("clouds");
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(getFOVModifier(partialTicks, true), (float) mc.displayWidth / (float) mc.displayHeight, 0.05F, clipDistance * 4.0F);
            GlStateManager.matrixMode(5888);
            GlStateManager.pushMatrix();
            setupFog(0, partialTicks);
            renderGlobalIn.renderClouds(partialTicks, pass);
            GlStateManager.disableFog();
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(getFOVModifier(partialTicks, true), (float) mc.displayWidth / (float) mc.displayHeight, 0.05F, clipDistance);
            GlStateManager.matrixMode(5888);
        }
    }

    private void addRainParticles()
    {
        float f = mc.theWorld.getRainStrength(1.0F);

        if (!Config.isRainFancy())
        {
            f /= 2.0F;
        }

        if (f != 0.0F && Config.isRainSplash())
        {
            random.setSeed((long) rendererUpdateCount * 312987231L);
            Entity entity = mc.getRenderViewEntity();
            WorldClient worldclient = mc.theWorld;
            BlockPos blockpos = new BlockPos(entity);
            byte b0 = 10;
            double d0 = 0.0D;
            double d1 = 0.0D;
            double d2 = 0.0D;
            int i = 0;
            int j = (int)(100.0F * f * f);

            if (mc.gameSettings.particleSetting == 1)
            {
                j >>= 1;
            }
            else if (mc.gameSettings.particleSetting == 2)
            {
                j = 0;
            }

            for (int k = 0; k < j; ++k)
            {
                BlockPos blockpos1 = worldclient.getPrecipitationHeight(blockpos.add(random.nextInt(b0) - random.nextInt(b0), 0, random.nextInt(b0) - random.nextInt(b0)));
                BiomeGenBase biomegenbase = worldclient.getBiomeGenForCoords(blockpos1);
                BlockPos blockpos2 = blockpos1.down();
                Block block = worldclient.getBlockState(blockpos2).getBlock();

                if (blockpos1.getY() <= blockpos.getY() + b0 && blockpos1.getY() >= blockpos.getY() - b0 && biomegenbase.canSpawnLightningBolt() && biomegenbase.getFloatTemperature(blockpos1) >= 0.15F)
                {
                    double d3 = random.nextDouble();
                    double d4 = random.nextDouble();

                    if (block.getMaterial() == Material.lava)
                    {
                        mc.theWorld.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (double)blockpos1.getX() + d3, (double)((float)blockpos1.getY() + 0.1F) - block.getBlockBoundsMinY(), (double)blockpos1.getZ() + d4, 0.0D, 0.0D, 0.0D);
                    }
                    else if (block.getMaterial() != Material.air)
                    {
                        block.setBlockBoundsBasedOnState(worldclient, blockpos2);
                        ++i;

                        if (random.nextInt(i) == 0)
                        {
                            d0 = (double)blockpos2.getX() + d3;
                            d1 = (double)((float)blockpos2.getY() + 0.1F) + block.getBlockBoundsMaxY() - 1.0D;
                            d2 = (double)blockpos2.getZ() + d4;
                        }

                        mc.theWorld.spawnParticle(EnumParticleTypes.WATER_DROP, (double)blockpos2.getX() + d3, (double)((float)blockpos2.getY() + 0.1F) + block.getBlockBoundsMaxY(), (double)blockpos2.getZ() + d4, 0.0D, 0.0D, 0.0D);
                    }
                }
            }

            if (i > 0 && random.nextInt(3) < rainSoundCounter++)
            {
                rainSoundCounter = 0;

                if (d1 > (double)(blockpos.getY() + 1) && worldclient.getPrecipitationHeight(blockpos).getY() > MathHelper.floor_float((float)blockpos.getY()))
                {
                    mc.theWorld.playSound(d0, d1, d2, "ambient.weather.rain", 0.1F, 0.5F, false);
                }
                else
                {
                    mc.theWorld.playSound(d0, d1, d2, "ambient.weather.rain", 0.2F, 1.0F, false);
                }
            }
        }
    }

    /**
     * Render rain and snow
     */
    protected void renderRainSnow(float partialTicks)
    {
        if (Reflector.ForgeWorldProvider_getWeatherRenderer.exists())
        {
            WorldProvider worldprovider = mc.theWorld.provider;
            Object object = Reflector.call(worldprovider, Reflector.ForgeWorldProvider_getWeatherRenderer);

            if (object != null)
            {
                Reflector.callVoid(object, Reflector.IRenderHandler_render, Float.valueOf(partialTicks), mc.theWorld, mc);
                return;
            }
        }

        float f5 = mc.theWorld.getRainStrength(partialTicks);

        if (f5 > 0.0F)
        {
            if (Config.isRainOff())
            {
                return;
            }

            enableLightmap();
            Entity entity = mc.getRenderViewEntity();
            WorldClient worldclient = mc.theWorld;
            int i = MathHelper.floor_double(entity.posX);
            int j = MathHelper.floor_double(entity.posY);
            int k = MathHelper.floor_double(entity.posZ);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            GlStateManager.disableCull();
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.alphaFunc(516, 0.1F);
            double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
            double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
            double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
            int l = MathHelper.floor_double(d1);
            byte b0 = 5;

            if (Config.isRainFancy())
            {
                b0 = 10;
            }

            byte b1 = -1;
            float f = (float) rendererUpdateCount + partialTicks;
            worldrenderer.setTranslation(-d0, -d1, -d2);

            if (Config.isRainFancy())
            {
                b0 = 10;
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int i1 = k - b0; i1 <= k + b0; ++i1)
            {
                for (int j1 = i - b0; j1 <= i + b0; ++j1)
                {
                    int k1 = (i1 - k + 16) * 32 + j1 - i + 16;
                    double d3 = (double) rainXCoords[k1] * 0.5D;
                    double d4 = (double) rainYCoords[k1] * 0.5D;
                    blockpos$mutableblockpos.func_181079_c(j1, 0, i1);
                    BiomeGenBase biomegenbase = worldclient.getBiomeGenForCoords(blockpos$mutableblockpos);

                    if (biomegenbase.canSpawnLightningBolt() || biomegenbase.getEnableSnow())
                    {
                        int l1 = worldclient.getPrecipitationHeight(blockpos$mutableblockpos).getY();
                        int i2 = j - b0;
                        int j2 = j + b0;

                        if (i2 < l1)
                        {
                            i2 = l1;
                        }

                        if (j2 < l1)
                        {
                            j2 = l1;
                        }

                        int k2 = l1;

                        if (l1 < l)
                        {
                            k2 = l;
                        }

                        if (i2 != j2)
                        {
                            random.setSeed(j1 * j1 * 3121 + j1 * 45238971 ^ i1 * i1 * 418711 + i1 * 13761);
                            blockpos$mutableblockpos.func_181079_c(j1, i2, i1);
                            float f1 = biomegenbase.getFloatTemperature(blockpos$mutableblockpos);

                            if (worldclient.getWorldChunkManager().getTemperatureAtHeight(f1, l1) >= 0.15F)
                            {
                                if (b1 != 0)
                                {
                                    if (b1 >= 0)
                                    {
                                        tessellator.draw();
                                    }

                                    b1 = 0;
                                    mc.getTextureManager().bindTexture(EntityRenderer.locationRainPng);
                                    worldrenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                                }

                                double d5 = ((double)(rendererUpdateCount + j1 * j1 * 3121 + j1 * 45238971 + i1 * i1 * 418711 + i1 * 13761 & 31) + (double)partialTicks) / 32.0D * (3.0D + random.nextDouble());
                                double d6 = (double)((float)j1 + 0.5F) - entity.posX;
                                double d7 = (double)((float)i1 + 0.5F) - entity.posZ;
                                float f2 = MathHelper.sqrt_double(d6 * d6 + d7 * d7) / (float)b0;
                                float f3 = ((1.0F - f2 * f2) * 0.5F + 0.5F) * f5;
                                blockpos$mutableblockpos.func_181079_c(j1, k2, i1);
                                int l2 = worldclient.getCombinedLight(blockpos$mutableblockpos, 0);
                                int i3 = l2 >> 16 & 65535;
                                int j3 = l2 & 65535;
                                worldrenderer.pos((double)j1 - d3 + 0.5D, i2, (double)i1 - d4 + 0.5D).tex(0.0D, (double)i2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f3).lightmap(i3, j3).endVertex();
                                worldrenderer.pos((double)j1 + d3 + 0.5D, i2, (double)i1 + d4 + 0.5D).tex(1.0D, (double)i2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f3).lightmap(i3, j3).endVertex();
                                worldrenderer.pos((double)j1 + d3 + 0.5D, j2, (double)i1 + d4 + 0.5D).tex(1.0D, (double)j2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f3).lightmap(i3, j3).endVertex();
                                worldrenderer.pos((double)j1 - d3 + 0.5D, j2, (double)i1 - d4 + 0.5D).tex(0.0D, (double)j2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f3).lightmap(i3, j3).endVertex();
                            }
                            else
                            {
                                if (b1 != 1)
                                {
                                    if (b1 >= 0)
                                    {
                                        tessellator.draw();
                                    }

                                    b1 = 1;
                                    mc.getTextureManager().bindTexture(EntityRenderer.locationSnowPng);
                                    worldrenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                                }

                                double d8 = ((float)(rendererUpdateCount & 511) + partialTicks) / 512.0F;
                                double d9 = random.nextDouble() + (double)f * 0.01D * (double)((float) random.nextGaussian());
                                double d10 = random.nextDouble() + (double)(f * (float) random.nextGaussian()) * 0.001D;
                                double d11 = (double)((float)j1 + 0.5F) - entity.posX;
                                double d12 = (double)((float)i1 + 0.5F) - entity.posZ;
                                float f6 = MathHelper.sqrt_double(d11 * d11 + d12 * d12) / (float)b0;
                                float f4 = ((1.0F - f6 * f6) * 0.3F + 0.5F) * f5;
                                blockpos$mutableblockpos.func_181079_c(j1, k2, i1);
                                int k3 = (worldclient.getCombinedLight(blockpos$mutableblockpos, 0) * 3 + 15728880) / 4;
                                int l3 = k3 >> 16 & 65535;
                                int i4 = k3 & 65535;
                                worldrenderer.pos((double)j1 - d3 + 0.5D, i2, (double)i1 - d4 + 0.5D).tex(0.0D + d9, (double)i2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f4).lightmap(l3, i4).endVertex();
                                worldrenderer.pos((double)j1 + d3 + 0.5D, i2, (double)i1 + d4 + 0.5D).tex(1.0D + d9, (double)i2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f4).lightmap(l3, i4).endVertex();
                                worldrenderer.pos((double)j1 + d3 + 0.5D, j2, (double)i1 + d4 + 0.5D).tex(1.0D + d9, (double)j2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f4).lightmap(l3, i4).endVertex();
                                worldrenderer.pos((double)j1 - d3 + 0.5D, j2, (double)i1 - d4 + 0.5D).tex(0.0D + d9, (double)j2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f4).lightmap(l3, i4).endVertex();
                            }
                        }
                    }
                }
            }

            if (b1 >= 0)
            {
                tessellator.draw();
            }

            worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
            disableLightmap();
        }
    }

    /**
     * Setup orthogonal projection for rendering GUI screen overlays
     */
    public void setupOverlayRendering()
    {
        ScaledResolution scaledresolution = new ScaledResolution(mc);
        GlStateManager.clear(256);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
    }

    /**
     * calculates fog and calls glClearColor
     */
    private void updateFogColor(float partialTicks)
    {
        WorldClient worldclient = mc.theWorld;
        Entity entity = mc.getRenderViewEntity();
        float f = 0.25F + 0.75F * (float) mc.gameSettings.renderDistanceChunks / 32.0F;
        f = 1.0F - (float)Math.pow(f, 0.25D);
        Vec3 vec3 = worldclient.getSkyColor(mc.getRenderViewEntity(), partialTicks);
        vec3 = CustomColors.getWorldSkyColor(vec3, worldclient, mc.getRenderViewEntity(), partialTicks);
        float f1 = (float)vec3.xCoord;
        float f2 = (float)vec3.yCoord;
        float f3 = (float)vec3.zCoord;
        Vec3 vec31 = worldclient.getFogColor(partialTicks);
        vec31 = CustomColors.getWorldFogColor(vec31, worldclient, mc.getRenderViewEntity(), partialTicks);
        fogColorRed = (float)vec31.xCoord;
        fogColorGreen = (float)vec31.yCoord;
        fogColorBlue = (float)vec31.zCoord;

        if (mc.gameSettings.renderDistanceChunks >= 4)
        {
            double d0 = -1.0D;
            Vec3 vec32 = MathHelper.sin(worldclient.getCelestialAngleRadians(partialTicks)) > 0.0F ? new Vec3(d0, 0.0D, 0.0D) : new Vec3(1.0D, 0.0D, 0.0D);
            float f4 = (float)entity.getLook(partialTicks).dotProduct(vec32);

            if (f4 < 0.0F)
            {
                f4 = 0.0F;
            }

            if (f4 > 0.0F)
            {
                float[] afloat = worldclient.provider.calcSunriseSunsetColors(worldclient.getCelestialAngle(partialTicks), partialTicks);

                if (afloat != null)
                {
                    f4 = f4 * afloat[3];
                    fogColorRed = fogColorRed * (1.0F - f4) + afloat[0] * f4;
                    fogColorGreen = fogColorGreen * (1.0F - f4) + afloat[1] * f4;
                    fogColorBlue = fogColorBlue * (1.0F - f4) + afloat[2] * f4;
                }
            }
        }

        fogColorRed += (f1 - fogColorRed) * f;
        fogColorGreen += (f2 - fogColorGreen) * f;
        fogColorBlue += (f3 - fogColorBlue) * f;
        float f10 = worldclient.getRainStrength(partialTicks);

        if (f10 > 0.0F)
        {
            float f5 = 1.0F - f10 * 0.5F;
            float f12 = 1.0F - f10 * 0.4F;
            fogColorRed *= f5;
            fogColorGreen *= f5;
            fogColorBlue *= f12;
        }

        float f11 = worldclient.getThunderStrength(partialTicks);

        if (f11 > 0.0F)
        {
            float f13 = 1.0F - f11 * 0.5F;
            fogColorRed *= f13;
            fogColorGreen *= f13;
            fogColorBlue *= f13;
        }

        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(mc.theWorld, entity, partialTicks);

        if (cloudFog)
        {
            Vec3 vec33 = worldclient.getCloudColour(partialTicks);
            fogColorRed = (float)vec33.xCoord;
            fogColorGreen = (float)vec33.yCoord;
            fogColorBlue = (float)vec33.zCoord;
        }
        else if (block.getMaterial() == Material.water)
        {
            float f8 = (float)EnchantmentHelper.getRespiration(entity) * 0.2F;

            if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.waterBreathing))
            {
                f8 = f8 * 0.3F + 0.6F;
            }

            fogColorRed = 0.02F + f8;
            fogColorGreen = 0.02F + f8;
            fogColorBlue = 0.2F + f8;
            Vec3 vec34 = CustomColors.getUnderwaterColor(mc.theWorld, mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY + 1.0D, mc.getRenderViewEntity().posZ);

            if (vec34 != null)
            {
                fogColorRed = (float)vec34.xCoord;
                fogColorGreen = (float)vec34.yCoord;
                fogColorBlue = (float)vec34.zCoord;
            }
        }
        else if (block.getMaterial() == Material.lava)
        {
            fogColorRed = 0.6F;
            fogColorGreen = 0.1F;
            fogColorBlue = 0.0F;
        }

        float f9 = fogColor2 + (fogColor1 - fogColor2) * partialTicks;
        fogColorRed *= f9;
        fogColorGreen *= f9;
        fogColorBlue *= f9;
        double d2 = worldclient.provider.getVoidFogYFactor();
        double d1 = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks) * d2;

        if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.blindness))
        {
            int i = ((EntityLivingBase)entity).getActivePotionEffect(Potion.blindness).getDuration();

            if (i < 20)
            {
                d1 *= 1.0F - (float)i / 20.0F;
            }
            else
            {
                d1 = 0.0D;
            }
        }

        if (d1 < 1.0D)
        {
            if (d1 < 0.0D)
            {
                d1 = 0.0D;
            }

            d1 = d1 * d1;
            fogColorRed = (float)((double) fogColorRed * d1);
            fogColorGreen = (float)((double) fogColorGreen * d1);
            fogColorBlue = (float)((double) fogColorBlue * d1);
        }

        if (bossColorModifier > 0.0F)
        {
            float f14 = bossColorModifierPrev + (bossColorModifier - bossColorModifierPrev) * partialTicks;
            fogColorRed = fogColorRed * (1.0F - f14) + fogColorRed * 0.7F * f14;
            fogColorGreen = fogColorGreen * (1.0F - f14) + fogColorGreen * 0.6F * f14;
            fogColorBlue = fogColorBlue * (1.0F - f14) + fogColorBlue * 0.6F * f14;
        }

        if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.nightVision))
        {
            float f15 = getNightVisionBrightness((EntityLivingBase)entity, partialTicks);
            float f6 = 1.0F / fogColorRed;

            if (f6 > 1.0F / fogColorGreen)
            {
                f6 = 1.0F / fogColorGreen;
            }

            if (f6 > 1.0F / fogColorBlue)
            {
                f6 = 1.0F / fogColorBlue;
            }

            fogColorRed = fogColorRed * (1.0F - f15) + fogColorRed * f6 * f15;
            fogColorGreen = fogColorGreen * (1.0F - f15) + fogColorGreen * f6 * f15;
            fogColorBlue = fogColorBlue * (1.0F - f15) + fogColorBlue * f6 * f15;
        }

        if (mc.gameSettings.anaglyph)
        {
            float f16 = (fogColorRed * 30.0F + fogColorGreen * 59.0F + fogColorBlue * 11.0F) / 100.0F;
            float f17 = (fogColorRed * 30.0F + fogColorGreen * 70.0F) / 100.0F;
            float f7 = (fogColorRed * 30.0F + fogColorBlue * 70.0F) / 100.0F;
            fogColorRed = f16;
            fogColorGreen = f17;
            fogColorBlue = f7;
        }

        if (Reflector.EntityViewRenderEvent_FogColors_Constructor.exists())
        {
            Object object = Reflector.newInstance(Reflector.EntityViewRenderEvent_FogColors_Constructor, this, entity, block, Float.valueOf(partialTicks), Float.valueOf(fogColorRed), Float.valueOf(fogColorGreen), Float.valueOf(fogColorBlue));
            Reflector.postForgeBusEvent(object);
            fogColorRed = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_FogColors_red, fogColorRed);
            fogColorGreen = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_FogColors_green, fogColorGreen);
            fogColorBlue = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_FogColors_blue, fogColorBlue);
        }

        Shaders.setClearColor(fogColorRed, fogColorGreen, fogColorBlue, 0.0F);
    }

    /**
     * Sets up the fog to be rendered. If the arg passed in is -1 the fog starts at 0 and goes to 80% of far plane
     * distance and is used for sky rendering.
     */
    private void setupFog(int p_78468_1_, float partialTicks)
    {
        Entity entity = mc.getRenderViewEntity();
        boolean flag = false;
        fogStandard = false;

        if (entity instanceof EntityPlayer)
        {
            flag = ((EntityPlayer)entity).capabilities.isCreativeMode;
        }

        GL11.glFog(GL11.GL_FOG_COLOR, setFogColorBuffer(fogColorRed, fogColorGreen, fogColorBlue, 1.0F));
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(mc.theWorld, entity, partialTicks);
        float f1 = -1.0F;

        if (Reflector.ForgeHooksClient_getFogDensity.exists())
        {
            f1 = Reflector.callFloat(Reflector.ForgeHooksClient_getFogDensity, this, entity, block, Float.valueOf(partialTicks), Float.valueOf(0.1F));
        }

        if (f1 >= 0.0F)
        {
            GlStateManager.setFogDensity(f1);
        }
        else if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.blindness))
        {
            float f2 = 5.0F;
            int i = ((EntityLivingBase)entity).getActivePotionEffect(Potion.blindness).getDuration();

            if (i < 20)
            {
                f2 = 5.0F + (farPlaneDistance - 5.0F) * (1.0F - (float)i / 20.0F);
            }

            if (Config.isShaders())
            {
                Shaders.setFog(9729);
            }
            else
            {
                GlStateManager.setFog(9729);
            }

            if (p_78468_1_ == -1)
            {
                GlStateManager.setFogStart(0.0F);
                GlStateManager.setFogEnd(f2 * 0.8F);
            }
            else
            {
                GlStateManager.setFogStart(f2 * 0.25F);
                GlStateManager.setFogEnd(f2);
            }

            if (GLContext.getCapabilities().GL_NV_fog_distance && Config.isFogFancy())
            {
                GL11.glFogi(34138, 34139);
            }
        }
        else if (cloudFog)
        {
            if (Config.isShaders())
            {
                Shaders.setFog(2048);
            }
            else
            {
                GlStateManager.setFog(2048);
            }

            GlStateManager.setFogDensity(0.1F);
        }
        else if (block.getMaterial() == Material.water)
        {
            if (Config.isShaders())
            {
                Shaders.setFog(2048);
            }
            else
            {
                GlStateManager.setFog(2048);
            }

            if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.waterBreathing))
            {
                GlStateManager.setFogDensity(0.01F);
            }
            else
            {
                GlStateManager.setFogDensity(0.1F - (float)EnchantmentHelper.getRespiration(entity) * 0.03F);
            }

            if (Config.isClearWater())
            {
                GlStateManager.setFogDensity(0.02F);
            }
        }
        else if (block.getMaterial() == Material.lava)
        {
            if (Config.isShaders())
            {
                Shaders.setFog(2048);
            }
            else
            {
                GlStateManager.setFog(2048);
            }

            GlStateManager.setFogDensity(2.0F);
        }
        else
        {
            float f = farPlaneDistance;
            fogStandard = true;

            if (Config.isShaders())
            {
                Shaders.setFog(9729);
            }
            else
            {
                GlStateManager.setFog(9729);
            }

            if (p_78468_1_ == -1)
            {
                GlStateManager.setFogStart(0.0F);
                GlStateManager.setFogEnd(f);
            }
            else
            {
                GlStateManager.setFogStart(f * Config.getFogStart());
                GlStateManager.setFogEnd(f);
            }

            if (GLContext.getCapabilities().GL_NV_fog_distance)
            {
                if (Config.isFogFancy())
                {
                    GL11.glFogi(34138, 34139);
                }

                if (Config.isFogFast())
                {
                    GL11.glFogi(34138, 34140);
                }
            }

            if (mc.theWorld.provider.doesXZShowFog((int)entity.posX, (int)entity.posZ))
            {
                GlStateManager.setFogStart(f * 0.05F);
                GlStateManager.setFogEnd(f);
            }

            if (Reflector.ForgeHooksClient_onFogRender.exists())
            {
                Reflector.callVoid(Reflector.ForgeHooksClient_onFogRender, this, entity, block, Float.valueOf(partialTicks), Integer.valueOf(p_78468_1_), Float.valueOf(f));
            }
        }

        GlStateManager.enableColorMaterial();
        GlStateManager.enableFog();
        GlStateManager.colorMaterial(1028, 4608);
    }

    /**
     * Update and return fogColorBuffer with the RGBA values passed as arguments
     */
    private FloatBuffer setFogColorBuffer(float red, float green, float blue, float alpha)
    {
        if (Config.isShaders())
        {
            Shaders.setFogColor(red, green, blue);
        }

        fogColorBuffer.clear();
        fogColorBuffer.put(red).put(green).put(blue).put(alpha);
        fogColorBuffer.flip();
        return fogColorBuffer;
    }

    public MapItemRenderer getMapItemRenderer()
    {
        return theMapItemRenderer;
    }

    private void waitForServerThread()
    {
        serverWaitTimeCurrent = 0;

        if (Config.isSmoothWorld() && Config.isSingleProcessor())
        {
            if (mc.isIntegratedServerRunning())
            {
                IntegratedServer integratedserver = mc.getIntegratedServer();

                if (integratedserver != null)
                {
                    boolean flag = mc.isGamePaused();

                    if (!flag && !(mc.currentScreen instanceof GuiDownloadTerrain))
                    {
                        if (serverWaitTime > 0)
                        {
                            Lagometer.timerServer.start();
                            Config.sleep(serverWaitTime);
                            Lagometer.timerServer.end();
                            serverWaitTimeCurrent = serverWaitTime;
                        }

                        long i = System.nanoTime() / 1000000L;

                        if (lastServerTime != 0L && lastServerTicks != 0)
                        {
                            long j = i - lastServerTime;

                            if (j < 0L)
                            {
                                lastServerTime = i;
                                j = 0L;
                            }

                            if (j >= 50L)
                            {
                                lastServerTime = i;
                                int k = integratedserver.getTickCounter();
                                int l = k - lastServerTicks;

                                if (l < 0)
                                {
                                    lastServerTicks = k;
                                    l = 0;
                                }

                                if (l < 1 && serverWaitTime < 100)
                                {
                                    serverWaitTime += 2;
                                }

                                if (l > 1 && serverWaitTime > 0)
                                {
                                    --serverWaitTime;
                                }

                                lastServerTicks = k;
                            }
                        }
                        else
                        {
                            lastServerTime = i;
                            lastServerTicks = integratedserver.getTickCounter();
                            avgServerTickDiff = 1.0F;
                            avgServerTimeDiff = 50.0F;
                        }
                    }
                    else
                    {
                        if (mc.currentScreen instanceof GuiDownloadTerrain)
                        {
                            Config.sleep(20L);
                        }

                        lastServerTime = 0L;
                        lastServerTicks = 0;
                    }
                }
            }
        }
        else
        {
            lastServerTime = 0L;
            lastServerTicks = 0;
        }
    }

    private void frameInit()
    {
        if (!initialized)
        {
            TextureUtils.registerResourceListener();

            if (Config.getBitsOs() == 64 && Config.getBitsJre() == 32)
            {
                Config.setNotify64BitJava(true);
            }

            initialized = true;
        }

        Config.checkDisplayMode();
        World world = mc.theWorld;

        if (world != null)
        {
            if (Config.getNewRelease() != null)
            {
                String s = "HD_U".replace("HD_U", "HD Ultra").replace("L", "Light");
                String s1 = s + " " + Config.getNewRelease();
                ChatComponentText chatcomponenttext = new ChatComponentText(I18n.format("of.message.newVersion", s1));
                mc.ingameGUI.getChatGUI().printChatMessage(chatcomponenttext);
                Config.setNewRelease(null);
            }

            if (Config.isNotify64BitJava())
            {
                Config.setNotify64BitJava(false);
                ChatComponentText chatcomponenttext1 = new ChatComponentText(I18n.format("of.message.java64Bit"));
                mc.ingameGUI.getChatGUI().printChatMessage(chatcomponenttext1);
            }
        }

        if (mc.currentScreen instanceof GuiMainMenu)
        {
            updateMainMenu((GuiMainMenu) mc.currentScreen);
        }

        if (updatedWorld != world)
        {
            RandomMobs.worldChanged(updatedWorld, world);
            Config.updateThreadPriorities();
            lastServerTime = 0L;
            lastServerTicks = 0;
            updatedWorld = world;
        }

        if (!setFxaaShader(Shaders.configAntialiasingLevel))
        {
            Shaders.configAntialiasingLevel = 0;
        }
    }

    private void frameFinish()
    {
        if (mc.theWorld != null)
        {
            long i = System.currentTimeMillis();

            if (i > lastErrorCheckTimeMs + 10000L)
            {
                lastErrorCheckTimeMs = i;
                int j = GL11.glGetError();

                if (j != 0)
                {
                    String s = GLU.gluErrorString(j);
                    ChatComponentText chatcomponenttext = new ChatComponentText(I18n.format("of.message.openglError", Integer.valueOf(j), s));
                    mc.ingameGUI.getChatGUI().printChatMessage(chatcomponenttext);
                }
            }
        }
    }

    private void updateMainMenu(GuiMainMenu p_updateMainMenu_1_)
    {
        try
        {
            String s = null;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int i = calendar.get(5);
            int j = calendar.get(2) + 1;

            if (i == 8 && j == 4)
            {
                s = "Happy birthday, OptiFine!";
            }

            if (i == 14 && j == 8)
            {
                s = "Happy birthday, sp614x!";
            }

            if (s == null)
            {
                return;
            }

            Field[] afield = GuiMainMenu.class.getDeclaredFields();

            for (int k = 0; k < afield.length; ++k)
            {
                if (afield[k].getType() == String.class)
                {
                    afield[k].setAccessible(true);
                    afield[k].set(p_updateMainMenu_1_, s);
                    break;
                }
            }
        }
        catch (Throwable var8)
        {
        }
    }

    public boolean setFxaaShader(int p_setFxaaShader_1_)
    {
        if (!OpenGlHelper.isFramebufferEnabled())
        {
            return false;
        }
        else if (theShaderGroup != null && theShaderGroup != fxaaShaders[2] && theShaderGroup != fxaaShaders[4])
        {
            return true;
        }
        else if (p_setFxaaShader_1_ != 2 && p_setFxaaShader_1_ != 4)
        {
            if (theShaderGroup == null)
            {
                return true;
            }
            else
            {
                theShaderGroup.deleteShaderGroup();
                theShaderGroup = null;
                return true;
            }
        }
        else if (theShaderGroup != null && theShaderGroup == fxaaShaders[p_setFxaaShader_1_])
        {
            return true;
        }
        else if (mc.theWorld == null)
        {
            return true;
        }
        else
        {
            loadShader(new ResourceLocation("shaders/post/fxaa_of_" + p_setFxaaShader_1_ + "x.json"));
            fxaaShaders[p_setFxaaShader_1_] = theShaderGroup;
            return useShader;
        }
    }
}
