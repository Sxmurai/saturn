package net.minecraft.world.demo;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class DemoWorldManager extends ItemInWorldManager
{
    private boolean field_73105_c;
    private boolean demoTimeExpired;
    private int field_73104_e;
    private int field_73102_f;

    public DemoWorldManager(World worldIn)
    {
        super(worldIn);
    }

    public void updateBlockRemoving()
    {
        super.updateBlockRemoving();
        ++field_73102_f;
        long i = theWorld.getTotalWorldTime();
        long j = i / 24000L + 1L;

        if (!field_73105_c && field_73102_f > 20)
        {
            field_73105_c = true;
            thisPlayerMP.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(5, 0.0F));
        }

        demoTimeExpired = i > 120500L;

        if (demoTimeExpired)
        {
            ++field_73104_e;
        }

        if (i % 24000L == 500L)
        {
            if (j <= 6L)
            {
                thisPlayerMP.addChatMessage(new ChatComponentTranslation("demo.day." + j));
            }
        }
        else if (j == 1L)
        {
            if (i == 100L)
            {
                thisPlayerMP.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(5, 101.0F));
            }
            else if (i == 175L)
            {
                thisPlayerMP.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(5, 102.0F));
            }
            else if (i == 250L)
            {
                thisPlayerMP.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(5, 103.0F));
            }
        }
        else if (j == 5L && i % 24000L == 22000L)
        {
            thisPlayerMP.addChatMessage(new ChatComponentTranslation("demo.day.warning"));
        }
    }

    /**
     * Sends a message to the player reminding them that this is the demo version
     */
    private void sendDemoReminder()
    {
        if (field_73104_e > 100)
        {
            thisPlayerMP.addChatMessage(new ChatComponentTranslation("demo.reminder"));
            field_73104_e = 0;
        }
    }

    /**
     * If not creative, it calls sendBlockBreakProgress until the block is broken first. tryHarvestBlock can also be the
     * result of this call.
     */
    public void onBlockClicked(BlockPos pos, EnumFacing side)
    {
        if (demoTimeExpired)
        {
            sendDemoReminder();
        }
        else
        {
            super.onBlockClicked(pos, side);
        }
    }

    public void blockRemoving(BlockPos pos)
    {
        if (!demoTimeExpired)
        {
            super.blockRemoving(pos);
        }
    }

    /**
     * Attempts to harvest a block
     */
    public boolean tryHarvestBlock(BlockPos pos)
    {
        return !this.demoTimeExpired && super.tryHarvestBlock(pos);
    }

    /**
     * Attempts to right-click use an item by the given EntityPlayer in the given World
     */
    public boolean tryUseItem(EntityPlayer player, World worldIn, ItemStack stack)
    {
        if (demoTimeExpired)
        {
            sendDemoReminder();
            return false;
        }
        else
        {
            return super.tryUseItem(player, worldIn, stack);
        }
    }

    /**
     * Activate the clicked on block, otherwise use the held item.
     */
    public boolean activateBlockOrUseItem(EntityPlayer player, World worldIn, ItemStack stack, BlockPos pos, EnumFacing side, float offsetX, float offsetY, float offsetZ)
    {
        if (demoTimeExpired)
        {
            sendDemoReminder();
            return false;
        }
        else
        {
            return super.activateBlockOrUseItem(player, worldIn, stack, pos, side, offsetX, offsetY, offsetZ);
        }
    }
}
