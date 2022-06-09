package net.minecraft.entity.item;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityFallingBlock extends Entity
{
    private IBlockState fallTile;
    public int fallTime;
    public boolean shouldDropItem = true;
    private boolean canSetAsBlock;
    private boolean hurtEntities;
    private int fallHurtMax = 40;
    private float fallHurtAmount = 2.0F;
    public NBTTagCompound tileEntityData;

    public EntityFallingBlock(World worldIn)
    {
        super(worldIn);
    }

    public EntityFallingBlock(World worldIn, double x, double y, double z, IBlockState fallingBlockState)
    {
        super(worldIn);
        fallTile = fallingBlockState;
        preventEntitySpawning = true;
        setSize(0.98F, 0.98F);
        setPosition(x, y, z);
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;
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
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean canBeCollidedWith()
    {
        return !isDead;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        Block block = fallTile.getBlock();

        if (block.getMaterial() == Material.air)
        {
            setDead();
        }
        else
        {
            prevPosX = posX;
            prevPosY = posY;
            prevPosZ = posZ;

            if (fallTime++ == 0)
            {
                BlockPos blockpos = new BlockPos(this);

                if (worldObj.getBlockState(blockpos).getBlock() == block)
                {
                    worldObj.setBlockToAir(blockpos);
                }
                else if (!worldObj.isRemote)
                {
                    setDead();
                    return;
                }
            }

            motionY -= 0.03999999910593033D;
            moveEntity(motionX, motionY, motionZ);
            motionX *= 0.9800000190734863D;
            motionY *= 0.9800000190734863D;
            motionZ *= 0.9800000190734863D;

            if (!worldObj.isRemote)
            {
                BlockPos blockpos1 = new BlockPos(this);

                if (onGround)
                {
                    motionX *= 0.699999988079071D;
                    motionZ *= 0.699999988079071D;
                    motionY *= -0.5D;

                    if (worldObj.getBlockState(blockpos1).getBlock() != Blocks.piston_extension)
                    {
                        setDead();

                        if (!canSetAsBlock)
                        {
                            if (worldObj.canBlockBePlaced(block, blockpos1, true, EnumFacing.UP, null, null) && !BlockFalling.canFallInto(worldObj, blockpos1.down()) && worldObj.setBlockState(blockpos1, fallTile, 3))
                            {
                                if (block instanceof BlockFalling)
                                {
                                    ((BlockFalling)block).onEndFalling(worldObj, blockpos1);
                                }

                                if (tileEntityData != null && block instanceof ITileEntityProvider)
                                {
                                    TileEntity tileentity = worldObj.getTileEntity(blockpos1);

                                    if (tileentity != null)
                                    {
                                        NBTTagCompound nbttagcompound = new NBTTagCompound();
                                        tileentity.writeToNBT(nbttagcompound);

                                        for (String s : tileEntityData.getKeySet())
                                        {
                                            NBTBase nbtbase = tileEntityData.getTag(s);

                                            if (!s.equals("x") && !s.equals("y") && !s.equals("z"))
                                            {
                                                nbttagcompound.setTag(s, nbtbase.copy());
                                            }
                                        }

                                        tileentity.readFromNBT(nbttagcompound);
                                        tileentity.markDirty();
                                    }
                                }
                            }
                            else if (shouldDropItem && worldObj.getGameRules().getBoolean("doEntityDrops"))
                            {
                                entityDropItem(new ItemStack(block, 1, block.damageDropped(fallTile)), 0.0F);
                            }
                        }
                    }
                }
                else if (fallTime > 100 && !worldObj.isRemote && (blockpos1.getY() < 1 || blockpos1.getY() > 256) || fallTime > 600)
                {
                    if (shouldDropItem && worldObj.getGameRules().getBoolean("doEntityDrops"))
                    {
                        entityDropItem(new ItemStack(block, 1, block.damageDropped(fallTile)), 0.0F);
                    }

                    setDead();
                }
            }
        }
    }

    public void fall(float distance, float damageMultiplier)
    {
        Block block = fallTile.getBlock();

        if (hurtEntities)
        {
            int i = MathHelper.ceiling_float_int(distance - 1.0F);

            if (i > 0)
            {
                List<Entity> list = Lists.newArrayList(worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox()));
                boolean flag = block == Blocks.anvil;
                DamageSource damagesource = flag ? DamageSource.anvil : DamageSource.fallingBlock;

                for (Entity entity : list)
                {
                    entity.attackEntityFrom(damagesource, (float)Math.min(MathHelper.floor_float((float)i * fallHurtAmount), fallHurtMax));
                }

                if (flag && (double) rand.nextFloat() < 0.05000000074505806D + (double)i * 0.05D)
                {
                    int j = fallTile.getValue(BlockAnvil.DAMAGE).intValue();
                    ++j;

                    if (j > 2)
                    {
                        canSetAsBlock = true;
                    }
                    else
                    {
                        fallTile = fallTile.withProperty(BlockAnvil.DAMAGE, Integer.valueOf(j));
                    }
                }
            }
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        Block block = fallTile != null ? fallTile.getBlock() : Blocks.air;
        ResourceLocation resourcelocation = Block.blockRegistry.getNameForObject(block);
        tagCompound.setString("Block", resourcelocation == null ? "" : resourcelocation.toString());
        tagCompound.setByte("Data", (byte)block.getMetaFromState(fallTile));
        tagCompound.setByte("Time", (byte) fallTime);
        tagCompound.setBoolean("DropItem", shouldDropItem);
        tagCompound.setBoolean("HurtEntities", hurtEntities);
        tagCompound.setFloat("FallHurtAmount", fallHurtAmount);
        tagCompound.setInteger("FallHurtMax", fallHurtMax);

        if (tileEntityData != null)
        {
            tagCompound.setTag("TileEntityData", tileEntityData);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        int i = tagCompund.getByte("Data") & 255;

        if (tagCompund.hasKey("Block", 8))
        {
            fallTile = Block.getBlockFromName(tagCompund.getString("Block")).getStateFromMeta(i);
        }
        else if (tagCompund.hasKey("TileID", 99))
        {
            fallTile = Block.getBlockById(tagCompund.getInteger("TileID")).getStateFromMeta(i);
        }
        else
        {
            fallTile = Block.getBlockById(tagCompund.getByte("Tile") & 255).getStateFromMeta(i);
        }

        fallTime = tagCompund.getByte("Time") & 255;
        Block block = fallTile.getBlock();

        if (tagCompund.hasKey("HurtEntities", 99))
        {
            hurtEntities = tagCompund.getBoolean("HurtEntities");
            fallHurtAmount = tagCompund.getFloat("FallHurtAmount");
            fallHurtMax = tagCompund.getInteger("FallHurtMax");
        }
        else if (block == Blocks.anvil)
        {
            hurtEntities = true;
        }

        if (tagCompund.hasKey("DropItem", 99))
        {
            shouldDropItem = tagCompund.getBoolean("DropItem");
        }

        if (tagCompund.hasKey("TileEntityData", 10))
        {
            tileEntityData = tagCompund.getCompoundTag("TileEntityData");
        }

        if (block == null || block.getMaterial() == Material.air)
        {
            fallTile = Blocks.sand.getDefaultState();
        }
    }

    public World getWorldObj()
    {
        return worldObj;
    }

    public void setHurtEntities(boolean p_145806_1_)
    {
        hurtEntities = p_145806_1_;
    }

    /**
     * Return whether this entity should be rendered as on fire.
     */
    public boolean canRenderOnFire()
    {
        return false;
    }

    public void addEntityCrashInfo(CrashReportCategory category)
    {
        super.addEntityCrashInfo(category);

        if (fallTile != null)
        {
            Block block = fallTile.getBlock();
            category.addCrashSection("Immitating block ID", Integer.valueOf(Block.getIdFromBlock(block)));
            category.addCrashSection("Immitating block data", Integer.valueOf(block.getMetaFromState(fallTile)));
        }
    }

    public IBlockState getBlock()
    {
        return fallTile;
    }
}
