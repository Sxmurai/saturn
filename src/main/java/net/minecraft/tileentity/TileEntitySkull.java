package net.minecraft.tileentity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringUtils;

public class TileEntitySkull extends TileEntity
{
    private int skullType;
    private int skullRotation;
    private GameProfile playerProfile = null;

    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setByte("SkullType", (byte)(skullType & 255));
        compound.setByte("Rot", (byte)(skullRotation & 255));

        if (playerProfile != null)
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            NBTUtil.writeGameProfile(nbttagcompound, playerProfile);
            compound.setTag("Owner", nbttagcompound);
        }
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        skullType = compound.getByte("SkullType");
        skullRotation = compound.getByte("Rot");

        if (skullType == 3)
        {
            if (compound.hasKey("Owner", 10))
            {
                playerProfile = NBTUtil.readGameProfileFromNBT(compound.getCompoundTag("Owner"));
            }
            else if (compound.hasKey("ExtraType", 8))
            {
                String s = compound.getString("ExtraType");

                if (!StringUtils.isNullOrEmpty(s))
                {
                    playerProfile = new GameProfile(null, s);
                    updatePlayerProfile();
                }
            }
        }
    }

    public GameProfile getPlayerProfile()
    {
        return playerProfile;
    }

    /**
     * Allows for a specialized description packet to be created. This is often used to sync tile entity data from the
     * server to the client easily. For example this is used by signs to synchronise the text to be displayed.
     */
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(pos, 4, nbttagcompound);
    }

    public void setType(int type)
    {
        skullType = type;
        playerProfile = null;
    }

    public void setPlayerProfile(GameProfile playerProfile)
    {
        skullType = 3;
        this.playerProfile = playerProfile;
        updatePlayerProfile();
    }

    private void updatePlayerProfile()
    {
        playerProfile = TileEntitySkull.updateGameprofile(playerProfile);
        markDirty();
    }

    public static GameProfile updateGameprofile(GameProfile input)
    {
        if (input != null && !StringUtils.isNullOrEmpty(input.getName()))
        {
            if (input.isComplete() && input.getProperties().containsKey("textures"))
            {
                return input;
            }
            else if (MinecraftServer.getServer() == null)
            {
                return input;
            }
            else
            {
                GameProfile gameprofile = MinecraftServer.getServer().getPlayerProfileCache().getGameProfileForUsername(input.getName());

                if (gameprofile == null)
                {
                    return input;
                }
                else
                {
                    Property property = Iterables.getFirst(gameprofile.getProperties().get("textures"), null);

                    if (property == null)
                    {
                        gameprofile = MinecraftServer.getServer().getMinecraftSessionService().fillProfileProperties(gameprofile, true);
                    }

                    return gameprofile;
                }
            }
        }
        else
        {
            return input;
        }
    }

    public int getSkullType()
    {
        return skullType;
    }

    public int getSkullRotation()
    {
        return skullRotation;
    }

    public void setSkullRotation(int rotation)
    {
        skullRotation = rotation;
    }
}
