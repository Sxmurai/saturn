package net.minecraft.entity.item;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityXPOrb extends Entity
{
    /**
     * A constantly increasing value that RenderXPOrb uses to control the colour shifting (Green / yellow)
     */
    public int xpColor;

    /** The age of the XP orb in ticks. */
    public int xpOrbAge;
    public int delayBeforeCanPickup;

    /** The health of this XP orb. */
    private int xpOrbHealth = 5;

    /** This is how much XP this orb has. */
    private int xpValue;

    /** The closest EntityPlayer to this orb. */
    private EntityPlayer closestPlayer;

    /** Threshold color for tracking players */
    private int xpTargetColor;

    public EntityXPOrb(World worldIn, double x, double y, double z, int expValue)
    {
        super(worldIn);
        setSize(0.5F, 0.5F);
        setPosition(x, y, z);
        rotationYaw = (float)(Math.random() * 360.0D);
        motionX = (float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D) * 2.0F;
        motionY = (float)(Math.random() * 0.2D) * 2.0F;
        motionZ = (float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D) * 2.0F;
        xpValue = expValue;
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking()
    {
        return false;
    }

    public EntityXPOrb(World worldIn)
    {
        super(worldIn);
        setSize(0.25F, 0.25F);
    }

    protected void entityInit()
    {
    }

    public int getBrightnessForRender(float partialTicks)
    {
        float f = 0.5F;
        f = MathHelper.clamp_float(f, 0.0F, 1.0F);
        int i = super.getBrightnessForRender(partialTicks);
        int j = i & 255;
        int k = i >> 16 & 255;
        j = j + (int)(f * 15.0F * 16.0F);

        if (j > 240)
        {
            j = 240;
        }

        return j | k << 16;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (delayBeforeCanPickup > 0)
        {
            --delayBeforeCanPickup;
        }

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        motionY -= 0.029999999329447746D;

        if (worldObj.getBlockState(new BlockPos(this)).getBlock().getMaterial() == Material.lava)
        {
            motionY = 0.20000000298023224D;
            motionX = (rand.nextFloat() - rand.nextFloat()) * 0.2F;
            motionZ = (rand.nextFloat() - rand.nextFloat()) * 0.2F;
            playSound("random.fizz", 0.4F, 2.0F + rand.nextFloat() * 0.4F);
        }

        pushOutOfBlocks(posX, (getEntityBoundingBox().minY + getEntityBoundingBox().maxY) / 2.0D, posZ);
        double d0 = 8.0D;

        if (xpTargetColor < xpColor - 20 + getEntityId() % 100)
        {
            if (closestPlayer == null || closestPlayer.getDistanceSqToEntity(this) > d0 * d0)
            {
                closestPlayer = worldObj.getClosestPlayerToEntity(this, d0);
            }

            xpTargetColor = xpColor;
        }

        if (closestPlayer != null && closestPlayer.isSpectator())
        {
            closestPlayer = null;
        }

        if (closestPlayer != null)
        {
            double d1 = (closestPlayer.posX - posX) / d0;
            double d2 = (closestPlayer.posY + (double) closestPlayer.getEyeHeight() - posY) / d0;
            double d3 = (closestPlayer.posZ - posZ) / d0;
            double d4 = Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
            double d5 = 1.0D - d4;

            if (d5 > 0.0D)
            {
                d5 = d5 * d5;
                motionX += d1 / d4 * d5 * 0.1D;
                motionY += d2 / d4 * d5 * 0.1D;
                motionZ += d3 / d4 * d5 * 0.1D;
            }
        }

        moveEntity(motionX, motionY, motionZ);
        float f = 0.98F;

        if (onGround)
        {
            f = worldObj.getBlockState(new BlockPos(MathHelper.floor_double(posX), MathHelper.floor_double(getEntityBoundingBox().minY) - 1, MathHelper.floor_double(posZ))).getBlock().slipperiness * 0.98F;
        }

        motionX *= f;
        motionY *= 0.9800000190734863D;
        motionZ *= f;

        if (onGround)
        {
            motionY *= -0.8999999761581421D;
        }

        ++xpColor;
        ++xpOrbAge;

        if (xpOrbAge >= 6000)
        {
            setDead();
        }
    }

    /**
     * Returns if this entity is in water and will end up adding the waters velocity to the entity
     */
    public boolean handleWaterMovement()
    {
        return worldObj.handleMaterialAcceleration(getEntityBoundingBox(), Material.water, this);
    }

    /**
     * Will deal the specified amount of damage to the entity if the entity isn't immune to fire damage. Args:
     * amountDamage
     */
    protected void dealFireDamage(int amount)
    {
        attackEntityFrom(DamageSource.inFire, (float)amount);
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (isEntityInvulnerable(source))
        {
            return false;
        }
        else
        {
            setBeenAttacked();
            xpOrbHealth = (int)((float) xpOrbHealth - amount);

            if (xpOrbHealth <= 0)
            {
                setDead();
            }

            return false;
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        tagCompound.setShort("Health", (byte) xpOrbHealth);
        tagCompound.setShort("Age", (short) xpOrbAge);
        tagCompound.setShort("Value", (short) xpValue);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        xpOrbHealth = tagCompund.getShort("Health") & 255;
        xpOrbAge = tagCompund.getShort("Age");
        xpValue = tagCompund.getShort("Value");
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    public void onCollideWithPlayer(EntityPlayer entityIn)
    {
        if (!worldObj.isRemote)
        {
            if (delayBeforeCanPickup == 0 && entityIn.xpCooldown == 0)
            {
                entityIn.xpCooldown = 2;
                worldObj.playSoundAtEntity(entityIn, "random.orb", 0.1F, 0.5F * ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.8F));
                entityIn.onItemPickup(this, 1);
                entityIn.addExperience(xpValue);
                setDead();
            }
        }
    }

    /**
     * Returns the XP value of this XP orb.
     */
    public int getXpValue()
    {
        return xpValue;
    }

    /**
     * Returns a number from 1 to 10 based on how much XP this orb is worth. This is used by RenderXPOrb to determine
     * what texture to use.
     */
    public int getTextureByXP()
    {
        return xpValue >= 2477 ? 10 : (xpValue >= 1237 ? 9 : (xpValue >= 617 ? 8 : (xpValue >= 307 ? 7 : (xpValue >= 149 ? 6 : (xpValue >= 73 ? 5 : (xpValue >= 37 ? 4 : (xpValue >= 17 ? 3 : (xpValue >= 7 ? 2 : (xpValue >= 3 ? 1 : 0)))))))));
    }

    /**
     * Get a fragment of the maximum experience points value for the supplied value of experience points value.
     */
    public static int getXPSplit(int expValue)
    {
        return expValue >= 2477 ? 2477 : (expValue >= 1237 ? 1237 : (expValue >= 617 ? 617 : (expValue >= 307 ? 307 : (expValue >= 149 ? 149 : (expValue >= 73 ? 73 : (expValue >= 37 ? 37 : (expValue >= 17 ? 17 : (expValue >= 7 ? 7 : (expValue >= 3 ? 3 : 1)))))))));
    }

    /**
     * If returns false, the item will not inflict any damage against entities.
     */
    public boolean canAttackWithItem()
    {
        return false;
    }
}
