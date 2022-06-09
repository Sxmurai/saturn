package net.minecraft.entity.projectile;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityArrow extends Entity implements IProjectile
{
    private int xTile = -1;
    private int yTile = -1;
    private int zTile = -1;
    private Block inTile;
    private int inData;
    private boolean inGround;

    /** 1 if the player can pick up the arrow */
    public int canBePickedUp;

    /** Seems to be some sort of timer for animating an arrow. */
    public int arrowShake;

    /** The owner of this arrow. */
    public Entity shootingEntity;
    private int ticksInGround;
    private int ticksInAir;
    private double damage = 2.0D;

    /** The amount of knockback an arrow applies when it hits a mob. */
    private int knockbackStrength;

    public EntityArrow(World worldIn)
    {
        super(worldIn);
        renderDistanceWeight = 10.0D;
        setSize(0.5F, 0.5F);
    }

    public EntityArrow(World worldIn, double x, double y, double z)
    {
        super(worldIn);
        renderDistanceWeight = 10.0D;
        setSize(0.5F, 0.5F);
        setPosition(x, y, z);
    }

    public EntityArrow(World worldIn, EntityLivingBase shooter, EntityLivingBase p_i1755_3_, float p_i1755_4_, float p_i1755_5_)
    {
        super(worldIn);
        renderDistanceWeight = 10.0D;
        shootingEntity = shooter;

        if (shooter instanceof EntityPlayer)
        {
            canBePickedUp = 1;
        }

        posY = shooter.posY + (double)shooter.getEyeHeight() - 0.10000000149011612D;
        double d0 = p_i1755_3_.posX - shooter.posX;
        double d1 = p_i1755_3_.getEntityBoundingBox().minY + (double)(p_i1755_3_.height / 3.0F) - posY;
        double d2 = p_i1755_3_.posZ - shooter.posZ;
        double d3 = MathHelper.sqrt_double(d0 * d0 + d2 * d2);

        if (d3 >= 1.0E-7D)
        {
            float f = (float)(MathHelper.func_181159_b(d2, d0) * 180.0D / Math.PI) - 90.0F;
            float f1 = (float)(-(MathHelper.func_181159_b(d1, d3) * 180.0D / Math.PI));
            double d4 = d0 / d3;
            double d5 = d2 / d3;
            setLocationAndAngles(shooter.posX + d4, posY, shooter.posZ + d5, f, f1);
            float f2 = (float)(d3 * 0.20000000298023224D);
            setThrowableHeading(d0, d1 + (double)f2, d2, p_i1755_4_, p_i1755_5_);
        }
    }

    public EntityArrow(World worldIn, EntityLivingBase shooter, float velocity)
    {
        super(worldIn);
        renderDistanceWeight = 10.0D;
        shootingEntity = shooter;

        if (shooter instanceof EntityPlayer)
        {
            canBePickedUp = 1;
        }

        setSize(0.5F, 0.5F);
        setLocationAndAngles(shooter.posX, shooter.posY + (double)shooter.getEyeHeight(), shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);
        posX -= MathHelper.cos(rotationYaw / 180.0F * (float)Math.PI) * 0.16F;
        posY -= 0.10000000149011612D;
        posZ -= MathHelper.sin(rotationYaw / 180.0F * (float)Math.PI) * 0.16F;
        setPosition(posX, posY, posZ);
        motionX = -MathHelper.sin(rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float)Math.PI);
        motionZ = MathHelper.cos(rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float)Math.PI);
        motionY = -MathHelper.sin(rotationPitch / 180.0F * (float)Math.PI);
        setThrowableHeading(motionX, motionY, motionZ, velocity * 1.5F, 1.0F);
    }

    protected void entityInit()
    {
        dataWatcher.addObject(16, Byte.valueOf((byte)0));
    }

    /**
     * Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction.
     */
    public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy)
    {
        float f = MathHelper.sqrt_double(x * x + y * y + z * z);
        x = x / (double)f;
        y = y / (double)f;
        z = z / (double)f;
        x = x + rand.nextGaussian() * (double)(rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double)inaccuracy;
        y = y + rand.nextGaussian() * (double)(rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double)inaccuracy;
        z = z + rand.nextGaussian() * (double)(rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double)inaccuracy;
        x = x * (double)velocity;
        y = y * (double)velocity;
        z = z * (double)velocity;
        motionX = x;
        motionY = y;
        motionZ = z;
        float f1 = MathHelper.sqrt_double(x * x + z * z);
        prevRotationYaw = rotationYaw = (float)(MathHelper.func_181159_b(x, z) * 180.0D / Math.PI);
        prevRotationPitch = rotationPitch = (float)(MathHelper.func_181159_b(y, f1) * 180.0D / Math.PI);
        ticksInGround = 0;
    }

    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean p_180426_10_)
    {
        setPosition(x, y, z);
        setRotation(yaw, pitch);
    }

    /**
     * Sets the velocity to the args. Args: x, y, z
     */
    public void setVelocity(double x, double y, double z)
    {
        motionX = x;
        motionY = y;
        motionZ = z;

        if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(x * x + z * z);
            prevRotationYaw = rotationYaw = (float)(MathHelper.func_181159_b(x, z) * 180.0D / Math.PI);
            prevRotationPitch = rotationPitch = (float)(MathHelper.func_181159_b(y, f) * 180.0D / Math.PI);
            prevRotationPitch = rotationPitch;
            prevRotationYaw = rotationYaw;
            setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
            ticksInGround = 0;
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
            prevRotationYaw = rotationYaw = (float)(MathHelper.func_181159_b(motionX, motionZ) * 180.0D / Math.PI);
            prevRotationPitch = rotationPitch = (float)(MathHelper.func_181159_b(motionY, f) * 180.0D / Math.PI);
        }

        BlockPos blockpos = new BlockPos(xTile, yTile, zTile);
        IBlockState iblockstate = worldObj.getBlockState(blockpos);
        Block block = iblockstate.getBlock();

        if (block.getMaterial() != Material.air)
        {
            block.setBlockBoundsBasedOnState(worldObj, blockpos);
            AxisAlignedBB axisalignedbb = block.getCollisionBoundingBox(worldObj, blockpos, iblockstate);

            if (axisalignedbb != null && axisalignedbb.isVecInside(new Vec3(posX, posY, posZ)))
            {
                inGround = true;
            }
        }

        if (arrowShake > 0)
        {
            --arrowShake;
        }

        if (inGround)
        {
            int j = block.getMetaFromState(iblockstate);

            if (block == inTile && j == inData)
            {
                ++ticksInGround;

                if (ticksInGround >= 1200)
                {
                    setDead();
                }
            }
            else
            {
                inGround = false;
                motionX *= rand.nextFloat() * 0.2F;
                motionY *= rand.nextFloat() * 0.2F;
                motionZ *= rand.nextFloat() * 0.2F;
                ticksInGround = 0;
                ticksInAir = 0;
            }
        }
        else
        {
            ++ticksInAir;
            Vec3 vec31 = new Vec3(posX, posY, posZ);
            Vec3 vec3 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
            MovingObjectPosition movingobjectposition = worldObj.rayTraceBlocks(vec31, vec3, false, true, false);
            vec31 = new Vec3(posX, posY, posZ);
            vec3 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);

            if (movingobjectposition != null)
            {
                vec3 = new Vec3(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            }

            Entity entity = null;
            List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;

            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity1 = list.get(i);

                if (entity1.canBeCollidedWith() && (entity1 != shootingEntity || ticksInAir >= 5))
                {
                    float f1 = 0.3F;
                    AxisAlignedBB axisalignedbb1 = entity1.getEntityBoundingBox().expand(f1, f1, f1);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept(vec31, vec3);

                    if (movingobjectposition1 != null)
                    {
                        double d1 = vec31.squareDistanceTo(movingobjectposition1.hitVec);

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

            if (movingobjectposition != null && movingobjectposition.entityHit != null && movingobjectposition.entityHit instanceof EntityPlayer)
            {
                EntityPlayer entityplayer = (EntityPlayer)movingobjectposition.entityHit;

                if (entityplayer.capabilities.disableDamage || shootingEntity instanceof EntityPlayer && !((EntityPlayer) shootingEntity).canAttackPlayer(entityplayer))
                {
                    movingobjectposition = null;
                }
            }

            if (movingobjectposition != null)
            {
                if (movingobjectposition.entityHit != null)
                {
                    float f2 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
                    int l = MathHelper.ceiling_double_int((double)f2 * damage);

                    if (getIsCritical())
                    {
                        l += rand.nextInt(l / 2 + 2);
                    }

                    DamageSource damagesource;

                    if (shootingEntity == null)
                    {
                        damagesource = DamageSource.causeArrowDamage(this, this);
                    }
                    else
                    {
                        damagesource = DamageSource.causeArrowDamage(this, shootingEntity);
                    }

                    if (isBurning() && !(movingobjectposition.entityHit instanceof EntityEnderman))
                    {
                        movingobjectposition.entityHit.setFire(5);
                    }

                    if (movingobjectposition.entityHit.attackEntityFrom(damagesource, (float)l))
                    {
                        if (movingobjectposition.entityHit instanceof EntityLivingBase)
                        {
                            EntityLivingBase entitylivingbase = (EntityLivingBase)movingobjectposition.entityHit;

                            if (!worldObj.isRemote)
                            {
                                entitylivingbase.setArrowCountInEntity(entitylivingbase.getArrowCountInEntity() + 1);
                            }

                            if (knockbackStrength > 0)
                            {
                                float f7 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);

                                if (f7 > 0.0F)
                                {
                                    movingobjectposition.entityHit.addVelocity(motionX * (double) knockbackStrength * 0.6000000238418579D / (double)f7, 0.1D, motionZ * (double) knockbackStrength * 0.6000000238418579D / (double)f7);
                                }
                            }

                            if (shootingEntity instanceof EntityLivingBase)
                            {
                                EnchantmentHelper.applyThornEnchantments(entitylivingbase, shootingEntity);
                                EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase) shootingEntity, entitylivingbase);
                            }

                            if (shootingEntity != null && movingobjectposition.entityHit != shootingEntity && movingobjectposition.entityHit instanceof EntityPlayer && shootingEntity instanceof EntityPlayerMP)
                            {
                                ((EntityPlayerMP) shootingEntity).playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(6, 0.0F));
                            }
                        }

                        playSound("random.bowhit", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));

                        if (!(movingobjectposition.entityHit instanceof EntityEnderman))
                        {
                            setDead();
                        }
                    }
                    else
                    {
                        motionX *= -0.10000000149011612D;
                        motionY *= -0.10000000149011612D;
                        motionZ *= -0.10000000149011612D;
                        rotationYaw += 180.0F;
                        prevRotationYaw += 180.0F;
                        ticksInAir = 0;
                    }
                }
                else
                {
                    BlockPos blockpos1 = movingobjectposition.getBlockPos();
                    xTile = blockpos1.getX();
                    yTile = blockpos1.getY();
                    zTile = blockpos1.getZ();
                    IBlockState iblockstate1 = worldObj.getBlockState(blockpos1);
                    inTile = iblockstate1.getBlock();
                    inData = inTile.getMetaFromState(iblockstate1);
                    motionX = (float)(movingobjectposition.hitVec.xCoord - posX);
                    motionY = (float)(movingobjectposition.hitVec.yCoord - posY);
                    motionZ = (float)(movingobjectposition.hitVec.zCoord - posZ);
                    float f5 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
                    posX -= motionX / (double)f5 * 0.05000000074505806D;
                    posY -= motionY / (double)f5 * 0.05000000074505806D;
                    posZ -= motionZ / (double)f5 * 0.05000000074505806D;
                    playSound("random.bowhit", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
                    inGround = true;
                    arrowShake = 7;
                    setIsCritical(false);

                    if (inTile.getMaterial() != Material.air)
                    {
                        inTile.onEntityCollidedWithBlock(worldObj, blockpos1, iblockstate1, this);
                    }
                }
            }

            if (getIsCritical())
            {
                for (int k = 0; k < 4; ++k)
                {
                    worldObj.spawnParticle(EnumParticleTypes.CRIT, posX + motionX * (double)k / 4.0D, posY + motionY * (double)k / 4.0D, posZ + motionZ * (double)k / 4.0D, -motionX, -motionY + 0.2D, -motionZ);
                }
            }

            posX += motionX;
            posY += motionY;
            posZ += motionZ;
            float f3 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
            rotationYaw = (float)(MathHelper.func_181159_b(motionX, motionZ) * 180.0D / Math.PI);

            for (rotationPitch = (float)(MathHelper.func_181159_b(motionY, f3) * 180.0D / Math.PI); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F)
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
            float f4 = 0.99F;
            float f6 = 0.05F;

            if (isInWater())
            {
                for (int i1 = 0; i1 < 4; ++i1)
                {
                    float f8 = 0.25F;
                    worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * (double)f8, posY - motionY * (double)f8, posZ - motionZ * (double)f8, motionX, motionY, motionZ);
                }

                f4 = 0.6F;
            }

            if (isWet())
            {
                extinguish();
            }

            motionX *= f4;
            motionY *= f4;
            motionZ *= f4;
            motionY -= f6;
            setPosition(posX, posY, posZ);
            doBlockCollisions();
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        tagCompound.setShort("xTile", (short) xTile);
        tagCompound.setShort("yTile", (short) yTile);
        tagCompound.setShort("zTile", (short) zTile);
        tagCompound.setShort("life", (short) ticksInGround);
        ResourceLocation resourcelocation = Block.blockRegistry.getNameForObject(inTile);
        tagCompound.setString("inTile", resourcelocation == null ? "" : resourcelocation.toString());
        tagCompound.setByte("inData", (byte) inData);
        tagCompound.setByte("shake", (byte) arrowShake);
        tagCompound.setByte("inGround", (byte)(inGround ? 1 : 0));
        tagCompound.setByte("pickup", (byte) canBePickedUp);
        tagCompound.setDouble("damage", damage);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        xTile = tagCompund.getShort("xTile");
        yTile = tagCompund.getShort("yTile");
        zTile = tagCompund.getShort("zTile");
        ticksInGround = tagCompund.getShort("life");

        if (tagCompund.hasKey("inTile", 8))
        {
            inTile = Block.getBlockFromName(tagCompund.getString("inTile"));
        }
        else
        {
            inTile = Block.getBlockById(tagCompund.getByte("inTile") & 255);
        }

        inData = tagCompund.getByte("inData") & 255;
        arrowShake = tagCompund.getByte("shake") & 255;
        inGround = tagCompund.getByte("inGround") == 1;

        if (tagCompund.hasKey("damage", 99))
        {
            damage = tagCompund.getDouble("damage");
        }

        if (tagCompund.hasKey("pickup", 99))
        {
            canBePickedUp = tagCompund.getByte("pickup");
        }
        else if (tagCompund.hasKey("player", 99))
        {
            canBePickedUp = tagCompund.getBoolean("player") ? 1 : 0;
        }
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    public void onCollideWithPlayer(EntityPlayer entityIn)
    {
        if (!worldObj.isRemote && inGround && arrowShake <= 0)
        {
            boolean flag = canBePickedUp == 1 || canBePickedUp == 2 && entityIn.capabilities.isCreativeMode;

            if (canBePickedUp == 1 && !entityIn.inventory.addItemStackToInventory(new ItemStack(Items.arrow, 1)))
            {
                flag = false;
            }

            if (flag)
            {
                playSound("random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                entityIn.onItemPickup(this, 1);
                setDead();
            }
        }
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking()
    {
        return false;
    }

    public void setDamage(double damageIn)
    {
        damage = damageIn;
    }

    public double getDamage()
    {
        return damage;
    }

    /**
     * Sets the amount of knockback the arrow applies when it hits a mob.
     */
    public void setKnockbackStrength(int knockbackStrengthIn)
    {
        knockbackStrength = knockbackStrengthIn;
    }

    /**
     * If returns false, the item will not inflict any damage against entities.
     */
    public boolean canAttackWithItem()
    {
        return false;
    }

    public float getEyeHeight()
    {
        return 0.0F;
    }

    /**
     * Whether the arrow has a stream of critical hit particles flying behind it.
     */
    public void setIsCritical(boolean critical)
    {
        byte b0 = dataWatcher.getWatchableObjectByte(16);

        if (critical)
        {
            dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 | 1)));
        }
        else
        {
            dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 & -2)));
        }
    }

    /**
     * Whether the arrow has a stream of critical hit particles flying behind it.
     */
    public boolean getIsCritical()
    {
        byte b0 = dataWatcher.getWatchableObjectByte(16);
        return (b0 & 1) != 0;
    }
}
