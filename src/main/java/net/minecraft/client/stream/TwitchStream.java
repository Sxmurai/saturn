package net.minecraft.client.stream;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.stream.GuiTwitchUserMode;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.lwjgl.opengl.GL11;
import tv.twitch.AuthToken;
import tv.twitch.ErrorCode;
import tv.twitch.broadcast.EncodingCpuUsage;
import tv.twitch.broadcast.FrameBuffer;
import tv.twitch.broadcast.GameInfo;
import tv.twitch.broadcast.IngestList;
import tv.twitch.broadcast.IngestServer;
import tv.twitch.broadcast.StreamInfo;
import tv.twitch.broadcast.VideoParams;
import tv.twitch.chat.ChatRawMessage;
import tv.twitch.chat.ChatTokenizedMessage;
import tv.twitch.chat.ChatUserInfo;
import tv.twitch.chat.ChatUserMode;
import tv.twitch.chat.ChatUserSubscription;

public class TwitchStream implements BroadcastController.BroadcastListener, ChatController.ChatListener, IngestServerTester.IngestTestListener, IStream
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Marker STREAM_MARKER = MarkerManager.getMarker("STREAM");
    private final BroadcastController broadcastController;
    private final ChatController chatController;
    private String field_176029_e;

    /** the minecraft instance */
    private final Minecraft mc;
    private final IChatComponent twitchComponent = new ChatComponentText("Twitch");
    private final Map<String, ChatUserInfo> field_152955_g = Maps.newHashMap();
    private Framebuffer framebuffer;
    private boolean field_152957_i;

    /** stream's target fps */
    private int targetFPS = 30;
    private long field_152959_k = 0L;
    private boolean field_152960_l = false;
    private boolean loggedIn;
    private boolean field_152962_n;
    private boolean field_152963_o;
    private IStream.AuthFailureReason authFailureReason = IStream.AuthFailureReason.ERROR;
    private static boolean field_152965_q;

    public TwitchStream(Minecraft mcIn, final Property streamProperty)
    {
        mc = mcIn;
        broadcastController = new BroadcastController();
        chatController = new ChatController();
        broadcastController.func_152841_a(this);
        chatController.func_152990_a(this);
        broadcastController.func_152842_a("nmt37qblda36pvonovdkbopzfzw3wlq");
        chatController.func_152984_a("nmt37qblda36pvonovdkbopzfzw3wlq");
        twitchComponent.getChatStyle().setColor(EnumChatFormatting.DARK_PURPLE);

        if (streamProperty != null && !Strings.isNullOrEmpty(streamProperty.getValue()) && OpenGlHelper.framebufferSupported)
        {
            Thread thread = new Thread("Twitch authenticator")
            {
                public void run()
                {
                    try
                    {
                        URL url = new URL("https://api.twitch.tv/kraken?oauth_token=" + URLEncoder.encode(streamProperty.getValue(), "UTF-8"));
                        String s = HttpUtil.get(url);
                        JsonObject jsonobject = JsonUtils.getJsonObject((new JsonParser()).parse(s), "Response");
                        JsonObject jsonobject1 = JsonUtils.getJsonObject(jsonobject, "token");

                        if (JsonUtils.getBoolean(jsonobject1, "valid"))
                        {
                            String s1 = JsonUtils.getString(jsonobject1, "user_name");
                            TwitchStream.LOGGER.debug(TwitchStream.STREAM_MARKER, "Authenticated with twitch; username is {}", new Object[] {s1});
                            AuthToken authtoken = new AuthToken();
                            authtoken.data = streamProperty.getValue();
                            broadcastController.func_152818_a(s1, authtoken);
                            chatController.func_152998_c(s1);
                            chatController.func_152994_a(authtoken);
                            Runtime.getRuntime().addShutdownHook(new Thread("Twitch shutdown hook")
                            {
                                public void run()
                                {
                                    shutdownStream();
                                }
                            });
                            broadcastController.func_152817_A();
                            chatController.func_175984_n();
                        }
                        else
                        {
                            authFailureReason = IStream.AuthFailureReason.INVALID_TOKEN;
                            TwitchStream.LOGGER.error(TwitchStream.STREAM_MARKER, "Given twitch access token is invalid");
                        }
                    }
                    catch (IOException ioexception)
                    {
                        authFailureReason = IStream.AuthFailureReason.ERROR;
                        TwitchStream.LOGGER.error(TwitchStream.STREAM_MARKER, "Could not authenticate with twitch", ioexception);
                    }
                }
            };
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Shuts down a steam
     */
    public void shutdownStream()
    {
        TwitchStream.LOGGER.debug(TwitchStream.STREAM_MARKER, "Shutdown streaming");
        broadcastController.statCallback();
        chatController.func_175988_p();
    }

    public void func_152935_j()
    {
        int i = mc.gameSettings.streamChatEnabled;
        boolean flag = field_176029_e != null && chatController.func_175990_d(field_176029_e);
        boolean flag1 = chatController.func_153000_j() == ChatController.ChatState.Initialized && (field_176029_e == null || chatController.func_175989_e(field_176029_e) == ChatController.EnumChannelState.Disconnected);

        if (i == 2)
        {
            if (flag)
            {
                TwitchStream.LOGGER.debug(TwitchStream.STREAM_MARKER, "Disconnecting from twitch chat per user options");
                chatController.func_175991_l(field_176029_e);
            }
        }
        else if (i == 1)
        {
            if (flag1 && broadcastController.func_152849_q())
            {
                TwitchStream.LOGGER.debug(TwitchStream.STREAM_MARKER, "Connecting to twitch chat per user options");
                func_152942_I();
            }
        }
        else if (i == 0)
        {
            if (flag && !isBroadcasting())
            {
                TwitchStream.LOGGER.debug(TwitchStream.STREAM_MARKER, "Disconnecting from twitch chat as user is no longer streaming");
                chatController.func_175991_l(field_176029_e);
            }
            else if (flag1 && isBroadcasting())
            {
                TwitchStream.LOGGER.debug(TwitchStream.STREAM_MARKER, "Connecting to twitch chat as user is streaming");
                func_152942_I();
            }
        }

        broadcastController.func_152821_H();
        chatController.func_152997_n();
    }

    protected void func_152942_I()
    {
        ChatController.ChatState chatcontroller$chatstate = chatController.func_153000_j();
        String s = broadcastController.getChannelInfo().name;
        field_176029_e = s;

        if (chatcontroller$chatstate != ChatController.ChatState.Initialized)
        {
            TwitchStream.LOGGER.warn("Invalid twitch chat state {}", new Object[] {chatcontroller$chatstate});
        }
        else if (chatController.func_175989_e(field_176029_e) == ChatController.EnumChannelState.Disconnected)
        {
            chatController.func_152986_d(s);
        }
        else
        {
            TwitchStream.LOGGER.warn("Invalid twitch chat state {}", new Object[] {chatcontroller$chatstate});
        }
    }

    public void func_152922_k()
    {
        if (broadcastController.isBroadcasting() && !broadcastController.isBroadcastPaused())
        {
            long i = System.nanoTime();
            long j = 1000000000 / targetFPS;
            long k = i - field_152959_k;
            boolean flag = k >= j;

            if (flag)
            {
                FrameBuffer framebuffer = broadcastController.func_152822_N();
                Framebuffer framebuffer1 = mc.getFramebuffer();
                this.framebuffer.bindFramebuffer(true);
                GlStateManager.matrixMode(5889);
                GlStateManager.pushMatrix();
                GlStateManager.loadIdentity();
                GlStateManager.ortho(0.0D, this.framebuffer.framebufferWidth, this.framebuffer.framebufferHeight, 0.0D, 1000.0D, 3000.0D);
                GlStateManager.matrixMode(5888);
                GlStateManager.pushMatrix();
                GlStateManager.loadIdentity();
                GlStateManager.translate(0.0F, 0.0F, -2000.0F);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.viewport(0, 0, this.framebuffer.framebufferWidth, this.framebuffer.framebufferHeight);
                GlStateManager.enableTexture2D();
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
                float f = (float)this.framebuffer.framebufferWidth;
                float f1 = (float)this.framebuffer.framebufferHeight;
                float f2 = (float)framebuffer1.framebufferWidth / (float)framebuffer1.framebufferTextureWidth;
                float f3 = (float)framebuffer1.framebufferHeight / (float)framebuffer1.framebufferTextureHeight;
                framebuffer1.bindFramebufferTexture();
                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, 9729.0F);
                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, 9729.0F);
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos(0.0D, f1, 0.0D).tex(0.0D, f3).endVertex();
                worldrenderer.pos(f, f1, 0.0D).tex(f2, f3).endVertex();
                worldrenderer.pos(f, 0.0D, 0.0D).tex(f2, 0.0D).endVertex();
                worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
                tessellator.draw();
                framebuffer1.unbindFramebufferTexture();
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(5889);
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(5888);
                broadcastController.captureFramebuffer(framebuffer);
                this.framebuffer.unbindFramebuffer();
                broadcastController.submitStreamFrame(framebuffer);
                field_152959_k = i;
            }
        }
    }

    public boolean func_152936_l()
    {
        return broadcastController.func_152849_q();
    }

    public boolean isReadyToBroadcast()
    {
        return broadcastController.isReadyToBroadcast();
    }

    public boolean isBroadcasting()
    {
        return broadcastController.isBroadcasting();
    }

    public void func_152911_a(Metadata p_152911_1_, long p_152911_2_)
    {
        if (isBroadcasting() && field_152957_i)
        {
            long i = broadcastController.func_152844_x();

            if (!broadcastController.func_152840_a(p_152911_1_.func_152810_c(), i + p_152911_2_, p_152911_1_.func_152809_a(), p_152911_1_.func_152806_b()))
            {
                TwitchStream.LOGGER.warn(TwitchStream.STREAM_MARKER, "Couldn't send stream metadata action at {}: {}", new Object[] {Long.valueOf(i + p_152911_2_), p_152911_1_});
            }
            else
            {
                TwitchStream.LOGGER.debug(TwitchStream.STREAM_MARKER, "Sent stream metadata action at {}: {}", new Object[] {Long.valueOf(i + p_152911_2_), p_152911_1_});
            }
        }
    }

    public void func_176026_a(Metadata p_176026_1_, long p_176026_2_, long p_176026_4_)
    {
        if (isBroadcasting() && field_152957_i)
        {
            long i = broadcastController.func_152844_x();
            String s = p_176026_1_.func_152809_a();
            String s1 = p_176026_1_.func_152806_b();
            long j = broadcastController.func_177946_b(p_176026_1_.func_152810_c(), i + p_176026_2_, s, s1);

            if (j < 0L)
            {
                TwitchStream.LOGGER.warn(TwitchStream.STREAM_MARKER, "Could not send stream metadata sequence from {} to {}: {}", new Object[] {Long.valueOf(i + p_176026_2_), Long.valueOf(i + p_176026_4_), p_176026_1_});
            }
            else if (broadcastController.func_177947_a(p_176026_1_.func_152810_c(), i + p_176026_4_, j, s, s1))
            {
                TwitchStream.LOGGER.debug(TwitchStream.STREAM_MARKER, "Sent stream metadata sequence from {} to {}: {}", new Object[] {Long.valueOf(i + p_176026_2_), Long.valueOf(i + p_176026_4_), p_176026_1_});
            }
            else
            {
                TwitchStream.LOGGER.warn(TwitchStream.STREAM_MARKER, "Half-sent stream metadata sequence from {} to {}: {}", new Object[] {Long.valueOf(i + p_176026_2_), Long.valueOf(i + p_176026_4_), p_176026_1_});
            }
        }
    }

    public boolean isPaused()
    {
        return broadcastController.isBroadcastPaused();
    }

    public void requestCommercial()
    {
        if (broadcastController.requestCommercial())
        {
            TwitchStream.LOGGER.debug(TwitchStream.STREAM_MARKER, "Requested commercial from Twitch");
        }
        else
        {
            TwitchStream.LOGGER.warn(TwitchStream.STREAM_MARKER, "Could not request commercial from Twitch");
        }
    }

    /**
     * pauses a stream
     */
    public void pause()
    {
        broadcastController.func_152847_F();
        field_152962_n = true;
        updateStreamVolume();
    }

    /**
     * unpauses a stream
     */
    public void unpause()
    {
        broadcastController.func_152854_G();
        field_152962_n = false;
        updateStreamVolume();
    }

    public void updateStreamVolume()
    {
        if (isBroadcasting())
        {
            float f = mc.gameSettings.streamGameVolume;
            boolean flag = field_152962_n || f <= 0.0F;
            broadcastController.setPlaybackDeviceVolume(flag ? 0.0F : f);
            broadcastController.setRecordingDeviceVolume(func_152929_G() ? 0.0F : mc.gameSettings.streamMicVolume);
        }
    }

    public void func_152930_t()
    {
        GameSettings gamesettings = mc.gameSettings;
        VideoParams videoparams = broadcastController.func_152834_a(TwitchStream.formatStreamKbps(gamesettings.streamKbps), TwitchStream.formatStreamFps(gamesettings.streamFps), TwitchStream.formatStreamBps(gamesettings.streamBytesPerPixel), (float) mc.displayWidth / (float) mc.displayHeight);

        switch (gamesettings.streamCompression)
        {
            case 0:
                videoparams.encodingCpuUsage = EncodingCpuUsage.TTV_ECU_LOW;
                break;

            case 1:
                videoparams.encodingCpuUsage = EncodingCpuUsage.TTV_ECU_MEDIUM;
                break;

            case 2:
                videoparams.encodingCpuUsage = EncodingCpuUsage.TTV_ECU_HIGH;
        }

        if (framebuffer == null)
        {
            framebuffer = new Framebuffer(videoparams.outputWidth, videoparams.outputHeight, false);
        }
        else
        {
            framebuffer.createBindFramebuffer(videoparams.outputWidth, videoparams.outputHeight);
        }

        if (gamesettings.streamPreferredServer != null && gamesettings.streamPreferredServer.length() > 0)
        {
            for (IngestServer ingestserver : func_152925_v())
            {
                if (ingestserver.serverUrl.equals(gamesettings.streamPreferredServer))
                {
                    broadcastController.func_152824_a(ingestserver);
                    break;
                }
            }
        }

        targetFPS = videoparams.targetFps;
        field_152957_i = gamesettings.streamSendMetadata;
        broadcastController.func_152836_a(videoparams);
        TwitchStream.LOGGER.info(TwitchStream.STREAM_MARKER, "Streaming at {}/{} at {} kbps to {}", new Object[] {Integer.valueOf(videoparams.outputWidth), Integer.valueOf(videoparams.outputHeight), Integer.valueOf(videoparams.maxKbps), broadcastController.func_152833_s().serverUrl});
        broadcastController.func_152828_a(null, "Minecraft", null);
    }

    public void stopBroadcasting()
    {
        if (broadcastController.stopBroadcasting())
        {
            TwitchStream.LOGGER.info(TwitchStream.STREAM_MARKER, "Stopped streaming to Twitch");
        }
        else
        {
            TwitchStream.LOGGER.warn(TwitchStream.STREAM_MARKER, "Could not stop streaming to Twitch");
        }
    }

    public void func_152900_a(ErrorCode p_152900_1_, AuthToken p_152900_2_)
    {
    }

    public void func_152897_a(ErrorCode p_152897_1_)
    {
        if (ErrorCode.succeeded(p_152897_1_))
        {
            TwitchStream.LOGGER.debug(TwitchStream.STREAM_MARKER, "Login attempt successful");
            loggedIn = true;
        }
        else
        {
            TwitchStream.LOGGER.warn(TwitchStream.STREAM_MARKER, "Login attempt unsuccessful: {} (error code {})", new Object[] {ErrorCode.getString(p_152897_1_), Integer.valueOf(p_152897_1_.getValue())});
            loggedIn = false;
        }
    }

    public void func_152898_a(ErrorCode p_152898_1_, GameInfo[] p_152898_2_)
    {
    }

    public void func_152891_a(BroadcastController.BroadcastState p_152891_1_)
    {
        TwitchStream.LOGGER.debug(TwitchStream.STREAM_MARKER, "Broadcast state changed to {}", new Object[] {p_152891_1_});

        if (p_152891_1_ == BroadcastController.BroadcastState.Initialized)
        {
            broadcastController.func_152827_a(BroadcastController.BroadcastState.Authenticated);
        }
    }

    public void func_152895_a()
    {
        TwitchStream.LOGGER.info(TwitchStream.STREAM_MARKER, "Logged out of twitch");
    }

    public void func_152894_a(StreamInfo p_152894_1_)
    {
        TwitchStream.LOGGER.debug(TwitchStream.STREAM_MARKER, "Stream info updated; {} viewers on stream ID {}", new Object[] {Integer.valueOf(p_152894_1_.viewers), Long.valueOf(p_152894_1_.streamId)});
    }

    public void func_152896_a(IngestList p_152896_1_)
    {
    }

    public void func_152893_b(ErrorCode p_152893_1_)
    {
        TwitchStream.LOGGER.warn(TwitchStream.STREAM_MARKER, "Issue submitting frame: {} (Error code {})", new Object[] {ErrorCode.getString(p_152893_1_), Integer.valueOf(p_152893_1_.getValue())});
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText("Issue streaming frame: " + p_152893_1_ + " (" + ErrorCode.getString(p_152893_1_) + ")"), 2);
    }

    public void func_152899_b()
    {
        updateStreamVolume();
        TwitchStream.LOGGER.info(TwitchStream.STREAM_MARKER, "Broadcast to Twitch has started");
    }

    public void func_152901_c()
    {
        TwitchStream.LOGGER.info(TwitchStream.STREAM_MARKER, "Broadcast to Twitch has stopped");
    }

    public void func_152892_c(ErrorCode p_152892_1_)
    {
        if (p_152892_1_ == ErrorCode.TTV_EC_SOUNDFLOWER_NOT_INSTALLED)
        {
            IChatComponent ichatcomponent = new ChatComponentTranslation("stream.unavailable.soundflower.chat.link");
            ichatcomponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://help.mojang.com/customer/portal/articles/1374877-configuring-soundflower-for-streaming-on-apple-computers"));
            ichatcomponent.getChatStyle().setUnderlined(Boolean.valueOf(true));
            IChatComponent ichatcomponent1 = new ChatComponentTranslation("stream.unavailable.soundflower.chat", ichatcomponent);
            ichatcomponent1.getChatStyle().setColor(EnumChatFormatting.DARK_RED);
            mc.ingameGUI.getChatGUI().printChatMessage(ichatcomponent1);
        }
        else
        {
            IChatComponent ichatcomponent2 = new ChatComponentTranslation("stream.unavailable.unknown.chat", ErrorCode.getString(p_152892_1_));
            ichatcomponent2.getChatStyle().setColor(EnumChatFormatting.DARK_RED);
            mc.ingameGUI.getChatGUI().printChatMessage(ichatcomponent2);
        }
    }

    public void func_152907_a(IngestServerTester p_152907_1_, IngestServerTester.IngestTestState p_152907_2_)
    {
        TwitchStream.LOGGER.debug(TwitchStream.STREAM_MARKER, "Ingest test state changed to {}", new Object[] {p_152907_2_});

        if (p_152907_2_ == IngestServerTester.IngestTestState.Finished)
        {
            field_152960_l = true;
        }
    }

    public static int formatStreamFps(float p_152948_0_)
    {
        return MathHelper.floor_float(10.0F + p_152948_0_ * 50.0F);
    }

    public static int formatStreamKbps(float p_152946_0_)
    {
        return MathHelper.floor_float(230.0F + p_152946_0_ * 3270.0F);
    }

    public static float formatStreamBps(float p_152947_0_)
    {
        return 0.1F + p_152947_0_ * 0.1F;
    }

    public IngestServer[] func_152925_v()
    {
        return broadcastController.func_152855_t().getServers();
    }

    public void func_152909_x()
    {
        IngestServerTester ingestservertester = broadcastController.func_152838_J();

        if (ingestservertester != null)
        {
            ingestservertester.func_153042_a(this);
        }
    }

    public IngestServerTester func_152932_y()
    {
        return broadcastController.isReady();
    }

    public boolean func_152908_z()
    {
        return broadcastController.isIngestTesting();
    }

    public int func_152920_A()
    {
        return isBroadcasting() ? broadcastController.getStreamInfo().viewers : 0;
    }

    public void func_176023_d(ErrorCode p_176023_1_)
    {
        if (ErrorCode.failed(p_176023_1_))
        {
            TwitchStream.LOGGER.error(TwitchStream.STREAM_MARKER, "Chat failed to initialize");
        }
    }

    public void func_176022_e(ErrorCode p_176022_1_)
    {
        if (ErrorCode.failed(p_176022_1_))
        {
            TwitchStream.LOGGER.error(TwitchStream.STREAM_MARKER, "Chat failed to shutdown");
        }
    }

    public void func_176017_a(ChatController.ChatState p_176017_1_)
    {
    }

    public void func_180605_a(String p_180605_1_, ChatRawMessage[] p_180605_2_)
    {
        for (ChatRawMessage chatrawmessage : p_180605_2_)
        {
            func_176027_a(chatrawmessage.userName, chatrawmessage);

            if (func_176028_a(chatrawmessage.modes, chatrawmessage.subscriptions, mc.gameSettings.streamChatUserFilter))
            {
                IChatComponent ichatcomponent = new ChatComponentText(chatrawmessage.userName);
                IChatComponent ichatcomponent1 = new ChatComponentTranslation("chat.stream." + (chatrawmessage.action ? "emote" : "text"), twitchComponent, ichatcomponent, EnumChatFormatting.getTextWithoutFormattingCodes(chatrawmessage.message));

                if (chatrawmessage.action)
                {
                    ichatcomponent1.getChatStyle().setItalic(Boolean.valueOf(true));
                }

                IChatComponent ichatcomponent2 = new ChatComponentText("");
                ichatcomponent2.appendSibling(new ChatComponentTranslation("stream.userinfo.chatTooltip"));

                for (IChatComponent ichatcomponent3 : GuiTwitchUserMode.func_152328_a(chatrawmessage.modes, chatrawmessage.subscriptions, null))
                {
                    ichatcomponent2.appendText("\n");
                    ichatcomponent2.appendSibling(ichatcomponent3);
                }

                ichatcomponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ichatcomponent2));
                ichatcomponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.TWITCH_USER_INFO, chatrawmessage.userName));
                mc.ingameGUI.getChatGUI().printChatMessage(ichatcomponent1);
            }
        }
    }

    public void func_176025_a(String p_176025_1_, ChatTokenizedMessage[] p_176025_2_)
    {
    }

    private void func_176027_a(String p_176027_1_, ChatRawMessage p_176027_2_)
    {
        ChatUserInfo chatuserinfo = field_152955_g.get(p_176027_1_);

        if (chatuserinfo == null)
        {
            chatuserinfo = new ChatUserInfo();
            chatuserinfo.displayName = p_176027_1_;
            field_152955_g.put(p_176027_1_, chatuserinfo);
        }

        chatuserinfo.subscriptions = p_176027_2_.subscriptions;
        chatuserinfo.modes = p_176027_2_.modes;
        chatuserinfo.nameColorARGB = p_176027_2_.nameColorARGB;
    }

    private boolean func_176028_a(Set<ChatUserMode> p_176028_1_, Set<ChatUserSubscription> p_176028_2_, int p_176028_3_)
    {
        return !p_176028_1_.contains(ChatUserMode.TTV_CHAT_USERMODE_BANNED) && (p_176028_1_.contains(ChatUserMode.TTV_CHAT_USERMODE_ADMINSTRATOR) || (p_176028_1_.contains(ChatUserMode.TTV_CHAT_USERMODE_MODERATOR) || (p_176028_1_.contains(ChatUserMode.TTV_CHAT_USERMODE_STAFF) || (p_176028_3_ == 0 || (p_176028_3_ == 1 && p_176028_2_.contains(ChatUserSubscription.TTV_CHAT_USERSUB_SUBSCRIBER))))));
    }

    public void func_176018_a(String p_176018_1_, ChatUserInfo[] p_176018_2_, ChatUserInfo[] p_176018_3_, ChatUserInfo[] p_176018_4_)
    {
        for (ChatUserInfo chatuserinfo : p_176018_3_)
        {
            field_152955_g.remove(chatuserinfo.displayName);
        }

        for (ChatUserInfo chatuserinfo1 : p_176018_4_)
        {
            field_152955_g.put(chatuserinfo1.displayName, chatuserinfo1);
        }

        for (ChatUserInfo chatuserinfo2 : p_176018_2_)
        {
            field_152955_g.put(chatuserinfo2.displayName, chatuserinfo2);
        }
    }

    public void func_180606_a(String p_180606_1_)
    {
        TwitchStream.LOGGER.debug(TwitchStream.STREAM_MARKER, "Chat connected");
    }

    public void func_180607_b(String p_180607_1_)
    {
        TwitchStream.LOGGER.debug(TwitchStream.STREAM_MARKER, "Chat disconnected");
        field_152955_g.clear();
    }

    public void func_176019_a(String p_176019_1_, String p_176019_2_)
    {
    }

    public void func_176021_d()
    {
    }

    public void func_176024_e()
    {
    }

    public void func_176016_c(String p_176016_1_)
    {
    }

    public void func_176020_d(String p_176020_1_)
    {
    }

    public boolean func_152927_B()
    {
        return field_176029_e != null && field_176029_e.equals(broadcastController.getChannelInfo().name);
    }

    public String func_152921_C()
    {
        return field_176029_e;
    }

    public ChatUserInfo func_152926_a(String p_152926_1_)
    {
        return field_152955_g.get(p_152926_1_);
    }

    public void func_152917_b(String p_152917_1_)
    {
        chatController.func_175986_a(field_176029_e, p_152917_1_);
    }

    public boolean func_152928_D()
    {
        return TwitchStream.field_152965_q && broadcastController.func_152858_b();
    }

    public ErrorCode func_152912_E()
    {
        return !TwitchStream.field_152965_q ? ErrorCode.TTV_EC_OS_TOO_OLD : broadcastController.getErrorCode();
    }

    public boolean func_152913_F()
    {
        return loggedIn;
    }

    /**
     * mutes or unmutes the microphone based on the boolean parameter passed into the method
     */
    public void muteMicrophone(boolean p_152910_1_)
    {
        field_152963_o = p_152910_1_;
        updateStreamVolume();
    }

    public boolean func_152929_G()
    {
        boolean flag = mc.gameSettings.streamMicToggleBehavior == 1;
        return field_152962_n || mc.gameSettings.streamMicVolume <= 0.0F || flag != field_152963_o;
    }

    public IStream.AuthFailureReason func_152918_H()
    {
        return authFailureReason;
    }

    static
    {
        try
        {
            if (Util.getOSType() == Util.EnumOS.WINDOWS)
            {
                System.loadLibrary("avutil-ttv-51");
                System.loadLibrary("swresample-ttv-0");
                System.loadLibrary("libmp3lame-ttv");

                if (System.getProperty("os.arch").contains("64"))
                {
                    System.loadLibrary("libmfxsw64");
                }
                else
                {
                    System.loadLibrary("libmfxsw32");
                }
            }

            TwitchStream.field_152965_q = true;
        }
        catch (Throwable var1)
        {
            TwitchStream.field_152965_q = false;
        }
    }
}
