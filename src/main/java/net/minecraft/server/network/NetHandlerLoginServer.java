package net.minecraft.server.network;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.GenericFutureListener;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.crypto.SecretKey;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.INetHandlerLoginServer;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.network.login.server.S01PacketEncryptionRequest;
import net.minecraft.network.login.server.S02PacketLoginSuccess;
import net.minecraft.network.login.server.S03PacketEnableCompression;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.CryptManager;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetHandlerLoginServer implements INetHandlerLoginServer, ITickable
{
    private static final AtomicInteger AUTHENTICATOR_THREAD_ID = new AtomicInteger(0);
    private static final Logger logger = LogManager.getLogger();
    private static final Random RANDOM = new Random();
    private final byte[] verifyToken = new byte[4];
    private final MinecraftServer server;
    public final NetworkManager networkManager;
    private NetHandlerLoginServer.LoginState currentLoginState = NetHandlerLoginServer.LoginState.HELLO;

    /** How long has player been trying to login into the server. */
    private int connectionTimer;
    private GameProfile loginGameProfile;
    private final String serverId = "";
    private SecretKey secretKey;
    private EntityPlayerMP field_181025_l;

    public NetHandlerLoginServer(MinecraftServer p_i45298_1_, NetworkManager p_i45298_2_)
    {
        server = p_i45298_1_;
        networkManager = p_i45298_2_;
        NetHandlerLoginServer.RANDOM.nextBytes(verifyToken);
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update()
    {
        if (currentLoginState == NetHandlerLoginServer.LoginState.READY_TO_ACCEPT)
        {
            tryAcceptPlayer();
        }
        else if (currentLoginState == NetHandlerLoginServer.LoginState.DELAY_ACCEPT)
        {
            EntityPlayerMP entityplayermp = server.getConfigurationManager().getPlayerByUUID(loginGameProfile.getId());

            if (entityplayermp == null)
            {
                currentLoginState = NetHandlerLoginServer.LoginState.READY_TO_ACCEPT;
                server.getConfigurationManager().initializeConnectionToPlayer(networkManager, field_181025_l);
                field_181025_l = null;
            }
        }

        if (connectionTimer++ == 600)
        {
            closeConnection("Took too long to log in");
        }
    }

    public void closeConnection(String reason)
    {
        try
        {
            NetHandlerLoginServer.logger.info("Disconnecting " + getConnectionInfo() + ": " + reason);
            ChatComponentText chatcomponenttext = new ChatComponentText(reason);
            networkManager.sendPacket(new S00PacketDisconnect(chatcomponenttext));
            networkManager.closeChannel(chatcomponenttext);
        }
        catch (Exception exception)
        {
            NetHandlerLoginServer.logger.error("Error whilst disconnecting player", exception);
        }
    }

    public void tryAcceptPlayer()
    {
        if (!loginGameProfile.isComplete())
        {
            loginGameProfile = getOfflineProfile(loginGameProfile);
        }

        String s = server.getConfigurationManager().allowUserToConnect(networkManager.getRemoteAddress(), loginGameProfile);

        if (s != null)
        {
            closeConnection(s);
        }
        else
        {
            currentLoginState = NetHandlerLoginServer.LoginState.ACCEPTED;

            if (server.getNetworkCompressionTreshold() >= 0 && !networkManager.isLocalChannel())
            {
                networkManager.sendPacket(new S03PacketEnableCompression(server.getNetworkCompressionTreshold()), new ChannelFutureListener()
                {
                    public void operationComplete(ChannelFuture p_operationComplete_1_) throws Exception
                    {
                        networkManager.setCompressionTreshold(server.getNetworkCompressionTreshold());
                    }
                });
            }

            networkManager.sendPacket(new S02PacketLoginSuccess(loginGameProfile));
            EntityPlayerMP entityplayermp = server.getConfigurationManager().getPlayerByUUID(loginGameProfile.getId());

            if (entityplayermp != null)
            {
                currentLoginState = NetHandlerLoginServer.LoginState.DELAY_ACCEPT;
                field_181025_l = server.getConfigurationManager().createPlayerForUser(loginGameProfile);
            }
            else
            {
                server.getConfigurationManager().initializeConnectionToPlayer(networkManager, server.getConfigurationManager().createPlayerForUser(loginGameProfile));
            }
        }
    }

    /**
     * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
     */
    public void onDisconnect(IChatComponent reason)
    {
        NetHandlerLoginServer.logger.info(getConnectionInfo() + " lost connection: " + reason.getUnformattedText());
    }

    public String getConnectionInfo()
    {
        return loginGameProfile != null ? loginGameProfile + " (" + networkManager.getRemoteAddress().toString() + ")" : String.valueOf(networkManager.getRemoteAddress());
    }

    public void processLoginStart(C00PacketLoginStart packetIn)
    {
        Validate.validState(currentLoginState == NetHandlerLoginServer.LoginState.HELLO, "Unexpected hello packet");
        loginGameProfile = packetIn.getProfile();

        if (server.isServerInOnlineMode() && !networkManager.isLocalChannel())
        {
            currentLoginState = NetHandlerLoginServer.LoginState.KEY;
            networkManager.sendPacket(new S01PacketEncryptionRequest(serverId, server.getKeyPair().getPublic(), verifyToken));
        }
        else
        {
            currentLoginState = NetHandlerLoginServer.LoginState.READY_TO_ACCEPT;
        }
    }

    public void processEncryptionResponse(C01PacketEncryptionResponse packetIn)
    {
        Validate.validState(currentLoginState == NetHandlerLoginServer.LoginState.KEY, "Unexpected key packet");
        PrivateKey privatekey = server.getKeyPair().getPrivate();

        if (!Arrays.equals(verifyToken, packetIn.getVerifyToken(privatekey)))
        {
            throw new IllegalStateException("Invalid nonce!");
        }
        else
        {
            secretKey = packetIn.getSecretKey(privatekey);
            currentLoginState = NetHandlerLoginServer.LoginState.AUTHENTICATING;
            networkManager.enableEncryption(secretKey);
            (new Thread("User Authenticator #" + NetHandlerLoginServer.AUTHENTICATOR_THREAD_ID.incrementAndGet())
            {
                public void run()
                {
                    GameProfile gameprofile = loginGameProfile;

                    try
                    {
                        String s = (new BigInteger(CryptManager.getServerIdHash(serverId, server.getKeyPair().getPublic(), secretKey))).toString(16);
                        loginGameProfile = server.getMinecraftSessionService().hasJoinedServer(new GameProfile(null, gameprofile.getName()), s);

                        if (loginGameProfile != null)
                        {
                            NetHandlerLoginServer.logger.info("UUID of player " + loginGameProfile.getName() + " is " + loginGameProfile.getId());
                            currentLoginState = NetHandlerLoginServer.LoginState.READY_TO_ACCEPT;
                        }
                        else if (server.isSinglePlayer())
                        {
                            NetHandlerLoginServer.logger.warn("Failed to verify username but will let them in anyway!");
                            loginGameProfile = getOfflineProfile(gameprofile);
                            currentLoginState = NetHandlerLoginServer.LoginState.READY_TO_ACCEPT;
                        }
                        else
                        {
                            closeConnection("Failed to verify username!");
                            NetHandlerLoginServer.logger.error("Username '" + loginGameProfile.getName() + "' tried to join with an invalid session");
                        }
                    }
                    catch (AuthenticationUnavailableException var3)
                    {
                        if (server.isSinglePlayer())
                        {
                            NetHandlerLoginServer.logger.warn("Authentication servers are down but will let them in anyway!");
                            loginGameProfile = getOfflineProfile(gameprofile);
                            currentLoginState = NetHandlerLoginServer.LoginState.READY_TO_ACCEPT;
                        }
                        else
                        {
                            closeConnection("Authentication servers are down. Please try again later, sorry!");
                            NetHandlerLoginServer.logger.error("Couldn't verify username because servers are unavailable");
                        }
                    }
                }
            }).start();
        }
    }

    protected GameProfile getOfflineProfile(GameProfile original)
    {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + original.getName()).getBytes(Charsets.UTF_8));
        return new GameProfile(uuid, original.getName());
    }

    static enum LoginState
    {
        HELLO,
        KEY,
        AUTHENTICATING,
        READY_TO_ACCEPT,
        DELAY_ACCEPT,
        ACCEPTED
    }
}
