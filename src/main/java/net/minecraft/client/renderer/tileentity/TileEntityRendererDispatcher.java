package net.minecraft.client.renderer.tileentity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;

public class TileEntityRendererDispatcher
{
    private final Map < Class <? extends TileEntity > , TileEntitySpecialRenderer <? extends TileEntity >> mapSpecialRenderers = Maps.newHashMap();
    public static TileEntityRendererDispatcher instance = new TileEntityRendererDispatcher();
    private FontRenderer fontRenderer;

    /** The player's current X position (same as playerX) */
    public static double staticPlayerX;

    /** The player's current Y position (same as playerY) */
    public static double staticPlayerY;

    /** The player's current Z position (same as playerZ) */
    public static double staticPlayerZ;
    public TextureManager renderEngine;
    public World worldObj;
    public Entity entity;
    public float entityYaw;
    public float entityPitch;
    public double entityX;
    public double entityY;
    public double entityZ;

    private TileEntityRendererDispatcher()
    {
        mapSpecialRenderers.put(TileEntitySign.class, new TileEntitySignRenderer());
        mapSpecialRenderers.put(TileEntityMobSpawner.class, new TileEntityMobSpawnerRenderer());
        mapSpecialRenderers.put(TileEntityPiston.class, new TileEntityPistonRenderer());
        mapSpecialRenderers.put(TileEntityChest.class, new TileEntityChestRenderer());
        mapSpecialRenderers.put(TileEntityEnderChest.class, new TileEntityEnderChestRenderer());
        mapSpecialRenderers.put(TileEntityEnchantmentTable.class, new TileEntityEnchantmentTableRenderer());
        mapSpecialRenderers.put(TileEntityEndPortal.class, new TileEntityEndPortalRenderer());
        mapSpecialRenderers.put(TileEntityBeacon.class, new TileEntityBeaconRenderer());
        mapSpecialRenderers.put(TileEntitySkull.class, new TileEntitySkullRenderer());
        mapSpecialRenderers.put(TileEntityBanner.class, new TileEntityBannerRenderer());

        for (TileEntitySpecialRenderer<?> tileentityspecialrenderer : mapSpecialRenderers.values())
        {
            tileentityspecialrenderer.setRendererDispatcher(this);
        }
    }

    public <T extends TileEntity> TileEntitySpecialRenderer<T> getSpecialRendererByClass(Class <? extends TileEntity > teClass)
    {
        TileEntitySpecialRenderer <? extends TileEntity > tileentityspecialrenderer = mapSpecialRenderers.get(teClass);

        if (tileentityspecialrenderer == null && teClass != TileEntity.class)
        {
            tileentityspecialrenderer = this.getSpecialRendererByClass((Class <? extends TileEntity >)teClass.getSuperclass());
            mapSpecialRenderers.put(teClass, tileentityspecialrenderer);
        }

        return (TileEntitySpecialRenderer<T>)tileentityspecialrenderer;
    }

    public <T extends TileEntity> TileEntitySpecialRenderer<T> getSpecialRenderer(TileEntity tileEntityIn)
    {
        return tileEntityIn == null ? null : getSpecialRendererByClass(tileEntityIn.getClass());
    }

    public void cacheActiveRenderInfo(World worldIn, TextureManager textureManagerIn, FontRenderer fontrendererIn, Entity entityIn, float partialTicks)
    {
        if (worldObj != worldIn)
        {
            setWorld(worldIn);
        }

        renderEngine = textureManagerIn;
        entity = entityIn;
        fontRenderer = fontrendererIn;
        entityYaw = entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks;
        entityPitch = entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks;
        entityX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        entityY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        entityZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
    }

    public void renderTileEntity(TileEntity tileentityIn, float partialTicks, int destroyStage)
    {
        if (tileentityIn.getDistanceSq(entityX, entityY, entityZ) < tileentityIn.getMaxRenderDistanceSquared())
        {
            int i = worldObj.getCombinedLight(tileentityIn.getPos(), 0);
            int j = i % 65536;
            int k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos blockpos = tileentityIn.getPos();
            renderTileEntityAt(tileentityIn, (double)blockpos.getX() - TileEntityRendererDispatcher.staticPlayerX, (double)blockpos.getY() - TileEntityRendererDispatcher.staticPlayerY, (double)blockpos.getZ() - TileEntityRendererDispatcher.staticPlayerZ, partialTicks, destroyStage);
        }
    }

    /**
     * Render this TileEntity at a given set of coordinates
     */
    public void renderTileEntityAt(TileEntity tileEntityIn, double x, double y, double z, float partialTicks)
    {
        renderTileEntityAt(tileEntityIn, x, y, z, partialTicks, -1);
    }

    public void renderTileEntityAt(TileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage)
    {
        TileEntitySpecialRenderer<TileEntity> tileentityspecialrenderer = this.getSpecialRenderer(tileEntityIn);

        if (tileentityspecialrenderer != null)
        {
            try
            {
                tileentityspecialrenderer.renderTileEntityAt(tileEntityIn, x, y, z, partialTicks, destroyStage);
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Block Entity");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Block Entity Details");
                tileEntityIn.addInfoToCrashReport(crashreportcategory);
                throw new ReportedException(crashreport);
            }
        }
    }

    public void setWorld(World worldIn)
    {
        worldObj = worldIn;
    }

    public FontRenderer getFontRenderer()
    {
        return fontRenderer;
    }
}
