package net.minecraft.client.settings;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.stream.TwitchStream;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import optifine.ClearWater;
import optifine.Config;
import optifine.CustomColors;
import optifine.CustomSky;
import optifine.DynamicLights;
import optifine.Lang;
import optifine.NaturalTextures;
import optifine.RandomMobs;
import optifine.Reflector;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import shadersmod.client.Shaders;

public class GameSettings
{
    private static final Logger logger = LogManager.getLogger();
    private static final Gson gson = new Gson();
    private static final ParameterizedType typeListString = new ParameterizedType()
    {
        private static final String __OBFID = "CL_00000651";
        public Type[] getActualTypeArguments()
        {
            return new Type[] {String.class};
        }
        public Type getRawType()
        {
            return List.class;
        }
        public Type getOwnerType()
        {
            return null;
        }
    };

    /** GUI scale values */
    private static final String[] GUISCALES = new String[] {"options.guiScale.auto", "options.guiScale.small", "options.guiScale.normal", "options.guiScale.large"};
    private static final String[] PARTICLES = new String[] {"options.particles.all", "options.particles.decreased", "options.particles.minimal"};
    private static final String[] AMBIENT_OCCLUSIONS = new String[] {"options.ao.off", "options.ao.min", "options.ao.max"};
    private static final String[] STREAM_COMPRESSIONS = new String[] {"options.stream.compression.low", "options.stream.compression.medium", "options.stream.compression.high"};
    private static final String[] STREAM_CHAT_MODES = new String[] {"options.stream.chat.enabled.streaming", "options.stream.chat.enabled.always", "options.stream.chat.enabled.never"};
    private static final String[] STREAM_CHAT_FILTER_MODES = new String[] {"options.stream.chat.userFilter.all", "options.stream.chat.userFilter.subs", "options.stream.chat.userFilter.mods"};
    private static final String[] STREAM_MIC_MODES = new String[] {"options.stream.mic_toggle.mute", "options.stream.mic_toggle.talk"};
    private static final String[] field_181149_aW = new String[] {"options.off", "options.graphics.fast", "options.graphics.fancy"};
    public float mouseSensitivity = 0.5F;
    public boolean invertMouse;
    public int renderDistanceChunks = -1;
    public boolean viewBobbing = true;
    public boolean anaglyph;
    public boolean fboEnable = true;
    public int limitFramerate = 120;

    /** Clouds flag */
    public int clouds = 2;
    public boolean fancyGraphics = true;

    /** Smooth Lighting */
    public int ambientOcclusion = 2;
    public List resourcePacks = Lists.newArrayList();
    public List field_183018_l = Lists.newArrayList();
    public EntityPlayer.EnumChatVisibility chatVisibility = EntityPlayer.EnumChatVisibility.FULL;
    public boolean chatColours = true;
    public boolean chatLinks = true;
    public boolean chatLinksPrompt = true;
    public float chatOpacity = 1.0F;
    public boolean snooperEnabled = true;
    public boolean fullScreen;
    public boolean enableVsync = true;
    public boolean useVbo = false;
    public boolean allowBlockAlternatives = true;
    public boolean reducedDebugInfo = false;
    public boolean hideServerAddress;

    /**
     * Whether to show advanced information on item tooltips, toggled by F3+H
     */
    public boolean advancedItemTooltips;

    /** Whether to pause when the game loses focus, toggled by F3+P */
    public boolean pauseOnLostFocus = true;
    private final Set setModelParts = Sets.newHashSet(EnumPlayerModelParts.values());
    public boolean touchscreen;
    public int overrideWidth;
    public int overrideHeight;
    public boolean heldItemTooltips = true;
    public float chatScale = 1.0F;
    public float chatWidth = 1.0F;
    public float chatHeightUnfocused = 0.44366196F;
    public float chatHeightFocused = 1.0F;
    public boolean showInventoryAchievementHint = true;
    public int mipmapLevels = 4;
    private final Map mapSoundLevels = Maps.newEnumMap(SoundCategory.class);
    public float streamBytesPerPixel = 0.5F;
    public float streamMicVolume = 1.0F;
    public float streamGameVolume = 1.0F;
    public float streamKbps = 0.5412844F;
    public float streamFps = 0.31690142F;
    public int streamCompression = 1;
    public boolean streamSendMetadata = true;
    public String streamPreferredServer = "";
    public int streamChatEnabled = 0;
    public int streamChatUserFilter = 0;
    public int streamMicToggleBehavior = 0;
    public boolean field_181150_U = true;
    public boolean field_181151_V = true;
    public KeyBinding keyBindForward = new KeyBinding("key.forward", 17, "key.categories.movement");
    public KeyBinding keyBindLeft = new KeyBinding("key.left", 30, "key.categories.movement");
    public KeyBinding keyBindBack = new KeyBinding("key.back", 31, "key.categories.movement");
    public KeyBinding keyBindRight = new KeyBinding("key.right", 32, "key.categories.movement");
    public KeyBinding keyBindJump = new KeyBinding("key.jump", 57, "key.categories.movement");
    public KeyBinding keyBindSneak = new KeyBinding("key.sneak", 42, "key.categories.movement");
    public KeyBinding keyBindSprint = new KeyBinding("key.sprint", 29, "key.categories.movement");
    public KeyBinding keyBindInventory = new KeyBinding("key.inventory", 18, "key.categories.inventory");
    public KeyBinding keyBindUseItem = new KeyBinding("key.use", -99, "key.categories.gameplay");
    public KeyBinding keyBindDrop = new KeyBinding("key.drop", 16, "key.categories.gameplay");
    public KeyBinding keyBindAttack = new KeyBinding("key.attack", -100, "key.categories.gameplay");
    public KeyBinding keyBindPickBlock = new KeyBinding("key.pickItem", -98, "key.categories.gameplay");
    public KeyBinding keyBindChat = new KeyBinding("key.chat", 20, "key.categories.multiplayer");
    public KeyBinding keyBindPlayerList = new KeyBinding("key.playerlist", 15, "key.categories.multiplayer");
    public KeyBinding keyBindCommand = new KeyBinding("key.command", 53, "key.categories.multiplayer");
    public KeyBinding keyBindScreenshot = new KeyBinding("key.screenshot", 60, "key.categories.misc");
    public KeyBinding keyBindTogglePerspective = new KeyBinding("key.togglePerspective", 63, "key.categories.misc");
    public KeyBinding keyBindSmoothCamera = new KeyBinding("key.smoothCamera", 0, "key.categories.misc");
    public KeyBinding keyBindFullscreen = new KeyBinding("key.fullscreen", 87, "key.categories.misc");
    public KeyBinding keyBindSpectatorOutlines = new KeyBinding("key.spectatorOutlines", 0, "key.categories.misc");
    public KeyBinding keyBindStreamStartStop = new KeyBinding("key.streamStartStop", 64, "key.categories.stream");
    public KeyBinding keyBindStreamPauseUnpause = new KeyBinding("key.streamPauseUnpause", 65, "key.categories.stream");
    public KeyBinding keyBindStreamCommercials = new KeyBinding("key.streamCommercial", 0, "key.categories.stream");
    public KeyBinding keyBindStreamToggleMic = new KeyBinding("key.streamToggleMic", 0, "key.categories.stream");
    public KeyBinding[] keyBindsHotbar = new KeyBinding[] {new KeyBinding("key.hotbar.1", 2, "key.categories.inventory"), new KeyBinding("key.hotbar.2", 3, "key.categories.inventory"), new KeyBinding("key.hotbar.3", 4, "key.categories.inventory"), new KeyBinding("key.hotbar.4", 5, "key.categories.inventory"), new KeyBinding("key.hotbar.5", 6, "key.categories.inventory"), new KeyBinding("key.hotbar.6", 7, "key.categories.inventory"), new KeyBinding("key.hotbar.7", 8, "key.categories.inventory"), new KeyBinding("key.hotbar.8", 9, "key.categories.inventory"), new KeyBinding("key.hotbar.9", 10, "key.categories.inventory")};
    public KeyBinding[] keyBindings;
    protected Minecraft mc;
    private File optionsFile;
    public EnumDifficulty difficulty;
    public boolean hideGUI;
    public int thirdPersonView;

    /** true if debug info should be displayed instead of version */
    public boolean showDebugInfo;
    public boolean showDebugProfilerChart;
    public boolean field_181657_aC;

    /** The lastServer string. */
    public String lastServer;

    /** Smooth Camera Toggle */
    public boolean smoothCamera;
    public boolean debugCamEnable;
    public float fovSetting;
    public float gammaSetting;
    public float saturation;

    /** GUI scale */
    public int guiScale;

    /** Determines amount of particles. 0 = All, 1 = Decreased, 2 = Minimal */
    public int particleSetting;

    /** Game settings language */
    public String language;
    public boolean forceUnicodeFont;
    private static final String __OBFID = "CL_00000650";
    public int ofFogType = 1;
    public float ofFogStart = 0.8F;
    public int ofMipmapType = 0;
    public boolean ofOcclusionFancy = false;
    public boolean ofSmoothFps = false;
    public boolean ofSmoothWorld = Config.isSingleProcessor();
    public boolean ofLazyChunkLoading = Config.isSingleProcessor();
    public float ofAoLevel = 1.0F;
    public int ofAaLevel = 0;
    public int ofAfLevel = 1;
    public int ofClouds = 0;
    public float ofCloudsHeight = 0.0F;
    public int ofTrees = 0;
    public int ofRain = 0;
    public int ofDroppedItems = 0;
    public int ofBetterGrass = 3;
    public int ofAutoSaveTicks = 4000;
    public boolean ofLagometer = false;
    public boolean ofProfiler = false;
    public boolean ofShowFps = false;
    public boolean ofWeather = true;
    public boolean ofSky = true;
    public boolean ofStars = true;
    public boolean ofSunMoon = true;
    public int ofVignette = 0;
    public int ofChunkUpdates = 1;
    public boolean ofChunkUpdatesDynamic = false;
    public int ofTime = 0;
    public boolean ofClearWater = false;
    public boolean ofBetterSnow = false;
    public String ofFullscreenMode = "Default";
    public boolean ofSwampColors = true;
    public boolean ofRandomMobs = true;
    public boolean ofSmoothBiomes = true;
    public boolean ofCustomFonts = true;
    public boolean ofCustomColors = true;
    public boolean ofCustomSky = true;
    public boolean ofShowCapes = true;
    public int ofConnectedTextures = 2;
    public boolean ofCustomItems = true;
    public boolean ofNaturalTextures = false;
    public boolean ofFastMath = false;
    public boolean ofFastRender = true;
    public int ofTranslucentBlocks = 0;
    public boolean ofDynamicFov = true;
    public int ofDynamicLights = 3;
    public int ofAnimatedWater = 0;
    public int ofAnimatedLava = 0;
    public boolean ofAnimatedFire = true;
    public boolean ofAnimatedPortal = true;
    public boolean ofAnimatedRedstone = true;
    public boolean ofAnimatedExplosion = true;
    public boolean ofAnimatedFlame = true;
    public boolean ofAnimatedSmoke = true;
    public boolean ofVoidParticles = true;
    public boolean ofWaterParticles = true;
    public boolean ofRainSplash = true;
    public boolean ofPortalParticles = true;
    public boolean ofPotionParticles = true;
    public boolean ofFireworkParticles = true;
    public boolean ofDrippingWaterLava = true;
    public boolean ofAnimatedTerrain = true;
    public boolean ofAnimatedTextures = true;
    public static final int DEFAULT = 0;
    public static final int FAST = 1;
    public static final int FANCY = 2;
    public static final int OFF = 3;
    public static final int SMART = 4;
    public static final int ANIM_ON = 0;
    public static final int ANIM_GENERATED = 1;
    public static final int ANIM_OFF = 2;
    public static final String DEFAULT_STR = "Default";
    private static final int[] OF_TREES_VALUES = new int[] {0, 1, 4, 2};
    private static final int[] OF_DYNAMIC_LIGHTS = new int[] {3, 1, 2};
    private static final String[] KEYS_DYNAMIC_LIGHTS = new String[] {"options.off", "options.graphics.fast", "options.graphics.fancy"};
    public KeyBinding ofKeyBindZoom;
    private File optionsFileOF;

    public GameSettings(Minecraft mcIn, File p_i46326_2_)
    {
        keyBindings = ArrayUtils.addAll(new KeyBinding[] {keyBindAttack, keyBindUseItem, keyBindForward, keyBindLeft, keyBindBack, keyBindRight, keyBindJump, keyBindSneak, keyBindSprint, keyBindDrop, keyBindInventory, keyBindChat, keyBindPlayerList, keyBindPickBlock, keyBindCommand, keyBindScreenshot, keyBindTogglePerspective, keyBindSmoothCamera, keyBindStreamStartStop, keyBindStreamPauseUnpause, keyBindStreamCommercials, keyBindStreamToggleMic, keyBindFullscreen, keyBindSpectatorOutlines}, keyBindsHotbar);
        difficulty = EnumDifficulty.NORMAL;
        lastServer = "";
        fovSetting = 70.0F;
        language = "en_US";
        forceUnicodeFont = false;
        mc = mcIn;
        optionsFile = new File(p_i46326_2_, "options.txt");
        optionsFileOF = new File(p_i46326_2_, "optionsof.txt");
        limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
        ofKeyBindZoom = new KeyBinding("of.key.zoom", 46, "key.categories.misc");
        keyBindings = ArrayUtils.add(keyBindings, ofKeyBindZoom);
        GameSettings.Options.RENDER_DISTANCE.setValueMax(32.0F);
        renderDistanceChunks = 8;
        loadOptions();
        Config.initGameSettings(this);
    }

    public GameSettings()
    {
        keyBindings = ArrayUtils.addAll(new KeyBinding[] {keyBindAttack, keyBindUseItem, keyBindForward, keyBindLeft, keyBindBack, keyBindRight, keyBindJump, keyBindSneak, keyBindSprint, keyBindDrop, keyBindInventory, keyBindChat, keyBindPlayerList, keyBindPickBlock, keyBindCommand, keyBindScreenshot, keyBindTogglePerspective, keyBindSmoothCamera, keyBindStreamStartStop, keyBindStreamPauseUnpause, keyBindStreamCommercials, keyBindStreamToggleMic, keyBindFullscreen, keyBindSpectatorOutlines}, keyBindsHotbar);
        difficulty = EnumDifficulty.NORMAL;
        lastServer = "";
        fovSetting = 70.0F;
        language = "en_US";
        forceUnicodeFont = false;
    }

    /**
     * Represents a key or mouse button as a string. Args: key
     */
    public static String getKeyDisplayString(int p_74298_0_)
    {
        return p_74298_0_ < 0 ? I18n.format("key.mouseButton", Integer.valueOf(p_74298_0_ + 101)): (p_74298_0_ < 256 ? Keyboard.getKeyName(p_74298_0_) : String.format("%c", Character.valueOf((char)(p_74298_0_ - 256))).toUpperCase());
    }

    /**
     * Returns whether the specified key binding is currently being pressed.
     */
    public static boolean isKeyDown(KeyBinding p_100015_0_)
    {
        int i = p_100015_0_.getKeyCode();
        return i >= -100 && i <= 255 && (p_100015_0_.getKeyCode() != 0 && (p_100015_0_.getKeyCode() < 0 ? Mouse.isButtonDown(p_100015_0_.getKeyCode() + 100) : Keyboard.isKeyDown(p_100015_0_.getKeyCode())));
    }

    /**
     * Sets a key binding and then saves all settings.
     */
    public void setOptionKeyBinding(KeyBinding p_151440_1_, int p_151440_2_)
    {
        p_151440_1_.setKeyCode(p_151440_2_);
        saveOptions();
    }

    /**
     * If the specified option is controlled by a slider (float value), this will set the float value.
     */
    public void setOptionFloatValue(GameSettings.Options p_74304_1_, float p_74304_2_)
    {
        setOptionFloatValueOF(p_74304_1_, p_74304_2_);

        if (p_74304_1_ == GameSettings.Options.SENSITIVITY)
        {
            mouseSensitivity = p_74304_2_;
        }

        if (p_74304_1_ == GameSettings.Options.FOV)
        {
            fovSetting = p_74304_2_;
        }

        if (p_74304_1_ == GameSettings.Options.GAMMA)
        {
            gammaSetting = p_74304_2_;
        }

        if (p_74304_1_ == GameSettings.Options.FRAMERATE_LIMIT)
        {
            limitFramerate = (int)p_74304_2_;
            enableVsync = false;

            if (limitFramerate <= 0)
            {
                limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                enableVsync = true;
            }

            updateVSync();
        }

        if (p_74304_1_ == GameSettings.Options.CHAT_OPACITY)
        {
            chatOpacity = p_74304_2_;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (p_74304_1_ == GameSettings.Options.CHAT_HEIGHT_FOCUSED)
        {
            chatHeightFocused = p_74304_2_;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (p_74304_1_ == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED)
        {
            chatHeightUnfocused = p_74304_2_;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (p_74304_1_ == GameSettings.Options.CHAT_WIDTH)
        {
            chatWidth = p_74304_2_;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (p_74304_1_ == GameSettings.Options.CHAT_SCALE)
        {
            chatScale = p_74304_2_;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (p_74304_1_ == GameSettings.Options.MIPMAP_LEVELS)
        {
            int i = mipmapLevels;
            mipmapLevels = (int)p_74304_2_;

            if ((float)i != p_74304_2_)
            {
                mc.getTextureMapBlocks().setMipmapLevels(mipmapLevels);
                mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
                mc.getTextureMapBlocks().setBlurMipmapDirect(false, mipmapLevels > 0);
                mc.scheduleResourcesRefresh();
            }
        }

        if (p_74304_1_ == GameSettings.Options.BLOCK_ALTERNATIVES)
        {
            allowBlockAlternatives = !allowBlockAlternatives;
            mc.renderGlobal.loadRenderers();
        }

        if (p_74304_1_ == GameSettings.Options.RENDER_DISTANCE)
        {
            renderDistanceChunks = (int)p_74304_2_;
            mc.renderGlobal.setDisplayListEntitiesDirty();
        }

        if (p_74304_1_ == GameSettings.Options.STREAM_BYTES_PER_PIXEL)
        {
            streamBytesPerPixel = p_74304_2_;
        }

        if (p_74304_1_ == GameSettings.Options.STREAM_VOLUME_MIC)
        {
            streamMicVolume = p_74304_2_;
            mc.getTwitchStream().updateStreamVolume();
        }

        if (p_74304_1_ == GameSettings.Options.STREAM_VOLUME_SYSTEM)
        {
            streamGameVolume = p_74304_2_;
            mc.getTwitchStream().updateStreamVolume();
        }

        if (p_74304_1_ == GameSettings.Options.STREAM_KBPS)
        {
            streamKbps = p_74304_2_;
        }

        if (p_74304_1_ == GameSettings.Options.STREAM_FPS)
        {
            streamFps = p_74304_2_;
        }
    }

    /**
     * For non-float options. Toggles the option on/off, or cycles through the list i.e. render distances.
     */
    public void setOptionValue(GameSettings.Options p_74306_1_, int p_74306_2_)
    {
        setOptionValueOF(p_74306_1_, p_74306_2_);

        if (p_74306_1_ == GameSettings.Options.INVERT_MOUSE)
        {
            invertMouse = !invertMouse;
        }

        if (p_74306_1_ == GameSettings.Options.GUI_SCALE)
        {
            guiScale = guiScale + p_74306_2_ & 3;
        }

        if (p_74306_1_ == GameSettings.Options.PARTICLES)
        {
            particleSetting = (particleSetting + p_74306_2_) % 3;
        }

        if (p_74306_1_ == GameSettings.Options.VIEW_BOBBING)
        {
            viewBobbing = !viewBobbing;
        }

        if (p_74306_1_ == GameSettings.Options.RENDER_CLOUDS)
        {
            clouds = (clouds + p_74306_2_) % 3;
        }

        if (p_74306_1_ == GameSettings.Options.FORCE_UNICODE_FONT)
        {
            forceUnicodeFont = !forceUnicodeFont;
            mc.fontRendererObj.setUnicodeFlag(mc.getLanguageManager().isCurrentLocaleUnicode() || forceUnicodeFont);
        }

        if (p_74306_1_ == GameSettings.Options.FBO_ENABLE)
        {
            fboEnable = !fboEnable;
        }

        if (p_74306_1_ == GameSettings.Options.ANAGLYPH)
        {
            if (!anaglyph && Config.isShaders())
            {
                Config.showGuiMessage(Lang.get("of.message.an.shaders1"), Lang.get("of.message.an.shaders2"));
                return;
            }

            anaglyph = !anaglyph;
            mc.refreshResources();
        }

        if (p_74306_1_ == GameSettings.Options.GRAPHICS)
        {
            fancyGraphics = !fancyGraphics;
            updateRenderClouds();
            mc.renderGlobal.loadRenderers();
        }

        if (p_74306_1_ == GameSettings.Options.AMBIENT_OCCLUSION)
        {
            ambientOcclusion = (ambientOcclusion + p_74306_2_) % 3;
            mc.renderGlobal.loadRenderers();
        }

        if (p_74306_1_ == GameSettings.Options.CHAT_VISIBILITY)
        {
            chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility((chatVisibility.getChatVisibility() + p_74306_2_) % 3);
        }

        if (p_74306_1_ == GameSettings.Options.STREAM_COMPRESSION)
        {
            streamCompression = (streamCompression + p_74306_2_) % 3;
        }

        if (p_74306_1_ == GameSettings.Options.STREAM_SEND_METADATA)
        {
            streamSendMetadata = !streamSendMetadata;
        }

        if (p_74306_1_ == GameSettings.Options.STREAM_CHAT_ENABLED)
        {
            streamChatEnabled = (streamChatEnabled + p_74306_2_) % 3;
        }

        if (p_74306_1_ == GameSettings.Options.STREAM_CHAT_USER_FILTER)
        {
            streamChatUserFilter = (streamChatUserFilter + p_74306_2_) % 3;
        }

        if (p_74306_1_ == GameSettings.Options.STREAM_MIC_TOGGLE_BEHAVIOR)
        {
            streamMicToggleBehavior = (streamMicToggleBehavior + p_74306_2_) % 2;
        }

        if (p_74306_1_ == GameSettings.Options.CHAT_COLOR)
        {
            chatColours = !chatColours;
        }

        if (p_74306_1_ == GameSettings.Options.CHAT_LINKS)
        {
            chatLinks = !chatLinks;
        }

        if (p_74306_1_ == GameSettings.Options.CHAT_LINKS_PROMPT)
        {
            chatLinksPrompt = !chatLinksPrompt;
        }

        if (p_74306_1_ == GameSettings.Options.SNOOPER_ENABLED)
        {
            snooperEnabled = !snooperEnabled;
        }

        if (p_74306_1_ == GameSettings.Options.TOUCHSCREEN)
        {
            touchscreen = !touchscreen;
        }

        if (p_74306_1_ == GameSettings.Options.USE_FULLSCREEN)
        {
            fullScreen = !fullScreen;

            if (mc.isFullScreen() != fullScreen)
            {
                mc.toggleFullscreen();
            }
        }

        if (p_74306_1_ == GameSettings.Options.ENABLE_VSYNC)
        {
            enableVsync = !enableVsync;
            Display.setVSyncEnabled(enableVsync);
        }

        if (p_74306_1_ == GameSettings.Options.USE_VBO)
        {
            useVbo = !useVbo;
            mc.renderGlobal.loadRenderers();
        }

        if (p_74306_1_ == GameSettings.Options.BLOCK_ALTERNATIVES)
        {
            allowBlockAlternatives = !allowBlockAlternatives;
            mc.renderGlobal.loadRenderers();
        }

        if (p_74306_1_ == GameSettings.Options.REDUCED_DEBUG_INFO)
        {
            reducedDebugInfo = !reducedDebugInfo;
        }

        if (p_74306_1_ == GameSettings.Options.ENTITY_SHADOWS)
        {
            field_181151_V = !field_181151_V;
        }

        saveOptions();
    }

    public float getOptionFloatValue(GameSettings.Options p_74296_1_)
    {
        return p_74296_1_ == GameSettings.Options.CLOUD_HEIGHT ? ofCloudsHeight : (p_74296_1_ == GameSettings.Options.AO_LEVEL ? ofAoLevel : (p_74296_1_ == GameSettings.Options.AA_LEVEL ? (float) ofAaLevel : (p_74296_1_ == GameSettings.Options.AF_LEVEL ? (float) ofAfLevel : (p_74296_1_ == GameSettings.Options.MIPMAP_TYPE ? (float) ofMipmapType : (p_74296_1_ == GameSettings.Options.FRAMERATE_LIMIT ? ((float) limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() && enableVsync ? 0.0F : (float) limitFramerate) : (p_74296_1_ == GameSettings.Options.FOV ? fovSetting : (p_74296_1_ == GameSettings.Options.GAMMA ? gammaSetting : (p_74296_1_ == GameSettings.Options.SATURATION ? saturation : (p_74296_1_ == GameSettings.Options.SENSITIVITY ? mouseSensitivity : (p_74296_1_ == GameSettings.Options.CHAT_OPACITY ? chatOpacity : (p_74296_1_ == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? chatHeightFocused : (p_74296_1_ == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? chatHeightUnfocused : (p_74296_1_ == GameSettings.Options.CHAT_SCALE ? chatScale : (p_74296_1_ == GameSettings.Options.CHAT_WIDTH ? chatWidth : (p_74296_1_ == GameSettings.Options.FRAMERATE_LIMIT ? (float) limitFramerate : (p_74296_1_ == GameSettings.Options.MIPMAP_LEVELS ? (float) mipmapLevels : (p_74296_1_ == GameSettings.Options.RENDER_DISTANCE ? (float) renderDistanceChunks : (p_74296_1_ == GameSettings.Options.STREAM_BYTES_PER_PIXEL ? streamBytesPerPixel : (p_74296_1_ == GameSettings.Options.STREAM_VOLUME_MIC ? streamMicVolume : (p_74296_1_ == GameSettings.Options.STREAM_VOLUME_SYSTEM ? streamGameVolume : (p_74296_1_ == GameSettings.Options.STREAM_KBPS ? streamKbps : (p_74296_1_ == GameSettings.Options.STREAM_FPS ? streamFps : 0.0F))))))))))))))))))))));
    }

    public boolean getOptionOrdinalValue(GameSettings.Options p_74308_1_)
    {
        switch (GameSettings.GameSettings$2.field_151477_a[p_74308_1_.ordinal()])
        {
            case 1:
                return invertMouse;

            case 2:
                return viewBobbing;

            case 3:
                return anaglyph;

            case 4:
                return fboEnable;

            case 5:
                return chatColours;

            case 6:
                return chatLinks;

            case 7:
                return chatLinksPrompt;

            case 8:
                return snooperEnabled;

            case 9:
                return fullScreen;

            case 10:
                return enableVsync;

            case 11:
                return useVbo;

            case 12:
                return touchscreen;

            case 13:
                return streamSendMetadata;

            case 14:
                return forceUnicodeFont;

            case 15:
                return allowBlockAlternatives;

            case 16:
                return reducedDebugInfo;

            case 17:
                return field_181151_V;

            default:
                return false;
        }
    }

    /**
     * Returns the translation of the given index in the given String array. If the index is smaller than 0 or greater
     * than/equal to the length of the String array, it is changed to 0.
     */
    private static String getTranslation(String[] p_74299_0_, int p_74299_1_)
    {
        if (p_74299_1_ < 0 || p_74299_1_ >= p_74299_0_.length)
        {
            p_74299_1_ = 0;
        }

        return I18n.format(p_74299_0_[p_74299_1_]);
    }

    /**
     * Gets a key binding.
     */
    public String getKeyBinding(GameSettings.Options p_74297_1_)
    {
        String s = getKeyBindingOF(p_74297_1_);

        if (s != null)
        {
            return s;
        }
        else
        {
            String s1 = I18n.format(p_74297_1_.getEnumString()) + ": ";

            if (p_74297_1_.getEnumFloat())
            {
                float f1 = getOptionFloatValue(p_74297_1_);
                float f = p_74297_1_.normalizeValue(f1);
                return p_74297_1_ == GameSettings.Options.SENSITIVITY ? (f == 0.0F ? s1 + I18n.format("options.sensitivity.min") : (f == 1.0F ? s1 + I18n.format("options.sensitivity.max") : s1 + (int)(f * 200.0F) + "%")) : (p_74297_1_ == GameSettings.Options.FOV ? (f1 == 70.0F ? s1 + I18n.format("options.fov.min") : (f1 == 110.0F ? s1 + I18n.format("options.fov.max") : s1 + (int)f1)) : (p_74297_1_ == GameSettings.Options.FRAMERATE_LIMIT ? (f1 == p_74297_1_.valueMax ? s1 + I18n.format("options.framerateLimit.max") : s1 + (int)f1 + " fps") : (p_74297_1_ == GameSettings.Options.RENDER_CLOUDS ? (f1 == p_74297_1_.valueMin ? s1 + I18n.format("options.cloudHeight.min") : s1 + ((int)f1 + 128)) : (p_74297_1_ == GameSettings.Options.GAMMA ? (f == 0.0F ? s1 + I18n.format("options.gamma.min") : (f == 1.0F ? s1 + I18n.format("options.gamma.max") : s1 + "+" + (int)(f * 100.0F) + "%")) : (p_74297_1_ == GameSettings.Options.SATURATION ? s1 + (int)(f * 400.0F) + "%" : (p_74297_1_ == GameSettings.Options.CHAT_OPACITY ? s1 + (int)(f * 90.0F + 10.0F) + "%" : (p_74297_1_ == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? s1 + GuiNewChat.calculateChatboxHeight(f) + "px" : (p_74297_1_ == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? s1 + GuiNewChat.calculateChatboxHeight(f) + "px" : (p_74297_1_ == GameSettings.Options.CHAT_WIDTH ? s1 + GuiNewChat.calculateChatboxWidth(f) + "px" : (p_74297_1_ == GameSettings.Options.RENDER_DISTANCE ? s1 + (int)f1 + " chunks" : (p_74297_1_ == GameSettings.Options.MIPMAP_LEVELS ? (f1 == 0.0F ? s1 + I18n.format("options.off") : s1 + (int)f1) : (p_74297_1_ == GameSettings.Options.STREAM_FPS ? s1 + TwitchStream.formatStreamFps(f) + " fps" : (p_74297_1_ == GameSettings.Options.STREAM_KBPS ? s1 + TwitchStream.formatStreamKbps(f) + " Kbps" : (p_74297_1_ == GameSettings.Options.STREAM_BYTES_PER_PIXEL ? s1 + String.format("%.3f bpp", Float.valueOf(TwitchStream.formatStreamBps(f))): (f == 0.0F ? s1 + I18n.format("options.off") : s1 + (int)(f * 100.0F) + "%")))))))))))))));
            }
            else if (p_74297_1_.getEnumBoolean())
            {
                boolean flag = getOptionOrdinalValue(p_74297_1_);
                return flag ? s1 + I18n.format("options.on") : s1 + I18n.format("options.off");
            }
            else if (p_74297_1_ == GameSettings.Options.GUI_SCALE)
            {
                return s1 + GameSettings.getTranslation(GameSettings.GUISCALES, guiScale);
            }
            else if (p_74297_1_ == GameSettings.Options.CHAT_VISIBILITY)
            {
                return s1 + I18n.format(chatVisibility.getResourceKey());
            }
            else if (p_74297_1_ == GameSettings.Options.PARTICLES)
            {
                return s1 + GameSettings.getTranslation(GameSettings.PARTICLES, particleSetting);
            }
            else if (p_74297_1_ == GameSettings.Options.AMBIENT_OCCLUSION)
            {
                return s1 + GameSettings.getTranslation(GameSettings.AMBIENT_OCCLUSIONS, ambientOcclusion);
            }
            else if (p_74297_1_ == GameSettings.Options.STREAM_COMPRESSION)
            {
                return s1 + GameSettings.getTranslation(GameSettings.STREAM_COMPRESSIONS, streamCompression);
            }
            else if (p_74297_1_ == GameSettings.Options.STREAM_CHAT_ENABLED)
            {
                return s1 + GameSettings.getTranslation(GameSettings.STREAM_CHAT_MODES, streamChatEnabled);
            }
            else if (p_74297_1_ == GameSettings.Options.STREAM_CHAT_USER_FILTER)
            {
                return s1 + GameSettings.getTranslation(GameSettings.STREAM_CHAT_FILTER_MODES, streamChatUserFilter);
            }
            else if (p_74297_1_ == GameSettings.Options.STREAM_MIC_TOGGLE_BEHAVIOR)
            {
                return s1 + GameSettings.getTranslation(GameSettings.STREAM_MIC_MODES, streamMicToggleBehavior);
            }
            else if (p_74297_1_ == GameSettings.Options.RENDER_CLOUDS)
            {
                return s1 + GameSettings.getTranslation(GameSettings.field_181149_aW, clouds);
            }
            else if (p_74297_1_ == GameSettings.Options.GRAPHICS)
            {
                if (fancyGraphics)
                {
                    return s1 + I18n.format("options.graphics.fancy");
                }
                else
                {
                    String s2 = "options.graphics.fast";
                    return s1 + I18n.format("options.graphics.fast");
                }
            }
            else
            {
                return s1;
            }
        }
    }

    /**
     * Loads the options from the options file. It appears that this has replaced the previous 'loadOptions'
     */
    public void loadOptions()
    {
        try
        {
            if (!optionsFile.exists())
            {
                return;
            }

            BufferedReader bufferedreader = new BufferedReader(new FileReader(optionsFile));
            String s = "";
            mapSoundLevels.clear();

            while ((s = bufferedreader.readLine()) != null)
            {
                try
                {
                    String[] astring = s.split(":");

                    if (astring[0].equals("mouseSensitivity"))
                    {
                        mouseSensitivity = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("fov"))
                    {
                        fovSetting = parseFloat(astring[1]) * 40.0F + 70.0F;
                    }

                    if (astring[0].equals("gamma"))
                    {
                        gammaSetting = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("saturation"))
                    {
                        saturation = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("invertYMouse"))
                    {
                        invertMouse = astring[1].equals("true");
                    }

                    if (astring[0].equals("renderDistance"))
                    {
                        renderDistanceChunks = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("guiScale"))
                    {
                        guiScale = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("particles"))
                    {
                        particleSetting = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("bobView"))
                    {
                        viewBobbing = astring[1].equals("true");
                    }

                    if (astring[0].equals("anaglyph3d"))
                    {
                        anaglyph = astring[1].equals("true");
                    }

                    if (astring[0].equals("maxFps"))
                    {
                        limitFramerate = Integer.parseInt(astring[1]);
                        enableVsync = false;

                        if (limitFramerate <= 0)
                        {
                            limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                            enableVsync = true;
                        }

                        updateVSync();
                    }

                    if (astring[0].equals("fboEnable"))
                    {
                        fboEnable = astring[1].equals("true");
                    }

                    if (astring[0].equals("difficulty"))
                    {
                        difficulty = EnumDifficulty.getDifficultyEnum(Integer.parseInt(astring[1]));
                    }

                    if (astring[0].equals("fancyGraphics"))
                    {
                        fancyGraphics = astring[1].equals("true");
                        updateRenderClouds();
                    }

                    if (astring[0].equals("ao"))
                    {
                        if (astring[1].equals("true"))
                        {
                            ambientOcclusion = 2;
                        }
                        else if (astring[1].equals("false"))
                        {
                            ambientOcclusion = 0;
                        }
                        else
                        {
                            ambientOcclusion = Integer.parseInt(astring[1]);
                        }
                    }

                    if (astring[0].equals("renderClouds"))
                    {
                        if (astring[1].equals("true"))
                        {
                            clouds = 2;
                        }
                        else if (astring[1].equals("false"))
                        {
                            clouds = 0;
                        }
                        else if (astring[1].equals("fast"))
                        {
                            clouds = 1;
                        }
                    }

                    if (astring[0].equals("resourcePacks"))
                    {
                        resourcePacks = GameSettings.gson.fromJson(s.substring(s.indexOf(58) + 1), GameSettings.typeListString);

                        if (resourcePacks == null)
                        {
                            resourcePacks = Lists.newArrayList();
                        }
                    }

                    if (astring[0].equals("incompatibleResourcePacks"))
                    {
                        field_183018_l = GameSettings.gson.fromJson(s.substring(s.indexOf(58) + 1), GameSettings.typeListString);

                        if (field_183018_l == null)
                        {
                            field_183018_l = Lists.newArrayList();
                        }
                    }

                    if (astring[0].equals("lastServer") && astring.length >= 2)
                    {
                        lastServer = s.substring(s.indexOf(58) + 1);
                    }

                    if (astring[0].equals("lang") && astring.length >= 2)
                    {
                        language = astring[1];
                    }

                    if (astring[0].equals("chatVisibility"))
                    {
                        chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility(Integer.parseInt(astring[1]));
                    }

                    if (astring[0].equals("chatColors"))
                    {
                        chatColours = astring[1].equals("true");
                    }

                    if (astring[0].equals("chatLinks"))
                    {
                        chatLinks = astring[1].equals("true");
                    }

                    if (astring[0].equals("chatLinksPrompt"))
                    {
                        chatLinksPrompt = astring[1].equals("true");
                    }

                    if (astring[0].equals("chatOpacity"))
                    {
                        chatOpacity = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("snooperEnabled"))
                    {
                        snooperEnabled = astring[1].equals("true");
                    }

                    if (astring[0].equals("fullscreen"))
                    {
                        fullScreen = astring[1].equals("true");
                    }

                    if (astring[0].equals("enableVsync"))
                    {
                        enableVsync = astring[1].equals("true");
                        updateVSync();
                    }

                    if (astring[0].equals("useVbo"))
                    {
                        useVbo = astring[1].equals("true");
                    }

                    if (astring[0].equals("hideServerAddress"))
                    {
                        hideServerAddress = astring[1].equals("true");
                    }

                    if (astring[0].equals("advancedItemTooltips"))
                    {
                        advancedItemTooltips = astring[1].equals("true");
                    }

                    if (astring[0].equals("pauseOnLostFocus"))
                    {
                        pauseOnLostFocus = astring[1].equals("true");
                    }

                    if (astring[0].equals("touchscreen"))
                    {
                        touchscreen = astring[1].equals("true");
                    }

                    if (astring[0].equals("overrideHeight"))
                    {
                        overrideHeight = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("overrideWidth"))
                    {
                        overrideWidth = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("heldItemTooltips"))
                    {
                        heldItemTooltips = astring[1].equals("true");
                    }

                    if (astring[0].equals("chatHeightFocused"))
                    {
                        chatHeightFocused = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("chatHeightUnfocused"))
                    {
                        chatHeightUnfocused = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("chatScale"))
                    {
                        chatScale = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("chatWidth"))
                    {
                        chatWidth = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("showInventoryAchievementHint"))
                    {
                        showInventoryAchievementHint = astring[1].equals("true");
                    }

                    if (astring[0].equals("mipmapLevels"))
                    {
                        mipmapLevels = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("streamBytesPerPixel"))
                    {
                        streamBytesPerPixel = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("streamMicVolume"))
                    {
                        streamMicVolume = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("streamSystemVolume"))
                    {
                        streamGameVolume = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("streamKbps"))
                    {
                        streamKbps = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("streamFps"))
                    {
                        streamFps = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("streamCompression"))
                    {
                        streamCompression = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("streamSendMetadata"))
                    {
                        streamSendMetadata = astring[1].equals("true");
                    }

                    if (astring[0].equals("streamPreferredServer") && astring.length >= 2)
                    {
                        streamPreferredServer = s.substring(s.indexOf(58) + 1);
                    }

                    if (astring[0].equals("streamChatEnabled"))
                    {
                        streamChatEnabled = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("streamChatUserFilter"))
                    {
                        streamChatUserFilter = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("streamMicToggleBehavior"))
                    {
                        streamMicToggleBehavior = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("forceUnicodeFont"))
                    {
                        forceUnicodeFont = astring[1].equals("true");
                    }

                    if (astring[0].equals("allowBlockAlternatives"))
                    {
                        allowBlockAlternatives = astring[1].equals("true");
                    }

                    if (astring[0].equals("reducedDebugInfo"))
                    {
                        reducedDebugInfo = astring[1].equals("true");
                    }

                    if (astring[0].equals("useNativeTransport"))
                    {
                        field_181150_U = astring[1].equals("true");
                    }

                    if (astring[0].equals("entityShadows"))
                    {
                        field_181151_V = astring[1].equals("true");
                    }

                    for (KeyBinding keybinding : keyBindings)
                    {
                        if (astring[0].equals("key_" + keybinding.getKeyDescription()))
                        {
                            keybinding.setKeyCode(Integer.parseInt(astring[1]));
                        }
                    }

                    for (SoundCategory soundcategory : SoundCategory.values())
                    {
                        if (astring[0].equals("soundCategory_" + soundcategory.getCategoryName()))
                        {
                            mapSoundLevels.put(soundcategory, Float.valueOf(parseFloat(astring[1])));
                        }
                    }

                    for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values())
                    {
                        if (astring[0].equals("modelPart_" + enumplayermodelparts.getPartName()))
                        {
                            setModelPartEnabled(enumplayermodelparts, astring[1].equals("true"));
                        }
                    }
                }
                catch (Exception exception)
                {
                    GameSettings.logger.warn("Skipping bad option: " + s);
                    exception.printStackTrace();
                }
            }

            KeyBinding.resetKeyBindingArrayAndHash();
            bufferedreader.close();
        }
        catch (Exception exception1)
        {
            GameSettings.logger.error("Failed to load options", exception1);
        }

        loadOfOptions();
    }

    /**
     * Parses a string into a float.
     */
    private float parseFloat(String p_74305_1_)
    {
        return p_74305_1_.equals("true") ? 1.0F : (p_74305_1_.equals("false") ? 0.0F : Float.parseFloat(p_74305_1_));
    }

    /**
     * Saves the options to the options file.
     */
    public void saveOptions()
    {
        if (Reflector.FMLClientHandler.exists())
        {
            Object object = Reflector.call(Reflector.FMLClientHandler_instance);

            if (object != null && Reflector.callBoolean(object, Reflector.FMLClientHandler_isLoading))
            {
                return;
            }
        }

        try
        {
            PrintWriter printwriter = new PrintWriter(new FileWriter(optionsFile));
            printwriter.println("invertYMouse:" + invertMouse);
            printwriter.println("mouseSensitivity:" + mouseSensitivity);
            printwriter.println("fov:" + (fovSetting - 70.0F) / 40.0F);
            printwriter.println("gamma:" + gammaSetting);
            printwriter.println("saturation:" + saturation);
            printwriter.println("renderDistance:" + renderDistanceChunks);
            printwriter.println("guiScale:" + guiScale);
            printwriter.println("particles:" + particleSetting);
            printwriter.println("bobView:" + viewBobbing);
            printwriter.println("anaglyph3d:" + anaglyph);
            printwriter.println("maxFps:" + limitFramerate);
            printwriter.println("fboEnable:" + fboEnable);
            printwriter.println("difficulty:" + difficulty.getDifficultyId());
            printwriter.println("fancyGraphics:" + fancyGraphics);
            printwriter.println("ao:" + ambientOcclusion);

            switch (clouds)
            {
                case 0:
                    printwriter.println("renderClouds:false");
                    break;

                case 1:
                    printwriter.println("renderClouds:fast");
                    break;

                case 2:
                    printwriter.println("renderClouds:true");
            }

            printwriter.println("resourcePacks:" + GameSettings.gson.toJson(resourcePacks));
            printwriter.println("incompatibleResourcePacks:" + GameSettings.gson.toJson(field_183018_l));
            printwriter.println("lastServer:" + lastServer);
            printwriter.println("lang:" + language);
            printwriter.println("chatVisibility:" + chatVisibility.getChatVisibility());
            printwriter.println("chatColors:" + chatColours);
            printwriter.println("chatLinks:" + chatLinks);
            printwriter.println("chatLinksPrompt:" + chatLinksPrompt);
            printwriter.println("chatOpacity:" + chatOpacity);
            printwriter.println("snooperEnabled:" + snooperEnabled);
            printwriter.println("fullscreen:" + fullScreen);
            printwriter.println("enableVsync:" + enableVsync);
            printwriter.println("useVbo:" + useVbo);
            printwriter.println("hideServerAddress:" + hideServerAddress);
            printwriter.println("advancedItemTooltips:" + advancedItemTooltips);
            printwriter.println("pauseOnLostFocus:" + pauseOnLostFocus);
            printwriter.println("touchscreen:" + touchscreen);
            printwriter.println("overrideWidth:" + overrideWidth);
            printwriter.println("overrideHeight:" + overrideHeight);
            printwriter.println("heldItemTooltips:" + heldItemTooltips);
            printwriter.println("chatHeightFocused:" + chatHeightFocused);
            printwriter.println("chatHeightUnfocused:" + chatHeightUnfocused);
            printwriter.println("chatScale:" + chatScale);
            printwriter.println("chatWidth:" + chatWidth);
            printwriter.println("showInventoryAchievementHint:" + showInventoryAchievementHint);
            printwriter.println("mipmapLevels:" + mipmapLevels);
            printwriter.println("streamBytesPerPixel:" + streamBytesPerPixel);
            printwriter.println("streamMicVolume:" + streamMicVolume);
            printwriter.println("streamSystemVolume:" + streamGameVolume);
            printwriter.println("streamKbps:" + streamKbps);
            printwriter.println("streamFps:" + streamFps);
            printwriter.println("streamCompression:" + streamCompression);
            printwriter.println("streamSendMetadata:" + streamSendMetadata);
            printwriter.println("streamPreferredServer:" + streamPreferredServer);
            printwriter.println("streamChatEnabled:" + streamChatEnabled);
            printwriter.println("streamChatUserFilter:" + streamChatUserFilter);
            printwriter.println("streamMicToggleBehavior:" + streamMicToggleBehavior);
            printwriter.println("forceUnicodeFont:" + forceUnicodeFont);
            printwriter.println("allowBlockAlternatives:" + allowBlockAlternatives);
            printwriter.println("reducedDebugInfo:" + reducedDebugInfo);
            printwriter.println("useNativeTransport:" + field_181150_U);
            printwriter.println("entityShadows:" + field_181151_V);

            for (KeyBinding keybinding : keyBindings)
            {
                printwriter.println("key_" + keybinding.getKeyDescription() + ":" + keybinding.getKeyCode());
            }

            for (SoundCategory soundcategory : SoundCategory.values())
            {
                printwriter.println("soundCategory_" + soundcategory.getCategoryName() + ":" + getSoundLevel(soundcategory));
            }

            for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values())
            {
                printwriter.println("modelPart_" + enumplayermodelparts.getPartName() + ":" + setModelParts.contains(enumplayermodelparts));
            }

            printwriter.close();
        }
        catch (Exception exception)
        {
            GameSettings.logger.error("Failed to save options", exception);
        }

        saveOfOptions();
        sendSettingsToServer();
    }

    public float getSoundLevel(SoundCategory p_151438_1_)
    {
        return mapSoundLevels.containsKey(p_151438_1_) ? ((Float) mapSoundLevels.get(p_151438_1_)).floatValue() : 1.0F;
    }

    public void setSoundLevel(SoundCategory p_151439_1_, float p_151439_2_)
    {
        mc.getSoundHandler().setSoundLevel(p_151439_1_, p_151439_2_);
        mapSoundLevels.put(p_151439_1_, Float.valueOf(p_151439_2_));
    }

    /**
     * Send a client info packet with settings information to the server
     */
    public void sendSettingsToServer()
    {
        if (mc.thePlayer != null)
        {
            int i = 0;

            for (Object enumplayermodelparts : setModelParts)
            {
                i |= ((EnumPlayerModelParts) enumplayermodelparts).getPartMask();
            }

            mc.thePlayer.sendQueue.addToSendQueue(new C15PacketClientSettings(language, renderDistanceChunks, chatVisibility, chatColours, i));
        }
    }

    public Set getModelParts()
    {
        return ImmutableSet.copyOf(setModelParts);
    }

    public void setModelPartEnabled(EnumPlayerModelParts p_178878_1_, boolean p_178878_2_)
    {
        if (p_178878_2_)
        {
            setModelParts.add(p_178878_1_);
        }
        else
        {
            setModelParts.remove(p_178878_1_);
        }

        sendSettingsToServer();
    }

    public void switchModelPartEnabled(EnumPlayerModelParts p_178877_1_)
    {
        if (!getModelParts().contains(p_178877_1_))
        {
            setModelParts.add(p_178877_1_);
        }
        else
        {
            setModelParts.remove(p_178877_1_);
        }

        sendSettingsToServer();
    }

    public int func_181147_e()
    {
        return renderDistanceChunks >= 4 ? clouds : 0;
    }

    public boolean func_181148_f()
    {
        return field_181150_U;
    }

    private void setOptionFloatValueOF(GameSettings.Options p_setOptionFloatValueOF_1_, float p_setOptionFloatValueOF_2_)
    {
        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.CLOUD_HEIGHT)
        {
            ofCloudsHeight = p_setOptionFloatValueOF_2_;
            mc.renderGlobal.resetClouds();
        }

        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.AO_LEVEL)
        {
            ofAoLevel = p_setOptionFloatValueOF_2_;
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.AA_LEVEL)
        {
            int i = (int)p_setOptionFloatValueOF_2_;

            if (i > 0 && Config.isShaders())
            {
                Config.showGuiMessage(Lang.get("of.message.aa.shaders1"), Lang.get("of.message.aa.shaders2"));
                return;
            }

            int[] aint = new int[] {0, 2, 4, 6, 8, 12, 16};
            ofAaLevel = 0;

            for (int j = 0; j < aint.length; ++j)
            {
                if (i >= aint[j])
                {
                    ofAaLevel = aint[j];
                }
            }

            ofAaLevel = Config.limit(ofAaLevel, 0, 16);
        }

        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.AF_LEVEL)
        {
            int k = (int)p_setOptionFloatValueOF_2_;

            if (k > 1 && Config.isShaders())
            {
                Config.showGuiMessage(Lang.get("of.message.af.shaders1"), Lang.get("of.message.af.shaders2"));
                return;
            }

            for (ofAfLevel = 1; ofAfLevel * 2 <= k; ofAfLevel *= 2)
            {
            }

            ofAfLevel = Config.limit(ofAfLevel, 1, 16);
            mc.refreshResources();
        }

        if (p_setOptionFloatValueOF_1_ == GameSettings.Options.MIPMAP_TYPE)
        {
            int l = (int)p_setOptionFloatValueOF_2_;
            ofMipmapType = Config.limit(l, 0, 3);
            mc.refreshResources();
        }
    }

    private void setOptionValueOF(GameSettings.Options p_setOptionValueOF_1_, int p_setOptionValueOF_2_)
    {
        if (p_setOptionValueOF_1_ == GameSettings.Options.FOG_FANCY)
        {
            switch (ofFogType)
            {
                case 1:
                    ofFogType = 2;

                    if (!Config.isFancyFogAvailable())
                    {
                        ofFogType = 3;
                    }

                    break;

                case 2:
                    ofFogType = 3;
                    break;

                case 3:
                    ofFogType = 1;
                    break;

                default:
                    ofFogType = 1;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.FOG_START)
        {
            ofFogStart += 0.2F;

            if (ofFogStart > 0.81F)
            {
                ofFogStart = 0.2F;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SMOOTH_FPS)
        {
            ofSmoothFps = !ofSmoothFps;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SMOOTH_WORLD)
        {
            ofSmoothWorld = !ofSmoothWorld;
            Config.updateThreadPriorities();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CLOUDS)
        {
            ++ofClouds;

            if (ofClouds > 3)
            {
                ofClouds = 0;
            }

            updateRenderClouds();
            mc.renderGlobal.resetClouds();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.TREES)
        {
            ofTrees = GameSettings.nextValue(ofTrees, GameSettings.OF_TREES_VALUES);
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.DROPPED_ITEMS)
        {
            ++ofDroppedItems;

            if (ofDroppedItems > 2)
            {
                ofDroppedItems = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.RAIN)
        {
            ++ofRain;

            if (ofRain > 3)
            {
                ofRain = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_WATER)
        {
            ++ofAnimatedWater;

            if (ofAnimatedWater == 1)
            {
                ++ofAnimatedWater;
            }

            if (ofAnimatedWater > 2)
            {
                ofAnimatedWater = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_LAVA)
        {
            ++ofAnimatedLava;

            if (ofAnimatedLava == 1)
            {
                ++ofAnimatedLava;
            }

            if (ofAnimatedLava > 2)
            {
                ofAnimatedLava = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_FIRE)
        {
            ofAnimatedFire = !ofAnimatedFire;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_PORTAL)
        {
            ofAnimatedPortal = !ofAnimatedPortal;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_REDSTONE)
        {
            ofAnimatedRedstone = !ofAnimatedRedstone;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_EXPLOSION)
        {
            ofAnimatedExplosion = !ofAnimatedExplosion;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_FLAME)
        {
            ofAnimatedFlame = !ofAnimatedFlame;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_SMOKE)
        {
            ofAnimatedSmoke = !ofAnimatedSmoke;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.VOID_PARTICLES)
        {
            ofVoidParticles = !ofVoidParticles;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.WATER_PARTICLES)
        {
            ofWaterParticles = !ofWaterParticles;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.PORTAL_PARTICLES)
        {
            ofPortalParticles = !ofPortalParticles;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.POTION_PARTICLES)
        {
            ofPotionParticles = !ofPotionParticles;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.FIREWORK_PARTICLES)
        {
            ofFireworkParticles = !ofFireworkParticles;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.DRIPPING_WATER_LAVA)
        {
            ofDrippingWaterLava = !ofDrippingWaterLava;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_TERRAIN)
        {
            ofAnimatedTerrain = !ofAnimatedTerrain;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.ANIMATED_TEXTURES)
        {
            ofAnimatedTextures = !ofAnimatedTextures;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.RAIN_SPLASH)
        {
            ofRainSplash = !ofRainSplash;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.LAGOMETER)
        {
            ofLagometer = !ofLagometer;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SHOW_FPS)
        {
            ofShowFps = !ofShowFps;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.AUTOSAVE_TICKS)
        {
            ofAutoSaveTicks *= 10;

            if (ofAutoSaveTicks > 40000)
            {
                ofAutoSaveTicks = 40;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.BETTER_GRASS)
        {
            ++ofBetterGrass;

            if (ofBetterGrass > 3)
            {
                ofBetterGrass = 1;
            }

            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CONNECTED_TEXTURES)
        {
            ++ofConnectedTextures;

            if (ofConnectedTextures > 3)
            {
                ofConnectedTextures = 1;
            }

            if (ofConnectedTextures != 2)
            {
                mc.refreshResources();
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.WEATHER)
        {
            ofWeather = !ofWeather;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SKY)
        {
            ofSky = !ofSky;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.STARS)
        {
            ofStars = !ofStars;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SUN_MOON)
        {
            ofSunMoon = !ofSunMoon;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.VIGNETTE)
        {
            ++ofVignette;

            if (ofVignette > 2)
            {
                ofVignette = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CHUNK_UPDATES)
        {
            ++ofChunkUpdates;

            if (ofChunkUpdates > 5)
            {
                ofChunkUpdates = 1;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CHUNK_UPDATES_DYNAMIC)
        {
            ofChunkUpdatesDynamic = !ofChunkUpdatesDynamic;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.TIME)
        {
            ++ofTime;

            if (ofTime > 2)
            {
                ofTime = 0;
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CLEAR_WATER)
        {
            ofClearWater = !ofClearWater;
            updateWaterOpacity();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.PROFILER)
        {
            ofProfiler = !ofProfiler;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.BETTER_SNOW)
        {
            ofBetterSnow = !ofBetterSnow;
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SWAMP_COLORS)
        {
            ofSwampColors = !ofSwampColors;
            CustomColors.updateUseDefaultGrassFoliageColors();
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.RANDOM_MOBS)
        {
            ofRandomMobs = !ofRandomMobs;
            RandomMobs.resetTextures();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SMOOTH_BIOMES)
        {
            ofSmoothBiomes = !ofSmoothBiomes;
            CustomColors.updateUseDefaultGrassFoliageColors();
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CUSTOM_FONTS)
        {
            ofCustomFonts = !ofCustomFonts;
            mc.fontRendererObj.onResourceManagerReload(Config.getResourceManager());
            mc.standardGalacticFontRenderer.onResourceManagerReload(Config.getResourceManager());
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CUSTOM_COLORS)
        {
            ofCustomColors = !ofCustomColors;
            CustomColors.update();
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CUSTOM_ITEMS)
        {
            ofCustomItems = !ofCustomItems;
            mc.refreshResources();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.CUSTOM_SKY)
        {
            ofCustomSky = !ofCustomSky;
            CustomSky.update();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.SHOW_CAPES)
        {
            ofShowCapes = !ofShowCapes;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.NATURAL_TEXTURES)
        {
            ofNaturalTextures = !ofNaturalTextures;
            NaturalTextures.update();
            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.FAST_MATH)
        {
            ofFastMath = !ofFastMath;
            MathHelper.fastMath = ofFastMath;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.FAST_RENDER)
        {
            if (!ofFastRender && Config.isShaders())
            {
                Config.showGuiMessage(Lang.get("of.message.fr.shaders1"), Lang.get("of.message.fr.shaders2"));
                return;
            }

            ofFastRender = !ofFastRender;

            if (ofFastRender)
            {
                mc.entityRenderer.func_181022_b();
            }

            Config.updateFramebufferSize();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.TRANSLUCENT_BLOCKS)
        {
            if (ofTranslucentBlocks == 0)
            {
                ofTranslucentBlocks = 1;
            }
            else if (ofTranslucentBlocks == 1)
            {
                ofTranslucentBlocks = 2;
            }
            else if (ofTranslucentBlocks == 2)
            {
                ofTranslucentBlocks = 0;
            }
            else
            {
                ofTranslucentBlocks = 0;
            }

            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.LAZY_CHUNK_LOADING)
        {
            ofLazyChunkLoading = !ofLazyChunkLoading;
            Config.updateAvailableProcessors();

            if (!Config.isSingleProcessor())
            {
                ofLazyChunkLoading = false;
            }

            mc.renderGlobal.loadRenderers();
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.FULLSCREEN_MODE)
        {
            List list = Arrays.asList(Config.getDisplayModeNames());

            if (ofFullscreenMode.equals("Default"))
            {
                ofFullscreenMode = (String)list.get(0);
            }
            else
            {
                int i = list.indexOf(ofFullscreenMode);

                if (i < 0)
                {
                    ofFullscreenMode = "Default";
                }
                else
                {
                    ++i;

                    if (i >= list.size())
                    {
                        ofFullscreenMode = "Default";
                    }
                    else
                    {
                        ofFullscreenMode = (String)list.get(i);
                    }
                }
            }
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.DYNAMIC_FOV)
        {
            ofDynamicFov = !ofDynamicFov;
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.DYNAMIC_LIGHTS)
        {
            ofDynamicLights = GameSettings.nextValue(ofDynamicLights, GameSettings.OF_DYNAMIC_LIGHTS);
            DynamicLights.removeLights(mc.renderGlobal);
        }

        if (p_setOptionValueOF_1_ == GameSettings.Options.HELD_ITEM_TOOLTIPS)
        {
            heldItemTooltips = !heldItemTooltips;
        }
    }

    private String getKeyBindingOF(GameSettings.Options p_getKeyBindingOF_1_)
    {
        String s = I18n.format(p_getKeyBindingOF_1_.getEnumString()) + ": ";

        if (s == null)
        {
            s = p_getKeyBindingOF_1_.getEnumString();
        }

        if (p_getKeyBindingOF_1_ == GameSettings.Options.RENDER_DISTANCE)
        {
            int l = (int) getOptionFloatValue(p_getKeyBindingOF_1_);
            String s1 = I18n.format("options.renderDistance.tiny");
            int i = 2;

            if (l >= 4)
            {
                s1 = I18n.format("options.renderDistance.short");
                i = 4;
            }

            if (l >= 8)
            {
                s1 = I18n.format("options.renderDistance.normal");
                i = 8;
            }

            if (l >= 16)
            {
                s1 = I18n.format("options.renderDistance.far");
                i = 16;
            }

            if (l >= 32)
            {
                s1 = Lang.get("of.options.renderDistance.extreme");
                i = 32;
            }

            int j = renderDistanceChunks - i;
            String s2 = s1;

            if (j > 0)
            {
                s2 = s1 + "+";
            }

            return s + l + " " + s2 + "";
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.FOG_FANCY)
        {
            switch (ofFogType)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                case 3:
                    return s + Lang.getOff();

                default:
                    return s + Lang.getOff();
            }
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.FOG_START)
        {
            return s + ofFogStart;
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.MIPMAP_TYPE)
        {
            switch (ofMipmapType)
            {
                case 0:
                    return s + Lang.get("of.options.mipmap.nearest");

                case 1:
                    return s + Lang.get("of.options.mipmap.linear");

                case 2:
                    return s + Lang.get("of.options.mipmap.bilinear");

                case 3:
                    return s + Lang.get("of.options.mipmap.trilinear");

                default:
                    return s + "of.options.mipmap.nearest";
            }
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.SMOOTH_FPS)
        {
            return ofSmoothFps ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.SMOOTH_WORLD)
        {
            return ofSmoothWorld ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.CLOUDS)
        {
            switch (ofClouds)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                case 3:
                    return s + Lang.getOff();

                default:
                    return s + Lang.getDefault();
            }
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.TREES)
        {
            switch (ofTrees)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                case 3:
                default:
                    return s + Lang.getDefault();

                case 4:
                    return s + Lang.get("of.general.smart");
            }
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.DROPPED_ITEMS)
        {
            switch (ofDroppedItems)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                default:
                    return s + Lang.getDefault();
            }
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.RAIN)
        {
            switch (ofRain)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                case 3:
                    return s + Lang.getOff();

                default:
                    return s + Lang.getDefault();
            }
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_WATER)
        {
            switch (ofAnimatedWater)
            {
                case 1:
                    return s + Lang.get("of.options.animation.dynamic");

                case 2:
                    return s + Lang.getOff();

                default:
                    return s + Lang.getOn();
            }
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_LAVA)
        {
            switch (ofAnimatedLava)
            {
                case 1:
                    return s + Lang.get("of.options.animation.dynamic");

                case 2:
                    return s + Lang.getOff();

                default:
                    return s + Lang.getOn();
            }
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_FIRE)
        {
            return ofAnimatedFire ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_PORTAL)
        {
            return ofAnimatedPortal ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_REDSTONE)
        {
            return ofAnimatedRedstone ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_EXPLOSION)
        {
            return ofAnimatedExplosion ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_FLAME)
        {
            return ofAnimatedFlame ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_SMOKE)
        {
            return ofAnimatedSmoke ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.VOID_PARTICLES)
        {
            return ofVoidParticles ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.WATER_PARTICLES)
        {
            return ofWaterParticles ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.PORTAL_PARTICLES)
        {
            return ofPortalParticles ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.POTION_PARTICLES)
        {
            return ofPotionParticles ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.FIREWORK_PARTICLES)
        {
            return ofFireworkParticles ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.DRIPPING_WATER_LAVA)
        {
            return ofDrippingWaterLava ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_TERRAIN)
        {
            return ofAnimatedTerrain ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.ANIMATED_TEXTURES)
        {
            return ofAnimatedTextures ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.RAIN_SPLASH)
        {
            return ofRainSplash ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.LAGOMETER)
        {
            return ofLagometer ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.SHOW_FPS)
        {
            return ofShowFps ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.AUTOSAVE_TICKS)
        {
            return ofAutoSaveTicks <= 40 ? s + Lang.get("of.options.save.default") : (ofAutoSaveTicks <= 400 ? s + Lang.get("of.options.save.20s") : (ofAutoSaveTicks <= 4000 ? s + Lang.get("of.options.save.3min") : s + Lang.get("of.options.save.30min")));
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.BETTER_GRASS)
        {
            switch (ofBetterGrass)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                default:
                    return s + Lang.getOff();
            }
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.CONNECTED_TEXTURES)
        {
            switch (ofConnectedTextures)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                default:
                    return s + Lang.getOff();
            }
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.WEATHER)
        {
            return ofWeather ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.SKY)
        {
            return ofSky ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.STARS)
        {
            return ofStars ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.SUN_MOON)
        {
            return ofSunMoon ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.VIGNETTE)
        {
            switch (ofVignette)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                default:
                    return s + Lang.getDefault();
            }
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.CHUNK_UPDATES)
        {
            return s + ofChunkUpdates;
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.CHUNK_UPDATES_DYNAMIC)
        {
            return ofChunkUpdatesDynamic ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.TIME)
        {
            return ofTime == 1 ? s + Lang.get("of.options.time.dayOnly") : (ofTime == 2 ? s + Lang.get("of.options.time.nightOnly") : s + Lang.getDefault());
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.CLEAR_WATER)
        {
            return ofClearWater ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.AA_LEVEL)
        {
            String s3 = "";

            if (ofAaLevel != Config.getAntialiasingLevel())
            {
                s3 = " (" + Lang.get("of.general.restart") + ")";
            }

            return ofAaLevel == 0 ? s + Lang.getOff() + s3 : s + ofAaLevel + s3;
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.AF_LEVEL)
        {
            return ofAfLevel == 1 ? s + Lang.getOff() : s + ofAfLevel;
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.PROFILER)
        {
            return ofProfiler ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.BETTER_SNOW)
        {
            return ofBetterSnow ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.SWAMP_COLORS)
        {
            return ofSwampColors ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.RANDOM_MOBS)
        {
            return ofRandomMobs ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.SMOOTH_BIOMES)
        {
            return ofSmoothBiomes ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.CUSTOM_FONTS)
        {
            return ofCustomFonts ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.CUSTOM_COLORS)
        {
            return ofCustomColors ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.CUSTOM_SKY)
        {
            return ofCustomSky ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.SHOW_CAPES)
        {
            return ofShowCapes ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.CUSTOM_ITEMS)
        {
            return ofCustomItems ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.NATURAL_TEXTURES)
        {
            return ofNaturalTextures ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.FAST_MATH)
        {
            return ofFastMath ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.FAST_RENDER)
        {
            return ofFastRender ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.TRANSLUCENT_BLOCKS)
        {
            return ofTranslucentBlocks == 1 ? s + Lang.getFast() : (ofTranslucentBlocks == 2 ? s + Lang.getFancy() : s + Lang.getDefault());
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.LAZY_CHUNK_LOADING)
        {
            return ofLazyChunkLoading ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.DYNAMIC_FOV)
        {
            return ofDynamicFov ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.DYNAMIC_LIGHTS)
        {
            int k = GameSettings.indexOf(ofDynamicLights, GameSettings.OF_DYNAMIC_LIGHTS);
            return s + GameSettings.getTranslation(GameSettings.KEYS_DYNAMIC_LIGHTS, k);
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.FULLSCREEN_MODE)
        {
            return ofFullscreenMode.equals("Default") ? s + Lang.getDefault() : s + ofFullscreenMode;
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.HELD_ITEM_TOOLTIPS)
        {
            return heldItemTooltips ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (p_getKeyBindingOF_1_ == GameSettings.Options.FRAMERATE_LIMIT)
        {
            float f = getOptionFloatValue(p_getKeyBindingOF_1_);
            return f == 0.0F ? s + Lang.get("of.options.framerateLimit.vsync") : (f == p_getKeyBindingOF_1_.valueMax ? s + I18n.format("options.framerateLimit.max") : s + (int)f + " fps");
        }
        else
        {
            return null;
        }
    }

    public void loadOfOptions()
    {
        try
        {
            File file1 = optionsFileOF;

            if (!file1.exists())
            {
                file1 = optionsFile;
            }

            if (!file1.exists())
            {
                return;
            }

            BufferedReader bufferedreader = new BufferedReader(new FileReader(file1));
            String s = "";

            while ((s = bufferedreader.readLine()) != null)
            {
                try
                {
                    String[] astring = s.split(":");

                    if (astring[0].equals("ofRenderDistanceChunks") && astring.length >= 2)
                    {
                        renderDistanceChunks = Integer.valueOf(astring[1]).intValue();
                        renderDistanceChunks = Config.limit(renderDistanceChunks, 2, 32);
                    }

                    if (astring[0].equals("ofFogType") && astring.length >= 2)
                    {
                        ofFogType = Integer.valueOf(astring[1]).intValue();
                        ofFogType = Config.limit(ofFogType, 1, 3);
                    }

                    if (astring[0].equals("ofFogStart") && astring.length >= 2)
                    {
                        ofFogStart = Float.valueOf(astring[1]).floatValue();

                        if (ofFogStart < 0.2F)
                        {
                            ofFogStart = 0.2F;
                        }

                        if (ofFogStart > 0.81F)
                        {
                            ofFogStart = 0.8F;
                        }
                    }

                    if (astring[0].equals("ofMipmapType") && astring.length >= 2)
                    {
                        ofMipmapType = Integer.valueOf(astring[1]).intValue();
                        ofMipmapType = Config.limit(ofMipmapType, 0, 3);
                    }

                    if (astring[0].equals("ofOcclusionFancy") && astring.length >= 2)
                    {
                        ofOcclusionFancy = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofSmoothFps") && astring.length >= 2)
                    {
                        ofSmoothFps = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofSmoothWorld") && astring.length >= 2)
                    {
                        ofSmoothWorld = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAoLevel") && astring.length >= 2)
                    {
                        ofAoLevel = Float.valueOf(astring[1]).floatValue();
                        ofAoLevel = Config.limit(ofAoLevel, 0.0F, 1.0F);
                    }

                    if (astring[0].equals("ofClouds") && astring.length >= 2)
                    {
                        ofClouds = Integer.valueOf(astring[1]).intValue();
                        ofClouds = Config.limit(ofClouds, 0, 3);
                        updateRenderClouds();
                    }

                    if (astring[0].equals("ofCloudsHeight") && astring.length >= 2)
                    {
                        ofCloudsHeight = Float.valueOf(astring[1]).floatValue();
                        ofCloudsHeight = Config.limit(ofCloudsHeight, 0.0F, 1.0F);
                    }

                    if (astring[0].equals("ofTrees") && astring.length >= 2)
                    {
                        ofTrees = Integer.valueOf(astring[1]).intValue();
                        ofTrees = GameSettings.limit(ofTrees, GameSettings.OF_TREES_VALUES);
                    }

                    if (astring[0].equals("ofDroppedItems") && astring.length >= 2)
                    {
                        ofDroppedItems = Integer.valueOf(astring[1]).intValue();
                        ofDroppedItems = Config.limit(ofDroppedItems, 0, 2);
                    }

                    if (astring[0].equals("ofRain") && astring.length >= 2)
                    {
                        ofRain = Integer.valueOf(astring[1]).intValue();
                        ofRain = Config.limit(ofRain, 0, 3);
                    }

                    if (astring[0].equals("ofAnimatedWater") && astring.length >= 2)
                    {
                        ofAnimatedWater = Integer.valueOf(astring[1]).intValue();
                        ofAnimatedWater = Config.limit(ofAnimatedWater, 0, 2);
                    }

                    if (astring[0].equals("ofAnimatedLava") && astring.length >= 2)
                    {
                        ofAnimatedLava = Integer.valueOf(astring[1]).intValue();
                        ofAnimatedLava = Config.limit(ofAnimatedLava, 0, 2);
                    }

                    if (astring[0].equals("ofAnimatedFire") && astring.length >= 2)
                    {
                        ofAnimatedFire = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAnimatedPortal") && astring.length >= 2)
                    {
                        ofAnimatedPortal = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAnimatedRedstone") && astring.length >= 2)
                    {
                        ofAnimatedRedstone = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAnimatedExplosion") && astring.length >= 2)
                    {
                        ofAnimatedExplosion = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAnimatedFlame") && astring.length >= 2)
                    {
                        ofAnimatedFlame = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAnimatedSmoke") && astring.length >= 2)
                    {
                        ofAnimatedSmoke = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofVoidParticles") && astring.length >= 2)
                    {
                        ofVoidParticles = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofWaterParticles") && astring.length >= 2)
                    {
                        ofWaterParticles = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofPortalParticles") && astring.length >= 2)
                    {
                        ofPortalParticles = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofPotionParticles") && astring.length >= 2)
                    {
                        ofPotionParticles = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofFireworkParticles") && astring.length >= 2)
                    {
                        ofFireworkParticles = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofDrippingWaterLava") && astring.length >= 2)
                    {
                        ofDrippingWaterLava = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAnimatedTerrain") && astring.length >= 2)
                    {
                        ofAnimatedTerrain = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAnimatedTextures") && astring.length >= 2)
                    {
                        ofAnimatedTextures = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofRainSplash") && astring.length >= 2)
                    {
                        ofRainSplash = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofLagometer") && astring.length >= 2)
                    {
                        ofLagometer = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofShowFps") && astring.length >= 2)
                    {
                        ofShowFps = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAutoSaveTicks") && astring.length >= 2)
                    {
                        ofAutoSaveTicks = Integer.valueOf(astring[1]).intValue();
                        ofAutoSaveTicks = Config.limit(ofAutoSaveTicks, 40, 40000);
                    }

                    if (astring[0].equals("ofBetterGrass") && astring.length >= 2)
                    {
                        ofBetterGrass = Integer.valueOf(astring[1]).intValue();
                        ofBetterGrass = Config.limit(ofBetterGrass, 1, 3);
                    }

                    if (astring[0].equals("ofConnectedTextures") && astring.length >= 2)
                    {
                        ofConnectedTextures = Integer.valueOf(astring[1]).intValue();
                        ofConnectedTextures = Config.limit(ofConnectedTextures, 1, 3);
                    }

                    if (astring[0].equals("ofWeather") && astring.length >= 2)
                    {
                        ofWeather = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofSky") && astring.length >= 2)
                    {
                        ofSky = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofStars") && astring.length >= 2)
                    {
                        ofStars = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofSunMoon") && astring.length >= 2)
                    {
                        ofSunMoon = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofVignette") && astring.length >= 2)
                    {
                        ofVignette = Integer.valueOf(astring[1]).intValue();
                        ofVignette = Config.limit(ofVignette, 0, 2);
                    }

                    if (astring[0].equals("ofChunkUpdates") && astring.length >= 2)
                    {
                        ofChunkUpdates = Integer.valueOf(astring[1]).intValue();
                        ofChunkUpdates = Config.limit(ofChunkUpdates, 1, 5);
                    }

                    if (astring[0].equals("ofChunkUpdatesDynamic") && astring.length >= 2)
                    {
                        ofChunkUpdatesDynamic = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofTime") && astring.length >= 2)
                    {
                        ofTime = Integer.valueOf(astring[1]).intValue();
                        ofTime = Config.limit(ofTime, 0, 2);
                    }

                    if (astring[0].equals("ofClearWater") && astring.length >= 2)
                    {
                        ofClearWater = Boolean.valueOf(astring[1]).booleanValue();
                        updateWaterOpacity();
                    }

                    if (astring[0].equals("ofAaLevel") && astring.length >= 2)
                    {
                        ofAaLevel = Integer.valueOf(astring[1]).intValue();
                        ofAaLevel = Config.limit(ofAaLevel, 0, 16);
                    }

                    if (astring[0].equals("ofAfLevel") && astring.length >= 2)
                    {
                        ofAfLevel = Integer.valueOf(astring[1]).intValue();
                        ofAfLevel = Config.limit(ofAfLevel, 1, 16);
                    }

                    if (astring[0].equals("ofProfiler") && astring.length >= 2)
                    {
                        ofProfiler = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofBetterSnow") && astring.length >= 2)
                    {
                        ofBetterSnow = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofSwampColors") && astring.length >= 2)
                    {
                        ofSwampColors = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofRandomMobs") && astring.length >= 2)
                    {
                        ofRandomMobs = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofSmoothBiomes") && astring.length >= 2)
                    {
                        ofSmoothBiomes = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofCustomFonts") && astring.length >= 2)
                    {
                        ofCustomFonts = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofCustomColors") && astring.length >= 2)
                    {
                        ofCustomColors = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofCustomItems") && astring.length >= 2)
                    {
                        ofCustomItems = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofCustomSky") && astring.length >= 2)
                    {
                        ofCustomSky = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofShowCapes") && astring.length >= 2)
                    {
                        ofShowCapes = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofNaturalTextures") && astring.length >= 2)
                    {
                        ofNaturalTextures = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofLazyChunkLoading") && astring.length >= 2)
                    {
                        ofLazyChunkLoading = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofDynamicFov") && astring.length >= 2)
                    {
                        ofDynamicFov = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofDynamicLights") && astring.length >= 2)
                    {
                        ofDynamicLights = Integer.valueOf(astring[1]).intValue();
                        ofDynamicLights = GameSettings.limit(ofDynamicLights, GameSettings.OF_DYNAMIC_LIGHTS);
                    }

                    if (astring[0].equals("ofFullscreenMode") && astring.length >= 2)
                    {
                        ofFullscreenMode = astring[1];
                    }

                    if (astring[0].equals("ofFastMath") && astring.length >= 2)
                    {
                        ofFastMath = Boolean.valueOf(astring[1]).booleanValue();
                        MathHelper.fastMath = ofFastMath;
                    }

                    if (astring[0].equals("ofFastRender") && astring.length >= 2)
                    {
                        ofFastRender = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofTranslucentBlocks") && astring.length >= 2)
                    {
                        ofTranslucentBlocks = Integer.valueOf(astring[1]).intValue();
                        ofTranslucentBlocks = Config.limit(ofTranslucentBlocks, 0, 2);
                    }

                    if (astring[0].equals("key_" + ofKeyBindZoom.getKeyDescription()))
                    {
                        ofKeyBindZoom.setKeyCode(Integer.parseInt(astring[1]));
                    }
                }
                catch (Exception exception)
                {
                    Config.dbg("Skipping bad option: " + s);
                    exception.printStackTrace();
                }
            }

            KeyBinding.resetKeyBindingArrayAndHash();
            bufferedreader.close();
        }
        catch (Exception exception1)
        {
            Config.warn("Failed to load options");
            exception1.printStackTrace();
        }
    }

    public void saveOfOptions()
    {
        try
        {
            PrintWriter printwriter = new PrintWriter(new FileWriter(optionsFileOF));
            printwriter.println("ofRenderDistanceChunks:" + renderDistanceChunks);
            printwriter.println("ofFogType:" + ofFogType);
            printwriter.println("ofFogStart:" + ofFogStart);
            printwriter.println("ofMipmapType:" + ofMipmapType);
            printwriter.println("ofOcclusionFancy:" + ofOcclusionFancy);
            printwriter.println("ofSmoothFps:" + ofSmoothFps);
            printwriter.println("ofSmoothWorld:" + ofSmoothWorld);
            printwriter.println("ofAoLevel:" + ofAoLevel);
            printwriter.println("ofClouds:" + ofClouds);
            printwriter.println("ofCloudsHeight:" + ofCloudsHeight);
            printwriter.println("ofTrees:" + ofTrees);
            printwriter.println("ofDroppedItems:" + ofDroppedItems);
            printwriter.println("ofRain:" + ofRain);
            printwriter.println("ofAnimatedWater:" + ofAnimatedWater);
            printwriter.println("ofAnimatedLava:" + ofAnimatedLava);
            printwriter.println("ofAnimatedFire:" + ofAnimatedFire);
            printwriter.println("ofAnimatedPortal:" + ofAnimatedPortal);
            printwriter.println("ofAnimatedRedstone:" + ofAnimatedRedstone);
            printwriter.println("ofAnimatedExplosion:" + ofAnimatedExplosion);
            printwriter.println("ofAnimatedFlame:" + ofAnimatedFlame);
            printwriter.println("ofAnimatedSmoke:" + ofAnimatedSmoke);
            printwriter.println("ofVoidParticles:" + ofVoidParticles);
            printwriter.println("ofWaterParticles:" + ofWaterParticles);
            printwriter.println("ofPortalParticles:" + ofPortalParticles);
            printwriter.println("ofPotionParticles:" + ofPotionParticles);
            printwriter.println("ofFireworkParticles:" + ofFireworkParticles);
            printwriter.println("ofDrippingWaterLava:" + ofDrippingWaterLava);
            printwriter.println("ofAnimatedTerrain:" + ofAnimatedTerrain);
            printwriter.println("ofAnimatedTextures:" + ofAnimatedTextures);
            printwriter.println("ofRainSplash:" + ofRainSplash);
            printwriter.println("ofLagometer:" + ofLagometer);
            printwriter.println("ofShowFps:" + ofShowFps);
            printwriter.println("ofAutoSaveTicks:" + ofAutoSaveTicks);
            printwriter.println("ofBetterGrass:" + ofBetterGrass);
            printwriter.println("ofConnectedTextures:" + ofConnectedTextures);
            printwriter.println("ofWeather:" + ofWeather);
            printwriter.println("ofSky:" + ofSky);
            printwriter.println("ofStars:" + ofStars);
            printwriter.println("ofSunMoon:" + ofSunMoon);
            printwriter.println("ofVignette:" + ofVignette);
            printwriter.println("ofChunkUpdates:" + ofChunkUpdates);
            printwriter.println("ofChunkUpdatesDynamic:" + ofChunkUpdatesDynamic);
            printwriter.println("ofTime:" + ofTime);
            printwriter.println("ofClearWater:" + ofClearWater);
            printwriter.println("ofAaLevel:" + ofAaLevel);
            printwriter.println("ofAfLevel:" + ofAfLevel);
            printwriter.println("ofProfiler:" + ofProfiler);
            printwriter.println("ofBetterSnow:" + ofBetterSnow);
            printwriter.println("ofSwampColors:" + ofSwampColors);
            printwriter.println("ofRandomMobs:" + ofRandomMobs);
            printwriter.println("ofSmoothBiomes:" + ofSmoothBiomes);
            printwriter.println("ofCustomFonts:" + ofCustomFonts);
            printwriter.println("ofCustomColors:" + ofCustomColors);
            printwriter.println("ofCustomItems:" + ofCustomItems);
            printwriter.println("ofCustomSky:" + ofCustomSky);
            printwriter.println("ofShowCapes:" + ofShowCapes);
            printwriter.println("ofNaturalTextures:" + ofNaturalTextures);
            printwriter.println("ofLazyChunkLoading:" + ofLazyChunkLoading);
            printwriter.println("ofDynamicFov:" + ofDynamicFov);
            printwriter.println("ofDynamicLights:" + ofDynamicLights);
            printwriter.println("ofFullscreenMode:" + ofFullscreenMode);
            printwriter.println("ofFastMath:" + ofFastMath);
            printwriter.println("ofFastRender:" + ofFastRender);
            printwriter.println("ofTranslucentBlocks:" + ofTranslucentBlocks);
            printwriter.println("key_" + ofKeyBindZoom.getKeyDescription() + ":" + ofKeyBindZoom.getKeyCode());
            printwriter.close();
        }
        catch (Exception exception)
        {
            Config.warn("Failed to save options");
            exception.printStackTrace();
        }
    }

    private void updateRenderClouds()
    {
        switch (ofClouds)
        {
            case 1:
                clouds = 1;
                break;

            case 2:
                clouds = 2;
                break;

            case 3:
                clouds = 0;
                break;

            default:
                if (fancyGraphics)
                {
                    clouds = 2;
                }
                else
                {
                    clouds = 1;
                }
        }
    }

    public void resetSettings()
    {
        renderDistanceChunks = 8;
        viewBobbing = true;
        anaglyph = false;
        limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
        enableVsync = false;
        updateVSync();
        mipmapLevels = 4;
        fancyGraphics = true;
        ambientOcclusion = 2;
        clouds = 2;
        fovSetting = 70.0F;
        gammaSetting = 0.0F;
        guiScale = 0;
        particleSetting = 0;
        heldItemTooltips = true;
        useVbo = false;
        allowBlockAlternatives = true;
        forceUnicodeFont = false;
        ofFogType = 1;
        ofFogStart = 0.8F;
        ofMipmapType = 0;
        ofOcclusionFancy = false;
        ofSmoothFps = false;
        Config.updateAvailableProcessors();
        ofSmoothWorld = Config.isSingleProcessor();
        ofLazyChunkLoading = Config.isSingleProcessor();
        ofFastMath = false;
        ofFastRender = false;
        ofTranslucentBlocks = 0;
        ofDynamicFov = true;
        ofDynamicLights = 3;
        ofAoLevel = 1.0F;
        ofAaLevel = 0;
        ofAfLevel = 1;
        ofClouds = 0;
        ofCloudsHeight = 0.0F;
        ofTrees = 0;
        ofRain = 0;
        ofBetterGrass = 3;
        ofAutoSaveTicks = 4000;
        ofLagometer = false;
        ofShowFps = false;
        ofProfiler = false;
        ofWeather = true;
        ofSky = true;
        ofStars = true;
        ofSunMoon = true;
        ofVignette = 0;
        ofChunkUpdates = 1;
        ofChunkUpdatesDynamic = false;
        ofTime = 0;
        ofClearWater = false;
        ofBetterSnow = false;
        ofFullscreenMode = "Default";
        ofSwampColors = true;
        ofRandomMobs = true;
        ofSmoothBiomes = true;
        ofCustomFonts = true;
        ofCustomColors = true;
        ofCustomItems = true;
        ofCustomSky = true;
        ofShowCapes = true;
        ofConnectedTextures = 2;
        ofNaturalTextures = false;
        ofAnimatedWater = 0;
        ofAnimatedLava = 0;
        ofAnimatedFire = true;
        ofAnimatedPortal = true;
        ofAnimatedRedstone = true;
        ofAnimatedExplosion = true;
        ofAnimatedFlame = true;
        ofAnimatedSmoke = true;
        ofVoidParticles = true;
        ofWaterParticles = true;
        ofRainSplash = true;
        ofPortalParticles = true;
        ofPotionParticles = true;
        ofFireworkParticles = true;
        ofDrippingWaterLava = true;
        ofAnimatedTerrain = true;
        ofAnimatedTextures = true;
        Shaders.setShaderPack(Shaders.packNameNone);
        Shaders.configAntialiasingLevel = 0;
        Shaders.uninit();
        Shaders.storeConfig();
        updateWaterOpacity();
        mc.refreshResources();
        saveOptions();
    }

    public void updateVSync()
    {
        Display.setVSyncEnabled(enableVsync);
    }

    private void updateWaterOpacity()
    {
        if (mc.isIntegratedServerRunning() && mc.getIntegratedServer() != null)
        {
            Config.waterOpacityChanged = true;
        }

        ClearWater.updateWaterOpacity(this, mc.theWorld);
    }

    public void setAllAnimations(boolean p_setAllAnimations_1_)
    {
        int i = p_setAllAnimations_1_ ? 0 : 2;
        ofAnimatedWater = i;
        ofAnimatedLava = i;
        ofAnimatedFire = p_setAllAnimations_1_;
        ofAnimatedPortal = p_setAllAnimations_1_;
        ofAnimatedRedstone = p_setAllAnimations_1_;
        ofAnimatedExplosion = p_setAllAnimations_1_;
        ofAnimatedFlame = p_setAllAnimations_1_;
        ofAnimatedSmoke = p_setAllAnimations_1_;
        ofVoidParticles = p_setAllAnimations_1_;
        ofWaterParticles = p_setAllAnimations_1_;
        ofRainSplash = p_setAllAnimations_1_;
        ofPortalParticles = p_setAllAnimations_1_;
        ofPotionParticles = p_setAllAnimations_1_;
        ofFireworkParticles = p_setAllAnimations_1_;
        particleSetting = p_setAllAnimations_1_ ? 0 : 2;
        ofDrippingWaterLava = p_setAllAnimations_1_;
        ofAnimatedTerrain = p_setAllAnimations_1_;
        ofAnimatedTextures = p_setAllAnimations_1_;
    }

    private static int nextValue(int p_nextValue_0_, int[] p_nextValue_1_)
    {
        int i = GameSettings.indexOf(p_nextValue_0_, p_nextValue_1_);

        if (i < 0)
        {
            return p_nextValue_1_[0];
        }
        else
        {
            ++i;

            if (i >= p_nextValue_1_.length)
            {
                i = 0;
            }

            return p_nextValue_1_[i];
        }
    }

    private static int limit(int p_limit_0_, int[] p_limit_1_)
    {
        int i = GameSettings.indexOf(p_limit_0_, p_limit_1_);
        return i < 0 ? p_limit_1_[0] : p_limit_0_;
    }

    private static int indexOf(int p_indexOf_0_, int[] p_indexOf_1_)
    {
        for (int i = 0; i < p_indexOf_1_.length; ++i)
        {
            if (p_indexOf_1_[i] == p_indexOf_0_)
            {
                return i;
            }
        }

        return -1;
    }

    static final class GameSettings$2
    {
        static final int[] field_151477_a = new int[GameSettings.Options.values().length];
        private static final String __OBFID = "CL_00000652";

        static
        {
            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.INVERT_MOUSE.ordinal()] = 1;
            }
            catch (NoSuchFieldError var17)
            {
            }

            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.VIEW_BOBBING.ordinal()] = 2;
            }
            catch (NoSuchFieldError var16)
            {
            }

            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.ANAGLYPH.ordinal()] = 3;
            }
            catch (NoSuchFieldError var15)
            {
            }

            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.FBO_ENABLE.ordinal()] = 4;
            }
            catch (NoSuchFieldError var14)
            {
            }

            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.CHAT_COLOR.ordinal()] = 5;
            }
            catch (NoSuchFieldError var13)
            {
            }

            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.CHAT_LINKS.ordinal()] = 6;
            }
            catch (NoSuchFieldError var12)
            {
            }

            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.CHAT_LINKS_PROMPT.ordinal()] = 7;
            }
            catch (NoSuchFieldError var11)
            {
            }

            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.SNOOPER_ENABLED.ordinal()] = 8;
            }
            catch (NoSuchFieldError var10)
            {
            }

            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.USE_FULLSCREEN.ordinal()] = 9;
            }
            catch (NoSuchFieldError var9)
            {
            }

            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.ENABLE_VSYNC.ordinal()] = 10;
            }
            catch (NoSuchFieldError var8)
            {
            }

            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.USE_VBO.ordinal()] = 11;
            }
            catch (NoSuchFieldError var7)
            {
            }

            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.TOUCHSCREEN.ordinal()] = 12;
            }
            catch (NoSuchFieldError var6)
            {
            }

            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.STREAM_SEND_METADATA.ordinal()] = 13;
            }
            catch (NoSuchFieldError var5)
            {
            }

            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.FORCE_UNICODE_FONT.ordinal()] = 14;
            }
            catch (NoSuchFieldError var4)
            {
            }

            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.BLOCK_ALTERNATIVES.ordinal()] = 15;
            }
            catch (NoSuchFieldError var3)
            {
            }

            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.REDUCED_DEBUG_INFO.ordinal()] = 16;
            }
            catch (NoSuchFieldError var2)
            {
            }

            try
            {
                GameSettings$2.field_151477_a[GameSettings.Options.ENTITY_SHADOWS.ordinal()] = 17;
            }
            catch (NoSuchFieldError var1)
            {
            }
        }
    }

    public static enum Options
    {
        INVERT_MOUSE("INVERT_MOUSE", 0, "options.invertMouse", false, true),
        SENSITIVITY("SENSITIVITY", 1, "options.sensitivity", true, false),
        FOV("FOV", 2, "options.fov", true, false, 30.0F, 110.0F, 1.0F),
        GAMMA("GAMMA", 3, "options.gamma", true, false),
        SATURATION("SATURATION", 4, "options.saturation", true, false),
        RENDER_DISTANCE("RENDER_DISTANCE", 5, "options.renderDistance", true, false, 2.0F, 16.0F, 1.0F),
        VIEW_BOBBING("VIEW_BOBBING", 6, "options.viewBobbing", false, true),
        ANAGLYPH("ANAGLYPH", 7, "options.anaglyph", false, true),
        FRAMERATE_LIMIT("FRAMERATE_LIMIT", 8, "options.framerateLimit", true, false, 0.0F, 260.0F, 5.0F),
        FBO_ENABLE("FBO_ENABLE", 9, "options.fboEnable", false, true),
        RENDER_CLOUDS("RENDER_CLOUDS", 10, "options.renderClouds", false, false),
        GRAPHICS("GRAPHICS", 11, "options.graphics", false, false),
        AMBIENT_OCCLUSION("AMBIENT_OCCLUSION", 12, "options.ao", false, false),
        GUI_SCALE("GUI_SCALE", 13, "options.guiScale", false, false),
        PARTICLES("PARTICLES", 14, "options.particles", false, false),
        CHAT_VISIBILITY("CHAT_VISIBILITY", 15, "options.chat.visibility", false, false),
        CHAT_COLOR("CHAT_COLOR", 16, "options.chat.color", false, true),
        CHAT_LINKS("CHAT_LINKS", 17, "options.chat.links", false, true),
        CHAT_OPACITY("CHAT_OPACITY", 18, "options.chat.opacity", true, false),
        CHAT_LINKS_PROMPT("CHAT_LINKS_PROMPT", 19, "options.chat.links.prompt", false, true),
        SNOOPER_ENABLED("SNOOPER_ENABLED", 20, "options.snooper", false, true),
        USE_FULLSCREEN("USE_FULLSCREEN", 21, "options.fullscreen", false, true),
        ENABLE_VSYNC("ENABLE_VSYNC", 22, "options.vsync", false, true),
        USE_VBO("USE_VBO", 23, "options.vbo", false, true),
        TOUCHSCREEN("TOUCHSCREEN", 24, "options.touchscreen", false, true),
        CHAT_SCALE("CHAT_SCALE", 25, "options.chat.scale", true, false),
        CHAT_WIDTH("CHAT_WIDTH", 26, "options.chat.width", true, false),
        CHAT_HEIGHT_FOCUSED("CHAT_HEIGHT_FOCUSED", 27, "options.chat.height.focused", true, false),
        CHAT_HEIGHT_UNFOCUSED("CHAT_HEIGHT_UNFOCUSED", 28, "options.chat.height.unfocused", true, false),
        MIPMAP_LEVELS("MIPMAP_LEVELS", 29, "options.mipmapLevels", true, false, 0.0F, 4.0F, 1.0F),
        FORCE_UNICODE_FONT("FORCE_UNICODE_FONT", 30, "options.forceUnicodeFont", false, true),
        STREAM_BYTES_PER_PIXEL("STREAM_BYTES_PER_PIXEL", 31, "options.stream.bytesPerPixel", true, false),
        STREAM_VOLUME_MIC("STREAM_VOLUME_MIC", 32, "options.stream.micVolumne", true, false),
        STREAM_VOLUME_SYSTEM("STREAM_VOLUME_SYSTEM", 33, "options.stream.systemVolume", true, false),
        STREAM_KBPS("STREAM_KBPS", 34, "options.stream.kbps", true, false),
        STREAM_FPS("STREAM_FPS", 35, "options.stream.fps", true, false),
        STREAM_COMPRESSION("STREAM_COMPRESSION", 36, "options.stream.compression", false, false),
        STREAM_SEND_METADATA("STREAM_SEND_METADATA", 37, "options.stream.sendMetadata", false, true),
        STREAM_CHAT_ENABLED("STREAM_CHAT_ENABLED", 38, "options.stream.chat.enabled", false, false),
        STREAM_CHAT_USER_FILTER("STREAM_CHAT_USER_FILTER", 39, "options.stream.chat.userFilter", false, false),
        STREAM_MIC_TOGGLE_BEHAVIOR("STREAM_MIC_TOGGLE_BEHAVIOR", 40, "options.stream.micToggleBehavior", false, false),
        BLOCK_ALTERNATIVES("BLOCK_ALTERNATIVES", 41, "options.blockAlternatives", false, true),
        REDUCED_DEBUG_INFO("REDUCED_DEBUG_INFO", 42, "options.reducedDebugInfo", false, true),
        ENTITY_SHADOWS("ENTITY_SHADOWS", 43, "options.entityShadows", false, true),
        FOG_FANCY("", 999, "of.options.FOG_FANCY", false, false),
        FOG_START("", 999, "of.options.FOG_START", false, false),
        MIPMAP_TYPE("", 999, "of.options.MIPMAP_TYPE", true, false, 0.0F, 3.0F, 1.0F),
        SMOOTH_FPS("", 999, "of.options.SMOOTH_FPS", false, false),
        CLOUDS("", 999, "of.options.CLOUDS", false, false),
        CLOUD_HEIGHT("", 999, "of.options.CLOUD_HEIGHT", true, false),
        TREES("", 999, "of.options.TREES", false, false),
        RAIN("", 999, "of.options.RAIN", false, false),
        ANIMATED_WATER("", 999, "of.options.ANIMATED_WATER", false, false),
        ANIMATED_LAVA("", 999, "of.options.ANIMATED_LAVA", false, false),
        ANIMATED_FIRE("", 999, "of.options.ANIMATED_FIRE", false, false),
        ANIMATED_PORTAL("", 999, "of.options.ANIMATED_PORTAL", false, false),
        AO_LEVEL("", 999, "of.options.AO_LEVEL", true, false),
        LAGOMETER("", 999, "of.options.LAGOMETER", false, false),
        SHOW_FPS("", 999, "of.options.SHOW_FPS", false, false),
        AUTOSAVE_TICKS("", 999, "of.options.AUTOSAVE_TICKS", false, false),
        BETTER_GRASS("", 999, "of.options.BETTER_GRASS", false, false),
        ANIMATED_REDSTONE("", 999, "of.options.ANIMATED_REDSTONE", false, false),
        ANIMATED_EXPLOSION("", 999, "of.options.ANIMATED_EXPLOSION", false, false),
        ANIMATED_FLAME("", 999, "of.options.ANIMATED_FLAME", false, false),
        ANIMATED_SMOKE("", 999, "of.options.ANIMATED_SMOKE", false, false),
        WEATHER("", 999, "of.options.WEATHER", false, false),
        SKY("", 999, "of.options.SKY", false, false),
        STARS("", 999, "of.options.STARS", false, false),
        SUN_MOON("", 999, "of.options.SUN_MOON", false, false),
        VIGNETTE("", 999, "of.options.VIGNETTE", false, false),
        CHUNK_UPDATES("", 999, "of.options.CHUNK_UPDATES", false, false),
        CHUNK_UPDATES_DYNAMIC("", 999, "of.options.CHUNK_UPDATES_DYNAMIC", false, false),
        TIME("", 999, "of.options.TIME", false, false),
        CLEAR_WATER("", 999, "of.options.CLEAR_WATER", false, false),
        SMOOTH_WORLD("", 999, "of.options.SMOOTH_WORLD", false, false),
        VOID_PARTICLES("", 999, "of.options.VOID_PARTICLES", false, false),
        WATER_PARTICLES("", 999, "of.options.WATER_PARTICLES", false, false),
        RAIN_SPLASH("", 999, "of.options.RAIN_SPLASH", false, false),
        PORTAL_PARTICLES("", 999, "of.options.PORTAL_PARTICLES", false, false),
        POTION_PARTICLES("", 999, "of.options.POTION_PARTICLES", false, false),
        FIREWORK_PARTICLES("", 999, "of.options.FIREWORK_PARTICLES", false, false),
        PROFILER("", 999, "of.options.PROFILER", false, false),
        DRIPPING_WATER_LAVA("", 999, "of.options.DRIPPING_WATER_LAVA", false, false),
        BETTER_SNOW("", 999, "of.options.BETTER_SNOW", false, false),
        FULLSCREEN_MODE("", 999, "of.options.FULLSCREEN_MODE", false, false),
        ANIMATED_TERRAIN("", 999, "of.options.ANIMATED_TERRAIN", false, false),
        SWAMP_COLORS("", 999, "of.options.SWAMP_COLORS", false, false),
        RANDOM_MOBS("", 999, "of.options.RANDOM_MOBS", false, false),
        SMOOTH_BIOMES("", 999, "of.options.SMOOTH_BIOMES", false, false),
        CUSTOM_FONTS("", 999, "of.options.CUSTOM_FONTS", false, false),
        CUSTOM_COLORS("", 999, "of.options.CUSTOM_COLORS", false, false),
        SHOW_CAPES("", 999, "of.options.SHOW_CAPES", false, false),
        CONNECTED_TEXTURES("", 999, "of.options.CONNECTED_TEXTURES", false, false),
        CUSTOM_ITEMS("", 999, "of.options.CUSTOM_ITEMS", false, false),
        AA_LEVEL("", 999, "of.options.AA_LEVEL", true, false, 0.0F, 16.0F, 1.0F),
        AF_LEVEL("", 999, "of.options.AF_LEVEL", true, false, 1.0F, 16.0F, 1.0F),
        ANIMATED_TEXTURES("", 999, "of.options.ANIMATED_TEXTURES", false, false),
        NATURAL_TEXTURES("", 999, "of.options.NATURAL_TEXTURES", false, false),
        HELD_ITEM_TOOLTIPS("", 999, "of.options.HELD_ITEM_TOOLTIPS", false, false),
        DROPPED_ITEMS("", 999, "of.options.DROPPED_ITEMS", false, false),
        LAZY_CHUNK_LOADING("", 999, "of.options.LAZY_CHUNK_LOADING", false, false),
        CUSTOM_SKY("", 999, "of.options.CUSTOM_SKY", false, false),
        FAST_MATH("", 999, "of.options.FAST_MATH", false, false),
        FAST_RENDER("", 999, "of.options.FAST_RENDER", false, false),
        TRANSLUCENT_BLOCKS("", 999, "of.options.TRANSLUCENT_BLOCKS", false, false),
        DYNAMIC_FOV("", 999, "of.options.DYNAMIC_FOV", false, false),
        DYNAMIC_LIGHTS("", 999, "of.options.DYNAMIC_LIGHTS", false, false);

        private final boolean enumFloat;
        private final boolean enumBoolean;
        private final String enumString;
        private final float valueStep;
        private final float valueMin;
        private float valueMax;
        private static final GameSettings.Options[] $VALUES = new GameSettings.Options[]{Options.INVERT_MOUSE, Options.SENSITIVITY, Options.FOV, Options.GAMMA, Options.SATURATION, Options.RENDER_DISTANCE, Options.VIEW_BOBBING, Options.ANAGLYPH, Options.FRAMERATE_LIMIT, Options.FBO_ENABLE, Options.RENDER_CLOUDS, Options.GRAPHICS, Options.AMBIENT_OCCLUSION, Options.GUI_SCALE, Options.PARTICLES, Options.CHAT_VISIBILITY, Options.CHAT_COLOR, Options.CHAT_LINKS, Options.CHAT_OPACITY, Options.CHAT_LINKS_PROMPT, Options.SNOOPER_ENABLED, Options.USE_FULLSCREEN, Options.ENABLE_VSYNC, Options.USE_VBO, Options.TOUCHSCREEN, Options.CHAT_SCALE, Options.CHAT_WIDTH, Options.CHAT_HEIGHT_FOCUSED, Options.CHAT_HEIGHT_UNFOCUSED, Options.MIPMAP_LEVELS, Options.FORCE_UNICODE_FONT, Options.STREAM_BYTES_PER_PIXEL, Options.STREAM_VOLUME_MIC, Options.STREAM_VOLUME_SYSTEM, Options.STREAM_KBPS, Options.STREAM_FPS, Options.STREAM_COMPRESSION, Options.STREAM_SEND_METADATA, Options.STREAM_CHAT_ENABLED, Options.STREAM_CHAT_USER_FILTER, Options.STREAM_MIC_TOGGLE_BEHAVIOR, Options.BLOCK_ALTERNATIVES, Options.REDUCED_DEBUG_INFO, Options.ENTITY_SHADOWS};
        private static final String __OBFID = "CL_00000653";

        public static GameSettings.Options getEnumOptions(int p_74379_0_)
        {
            for (GameSettings.Options gamesettings$options : Options.values())
            {
                if (gamesettings$options.returnEnumOrdinal() == p_74379_0_)
                {
                    return gamesettings$options;
                }
            }

            return null;
        }

        private Options(String p_i0_3_, int p_i0_4_, String p_i0_5_, boolean p_i0_6_, boolean p_i0_7_)
        {
            this(p_i0_3_, p_i0_4_, p_i0_5_, p_i0_6_, p_i0_7_, 0.0F, 1.0F, 0.0F);
        }

        private Options(String p_i1_3_, int p_i1_4_, String p_i1_5_, boolean p_i1_6_, boolean p_i1_7_, float p_i1_8_, float p_i1_9_, float p_i1_10_)
        {
            enumString = p_i1_5_;
            enumFloat = p_i1_6_;
            enumBoolean = p_i1_7_;
            valueMin = p_i1_8_;
            valueMax = p_i1_9_;
            valueStep = p_i1_10_;
        }

        public boolean getEnumFloat()
        {
            return enumFloat;
        }

        public boolean getEnumBoolean()
        {
            return enumBoolean;
        }

        public int returnEnumOrdinal()
        {
            return ordinal();
        }

        public String getEnumString()
        {
            return enumString;
        }

        public float getValueMax()
        {
            return valueMax;
        }

        public void setValueMax(float p_148263_1_)
        {
            valueMax = p_148263_1_;
        }

        public float normalizeValue(float p_148266_1_)
        {
            return MathHelper.clamp_float((snapToStepClamp(p_148266_1_) - valueMin) / (valueMax - valueMin), 0.0F, 1.0F);
        }

        public float denormalizeValue(float p_148262_1_)
        {
            return snapToStepClamp(valueMin + (valueMax - valueMin) * MathHelper.clamp_float(p_148262_1_, 0.0F, 1.0F));
        }

        public float snapToStepClamp(float p_148268_1_)
        {
            p_148268_1_ = snapToStep(p_148268_1_);
            return MathHelper.clamp_float(p_148268_1_, valueMin, valueMax);
        }

        protected float snapToStep(float p_148264_1_)
        {
            if (valueStep > 0.0F)
            {
                p_148264_1_ = valueStep * (float)Math.round(p_148264_1_ / valueStep);
            }

            return p_148264_1_;
        }
    }
}
