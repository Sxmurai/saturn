package net.minecraft.util;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Map;
import java.util.UUID;

public class Session
{
    private final String username;
    private final String playerID;
    private final String token;
    private final Session.Type sessionType;

    public Session(String usernameIn, String playerIDIn, String tokenIn, String sessionTypeIn)
    {
        username = usernameIn;
        playerID = playerIDIn;
        token = tokenIn;
        sessionType = Session.Type.setSessionType(sessionTypeIn);
    }

    public String getSessionID()
    {
        return "token:" + token + ":" + playerID;
    }

    public String getPlayerID()
    {
        return playerID;
    }

    public String getUsername()
    {
        return username;
    }

    public String getToken()
    {
        return token;
    }

    public GameProfile getProfile()
    {
        try
        {
            UUID uuid = UUIDTypeAdapter.fromString(getPlayerID());
            return new GameProfile(uuid, getUsername());
        }
        catch (IllegalArgumentException var2)
        {
            return new GameProfile(null, getUsername());
        }
    }

    /**
     * Returns either 'legacy' or 'mojang' whether the account is migrated or not
     */
    public Session.Type getSessionType()
    {
        return sessionType;
    }

    public static enum Type
    {
        LEGACY("legacy"),
        MOJANG("mojang");

        private static final Map<String, Session.Type> SESSION_TYPES = Maps.newHashMap();
        private final String sessionType;

        private Type(String sessionTypeIn)
        {
            sessionType = sessionTypeIn;
        }

        public static Session.Type setSessionType(String sessionTypeIn)
        {
            return Type.SESSION_TYPES.get(sessionTypeIn.toLowerCase());
        }

        static {
            for (Session.Type session$type : Type.values())
            {
                Type.SESSION_TYPES.put(session$type.sessionType, session$type);
            }
        }
    }
}
