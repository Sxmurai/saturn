package net.minecraft.entity.projectile;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class EntityFireball extends Entity
{
    private int xTile = -1;
    private int yTile = -1;
    private int zTile = -1;
    private Block inTile;
    private boolean inGround;
    public EntityLivingBase shootingEntity;
    private int ticksAlive;
    private int ticksInAir;
    public double accelerationX;
    public double accelerationY;
    public double accelerationZ;

    public EntityFireball(World worldIn)
    {
        super(worldIn);
        setSize(1.0F, 1.0F);
    }

    protected void entityInit()
    {
    }

    /**
     * Checks if the entity is in range to render by using the past in distance and comparing it to its average edge
     * length * 64 * renderDistanceWeight Args: distance
     */
    public boolean isInRangeToRenderDist(double distance)
    {
        double d0 = getEntityBoundingBox().getAverageEdgeLength() * 4.0D;

        if (Double.isNaN(d0))
        {
            d0 = 4.0D;
        }

        d0 = d0 * 64.0D;
        return distance < d0 * d0;
    }

    public EntityFireball(World worldIn, double x, double y, double z, double accelX, double accelY, double accelZ)
    {
        super(worldIn);
        setSize(1.0F, 1.0F);
        setLocationAndAngles(x, y, z, rotationYaw, rotationPitch);
        setPosition(x, y, z);
        double d0 = MathHelper.sqrt_double(accelX * accelX + accelY * accelY + accelZ * accelZ);
        accelerationX = accelX / d0 * 0.1D;
        accelerationY = accelY / d0 * 0.1D;
        accelerationZ = accelZ / d0 * 0.1D;
    }

    public EntityFireball(World worldIn, EntityLivingBase shooter, double accelX, double accelY, double accelZ)
    {
        super(worldIn);
        shootingEntity = shooter;
        setSize(1.0F, 1.0F);
        setLocationAndAngles(shooter.posX, shooter.posY, shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);
        setPosition(posX, posY, posZ);
        motionX = motionY = motionZ = 0.0D;
        accelX = accelX + rand.nextGaussian() * 0.4D;
        accelY = accelY + rand.nextGaussian() * 0.4D;
        accelZ = accelZ + rand.nextGaussian() * 0.4D;
        double d0 = MathHelper.sqrt_double(accelX * accelX + accelY * accelY + accelZ * accelZ);
        accelerationX = accelX / d0 * 0.1D;
        accelerationY = accelY / d0 * 0.1D;
        accelerationZ = accelZ / d0 * 0.1D;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        if (worldObj.isRemote || (shootingEntity == null || !shootingEntity.isDead) && worldObj.isBlockLoaded(new BlockPos(this)))
        {
            super.onUpdate();
            setFire(1);

            if (inGround)
            {
                if (worldObj.getBlockState(new BlockPos(xTile, yTile, zTile)).getBlock() == inTile)
                {
                    ++ticksAlive;

                    if (ticksAlive == 600)
                    {
                        setDead();
                    }

                    return;
                }

                inGround = false;
                motionX *= rand.nextFloat() * 0.2F;
                motionY *= rand.nextFloat() * 0.2F;
                motionZ *= rand.nextFloat() * 0.2F;
                ticksAlive = 0;
                ticksInAir = 0;
            }
            else
            {
                ++ticksInAir;
            }

            Vec3 vec3 = new Vec3(posX, posY, posZ);
            Vec3 vec31 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
            MovingObjectPosition movingobjectposition = worldObj.rayTraceBlocks(vec3, vec31);
            vec3 = new Vec3(posX, posY, posZ);
            vec31 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);

            if (movingobjectposition != null)
            {
                vec31 = new Vec3(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            }

            Entity entity = null;
            List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;

            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity1 = list.get(i);

                if (entity1.canBeCollidedWith() && (!entity1.isEntityEqual(shootingEntity) || ticksInAir >= 25))
                {
                    float f = 0.3F;
                    AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f, f, f);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3, vec31);

                    if (movingobjectposition1 != null)
                    {
                        double d1 = vec3.squareDistanceTo(movingobjectposition1.hitVec);

                        if (d1 < d0 || d0 == 0.0D)
                        {
                            entity = entity1;
                            d0 = d1;
                        }
                    }
                }
            }

            if (entity != null)
            {
                movingobjectposition = new MovingObjectPosition(entity);
            }

            if (movingobjectposition != null)
            {
                onImpact(movingobjectposition);
            }

            posX += motionX;
            posY += motionY;
            posZ += motionZ;
            float f1 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
            rotationYaw = (float)(MathHelper.func_181159_b(motionZ, motionX) * 180.0D / Math.PI) + 90.0F;

            for (rotationPitch = (float)(MathHelper.func_181159_b(f1, motionY) * 180.0D / Math.PI) - 90.0F; rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F)
            {
            }

            while (rotationPitch - prevRotationPitch >= 180.0F)
            {
                prevRotationPitch += 360.0F;
            }

            while (rotationYaw - prevRotationYaw < -180.0F)
            {
                prevRotationYaw -= 360.0F;
            }

            while (rotationYaw - prevRotationYaw >= 180.0F)
            {
                prevRotationYaw += 360.0F;
            }

            rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
            rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
            float f2 = getMotionFactor();

            if (isInWater())
            {
                for (int j = 0; j < 4; ++j)
                {
                    float f3 = 0.25F;
                    worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * (double)f3, posY - motionY * (double)f3, posZ - motionZ * (double)f3, motionX, motionY, motionZ);
                }

                f2 = 0.8F;
            }

            motionX += accelerationX;
            motionY += accelerationY;
            motionZ += accelerationZ;
            motionX *= f2;
            motionY *= f2;
            motionZ *= f2;
            worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY + 0.5D, posZ, 0.0D, 0.0D, 0.0D);
            setPosition(posX, posY, posZ);
        }
        else
        {
            setDead();
        }
    }

    /**
     * Return the motion factor for this projectile. The factor is multiplied by the original motion.
     */
    protected float getMotionFactor()
    {
        return 0.95F;
    }

    /**
     * Called when this EntityFireball hits a block or entity.
     */
    protected abstract void onImpact(MovingObjectPosition movingObject);

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        tagCompound.setShort("xTile", (short) xTile);
        tagCompound.setShort("yTile", (short) yTile);
        tagCompound.setShort("zTile", (short) zTile);
        ResourceLocation resourcelocation = Block.blockRegistry.getNameForObject(inTile);
        tagCompound.setString("inTile", resourcelocation == null ? "" : resourcelocation.toString());
        tagCompound.setByte("inGround", (byte)(inGround ? 1 : 0));
        tagCompound.setTag("direction", newDoubleNBTList(motionX, motionY, motionZ));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        xTile = tagCompund.getShort("xTile");
        yTile = tagCompund.getShort("yTile");
        zTile = tagCompund.getShort("zTile");

        if (tagCompund.hasKey("inTile", 8))
        {
            inTile = Block.getBlockFromName(tagCompund.getString("inTile"));
        }
        else
        {
            inTile = Block.getBlockById(tagCompund.getByte("inTile") & 255);
        }

        inGround = tagCompund.getByte("inGround") == 1;

        if (tagCompund.hasKey("direction", 9))
        {
            NBTTagList nbttaglist = tagCompund.getTagList("direction", 6);
            motionX = nbttaglist.getDoubleAt(0);
            motionY = nbttaglist.getDoubleAt(1);
            motionZ = nbttaglist.getDoubleAt(2);
        }
        else
        {
            setDead();
        }
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean canBeCollidedWith()
    {
        return true;
    }

    public float getCollisionBorderSize()
    {
        return 1.0F;
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

            if (source.getEntity() != null)
            {
                Vec3 vec3 = source.getEntity().getLookVec();

                if (vec3 != null)
                {
                    motionX = vec3.xCoord;
                    motionY = vec3.yCoord;
                    motionZ = vec3.zCoord;
                    accelerationX = motionX * 0.1D;
                    accelerationY = motionY * 0.1D;
                    accelerationZ = motionZ * 0.1D;
                }

                if (source.getEntity() instanceof EntityLivingBase)
                {
                    shootingEntity = (EntityLivingBase)source.getEntity();
                }

                return true;
            }
            else
            {
                return false;
            }
        }
    }

    /**
     * Gets how bright this entity is.
     */
    public float getBrightness(float partialTicks)
    {
        return 1.0F;
    }

    public int getBrightnessForRender(float partialTicks)
    {
        return 15728880;
    }
}
