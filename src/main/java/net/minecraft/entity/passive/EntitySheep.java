package net.minecraft.entity.passive;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIEatGrass;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntitySheep extends EntityAnimal
{
    /**
     * Internal crafting inventory used to check the result of mixing dyes corresponding to the fleece color when
     * breeding sheep.
     */
    private final InventoryCrafting inventoryCrafting = new InventoryCrafting(new Container()
    {
        public boolean canInteractWith(EntityPlayer playerIn)
        {
            return false;
        }
    }, 2, 1);
    private static final Map<EnumDyeColor, float[]> DYE_TO_RGB = Maps.newEnumMap(EnumDyeColor.class);

    /**
     * Used to control movement as well as wool regrowth. Set to 40 on handleHealthUpdate and counts down with each
     * tick.
     */
    private int sheepTimer;
    private final EntityAIEatGrass entityAIEatGrass = new EntityAIEatGrass(this);

    public static float[] func_175513_a(EnumDyeColor dyeColor)
    {
        return EntitySheep.DYE_TO_RGB.get(dyeColor);
    }

    public EntitySheep(World worldIn)
    {
        super(worldIn);
        setSize(0.9F, 1.3F);
        ((PathNavigateGround) getNavigator()).setAvoidsWater(true);
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 1.25D));
        tasks.addTask(2, new EntityAIMate(this, 1.0D));
        tasks.addTask(3, new EntityAITempt(this, 1.1D, Items.wheat, false));
        tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        tasks.addTask(5, entityAIEatGrass);
        tasks.addTask(6, new EntityAIWander(this, 1.0D));
        tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
        inventoryCrafting.setInventorySlotContents(0, new ItemStack(Items.dye, 1, 0));
        inventoryCrafting.setInventorySlotContents(1, new ItemStack(Items.dye, 1, 0));
    }

    protected void updateAITasks()
    {
        sheepTimer = entityAIEatGrass.getEatingGrassTimer();
        super.updateAITasks();
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        if (worldObj.isRemote)
        {
            sheepTimer = Math.max(0, sheepTimer - 1);
        }

        super.onLivingUpdate();
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(8.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.23000000417232513D);
    }

    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(16, new Byte((byte)0));
    }

    /**
     * Drop 0-2 items of this living's type
     */
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
        if (!getSheared())
        {
            entityDropItem(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, getFleeceColor().getMetadata()), 0.0F);
        }

        int i = rand.nextInt(2) + 1 + rand.nextInt(1 + p_70628_2_);

        for (int j = 0; j < i; ++j)
        {
            if (isBurning())
            {
                dropItem(Items.cooked_mutton, 1);
            }
            else
            {
                dropItem(Items.mutton, 1);
            }
        }
    }

    protected Item getDropItem()
    {
        return Item.getItemFromBlock(Blocks.wool);
    }

    public void handleStatusUpdate(byte id)
    {
        if (id == 10)
        {
            sheepTimer = 40;
        }
        else
        {
            super.handleStatusUpdate(id);
        }
    }

    public float getHeadRotationPointY(float p_70894_1_)
    {
        return sheepTimer <= 0 ? 0.0F : (sheepTimer >= 4 && sheepTimer <= 36 ? 1.0F : (sheepTimer < 4 ? ((float) sheepTimer - p_70894_1_) / 4.0F : -((float)(sheepTimer - 40) - p_70894_1_) / 4.0F));
    }

    public float getHeadRotationAngleX(float p_70890_1_)
    {
        if (sheepTimer > 4 && sheepTimer <= 36)
        {
            float f = ((float)(sheepTimer - 4) - p_70890_1_) / 32.0F;
            return ((float)Math.PI / 5F) + ((float)Math.PI * 7F / 100F) * MathHelper.sin(f * 28.7F);
        }
        else
        {
            return sheepTimer > 0 ? ((float)Math.PI / 5F) : rotationPitch / (180F / (float)Math.PI);
        }
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    public boolean interact(EntityPlayer player)
    {
        ItemStack itemstack = player.inventory.getCurrentItem();

        if (itemstack != null && itemstack.getItem() == Items.shears && !getSheared() && !isChild())
        {
            if (!worldObj.isRemote)
            {
                setSheared(true);
                int i = 1 + rand.nextInt(3);

                for (int j = 0; j < i; ++j)
                {
                    EntityItem entityitem = entityDropItem(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, getFleeceColor().getMetadata()), 1.0F);
                    entityitem.motionY += rand.nextFloat() * 0.05F;
                    entityitem.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                    entityitem.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                }
            }

            itemstack.damageItem(1, player);
            playSound("mob.sheep.shear", 1.0F, 1.0F);
        }

        return super.interact(player);
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setBoolean("Sheared", getSheared());
        tagCompound.setByte("Color", (byte) getFleeceColor().getMetadata());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        setSheared(tagCompund.getBoolean("Sheared"));
        setFleeceColor(EnumDyeColor.byMetadata(tagCompund.getByte("Color")));
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    protected String getLivingSound()
    {
        return "mob.sheep.say";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return "mob.sheep.say";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        return "mob.sheep.say";
    }

    protected void playStepSound(BlockPos pos, Block blockIn)
    {
        playSound("mob.sheep.step", 0.15F, 1.0F);
    }

    /**
     * Gets the wool color of this sheep.
     */
    public EnumDyeColor getFleeceColor()
    {
        return EnumDyeColor.byMetadata(dataWatcher.getWatchableObjectByte(16) & 15);
    }

    /**
     * Sets the wool color of this sheep
     */
    public void setFleeceColor(EnumDyeColor color)
    {
        byte b0 = dataWatcher.getWatchableObjectByte(16);
        dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 & 240 | color.getMetadata() & 15)));
    }

    /**
     * returns true if a sheeps wool has been sheared
     */
    public boolean getSheared()
    {
        return (dataWatcher.getWatchableObjectByte(16) & 16) != 0;
    }

    /**
     * make a sheep sheared if set to true
     */
    public void setSheared(boolean sheared)
    {
        byte b0 = dataWatcher.getWatchableObjectByte(16);

        if (sheared)
        {
            dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 | 16)));
        }
        else
        {
            dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 & -17)));
        }
    }

    /**
     * Chooses a "vanilla" sheep color based on the provided random.
     */
    public static EnumDyeColor getRandomSheepColor(Random random)
    {
        int i = random.nextInt(100);
        return i < 5 ? EnumDyeColor.BLACK : (i < 10 ? EnumDyeColor.GRAY : (i < 15 ? EnumDyeColor.SILVER : (i < 18 ? EnumDyeColor.BROWN : (random.nextInt(500) == 0 ? EnumDyeColor.PINK : EnumDyeColor.WHITE))));
    }

    public EntitySheep createChild(EntityAgeable ageable)
    {
        EntitySheep entitysheep = (EntitySheep)ageable;
        EntitySheep entitysheep1 = new EntitySheep(worldObj);
        entitysheep1.setFleeceColor(getDyeColorMixFromParents(this, entitysheep));
        return entitysheep1;
    }

    /**
     * This function applies the benefits of growing back wool and faster growing up to the acting entity. (This
     * function is used in the AIEatGrass)
     */
    public void eatGrassBonus()
    {
        setSheared(false);

        if (isChild())
        {
            addGrowth(60);
        }
    }

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata)
    {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        setFleeceColor(EntitySheep.getRandomSheepColor(worldObj.rand));
        return livingdata;
    }

    /**
     * Attempts to mix both parent sheep to come up with a mixed dye color.
     */
    private EnumDyeColor getDyeColorMixFromParents(EntityAnimal father, EntityAnimal mother)
    {
        int i = ((EntitySheep)father).getFleeceColor().getDyeDamage();
        int j = ((EntitySheep)mother).getFleeceColor().getDyeDamage();
        inventoryCrafting.getStackInSlot(0).setItemDamage(i);
        inventoryCrafting.getStackInSlot(1).setItemDamage(j);
        ItemStack itemstack = CraftingManager.getInstance().findMatchingRecipe(inventoryCrafting, father.worldObj);
        int k;

        if (itemstack != null && itemstack.getItem() == Items.dye)
        {
            k = itemstack.getMetadata();
        }
        else
        {
            k = worldObj.rand.nextBoolean() ? i : j;
        }

        return EnumDyeColor.byDyeDamage(k);
    }

    public float getEyeHeight()
    {
        return 0.95F * height;
    }

    static
    {
        EntitySheep.DYE_TO_RGB.put(EnumDyeColor.WHITE, new float[] {1.0F, 1.0F, 1.0F});
        EntitySheep.DYE_TO_RGB.put(EnumDyeColor.ORANGE, new float[] {0.85F, 0.5F, 0.2F});
        EntitySheep.DYE_TO_RGB.put(EnumDyeColor.MAGENTA, new float[] {0.7F, 0.3F, 0.85F});
        EntitySheep.DYE_TO_RGB.put(EnumDyeColor.LIGHT_BLUE, new float[] {0.4F, 0.6F, 0.85F});
        EntitySheep.DYE_TO_RGB.put(EnumDyeColor.YELLOW, new float[] {0.9F, 0.9F, 0.2F});
        EntitySheep.DYE_TO_RGB.put(EnumDyeColor.LIME, new float[] {0.5F, 0.8F, 0.1F});
        EntitySheep.DYE_TO_RGB.put(EnumDyeColor.PINK, new float[] {0.95F, 0.5F, 0.65F});
        EntitySheep.DYE_TO_RGB.put(EnumDyeColor.GRAY, new float[] {0.3F, 0.3F, 0.3F});
        EntitySheep.DYE_TO_RGB.put(EnumDyeColor.SILVER, new float[] {0.6F, 0.6F, 0.6F});
        EntitySheep.DYE_TO_RGB.put(EnumDyeColor.CYAN, new float[] {0.3F, 0.5F, 0.6F});
        EntitySheep.DYE_TO_RGB.put(EnumDyeColor.PURPLE, new float[] {0.5F, 0.25F, 0.7F});
        EntitySheep.DYE_TO_RGB.put(EnumDyeColor.BLUE, new float[] {0.2F, 0.3F, 0.7F});
        EntitySheep.DYE_TO_RGB.put(EnumDyeColor.BROWN, new float[] {0.4F, 0.3F, 0.2F});
        EntitySheep.DYE_TO_RGB.put(EnumDyeColor.GREEN, new float[] {0.4F, 0.5F, 0.2F});
        EntitySheep.DYE_TO_RGB.put(EnumDyeColor.RED, new float[] {0.6F, 0.2F, 0.2F});
        EntitySheep.DYE_TO_RGB.put(EnumDyeColor.BLACK, new float[] {0.1F, 0.1F, 0.1F});
    }
}
