package net.minecraft.tileentity;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;

public class TileEntityBeacon extends TileEntityLockable implements ITickable, IInventory
{
    /** List of effects that Beacon can apply */
    public static final Potion[][] effectsList = new Potion[][] {{Potion.moveSpeed, Potion.digSpeed}, {Potion.resistance, Potion.jump}, {Potion.damageBoost}, {Potion.regeneration}};
    private final List<TileEntityBeacon.BeamSegment> beamSegments = Lists.newArrayList();
    private long beamRenderCounter;
    private float field_146014_j;
    private boolean isComplete;

    /** Level of this beacon's pyramid. */
    private int levels = -1;

    /** Primary potion effect given by this beacon. */
    private int primaryEffect;

    /** Secondary potion effect given by this beacon. */
    private int secondaryEffect;

    /** Item given to this beacon as payment. */
    private ItemStack payment;
    private String customName;

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update()
    {
        if (worldObj.getTotalWorldTime() % 80L == 0L)
        {
            updateBeacon();
        }
    }

    public void updateBeacon()
    {
        updateSegmentColors();
        addEffectsToPlayers();
    }

    private void addEffectsToPlayers()
    {
        if (isComplete && levels > 0 && !worldObj.isRemote && primaryEffect > 0)
        {
            double d0 = levels * 10 + 10;
            int i = 0;

            if (levels >= 4 && primaryEffect == secondaryEffect)
            {
                i = 1;
            }

            int j = pos.getX();
            int k = pos.getY();
            int l = pos.getZ();
            AxisAlignedBB axisalignedbb = (new AxisAlignedBB(j, k, l, j + 1, k + 1, l + 1)).expand(d0, d0, d0).addCoord(0.0D, worldObj.getHeight(), 0.0D);
            List<EntityPlayer> list = worldObj.getEntitiesWithinAABB(EntityPlayer.class, axisalignedbb);

            for (EntityPlayer entityplayer : list)
            {
                entityplayer.addPotionEffect(new PotionEffect(primaryEffect, 180, i, true, true));
            }

            if (levels >= 4 && primaryEffect != secondaryEffect && secondaryEffect > 0)
            {
                for (EntityPlayer entityplayer1 : list)
                {
                    entityplayer1.addPotionEffect(new PotionEffect(secondaryEffect, 180, 0, true, true));
                }
            }
        }
    }

    private void updateSegmentColors()
    {
        int i = levels;
        int j = pos.getX();
        int k = pos.getY();
        int l = pos.getZ();
        levels = 0;
        beamSegments.clear();
        isComplete = true;
        TileEntityBeacon.BeamSegment tileentitybeacon$beamsegment = new TileEntityBeacon.BeamSegment(EntitySheep.func_175513_a(EnumDyeColor.WHITE));
        beamSegments.add(tileentitybeacon$beamsegment);
        boolean flag = true;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int i1 = k + 1; i1 < 256; ++i1)
        {
            IBlockState iblockstate = worldObj.getBlockState(blockpos$mutableblockpos.func_181079_c(j, i1, l));
            float[] afloat;

            if (iblockstate.getBlock() == Blocks.stained_glass)
            {
                afloat = EntitySheep.func_175513_a(iblockstate.getValue(BlockStainedGlass.COLOR));
            }
            else
            {
                if (iblockstate.getBlock() != Blocks.stained_glass_pane)
                {
                    if (iblockstate.getBlock().getLightOpacity() >= 15 && iblockstate.getBlock() != Blocks.bedrock)
                    {
                        isComplete = false;
                        beamSegments.clear();
                        break;
                    }

                    tileentitybeacon$beamsegment.incrementHeight();
                    continue;
                }

                afloat = EntitySheep.func_175513_a(iblockstate.getValue(BlockStainedGlassPane.COLOR));
            }

            if (!flag)
            {
                afloat = new float[] {(tileentitybeacon$beamsegment.getColors()[0] + afloat[0]) / 2.0F, (tileentitybeacon$beamsegment.getColors()[1] + afloat[1]) / 2.0F, (tileentitybeacon$beamsegment.getColors()[2] + afloat[2]) / 2.0F};
            }

            if (Arrays.equals(afloat, tileentitybeacon$beamsegment.getColors()))
            {
                tileentitybeacon$beamsegment.incrementHeight();
            }
            else
            {
                tileentitybeacon$beamsegment = new TileEntityBeacon.BeamSegment(afloat);
                beamSegments.add(tileentitybeacon$beamsegment);
            }

            flag = false;
        }

        if (isComplete)
        {
            for (int l1 = 1; l1 <= 4; levels = l1++)
            {
                int i2 = k - l1;

                if (i2 < 0)
                {
                    break;
                }

                boolean flag1 = true;

                for (int j1 = j - l1; j1 <= j + l1 && flag1; ++j1)
                {
                    for (int k1 = l - l1; k1 <= l + l1; ++k1)
                    {
                        Block block = worldObj.getBlockState(new BlockPos(j1, i2, k1)).getBlock();

                        if (block != Blocks.emerald_block && block != Blocks.gold_block && block != Blocks.diamond_block && block != Blocks.iron_block)
                        {
                            flag1 = false;
                            break;
                        }
                    }
                }

                if (!flag1)
                {
                    break;
                }
            }

            if (levels == 0)
            {
                isComplete = false;
            }
        }

        if (!worldObj.isRemote && levels == 4 && i < levels)
        {
            for (EntityPlayer entityplayer : worldObj.getEntitiesWithinAABB(EntityPlayer.class, (new AxisAlignedBB(j, k, l, j, k - 4, l)).expand(10.0D, 5.0D, 10.0D)))
            {
                entityplayer.triggerAchievement(AchievementList.fullBeacon);
            }
        }
    }

    public List<TileEntityBeacon.BeamSegment> getBeamSegments()
    {
        return beamSegments;
    }

    public float shouldBeamRender()
    {
        if (!isComplete)
        {
            return 0.0F;
        }
        else
        {
            int i = (int)(worldObj.getTotalWorldTime() - beamRenderCounter);
            beamRenderCounter = worldObj.getTotalWorldTime();

            if (i > 1)
            {
                field_146014_j -= (float)i / 40.0F;

                if (field_146014_j < 0.0F)
                {
                    field_146014_j = 0.0F;
                }
            }

            field_146014_j += 0.025F;

            if (field_146014_j > 1.0F)
            {
                field_146014_j = 1.0F;
            }

            return field_146014_j;
        }
    }

    /**
     * Allows for a specialized description packet to be created. This is often used to sync tile entity data from the
     * server to the client easily. For example this is used by signs to synchronise the text to be displayed.
     */
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(pos, 3, nbttagcompound);
    }

    public double getMaxRenderDistanceSquared()
    {
        return 65536.0D;
    }

    private int func_183001_h(int p_183001_1_)
    {
        if (p_183001_1_ >= 0 && p_183001_1_ < Potion.potionTypes.length && Potion.potionTypes[p_183001_1_] != null)
        {
            Potion potion = Potion.potionTypes[p_183001_1_];
            return potion != Potion.moveSpeed && potion != Potion.digSpeed && potion != Potion.resistance && potion != Potion.jump && potion != Potion.damageBoost && potion != Potion.regeneration ? 0 : p_183001_1_;
        }
        else
        {
            return 0;
        }
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        primaryEffect = func_183001_h(compound.getInteger("Primary"));
        secondaryEffect = func_183001_h(compound.getInteger("Secondary"));
        levels = compound.getInteger("Levels");
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger("Primary", primaryEffect);
        compound.setInteger("Secondary", secondaryEffect);
        compound.setInteger("Levels", levels);
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSizeInventory()
    {
        return 1;
    }

    /**
     * Returns the stack in the given slot.
     */
    public ItemStack getStackInSlot(int index)
    {
        return index == 0 ? payment : null;
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    public ItemStack decrStackSize(int index, int count)
    {
        if (index == 0 && payment != null)
        {
            if (count >= payment.stackSize)
            {
                ItemStack itemstack = payment;
                payment = null;
                return itemstack;
            }
            else
            {
                payment.stackSize -= count;
                return new ItemStack(payment.getItem(), count, payment.getMetadata());
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    public ItemStack removeStackFromSlot(int index)
    {
        if (index == 0 && payment != null)
        {
            ItemStack itemstack = payment;
            payment = null;
            return itemstack;
        }
        else
        {
            return null;
        }
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (index == 0)
        {
            payment = stack;
        }
    }

    /**
     * Gets the name of this command sender (usually username, but possibly "Rcon")
     */
    public String getName()
    {
        return hasCustomName() ? customName : "container.beacon";
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName()
    {
        return customName != null && customName.length() > 0;
    }

    public void setName(String name)
    {
        customName = name;
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    public int getInventoryStackLimit()
    {
        return 1;
    }

    /**
     * Do not make give this method the name canInteractWith because it clashes with Container
     */
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return worldObj.getTileEntity(pos) == this && player.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D;
    }

    public void openInventory(EntityPlayer player)
    {
    }

    public void closeInventory(EntityPlayer player)
    {
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     */
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return stack.getItem() == Items.emerald || stack.getItem() == Items.diamond || stack.getItem() == Items.gold_ingot || stack.getItem() == Items.iron_ingot;
    }

    public String getGuiID()
    {
        return "minecraft:beacon";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        return new ContainerBeacon(playerInventory, this);
    }

    public int getField(int id)
    {
        switch (id)
        {
            case 0:
                return levels;

            case 1:
                return primaryEffect;

            case 2:
                return secondaryEffect;

            default:
                return 0;
        }
    }

    public void setField(int id, int value)
    {
        switch (id)
        {
            case 0:
                levels = value;
                break;

            case 1:
                primaryEffect = func_183001_h(value);
                break;

            case 2:
                secondaryEffect = func_183001_h(value);
        }
    }

    public int getFieldCount()
    {
        return 3;
    }

    public void clear()
    {
        payment = null;
    }

    public boolean receiveClientEvent(int id, int type)
    {
        if (id == 1)
        {
            updateBeacon();
            return true;
        }
        else
        {
            return super.receiveClientEvent(id, type);
        }
    }

    public static class BeamSegment
    {
        private final float[] colors;
        private int height;

        public BeamSegment(float[] p_i45669_1_)
        {
            colors = p_i45669_1_;
            height = 1;
        }

        protected void incrementHeight()
        {
            ++height;
        }

        public float[] getColors()
        {
            return colors;
        }

        public int getHeight()
        {
            return height;
        }
    }
}
