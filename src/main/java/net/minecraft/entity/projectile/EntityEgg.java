package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityEgg extends EntityThrowable
{
    public EntityEgg(World worldIn)
    {
        super(worldIn);
    }

    public EntityEgg(World worldIn, EntityLivingBase throwerIn)
    {
        super(worldIn, throwerIn);
    }

    public EntityEgg(World worldIn, double x, double y, double z)
    {
        super(worldIn, x, y, z);
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    protected void onImpact(MovingObjectPosition p_70184_1_)
    {
        if (p_70184_1_.entityHit != null)
        {
            p_70184_1_.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), 0.0F);
        }

        if (!worldObj.isRemote && rand.nextInt(8) == 0)
        {
            int i = 1;

            if (rand.nextInt(32) == 0)
            {
                i = 4;
            }

            for (int j = 0; j < i; ++j)
            {
                EntityChicken entitychicken = new EntityChicken(worldObj);
                entitychicken.setGrowingAge(-24000);
                entitychicken.setLocationAndAngles(posX, posY, posZ, rotationYaw, 0.0F);
                worldObj.spawnEntityInWorld(entitychicken);
            }
        }

        double d0 = 0.08D;

        for (int k = 0; k < 8; ++k)
        {
            worldObj.spawnParticle(EnumParticleTypes.ITEM_CRACK, posX, posY, posZ, ((double) rand.nextFloat() - 0.5D) * 0.08D, ((double) rand.nextFloat() - 0.5D) * 0.08D, ((double) rand.nextFloat() - 0.5D) * 0.08D, Item.getIdFromItem(Items.egg));
        }

        if (!worldObj.isRemote)
        {
            setDead();
        }
    }
}
