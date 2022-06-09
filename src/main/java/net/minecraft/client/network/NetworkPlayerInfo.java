package net.minecraft.client.network;

import com.google.common.base.Objects;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldSettings;

public class NetworkPlayerInfo
{
    /**
     * The GameProfile for the player represented by this NetworkPlayerInfo instance
     */
    private final GameProfile gameProfile;
    private WorldSettings.GameType gameType;

    /** Player response time to server in milliseconds */
    private int responseTime;
    private boolean playerTexturesLoaded = false;
    private ResourceLocation locationSkin;
    private ResourceLocation locationCape;
    private String skinType;

    /**
     * When this is non-null, it is displayed instead of the player's real name
     */
    private IChatComponent displayName;
    private int field_178873_i = 0;
    private int field_178870_j = 0;
    private long field_178871_k = 0L;
    private long field_178868_l = 0L;
    private long field_178869_m = 0L;

    public NetworkPlayerInfo(GameProfile p_i46294_1_)
    {
        gameProfile = p_i46294_1_;
    }

    public NetworkPlayerInfo(S38PacketPlayerListItem.AddPlayerData p_i46295_1_)
    {
        gameProfile = p_i46295_1_.getProfile();
        gameType = p_i46295_1_.getGameMode();
        responseTime = p_i46295_1_.getPing();
        displayName = p_i46295_1_.getDisplayName();
    }

    /**
     * Returns the GameProfile for the player represented by this NetworkPlayerInfo instance
     */
    public GameProfile getGameProfile()
    {
        return gameProfile;
    }

    public WorldSettings.GameType getGameType()
    {
        return gameType;
    }

    public int getResponseTime()
    {
        return responseTime;
    }

    protected void setGameType(WorldSettings.GameType p_178839_1_)
    {
        gameType = p_178839_1_;
    }

    protected void setResponseTime(int p_178838_1_)
    {
        responseTime = p_178838_1_;
    }

    public boolean hasLocationSkin()
    {
        return locationSkin != null;
    }

    public String getSkinType()
    {
        return skinType == null ? DefaultPlayerSkin.getSkinType(gameProfile.getId()) : skinType;
    }

    public ResourceLocation getLocationSkin()
    {
        if (locationSkin == null)
        {
            loadPlayerTextures();
        }

        return Objects.firstNonNull(locationSkin, DefaultPlayerSkin.getDefaultSkin(gameProfile.getId()));
    }

    public ResourceLocation getLocationCape()
    {
        if (locationCape == null)
        {
            loadPlayerTextures();
        }

        return locationCape;
    }

    public ScorePlayerTeam getPlayerTeam()
    {
        return Minecraft.getMinecraft().theWorld.getScoreboard().getPlayersTeam(getGameProfile().getName());
    }

    protected void loadPlayerTextures()
    {
        synchronized (this)
        {
            if (!playerTexturesLoaded)
            {
                playerTexturesLoaded = true;
                Minecraft.getMinecraft().getSkinManager().loadProfileTextures(gameProfile, new SkinManager.SkinAvailableCallback()
                {
                    public void skinAvailable(MinecraftProfileTexture.Type p_180521_1_, ResourceLocation location, MinecraftProfileTexture profileTexture)
                    {
                        switch (p_180521_1_)
                        {
                            case SKIN:
                                locationSkin = location;
                                skinType = profileTexture.getMetadata("model");

                                if (skinType == null)
                                {
                                    skinType = "default";
                                }

                                break;

                            case CAPE:
                                locationCape = location;
                        }
                    }
                }, true);
            }
        }
    }

    public void setDisplayName(IChatComponent displayNameIn)
    {
        displayName = displayNameIn;
    }

    public IChatComponent getDisplayName()
    {
        return displayName;
    }

    public int func_178835_l()
    {
        return field_178873_i;
    }

    public void func_178836_b(int p_178836_1_)
    {
        field_178873_i = p_178836_1_;
    }

    public int func_178860_m()
    {
        return field_178870_j;
    }

    public void func_178857_c(int p_178857_1_)
    {
        field_178870_j = p_178857_1_;
    }

    public long func_178847_n()
    {
        return field_178871_k;
    }

    public void func_178846_a(long p_178846_1_)
    {
        field_178871_k = p_178846_1_;
    }

    public long func_178858_o()
    {
        return field_178868_l;
    }

    public void func_178844_b(long p_178844_1_)
    {
        field_178868_l = p_178844_1_;
    }

    public long func_178855_p()
    {
        return field_178869_m;
    }

    public void func_178843_c(long p_178843_1_)
    {
        field_178869_m = p_178843_1_;
    }
}
