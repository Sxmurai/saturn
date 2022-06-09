package net.minecraft.entity;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.ServersideAttributeMap;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S0APacketUseBed;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S0FPacketSpawnMob;
import net.minecraft.network.play.server.S10PacketSpawnPainting;
import net.minecraft.network.play.server.S11PacketSpawnExperienceOrb;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S19PacketEntityHeadLook;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S20PacketEntityProperties;
import net.minecraft.network.play.server.S49PacketUpdateEntityNBT;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.storage.MapData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityTrackerEntry
{
    private static final Logger logger = LogManager.getLogger();

    /** The entity that this EntityTrackerEntry tracks. */
    public Entity trackedEntity;
    public int trackingDistanceThreshold;

    /** check for sync when ticks % updateFrequency==0 */
    public int updateFrequency;

    /** The encoded entity X position. */
    public int encodedPosX;

    /** The encoded entity Y position. */
    public int encodedPosY;

    /** The encoded entity Z position. */
    public int encodedPosZ;

    /** The encoded entity yaw rotation. */
    public int encodedRotationYaw;

    /** The encoded entity pitch rotation. */
    public int encodedRotationPitch;
    public int lastHeadMotion;
    public double lastTrackedEntityMotionX;
    public double lastTrackedEntityMotionY;
    public double motionZ;
    public int updateCounter;
    private double lastTrackedEntityPosX;
    private double lastTrackedEntityPosY;
    private double lastTrackedEntityPosZ;
    private boolean firstUpdateDone;
    private final boolean sendVelocityUpdates;

    /**
     * every 400 ticks a  full teleport packet is sent, rather than just a "move me +x" command, so that position
     * remains fully synced.
     */
    private int ticksSinceLastForcedTeleport;
    private Entity field_85178_v;
    private boolean ridingEntity;
    private boolean onGround;
    public boolean playerEntitiesUpdated;
    public Set<EntityPlayerMP> trackingPlayers = Sets.newHashSet();

    public EntityTrackerEntry(Entity trackedEntityIn, int trackingDistanceThresholdIn, int updateFrequencyIn, boolean sendVelocityUpdatesIn)
    {
        trackedEntity = trackedEntityIn;
        trackingDistanceThreshold = trackingDistanceThresholdIn;
        updateFrequency = updateFrequencyIn;
        sendVelocityUpdates = sendVelocityUpdatesIn;
        encodedPosX = MathHelper.floor_double(trackedEntityIn.posX * 32.0D);
        encodedPosY = MathHelper.floor_double(trackedEntityIn.posY * 32.0D);
        encodedPosZ = MathHelper.floor_double(trackedEntityIn.posZ * 32.0D);
        encodedRotationYaw = MathHelper.floor_float(trackedEntityIn.rotationYaw * 256.0F / 360.0F);
        encodedRotationPitch = MathHelper.floor_float(trackedEntityIn.rotationPitch * 256.0F / 360.0F);
        lastHeadMotion = MathHelper.floor_float(trackedEntityIn.getRotationYawHead() * 256.0F / 360.0F);
        onGround = trackedEntityIn.onGround;
    }

    public boolean equals(Object p_equals_1_)
    {
        return p_equals_1_ instanceof EntityTrackerEntry && ((EntityTrackerEntry) p_equals_1_).trackedEntity.getEntityId() == trackedEntity.getEntityId();
    }

    public int hashCode()
    {
        return trackedEntity.getEntityId();
    }

    public void updatePlayerList(List<EntityPlayer> p_73122_1_)
    {
        playerEntitiesUpdated = false;

        if (!firstUpdateDone || trackedEntity.getDistanceSq(lastTrackedEntityPosX, lastTrackedEntityPosY, lastTrackedEntityPosZ) > 16.0D)
        {
            lastTrackedEntityPosX = trackedEntity.posX;
            lastTrackedEntityPosY = trackedEntity.posY;
            lastTrackedEntityPosZ = trackedEntity.posZ;
            firstUpdateDone = true;
            playerEntitiesUpdated = true;
            updatePlayerEntities(p_73122_1_);
        }

        if (field_85178_v != trackedEntity.ridingEntity || trackedEntity.ridingEntity != null && updateCounter % 60 == 0)
        {
            field_85178_v = trackedEntity.ridingEntity;
            sendPacketToTrackedPlayers(new S1BPacketEntityAttach(0, trackedEntity, trackedEntity.ridingEntity));
        }

        if (trackedEntity instanceof EntityItemFrame && updateCounter % 10 == 0)
        {
            EntityItemFrame entityitemframe = (EntityItemFrame) trackedEntity;
            ItemStack itemstack = entityitemframe.getDisplayedItem();

            if (itemstack != null && itemstack.getItem() instanceof ItemMap)
            {
                MapData mapdata = Items.filled_map.getMapData(itemstack, trackedEntity.worldObj);

                for (EntityPlayer entityplayer : p_73122_1_)
                {
                    EntityPlayerMP entityplayermp = (EntityPlayerMP)entityplayer;
                    mapdata.updateVisiblePlayers(entityplayermp, itemstack);
                    Packet packet = Items.filled_map.createMapDataPacket(itemstack, trackedEntity.worldObj, entityplayermp);

                    if (packet != null)
                    {
                        entityplayermp.playerNetServerHandler.sendPacket(packet);
                    }
                }
            }

            sendMetadataToAllAssociatedPlayers();
        }

        if (updateCounter % updateFrequency == 0 || trackedEntity.isAirBorne || trackedEntity.getDataWatcher().hasObjectChanged())
        {
            if (trackedEntity.ridingEntity == null)
            {
                ++ticksSinceLastForcedTeleport;
                int k = MathHelper.floor_double(trackedEntity.posX * 32.0D);
                int j1 = MathHelper.floor_double(trackedEntity.posY * 32.0D);
                int k1 = MathHelper.floor_double(trackedEntity.posZ * 32.0D);
                int l1 = MathHelper.floor_float(trackedEntity.rotationYaw * 256.0F / 360.0F);
                int i2 = MathHelper.floor_float(trackedEntity.rotationPitch * 256.0F / 360.0F);
                int j2 = k - encodedPosX;
                int k2 = j1 - encodedPosY;
                int i = k1 - encodedPosZ;
                Packet packet1 = null;
                boolean flag = Math.abs(j2) >= 4 || Math.abs(k2) >= 4 || Math.abs(i) >= 4 || updateCounter % 60 == 0;
                boolean flag1 = Math.abs(l1 - encodedRotationYaw) >= 4 || Math.abs(i2 - encodedRotationPitch) >= 4;

                if (updateCounter > 0 || trackedEntity instanceof EntityArrow)
                {
                    if (j2 >= -128 && j2 < 128 && k2 >= -128 && k2 < 128 && i >= -128 && i < 128 && ticksSinceLastForcedTeleport <= 400 && !ridingEntity && onGround == trackedEntity.onGround)
                    {
                        if ((!flag || !flag1) && !(trackedEntity instanceof EntityArrow))
                        {
                            if (flag)
                            {
                                packet1 = new S14PacketEntity.S15PacketEntityRelMove(trackedEntity.getEntityId(), (byte)j2, (byte)k2, (byte)i, trackedEntity.onGround);
                            }
                            else if (flag1)
                            {
                                packet1 = new S14PacketEntity.S16PacketEntityLook(trackedEntity.getEntityId(), (byte)l1, (byte)i2, trackedEntity.onGround);
                            }
                        }
                        else
                        {
                            packet1 = new S14PacketEntity.S17PacketEntityLookMove(trackedEntity.getEntityId(), (byte)j2, (byte)k2, (byte)i, (byte)l1, (byte)i2, trackedEntity.onGround);
                        }
                    }
                    else
                    {
                        onGround = trackedEntity.onGround;
                        ticksSinceLastForcedTeleport = 0;
                        packet1 = new S18PacketEntityTeleport(trackedEntity.getEntityId(), k, j1, k1, (byte)l1, (byte)i2, trackedEntity.onGround);
                    }
                }

                if (sendVelocityUpdates)
                {
                    double d0 = trackedEntity.motionX - lastTrackedEntityMotionX;
                    double d1 = trackedEntity.motionY - lastTrackedEntityMotionY;
                    double d2 = trackedEntity.motionZ - motionZ;
                    double d3 = 0.02D;
                    double d4 = d0 * d0 + d1 * d1 + d2 * d2;

                    if (d4 > d3 * d3 || d4 > 0.0D && trackedEntity.motionX == 0.0D && trackedEntity.motionY == 0.0D && trackedEntity.motionZ == 0.0D)
                    {
                        lastTrackedEntityMotionX = trackedEntity.motionX;
                        lastTrackedEntityMotionY = trackedEntity.motionY;
                        motionZ = trackedEntity.motionZ;
                        sendPacketToTrackedPlayers(new S12PacketEntityVelocity(trackedEntity.getEntityId(), lastTrackedEntityMotionX, lastTrackedEntityMotionY, motionZ));
                    }
                }

                if (packet1 != null)
                {
                    sendPacketToTrackedPlayers(packet1);
                }

                sendMetadataToAllAssociatedPlayers();

                if (flag)
                {
                    encodedPosX = k;
                    encodedPosY = j1;
                    encodedPosZ = k1;
                }

                if (flag1)
                {
                    encodedRotationYaw = l1;
                    encodedRotationPitch = i2;
                }

                ridingEntity = false;
            }
            else
            {
                int j = MathHelper.floor_float(trackedEntity.rotationYaw * 256.0F / 360.0F);
                int i1 = MathHelper.floor_float(trackedEntity.rotationPitch * 256.0F / 360.0F);
                boolean flag2 = Math.abs(j - encodedRotationYaw) >= 4 || Math.abs(i1 - encodedRotationPitch) >= 4;

                if (flag2)
                {
                    sendPacketToTrackedPlayers(new S14PacketEntity.S16PacketEntityLook(trackedEntity.getEntityId(), (byte)j, (byte)i1, trackedEntity.onGround));
                    encodedRotationYaw = j;
                    encodedRotationPitch = i1;
                }

                encodedPosX = MathHelper.floor_double(trackedEntity.posX * 32.0D);
                encodedPosY = MathHelper.floor_double(trackedEntity.posY * 32.0D);
                encodedPosZ = MathHelper.floor_double(trackedEntity.posZ * 32.0D);
                sendMetadataToAllAssociatedPlayers();
                ridingEntity = true;
            }

            int l = MathHelper.floor_float(trackedEntity.getRotationYawHead() * 256.0F / 360.0F);

            if (Math.abs(l - lastHeadMotion) >= 4)
            {
                sendPacketToTrackedPlayers(new S19PacketEntityHeadLook(trackedEntity, (byte)l));
                lastHeadMotion = l;
            }

            trackedEntity.isAirBorne = false;
        }

        ++updateCounter;

        if (trackedEntity.velocityChanged)
        {
            func_151261_b(new S12PacketEntityVelocity(trackedEntity));
            trackedEntity.velocityChanged = false;
        }
    }

    /**
     * Sends the entity metadata (DataWatcher) and attributes to all players tracking this entity, including the entity
     * itself if a player.
     */
    private void sendMetadataToAllAssociatedPlayers()
    {
        DataWatcher datawatcher = trackedEntity.getDataWatcher();

        if (datawatcher.hasObjectChanged())
        {
            func_151261_b(new S1CPacketEntityMetadata(trackedEntity.getEntityId(), datawatcher, false));
        }

        if (trackedEntity instanceof EntityLivingBase)
        {
            ServersideAttributeMap serversideattributemap = (ServersideAttributeMap)((EntityLivingBase) trackedEntity).getAttributeMap();
            Set<IAttributeInstance> set = serversideattributemap.getAttributeInstanceSet();

            if (!set.isEmpty())
            {
                func_151261_b(new S20PacketEntityProperties(trackedEntity.getEntityId(), set));
            }

            set.clear();
        }
    }

    /**
     * Send the given packet to all players tracking this entity.
     */
    public void sendPacketToTrackedPlayers(Packet packetIn)
    {
        for (EntityPlayerMP entityplayermp : trackingPlayers)
        {
            entityplayermp.playerNetServerHandler.sendPacket(packetIn);
        }
    }

    public void func_151261_b(Packet packetIn)
    {
        sendPacketToTrackedPlayers(packetIn);

        if (trackedEntity instanceof EntityPlayerMP)
        {
            ((EntityPlayerMP) trackedEntity).playerNetServerHandler.sendPacket(packetIn);
        }
    }

    public void sendDestroyEntityPacketToTrackedPlayers()
    {
        for (EntityPlayerMP entityplayermp : trackingPlayers)
        {
            entityplayermp.removeEntity(trackedEntity);
        }
    }

    public void removeFromTrackedPlayers(EntityPlayerMP playerMP)
    {
        if (trackingPlayers.contains(playerMP))
        {
            playerMP.removeEntity(trackedEntity);
            trackingPlayers.remove(playerMP);
        }
    }

    public void updatePlayerEntity(EntityPlayerMP playerMP)
    {
        if (playerMP != trackedEntity)
        {
            if (func_180233_c(playerMP))
            {
                if (!trackingPlayers.contains(playerMP) && (isPlayerWatchingThisChunk(playerMP) || trackedEntity.forceSpawn))
                {
                    trackingPlayers.add(playerMP);
                    Packet packet = func_151260_c();
                    playerMP.playerNetServerHandler.sendPacket(packet);

                    if (!trackedEntity.getDataWatcher().getIsBlank())
                    {
                        playerMP.playerNetServerHandler.sendPacket(new S1CPacketEntityMetadata(trackedEntity.getEntityId(), trackedEntity.getDataWatcher(), true));
                    }

                    NBTTagCompound nbttagcompound = trackedEntity.getNBTTagCompound();

                    if (nbttagcompound != null)
                    {
                        playerMP.playerNetServerHandler.sendPacket(new S49PacketUpdateEntityNBT(trackedEntity.getEntityId(), nbttagcompound));
                    }

                    if (trackedEntity instanceof EntityLivingBase)
                    {
                        ServersideAttributeMap serversideattributemap = (ServersideAttributeMap)((EntityLivingBase) trackedEntity).getAttributeMap();
                        Collection<IAttributeInstance> collection = serversideattributemap.getWatchedAttributes();

                        if (!collection.isEmpty())
                        {
                            playerMP.playerNetServerHandler.sendPacket(new S20PacketEntityProperties(trackedEntity.getEntityId(), collection));
                        }
                    }

                    lastTrackedEntityMotionX = trackedEntity.motionX;
                    lastTrackedEntityMotionY = trackedEntity.motionY;
                    motionZ = trackedEntity.motionZ;

                    if (sendVelocityUpdates && !(packet instanceof S0FPacketSpawnMob))
                    {
                        playerMP.playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(trackedEntity.getEntityId(), trackedEntity.motionX, trackedEntity.motionY, trackedEntity.motionZ));
                    }

                    if (trackedEntity.ridingEntity != null)
                    {
                        playerMP.playerNetServerHandler.sendPacket(new S1BPacketEntityAttach(0, trackedEntity, trackedEntity.ridingEntity));
                    }

                    if (trackedEntity instanceof EntityLiving && ((EntityLiving) trackedEntity).getLeashedToEntity() != null)
                    {
                        playerMP.playerNetServerHandler.sendPacket(new S1BPacketEntityAttach(1, trackedEntity, ((EntityLiving) trackedEntity).getLeashedToEntity()));
                    }

                    if (trackedEntity instanceof EntityLivingBase)
                    {
                        for (int i = 0; i < 5; ++i)
                        {
                            ItemStack itemstack = ((EntityLivingBase) trackedEntity).getEquipmentInSlot(i);

                            if (itemstack != null)
                            {
                                playerMP.playerNetServerHandler.sendPacket(new S04PacketEntityEquipment(trackedEntity.getEntityId(), i, itemstack));
                            }
                        }
                    }

                    if (trackedEntity instanceof EntityPlayer)
                    {
                        EntityPlayer entityplayer = (EntityPlayer) trackedEntity;

                        if (entityplayer.isPlayerSleeping())
                        {
                            playerMP.playerNetServerHandler.sendPacket(new S0APacketUseBed(entityplayer, new BlockPos(trackedEntity)));
                        }
                    }

                    if (trackedEntity instanceof EntityLivingBase)
                    {
                        EntityLivingBase entitylivingbase = (EntityLivingBase) trackedEntity;

                        for (PotionEffect potioneffect : entitylivingbase.getActivePotionEffects())
                        {
                            playerMP.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(trackedEntity.getEntityId(), potioneffect));
                        }
                    }
                }
            }
            else if (trackingPlayers.contains(playerMP))
            {
                trackingPlayers.remove(playerMP);
                playerMP.removeEntity(trackedEntity);
            }
        }
    }

    public boolean func_180233_c(EntityPlayerMP playerMP)
    {
        double d0 = playerMP.posX - (double)(encodedPosX / 32);
        double d1 = playerMP.posZ - (double)(encodedPosZ / 32);
        return d0 >= (double)(-trackingDistanceThreshold) && d0 <= (double) trackingDistanceThreshold && d1 >= (double)(-trackingDistanceThreshold) && d1 <= (double) trackingDistanceThreshold && trackedEntity.isSpectatedByPlayer(playerMP);
    }

    private boolean isPlayerWatchingThisChunk(EntityPlayerMP playerMP)
    {
        return playerMP.getServerForPlayer().getPlayerManager().isPlayerWatchingChunk(playerMP, trackedEntity.chunkCoordX, trackedEntity.chunkCoordZ);
    }

    public void updatePlayerEntities(List<EntityPlayer> p_73125_1_)
    {
        for (int i = 0; i < p_73125_1_.size(); ++i)
        {
            updatePlayerEntity((EntityPlayerMP)p_73125_1_.get(i));
        }
    }

    private Packet func_151260_c()
    {
        if (trackedEntity.isDead)
        {
            EntityTrackerEntry.logger.warn("Fetching addPacket for removed entity");
        }

        if (trackedEntity instanceof EntityItem)
        {
            return new S0EPacketSpawnObject(trackedEntity, 2, 1);
        }
        else if (trackedEntity instanceof EntityPlayerMP)
        {
            return new S0CPacketSpawnPlayer((EntityPlayer) trackedEntity);
        }
        else if (trackedEntity instanceof EntityMinecart)
        {
            EntityMinecart entityminecart = (EntityMinecart) trackedEntity;
            return new S0EPacketSpawnObject(trackedEntity, 10, entityminecart.getMinecartType().getNetworkID());
        }
        else if (trackedEntity instanceof EntityBoat)
        {
            return new S0EPacketSpawnObject(trackedEntity, 1);
        }
        else if (trackedEntity instanceof IAnimals)
        {
            lastHeadMotion = MathHelper.floor_float(trackedEntity.getRotationYawHead() * 256.0F / 360.0F);
            return new S0FPacketSpawnMob((EntityLivingBase) trackedEntity);
        }
        else if (trackedEntity instanceof EntityFishHook)
        {
            Entity entity1 = ((EntityFishHook) trackedEntity).angler;
            return new S0EPacketSpawnObject(trackedEntity, 90, entity1 != null ? entity1.getEntityId() : trackedEntity.getEntityId());
        }
        else if (trackedEntity instanceof EntityArrow)
        {
            Entity entity = ((EntityArrow) trackedEntity).shootingEntity;
            return new S0EPacketSpawnObject(trackedEntity, 60, entity != null ? entity.getEntityId() : trackedEntity.getEntityId());
        }
        else if (trackedEntity instanceof EntitySnowball)
        {
            return new S0EPacketSpawnObject(trackedEntity, 61);
        }
        else if (trackedEntity instanceof EntityPotion)
        {
            return new S0EPacketSpawnObject(trackedEntity, 73, ((EntityPotion) trackedEntity).getPotionDamage());
        }
        else if (trackedEntity instanceof EntityExpBottle)
        {
            return new S0EPacketSpawnObject(trackedEntity, 75);
        }
        else if (trackedEntity instanceof EntityEnderPearl)
        {
            return new S0EPacketSpawnObject(trackedEntity, 65);
        }
        else if (trackedEntity instanceof EntityEnderEye)
        {
            return new S0EPacketSpawnObject(trackedEntity, 72);
        }
        else if (trackedEntity instanceof EntityFireworkRocket)
        {
            return new S0EPacketSpawnObject(trackedEntity, 76);
        }
        else if (trackedEntity instanceof EntityFireball)
        {
            EntityFireball entityfireball = (EntityFireball) trackedEntity;
            S0EPacketSpawnObject s0epacketspawnobject2 = null;
            int i = 63;

            if (trackedEntity instanceof EntitySmallFireball)
            {
                i = 64;
            }
            else if (trackedEntity instanceof EntityWitherSkull)
            {
                i = 66;
            }

            if (entityfireball.shootingEntity != null)
            {
                s0epacketspawnobject2 = new S0EPacketSpawnObject(trackedEntity, i, ((EntityFireball) trackedEntity).shootingEntity.getEntityId());
            }
            else
            {
                s0epacketspawnobject2 = new S0EPacketSpawnObject(trackedEntity, i, 0);
            }

            s0epacketspawnobject2.setSpeedX((int)(entityfireball.accelerationX * 8000.0D));
            s0epacketspawnobject2.setSpeedY((int)(entityfireball.accelerationY * 8000.0D));
            s0epacketspawnobject2.setSpeedZ((int)(entityfireball.accelerationZ * 8000.0D));
            return s0epacketspawnobject2;
        }
        else if (trackedEntity instanceof EntityEgg)
        {
            return new S0EPacketSpawnObject(trackedEntity, 62);
        }
        else if (trackedEntity instanceof EntityTNTPrimed)
        {
            return new S0EPacketSpawnObject(trackedEntity, 50);
        }
        else if (trackedEntity instanceof EntityEnderCrystal)
        {
            return new S0EPacketSpawnObject(trackedEntity, 51);
        }
        else if (trackedEntity instanceof EntityFallingBlock)
        {
            EntityFallingBlock entityfallingblock = (EntityFallingBlock) trackedEntity;
            return new S0EPacketSpawnObject(trackedEntity, 70, Block.getStateId(entityfallingblock.getBlock()));
        }
        else if (trackedEntity instanceof EntityArmorStand)
        {
            return new S0EPacketSpawnObject(trackedEntity, 78);
        }
        else if (trackedEntity instanceof EntityPainting)
        {
            return new S10PacketSpawnPainting((EntityPainting) trackedEntity);
        }
        else if (trackedEntity instanceof EntityItemFrame)
        {
            EntityItemFrame entityitemframe = (EntityItemFrame) trackedEntity;
            S0EPacketSpawnObject s0epacketspawnobject1 = new S0EPacketSpawnObject(trackedEntity, 71, entityitemframe.facingDirection.getHorizontalIndex());
            BlockPos blockpos1 = entityitemframe.getHangingPosition();
            s0epacketspawnobject1.setX(MathHelper.floor_float((float)(blockpos1.getX() * 32)));
            s0epacketspawnobject1.setY(MathHelper.floor_float((float)(blockpos1.getY() * 32)));
            s0epacketspawnobject1.setZ(MathHelper.floor_float((float)(blockpos1.getZ() * 32)));
            return s0epacketspawnobject1;
        }
        else if (trackedEntity instanceof EntityLeashKnot)
        {
            EntityLeashKnot entityleashknot = (EntityLeashKnot) trackedEntity;
            S0EPacketSpawnObject s0epacketspawnobject = new S0EPacketSpawnObject(trackedEntity, 77);
            BlockPos blockpos = entityleashknot.getHangingPosition();
            s0epacketspawnobject.setX(MathHelper.floor_float((float)(blockpos.getX() * 32)));
            s0epacketspawnobject.setY(MathHelper.floor_float((float)(blockpos.getY() * 32)));
            s0epacketspawnobject.setZ(MathHelper.floor_float((float)(blockpos.getZ() * 32)));
            return s0epacketspawnobject;
        }
        else if (trackedEntity instanceof EntityXPOrb)
        {
            return new S11PacketSpawnExperienceOrb((EntityXPOrb) trackedEntity);
        }
        else
        {
            throw new IllegalArgumentException("Don't know how to add " + trackedEntity.getClass() + "!");
        }
    }

    /**
     * Remove a tracked player from our list and tell the tracked player to destroy us from their world.
     */
    public void removeTrackedPlayerSymmetric(EntityPlayerMP playerMP)
    {
        if (trackingPlayers.contains(playerMP))
        {
            trackingPlayers.remove(playerMP);
            playerMP.removeEntity(trackedEntity);
        }
    }
}
