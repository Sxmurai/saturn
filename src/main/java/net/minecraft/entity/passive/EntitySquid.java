package net.minecraft.entity.passive;

import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntitySquid extends EntityWaterMob
{
    public float squidPitch;
    public float prevSquidPitch;
    public float squidYaw;
    public float prevSquidYaw;

    /**
     * appears to be rotation in radians; we already have pitch & yaw, so this completes the triumvirate.
     */
    public float squidRotation;

    /** previous squidRotation in radians */
    public float prevSquidRotation;

    /** angle of the tentacles in radians */
    public float tentacleAngle;

    /** the last calculated angle of the tentacles in radians */
    public float lastTentacleAngle;
    private float randomMotionSpeed;

    /** change in squidRotation in radians. */
    private float rotationVelocity;
    private float field_70871_bB;
    private float randomMotionVecX;
    private float randomMotionVecY;
    private float randomMotionVecZ;

    public EntitySquid(World worldIn)
    {
        super(worldIn);
        setSize(0.95F, 0.95F);
        rand.setSeed(1 + getEntityId());
        rotationVelocity = 1.0F / (rand.nextFloat() + 1.0F) * 0.2F;
        tasks.addTask(0, new EntitySquid.AIMoveRandom(this));
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0D);
    }

    public float getEyeHeight()
    {
        return height * 0.5F;
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    protected String getLivingSound()
    {
        return null;
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return null;
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        return null;
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 0.4F;
    }

    protected Item getDropItem()
    {
        return null;
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
     * Drop 0-2 items of this living's type
     */
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
        int i = rand.nextInt(3 + p_70628_2_) + 1;

        for (int j = 0; j < i; ++j)
        {
            entityDropItem(new ItemStack(Items.dye, 1, EnumDyeColor.BLACK.getDyeDamage()), 0.0F);
        }
    }

    /**
     * Checks if this entity is inside water (if inWater field is true as a result of handleWaterMovement() returning
     * true)
     */
    public boolean isInWater()
    {
        return worldObj.handleMaterialAcceleration(getEntityBoundingBox().expand(0.0D, -0.6000000238418579D, 0.0D), Material.water, this);
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
        prevSquidPitch = squidPitch;
        prevSquidYaw = squidYaw;
        prevSquidRotation = squidRotation;
        lastTentacleAngle = tentacleAngle;
        squidRotation += rotationVelocity;

        if ((double) squidRotation > (Math.PI * 2D))
        {
            if (worldObj.isRemote)
            {
                squidRotation = ((float)Math.PI * 2F);
            }
            else
            {
                squidRotation = (float)((double) squidRotation - (Math.PI * 2D));

                if (rand.nextInt(10) == 0)
                {
                    rotationVelocity = 1.0F / (rand.nextFloat() + 1.0F) * 0.2F;
                }

                worldObj.setEntityState(this, (byte)19);
            }
        }

        if (inWater)
        {
            if (squidRotation < (float)Math.PI)
            {
                float f = squidRotation / (float)Math.PI;
                tentacleAngle = MathHelper.sin(f * f * (float)Math.PI) * (float)Math.PI * 0.25F;

                if ((double)f > 0.75D)
                {
                    randomMotionSpeed = 1.0F;
                    field_70871_bB = 1.0F;
                }
                else
                {
                    field_70871_bB *= 0.8F;
                }
            }
            else
            {
                tentacleAngle = 0.0F;
                randomMotionSpeed *= 0.9F;
                field_70871_bB *= 0.99F;
            }

            if (!worldObj.isRemote)
            {
                motionX = randomMotionVecX * randomMotionSpeed;
                motionY = randomMotionVecY * randomMotionSpeed;
                motionZ = randomMotionVecZ * randomMotionSpeed;
            }

            float f1 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
            renderYawOffset += (-((float)MathHelper.func_181159_b(motionX, motionZ)) * 180.0F / (float)Math.PI - renderYawOffset) * 0.1F;
            rotationYaw = renderYawOffset;
            squidYaw = (float)((double) squidYaw + Math.PI * (double) field_70871_bB * 1.5D);
            squidPitch += (-((float)MathHelper.func_181159_b(f1, motionY)) * 180.0F / (float)Math.PI - squidPitch) * 0.1F;
        }
        else
        {
            tentacleAngle = MathHelper.abs(MathHelper.sin(squidRotation)) * (float)Math.PI * 0.25F;

            if (!worldObj.isRemote)
            {
                motionX = 0.0D;
                motionY -= 0.08D;
                motionY *= 0.9800000190734863D;
                motionZ = 0.0D;
            }

            squidPitch = (float)((double) squidPitch + (double)(-90.0F - squidPitch) * 0.02D);
        }
    }

    /**
     * Moves the entity based on the specified heading.  Args: strafe, forward
     */
    public void moveEntityWithHeading(float strafe, float forward)
    {
        moveEntity(motionX, motionY, motionZ);
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    public boolean getCanSpawnHere()
    {
        return posY > 45.0D && posY < (double) worldObj.func_181545_F() && super.getCanSpawnHere();
    }

    public void handleStatusUpdate(byte id)
    {
        if (id == 19)
        {
            squidRotation = 0.0F;
        }
        else
        {
            super.handleStatusUpdate(id);
        }
    }

    public void func_175568_b(float randomMotionVecXIn, float randomMotionVecYIn, float randomMotionVecZIn)
    {
        randomMotionVecX = randomMotionVecXIn;
        randomMotionVecY = randomMotionVecYIn;
        randomMotionVecZ = randomMotionVecZIn;
    }

    public boolean func_175567_n()
    {
        return randomMotionVecX != 0.0F || randomMotionVecY != 0.0F || randomMotionVecZ != 0.0F;
    }

    static class AIMoveRandom extends EntityAIBase
    {
        private final EntitySquid squid;

        public AIMoveRandom(EntitySquid p_i45859_1_)
        {
            squid = p_i45859_1_;
        }

        public boolean shouldExecute()
        {
            return true;
        }

        public void updateTask()
        {
            int i = squid.getAge();

            if (i > 100)
            {
                squid.func_175568_b(0.0F, 0.0F, 0.0F);
            }
            else if (squid.getRNG().nextInt(50) == 0 || !squid.inWater || !squid.func_175567_n())
            {
                float f = squid.getRNG().nextFloat() * (float)Math.PI * 2.0F;
                float f1 = MathHelper.cos(f) * 0.2F;
                float f2 = -0.1F + squid.getRNG().nextFloat() * 0.2F;
                float f3 = MathHelper.sin(f) * 0.2F;
                squid.func_175568_b(f1, f2, f3);
            }
        }
    }
}
