package net.minecraft.client.particle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import optifine.Config;
import optifine.Reflector;

public class EffectRenderer
{
    private static final ResourceLocation particleTextures = new ResourceLocation("textures/particle/particles.png");

    /** Reference to the World object. */
    protected World worldObj;
    private final List<EntityFX>[][] fxLayers = new List[4][];
    private final List<EntityParticleEmitter> particleEmitters = new CopyOnWriteArrayList<>();
    private final TextureManager renderer;

    /** RNG. */
    private final Random rand = new Random();
    private final Map particleTypes = Maps.newHashMap();

    public EffectRenderer(World worldIn, TextureManager rendererIn)
    {
        worldObj = worldIn;
        renderer = rendererIn;

        for (int i = 0; i < 4; ++i)
        {
            fxLayers[i] = new List[2];

            for (int j = 0; j < 2; ++j)
            {
                fxLayers[i][j] = Lists.newArrayList();
            }
        }

        registerVanillaParticles();
    }

    private void registerVanillaParticles()
    {
        registerParticle(EnumParticleTypes.EXPLOSION_NORMAL.getParticleID(), new EntityExplodeFX.Factory());
        registerParticle(EnumParticleTypes.WATER_BUBBLE.getParticleID(), new EntityBubbleFX.Factory());
        registerParticle(EnumParticleTypes.WATER_SPLASH.getParticleID(), new EntitySplashFX.Factory());
        registerParticle(EnumParticleTypes.WATER_WAKE.getParticleID(), new EntityFishWakeFX.Factory());
        registerParticle(EnumParticleTypes.WATER_DROP.getParticleID(), new EntityRainFX.Factory());
        registerParticle(EnumParticleTypes.SUSPENDED.getParticleID(), new EntitySuspendFX.Factory());
        registerParticle(EnumParticleTypes.SUSPENDED_DEPTH.getParticleID(), new EntityAuraFX.Factory());
        registerParticle(EnumParticleTypes.CRIT.getParticleID(), new EntityCrit2FX.Factory());
        registerParticle(EnumParticleTypes.CRIT_MAGIC.getParticleID(), new EntityCrit2FX.MagicFactory());
        registerParticle(EnumParticleTypes.SMOKE_NORMAL.getParticleID(), new EntitySmokeFX.Factory());
        registerParticle(EnumParticleTypes.SMOKE_LARGE.getParticleID(), new EntityCritFX.Factory());
        registerParticle(EnumParticleTypes.SPELL.getParticleID(), new EntitySpellParticleFX.Factory());
        registerParticle(EnumParticleTypes.SPELL_INSTANT.getParticleID(), new EntitySpellParticleFX.InstantFactory());
        registerParticle(EnumParticleTypes.SPELL_MOB.getParticleID(), new EntitySpellParticleFX.MobFactory());
        registerParticle(EnumParticleTypes.SPELL_MOB_AMBIENT.getParticleID(), new EntitySpellParticleFX.AmbientMobFactory());
        registerParticle(EnumParticleTypes.SPELL_WITCH.getParticleID(), new EntitySpellParticleFX.WitchFactory());
        registerParticle(EnumParticleTypes.DRIP_WATER.getParticleID(), new EntityDropParticleFX.WaterFactory());
        registerParticle(EnumParticleTypes.DRIP_LAVA.getParticleID(), new EntityDropParticleFX.LavaFactory());
        registerParticle(EnumParticleTypes.VILLAGER_ANGRY.getParticleID(), new EntityHeartFX.AngryVillagerFactory());
        registerParticle(EnumParticleTypes.VILLAGER_HAPPY.getParticleID(), new EntityAuraFX.HappyVillagerFactory());
        registerParticle(EnumParticleTypes.TOWN_AURA.getParticleID(), new EntityAuraFX.Factory());
        registerParticle(EnumParticleTypes.NOTE.getParticleID(), new EntityNoteFX.Factory());
        registerParticle(EnumParticleTypes.PORTAL.getParticleID(), new EntityPortalFX.Factory());
        registerParticle(EnumParticleTypes.ENCHANTMENT_TABLE.getParticleID(), new EntityEnchantmentTableParticleFX.EnchantmentTable());
        registerParticle(EnumParticleTypes.FLAME.getParticleID(), new EntityFlameFX.Factory());
        registerParticle(EnumParticleTypes.LAVA.getParticleID(), new EntityLavaFX.Factory());
        registerParticle(EnumParticleTypes.FOOTSTEP.getParticleID(), new EntityFootStepFX.Factory());
        registerParticle(EnumParticleTypes.CLOUD.getParticleID(), new EntityCloudFX.Factory());
        registerParticle(EnumParticleTypes.REDSTONE.getParticleID(), new EntityReddustFX.Factory());
        registerParticle(EnumParticleTypes.SNOWBALL.getParticleID(), new EntityBreakingFX.SnowballFactory());
        registerParticle(EnumParticleTypes.SNOW_SHOVEL.getParticleID(), new EntitySnowShovelFX.Factory());
        registerParticle(EnumParticleTypes.SLIME.getParticleID(), new EntityBreakingFX.SlimeFactory());
        registerParticle(EnumParticleTypes.HEART.getParticleID(), new EntityHeartFX.Factory());
        registerParticle(EnumParticleTypes.BARRIER.getParticleID(), new Barrier.Factory());
        registerParticle(EnumParticleTypes.ITEM_CRACK.getParticleID(), new EntityBreakingFX.Factory());
        registerParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), new EntityDiggingFX.Factory());
        registerParticle(EnumParticleTypes.BLOCK_DUST.getParticleID(), new EntityBlockDustFX.Factory());
        registerParticle(EnumParticleTypes.EXPLOSION_HUGE.getParticleID(), new EntityHugeExplodeFX.Factory());
        registerParticle(EnumParticleTypes.EXPLOSION_LARGE.getParticleID(), new EntityLargeExplodeFX.Factory());
        registerParticle(EnumParticleTypes.FIREWORKS_SPARK.getParticleID(), new EntityFirework.Factory());
        registerParticle(EnumParticleTypes.MOB_APPEARANCE.getParticleID(), new MobAppearance.Factory());
    }

    public void registerParticle(int id, IParticleFactory particleFactory)
    {
        particleTypes.put(id, particleFactory);
    }

    public void emitParticleAtEntity(Entity entityIn, EnumParticleTypes particleTypes)
    {
        particleEmitters.add(new EntityParticleEmitter(worldObj, entityIn, particleTypes));
    }

    /**
     * Spawns the relevant particle according to the particle id.
     */
    public EntityFX spawnEffectParticle(int particleId, double p_178927_2_, double p_178927_4_, double p_178927_6_, double p_178927_8_, double p_178927_10_, double p_178927_12_, int... p_178927_14_)
    {
        IParticleFactory iparticlefactory = (IParticleFactory) particleTypes.get(particleId);

        if (iparticlefactory != null)
        {
            EntityFX entityfx = iparticlefactory.getEntityFX(particleId, worldObj, p_178927_2_, p_178927_4_, p_178927_6_, p_178927_8_, p_178927_10_, p_178927_12_, p_178927_14_);

            if (entityfx != null)
            {
                addEffect(entityfx);
                return entityfx;
            }
        }

        return null;
    }

    public void addEffect(EntityFX effect)
    {
        if (effect != null)
        {
            if (!(effect instanceof EntityFirework.SparkFX) || Config.isFireworkParticles())
            {
                int i = effect.getFXLayer();
                int j = effect.getAlpha() != 1.0F ? 0 : 1;

                if (fxLayers[i][j].size() >= 4000)
                {
                    fxLayers[i][j].remove(0);
                }

                if (!(effect instanceof Barrier) || !reuseBarrierParticle(effect, fxLayers[i][j]))
                {
                    fxLayers[i][j].add(effect);
                }
            }
        }
    }

    public void updateEffects()
    {
        for (int i = 0; i < 4; ++i) {
            updateEffectLayer(i);
        }

        ArrayList<EntityParticleEmitter> arraylist = Lists.newArrayList();

        for (EntityParticleEmitter particleEmitter : particleEmitters) {
            particleEmitter.onUpdate();

            if (particleEmitter.isDead) {
                arraylist.add(particleEmitter);
            }
        }

        particleEmitters.removeAll(arraylist);
    }

    private void updateEffectLayer(int p_178922_1_)
    {
        for (int i = 0; i < 2; ++i)
        {
            updateEffectAlphaLayer(fxLayers[p_178922_1_][i]);
        }
    }

    private void updateEffectAlphaLayer(List<EntityFX> p_178925_1_)
    {
        ArrayList<EntityFX> arraylist = Lists.newArrayList();

        for (EntityFX entityfx : new ArrayList<>(p_178925_1_)) {
            tickParticle(entityfx);

            if (entityfx.isDead) {
                arraylist.add(entityfx);
            }
        }

        p_178925_1_.removeAll(arraylist);
    }

    private void tickParticle(final EntityFX p_178923_1_)
    {
        try
        {
            p_178923_1_.onUpdate();
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking Particle");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being ticked");
            final int i = p_178923_1_.getFXLayer();
            crashreportcategory.addCrashSectionCallable("Particle", p_178923_1_::toString);
            crashreportcategory.addCrashSectionCallable("Particle Type", () -> i == 0 ? "MISC_TEXTURE" : (i == 1 ? "TERRAIN_TEXTURE" : (i == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i)));
            throw new ReportedException(crashreport);
        }
    }

    /**
     * Renders all current particles. Args player, partialTickTime
     */
    public void renderParticles(Entity entityIn, float partialTicks)
    {
        float f = ActiveRenderInfo.getRotationX();
        float f1 = ActiveRenderInfo.getRotationZ();
        float f2 = ActiveRenderInfo.getRotationYZ();
        float f3 = ActiveRenderInfo.getRotationXY();
        float f4 = ActiveRenderInfo.getRotationXZ();
        EntityFX.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        EntityFX.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        EntityFX.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.alphaFunc(516, 0.003921569F);

        for (int i = 0; i < 3; ++i)
        {
            final int j = i;

            for (int k = 0; k < 2; ++k)
            {
                if (!fxLayers[j][k].isEmpty())
                {
                    switch (k)
                    {
                        case 0:
                            GlStateManager.depthMask(false);
                            break;

                        case 1:
                            GlStateManager.depthMask(true);
                    }

                    switch (j)
                    {
                        case 0:
                        default:
                            renderer.bindTexture(EffectRenderer.particleTextures);
                            break;

                        case 1:
                            renderer.bindTexture(TextureMap.locationBlocksTexture);
                    }

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                    worldrenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

                    for (int l = 0; l < fxLayers[j][k].size(); ++l)
                    {
                        final EntityFX entityfx = (EntityFX) fxLayers[j][k].get(l);

                        try
                        {
                            entityfx.renderParticle(worldrenderer, entityIn, partialTicks, f, f4, f1, f2, f3);
                        }
                        catch (Throwable throwable)
                        {
                            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                            crashreportcategory.addCrashSectionCallable("Particle", entityfx::toString);
                            crashreportcategory.addCrashSectionCallable("Particle Type", () -> j == 0 ? "MISC_TEXTURE" : (j == 1 ? "TERRAIN_TEXTURE" : (j == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + j)));
                            throw new ReportedException(crashreport);
                        }
                    }

                    tessellator.draw();
                }
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1F);
    }

    public void renderLitParticles(Entity entityIn, float p_78872_2_)
    {
        float f = 0.017453292F;
        float f1 = MathHelper.cos(entityIn.rotationYaw * 0.017453292F);
        float f2 = MathHelper.sin(entityIn.rotationYaw * 0.017453292F);
        float f3 = -f2 * MathHelper.sin(entityIn.rotationPitch * 0.017453292F);
        float f4 = f1 * MathHelper.sin(entityIn.rotationPitch * 0.017453292F);
        float f5 = MathHelper.cos(entityIn.rotationPitch * 0.017453292F);

        for (int i = 0; i < 2; ++i)
        {
            List<EntityFX> list = fxLayers[3][i];

            if (!list.isEmpty())
            {
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();

                for (EntityFX entityfx : list) {
                    entityfx.renderParticle(worldrenderer, entityIn, p_78872_2_, f1, f5, f2, f3, f4);
                }
            }
        }
    }

    public void clearEffects(World worldIn)
    {
        worldObj = worldIn;

        for (int i = 0; i < 4; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                fxLayers[i][j].clear();
            }
        }

        particleEmitters.clear();
    }

    public void addBlockDestroyEffects(BlockPos pos, IBlockState state)
    {
        boolean flag;

        if (Reflector.ForgeBlock_addDestroyEffects.exists() && Reflector.ForgeBlock_isAir.exists())
        {
            Block block = state.getBlock();
            Reflector.callBoolean(block, Reflector.ForgeBlock_isAir, worldObj, pos);
            flag = !Reflector.callBoolean(block, Reflector.ForgeBlock_isAir, worldObj, pos) && !Reflector.callBoolean(block, Reflector.ForgeBlock_addDestroyEffects, worldObj, pos, this);
        }
        else
        {
            flag = state.getBlock().getMaterial() != Material.air;
        }

        if (flag)
        {
            state = state.getBlock().getActualState(state, worldObj, pos);
            byte b0 = 4;

            for (int i = 0; i < b0; ++i)
            {
                for (int j = 0; j < b0; ++j)
                {
                    for (int k = 0; k < b0; ++k)
                    {
                        double d0 = (double)pos.getX() + ((double)i + 0.5D) / (double)b0;
                        double d1 = (double)pos.getY() + ((double)j + 0.5D) / (double)b0;
                        double d2 = (double)pos.getZ() + ((double)k + 0.5D) / (double)b0;
                        addEffect((new EntityDiggingFX(worldObj, d0, d1, d2, d0 - (double)pos.getX() - 0.5D, d1 - (double)pos.getY() - 0.5D, d2 - (double)pos.getZ() - 0.5D, state)).func_174846_a(pos));
                    }
                }
            }
        }
    }

    /**
     * Adds block hit particles for the specified block
     */
    public void addBlockHitEffects(BlockPos pos, EnumFacing side)
    {
        IBlockState iblockstate = worldObj.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (block.getRenderType() != -1)
        {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            float f = 0.1F;
            double d0 = (double)i + rand.nextDouble() * (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - (double)(f * 2.0F)) + (double)f + block.getBlockBoundsMinX();
            double d1 = (double)j + rand.nextDouble() * (block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() - (double)(f * 2.0F)) + (double)f + block.getBlockBoundsMinY();
            double d2 = (double)k + rand.nextDouble() * (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() - (double)(f * 2.0F)) + (double)f + block.getBlockBoundsMinZ();

            if (side == EnumFacing.DOWN)
            {
                d1 = (double)j + block.getBlockBoundsMinY() - (double)f;
            }

            if (side == EnumFacing.UP)
            {
                d1 = (double)j + block.getBlockBoundsMaxY() + (double)f;
            }

            if (side == EnumFacing.NORTH)
            {
                d2 = (double)k + block.getBlockBoundsMinZ() - (double)f;
            }

            if (side == EnumFacing.SOUTH)
            {
                d2 = (double)k + block.getBlockBoundsMaxZ() + (double)f;
            }

            if (side == EnumFacing.WEST)
            {
                d0 = (double)i + block.getBlockBoundsMinX() - (double)f;
            }

            if (side == EnumFacing.EAST)
            {
                d0 = (double)i + block.getBlockBoundsMaxX() + (double)f;
            }

            addEffect((new EntityDiggingFX(worldObj, d0, d1, d2, 0.0D, 0.0D, 0.0D, iblockstate)).func_174846_a(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
        }
    }

    public void moveToAlphaLayer(EntityFX effect)
    {
        moveToLayer(effect, 1, 0);
    }

    public void moveToNoAlphaLayer(EntityFX effect)
    {
        moveToLayer(effect, 0, 1);
    }

    private void moveToLayer(EntityFX effect, int p_178924_2_, int p_178924_3_)
    {
        for (int i = 0; i < 4; ++i)
        {
            if (fxLayers[i][p_178924_2_].contains(effect))
            {
                fxLayers[i][p_178924_2_].remove(effect);
                fxLayers[i][p_178924_3_].add(effect);
            }
        }
    }

    public String getStatistics()
    {
        int i = 0;

        for (int j = 0; j < 4; ++j)
        {
            for (int k = 0; k < 2; ++k)
            {
                i += fxLayers[j][k].size();
            }
        }

        return "" + i;
    }

    private boolean reuseBarrierParticle(EntityFX p_reuseBarrierParticle_1_, List<EntityFX> p_reuseBarrierParticle_2_)
    {
        for (EntityFX entityfx : p_reuseBarrierParticle_2_)
        {
            if (entityfx instanceof Barrier && p_reuseBarrierParticle_1_.posX == entityfx.posX && p_reuseBarrierParticle_1_.posY == entityfx.posY && p_reuseBarrierParticle_1_.posZ == entityfx.posZ)
            {
                entityfx.particleAge = 0;
                return true;
            }
        }

        return false;
    }

    public void addBlockHitEffects(BlockPos p_addBlockHitEffects_1_, MovingObjectPosition p_addBlockHitEffects_2_)
    {
        Block block = worldObj.getBlockState(p_addBlockHitEffects_1_).getBlock();
        boolean flag = Reflector.callBoolean(block, Reflector.ForgeBlock_addHitEffects, worldObj, p_addBlockHitEffects_2_, this);

        if (block != null && !flag)
        {
            addBlockHitEffects(p_addBlockHitEffects_1_, p_addBlockHitEffects_2_.sideHit);
        }
    }
}
