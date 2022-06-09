package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityEnderPearl extends EntityThrowable
{
    private EntityLivingBase field_181555_c;

    public EntityEnderPearl(World p_i46455_1_)
    {
        super(p_i46455_1_);
    }

    public EntityEnderPearl(World worldIn, EntityLivingBase p_i1783_2_)
    {
        super(worldIn, p_i1783_2_);
        field_181555_c = p_i1783_2_;
    }

    public EntityEnderPearl(World worldIn, double p_i1784_2_, double p_i1784_4_, double p_i1784_6_)
    {
        super(worldIn, p_i1784_2_, p_i1784_4_, p_i1784_6_);
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    protected void onImpact(MovingObjectPosition p_70184_1_)
    {
        EntityLivingBase entitylivingbase = getThrower();

        if (p_70184_1_.entityHit != null)
        {
            if (p_70184_1_.entityHit == field_181555_c)
            {
                return;
            }

            p_70184_1_.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, entitylivingbase), 0.0F);
        }

        for (int i = 0; i < 32; ++i)
        {
            worldObj.spawnParticle(EnumParticleTypes.PORTAL, posX, posY + rand.nextDouble() * 2.0D, posZ, rand.nextGaussian(), 0.0D, rand.nextGaussian());
        }

        if (!worldObj.isRemote)
        {
            if (entitylivingbase instanceof EntityPlayerMP)
            {
                EntityPlayerMP entityplayermp = (EntityPlayerMP)entitylivingbase;

                if (entityplayermp.playerNetServerHandler.getNetworkManager().isChannelOpen() && entityplayermp.worldObj == worldObj && !entityplayermp.isPlayerSleeping())
                {
                    if (rand.nextFloat() < 0.05F && worldObj.getGameRules().getBoolean("doMobSpawning"))
                    {
                        EntityEndermite entityendermite = new EntityEndermite(worldObj);
                        entityendermite.setSpawnedByPlayer(true);
                        entityendermite.setLocationAndAngles(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, entitylivingbase.rotationYaw, entitylivingbase.rotationPitch);
                        worldObj.spawnEntityInWorld(entityendermite);
                    }

                    if (entitylivingbase.isRiding())
                    {
                        entitylivingbase.mountEntity(null);
                    }

                    entitylivingbase.setPositionAndUpdate(posX, posY, posZ);
                    entitylivingbase.fallDistance = 0.0F;
                    entitylivingbase.attackEntityFrom(DamageSource.fall, 5.0F);
                }
            }
            else if (entitylivingbase != null)
            {
                entitylivingbase.setPositionAndUpdate(posX, posY, posZ);
                entitylivingbase.fallDistance = 0.0F;
            }

            setDead();
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        EntityLivingBase entitylivingbase = getThrower();

        if (entitylivingbase != null && entitylivingbase instanceof EntityPlayer && !entitylivingbase.isEntityAlive())
        {
            setDead();
        }
        else
        {
            super.onUpdate();
        }
    }
}
