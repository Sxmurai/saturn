package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityTNTPrimed extends Entity
{
    /** How long the fuse is */
    public int fuse;
    private EntityLivingBase tntPlacedBy;

    public EntityTNTPrimed(World worldIn)
    {
        super(worldIn);
        preventEntitySpawning = true;
        setSize(0.98F, 0.98F);
    }

    public EntityTNTPrimed(World worldIn, double p_i1730_2_, double p_i1730_4_, double p_i1730_6_, EntityLivingBase p_i1730_8_)
    {
        this(worldIn);
        setPosition(p_i1730_2_, p_i1730_4_, p_i1730_6_);
        float f = (float)(Math.random() * Math.PI * 2.0D);
        motionX = -((float)Math.sin(f)) * 0.02F;
        motionY = 0.20000000298023224D;
        motionZ = -((float)Math.cos(f)) * 0.02F;
        fuse = 80;
        prevPosX = p_i1730_2_;
        prevPosY = p_i1730_4_;
        prevPosZ = p_i1730_6_;
        tntPlacedBy = p_i1730_8_;
    }

    protected void entityInit()
    {
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking()
    {
        return false;
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean canBeCollidedWith()
    {
        return !isDead;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        motionY -= 0.03999999910593033D;
        moveEntity(motionX, motionY, motionZ);
        motionX *= 0.9800000190734863D;
        motionY *= 0.9800000190734863D;
        motionZ *= 0.9800000190734863D;

        if (onGround)
        {
            motionX *= 0.699999988079071D;
            motionZ *= 0.699999988079071D;
            motionY *= -0.5D;
        }

        if (fuse-- <= 0)
        {
            setDead();

            if (!worldObj.isRemote)
            {
                explode();
            }
        }
        else
        {
            handleWaterMovement();
            worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY + 0.5D, posZ, 0.0D, 0.0D, 0.0D);
        }
    }

    private void explode()
    {
        float f = 4.0F;
        worldObj.createExplosion(this, posX, posY + (double)(height / 16.0F), posZ, f, true);
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        tagCompound.setByte("Fuse", (byte) fuse);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        fuse = tagCompund.getByte("Fuse");
    }

    /**
     * returns null or the entityliving it was placed or ignited by
     */
    public EntityLivingBase getTntPlacedBy()
    {
        return tntPlacedBy;
    }

    public float getEyeHeight()
    {
        return 0.0F;
    }
}
