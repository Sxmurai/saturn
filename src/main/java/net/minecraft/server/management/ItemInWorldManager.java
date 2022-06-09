package net.minecraft.server.management;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

public class ItemInWorldManager
{
    /** The world object that this object is connected to. */
    public World theWorld;

    /** The EntityPlayerMP object that this object is connected to. */
    public EntityPlayerMP thisPlayerMP;
    private WorldSettings.GameType gameType = WorldSettings.GameType.NOT_SET;

    /** True if the player is destroying a block */
    private boolean isDestroyingBlock;
    private int initialDamage;
    private BlockPos field_180240_f = BlockPos.ORIGIN;
    private int curblockDamage;

    /**
     * Set to true when the "finished destroying block" packet is received but the block wasn't fully damaged yet. The
     * block will not be destroyed while this is false.
     */
    private boolean receivedFinishDiggingPacket;
    private BlockPos field_180241_i = BlockPos.ORIGIN;
    private int initialBlockDamage;
    private int durabilityRemainingOnBlock = -1;

    public ItemInWorldManager(World worldIn)
    {
        theWorld = worldIn;
    }

    public void setGameType(WorldSettings.GameType type)
    {
        gameType = type;
        type.configurePlayerCapabilities(thisPlayerMP.capabilities);
        thisPlayerMP.sendPlayerAbilities();
        thisPlayerMP.mcServer.getConfigurationManager().sendPacketToAllPlayers(new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.UPDATE_GAME_MODE, thisPlayerMP));
    }

    public WorldSettings.GameType getGameType()
    {
        return gameType;
    }

    public boolean survivalOrAdventure()
    {
        return gameType.isSurvivalOrAdventure();
    }

    /**
     * Get if we are in creative game mode.
     */
    public boolean isCreative()
    {
        return gameType.isCreative();
    }

    /**
     * if the gameType is currently NOT_SET then change it to par1
     */
    public void initializeGameType(WorldSettings.GameType type)
    {
        if (gameType == WorldSettings.GameType.NOT_SET)
        {
            gameType = type;
        }

        setGameType(gameType);
    }

    public void updateBlockRemoving()
    {
        ++curblockDamage;

        if (receivedFinishDiggingPacket)
        {
            int i = curblockDamage - initialBlockDamage;
            Block block = theWorld.getBlockState(field_180241_i).getBlock();

            if (block.getMaterial() == Material.air)
            {
                receivedFinishDiggingPacket = false;
            }
            else
            {
                float f = block.getPlayerRelativeBlockHardness(thisPlayerMP, thisPlayerMP.worldObj, field_180241_i) * (float)(i + 1);
                int j = (int)(f * 10.0F);

                if (j != durabilityRemainingOnBlock)
                {
                    theWorld.sendBlockBreakProgress(thisPlayerMP.getEntityId(), field_180241_i, j);
                    durabilityRemainingOnBlock = j;
                }

                if (f >= 1.0F)
                {
                    receivedFinishDiggingPacket = false;
                    tryHarvestBlock(field_180241_i);
                }
            }
        }
        else if (isDestroyingBlock)
        {
            Block block1 = theWorld.getBlockState(field_180240_f).getBlock();

            if (block1.getMaterial() == Material.air)
            {
                theWorld.sendBlockBreakProgress(thisPlayerMP.getEntityId(), field_180240_f, -1);
                durabilityRemainingOnBlock = -1;
                isDestroyingBlock = false;
            }
            else
            {
                int k = curblockDamage - initialDamage;
                float f1 = block1.getPlayerRelativeBlockHardness(thisPlayerMP, thisPlayerMP.worldObj, field_180241_i) * (float)(k + 1);
                int l = (int)(f1 * 10.0F);

                if (l != durabilityRemainingOnBlock)
                {
                    theWorld.sendBlockBreakProgress(thisPlayerMP.getEntityId(), field_180240_f, l);
                    durabilityRemainingOnBlock = l;
                }
            }
        }
    }

    /**
     * If not creative, it calls sendBlockBreakProgress until the block is broken first. tryHarvestBlock can also be the
     * result of this call.
     */
    public void onBlockClicked(BlockPos pos, EnumFacing side)
    {
        if (isCreative())
        {
            if (!theWorld.extinguishFire(null, pos, side))
            {
                tryHarvestBlock(pos);
            }
        }
        else
        {
            Block block = theWorld.getBlockState(pos).getBlock();

            if (gameType.isAdventure())
            {
                if (gameType == WorldSettings.GameType.SPECTATOR)
                {
                    return;
                }

                if (!thisPlayerMP.isAllowEdit())
                {
                    ItemStack itemstack = thisPlayerMP.getCurrentEquippedItem();

                    if (itemstack == null)
                    {
                        return;
                    }

                    if (!itemstack.canDestroy(block))
                    {
                        return;
                    }
                }
            }

            theWorld.extinguishFire(null, pos, side);
            initialDamage = curblockDamage;
            float f = 1.0F;

            if (block.getMaterial() != Material.air)
            {
                block.onBlockClicked(theWorld, pos, thisPlayerMP);
                f = block.getPlayerRelativeBlockHardness(thisPlayerMP, thisPlayerMP.worldObj, pos);
            }

            if (block.getMaterial() != Material.air && f >= 1.0F)
            {
                tryHarvestBlock(pos);
            }
            else
            {
                isDestroyingBlock = true;
                field_180240_f = pos;
                int i = (int)(f * 10.0F);
                theWorld.sendBlockBreakProgress(thisPlayerMP.getEntityId(), pos, i);
                durabilityRemainingOnBlock = i;
            }
        }
    }

    public void blockRemoving(BlockPos pos)
    {
        if (pos.equals(field_180240_f))
        {
            int i = curblockDamage - initialDamage;
            Block block = theWorld.getBlockState(pos).getBlock();

            if (block.getMaterial() != Material.air)
            {
                float f = block.getPlayerRelativeBlockHardness(thisPlayerMP, thisPlayerMP.worldObj, pos) * (float)(i + 1);

                if (f >= 0.7F)
                {
                    isDestroyingBlock = false;
                    theWorld.sendBlockBreakProgress(thisPlayerMP.getEntityId(), pos, -1);
                    tryHarvestBlock(pos);
                }
                else if (!receivedFinishDiggingPacket)
                {
                    isDestroyingBlock = false;
                    receivedFinishDiggingPacket = true;
                    field_180241_i = pos;
                    initialBlockDamage = initialDamage;
                }
            }
        }
    }

    /**
     * Stops the block breaking process
     */
    public void cancelDestroyingBlock()
    {
        isDestroyingBlock = false;
        theWorld.sendBlockBreakProgress(thisPlayerMP.getEntityId(), field_180240_f, -1);
    }

    /**
     * Removes a block and triggers the appropriate events
     */
    private boolean removeBlock(BlockPos pos)
    {
        IBlockState iblockstate = theWorld.getBlockState(pos);
        iblockstate.getBlock().onBlockHarvested(theWorld, pos, iblockstate, thisPlayerMP);
        boolean flag = theWorld.setBlockToAir(pos);

        if (flag)
        {
            iblockstate.getBlock().onBlockDestroyedByPlayer(theWorld, pos, iblockstate);
        }

        return flag;
    }

    /**
     * Attempts to harvest a block
     */
    public boolean tryHarvestBlock(BlockPos pos)
    {
        if (gameType.isCreative() && thisPlayerMP.getHeldItem() != null && thisPlayerMP.getHeldItem().getItem() instanceof ItemSword)
        {
            return false;
        }
        else
        {
            IBlockState iblockstate = theWorld.getBlockState(pos);
            TileEntity tileentity = theWorld.getTileEntity(pos);

            if (gameType.isAdventure())
            {
                if (gameType == WorldSettings.GameType.SPECTATOR)
                {
                    return false;
                }

                if (!thisPlayerMP.isAllowEdit())
                {
                    ItemStack itemstack = thisPlayerMP.getCurrentEquippedItem();

                    if (itemstack == null)
                    {
                        return false;
                    }

                    if (!itemstack.canDestroy(iblockstate.getBlock()))
                    {
                        return false;
                    }
                }
            }

            theWorld.playAuxSFXAtEntity(thisPlayerMP, 2001, pos, Block.getStateId(iblockstate));
            boolean flag1 = removeBlock(pos);

            if (isCreative())
            {
                thisPlayerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(theWorld, pos));
            }
            else
            {
                ItemStack itemstack1 = thisPlayerMP.getCurrentEquippedItem();
                boolean flag = thisPlayerMP.canHarvestBlock(iblockstate.getBlock());

                if (itemstack1 != null)
                {
                    itemstack1.onBlockDestroyed(theWorld, iblockstate.getBlock(), pos, thisPlayerMP);

                    if (itemstack1.stackSize == 0)
                    {
                        thisPlayerMP.destroyCurrentEquippedItem();
                    }
                }

                if (flag1 && flag)
                {
                    iblockstate.getBlock().harvestBlock(theWorld, thisPlayerMP, pos, iblockstate, tileentity);
                }
            }

            return flag1;
        }
    }

    /**
     * Attempts to right-click use an item by the given EntityPlayer in the given World
     */
    public boolean tryUseItem(EntityPlayer player, World worldIn, ItemStack stack)
    {
        if (gameType == WorldSettings.GameType.SPECTATOR)
        {
            return false;
        }
        else
        {
            int i = stack.stackSize;
            int j = stack.getMetadata();
            ItemStack itemstack = stack.useItemRightClick(worldIn, player);

            if (itemstack != stack || itemstack != null && (itemstack.stackSize != i || itemstack.getMaxItemUseDuration() > 0 || itemstack.getMetadata() != j))
            {
                player.inventory.mainInventory[player.inventory.currentItem] = itemstack;

                if (isCreative())
                {
                    itemstack.stackSize = i;

                    if (itemstack.isItemStackDamageable())
                    {
                        itemstack.setItemDamage(j);
                    }
                }

                if (itemstack.stackSize == 0)
                {
                    player.inventory.mainInventory[player.inventory.currentItem] = null;
                }

                if (!player.isUsingItem())
                {
                    ((EntityPlayerMP)player).sendContainerToPlayer(player.inventoryContainer);
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
     * Activate the clicked on block, otherwise use the held item.
     */
    public boolean activateBlockOrUseItem(EntityPlayer player, World worldIn, ItemStack stack, BlockPos pos, EnumFacing side, float offsetX, float offsetY, float offsetZ)
    {
        if (gameType == WorldSettings.GameType.SPECTATOR)
        {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof ILockableContainer)
            {
                Block block = worldIn.getBlockState(pos).getBlock();
                ILockableContainer ilockablecontainer = (ILockableContainer)tileentity;

                if (ilockablecontainer instanceof TileEntityChest && block instanceof BlockChest)
                {
                    ilockablecontainer = ((BlockChest)block).getLockableContainer(worldIn, pos);
                }

                if (ilockablecontainer != null)
                {
                    player.displayGUIChest(ilockablecontainer);
                    return true;
                }
            }
            else if (tileentity instanceof IInventory)
            {
                player.displayGUIChest((IInventory)tileentity);
                return true;
            }

            return false;
        }
        else
        {
            if (!player.isSneaking() || player.getHeldItem() == null)
            {
                IBlockState iblockstate = worldIn.getBlockState(pos);

                if (iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, side, offsetX, offsetY, offsetZ))
                {
                    return true;
                }
            }

            if (stack == null)
            {
                return false;
            }
            else if (isCreative())
            {
                int j = stack.getMetadata();
                int i = stack.stackSize;
                boolean flag = stack.onItemUse(player, worldIn, pos, side, offsetX, offsetY, offsetZ);
                stack.setItemDamage(j);
                stack.stackSize = i;
                return flag;
            }
            else
            {
                return stack.onItemUse(player, worldIn, pos, side, offsetX, offsetY, offsetZ);
            }
        }
    }

    /**
     * Sets the world instance.
     */
    public void setWorld(WorldServer serverWorld)
    {
        theWorld = serverWorld;
    }
}
