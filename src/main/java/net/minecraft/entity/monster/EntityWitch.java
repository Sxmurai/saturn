package net.minecraft.entity.monster;

import java.util.List;
import java.util.UUID;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityWitch extends EntityMob implements IRangedAttackMob
{
    private static final UUID MODIFIER_UUID = UUID.fromString("5CD17E52-A79A-43D3-A529-90FDE04B181E");
    private static final AttributeModifier MODIFIER = (new AttributeModifier(EntityWitch.MODIFIER_UUID, "Drinking speed penalty", -0.25D, 0)).setSaved(false);

    /** List of items a witch should drop on death. */
    private static final Item[] witchDrops = new Item[] {Items.glowstone_dust, Items.sugar, Items.redstone, Items.spider_eye, Items.glass_bottle, Items.gunpowder, Items.stick, Items.stick};

    /**
     * Timer used as interval for a witch's attack, decremented every tick if aggressive and when reaches zero the witch
     * will throw a potion at the target entity.
     */
    private int witchAttackTimer;

    public EntityWitch(World worldIn)
    {
        super(worldIn);
        setSize(0.6F, 1.95F);
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIArrowAttack(this, 1.0D, 60, 10.0F));
        tasks.addTask(2, new EntityAIWander(this, 1.0D));
        tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(3, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
    }

    protected void entityInit()
    {
        super.entityInit();
        getDataWatcher().addObject(21, Byte.valueOf((byte)0));
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
     * Set whether this witch is aggressive at an entity.
     */
    public void setAggressive(boolean aggressive)
    {
        getDataWatcher().updateObject(21, Byte.valueOf((byte)(aggressive ? 1 : 0)));
    }

    /**
     * Return whether this witch is aggressive at an entity.
     */
    public boolean getAggressive()
    {
        return getDataWatcher().getWatchableObjectByte(21) == 1;
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(26.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        if (!worldObj.isRemote)
        {
            if (getAggressive())
            {
                if (witchAttackTimer-- <= 0)
                {
                    setAggressive(false);
                    ItemStack itemstack = getHeldItem();
                    setCurrentItemOrArmor(0, null);

                    if (itemstack != null && itemstack.getItem() == Items.potionitem)
                    {
                        List<PotionEffect> list = Items.potionitem.getEffects(itemstack);

                        if (list != null)
                        {
                            for (PotionEffect potioneffect : list)
                            {
                                addPotionEffect(new PotionEffect(potioneffect));
                            }
                        }
                    }

                    getEntityAttribute(SharedMonsterAttributes.movementSpeed).removeModifier(EntityWitch.MODIFIER);
                }
            }
            else
            {
                int i = -1;

                if (rand.nextFloat() < 0.15F && isInsideOfMaterial(Material.water) && !isPotionActive(Potion.waterBreathing))
                {
                    i = 8237;
                }
                else if (rand.nextFloat() < 0.15F && isBurning() && !isPotionActive(Potion.fireResistance))
                {
                    i = 16307;
                }
                else if (rand.nextFloat() < 0.05F && getHealth() < getMaxHealth())
                {
                    i = 16341;
                }
                else if (rand.nextFloat() < 0.25F && getAttackTarget() != null && !isPotionActive(Potion.moveSpeed) && getAttackTarget().getDistanceSqToEntity(this) > 121.0D)
                {
                    i = 16274;
                }
                else if (rand.nextFloat() < 0.25F && getAttackTarget() != null && !isPotionActive(Potion.moveSpeed) && getAttackTarget().getDistanceSqToEntity(this) > 121.0D)
                {
                    i = 16274;
                }

                if (i > -1)
                {
                    setCurrentItemOrArmor(0, new ItemStack(Items.potionitem, 1, i));
                    witchAttackTimer = getHeldItem().getMaxItemUseDuration();
                    setAggressive(true);
                    IAttributeInstance iattributeinstance = getEntityAttribute(SharedMonsterAttributes.movementSpeed);
                    iattributeinstance.removeModifier(EntityWitch.MODIFIER);
                    iattributeinstance.applyModifier(EntityWitch.MODIFIER);
                }
            }

            if (rand.nextFloat() < 7.5E-4F)
            {
                worldObj.setEntityState(this, (byte)15);
            }
        }

        super.onLivingUpdate();
    }

    public void handleStatusUpdate(byte id)
    {
        if (id == 15)
        {
            for (int i = 0; i < rand.nextInt(35) + 10; ++i)
            {
                worldObj.spawnParticle(EnumParticleTypes.SPELL_WITCH, posX + rand.nextGaussian() * 0.12999999523162842D, getEntityBoundingBox().maxY + 0.5D + rand.nextGaussian() * 0.12999999523162842D, posZ + rand.nextGaussian() * 0.12999999523162842D, 0.0D, 0.0D, 0.0D);
            }
        }
        else
        {
            super.handleStatusUpdate(id);
        }
    }

    /**
     * Reduces damage, depending on potions
     */
    protected float applyPotionDamageCalculations(DamageSource source, float damage)
    {
        damage = super.applyPotionDamageCalculations(source, damage);

        if (source.getEntity() == this)
        {
            damage = 0.0F;
        }

        if (source.isMagicDamage())
        {
            damage = (float)((double)damage * 0.15D);
        }

        return damage;
    }

    /**
     * Drop 0-2 items of this living's type
     */
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
        int i = rand.nextInt(3) + 1;

        for (int j = 0; j < i; ++j)
        {
            int k = rand.nextInt(3);
            Item item = EntityWitch.witchDrops[rand.nextInt(EntityWitch.witchDrops.length)];

            if (p_70628_2_ > 0)
            {
                k += rand.nextInt(p_70628_2_ + 1);
            }

            for (int l = 0; l < k; ++l)
            {
                dropItem(item, 1);
            }
        }
    }

    /**
     * Attack the specified entity using a ranged attack.
     */
    public void attackEntityWithRangedAttack(EntityLivingBase p_82196_1_, float p_82196_2_)
    {
        if (!getAggressive())
        {
            EntityPotion entitypotion = new EntityPotion(worldObj, this, 32732);
            double d0 = p_82196_1_.posY + (double)p_82196_1_.getEyeHeight() - 1.100000023841858D;
            entitypotion.rotationPitch -= -20.0F;
            double d1 = p_82196_1_.posX + p_82196_1_.motionX - posX;
            double d2 = d0 - posY;
            double d3 = p_82196_1_.posZ + p_82196_1_.motionZ - posZ;
            float f = MathHelper.sqrt_double(d1 * d1 + d3 * d3);

            if (f >= 8.0F && !p_82196_1_.isPotionActive(Potion.moveSlowdown))
            {
                entitypotion.setPotionDamage(32698);
            }
            else if (p_82196_1_.getHealth() >= 8.0F && !p_82196_1_.isPotionActive(Potion.poison))
            {
                entitypotion.setPotionDamage(32660);
            }
            else if (f <= 3.0F && !p_82196_1_.isPotionActive(Potion.weakness) && rand.nextFloat() < 0.25F)
            {
                entitypotion.setPotionDamage(32696);
            }

            entitypotion.setThrowableHeading(d1, d2 + (double)(f * 0.2F), d3, 0.75F, 8.0F);
            worldObj.spawnEntityInWorld(entitypotion);
        }
    }

    public float getEyeHeight()
    {
        return 1.62F;
    }
}
