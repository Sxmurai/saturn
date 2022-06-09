package net.minecraft.entity.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityBoat extends Entity
{
    /** true if no player in boat */
    private boolean isBoatEmpty;
    private double speedMultiplier;
    private int boatPosRotationIncrements;
    private double boatX;
    private double boatY;
    private double boatZ;
    private double boatYaw;
    private double boatPitch;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    public EntityBoat(World worldIn)
    {
        super(worldIn);
        isBoatEmpty = true;
        speedMultiplier = 0.07D;
        preventEntitySpawning = true;
        setSize(1.5F, 0.6F);
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking()
    {
        return false;
    }

    protected void entityInit()
    {
        dataWatcher.addObject(17, new Integer(0));
        dataWatcher.addObject(18, new Integer(1));
        dataWatcher.addObject(19, new Float(0.0F));
    }

    /**
     * Returns a boundingBox used to collide the entity with other entities and blocks. This enables the entity to be
     * pushable on contact, like boats or minecarts.
     */
    public AxisAlignedBB getCollisionBox(Entity entityIn)
    {
        return entityIn.getEntityBoundingBox();
    }

    /**
     * Returns the collision bounding box for this entity
     */
    public AxisAlignedBB getCollisionBoundingBox()
    {
        return getEntityBoundingBox();
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    public boolean canBePushed()
    {
        return true;
    }

    public EntityBoat(World worldIn, double p_i1705_2_, double p_i1705_4_, double p_i1705_6_)
    {
        this(worldIn);
        setPosition(p_i1705_2_, p_i1705_4_, p_i1705_6_);
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        prevPosX = p_i1705_2_;
        prevPosY = p_i1705_4_;
        prevPosZ = p_i1705_6_;
    }

    /**
     * Returns the Y offset from the entity's position for any entity riding this one.
     */
    public double getMountedYOffset()
    {
        return -0.3D;
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
        else if (!worldObj.isRemote && !isDead)
        {
            if (riddenByEntity != null && riddenByEntity == source.getEntity() && source instanceof EntityDamageSourceIndirect)
            {
                return false;
            }
            else
            {
                setForwardDirection(-getForwardDirection());
                setTimeSinceHit(10);
                setDamageTaken(getDamageTaken() + amount * 10.0F);
                setBeenAttacked();
                boolean flag = source.getEntity() instanceof EntityPlayer && ((EntityPlayer)source.getEntity()).capabilities.isCreativeMode;

                if (flag || getDamageTaken() > 40.0F)
                {
                    if (riddenByEntity != null)
                    {
                        riddenByEntity.mountEntity(this);
                    }

                    if (!flag && worldObj.getGameRules().getBoolean("doEntityDrops"))
                    {
                        dropItemWithOffset(Items.boat, 1, 0.0F);
                    }

                    setDead();
                }

                return true;
            }
        }
        else
        {
            return true;
        }
    }

    /**
     * Setups the entity to do the hurt animation. Only used by packets in multiplayer.
     */
    public void performHurtAnimation()
    {
        setForwardDirection(-getForwardDirection());
        setTimeSinceHit(10);
        setDamageTaken(getDamageTaken() * 11.0F);
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean canBeCollidedWith()
    {
        return !isDead;
    }

    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean p_180426_10_)
    {
        if (p_180426_10_ && riddenByEntity != null)
        {
            prevPosX = posX = x;
            prevPosY = posY = y;
            prevPosZ = posZ = z;
            rotationYaw = yaw;
            rotationPitch = pitch;
            boatPosRotationIncrements = 0;
            setPosition(x, y, z);
            motionX = velocityX = 0.0D;
            motionY = velocityY = 0.0D;
            motionZ = velocityZ = 0.0D;
        }
        else
        {
            if (isBoatEmpty)
            {
                boatPosRotationIncrements = posRotationIncrements + 5;
            }
            else
            {
                double d0 = x - posX;
                double d1 = y - posY;
                double d2 = z - posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (d3 <= 1.0D)
                {
                    return;
                }

                boatPosRotationIncrements = 3;
            }

            boatX = x;
            boatY = y;
            boatZ = z;
            boatYaw = yaw;
            boatPitch = pitch;
            motionX = velocityX;
            motionY = velocityY;
            motionZ = velocityZ;
        }
    }

    /**
     * Sets the velocity to the args. Args: x, y, z
     */
    public void setVelocity(double x, double y, double z)
    {
        velocityX = motionX = x;
        velocityY = motionY = y;
        velocityZ = motionZ = z;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (getTimeSinceHit() > 0)
        {
            setTimeSinceHit(getTimeSinceHit() - 1);
        }

        if (getDamageTaken() > 0.0F)
        {
            setDamageTaken(getDamageTaken() - 1.0F);
        }

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        int i = 5;
        double d0 = 0.0D;

        for (int j = 0; j < i; ++j)
        {
            double d1 = getEntityBoundingBox().minY + (getEntityBoundingBox().maxY - getEntityBoundingBox().minY) * (double)(j + 0) / (double)i - 0.125D;
            double d3 = getEntityBoundingBox().minY + (getEntityBoundingBox().maxY - getEntityBoundingBox().minY) * (double)(j + 1) / (double)i - 0.125D;
            AxisAlignedBB axisalignedbb = new AxisAlignedBB(getEntityBoundingBox().minX, d1, getEntityBoundingBox().minZ, getEntityBoundingBox().maxX, d3, getEntityBoundingBox().maxZ);

            if (worldObj.isAABBInMaterial(axisalignedbb, Material.water))
            {
                d0 += 1.0D / (double)i;
            }
        }

        double d9 = Math.sqrt(motionX * motionX + motionZ * motionZ);

        if (d9 > 0.2975D)
        {
            double d2 = Math.cos((double) rotationYaw * Math.PI / 180.0D);
            double d4 = Math.sin((double) rotationYaw * Math.PI / 180.0D);

            for (int k = 0; (double)k < 1.0D + d9 * 60.0D; ++k)
            {
                double d5 = rand.nextFloat() * 2.0F - 1.0F;
                double d6 = (double)(rand.nextInt(2) * 2 - 1) * 0.7D;

                if (rand.nextBoolean())
                {
                    double d7 = posX - d2 * d5 * 0.8D + d4 * d6;
                    double d8 = posZ - d4 * d5 * 0.8D - d2 * d6;
                    worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, d7, posY - 0.125D, d8, motionX, motionY, motionZ);
                }
                else
                {
                    double d24 = posX + d2 + d4 * d5 * 0.7D;
                    double d25 = posZ + d4 - d2 * d5 * 0.7D;
                    worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, d24, posY - 0.125D, d25, motionX, motionY, motionZ);
                }
            }
        }

        if (worldObj.isRemote && isBoatEmpty)
        {
            if (boatPosRotationIncrements > 0)
            {
                double d12 = posX + (boatX - posX) / (double) boatPosRotationIncrements;
                double d16 = posY + (boatY - posY) / (double) boatPosRotationIncrements;
                double d19 = posZ + (boatZ - posZ) / (double) boatPosRotationIncrements;
                double d22 = MathHelper.wrapAngleTo180_double(boatYaw - (double) rotationYaw);
                rotationYaw = (float)((double) rotationYaw + d22 / (double) boatPosRotationIncrements);
                rotationPitch = (float)((double) rotationPitch + (boatPitch - (double) rotationPitch) / (double) boatPosRotationIncrements);
                --boatPosRotationIncrements;
                setPosition(d12, d16, d19);
                setRotation(rotationYaw, rotationPitch);
            }
            else
            {
                double d13 = posX + motionX;
                double d17 = posY + motionY;
                double d20 = posZ + motionZ;
                setPosition(d13, d17, d20);

                if (onGround)
                {
                    motionX *= 0.5D;
                    motionY *= 0.5D;
                    motionZ *= 0.5D;
                }

                motionX *= 0.9900000095367432D;
                motionY *= 0.949999988079071D;
                motionZ *= 0.9900000095367432D;
            }
        }
        else
        {
            if (d0 < 1.0D)
            {
                double d10 = d0 * 2.0D - 1.0D;
                motionY += 0.03999999910593033D * d10;
            }
            else
            {
                if (motionY < 0.0D)
                {
                    motionY /= 2.0D;
                }

                motionY += 0.007000000216066837D;
            }

            if (riddenByEntity instanceof EntityLivingBase)
            {
                EntityLivingBase entitylivingbase = (EntityLivingBase) riddenByEntity;
                float f = riddenByEntity.rotationYaw + -entitylivingbase.moveStrafing * 90.0F;
                motionX += -Math.sin(f * (float)Math.PI / 180.0F) * speedMultiplier * (double)entitylivingbase.moveForward * 0.05000000074505806D;
                motionZ += Math.cos(f * (float)Math.PI / 180.0F) * speedMultiplier * (double)entitylivingbase.moveForward * 0.05000000074505806D;
            }

            double d11 = Math.sqrt(motionX * motionX + motionZ * motionZ);

            if (d11 > 0.35D)
            {
                double d14 = 0.35D / d11;
                motionX *= d14;
                motionZ *= d14;
                d11 = 0.35D;
            }

            if (d11 > d9 && speedMultiplier < 0.35D)
            {
                speedMultiplier += (0.35D - speedMultiplier) / 35.0D;

                if (speedMultiplier > 0.35D)
                {
                    speedMultiplier = 0.35D;
                }
            }
            else
            {
                speedMultiplier -= (speedMultiplier - 0.07D) / 35.0D;

                if (speedMultiplier < 0.07D)
                {
                    speedMultiplier = 0.07D;
                }
            }

            for (int i1 = 0; i1 < 4; ++i1)
            {
                int l1 = MathHelper.floor_double(posX + ((double)(i1 % 2) - 0.5D) * 0.8D);
                int i2 = MathHelper.floor_double(posZ + ((double)(i1 / 2) - 0.5D) * 0.8D);

                for (int j2 = 0; j2 < 2; ++j2)
                {
                    int l = MathHelper.floor_double(posY) + j2;
                    BlockPos blockpos = new BlockPos(l1, l, i2);
                    Block block = worldObj.getBlockState(blockpos).getBlock();

                    if (block == Blocks.snow_layer)
                    {
                        worldObj.setBlockToAir(blockpos);
                        isCollidedHorizontally = false;
                    }
                    else if (block == Blocks.waterlily)
                    {
                        worldObj.destroyBlock(blockpos, true);
                        isCollidedHorizontally = false;
                    }
                }
            }

            if (onGround)
            {
                motionX *= 0.5D;
                motionY *= 0.5D;
                motionZ *= 0.5D;
            }

            moveEntity(motionX, motionY, motionZ);

            if (isCollidedHorizontally && d9 > 0.2975D)
            {
                if (!worldObj.isRemote && !isDead)
                {
                    setDead();

                    if (worldObj.getGameRules().getBoolean("doEntityDrops"))
                    {
                        for (int j1 = 0; j1 < 3; ++j1)
                        {
                            dropItemWithOffset(Item.getItemFromBlock(Blocks.planks), 1, 0.0F);
                        }

                        for (int k1 = 0; k1 < 2; ++k1)
                        {
                            dropItemWithOffset(Items.stick, 1, 0.0F);
                        }
                    }
                }
            }
            else
            {
                motionX *= 0.9900000095367432D;
                motionY *= 0.949999988079071D;
                motionZ *= 0.9900000095367432D;
            }

            rotationPitch = 0.0F;
            double d15 = rotationYaw;
            double d18 = prevPosX - posX;
            double d21 = prevPosZ - posZ;

            if (d18 * d18 + d21 * d21 > 0.001D)
            {
                d15 = (float)(MathHelper.func_181159_b(d21, d18) * 180.0D / Math.PI);
            }

            double d23 = MathHelper.wrapAngleTo180_double(d15 - (double) rotationYaw);

            if (d23 > 20.0D)
            {
                d23 = 20.0D;
            }

            if (d23 < -20.0D)
            {
                d23 = -20.0D;
            }

            rotationYaw = (float)((double) rotationYaw + d23);
            setRotation(rotationYaw, rotationPitch);

            if (!worldObj.isRemote)
            {
                List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D));

                if (list != null && !list.isEmpty())
                {
                    for (int k2 = 0; k2 < list.size(); ++k2)
                    {
                        Entity entity = list.get(k2);

                        if (entity != riddenByEntity && entity.canBePushed() && entity instanceof EntityBoat)
                        {
                            entity.applyEntityCollision(this);
                        }
                    }
                }

                if (riddenByEntity != null && riddenByEntity.isDead)
                {
                    riddenByEntity = null;
                }
            }
        }
    }

    public void updateRiderPosition()
    {
        if (riddenByEntity != null)
        {
            double d0 = Math.cos((double) rotationYaw * Math.PI / 180.0D) * 0.4D;
            double d1 = Math.sin((double) rotationYaw * Math.PI / 180.0D) * 0.4D;
            riddenByEntity.setPosition(posX + d0, posY + getMountedYOffset() + riddenByEntity.getYOffset(), posZ + d1);
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound tagCompound)
    {
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound tagCompund)
    {
    }

    /**
     * First layer of player interaction
     */
    public boolean interactFirst(EntityPlayer playerIn)
    {
        if (riddenByEntity != null && riddenByEntity instanceof EntityPlayer && riddenByEntity != playerIn)
        {
            return true;
        }
        else
        {
            if (!worldObj.isRemote)
            {
                playerIn.mountEntity(this);
            }

            return true;
        }
    }

    protected void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos)
    {
        if (onGroundIn)
        {
            if (fallDistance > 3.0F)
            {
                fall(fallDistance, 1.0F);

                if (!worldObj.isRemote && !isDead)
                {
                    setDead();

                    if (worldObj.getGameRules().getBoolean("doEntityDrops"))
                    {
                        for (int i = 0; i < 3; ++i)
                        {
                            dropItemWithOffset(Item.getItemFromBlock(Blocks.planks), 1, 0.0F);
                        }

                        for (int j = 0; j < 2; ++j)
                        {
                            dropItemWithOffset(Items.stick, 1, 0.0F);
                        }
                    }
                }

                fallDistance = 0.0F;
            }
        }
        else if (worldObj.getBlockState((new BlockPos(this)).down()).getBlock().getMaterial() != Material.water && y < 0.0D)
        {
            fallDistance = (float)((double) fallDistance - y);
        }
    }

    /**
     * Sets the damage taken from the last hit.
     */
    public void setDamageTaken(float p_70266_1_)
    {
        dataWatcher.updateObject(19, Float.valueOf(p_70266_1_));
    }

    /**
     * Gets the damage taken from the last hit.
     */
    public float getDamageTaken()
    {
        return dataWatcher.getWatchableObjectFloat(19);
    }

    /**
     * Sets the time to count down from since the last time entity was hit.
     */
    public void setTimeSinceHit(int p_70265_1_)
    {
        dataWatcher.updateObject(17, Integer.valueOf(p_70265_1_));
    }

    /**
     * Gets the time since the last hit.
     */
    public int getTimeSinceHit()
    {
        return dataWatcher.getWatchableObjectInt(17);
    }

    /**
     * Sets the forward direction of the entity.
     */
    public void setForwardDirection(int p_70269_1_)
    {
        dataWatcher.updateObject(18, Integer.valueOf(p_70269_1_));
    }

    /**
     * Gets the forward direction of the entity.
     */
    public int getForwardDirection()
    {
        return dataWatcher.getWatchableObjectInt(18);
    }

    /**
     * true if no player in boat
     */
    public void setIsBoatEmpty(boolean p_70270_1_)
    {
        isBoatEmpty = p_70270_1_;
    }
}
