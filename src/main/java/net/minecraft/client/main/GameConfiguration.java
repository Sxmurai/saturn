package net.minecraft.client.main;

import com.mojang.authlib.properties.PropertyMap;
import java.io.File;
import java.net.Proxy;
import net.minecraft.util.Session;

public class GameConfiguration
{
    public final GameConfiguration.UserInformation userInfo;
    public final GameConfiguration.DisplayInformation displayInfo;
    public final GameConfiguration.FolderInformation folderInfo;
    public final GameConfiguration.GameInformation gameInfo;
    public final GameConfiguration.ServerInformation serverInfo;

    public GameConfiguration(GameConfiguration.UserInformation userInfoIn, GameConfiguration.DisplayInformation displayInfoIn, GameConfiguration.FolderInformation folderInfoIn, GameConfiguration.GameInformation gameInfoIn, GameConfiguration.ServerInformation serverInfoIn)
    {
        userInfo = userInfoIn;
        displayInfo = displayInfoIn;
        folderInfo = folderInfoIn;
        gameInfo = gameInfoIn;
        serverInfo = serverInfoIn;
    }

    public static class DisplayInformation
    {
        public final int width;
        public final int height;
        public final boolean fullscreen;
        public final boolean checkGlErrors;

        public DisplayInformation(int widthIn, int heightIn, boolean fullscreenIn, boolean checkGlErrorsIn)
        {
            width = widthIn;
            height = heightIn;
            fullscreen = fullscreenIn;
            checkGlErrors = checkGlErrorsIn;
        }
    }

    public static class FolderInformation
    {
        public final File mcDataDir;
        public final File resourcePacksDir;
        public final File assetsDir;
        public final String assetIndex;

        public FolderInformation(File mcDataDirIn, File resourcePacksDirIn, File assetsDirIn, String assetIndexIn)
        {
            mcDataDir = mcDataDirIn;
            resourcePacksDir = resourcePacksDirIn;
            assetsDir = assetsDirIn;
            assetIndex = assetIndexIn;
        }
    }

    public static class GameInformation
    {
        public final boolean isDemo;
        public final String version;

        public GameInformation(boolean isDemoIn, String versionIn)
        {
            isDemo = isDemoIn;
            version = versionIn;
        }
    }

    public static class ServerInformation
    {
        public final String serverName;
        public final int serverPort;

        public ServerInformation(String serverNameIn, int serverPortIn)
        {
            serverName = serverNameIn;
            serverPort = serverPortIn;
        }
    }

    public static class UserInformation
    {
        public final Session session;
        public final PropertyMap userProperties;
        public final PropertyMap field_181172_c;
        public final Proxy proxy;

        public UserInformation(Session p_i46375_1_, PropertyMap p_i46375_2_, PropertyMap p_i46375_3_, Proxy p_i46375_4_)
        {
            session = p_i46375_1_;
            userProperties = p_i46375_2_;
            field_181172_c = p_i46375_3_;
            proxy = p_i46375_4_;
        }
    }
}
