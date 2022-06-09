package net.minecraft.entity.passive;

import java.util.Random;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIFollowGolem;
import net.minecraft.entity.ai.EntityAIHarvestFarmland;
import net.minecraft.entity.ai.EntityAILookAtTradePlayer;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIPlay;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITradePlayer;
import net.minecraft.entity.ai.EntityAIVillagerInteract;
import net.minecraft.entity.ai.EntityAIVillagerMate;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Tuple;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.village.Village;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityVillager extends EntityAgeable implements IMerchant, INpc
{
    private int randomTickDivider;
    private boolean isMating;
    private boolean isPlaying;
    Village villageObj;

    /** This villager's current customer. */
    private EntityPlayer buyingPlayer;

    /** Initialises the MerchantRecipeList.java */
    private MerchantRecipeList buyingList;
    private int timeUntilReset;

    /** addDefaultEquipmentAndRecipies is called if this is true */
    private boolean needsInitilization;
    private boolean isWillingToMate;
    private int wealth;

    /** Last player to trade with this villager, used for aggressivity. */
    private String lastBuyingPlayer;
    private int careerId;

    /** This is the EntityVillager's career level value */
    private int careerLevel;
    private boolean isLookingForHome;
    private boolean areAdditionalTasksSet;
    private final InventoryBasic villagerInventory;

    /**
     * A multi-dimensional array mapping the various professions, careers and career levels that a Villager may offer
     */
    private static final EntityVillager.ITradeList[][][][] DEFAULT_TRADE_LIST_MAP = new EntityVillager.ITradeList[][][][] {{{{new EntityVillager.EmeraldForItems(Items.wheat, new EntityVillager.PriceInfo(18, 22)), new EntityVillager.EmeraldForItems(Items.potato, new EntityVillager.PriceInfo(15, 19)), new EntityVillager.EmeraldForItems(Items.carrot, new EntityVillager.PriceInfo(15, 19)), new EntityVillager.ListItemForEmeralds(Items.bread, new EntityVillager.PriceInfo(-4, -2))}, {new EntityVillager.EmeraldForItems(Item.getItemFromBlock(Blocks.pumpkin), new EntityVillager.PriceInfo(8, 13)), new EntityVillager.ListItemForEmeralds(Items.pumpkin_pie, new EntityVillager.PriceInfo(-3, -2))}, {new EntityVillager.EmeraldForItems(Item.getItemFromBlock(Blocks.melon_block), new EntityVillager.PriceInfo(7, 12)), new EntityVillager.ListItemForEmeralds(Items.apple, new EntityVillager.PriceInfo(-5, -7))}, {new EntityVillager.ListItemForEmeralds(Items.cookie, new EntityVillager.PriceInfo(-6, -10)), new EntityVillager.ListItemForEmeralds(Items.cake, new EntityVillager.PriceInfo(1, 1))}}, {{new EntityVillager.EmeraldForItems(Items.string, new EntityVillager.PriceInfo(15, 20)), new EntityVillager.EmeraldForItems(Items.coal, new EntityVillager.PriceInfo(16, 24)), new EntityVillager.ItemAndEmeraldToItem(Items.fish, new EntityVillager.PriceInfo(6, 6), Items.cooked_fish, new EntityVillager.PriceInfo(6, 6))}, {new EntityVillager.ListEnchantedItemForEmeralds(Items.fishing_rod, new EntityVillager.PriceInfo(7, 8))}}, {{new EntityVillager.EmeraldForItems(Item.getItemFromBlock(Blocks.wool), new EntityVillager.PriceInfo(16, 22)), new EntityVillager.ListItemForEmeralds(Items.shears, new EntityVillager.PriceInfo(3, 4))}, {new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 0), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 1), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 2), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 3), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 4), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 5), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 6), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 7), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 8), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 9), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 10), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 11), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 12), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 13), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 14), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 15), new EntityVillager.PriceInfo(1, 2))}}, {{new EntityVillager.EmeraldForItems(Items.string, new EntityVillager.PriceInfo(15, 20)), new EntityVillager.ListItemForEmeralds(Items.arrow, new EntityVillager.PriceInfo(-12, -8))}, {new EntityVillager.ListItemForEmeralds(Items.bow, new EntityVillager.PriceInfo(2, 3)), new EntityVillager.ItemAndEmeraldToItem(Item.getItemFromBlock(Blocks.gravel), new EntityVillager.PriceInfo(10, 10), Items.flint, new EntityVillager.PriceInfo(6, 10))}}}, {{{new EntityVillager.EmeraldForItems(Items.paper, new EntityVillager.PriceInfo(24, 36)), new EntityVillager.ListEnchantedBookForEmeralds()}, {new EntityVillager.EmeraldForItems(Items.book, new EntityVillager.PriceInfo(8, 10)), new EntityVillager.ListItemForEmeralds(Items.compass, new EntityVillager.PriceInfo(10, 12)), new EntityVillager.ListItemForEmeralds(Item.getItemFromBlock(Blocks.bookshelf), new EntityVillager.PriceInfo(3, 4))}, {new EntityVillager.EmeraldForItems(Items.written_book, new EntityVillager.PriceInfo(2, 2)), new EntityVillager.ListItemForEmeralds(Items.clock, new EntityVillager.PriceInfo(10, 12)), new EntityVillager.ListItemForEmeralds(Item.getItemFromBlock(Blocks.glass), new EntityVillager.PriceInfo(-5, -3))}, {new EntityVillager.ListEnchantedBookForEmeralds()}, {new EntityVillager.ListEnchantedBookForEmeralds()}, {new EntityVillager.ListItemForEmeralds(Items.name_tag, new EntityVillager.PriceInfo(20, 22))}}}, {{{new EntityVillager.EmeraldForItems(Items.rotten_flesh, new EntityVillager.PriceInfo(36, 40)), new EntityVillager.EmeraldForItems(Items.gold_ingot, new EntityVillager.PriceInfo(8, 10))}, {new EntityVillager.ListItemForEmeralds(Items.redstone, new EntityVillager.PriceInfo(-4, -1)), new EntityVillager.ListItemForEmeralds(new ItemStack(Items.dye, 1, EnumDyeColor.BLUE.getDyeDamage()), new EntityVillager.PriceInfo(-2, -1))}, {new EntityVillager.ListItemForEmeralds(Items.ender_eye, new EntityVillager.PriceInfo(7, 11)), new EntityVillager.ListItemForEmeralds(Item.getItemFromBlock(Blocks.glowstone), new EntityVillager.PriceInfo(-3, -1))}, {new EntityVillager.ListItemForEmeralds(Items.experience_bottle, new EntityVillager.PriceInfo(3, 11))}}}, {{{new EntityVillager.EmeraldForItems(Items.coal, new EntityVillager.PriceInfo(16, 24)), new EntityVillager.ListItemForEmeralds(Items.iron_helmet, new EntityVillager.PriceInfo(4, 6))}, {new EntityVillager.EmeraldForItems(Items.iron_ingot, new EntityVillager.PriceInfo(7, 9)), new EntityVillager.ListItemForEmeralds(Items.iron_chestplate, new EntityVillager.PriceInfo(10, 14))}, {new EntityVillager.EmeraldForItems(Items.diamond, new EntityVillager.PriceInfo(3, 4)), new EntityVillager.ListEnchantedItemForEmeralds(Items.diamond_chestplate, new EntityVillager.PriceInfo(16, 19))}, {new EntityVillager.ListItemForEmeralds(Items.chainmail_boots, new EntityVillager.PriceInfo(5, 7)), new EntityVillager.ListItemForEmeralds(Items.chainmail_leggings, new EntityVillager.PriceInfo(9, 11)), new EntityVillager.ListItemForEmeralds(Items.chainmail_helmet, new EntityVillager.PriceInfo(5, 7)), new EntityVillager.ListItemForEmeralds(Items.chainmail_chestplate, new EntityVillager.PriceInfo(11, 15))}}, {{new EntityVillager.EmeraldForItems(Items.coal, new EntityVillager.PriceInfo(16, 24)), new EntityVillager.ListItemForEmeralds(Items.iron_axe, new EntityVillager.PriceInfo(6, 8))}, {new EntityVillager.EmeraldForItems(Items.iron_ingot, new EntityVillager.PriceInfo(7, 9)), new EntityVillager.ListEnchantedItemForEmeralds(Items.iron_sword, new EntityVillager.PriceInfo(9, 10))}, {new EntityVillager.EmeraldForItems(Items.diamond, new EntityVillager.PriceInfo(3, 4)), new EntityVillager.ListEnchantedItemForEmeralds(Items.diamond_sword, new EntityVillager.PriceInfo(12, 15)), new EntityVillager.ListEnchantedItemForEmeralds(Items.diamond_axe, new EntityVillager.PriceInfo(9, 12))}}, {{new EntityVillager.EmeraldForItems(Items.coal, new EntityVillager.PriceInfo(16, 24)), new EntityVillager.ListEnchantedItemForEmeralds(Items.iron_shovel, new EntityVillager.PriceInfo(5, 7))}, {new EntityVillager.EmeraldForItems(Items.iron_ingot, new EntityVillager.PriceInfo(7, 9)), new EntityVillager.ListEnchantedItemForEmeralds(Items.iron_pickaxe, new EntityVillager.PriceInfo(9, 11))}, {new EntityVillager.EmeraldForItems(Items.diamond, new EntityVillager.PriceInfo(3, 4)), new EntityVillager.ListEnchantedItemForEmeralds(Items.diamond_pickaxe, new EntityVillager.PriceInfo(12, 15))}}}, {{{new EntityVillager.EmeraldForItems(Items.porkchop, new EntityVillager.PriceInfo(14, 18)), new EntityVillager.EmeraldForItems(Items.chicken, new EntityVillager.PriceInfo(14, 18))}, {new EntityVillager.EmeraldForItems(Items.coal, new EntityVillager.PriceInfo(16, 24)), new EntityVillager.ListItemForEmeralds(Items.cooked_porkchop, new EntityVillager.PriceInfo(-7, -5)), new EntityVillager.ListItemForEmeralds(Items.cooked_chicken, new EntityVillager.PriceInfo(-8, -6))}}, {{new EntityVillager.EmeraldForItems(Items.leather, new EntityVillager.PriceInfo(9, 12)), new EntityVillager.ListItemForEmeralds(Items.leather_leggings, new EntityVillager.PriceInfo(2, 4))}, {new EntityVillager.ListEnchantedItemForEmeralds(Items.leather_chestplate, new EntityVillager.PriceInfo(7, 12))}, {new EntityVillager.ListItemForEmeralds(Items.saddle, new EntityVillager.PriceInfo(8, 10))}}}};

    public EntityVillager(World worldIn)
    {
        this(worldIn, 0);
    }

    public EntityVillager(World worldIn, int professionId)
    {
        super(worldIn);
        villagerInventory = new InventoryBasic("Items", false, 8);
        setProfession(professionId);
        setSize(0.6F, 1.8F);
        ((PathNavigateGround) getNavigator()).setBreakDoors(true);
        ((PathNavigateGround) getNavigator()).setAvoidsWater(true);
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIAvoidEntity(this, EntityZombie.class, 8.0F, 0.6D, 0.6D));
        tasks.addTask(1, new EntityAITradePlayer(this));
        tasks.addTask(1, new EntityAILookAtTradePlayer(this));
        tasks.addTask(2, new EntityAIMoveIndoors(this));
        tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
        tasks.addTask(4, new EntityAIOpenDoor(this, true));
        tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6D));
        tasks.addTask(6, new EntityAIVillagerMate(this));
        tasks.addTask(7, new EntityAIFollowGolem(this));
        tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        tasks.addTask(9, new EntityAIVillagerInteract(this));
        tasks.addTask(9, new EntityAIWander(this, 0.6D));
        tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
        setCanPickUpLoot(true);
    }

    private void setAdditionalAItasks()
    {
        if (!areAdditionalTasksSet)
        {
            areAdditionalTasksSet = true;

            if (isChild())
            {
                tasks.addTask(8, new EntityAIPlay(this, 0.32D));
            }
            else if (getProfession() == 0)
            {
                tasks.addTask(6, new EntityAIHarvestFarmland(this, 0.6D));
            }
        }
    }

    /**
     * This is called when Entity's growing age timer reaches 0 (negative values are considered as a child, positive as
     * an adult)
     */
    protected void onGrowingAdult()
    {
        if (getProfession() == 0)
        {
            tasks.addTask(8, new EntityAIHarvestFarmland(this, 0.6D));
        }

        super.onGrowingAdult();
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.5D);
    }

    protected void updateAITasks()
    {
        if (--randomTickDivider <= 0)
        {
            BlockPos blockpos = new BlockPos(this);
            worldObj.getVillageCollection().addToVillagerPositionList(blockpos);
            randomTickDivider = 70 + rand.nextInt(50);
            villageObj = worldObj.getVillageCollection().getNearestVillage(blockpos, 32);

            if (villageObj == null)
            {
                detachHome();
            }
            else
            {
                BlockPos blockpos1 = villageObj.getCenter();
                setHomePosAndDistance(blockpos1, (int)((float) villageObj.getVillageRadius() * 1.0F));

                if (isLookingForHome)
                {
                    isLookingForHome = false;
                    villageObj.setDefaultPlayerReputation(5);
                }
            }
        }

        if (!isTrading() && timeUntilReset > 0)
        {
            --timeUntilReset;

            if (timeUntilReset <= 0)
            {
                if (needsInitilization)
                {
                    for (MerchantRecipe merchantrecipe : buyingList)
                    {
                        if (merchantrecipe.isRecipeDisabled())
                        {
                            merchantrecipe.increaseMaxTradeUses(rand.nextInt(6) + rand.nextInt(6) + 2);
                        }
                    }

                    populateBuyingList();
                    needsInitilization = false;

                    if (villageObj != null && lastBuyingPlayer != null)
                    {
                        worldObj.setEntityState(this, (byte)14);
                        villageObj.setReputationForPlayer(lastBuyingPlayer, 1);
                    }
                }

                addPotionEffect(new PotionEffect(Potion.regeneration.id, 200, 0));
            }
        }

        super.updateAITasks();
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    public boolean interact(EntityPlayer player)
    {
        ItemStack itemstack = player.inventory.getCurrentItem();
        boolean flag = itemstack != null && itemstack.getItem() == Items.spawn_egg;

        if (!flag && isEntityAlive() && !isTrading() && !isChild())
        {
            if (!worldObj.isRemote && (buyingList == null || buyingList.size() > 0))
            {
                setCustomer(player);
                player.displayVillagerTradeGui(this);
            }

            player.triggerAchievement(StatList.timesTalkedToVillagerStat);
            return true;
        }
        else
        {
            return super.interact(player);
        }
    }

    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(16, Integer.valueOf(0));
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("Profession", getProfession());
        tagCompound.setInteger("Riches", wealth);
        tagCompound.setInteger("Career", careerId);
        tagCompound.setInteger("CareerLevel", careerLevel);
        tagCompound.setBoolean("Willing", isWillingToMate);

        if (buyingList != null)
        {
            tagCompound.setTag("Offers", buyingList.getRecipiesAsTags());
        }

        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < villagerInventory.getSizeInventory(); ++i)
        {
            ItemStack itemstack = villagerInventory.getStackInSlot(i);

            if (itemstack != null)
            {
                nbttaglist.appendTag(itemstack.writeToNBT(new NBTTagCompound()));
            }
        }

        tagCompound.setTag("Inventory", nbttaglist);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        setProfession(tagCompund.getInteger("Profession"));
        wealth = tagCompund.getInteger("Riches");
        careerId = tagCompund.getInteger("Career");
        careerLevel = tagCompund.getInteger("CareerLevel");
        isWillingToMate = tagCompund.getBoolean("Willing");

        if (tagCompund.hasKey("Offers", 10))
        {
            NBTTagCompound nbttagcompound = tagCompund.getCompoundTag("Offers");
            buyingList = new MerchantRecipeList(nbttagcompound);
        }

        NBTTagList nbttaglist = tagCompund.getTagList("Inventory", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i));

            if (itemstack != null)
            {
                villagerInventory.func_174894_a(itemstack);
            }
        }

        setCanPickUpLoot(true);
        setAdditionalAItasks();
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    protected boolean canDespawn()
    {
        return false;
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    protected String getLivingSound()
    {
        return isTrading() ? "mob.villager.haggle" : "mob.villager.idle";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return "mob.villager.hit";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        return "mob.villager.death";
    }

    public void setProfession(int professionId)
    {
        dataWatcher.updateObject(16, Integer.valueOf(professionId));
    }

    public int getProfession()
    {
        return Math.max(dataWatcher.getWatchableObjectInt(16) % 5, 0);
    }

    public boolean isMating()
    {
        return isMating;
    }

    public void setMating(boolean mating)
    {
        isMating = mating;
    }

    public void setPlaying(boolean playing)
    {
        isPlaying = playing;
    }

    public boolean isPlaying()
    {
        return isPlaying;
    }

    public void setRevengeTarget(EntityLivingBase livingBase)
    {
        super.setRevengeTarget(livingBase);

        if (villageObj != null && livingBase != null)
        {
            villageObj.addOrRenewAgressor(livingBase);

            if (livingBase instanceof EntityPlayer)
            {
                int i = -1;

                if (isChild())
                {
                    i = -3;
                }

                villageObj.setReputationForPlayer(livingBase.getName(), i);

                if (isEntityAlive())
                {
                    worldObj.setEntityState(this, (byte)13);
                }
            }
        }
    }

    /**
     * Called when the mob's health reaches 0.
     */
    public void onDeath(DamageSource cause)
    {
        if (villageObj != null)
        {
            Entity entity = cause.getEntity();

            if (entity != null)
            {
                if (entity instanceof EntityPlayer)
                {
                    villageObj.setReputationForPlayer(entity.getName(), -2);
                }
                else if (entity instanceof IMob)
                {
                    villageObj.endMatingSeason();
                }
            }
            else
            {
                EntityPlayer entityplayer = worldObj.getClosestPlayerToEntity(this, 16.0D);

                if (entityplayer != null)
                {
                    villageObj.endMatingSeason();
                }
            }
        }

        super.onDeath(cause);
    }

    public void setCustomer(EntityPlayer p_70932_1_)
    {
        buyingPlayer = p_70932_1_;
    }

    public EntityPlayer getCustomer()
    {
        return buyingPlayer;
    }

    public boolean isTrading()
    {
        return buyingPlayer != null;
    }

    /**
     * Returns current or updated value of {@link #isWillingToMate}
     */
    public boolean getIsWillingToMate(boolean updateFirst)
    {
        if (!isWillingToMate && updateFirst && func_175553_cp())
        {
            boolean flag = false;

            for (int i = 0; i < villagerInventory.getSizeInventory(); ++i)
            {
                ItemStack itemstack = villagerInventory.getStackInSlot(i);

                if (itemstack != null)
                {
                    if (itemstack.getItem() == Items.bread && itemstack.stackSize >= 3)
                    {
                        flag = true;
                        villagerInventory.decrStackSize(i, 3);
                    }
                    else if ((itemstack.getItem() == Items.potato || itemstack.getItem() == Items.carrot) && itemstack.stackSize >= 12)
                    {
                        flag = true;
                        villagerInventory.decrStackSize(i, 12);
                    }
                }

                if (flag)
                {
                    worldObj.setEntityState(this, (byte)18);
                    isWillingToMate = true;
                    break;
                }
            }
        }

        return isWillingToMate;
    }

    public void setIsWillingToMate(boolean willingToTrade)
    {
        isWillingToMate = willingToTrade;
    }

    public void useRecipe(MerchantRecipe recipe)
    {
        recipe.incrementToolUses();
        livingSoundTime = -getTalkInterval();
        playSound("mob.villager.yes", getSoundVolume(), getSoundPitch());
        int i = 3 + rand.nextInt(4);

        if (recipe.getToolUses() == 1 || rand.nextInt(5) == 0)
        {
            timeUntilReset = 40;
            needsInitilization = true;
            isWillingToMate = true;

            if (buyingPlayer != null)
            {
                lastBuyingPlayer = buyingPlayer.getName();
            }
            else
            {
                lastBuyingPlayer = null;
            }

            i += 5;
        }

        if (recipe.getItemToBuy().getItem() == Items.emerald)
        {
            wealth += recipe.getItemToBuy().stackSize;
        }

        if (recipe.getRewardsExp())
        {
            worldObj.spawnEntityInWorld(new EntityXPOrb(worldObj, posX, posY + 0.5D, posZ, i));
        }
    }

    /**
     * Notifies the merchant of a possible merchantrecipe being fulfilled or not. Usually, this is just a sound byte
     * being played depending if the suggested itemstack is not null.
     */
    public void verifySellingItem(ItemStack stack)
    {
        if (!worldObj.isRemote && livingSoundTime > -getTalkInterval() + 20)
        {
            livingSoundTime = -getTalkInterval();

            if (stack != null)
            {
                playSound("mob.villager.yes", getSoundVolume(), getSoundPitch());
            }
            else
            {
                playSound("mob.villager.no", getSoundVolume(), getSoundPitch());
            }
        }
    }

    public MerchantRecipeList getRecipes(EntityPlayer p_70934_1_)
    {
        if (buyingList == null)
        {
            populateBuyingList();
        }

        return buyingList;
    }

    private void populateBuyingList()
    {
        EntityVillager.ITradeList[][][] aentityvillager$itradelist = EntityVillager.DEFAULT_TRADE_LIST_MAP[getProfession()];

        if (careerId != 0 && careerLevel != 0)
        {
            ++careerLevel;
        }
        else
        {
            careerId = rand.nextInt(aentityvillager$itradelist.length) + 1;
            careerLevel = 1;
        }

        if (buyingList == null)
        {
            buyingList = new MerchantRecipeList();
        }

        int i = careerId - 1;
        int j = careerLevel - 1;
        EntityVillager.ITradeList[][] aentityvillager$itradelist1 = aentityvillager$itradelist[i];

        if (j >= 0 && j < aentityvillager$itradelist1.length)
        {
            EntityVillager.ITradeList[] aentityvillager$itradelist2 = aentityvillager$itradelist1[j];

            for (EntityVillager.ITradeList entityvillager$itradelist : aentityvillager$itradelist2)
            {
                entityvillager$itradelist.modifyMerchantRecipeList(buyingList, rand);
            }
        }
    }

    public void setRecipes(MerchantRecipeList recipeList)
    {
    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    public IChatComponent getDisplayName()
    {
        String s = getCustomNameTag();

        if (s != null && s.length() > 0)
        {
            ChatComponentText chatcomponenttext = new ChatComponentText(s);
            chatcomponenttext.getChatStyle().setChatHoverEvent(getHoverEvent());
            chatcomponenttext.getChatStyle().setInsertion(getUniqueID().toString());
            return chatcomponenttext;
        }
        else
        {
            if (buyingList == null)
            {
                populateBuyingList();
            }

            String s1 = null;

            switch (getProfession())
            {
                case 0:
                    if (careerId == 1)
                    {
                        s1 = "farmer";
                    }
                    else if (careerId == 2)
                    {
                        s1 = "fisherman";
                    }
                    else if (careerId == 3)
                    {
                        s1 = "shepherd";
                    }
                    else if (careerId == 4)
                    {
                        s1 = "fletcher";
                    }

                    break;

                case 1:
                    s1 = "librarian";
                    break;

                case 2:
                    s1 = "cleric";
                    break;

                case 3:
                    if (careerId == 1)
                    {
                        s1 = "armor";
                    }
                    else if (careerId == 2)
                    {
                        s1 = "weapon";
                    }
                    else if (careerId == 3)
                    {
                        s1 = "tool";
                    }

                    break;

                case 4:
                    if (careerId == 1)
                    {
                        s1 = "butcher";
                    }
                    else if (careerId == 2)
                    {
                        s1 = "leather";
                    }
            }

            if (s1 != null)
            {
                ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("entity.Villager." + s1);
                chatcomponenttranslation.getChatStyle().setChatHoverEvent(getHoverEvent());
                chatcomponenttranslation.getChatStyle().setInsertion(getUniqueID().toString());
                return chatcomponenttranslation;
            }
            else
            {
                return super.getDisplayName();
            }
        }
    }

    public float getEyeHeight()
    {
        float f = 1.62F;

        if (isChild())
        {
            f = (float)((double)f - 0.81D);
        }

        return f;
    }

    public void handleStatusUpdate(byte id)
    {
        if (id == 12)
        {
            spawnParticles(EnumParticleTypes.HEART);
        }
        else if (id == 13)
        {
            spawnParticles(EnumParticleTypes.VILLAGER_ANGRY);
        }
        else if (id == 14)
        {
            spawnParticles(EnumParticleTypes.VILLAGER_HAPPY);
        }
        else
        {
            super.handleStatusUpdate(id);
        }
    }

    private void spawnParticles(EnumParticleTypes particleType)
    {
        for (int i = 0; i < 5; ++i)
        {
            double d0 = rand.nextGaussian() * 0.02D;
            double d1 = rand.nextGaussian() * 0.02D;
            double d2 = rand.nextGaussian() * 0.02D;
            worldObj.spawnParticle(particleType, posX + (double)(rand.nextFloat() * width * 2.0F) - (double) width, posY + 1.0D + (double)(rand.nextFloat() * height), posZ + (double)(rand.nextFloat() * width * 2.0F) - (double) width, d0, d1, d2);
        }
    }

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata)
    {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        setProfession(worldObj.rand.nextInt(5));
        setAdditionalAItasks();
        return livingdata;
    }

    public void setLookingForHome()
    {
        isLookingForHome = true;
    }

    public EntityVillager createChild(EntityAgeable ageable)
    {
        EntityVillager entityvillager = new EntityVillager(worldObj);
        entityvillager.onInitialSpawn(worldObj.getDifficultyForLocation(new BlockPos(entityvillager)), null);
        return entityvillager;
    }

    public boolean allowLeashing()
    {
        return false;
    }

    /**
     * Called when a lightning bolt hits the entity.
     */
    public void onStruckByLightning(EntityLightningBolt lightningBolt)
    {
        if (!worldObj.isRemote && !isDead)
        {
            EntityWitch entitywitch = new EntityWitch(worldObj);
            entitywitch.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
            entitywitch.onInitialSpawn(worldObj.getDifficultyForLocation(new BlockPos(entitywitch)), null);
            entitywitch.setNoAI(isAIDisabled());

            if (hasCustomName())
            {
                entitywitch.setCustomNameTag(getCustomNameTag());
                entitywitch.setAlwaysRenderNameTag(getAlwaysRenderNameTag());
            }

            worldObj.spawnEntityInWorld(entitywitch);
            setDead();
        }
    }

    public InventoryBasic getVillagerInventory()
    {
        return villagerInventory;
    }

    /**
     * Tests if this entity should pickup a weapon or an armor. Entity drops current weapon or armor if the new one is
     * better.
     */
    protected void updateEquipmentIfNeeded(EntityItem itemEntity)
    {
        ItemStack itemstack = itemEntity.getEntityItem();
        Item item = itemstack.getItem();

        if (canVillagerPickupItem(item))
        {
            ItemStack itemstack1 = villagerInventory.func_174894_a(itemstack);

            if (itemstack1 == null)
            {
                itemEntity.setDead();
            }
            else
            {
                itemstack.stackSize = itemstack1.stackSize;
            }
        }
    }

    private boolean canVillagerPickupItem(Item itemIn)
    {
        return itemIn == Items.bread || itemIn == Items.potato || itemIn == Items.carrot || itemIn == Items.wheat || itemIn == Items.wheat_seeds;
    }

    public boolean func_175553_cp()
    {
        return hasEnoughItems(1);
    }

    /**
     * Used by {@link net.minecraft.entity.ai.EntityAIVillagerInteract EntityAIVillagerInteract} to check if the
     * villager can give some items from an inventory to another villager.
     */
    public boolean canAbondonItems()
    {
        return hasEnoughItems(2);
    }

    public boolean func_175557_cr()
    {
        boolean flag = getProfession() == 0;
        return flag ? !hasEnoughItems(5) : !hasEnoughItems(1);
    }

    /**
     * Returns true if villager has enough items in inventory
     */
    private boolean hasEnoughItems(int multiplier)
    {
        boolean flag = getProfession() == 0;

        for (int i = 0; i < villagerInventory.getSizeInventory(); ++i)
        {
            ItemStack itemstack = villagerInventory.getStackInSlot(i);

            if (itemstack != null)
            {
                if (itemstack.getItem() == Items.bread && itemstack.stackSize >= 3 * multiplier || itemstack.getItem() == Items.potato && itemstack.stackSize >= 12 * multiplier || itemstack.getItem() == Items.carrot && itemstack.stackSize >= 12 * multiplier)
                {
                    return true;
                }

                if (flag && itemstack.getItem() == Items.wheat && itemstack.stackSize >= 9 * multiplier)
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns true if villager has seeds, potatoes or carrots in inventory
     */
    public boolean isFarmItemInInventory()
    {
        for (int i = 0; i < villagerInventory.getSizeInventory(); ++i)
        {
            ItemStack itemstack = villagerInventory.getStackInSlot(i);

            if (itemstack != null && (itemstack.getItem() == Items.wheat_seeds || itemstack.getItem() == Items.potato || itemstack.getItem() == Items.carrot))
            {
                return true;
            }
        }

        return false;
    }

    public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn)
    {
        if (super.replaceItemInInventory(inventorySlot, itemStackIn))
        {
            return true;
        }
        else
        {
            int i = inventorySlot - 300;

            if (i >= 0 && i < villagerInventory.getSizeInventory())
            {
                villagerInventory.setInventorySlotContents(i, itemStackIn);
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    static class EmeraldForItems implements EntityVillager.ITradeList
    {
        public Item sellItem;
        public EntityVillager.PriceInfo price;

        public EmeraldForItems(Item itemIn, EntityVillager.PriceInfo priceIn)
        {
            sellItem = itemIn;
            price = priceIn;
        }

        public void modifyMerchantRecipeList(MerchantRecipeList recipeList, Random random)
        {
            int i = 1;

            if (price != null)
            {
                i = price.getPrice(random);
            }

            recipeList.add(new MerchantRecipe(new ItemStack(sellItem, i, 0), Items.emerald));
        }
    }

    interface ITradeList
    {
        void modifyMerchantRecipeList(MerchantRecipeList recipeList, Random random);
    }

    static class ItemAndEmeraldToItem implements EntityVillager.ITradeList
    {
        public ItemStack field_179411_a;
        public EntityVillager.PriceInfo field_179409_b;
        public ItemStack field_179410_c;
        public EntityVillager.PriceInfo field_179408_d;

        public ItemAndEmeraldToItem(Item p_i45813_1_, EntityVillager.PriceInfo p_i45813_2_, Item p_i45813_3_, EntityVillager.PriceInfo p_i45813_4_)
        {
            field_179411_a = new ItemStack(p_i45813_1_);
            field_179409_b = p_i45813_2_;
            field_179410_c = new ItemStack(p_i45813_3_);
            field_179408_d = p_i45813_4_;
        }

        public void modifyMerchantRecipeList(MerchantRecipeList recipeList, Random random)
        {
            int i = 1;

            if (field_179409_b != null)
            {
                i = field_179409_b.getPrice(random);
            }

            int j = 1;

            if (field_179408_d != null)
            {
                j = field_179408_d.getPrice(random);
            }

            recipeList.add(new MerchantRecipe(new ItemStack(field_179411_a.getItem(), i, field_179411_a.getMetadata()), new ItemStack(Items.emerald), new ItemStack(field_179410_c.getItem(), j, field_179410_c.getMetadata())));
        }
    }

    static class ListEnchantedBookForEmeralds implements EntityVillager.ITradeList
    {
        public void modifyMerchantRecipeList(MerchantRecipeList recipeList, Random random)
        {
            Enchantment enchantment = Enchantment.enchantmentsBookList[random.nextInt(Enchantment.enchantmentsBookList.length)];
            int i = MathHelper.getRandomIntegerInRange(random, enchantment.getMinLevel(), enchantment.getMaxLevel());
            ItemStack itemstack = Items.enchanted_book.getEnchantedItemStack(new EnchantmentData(enchantment, i));
            int j = 2 + random.nextInt(5 + i * 10) + 3 * i;

            if (j > 64)
            {
                j = 64;
            }

            recipeList.add(new MerchantRecipe(new ItemStack(Items.book), new ItemStack(Items.emerald, j), itemstack));
        }
    }

    static class ListEnchantedItemForEmeralds implements EntityVillager.ITradeList
    {
        public ItemStack field_179407_a;
        public EntityVillager.PriceInfo field_179406_b;

        public ListEnchantedItemForEmeralds(Item p_i45814_1_, EntityVillager.PriceInfo p_i45814_2_)
        {
            field_179407_a = new ItemStack(p_i45814_1_);
            field_179406_b = p_i45814_2_;
        }

        public void modifyMerchantRecipeList(MerchantRecipeList recipeList, Random random)
        {
            int i = 1;

            if (field_179406_b != null)
            {
                i = field_179406_b.getPrice(random);
            }

            ItemStack itemstack = new ItemStack(Items.emerald, i, 0);
            ItemStack itemstack1 = new ItemStack(field_179407_a.getItem(), 1, field_179407_a.getMetadata());
            itemstack1 = EnchantmentHelper.addRandomEnchantment(random, itemstack1, 5 + random.nextInt(15));
            recipeList.add(new MerchantRecipe(itemstack, itemstack1));
        }
    }

    static class ListItemForEmeralds implements EntityVillager.ITradeList
    {
        public ItemStack field_179403_a;
        public EntityVillager.PriceInfo field_179402_b;

        public ListItemForEmeralds(Item par1Item, EntityVillager.PriceInfo priceInfo)
        {
            field_179403_a = new ItemStack(par1Item);
            field_179402_b = priceInfo;
        }

        public ListItemForEmeralds(ItemStack stack, EntityVillager.PriceInfo priceInfo)
        {
            field_179403_a = stack;
            field_179402_b = priceInfo;
        }

        public void modifyMerchantRecipeList(MerchantRecipeList recipeList, Random random)
        {
            int i = 1;

            if (field_179402_b != null)
            {
                i = field_179402_b.getPrice(random);
            }

            ItemStack itemstack;
            ItemStack itemstack1;

            if (i < 0)
            {
                itemstack = new ItemStack(Items.emerald, 1, 0);
                itemstack1 = new ItemStack(field_179403_a.getItem(), -i, field_179403_a.getMetadata());
            }
            else
            {
                itemstack = new ItemStack(Items.emerald, i, 0);
                itemstack1 = new ItemStack(field_179403_a.getItem(), 1, field_179403_a.getMetadata());
            }

            recipeList.add(new MerchantRecipe(itemstack, itemstack1));
        }
    }

    static class PriceInfo extends Tuple<Integer, Integer>
    {
        public PriceInfo(int p_i45810_1_, int p_i45810_2_)
        {
            super(Integer.valueOf(p_i45810_1_), Integer.valueOf(p_i45810_2_));
        }

        public int getPrice(Random rand)
        {
            return getFirst().intValue() >= getSecond().intValue() ? getFirst().intValue() : getFirst().intValue() + rand.nextInt(getSecond().intValue() - getFirst().intValue() + 1);
        }
    }
}
