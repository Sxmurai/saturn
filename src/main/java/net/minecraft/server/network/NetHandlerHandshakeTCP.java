package net.minecraft.server.network;

import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class NetHandlerHandshakeTCP implements INetHandlerHandshakeServer
{
    private final MinecraftServer server;
    private final NetworkManager networkManager;

    public NetHandlerHandshakeTCP(MinecraftServer serverIn, NetworkManager netManager)
    {
        server = serverIn;
        networkManager = netManager;
    }

    /**
     * There are two recognized intentions for initiating a handshake: logging in and acquiring server status. The
     * NetworkManager's protocol will be reconfigured according to the specified intention, although a login-intention
     * must pass a versioncheck or receive a disconnect otherwise
     */
    public void processHandshake(C00Handshake packetIn)
    {
        switch (packetIn.getRequestedState())
        {
            case LOGIN:
                networkManager.setConnectionState(EnumConnectionState.LOGIN);

                if (packetIn.getProtocolVersion() > 47)
                {
                    ChatComponentText chatcomponenttext = new ChatComponentText("Outdated server! I'm still on 1.8.8");
                    networkManager.sendPacket(new S00PacketDisconnect(chatcomponenttext));
                    networkManager.closeChannel(chatcomponenttext);
                }
                else if (packetIn.getProtocolVersion() < 47)
                {
                    ChatComponentText chatcomponenttext1 = new ChatComponentText("Outdated client! Please use 1.8.8");
                    networkManager.sendPacket(new S00PacketDisconnect(chatcomponenttext1));
                    networkManager.closeChannel(chatcomponenttext1);
                }
                else
                {
                    networkManager.setNetHandler(new NetHandlerLoginServer(server, networkManager));
                }

                break;

            case STATUS:
                networkManager.setConnectionState(EnumConnectionState.STATUS);
                networkManager.setNetHandler(new NetHandlerStatusServer(server, networkManager));
                break;

            default:
                throw new UnsupportedOperationException("Invalid intention " + packetIn.getRequestedState());
        }
    }

    /**
     * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
     */
    public void onDisconnect(IChatComponent reason)
    {
    }
}
