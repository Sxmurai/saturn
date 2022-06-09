package net.minecraft.client.multiplayer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public class ServerData
{
    public String serverName;
    public String serverIP;

    /**
     * the string indicating number of players on and capacity of the server that is shown on the server browser (i.e.
     * "5/20" meaning 5 slots used out of 20 slots total)
     */
    public String populationInfo;

    /**
     * (better variable name would be 'hostname') server name as displayed in the server browser's second line (grey
     * text)
     */
    public String serverMOTD;

    /** last server ping that showed up in the server browser */
    public long pingToServer;
    public int version = 47;

    /** Game version for this server. */
    public String gameVersion = "1.8.8";
    public boolean field_78841_f;
    public String playerList;
    private ServerData.ServerResourceMode resourceMode = ServerData.ServerResourceMode.PROMPT;
    private String serverIcon;
    private boolean field_181042_l;

    public ServerData(String p_i46420_1_, String p_i46420_2_, boolean p_i46420_3_)
    {
        serverName = p_i46420_1_;
        serverIP = p_i46420_2_;
        field_181042_l = p_i46420_3_;
    }

    /**
     * Returns an NBTTagCompound with the server's name, IP and maybe acceptTextures.
     */
    public NBTTagCompound getNBTCompound()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setString("name", serverName);
        nbttagcompound.setString("ip", serverIP);

        if (serverIcon != null)
        {
            nbttagcompound.setString("icon", serverIcon);
        }

        if (resourceMode == ServerData.ServerResourceMode.ENABLED)
        {
            nbttagcompound.setBoolean("acceptTextures", true);
        }
        else if (resourceMode == ServerData.ServerResourceMode.DISABLED)
        {
            nbttagcompound.setBoolean("acceptTextures", false);
        }

        return nbttagcompound;
    }

    public ServerData.ServerResourceMode getResourceMode()
    {
        return resourceMode;
    }

    public void setResourceMode(ServerData.ServerResourceMode mode)
    {
        resourceMode = mode;
    }

    /**
     * Takes an NBTTagCompound with 'name' and 'ip' keys, returns a ServerData instance.
     */
    public static ServerData getServerDataFromNBTCompound(NBTTagCompound nbtCompound)
    {
        ServerData serverdata = new ServerData(nbtCompound.getString("name"), nbtCompound.getString("ip"), false);

        if (nbtCompound.hasKey("icon", 8))
        {
            serverdata.setBase64EncodedIconData(nbtCompound.getString("icon"));
        }

        if (nbtCompound.hasKey("acceptTextures", 1))
        {
            if (nbtCompound.getBoolean("acceptTextures"))
            {
                serverdata.setResourceMode(ServerData.ServerResourceMode.ENABLED);
            }
            else
            {
                serverdata.setResourceMode(ServerData.ServerResourceMode.DISABLED);
            }
        }
        else
        {
            serverdata.setResourceMode(ServerData.ServerResourceMode.PROMPT);
        }

        return serverdata;
    }

    /**
     * Returns the base-64 encoded representation of the server's icon, or null if not available
     */
    public String getBase64EncodedIconData()
    {
        return serverIcon;
    }

    public void setBase64EncodedIconData(String icon)
    {
        serverIcon = icon;
    }

    public boolean func_181041_d()
    {
        return field_181042_l;
    }

    public void copyFrom(ServerData serverDataIn)
    {
        serverIP = serverDataIn.serverIP;
        serverName = serverDataIn.serverName;
        setResourceMode(serverDataIn.getResourceMode());
        serverIcon = serverDataIn.serverIcon;
        field_181042_l = serverDataIn.field_181042_l;
    }

    public static enum ServerResourceMode
    {
        ENABLED("enabled"),
        DISABLED("disabled"),
        PROMPT("prompt");

        private final IChatComponent motd;

        private ServerResourceMode(String p_i1053_3_)
        {
            motd = new ChatComponentTranslation("addServer.resourcePack." + p_i1053_3_);
        }

        public IChatComponent getMotd()
        {
            return motd;
        }
    }
}
