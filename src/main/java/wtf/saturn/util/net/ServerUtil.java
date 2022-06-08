package wtf.saturn.util.net;

import net.minecraft.client.multiplayer.ServerData;
import wtf.saturn.util.Globals;

/**
 * General server utilities
 *
 * @author aesthetical
 * @since 6/7/22
 */
public class ServerUtil implements Globals {

    /**
     * Checks if we are on a server
     *
     * @param serverIp the server ip (can include the port)
     * @return if we are on that server
     */
    public static boolean isOnServer(String serverIp) {
        if (mc.isSingleplayer() && serverIp.equalsIgnoreCase("singleplayer")) {
            return true;
        }

        ServerData data = mc.getCurrentServerData();
        if (data == null) {
            return false;
        }

        return data.serverIP.toLowerCase().contains(serverIp.toLowerCase());
    }
}
