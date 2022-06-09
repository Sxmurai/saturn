package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.util.concurrent.Futures;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import net.minecraft.block.material.Material;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.client.C11PacketEnchantItem;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.client.C18PacketSpectate;
import net.minecraft.network.play.client.C19PacketResourcePackStatus;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.network.play.server.S3APacketTabComplete;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.stats.AchievementList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetHandlerPlayServer implements INetHandlerPlayServer, ITickable
{
    private static final Logger logger = LogManager.getLogger();
    public final NetworkManager netManager;
    private final MinecraftServer serverController;
    public EntityPlayerMP playerEntity;
    private int networkTickCount;
    private int field_175090_f;

    /**
     * Used to keep track of how the player is floating while gamerules should prevent that. Surpassing 80 ticks means
     * kick
     */
    private int floatingTickCount;
    private boolean field_147366_g;
    private int field_147378_h;
    private long lastPingTime;
    private long lastSentPingPacket;

    /**
     * Incremented by 20 each time a user sends a chat message, decreased by one every tick. Non-ops kicked when over
     * 200
     */
    private int chatSpamThresholdCount;
    private int itemDropThreshold;
    private final IntHashMap<Short> field_147372_n = new IntHashMap();
    private double lastPosX;
    private double lastPosY;
    private double lastPosZ;
    private boolean hasMoved = true;

    public NetHandlerPlayServer(MinecraftServer server, NetworkManager networkManagerIn, EntityPlayerMP playerIn)
    {
        serverController = server;
        netManager = networkManagerIn;
        networkManagerIn.setNetHandler(this);
        playerEntity = playerIn;
        playerIn.playerNetServerHandler = this;
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update()
    {
        field_147366_g = false;
        ++networkTickCount;
        serverController.theProfiler.startSection("keepAlive");

        if ((long) networkTickCount - lastSentPingPacket > 40L)
        {
            lastSentPingPacket = networkTickCount;
            lastPingTime = currentTimeMillis();
            field_147378_h = (int) lastPingTime;
            sendPacket(new S00PacketKeepAlive(field_147378_h));
        }

        serverController.theProfiler.endSection();

        if (chatSpamThresholdCount > 0)
        {
            --chatSpamThresholdCount;
        }

        if (itemDropThreshold > 0)
        {
            --itemDropThreshold;
        }

        if (playerEntity.getLastActiveTime() > 0L && serverController.getMaxPlayerIdleMinutes() > 0 && MinecraftServer.getCurrentTimeMillis() - playerEntity.getLastActiveTime() > (long)(serverController.getMaxPlayerIdleMinutes() * 1000 * 60))
        {
            kickPlayerFromServer("You have been idle for too long!");
        }
    }

    public NetworkManager getNetworkManager()
    {
        return netManager;
    }

    /**
     * Kick a player from the server with a reason
     */
    public void kickPlayerFromServer(String reason)
    {
        final ChatComponentText chatcomponenttext = new ChatComponentText(reason);
        netManager.sendPacket(new S40PacketDisconnect(chatcomponenttext), new GenericFutureListener < Future <? super Void >> ()
        {
            public void operationComplete(Future <? super Void > p_operationComplete_1_) throws Exception
            {
                netManager.closeChannel(chatcomponenttext);
            }
        });
        netManager.disableAutoRead();
        Futures.getUnchecked(serverController.addScheduledTask(new Runnable()
        {
            public void run()
            {
                netManager.checkDisconnected();
            }
        }));
    }

    /**
     * Processes player movement input. Includes walking, strafing, jumping, sneaking; excludes riding and toggling
     * flying/sprinting
     */
    public void processInput(C0CPacketInput packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());
        playerEntity.setEntityActionState(packetIn.getStrafeSpeed(), packetIn.getForwardSpeed(), packetIn.isJumping(), packetIn.isSneaking());
    }

    private boolean func_183006_b(C03PacketPlayer p_183006_1_)
    {
        return !Doubles.isFinite(p_183006_1_.getPositionX()) || !Doubles.isFinite(p_183006_1_.getPositionY()) || !Doubles.isFinite(p_183006_1_.getPositionZ()) || !Floats.isFinite(p_183006_1_.getPitch()) || !Floats.isFinite(p_183006_1_.getYaw());
    }

    /**
     * Processes clients perspective on player positioning and/or orientation
     */
    public void processPlayer(C03PacketPlayer packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());

        if (func_183006_b(packetIn))
        {
            kickPlayerFromServer("Invalid move packet received");
        }
        else
        {
            WorldServer worldserver = serverController.worldServerForDimension(playerEntity.dimension);
            field_147366_g = true;

            if (!playerEntity.playerConqueredTheEnd)
            {
                double d0 = playerEntity.posX;
                double d1 = playerEntity.posY;
                double d2 = playerEntity.posZ;
                double d3 = 0.0D;
                double d4 = packetIn.getPositionX() - lastPosX;
                double d5 = packetIn.getPositionY() - lastPosY;
                double d6 = packetIn.getPositionZ() - lastPosZ;

                if (packetIn.isMoving())
                {
                    d3 = d4 * d4 + d5 * d5 + d6 * d6;

                    if (!hasMoved && d3 < 0.25D)
                    {
                        hasMoved = true;
                    }
                }

                if (hasMoved)
                {
                    field_175090_f = networkTickCount;

                    if (playerEntity.ridingEntity != null)
                    {
                        float f4 = playerEntity.rotationYaw;
                        float f = playerEntity.rotationPitch;
                        playerEntity.ridingEntity.updateRiderPosition();
                        double d16 = playerEntity.posX;
                        double d17 = playerEntity.posY;
                        double d18 = playerEntity.posZ;

                        if (packetIn.getRotating())
                        {
                            f4 = packetIn.getYaw();
                            f = packetIn.getPitch();
                        }

                        playerEntity.onGround = packetIn.isOnGround();
                        playerEntity.onUpdateEntity();
                        playerEntity.setPositionAndRotation(d16, d17, d18, f4, f);

                        if (playerEntity.ridingEntity != null)
                        {
                            playerEntity.ridingEntity.updateRiderPosition();
                        }

                        serverController.getConfigurationManager().serverUpdateMountedMovingPlayer(playerEntity);

                        if (playerEntity.ridingEntity != null)
                        {
                            if (d3 > 4.0D)
                            {
                                Entity entity = playerEntity.ridingEntity;
                                playerEntity.playerNetServerHandler.sendPacket(new S18PacketEntityTeleport(entity));
                                setPlayerLocation(playerEntity.posX, playerEntity.posY, playerEntity.posZ, playerEntity.rotationYaw, playerEntity.rotationPitch);
                            }

                            playerEntity.ridingEntity.isAirBorne = true;
                        }

                        if (hasMoved)
                        {
                            lastPosX = playerEntity.posX;
                            lastPosY = playerEntity.posY;
                            lastPosZ = playerEntity.posZ;
                        }

                        worldserver.updateEntity(playerEntity);
                        return;
                    }

                    if (playerEntity.isPlayerSleeping())
                    {
                        playerEntity.onUpdateEntity();
                        playerEntity.setPositionAndRotation(lastPosX, lastPosY, lastPosZ, playerEntity.rotationYaw, playerEntity.rotationPitch);
                        worldserver.updateEntity(playerEntity);
                        return;
                    }

                    double d7 = playerEntity.posY;
                    lastPosX = playerEntity.posX;
                    lastPosY = playerEntity.posY;
                    lastPosZ = playerEntity.posZ;
                    double d8 = playerEntity.posX;
                    double d9 = playerEntity.posY;
                    double d10 = playerEntity.posZ;
                    float f1 = playerEntity.rotationYaw;
                    float f2 = playerEntity.rotationPitch;

                    if (packetIn.isMoving() && packetIn.getPositionY() == -999.0D)
                    {
                        packetIn.setMoving(false);
                    }

                    if (packetIn.isMoving())
                    {
                        d8 = packetIn.getPositionX();
                        d9 = packetIn.getPositionY();
                        d10 = packetIn.getPositionZ();

                        if (Math.abs(packetIn.getPositionX()) > 3.0E7D || Math.abs(packetIn.getPositionZ()) > 3.0E7D)
                        {
                            kickPlayerFromServer("Illegal position");
                            return;
                        }
                    }

                    if (packetIn.getRotating())
                    {
                        f1 = packetIn.getYaw();
                        f2 = packetIn.getPitch();
                    }

                    playerEntity.onUpdateEntity();
                    playerEntity.setPositionAndRotation(lastPosX, lastPosY, lastPosZ, f1, f2);

                    if (!hasMoved)
                    {
                        return;
                    }

                    double d11 = d8 - playerEntity.posX;
                    double d12 = d9 - playerEntity.posY;
                    double d13 = d10 - playerEntity.posZ;
                    double d14 = playerEntity.motionX * playerEntity.motionX + playerEntity.motionY * playerEntity.motionY + playerEntity.motionZ * playerEntity.motionZ;
                    double d15 = d11 * d11 + d12 * d12 + d13 * d13;

                    if (d15 - d14 > 100.0D && (!serverController.isSinglePlayer() || !serverController.getServerOwner().equals(playerEntity.getName())))
                    {
                        NetHandlerPlayServer.logger.warn(playerEntity.getName() + " moved too quickly! " + d11 + "," + d12 + "," + d13 + " (" + d11 + ", " + d12 + ", " + d13 + ")");
                        setPlayerLocation(lastPosX, lastPosY, lastPosZ, playerEntity.rotationYaw, playerEntity.rotationPitch);
                        return;
                    }

                    float f3 = 0.0625F;
                    boolean flag = worldserver.getCollidingBoundingBoxes(playerEntity, playerEntity.getEntityBoundingBox().contract(f3, f3, f3)).isEmpty();

                    if (playerEntity.onGround && !packetIn.isOnGround() && d12 > 0.0D)
                    {
                        playerEntity.jump();
                    }

                    playerEntity.moveEntity(d11, d12, d13);
                    playerEntity.onGround = packetIn.isOnGround();
                    d11 = d8 - playerEntity.posX;
                    d12 = d9 - playerEntity.posY;

                    if (d12 > -0.5D || d12 < 0.5D)
                    {
                        d12 = 0.0D;
                    }

                    d13 = d10 - playerEntity.posZ;
                    d15 = d11 * d11 + d12 * d12 + d13 * d13;
                    boolean flag1 = false;

                    if (d15 > 0.0625D && !playerEntity.isPlayerSleeping() && !playerEntity.theItemInWorldManager.isCreative())
                    {
                        flag1 = true;
                        NetHandlerPlayServer.logger.warn(playerEntity.getName() + " moved wrongly!");
                    }

                    playerEntity.setPositionAndRotation(d8, d9, d10, f1, f2);
                    playerEntity.addMovementStat(playerEntity.posX - d0, playerEntity.posY - d1, playerEntity.posZ - d2);

                    if (!playerEntity.noClip)
                    {
                        boolean flag2 = worldserver.getCollidingBoundingBoxes(playerEntity, playerEntity.getEntityBoundingBox().contract(f3, f3, f3)).isEmpty();

                        if (flag && (flag1 || !flag2) && !playerEntity.isPlayerSleeping())
                        {
                            setPlayerLocation(lastPosX, lastPosY, lastPosZ, f1, f2);
                            return;
                        }
                    }

                    AxisAlignedBB axisalignedbb = playerEntity.getEntityBoundingBox().expand(f3, f3, f3).addCoord(0.0D, -0.55D, 0.0D);

                    if (!serverController.isFlightAllowed() && !playerEntity.capabilities.allowFlying && !worldserver.checkBlockCollision(axisalignedbb))
                    {
                        if (d12 >= -0.03125D)
                        {
                            ++floatingTickCount;

                            if (floatingTickCount > 80)
                            {
                                NetHandlerPlayServer.logger.warn(playerEntity.getName() + " was kicked for floating too long!");
                                kickPlayerFromServer("Flying is not enabled on this server");
                                return;
                            }
                        }
                    }
                    else
                    {
                        floatingTickCount = 0;
                    }

                    playerEntity.onGround = packetIn.isOnGround();
                    serverController.getConfigurationManager().serverUpdateMountedMovingPlayer(playerEntity);
                    playerEntity.handleFalling(playerEntity.posY - d7, packetIn.isOnGround());
                }
                else if (networkTickCount - field_175090_f > 20)
                {
                    setPlayerLocation(lastPosX, lastPosY, lastPosZ, playerEntity.rotationYaw, playerEntity.rotationPitch);
                }
            }
        }
    }

    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch)
    {
        setPlayerLocation(x, y, z, yaw, pitch, Collections.emptySet());
    }

    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set<S08PacketPlayerPosLook.EnumFlags> relativeSet)
    {
        hasMoved = false;
        lastPosX = x;
        lastPosY = y;
        lastPosZ = z;

        if (relativeSet.contains(S08PacketPlayerPosLook.EnumFlags.X))
        {
            lastPosX += playerEntity.posX;
        }

        if (relativeSet.contains(S08PacketPlayerPosLook.EnumFlags.Y))
        {
            lastPosY += playerEntity.posY;
        }

        if (relativeSet.contains(S08PacketPlayerPosLook.EnumFlags.Z))
        {
            lastPosZ += playerEntity.posZ;
        }

        float f = yaw;
        float f1 = pitch;

        if (relativeSet.contains(S08PacketPlayerPosLook.EnumFlags.Y_ROT))
        {
            f = yaw + playerEntity.rotationYaw;
        }

        if (relativeSet.contains(S08PacketPlayerPosLook.EnumFlags.X_ROT))
        {
            f1 = pitch + playerEntity.rotationPitch;
        }

        playerEntity.setPositionAndRotation(lastPosX, lastPosY, lastPosZ, f, f1);
        playerEntity.playerNetServerHandler.sendPacket(new S08PacketPlayerPosLook(x, y, z, yaw, pitch, relativeSet));
    }

    /**
     * Processes the player initiating/stopping digging on a particular spot, as well as a player dropping items?. (0:
     * initiated, 1: reinitiated, 2? , 3-4 drop item (respectively without or with player control), 5: stopped; x,y,z,
     * side clicked on;)
     */
    public void processPlayerDigging(C07PacketPlayerDigging packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());
        WorldServer worldserver = serverController.worldServerForDimension(playerEntity.dimension);
        BlockPos blockpos = packetIn.getPosition();
        playerEntity.markPlayerActive();

        switch (packetIn.getStatus())
        {
            case DROP_ITEM:
                if (!playerEntity.isSpectator())
                {
                    playerEntity.dropOneItem(false);
                }

                return;

            case DROP_ALL_ITEMS:
                if (!playerEntity.isSpectator())
                {
                    playerEntity.dropOneItem(true);
                }

                return;

            case RELEASE_USE_ITEM:
                playerEntity.stopUsingItem();
                return;

            case START_DESTROY_BLOCK:
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
                double d0 = playerEntity.posX - ((double)blockpos.getX() + 0.5D);
                double d1 = playerEntity.posY - ((double)blockpos.getY() + 0.5D) + 1.5D;
                double d2 = playerEntity.posZ - ((double)blockpos.getZ() + 0.5D);
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (d3 > 36.0D)
                {
                    return;
                }
                else if (blockpos.getY() >= serverController.getBuildLimit())
                {
                    return;
                }
                else
                {
                    if (packetIn.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK)
                    {
                        if (!serverController.isBlockProtected(worldserver, blockpos, playerEntity) && worldserver.getWorldBorder().contains(blockpos))
                        {
                            playerEntity.theItemInWorldManager.onBlockClicked(blockpos, packetIn.getFacing());
                        }
                        else
                        {
                            playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(worldserver, blockpos));
                        }
                    }
                    else
                    {
                        if (packetIn.getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK)
                        {
                            playerEntity.theItemInWorldManager.blockRemoving(blockpos);
                        }
                        else if (packetIn.getStatus() == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK)
                        {
                            playerEntity.theItemInWorldManager.cancelDestroyingBlock();
                        }

                        if (worldserver.getBlockState(blockpos).getBlock().getMaterial() != Material.air)
                        {
                            playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(worldserver, blockpos));
                        }
                    }

                    return;
                }

            default:
                throw new IllegalArgumentException("Invalid player action");
        }
    }

    /**
     * Processes block placement and block activation (anvil, furnace, etc.)
     */
    public void processPlayerBlockPlacement(C08PacketPlayerBlockPlacement packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());
        WorldServer worldserver = serverController.worldServerForDimension(playerEntity.dimension);
        ItemStack itemstack = playerEntity.inventory.getCurrentItem();
        boolean flag = false;
        BlockPos blockpos = packetIn.getPosition();
        EnumFacing enumfacing = EnumFacing.getFront(packetIn.getPlacedBlockDirection());
        playerEntity.markPlayerActive();

        if (packetIn.getPlacedBlockDirection() == 255)
        {
            if (itemstack == null)
            {
                return;
            }

            playerEntity.theItemInWorldManager.tryUseItem(playerEntity, worldserver, itemstack);
        }
        else if (blockpos.getY() < serverController.getBuildLimit() - 1 || enumfacing != EnumFacing.UP && blockpos.getY() < serverController.getBuildLimit())
        {
            if (hasMoved && playerEntity.getDistanceSq((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D) < 64.0D && !serverController.isBlockProtected(worldserver, blockpos, playerEntity) && worldserver.getWorldBorder().contains(blockpos))
            {
                playerEntity.theItemInWorldManager.activateBlockOrUseItem(playerEntity, worldserver, itemstack, blockpos, enumfacing, packetIn.getPlacedBlockOffsetX(), packetIn.getPlacedBlockOffsetY(), packetIn.getPlacedBlockOffsetZ());
            }

            flag = true;
        }
        else
        {
            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("build.tooHigh", Integer.valueOf(serverController.getBuildLimit()));
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            playerEntity.playerNetServerHandler.sendPacket(new S02PacketChat(chatcomponenttranslation));
            flag = true;
        }

        if (flag)
        {
            playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(worldserver, blockpos));
            playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(worldserver, blockpos.offset(enumfacing)));
        }

        itemstack = playerEntity.inventory.getCurrentItem();

        if (itemstack != null && itemstack.stackSize == 0)
        {
            playerEntity.inventory.mainInventory[playerEntity.inventory.currentItem] = null;
            itemstack = null;
        }

        if (itemstack == null || itemstack.getMaxItemUseDuration() == 0)
        {
            playerEntity.isChangingQuantityOnly = true;
            playerEntity.inventory.mainInventory[playerEntity.inventory.currentItem] = ItemStack.copyItemStack(playerEntity.inventory.mainInventory[playerEntity.inventory.currentItem]);
            Slot slot = playerEntity.openContainer.getSlotFromInventory(playerEntity.inventory, playerEntity.inventory.currentItem);
            playerEntity.openContainer.detectAndSendChanges();
            playerEntity.isChangingQuantityOnly = false;

            if (!ItemStack.areItemStacksEqual(playerEntity.inventory.getCurrentItem(), packetIn.getStack()))
            {
                sendPacket(new S2FPacketSetSlot(playerEntity.openContainer.windowId, slot.slotNumber, playerEntity.inventory.getCurrentItem()));
            }
        }
    }

    public void handleSpectate(C18PacketSpectate packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());

        if (playerEntity.isSpectator())
        {
            Entity entity = null;

            for (WorldServer worldserver : serverController.worldServers)
            {
                if (worldserver != null)
                {
                    entity = packetIn.getEntity(worldserver);

                    if (entity != null)
                    {
                        break;
                    }
                }
            }

            if (entity != null)
            {
                playerEntity.setSpectatingEntity(playerEntity);
                playerEntity.mountEntity(null);

                if (entity.worldObj != playerEntity.worldObj)
                {
                    WorldServer worldserver1 = playerEntity.getServerForPlayer();
                    WorldServer worldserver2 = (WorldServer)entity.worldObj;
                    playerEntity.dimension = entity.dimension;
                    sendPacket(new S07PacketRespawn(playerEntity.dimension, worldserver1.getDifficulty(), worldserver1.getWorldInfo().getTerrainType(), playerEntity.theItemInWorldManager.getGameType()));
                    worldserver1.removePlayerEntityDangerously(playerEntity);
                    playerEntity.isDead = false;
                    playerEntity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

                    if (playerEntity.isEntityAlive())
                    {
                        worldserver1.updateEntityWithOptionalForce(playerEntity, false);
                        worldserver2.spawnEntityInWorld(playerEntity);
                        worldserver2.updateEntityWithOptionalForce(playerEntity, false);
                    }

                    playerEntity.setWorld(worldserver2);
                    serverController.getConfigurationManager().preparePlayer(playerEntity, worldserver1);
                    playerEntity.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
                    playerEntity.theItemInWorldManager.setWorld(worldserver2);
                    serverController.getConfigurationManager().updateTimeAndWeatherForPlayer(playerEntity, worldserver2);
                    serverController.getConfigurationManager().syncPlayerInventory(playerEntity);
                }
                else
                {
                    playerEntity.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
                }
            }
        }
    }

    public void handleResourcePackStatus(C19PacketResourcePackStatus packetIn)
    {
    }

    /**
     * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
     */
    public void onDisconnect(IChatComponent reason)
    {
        NetHandlerPlayServer.logger.info(playerEntity.getName() + " lost connection: " + reason);
        serverController.refreshStatusNextTick();
        ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("multiplayer.player.left", playerEntity.getDisplayName());
        chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.YELLOW);
        serverController.getConfigurationManager().sendChatMsg(chatcomponenttranslation);
        playerEntity.mountEntityAndWakeUp();
        serverController.getConfigurationManager().playerLoggedOut(playerEntity);

        if (serverController.isSinglePlayer() && playerEntity.getName().equals(serverController.getServerOwner()))
        {
            NetHandlerPlayServer.logger.info("Stopping singleplayer server as player logged out");
            serverController.initiateShutdown();
        }
    }

    public void sendPacket(final Packet packetIn)
    {
        if (packetIn instanceof S02PacketChat)
        {
            S02PacketChat s02packetchat = (S02PacketChat)packetIn;
            EntityPlayer.EnumChatVisibility entityplayer$enumchatvisibility = playerEntity.getChatVisibility();

            if (entityplayer$enumchatvisibility == EntityPlayer.EnumChatVisibility.HIDDEN)
            {
                return;
            }

            if (entityplayer$enumchatvisibility == EntityPlayer.EnumChatVisibility.SYSTEM && !s02packetchat.isChat())
            {
                return;
            }
        }

        try
        {
            netManager.sendPacket(packetIn);
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Sending packet");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Packet being sent");
            crashreportcategory.addCrashSectionCallable("Packet class", new Callable<String>()
            {
                public String call() throws Exception
                {
                    return packetIn.getClass().getCanonicalName();
                }
            });
            throw new ReportedException(crashreport);
        }
    }

    /**
     * Updates which quickbar slot is selected
     */
    public void processHeldItemChange(C09PacketHeldItemChange packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());

        if (packetIn.getSlotId() >= 0 && packetIn.getSlotId() < InventoryPlayer.getHotbarSize())
        {
            playerEntity.inventory.currentItem = packetIn.getSlotId();
            playerEntity.markPlayerActive();
        }
        else
        {
            NetHandlerPlayServer.logger.warn(playerEntity.getName() + " tried to set an invalid carried item");
        }
    }

    /**
     * Process chat messages (broadcast back to clients) and commands (executes)
     */
    public void processChatMessage(C01PacketChatMessage packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());

        if (playerEntity.getChatVisibility() == EntityPlayer.EnumChatVisibility.HIDDEN)
        {
            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("chat.cannotSend");
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            sendPacket(new S02PacketChat(chatcomponenttranslation));
        }
        else
        {
            playerEntity.markPlayerActive();
            String s = packetIn.getMessage();
            s = StringUtils.normalizeSpace(s);

            for (int i = 0; i < s.length(); ++i)
            {
                if (!ChatAllowedCharacters.isAllowedCharacter(s.charAt(i)))
                {
                    kickPlayerFromServer("Illegal characters in chat");
                    return;
                }
            }

            if (s.startsWith("/"))
            {
                handleSlashCommand(s);
            }
            else
            {
                IChatComponent ichatcomponent = new ChatComponentTranslation("chat.type.text", playerEntity.getDisplayName(), s);
                serverController.getConfigurationManager().sendChatMsgImpl(ichatcomponent, false);
            }

            chatSpamThresholdCount += 20;

            if (chatSpamThresholdCount > 200 && !serverController.getConfigurationManager().canSendCommands(playerEntity.getGameProfile()))
            {
                kickPlayerFromServer("disconnect.spam");
            }
        }
    }

    /**
     * Handle commands that start with a /
     */
    private void handleSlashCommand(String command)
    {
        serverController.getCommandManager().executeCommand(playerEntity, command);
    }

    public void handleAnimation(C0APacketAnimation packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());
        playerEntity.markPlayerActive();
        playerEntity.swingItem();
    }

    /**
     * Processes a range of action-types: sneaking, sprinting, waking from sleep, opening the inventory or setting jump
     * height of the horse the player is riding
     */
    public void processEntityAction(C0BPacketEntityAction packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());
        playerEntity.markPlayerActive();

        switch (packetIn.getAction())
        {
            case START_SNEAKING:
                playerEntity.setSneaking(true);
                break;

            case STOP_SNEAKING:
                playerEntity.setSneaking(false);
                break;

            case START_SPRINTING:
                playerEntity.setSprinting(true);
                break;

            case STOP_SPRINTING:
                playerEntity.setSprinting(false);
                break;

            case STOP_SLEEPING:
                playerEntity.wakeUpPlayer(false, true, true);
                hasMoved = false;
                break;

            case RIDING_JUMP:
                if (playerEntity.ridingEntity instanceof EntityHorse)
                {
                    ((EntityHorse) playerEntity.ridingEntity).setJumpPower(packetIn.getAuxData());
                }

                break;

            case OPEN_INVENTORY:
                if (playerEntity.ridingEntity instanceof EntityHorse)
                {
                    ((EntityHorse) playerEntity.ridingEntity).openGUI(playerEntity);
                }

                break;

            default:
                throw new IllegalArgumentException("Invalid client command!");
        }
    }

    /**
     * Processes interactions ((un)leashing, opening command block GUI) and attacks on an entity with players currently
     * equipped item
     */
    public void processUseEntity(C02PacketUseEntity packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());
        WorldServer worldserver = serverController.worldServerForDimension(playerEntity.dimension);
        Entity entity = packetIn.getEntityFromWorld(worldserver);
        playerEntity.markPlayerActive();

        if (entity != null)
        {
            boolean flag = playerEntity.canEntityBeSeen(entity);
            double d0 = 36.0D;

            if (!flag)
            {
                d0 = 9.0D;
            }

            if (playerEntity.getDistanceSqToEntity(entity) < d0)
            {
                if (packetIn.getAction() == C02PacketUseEntity.Action.INTERACT)
                {
                    playerEntity.interactWith(entity);
                }
                else if (packetIn.getAction() == C02PacketUseEntity.Action.INTERACT_AT)
                {
                    entity.interactAt(playerEntity, packetIn.getHitVec());
                }
                else if (packetIn.getAction() == C02PacketUseEntity.Action.ATTACK)
                {
                    if (entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityArrow || entity == playerEntity)
                    {
                        kickPlayerFromServer("Attempting to attack an invalid entity");
                        serverController.logWarning("Player " + playerEntity.getName() + " tried to attack an invalid entity");
                        return;
                    }

                    playerEntity.attackTargetEntityWithCurrentItem(entity);
                }
            }
        }
    }

    /**
     * Processes the client status updates: respawn attempt from player, opening statistics or achievements, or
     * acquiring 'open inventory' achievement
     */
    public void processClientStatus(C16PacketClientStatus packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());
        playerEntity.markPlayerActive();
        C16PacketClientStatus.EnumState c16packetclientstatus$enumstate = packetIn.getStatus();

        switch (c16packetclientstatus$enumstate)
        {
            case PERFORM_RESPAWN:
                if (playerEntity.playerConqueredTheEnd)
                {
                    playerEntity = serverController.getConfigurationManager().recreatePlayerEntity(playerEntity, 0, true);
                }
                else if (playerEntity.getServerForPlayer().getWorldInfo().isHardcoreModeEnabled())
                {
                    if (serverController.isSinglePlayer() && playerEntity.getName().equals(serverController.getServerOwner()))
                    {
                        playerEntity.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it's game over!");
                        serverController.deleteWorldAndStopServer();
                    }
                    else
                    {
                        UserListBansEntry userlistbansentry = new UserListBansEntry(playerEntity.getGameProfile(), null, "(You just lost the game)", null, "Death in Hardcore");
                        serverController.getConfigurationManager().getBannedPlayers().addEntry(userlistbansentry);
                        playerEntity.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it's game over!");
                    }
                }
                else
                {
                    if (playerEntity.getHealth() > 0.0F)
                    {
                        return;
                    }

                    playerEntity = serverController.getConfigurationManager().recreatePlayerEntity(playerEntity, 0, false);
                }

                break;

            case REQUEST_STATS:
                playerEntity.getStatFile().func_150876_a(playerEntity);
                break;

            case OPEN_INVENTORY_ACHIEVEMENT:
                playerEntity.triggerAchievement(AchievementList.openInventory);
        }
    }

    /**
     * Processes the client closing windows (container)
     */
    public void processCloseWindow(C0DPacketCloseWindow packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());
        playerEntity.closeContainer();
    }

    /**
     * Executes a container/inventory slot manipulation as indicated by the packet. Sends the serverside result if they
     * didn't match the indicated result and prevents further manipulation by the player until he confirms that it has
     * the same open container/inventory
     */
    public void processClickWindow(C0EPacketClickWindow packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());
        playerEntity.markPlayerActive();

        if (playerEntity.openContainer.windowId == packetIn.getWindowId() && playerEntity.openContainer.getCanCraft(playerEntity))
        {
            if (playerEntity.isSpectator())
            {
                List<ItemStack> list = Lists.newArrayList();

                for (int i = 0; i < playerEntity.openContainer.inventorySlots.size(); ++i)
                {
                    list.add(playerEntity.openContainer.inventorySlots.get(i).getStack());
                }

                playerEntity.updateCraftingInventory(playerEntity.openContainer, list);
            }
            else
            {
                ItemStack itemstack = playerEntity.openContainer.slotClick(packetIn.getSlotId(), packetIn.getUsedButton(), packetIn.getMode(), playerEntity);

                if (ItemStack.areItemStacksEqual(packetIn.getClickedItem(), itemstack))
                {
                    playerEntity.playerNetServerHandler.sendPacket(new S32PacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), true));
                    playerEntity.isChangingQuantityOnly = true;
                    playerEntity.openContainer.detectAndSendChanges();
                    playerEntity.updateHeldItem();
                    playerEntity.isChangingQuantityOnly = false;
                }
                else
                {
                    field_147372_n.addKey(playerEntity.openContainer.windowId, Short.valueOf(packetIn.getActionNumber()));
                    playerEntity.playerNetServerHandler.sendPacket(new S32PacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), false));
                    playerEntity.openContainer.setCanCraft(playerEntity, false);
                    List<ItemStack> list1 = Lists.newArrayList();

                    for (int j = 0; j < playerEntity.openContainer.inventorySlots.size(); ++j)
                    {
                        list1.add(playerEntity.openContainer.inventorySlots.get(j).getStack());
                    }

                    playerEntity.updateCraftingInventory(playerEntity.openContainer, list1);
                }
            }
        }
    }

    /**
     * Enchants the item identified by the packet given some convoluted conditions (matching window, which
     * should/shouldn't be in use?)
     */
    public void processEnchantItem(C11PacketEnchantItem packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());
        playerEntity.markPlayerActive();

        if (playerEntity.openContainer.windowId == packetIn.getWindowId() && playerEntity.openContainer.getCanCraft(playerEntity) && !playerEntity.isSpectator())
        {
            playerEntity.openContainer.enchantItem(playerEntity, packetIn.getButton());
            playerEntity.openContainer.detectAndSendChanges();
        }
    }

    /**
     * Update the server with an ItemStack in a slot.
     */
    public void processCreativeInventoryAction(C10PacketCreativeInventoryAction packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());

        if (playerEntity.theItemInWorldManager.isCreative())
        {
            boolean flag = packetIn.getSlotId() < 0;
            ItemStack itemstack = packetIn.getStack();

            if (itemstack != null && itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("BlockEntityTag", 10))
            {
                NBTTagCompound nbttagcompound = itemstack.getTagCompound().getCompoundTag("BlockEntityTag");

                if (nbttagcompound.hasKey("x") && nbttagcompound.hasKey("y") && nbttagcompound.hasKey("z"))
                {
                    BlockPos blockpos = new BlockPos(nbttagcompound.getInteger("x"), nbttagcompound.getInteger("y"), nbttagcompound.getInteger("z"));
                    TileEntity tileentity = playerEntity.worldObj.getTileEntity(blockpos);

                    if (tileentity != null)
                    {
                        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                        tileentity.writeToNBT(nbttagcompound1);
                        nbttagcompound1.removeTag("x");
                        nbttagcompound1.removeTag("y");
                        nbttagcompound1.removeTag("z");
                        itemstack.setTagInfo("BlockEntityTag", nbttagcompound1);
                    }
                }
            }

            boolean flag1 = packetIn.getSlotId() >= 1 && packetIn.getSlotId() < 36 + InventoryPlayer.getHotbarSize();
            boolean flag2 = itemstack == null || itemstack.getItem() != null;
            boolean flag3 = itemstack == null || itemstack.getMetadata() >= 0 && itemstack.stackSize <= 64 && itemstack.stackSize > 0;

            if (flag1 && flag2 && flag3)
            {
                if (itemstack == null)
                {
                    playerEntity.inventoryContainer.putStackInSlot(packetIn.getSlotId(), null);
                }
                else
                {
                    playerEntity.inventoryContainer.putStackInSlot(packetIn.getSlotId(), itemstack);
                }

                playerEntity.inventoryContainer.setCanCraft(playerEntity, true);
            }
            else if (flag && flag2 && flag3 && itemDropThreshold < 200)
            {
                itemDropThreshold += 20;
                EntityItem entityitem = playerEntity.dropPlayerItemWithRandomChoice(itemstack, true);

                if (entityitem != null)
                {
                    entityitem.setAgeToCreativeDespawnTime();
                }
            }
        }
    }

    /**
     * Received in response to the server requesting to confirm that the client-side open container matches the servers'
     * after a mismatched container-slot manipulation. It will unlock the player's ability to manipulate the container
     * contents
     */
    public void processConfirmTransaction(C0FPacketConfirmTransaction packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());
        Short oshort = field_147372_n.lookup(playerEntity.openContainer.windowId);

        if (oshort != null && packetIn.getUid() == oshort.shortValue() && playerEntity.openContainer.windowId == packetIn.getWindowId() && !playerEntity.openContainer.getCanCraft(playerEntity) && !playerEntity.isSpectator())
        {
            playerEntity.openContainer.setCanCraft(playerEntity, true);
        }
    }

    public void processUpdateSign(C12PacketUpdateSign packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());
        playerEntity.markPlayerActive();
        WorldServer worldserver = serverController.worldServerForDimension(playerEntity.dimension);
        BlockPos blockpos = packetIn.getPosition();

        if (worldserver.isBlockLoaded(blockpos))
        {
            TileEntity tileentity = worldserver.getTileEntity(blockpos);

            if (!(tileentity instanceof TileEntitySign))
            {
                return;
            }

            TileEntitySign tileentitysign = (TileEntitySign)tileentity;

            if (!tileentitysign.getIsEditable() || tileentitysign.getPlayer() != playerEntity)
            {
                serverController.logWarning("Player " + playerEntity.getName() + " just tried to change non-editable sign");
                return;
            }

            IChatComponent[] aichatcomponent = packetIn.getLines();

            for (int i = 0; i < aichatcomponent.length; ++i)
            {
                tileentitysign.signText[i] = new ChatComponentText(EnumChatFormatting.getTextWithoutFormattingCodes(aichatcomponent[i].getUnformattedText()));
            }

            tileentitysign.markDirty();
            worldserver.markBlockForUpdate(blockpos);
        }
    }

    /**
     * Updates a players' ping statistics
     */
    public void processKeepAlive(C00PacketKeepAlive packetIn)
    {
        if (packetIn.getKey() == field_147378_h)
        {
            int i = (int)(currentTimeMillis() - lastPingTime);
            playerEntity.ping = (playerEntity.ping * 3 + i) / 4;
        }
    }

    private long currentTimeMillis()
    {
        return System.nanoTime() / 1000000L;
    }

    /**
     * Processes a player starting/stopping flying
     */
    public void processPlayerAbilities(C13PacketPlayerAbilities packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());
        playerEntity.capabilities.isFlying = packetIn.isFlying() && playerEntity.capabilities.allowFlying;
    }

    /**
     * Retrieves possible tab completions for the requested command string and sends them to the client
     */
    public void processTabComplete(C14PacketTabComplete packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());
        List<String> list = Lists.newArrayList();

        for (String s : serverController.getTabCompletions(playerEntity, packetIn.getMessage(), packetIn.getTargetBlock()))
        {
            list.add(s);
        }

        playerEntity.playerNetServerHandler.sendPacket(new S3APacketTabComplete(list.toArray(new String[list.size()])));
    }

    /**
     * Updates serverside copy of client settings: language, render distance, chat visibility, chat colours, difficulty,
     * and whether to show the cape
     */
    public void processClientSettings(C15PacketClientSettings packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());
        playerEntity.handleClientSettings(packetIn);
    }

    /**
     * Synchronizes serverside and clientside book contents and signing
     */
    public void processVanilla250Packet(C17PacketCustomPayload packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, playerEntity.getServerForPlayer());

        if ("MC|BEdit".equals(packetIn.getChannelName()))
        {
            PacketBuffer packetbuffer3 = new PacketBuffer(Unpooled.wrappedBuffer(packetIn.getBufferData()));

            try
            {
                ItemStack itemstack1 = packetbuffer3.readItemStackFromBuffer();

                if (itemstack1 != null)
                {
                    if (!ItemWritableBook.isNBTValid(itemstack1.getTagCompound()))
                    {
                        throw new IOException("Invalid book tag!");
                    }

                    ItemStack itemstack3 = playerEntity.inventory.getCurrentItem();

                    if (itemstack3 == null)
                    {
                        return;
                    }

                    if (itemstack1.getItem() == Items.writable_book && itemstack1.getItem() == itemstack3.getItem())
                    {
                        itemstack3.setTagInfo("pages", itemstack1.getTagCompound().getTagList("pages", 8));
                    }

                    return;
                }
            }
            catch (Exception exception3)
            {
                NetHandlerPlayServer.logger.error("Couldn't handle book info", exception3);
                return;
            }
            finally
            {
                packetbuffer3.release();
            }

            return;
        }
        else if ("MC|BSign".equals(packetIn.getChannelName()))
        {
            PacketBuffer packetbuffer2 = new PacketBuffer(Unpooled.wrappedBuffer(packetIn.getBufferData()));

            try
            {
                ItemStack itemstack = packetbuffer2.readItemStackFromBuffer();

                if (itemstack != null)
                {
                    if (!ItemEditableBook.validBookTagContents(itemstack.getTagCompound()))
                    {
                        throw new IOException("Invalid book tag!");
                    }

                    ItemStack itemstack2 = playerEntity.inventory.getCurrentItem();

                    if (itemstack2 == null)
                    {
                        return;
                    }

                    if (itemstack.getItem() == Items.written_book && itemstack2.getItem() == Items.writable_book)
                    {
                        itemstack2.setTagInfo("author", new NBTTagString(playerEntity.getName()));
                        itemstack2.setTagInfo("title", new NBTTagString(itemstack.getTagCompound().getString("title")));
                        itemstack2.setTagInfo("pages", itemstack.getTagCompound().getTagList("pages", 8));
                        itemstack2.setItem(Items.written_book);
                    }

                    return;
                }
            }
            catch (Exception exception4)
            {
                NetHandlerPlayServer.logger.error("Couldn't sign book", exception4);
                return;
            }
            finally
            {
                packetbuffer2.release();
            }

            return;
        }
        else if ("MC|TrSel".equals(packetIn.getChannelName()))
        {
            try
            {
                int i = packetIn.getBufferData().readInt();
                Container container = playerEntity.openContainer;

                if (container instanceof ContainerMerchant)
                {
                    ((ContainerMerchant)container).setCurrentRecipeIndex(i);
                }
            }
            catch (Exception exception2)
            {
                NetHandlerPlayServer.logger.error("Couldn't select trade", exception2);
            }
        }
        else if ("MC|AdvCdm".equals(packetIn.getChannelName()))
        {
            if (!serverController.isCommandBlockEnabled())
            {
                playerEntity.addChatMessage(new ChatComponentTranslation("advMode.notEnabled"));
            }
            else if (playerEntity.canCommandSenderUseCommand(2, "") && playerEntity.capabilities.isCreativeMode)
            {
                PacketBuffer packetbuffer = packetIn.getBufferData();

                try
                {
                    int j = packetbuffer.readByte();
                    CommandBlockLogic commandblocklogic = null;

                    if (j == 0)
                    {
                        TileEntity tileentity = playerEntity.worldObj.getTileEntity(new BlockPos(packetbuffer.readInt(), packetbuffer.readInt(), packetbuffer.readInt()));

                        if (tileentity instanceof TileEntityCommandBlock)
                        {
                            commandblocklogic = ((TileEntityCommandBlock)tileentity).getCommandBlockLogic();
                        }
                    }
                    else if (j == 1)
                    {
                        Entity entity = playerEntity.worldObj.getEntityByID(packetbuffer.readInt());

                        if (entity instanceof EntityMinecartCommandBlock)
                        {
                            commandblocklogic = ((EntityMinecartCommandBlock)entity).getCommandBlockLogic();
                        }
                    }

                    String s1 = packetbuffer.readStringFromBuffer(packetbuffer.readableBytes());
                    boolean flag = packetbuffer.readBoolean();

                    if (commandblocklogic != null)
                    {
                        commandblocklogic.setCommand(s1);
                        commandblocklogic.setTrackOutput(flag);

                        if (!flag)
                        {
                            commandblocklogic.setLastOutput(null);
                        }

                        commandblocklogic.updateCommand();
                        playerEntity.addChatMessage(new ChatComponentTranslation("advMode.setCommand.success", s1));
                    }
                }
                catch (Exception exception1)
                {
                    NetHandlerPlayServer.logger.error("Couldn't set command block", exception1);
                }
                finally
                {
                    packetbuffer.release();
                }
            }
            else
            {
                playerEntity.addChatMessage(new ChatComponentTranslation("advMode.notAllowed"));
            }
        }
        else if ("MC|Beacon".equals(packetIn.getChannelName()))
        {
            if (playerEntity.openContainer instanceof ContainerBeacon)
            {
                try
                {
                    PacketBuffer packetbuffer1 = packetIn.getBufferData();
                    int k = packetbuffer1.readInt();
                    int l = packetbuffer1.readInt();
                    ContainerBeacon containerbeacon = (ContainerBeacon) playerEntity.openContainer;
                    Slot slot = containerbeacon.getSlot(0);

                    if (slot.getHasStack())
                    {
                        slot.decrStackSize(1);
                        IInventory iinventory = containerbeacon.func_180611_e();
                        iinventory.setField(1, k);
                        iinventory.setField(2, l);
                        iinventory.markDirty();
                    }
                }
                catch (Exception exception)
                {
                    NetHandlerPlayServer.logger.error("Couldn't set beacon", exception);
                }
            }
        }
        else if ("MC|ItemName".equals(packetIn.getChannelName()) && playerEntity.openContainer instanceof ContainerRepair)
        {
            ContainerRepair containerrepair = (ContainerRepair) playerEntity.openContainer;

            if (packetIn.getBufferData() != null && packetIn.getBufferData().readableBytes() >= 1)
            {
                String s = ChatAllowedCharacters.filterAllowedCharacters(packetIn.getBufferData().readStringFromBuffer(32767));

                if (s.length() <= 30)
                {
                    containerrepair.updateItemName(s);
                }
            }
            else
            {
                containerrepair.updateItemName("");
            }
        }
    }
}
