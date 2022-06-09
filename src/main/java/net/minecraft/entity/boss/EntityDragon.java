package net.minecraft.entity.boss;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class EntityDragon extends EntityLiving implements IBossDisplayData, IEntityMultiPart, IMob
{
    public double targetX;
    public double targetY;
    public double targetZ;

    /**
     * Ring buffer array for the last 64 Y-positions and yaw rotations. Used to calculate offsets for the animations.
     */
    public double[][] ringBuffer = new double[64][3];

    /**
     * Index into the ring buffer. Incremented once per tick and restarts at 0 once it reaches the end of the buffer.
     */
    public int ringBufferIndex = -1;

    /** An array containing all body parts of this dragon */
    public EntityDragonPart[] dragonPartArray;

    /** The head bounding box of a dragon */
    public EntityDragonPart dragonPartHead;

    /** The body bounding box of a dragon */
    public EntityDragonPart dragonPartBody;
    public EntityDragonPart dragonPartTail1;
    public EntityDragonPart dragonPartTail2;
    public EntityDragonPart dragonPartTail3;
    public EntityDragonPart dragonPartWing1;
    public EntityDragonPart dragonPartWing2;

    /** Animation time at previous tick. */
    public float prevAnimTime;

    /**
     * Animation time, used to control the speed of the animation cycles (wings flapping, jaw opening, etc.)
     */
    public float animTime;

    /** Force selecting a new flight target at next tick if set to true. */
    public boolean forceNewTarget;

    /**
     * Activated if the dragon is flying though obsidian, white stone or bedrock. Slows movement and animation speed.
     */
    public boolean slowed;
    private Entity target;
    public int deathTicks;

    /** The current endercrystal that is healing this dragon */
    public EntityEnderCrystal healingEnderCrystal;

    public EntityDragon(World worldIn)
    {
        super(worldIn);
        dragonPartArray = new EntityDragonPart[] {dragonPartHead = new EntityDragonPart(this, "head", 6.0F, 6.0F), dragonPartBody = new EntityDragonPart(this, "body", 8.0F, 8.0F), dragonPartTail1 = new EntityDragonPart(this, "tail", 4.0F, 4.0F), dragonPartTail2 = new EntityDragonPart(this, "tail", 4.0F, 4.0F), dragonPartTail3 = new EntityDragonPart(this, "tail", 4.0F, 4.0F), dragonPartWing1 = new EntityDragonPart(this, "wing", 4.0F, 4.0F), dragonPartWing2 = new EntityDragonPart(this, "wing", 4.0F, 4.0F)};
        setHealth(getMaxHealth());
        setSize(16.0F, 8.0F);
        noClip = true;
        isImmuneToFire = true;
        targetY = 100.0D;
        ignoreFrustumCheck = true;
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(200.0D);
    }

    protected void entityInit()
    {
        super.entityInit();
    }

    /**
     * Returns a double[3] array with movement offsets, used to calculate trailing tail/neck positions. [0] = yaw
     * offset, [1] = y offset, [2] = unused, always 0. Parameters: buffer index offset, partial ticks.
     */
    public double[] getMovementOffsets(int p_70974_1_, float p_70974_2_)
    {
        if (getHealth() <= 0.0F)
        {
            p_70974_2_ = 0.0F;
        }

        p_70974_2_ = 1.0F - p_70974_2_;
        int i = ringBufferIndex - p_70974_1_ * 1 & 63;
        int j = ringBufferIndex - p_70974_1_ * 1 - 1 & 63;
        double[] adouble = new double[3];
        double d0 = ringBuffer[i][0];
        double d1 = MathHelper.wrapAngleTo180_double(ringBuffer[j][0] - d0);
        adouble[0] = d0 + d1 * (double)p_70974_2_;
        d0 = ringBuffer[i][1];
        d1 = ringBuffer[j][1] - d0;
        adouble[1] = d0 + d1 * (double)p_70974_2_;
        adouble[2] = ringBuffer[i][2] + (ringBuffer[j][2] - ringBuffer[i][2]) * (double)p_70974_2_;
        return adouble;
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        if (worldObj.isRemote)
        {
            float f = MathHelper.cos(animTime * (float)Math.PI * 2.0F);
            float f1 = MathHelper.cos(prevAnimTime * (float)Math.PI * 2.0F);

            if (f1 <= -0.3F && f >= -0.3F && !isSilent())
            {
                worldObj.playSound(posX, posY, posZ, "mob.enderdragon.wings", 5.0F, 0.8F + rand.nextFloat() * 0.3F, false);
            }
        }

        prevAnimTime = animTime;

        if (getHealth() <= 0.0F)
        {
            float f11 = (rand.nextFloat() - 0.5F) * 8.0F;
            float f13 = (rand.nextFloat() - 0.5F) * 4.0F;
            float f14 = (rand.nextFloat() - 0.5F) * 8.0F;
            worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, posX + (double)f11, posY + 2.0D + (double)f13, posZ + (double)f14, 0.0D, 0.0D, 0.0D);
        }
        else
        {
            updateDragonEnderCrystal();
            float f10 = 0.2F / (MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ) * 10.0F + 1.0F);
            f10 = f10 * (float)Math.pow(2.0D, motionY);

            if (slowed)
            {
                animTime += f10 * 0.5F;
            }
            else
            {
                animTime += f10;
            }

            rotationYaw = MathHelper.wrapAngleTo180_float(rotationYaw);

            if (isAIDisabled())
            {
                animTime = 0.5F;
            }
            else
            {
                if (ringBufferIndex < 0)
                {
                    for (int i = 0; i < ringBuffer.length; ++i)
                    {
                        ringBuffer[i][0] = rotationYaw;
                        ringBuffer[i][1] = posY;
                    }
                }

                if (++ringBufferIndex == ringBuffer.length)
                {
                    ringBufferIndex = 0;
                }

                ringBuffer[ringBufferIndex][0] = rotationYaw;
                ringBuffer[ringBufferIndex][1] = posY;

                if (worldObj.isRemote)
                {
                    if (newPosRotationIncrements > 0)
                    {
                        double d10 = posX + (newPosX - posX) / (double) newPosRotationIncrements;
                        double d0 = posY + (newPosY - posY) / (double) newPosRotationIncrements;
                        double d1 = posZ + (newPosZ - posZ) / (double) newPosRotationIncrements;
                        double d2 = MathHelper.wrapAngleTo180_double(newRotationYaw - (double) rotationYaw);
                        rotationYaw = (float)((double) rotationYaw + d2 / (double) newPosRotationIncrements);
                        rotationPitch = (float)((double) rotationPitch + (newRotationPitch - (double) rotationPitch) / (double) newPosRotationIncrements);
                        --newPosRotationIncrements;
                        setPosition(d10, d0, d1);
                        setRotation(rotationYaw, rotationPitch);
                    }
                }
                else
                {
                    double d11 = targetX - posX;
                    double d12 = targetY - posY;
                    double d13 = targetZ - posZ;
                    double d14 = d11 * d11 + d12 * d12 + d13 * d13;

                    if (target != null)
                    {
                        targetX = target.posX;
                        targetZ = target.posZ;
                        double d3 = targetX - posX;
                        double d5 = targetZ - posZ;
                        double d7 = Math.sqrt(d3 * d3 + d5 * d5);
                        double d8 = 0.4000000059604645D + d7 / 80.0D - 1.0D;

                        if (d8 > 10.0D)
                        {
                            d8 = 10.0D;
                        }

                        targetY = target.getEntityBoundingBox().minY + d8;
                    }
                    else
                    {
                        targetX += rand.nextGaussian() * 2.0D;
                        targetZ += rand.nextGaussian() * 2.0D;
                    }

                    if (forceNewTarget || d14 < 100.0D || d14 > 22500.0D || isCollidedHorizontally || isCollidedVertically)
                    {
                        setNewTarget();
                    }

                    d12 = d12 / (double)MathHelper.sqrt_double(d11 * d11 + d13 * d13);
                    float f17 = 0.6F;
                    d12 = MathHelper.clamp_double(d12, -f17, f17);
                    motionY += d12 * 0.10000000149011612D;
                    rotationYaw = MathHelper.wrapAngleTo180_float(rotationYaw);
                    double d4 = 180.0D - MathHelper.func_181159_b(d11, d13) * 180.0D / Math.PI;
                    double d6 = MathHelper.wrapAngleTo180_double(d4 - (double) rotationYaw);

                    if (d6 > 50.0D)
                    {
                        d6 = 50.0D;
                    }

                    if (d6 < -50.0D)
                    {
                        d6 = -50.0D;
                    }

                    Vec3 vec3 = (new Vec3(targetX - posX, targetY - posY, targetZ - posZ)).normalize();
                    double d15 = -MathHelper.cos(rotationYaw * (float)Math.PI / 180.0F);
                    Vec3 vec31 = (new Vec3(MathHelper.sin(rotationYaw * (float)Math.PI / 180.0F), motionY, d15)).normalize();
                    float f5 = ((float)vec31.dotProduct(vec3) + 0.5F) / 1.5F;

                    if (f5 < 0.0F)
                    {
                        f5 = 0.0F;
                    }

                    randomYawVelocity *= 0.8F;
                    float f6 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ) * 1.0F + 1.0F;
                    double d9 = Math.sqrt(motionX * motionX + motionZ * motionZ) * 1.0D + 1.0D;

                    if (d9 > 40.0D)
                    {
                        d9 = 40.0D;
                    }

                    randomYawVelocity = (float)((double) randomYawVelocity + d6 * (0.699999988079071D / d9 / (double)f6));
                    rotationYaw += randomYawVelocity * 0.1F;
                    float f7 = (float)(2.0D / (d9 + 1.0D));
                    float f8 = 0.06F;
                    moveFlying(0.0F, -1.0F, f8 * (f5 * f7 + (1.0F - f7)));

                    if (slowed)
                    {
                        moveEntity(motionX * 0.800000011920929D, motionY * 0.800000011920929D, motionZ * 0.800000011920929D);
                    }
                    else
                    {
                        moveEntity(motionX, motionY, motionZ);
                    }

                    Vec3 vec32 = (new Vec3(motionX, motionY, motionZ)).normalize();
                    float f9 = ((float)vec32.dotProduct(vec31) + 1.0F) / 2.0F;
                    f9 = 0.8F + 0.15F * f9;
                    motionX *= f9;
                    motionZ *= f9;
                    motionY *= 0.9100000262260437D;
                }

                renderYawOffset = rotationYaw;
                dragonPartHead.width = dragonPartHead.height = 3.0F;
                dragonPartTail1.width = dragonPartTail1.height = 2.0F;
                dragonPartTail2.width = dragonPartTail2.height = 2.0F;
                dragonPartTail3.width = dragonPartTail3.height = 2.0F;
                dragonPartBody.height = 3.0F;
                dragonPartBody.width = 5.0F;
                dragonPartWing1.height = 2.0F;
                dragonPartWing1.width = 4.0F;
                dragonPartWing2.height = 3.0F;
                dragonPartWing2.width = 4.0F;
                float f12 = (float)(getMovementOffsets(5, 1.0F)[1] - getMovementOffsets(10, 1.0F)[1]) * 10.0F / 180.0F * (float)Math.PI;
                float f2 = MathHelper.cos(f12);
                float f15 = -MathHelper.sin(f12);
                float f3 = rotationYaw * (float)Math.PI / 180.0F;
                float f16 = MathHelper.sin(f3);
                float f4 = MathHelper.cos(f3);
                dragonPartBody.onUpdate();
                dragonPartBody.setLocationAndAngles(posX + (double)(f16 * 0.5F), posY, posZ - (double)(f4 * 0.5F), 0.0F, 0.0F);
                dragonPartWing1.onUpdate();
                dragonPartWing1.setLocationAndAngles(posX + (double)(f4 * 4.5F), posY + 2.0D, posZ + (double)(f16 * 4.5F), 0.0F, 0.0F);
                dragonPartWing2.onUpdate();
                dragonPartWing2.setLocationAndAngles(posX - (double)(f4 * 4.5F), posY + 2.0D, posZ - (double)(f16 * 4.5F), 0.0F, 0.0F);

                if (!worldObj.isRemote && hurtTime == 0)
                {
                    collideWithEntities(worldObj.getEntitiesWithinAABBExcludingEntity(this, dragonPartWing1.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D).offset(0.0D, -2.0D, 0.0D)));
                    collideWithEntities(worldObj.getEntitiesWithinAABBExcludingEntity(this, dragonPartWing2.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D).offset(0.0D, -2.0D, 0.0D)));
                    attackEntitiesInList(worldObj.getEntitiesWithinAABBExcludingEntity(this, dragonPartHead.getEntityBoundingBox().expand(1.0D, 1.0D, 1.0D)));
                }

                double[] adouble1 = getMovementOffsets(5, 1.0F);
                double[] adouble = getMovementOffsets(0, 1.0F);
                float f18 = MathHelper.sin(rotationYaw * (float)Math.PI / 180.0F - randomYawVelocity * 0.01F);
                float f19 = MathHelper.cos(rotationYaw * (float)Math.PI / 180.0F - randomYawVelocity * 0.01F);
                dragonPartHead.onUpdate();
                dragonPartHead.setLocationAndAngles(posX + (double)(f18 * 5.5F * f2), posY + (adouble[1] - adouble1[1]) * 1.0D + (double)(f15 * 5.5F), posZ - (double)(f19 * 5.5F * f2), 0.0F, 0.0F);

                for (int j = 0; j < 3; ++j)
                {
                    EntityDragonPart entitydragonpart = null;

                    if (j == 0)
                    {
                        entitydragonpart = dragonPartTail1;
                    }

                    if (j == 1)
                    {
                        entitydragonpart = dragonPartTail2;
                    }

                    if (j == 2)
                    {
                        entitydragonpart = dragonPartTail3;
                    }

                    double[] adouble2 = getMovementOffsets(12 + j * 2, 1.0F);
                    float f20 = rotationYaw * (float)Math.PI / 180.0F + simplifyAngle(adouble2[0] - adouble1[0]) * (float)Math.PI / 180.0F * 1.0F;
                    float f21 = MathHelper.sin(f20);
                    float f22 = MathHelper.cos(f20);
                    float f23 = 1.5F;
                    float f24 = (float)(j + 1) * 2.0F;
                    entitydragonpart.onUpdate();
                    entitydragonpart.setLocationAndAngles(posX - (double)((f16 * f23 + f21 * f24) * f2), posY + (adouble2[1] - adouble1[1]) * 1.0D - (double)((f24 + f23) * f15) + 1.5D, posZ + (double)((f4 * f23 + f22 * f24) * f2), 0.0F, 0.0F);
                }

                if (!worldObj.isRemote)
                {
                    slowed = destroyBlocksInAABB(dragonPartHead.getEntityBoundingBox()) | destroyBlocksInAABB(dragonPartBody.getEntityBoundingBox());
                }
            }
        }
    }

    /**
     * Updates the state of the enderdragon's current endercrystal.
     */
    private void updateDragonEnderCrystal()
    {
        if (healingEnderCrystal != null)
        {
            if (healingEnderCrystal.isDead)
            {
                if (!worldObj.isRemote)
                {
                    attackEntityFromPart(dragonPartHead, DamageSource.setExplosionSource(null), 10.0F);
                }

                healingEnderCrystal = null;
            }
            else if (ticksExisted % 10 == 0 && getHealth() < getMaxHealth())
            {
                setHealth(getHealth() + 1.0F);
            }
        }

        if (rand.nextInt(10) == 0)
        {
            float f = 32.0F;
            List<EntityEnderCrystal> list = worldObj.getEntitiesWithinAABB(EntityEnderCrystal.class, getEntityBoundingBox().expand(f, f, f));
            EntityEnderCrystal entityendercrystal = null;
            double d0 = Double.MAX_VALUE;

            for (EntityEnderCrystal entityendercrystal1 : list)
            {
                double d1 = entityendercrystal1.getDistanceSqToEntity(this);

                if (d1 < d0)
                {
                    d0 = d1;
                    entityendercrystal = entityendercrystal1;
                }
            }

            healingEnderCrystal = entityendercrystal;
        }
    }

    /**
     * Pushes all entities inside the list away from the enderdragon.
     */
    private void collideWithEntities(List<Entity> p_70970_1_)
    {
        double d0 = (dragonPartBody.getEntityBoundingBox().minX + dragonPartBody.getEntityBoundingBox().maxX) / 2.0D;
        double d1 = (dragonPartBody.getEntityBoundingBox().minZ + dragonPartBody.getEntityBoundingBox().maxZ) / 2.0D;

        for (Entity entity : p_70970_1_)
        {
            if (entity instanceof EntityLivingBase)
            {
                double d2 = entity.posX - d0;
                double d3 = entity.posZ - d1;
                double d4 = d2 * d2 + d3 * d3;
                entity.addVelocity(d2 / d4 * 4.0D, 0.20000000298023224D, d3 / d4 * 4.0D);
            }
        }
    }

    /**
     * Attacks all entities inside this list, dealing 5 hearts of damage.
     */
    private void attackEntitiesInList(List<Entity> p_70971_1_)
    {
        for (int i = 0; i < p_70971_1_.size(); ++i)
        {
            Entity entity = p_70971_1_.get(i);

            if (entity instanceof EntityLivingBase)
            {
                entity.attackEntityFrom(DamageSource.causeMobDamage(this), 10.0F);
                applyEnchantments(this, entity);
            }
        }
    }

    /**
     * Sets a new target for the flight AI. It can be a random coordinate or a nearby player.
     */
    private void setNewTarget()
    {
        forceNewTarget = false;
        List<EntityPlayer> list = Lists.newArrayList(worldObj.playerEntities);
        Iterator<EntityPlayer> iterator = list.iterator();

        while (iterator.hasNext())
        {
            if (iterator.next().isSpectator())
            {
                iterator.remove();
            }
        }

        if (rand.nextInt(2) == 0 && !list.isEmpty())
        {
            target = list.get(rand.nextInt(list.size()));
        }
        else
        {
            while (true)
            {
                targetX = 0.0D;
                targetY = 70.0F + rand.nextFloat() * 50.0F;
                targetZ = 0.0D;
                targetX += rand.nextFloat() * 120.0F - 60.0F;
                targetZ += rand.nextFloat() * 120.0F - 60.0F;
                double d0 = posX - targetX;
                double d1 = posY - targetY;
                double d2 = posZ - targetZ;
                boolean flag = d0 * d0 + d1 * d1 + d2 * d2 > 100.0D;

                if (flag)
                {
                    break;
                }
            }

            target = null;
        }
    }

    /**
     * Simplifies the value of a number by adding/subtracting 180 to the point that the number is between -180 and 180.
     */
    private float simplifyAngle(double p_70973_1_)
    {
        return (float)MathHelper.wrapAngleTo180_double(p_70973_1_);
    }

    /**
     * Destroys all blocks that aren't associated with 'The End' inside the given bounding box.
     */
    private boolean destroyBlocksInAABB(AxisAlignedBB p_70972_1_)
    {
        int i = MathHelper.floor_double(p_70972_1_.minX);
        int j = MathHelper.floor_double(p_70972_1_.minY);
        int k = MathHelper.floor_double(p_70972_1_.minZ);
        int l = MathHelper.floor_double(p_70972_1_.maxX);
        int i1 = MathHelper.floor_double(p_70972_1_.maxY);
        int j1 = MathHelper.floor_double(p_70972_1_.maxZ);
        boolean flag = false;
        boolean flag1 = false;

        for (int k1 = i; k1 <= l; ++k1)
        {
            for (int l1 = j; l1 <= i1; ++l1)
            {
                for (int i2 = k; i2 <= j1; ++i2)
                {
                    BlockPos blockpos = new BlockPos(k1, l1, i2);
                    Block block = worldObj.getBlockState(blockpos).getBlock();

                    if (block.getMaterial() != Material.air)
                    {
                        if (block != Blocks.barrier && block != Blocks.obsidian && block != Blocks.end_stone && block != Blocks.bedrock && block != Blocks.command_block && worldObj.getGameRules().getBoolean("mobGriefing"))
                        {
                            flag1 = worldObj.setBlockToAir(blockpos) || flag1;
                        }
                        else
                        {
                            flag = true;
                        }
                    }
                }
            }
        }

        if (flag1)
        {
            double d0 = p_70972_1_.minX + (p_70972_1_.maxX - p_70972_1_.minX) * (double) rand.nextFloat();
            double d1 = p_70972_1_.minY + (p_70972_1_.maxY - p_70972_1_.minY) * (double) rand.nextFloat();
            double d2 = p_70972_1_.minZ + (p_70972_1_.maxZ - p_70972_1_.minZ) * (double) rand.nextFloat();
            worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }

        return flag;
    }

    public boolean attackEntityFromPart(EntityDragonPart dragonPart, DamageSource source, float p_70965_3_)
    {
        if (dragonPart != dragonPartHead)
        {
            p_70965_3_ = p_70965_3_ / 4.0F + 1.0F;
        }

        float f = rotationYaw * (float)Math.PI / 180.0F;
        float f1 = MathHelper.sin(f);
        float f2 = MathHelper.cos(f);
        targetX = posX + (double)(f1 * 5.0F) + (double)((rand.nextFloat() - 0.5F) * 2.0F);
        targetY = posY + (double)(rand.nextFloat() * 3.0F) + 1.0D;
        targetZ = posZ - (double)(f2 * 5.0F) + (double)((rand.nextFloat() - 0.5F) * 2.0F);
        target = null;

        if (source.getEntity() instanceof EntityPlayer || source.isExplosion())
        {
            attackDragonFrom(source, p_70965_3_);
        }

        return true;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (source instanceof EntityDamageSource && ((EntityDamageSource)source).getIsThornsDamage())
        {
            attackDragonFrom(source, amount);
        }

        return false;
    }

    /**
     * Provides a way to cause damage to an ender dragon.
     */
    protected boolean attackDragonFrom(DamageSource source, float amount)
    {
        return super.attackEntityFrom(source, amount);
    }

    /**
     * Called by the /kill command.
     */
    public void onKillCommand()
    {
        setDead();
    }

    /**
     * handles entity death timer, experience orb and particle creation
     */
    protected void onDeathUpdate()
    {
        ++deathTicks;

        if (deathTicks >= 180 && deathTicks <= 200)
        {
            float f = (rand.nextFloat() - 0.5F) * 8.0F;
            float f1 = (rand.nextFloat() - 0.5F) * 4.0F;
            float f2 = (rand.nextFloat() - 0.5F) * 8.0F;
            worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, posX + (double)f, posY + 2.0D + (double)f1, posZ + (double)f2, 0.0D, 0.0D, 0.0D);
        }

        boolean flag = worldObj.getGameRules().getBoolean("doMobLoot");

        if (!worldObj.isRemote)
        {
            if (deathTicks > 150 && deathTicks % 5 == 0 && flag)
            {
                int i = 1000;

                while (i > 0)
                {
                    int k = EntityXPOrb.getXPSplit(i);
                    i -= k;
                    worldObj.spawnEntityInWorld(new EntityXPOrb(worldObj, posX, posY, posZ, k));
                }
            }

            if (deathTicks == 1)
            {
                worldObj.playBroadcastSound(1018, new BlockPos(this), 0);
            }
        }

        moveEntity(0.0D, 0.10000000149011612D, 0.0D);
        renderYawOffset = rotationYaw += 20.0F;

        if (deathTicks == 200 && !worldObj.isRemote)
        {
            if (flag)
            {
                int j = 2000;

                while (j > 0)
                {
                    int l = EntityXPOrb.getXPSplit(j);
                    j -= l;
                    worldObj.spawnEntityInWorld(new EntityXPOrb(worldObj, posX, posY, posZ, l));
                }
            }

            generatePortal(new BlockPos(posX, 64.0D, posZ));
            setDead();
        }
    }

    /**
     * Generate the portal when the dragon dies
     */
    private void generatePortal(BlockPos pos)
    {
        int i = 4;
        double d0 = 12.25D;
        double d1 = 6.25D;

        for (int j = -1; j <= 32; ++j)
        {
            for (int k = -4; k <= 4; ++k)
            {
                for (int l = -4; l <= 4; ++l)
                {
                    double d2 = k * k + l * l;

                    if (d2 <= 12.25D)
                    {
                        BlockPos blockpos = pos.add(k, j, l);

                        if (j < 0)
                        {
                            if (d2 <= 6.25D)
                            {
                                worldObj.setBlockState(blockpos, Blocks.bedrock.getDefaultState());
                            }
                        }
                        else if (j > 0)
                        {
                            worldObj.setBlockState(blockpos, Blocks.air.getDefaultState());
                        }
                        else if (d2 > 6.25D)
                        {
                            worldObj.setBlockState(blockpos, Blocks.bedrock.getDefaultState());
                        }
                        else
                        {
                            worldObj.setBlockState(blockpos, Blocks.end_portal.getDefaultState());
                        }
                    }
                }
            }
        }

        worldObj.setBlockState(pos, Blocks.bedrock.getDefaultState());
        worldObj.setBlockState(pos.up(), Blocks.bedrock.getDefaultState());
        BlockPos blockpos1 = pos.up(2);
        worldObj.setBlockState(blockpos1, Blocks.bedrock.getDefaultState());
        worldObj.setBlockState(blockpos1.west(), Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.EAST));
        worldObj.setBlockState(blockpos1.east(), Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.WEST));
        worldObj.setBlockState(blockpos1.north(), Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.SOUTH));
        worldObj.setBlockState(blockpos1.south(), Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.NORTH));
        worldObj.setBlockState(pos.up(3), Blocks.bedrock.getDefaultState());
        worldObj.setBlockState(pos.up(4), Blocks.dragon_egg.getDefaultState());
    }

    /**
     * Makes the entity despawn if requirements are reached
     */
    protected void despawnEntity()
    {
    }

    /**
     * Return the Entity parts making up this Entity (currently only for dragons)
     */
    public Entity[] getParts()
    {
        return dragonPartArray;
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean canBeCollidedWith()
    {
        return false;
    }

    public World getWorld()
    {
        return worldObj;
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    protected String getLivingSound()
    {
        return "mob.enderdragon.growl";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return "mob.enderdragon.hit";
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 5.0F;
    }
}
