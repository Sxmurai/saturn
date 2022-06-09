package net.minecraft.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class Explosion
{
    /** whether or not the explosion sets fire to blocks around it */
    private final boolean isFlaming;

    /** whether or not this explosion spawns smoke particles */
    private final boolean isSmoking;
    private final Random explosionRNG;
    private final World worldObj;
    private final double explosionX;
    private final double explosionY;
    private final double explosionZ;
    private final Entity exploder;
    private final float explosionSize;
    private final List<BlockPos> affectedBlockPositions;
    private final Map<EntityPlayer, Vec3> playerKnockbackMap;

    public Explosion(World worldIn, Entity p_i45752_2_, double p_i45752_3_, double p_i45752_5_, double p_i45752_7_, float p_i45752_9_, List<BlockPos> p_i45752_10_)
    {
        this(worldIn, p_i45752_2_, p_i45752_3_, p_i45752_5_, p_i45752_7_, p_i45752_9_, false, true, p_i45752_10_);
    }

    public Explosion(World worldIn, Entity p_i45753_2_, double p_i45753_3_, double p_i45753_5_, double p_i45753_7_, float p_i45753_9_, boolean p_i45753_10_, boolean p_i45753_11_, List<BlockPos> p_i45753_12_)
    {
        this(worldIn, p_i45753_2_, p_i45753_3_, p_i45753_5_, p_i45753_7_, p_i45753_9_, p_i45753_10_, p_i45753_11_);
        affectedBlockPositions.addAll(p_i45753_12_);
    }

    public Explosion(World worldIn, Entity p_i45754_2_, double p_i45754_3_, double p_i45754_5_, double p_i45754_7_, float size, boolean p_i45754_10_, boolean p_i45754_11_)
    {
        explosionRNG = new Random();
        affectedBlockPositions = Lists.newArrayList();
        playerKnockbackMap = Maps.newHashMap();
        worldObj = worldIn;
        exploder = p_i45754_2_;
        explosionSize = size;
        explosionX = p_i45754_3_;
        explosionY = p_i45754_5_;
        explosionZ = p_i45754_7_;
        isFlaming = p_i45754_10_;
        isSmoking = p_i45754_11_;
    }

    /**
     * Does the first part of the explosion (destroy blocks)
     */
    public void doExplosionA()
    {
        Set<BlockPos> set = Sets.newHashSet();
        int i = 16;

        for (int j = 0; j < 16; ++j)
        {
            for (int k = 0; k < 16; ++k)
            {
                for (int l = 0; l < 16; ++l)
                {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15)
                    {
                        double d0 = (float)j / 15.0F * 2.0F - 1.0F;
                        double d1 = (float)k / 15.0F * 2.0F - 1.0F;
                        double d2 = (float)l / 15.0F * 2.0F - 1.0F;
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        float f = explosionSize * (0.7F + worldObj.rand.nextFloat() * 0.6F);
                        double d4 = explosionX;
                        double d6 = explosionY;
                        double d8 = explosionZ;

                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F)
                        {
                            BlockPos blockpos = new BlockPos(d4, d6, d8);
                            IBlockState iblockstate = worldObj.getBlockState(blockpos);

                            if (iblockstate.getBlock().getMaterial() != Material.air)
                            {
                                float f2 = exploder != null ? exploder.getExplosionResistance(this, worldObj, blockpos, iblockstate) : iblockstate.getBlock().getExplosionResistance(null);
                                f -= (f2 + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && (exploder == null || exploder.verifyExplosion(this, worldObj, blockpos, iblockstate, f)))
                            {
                                set.add(blockpos);
                            }

                            d4 += d0 * 0.30000001192092896D;
                            d6 += d1 * 0.30000001192092896D;
                            d8 += d2 * 0.30000001192092896D;
                        }
                    }
                }
            }
        }

        affectedBlockPositions.addAll(set);
        float f3 = explosionSize * 2.0F;
        int k1 = MathHelper.floor_double(explosionX - (double)f3 - 1.0D);
        int l1 = MathHelper.floor_double(explosionX + (double)f3 + 1.0D);
        int i2 = MathHelper.floor_double(explosionY - (double)f3 - 1.0D);
        int i1 = MathHelper.floor_double(explosionY + (double)f3 + 1.0D);
        int j2 = MathHelper.floor_double(explosionZ - (double)f3 - 1.0D);
        int j1 = MathHelper.floor_double(explosionZ + (double)f3 + 1.0D);
        List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(exploder, new AxisAlignedBB(k1, i2, j2, l1, i1, j1));
        Vec3 vec3 = new Vec3(explosionX, explosionY, explosionZ);

        for (int k2 = 0; k2 < list.size(); ++k2)
        {
            Entity entity = list.get(k2);

            if (!entity.isImmuneToExplosions())
            {
                double d12 = entity.getDistance(explosionX, explosionY, explosionZ) / (double)f3;

                if (d12 <= 1.0D)
                {
                    double d5 = entity.posX - explosionX;
                    double d7 = entity.posY + (double)entity.getEyeHeight() - explosionY;
                    double d9 = entity.posZ - explosionZ;
                    double d13 = MathHelper.sqrt_double(d5 * d5 + d7 * d7 + d9 * d9);

                    if (d13 != 0.0D)
                    {
                        d5 = d5 / d13;
                        d7 = d7 / d13;
                        d9 = d9 / d13;
                        double d14 = worldObj.getBlockDensity(vec3, entity.getEntityBoundingBox());
                        double d10 = (1.0D - d12) * d14;
                        entity.attackEntityFrom(DamageSource.setExplosionSource(this), (float)((int)((d10 * d10 + d10) / 2.0D * 8.0D * (double)f3 + 1.0D)));
                        double d11 = EnchantmentProtection.func_92092_a(entity, d10);
                        entity.motionX += d5 * d11;
                        entity.motionY += d7 * d11;
                        entity.motionZ += d9 * d11;

                        if (entity instanceof EntityPlayer && !((EntityPlayer)entity).capabilities.disableDamage)
                        {
                            playerKnockbackMap.put((EntityPlayer)entity, new Vec3(d5 * d10, d7 * d10, d9 * d10));
                        }
                    }
                }
            }
        }
    }

    /**
     * Does the second part of the explosion (sound, particles, drop spawn)
     */
    public void doExplosionB(boolean spawnParticles)
    {
        worldObj.playSoundEffect(explosionX, explosionY, explosionZ, "random.explode", 4.0F, (1.0F + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.2F) * 0.7F);

        if (explosionSize >= 2.0F && isSmoking)
        {
            worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, explosionX, explosionY, explosionZ, 1.0D, 0.0D, 0.0D);
        }
        else
        {
            worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, explosionX, explosionY, explosionZ, 1.0D, 0.0D, 0.0D);
        }

        if (isSmoking)
        {
            for (BlockPos blockpos : affectedBlockPositions)
            {
                Block block = worldObj.getBlockState(blockpos).getBlock();

                if (spawnParticles)
                {
                    double d0 = (float)blockpos.getX() + worldObj.rand.nextFloat();
                    double d1 = (float)blockpos.getY() + worldObj.rand.nextFloat();
                    double d2 = (float)blockpos.getZ() + worldObj.rand.nextFloat();
                    double d3 = d0 - explosionX;
                    double d4 = d1 - explosionY;
                    double d5 = d2 - explosionZ;
                    double d6 = MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
                    d3 = d3 / d6;
                    d4 = d4 / d6;
                    d5 = d5 / d6;
                    double d7 = 0.5D / (d6 / (double) explosionSize + 0.1D);
                    d7 = d7 * (double)(worldObj.rand.nextFloat() * worldObj.rand.nextFloat() + 0.3F);
                    d3 = d3 * d7;
                    d4 = d4 * d7;
                    d5 = d5 * d7;
                    worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (d0 + explosionX * 1.0D) / 2.0D, (d1 + explosionY * 1.0D) / 2.0D, (d2 + explosionZ * 1.0D) / 2.0D, d3, d4, d5);
                    worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5);
                }

                if (block.getMaterial() != Material.air)
                {
                    if (block.canDropFromExplosion(this))
                    {
                        block.dropBlockAsItemWithChance(worldObj, blockpos, worldObj.getBlockState(blockpos), 1.0F / explosionSize, 0);
                    }

                    worldObj.setBlockState(blockpos, Blocks.air.getDefaultState(), 3);
                    block.onBlockDestroyedByExplosion(worldObj, blockpos, this);
                }
            }
        }

        if (isFlaming)
        {
            for (BlockPos blockpos1 : affectedBlockPositions)
            {
                if (worldObj.getBlockState(blockpos1).getBlock().getMaterial() == Material.air && worldObj.getBlockState(blockpos1.down()).getBlock().isFullBlock() && explosionRNG.nextInt(3) == 0)
                {
                    worldObj.setBlockState(blockpos1, Blocks.fire.getDefaultState());
                }
            }
        }
    }

    public Map<EntityPlayer, Vec3> getPlayerKnockbackMap()
    {
        return playerKnockbackMap;
    }

    /**
     * Returns either the entity that placed the explosive block, the entity that caused the explosion or null.
     */
    public EntityLivingBase getExplosivePlacedBy()
    {
        return exploder == null ? null : (exploder instanceof EntityTNTPrimed ? ((EntityTNTPrimed) exploder).getTntPlacedBy() : (exploder instanceof EntityLivingBase ? (EntityLivingBase) exploder : null));
    }

    public void func_180342_d()
    {
        affectedBlockPositions.clear();
    }

    public List<BlockPos> getAffectedBlockPositions()
    {
        return affectedBlockPositions;
    }
}
