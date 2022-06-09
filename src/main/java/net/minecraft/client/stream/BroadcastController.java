package net.minecraft.client.stream;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ThreadSafeBoundList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tv.twitch.AuthToken;
import tv.twitch.Core;
import tv.twitch.ErrorCode;
import tv.twitch.MessageLevel;
import tv.twitch.StandardCoreAPI;
import tv.twitch.broadcast.ArchivingState;
import tv.twitch.broadcast.AudioDeviceType;
import tv.twitch.broadcast.AudioParams;
import tv.twitch.broadcast.ChannelInfo;
import tv.twitch.broadcast.DesktopStreamAPI;
import tv.twitch.broadcast.EncodingCpuUsage;
import tv.twitch.broadcast.FrameBuffer;
import tv.twitch.broadcast.GameInfo;
import tv.twitch.broadcast.GameInfoList;
import tv.twitch.broadcast.IStatCallbacks;
import tv.twitch.broadcast.IStreamCallbacks;
import tv.twitch.broadcast.IngestList;
import tv.twitch.broadcast.IngestServer;
import tv.twitch.broadcast.PixelFormat;
import tv.twitch.broadcast.StartFlags;
import tv.twitch.broadcast.StatType;
import tv.twitch.broadcast.Stream;
import tv.twitch.broadcast.StreamInfo;
import tv.twitch.broadcast.StreamInfoForSetting;
import tv.twitch.broadcast.UserInfo;
import tv.twitch.broadcast.VideoParams;

public class BroadcastController
{
    private static final Logger logger = LogManager.getLogger();
    protected final int field_152865_a = 30;
    protected final int field_152866_b = 3;
    private static final ThreadSafeBoundList<String> field_152862_C = new ThreadSafeBoundList(String.class, 50);
    private String field_152863_D = null;
    protected BroadcastController.BroadcastListener broadcastListener = null;
    protected String field_152868_d = "";
    protected String field_152869_e = "";
    protected String field_152870_f = "";
    protected boolean field_152871_g = true;
    protected Core field_152872_h = null;
    protected Stream field_152873_i = null;
    protected List<FrameBuffer> field_152874_j = Lists.newArrayList();
    protected List<FrameBuffer> field_152875_k = Lists.newArrayList();
    protected boolean field_152876_l = false;
    protected boolean field_152877_m = false;
    protected boolean field_152878_n = false;
    protected BroadcastController.BroadcastState broadcastState = BroadcastController.BroadcastState.Uninitialized;
    protected String field_152880_p = null;
    protected VideoParams videoParamaters = null;
    protected AudioParams audioParamaters = null;
    protected IngestList ingestList = new IngestList(new IngestServer[0]);
    protected IngestServer field_152884_t = null;
    protected AuthToken authenticationToken = new AuthToken();
    protected ChannelInfo channelInfo = new ChannelInfo();
    protected UserInfo userInfo = new UserInfo();
    protected StreamInfo streamInfo = new StreamInfo();
    protected ArchivingState field_152889_y = new ArchivingState();
    protected long field_152890_z = 0L;
    protected IngestServerTester field_152860_A = null;
    private ErrorCode errorCode;
    protected IStreamCallbacks field_177948_B = new IStreamCallbacks()
    {
        public void requestAuthTokenCallback(ErrorCode p_requestAuthTokenCallback_1_, AuthToken p_requestAuthTokenCallback_2_)
        {
            if (ErrorCode.succeeded(p_requestAuthTokenCallback_1_))
            {
                authenticationToken = p_requestAuthTokenCallback_2_;
                func_152827_a(BroadcastController.BroadcastState.Authenticated);
            }
            else
            {
                authenticationToken.data = "";
                func_152827_a(BroadcastController.BroadcastState.Initialized);
                String s = ErrorCode.getString(p_requestAuthTokenCallback_1_);
                func_152820_d(String.format("RequestAuthTokenDoneCallback got failure: %s", s));
            }

            try
            {
                if (broadcastListener != null)
                {
                    broadcastListener.func_152900_a(p_requestAuthTokenCallback_1_, p_requestAuthTokenCallback_2_);
                }
            }
            catch (Exception exception)
            {
                func_152820_d(exception.toString());
            }
        }
        public void loginCallback(ErrorCode p_loginCallback_1_, ChannelInfo p_loginCallback_2_)
        {
            if (ErrorCode.succeeded(p_loginCallback_1_))
            {
                channelInfo = p_loginCallback_2_;
                func_152827_a(BroadcastController.BroadcastState.LoggedIn);
                field_152877_m = true;
            }
            else
            {
                func_152827_a(BroadcastController.BroadcastState.Initialized);
                field_152877_m = false;
                String s = ErrorCode.getString(p_loginCallback_1_);
                func_152820_d(String.format("LoginCallback got failure: %s", s));
            }

            try
            {
                if (broadcastListener != null)
                {
                    broadcastListener.func_152897_a(p_loginCallback_1_);
                }
            }
            catch (Exception exception)
            {
                func_152820_d(exception.toString());
            }
        }
        public void getIngestServersCallback(ErrorCode p_getIngestServersCallback_1_, IngestList p_getIngestServersCallback_2_)
        {
            if (ErrorCode.succeeded(p_getIngestServersCallback_1_))
            {
                ingestList = p_getIngestServersCallback_2_;
                field_152884_t = ingestList.getDefaultServer();
                func_152827_a(BroadcastController.BroadcastState.ReceivedIngestServers);

                try
                {
                    if (broadcastListener != null)
                    {
                        broadcastListener.func_152896_a(p_getIngestServersCallback_2_);
                    }
                }
                catch (Exception exception)
                {
                    func_152820_d(exception.toString());
                }
            }
            else
            {
                String s = ErrorCode.getString(p_getIngestServersCallback_1_);
                func_152820_d(String.format("IngestListCallback got failure: %s", s));
                func_152827_a(BroadcastController.BroadcastState.LoggingIn);
            }
        }
        public void getUserInfoCallback(ErrorCode p_getUserInfoCallback_1_, UserInfo p_getUserInfoCallback_2_)
        {
            userInfo = p_getUserInfoCallback_2_;

            if (ErrorCode.failed(p_getUserInfoCallback_1_))
            {
                String s = ErrorCode.getString(p_getUserInfoCallback_1_);
                func_152820_d(String.format("UserInfoDoneCallback got failure: %s", s));
            }
        }
        public void getStreamInfoCallback(ErrorCode p_getStreamInfoCallback_1_, StreamInfo p_getStreamInfoCallback_2_)
        {
            if (ErrorCode.succeeded(p_getStreamInfoCallback_1_))
            {
                streamInfo = p_getStreamInfoCallback_2_;

                try
                {
                    if (broadcastListener != null)
                    {
                        broadcastListener.func_152894_a(p_getStreamInfoCallback_2_);
                    }
                }
                catch (Exception exception)
                {
                    func_152820_d(exception.toString());
                }
            }
            else
            {
                String s = ErrorCode.getString(p_getStreamInfoCallback_1_);
                func_152832_e(String.format("StreamInfoDoneCallback got failure: %s", s));
            }
        }
        public void getArchivingStateCallback(ErrorCode p_getArchivingStateCallback_1_, ArchivingState p_getArchivingStateCallback_2_)
        {
            field_152889_y = p_getArchivingStateCallback_2_;

            if (ErrorCode.failed(p_getArchivingStateCallback_1_))
            {
            }
        }
        public void runCommercialCallback(ErrorCode p_runCommercialCallback_1_)
        {
            if (ErrorCode.failed(p_runCommercialCallback_1_))
            {
                String s = ErrorCode.getString(p_runCommercialCallback_1_);
                func_152832_e(String.format("RunCommercialCallback got failure: %s", s));
            }
        }
        public void setStreamInfoCallback(ErrorCode p_setStreamInfoCallback_1_)
        {
            if (ErrorCode.failed(p_setStreamInfoCallback_1_))
            {
                String s = ErrorCode.getString(p_setStreamInfoCallback_1_);
                func_152832_e(String.format("SetStreamInfoCallback got failure: %s", s));
            }
        }
        public void getGameNameListCallback(ErrorCode p_getGameNameListCallback_1_, GameInfoList p_getGameNameListCallback_2_)
        {
            if (ErrorCode.failed(p_getGameNameListCallback_1_))
            {
                String s = ErrorCode.getString(p_getGameNameListCallback_1_);
                func_152820_d(String.format("GameNameListCallback got failure: %s", s));
            }

            try
            {
                if (broadcastListener != null)
                {
                    broadcastListener.func_152898_a(p_getGameNameListCallback_1_, p_getGameNameListCallback_2_ == null ? new GameInfo[0] : p_getGameNameListCallback_2_.list);
                }
            }
            catch (Exception exception)
            {
                func_152820_d(exception.toString());
            }
        }
        public void bufferUnlockCallback(long p_bufferUnlockCallback_1_)
        {
            FrameBuffer framebuffer = FrameBuffer.lookupBuffer(p_bufferUnlockCallback_1_);
            field_152875_k.add(framebuffer);
        }
        public void startCallback(ErrorCode p_startCallback_1_)
        {
            if (ErrorCode.succeeded(p_startCallback_1_))
            {
                try
                {
                    if (broadcastListener != null)
                    {
                        broadcastListener.func_152899_b();
                    }
                }
                catch (Exception exception1)
                {
                    func_152820_d(exception1.toString());
                }

                func_152827_a(BroadcastController.BroadcastState.Broadcasting);
            }
            else
            {
                videoParamaters = null;
                audioParamaters = null;
                func_152827_a(BroadcastController.BroadcastState.ReadyToBroadcast);

                try
                {
                    if (broadcastListener != null)
                    {
                        broadcastListener.func_152892_c(p_startCallback_1_);
                    }
                }
                catch (Exception exception)
                {
                    func_152820_d(exception.toString());
                }

                String s = ErrorCode.getString(p_startCallback_1_);
                func_152820_d(String.format("startCallback got failure: %s", s));
            }
        }
        public void stopCallback(ErrorCode p_stopCallback_1_)
        {
            if (ErrorCode.succeeded(p_stopCallback_1_))
            {
                videoParamaters = null;
                audioParamaters = null;
                func_152831_M();

                try
                {
                    if (broadcastListener != null)
                    {
                        broadcastListener.func_152901_c();
                    }
                }
                catch (Exception exception)
                {
                    func_152820_d(exception.toString());
                }

                if (field_152877_m)
                {
                    func_152827_a(BroadcastController.BroadcastState.ReadyToBroadcast);
                }
                else
                {
                    func_152827_a(BroadcastController.BroadcastState.Initialized);
                }
            }
            else
            {
                func_152827_a(BroadcastController.BroadcastState.ReadyToBroadcast);
                String s = ErrorCode.getString(p_stopCallback_1_);
                func_152820_d(String.format("stopCallback got failure: %s", s));
            }
        }
        public void sendActionMetaDataCallback(ErrorCode p_sendActionMetaDataCallback_1_)
        {
            if (ErrorCode.failed(p_sendActionMetaDataCallback_1_))
            {
                String s = ErrorCode.getString(p_sendActionMetaDataCallback_1_);
                func_152820_d(String.format("sendActionMetaDataCallback got failure: %s", s));
            }
        }
        public void sendStartSpanMetaDataCallback(ErrorCode p_sendStartSpanMetaDataCallback_1_)
        {
            if (ErrorCode.failed(p_sendStartSpanMetaDataCallback_1_))
            {
                String s = ErrorCode.getString(p_sendStartSpanMetaDataCallback_1_);
                func_152820_d(String.format("sendStartSpanMetaDataCallback got failure: %s", s));
            }
        }
        public void sendEndSpanMetaDataCallback(ErrorCode p_sendEndSpanMetaDataCallback_1_)
        {
            if (ErrorCode.failed(p_sendEndSpanMetaDataCallback_1_))
            {
                String s = ErrorCode.getString(p_sendEndSpanMetaDataCallback_1_);
                func_152820_d(String.format("sendEndSpanMetaDataCallback got failure: %s", s));
            }
        }
    };
    protected IStatCallbacks field_177949_C = new IStatCallbacks()
    {
        public void statCallback(StatType p_statCallback_1_, long p_statCallback_2_)
        {
        }
    };

    public void func_152841_a(BroadcastController.BroadcastListener p_152841_1_)
    {
        broadcastListener = p_152841_1_;
    }

    public boolean func_152858_b()
    {
        return field_152876_l;
    }

    public void func_152842_a(String p_152842_1_)
    {
        field_152868_d = p_152842_1_;
    }

    public StreamInfo getStreamInfo()
    {
        return streamInfo;
    }

    public ChannelInfo getChannelInfo()
    {
        return channelInfo;
    }

    public boolean isBroadcasting()
    {
        return broadcastState == BroadcastController.BroadcastState.Broadcasting || broadcastState == BroadcastController.BroadcastState.Paused;
    }

    public boolean isReadyToBroadcast()
    {
        return broadcastState == BroadcastController.BroadcastState.ReadyToBroadcast;
    }

    public boolean isIngestTesting()
    {
        return broadcastState == BroadcastController.BroadcastState.IngestTesting;
    }

    public boolean isBroadcastPaused()
    {
        return broadcastState == BroadcastController.BroadcastState.Paused;
    }

    public boolean func_152849_q()
    {
        return field_152877_m;
    }

    public IngestServer func_152833_s()
    {
        return field_152884_t;
    }

    public void func_152824_a(IngestServer p_152824_1_)
    {
        field_152884_t = p_152824_1_;
    }

    public IngestList func_152855_t()
    {
        return ingestList;
    }

    public void setRecordingDeviceVolume(float p_152829_1_)
    {
        field_152873_i.setVolume(AudioDeviceType.TTV_RECORDER_DEVICE, p_152829_1_);
    }

    public void setPlaybackDeviceVolume(float p_152837_1_)
    {
        field_152873_i.setVolume(AudioDeviceType.TTV_PLAYBACK_DEVICE, p_152837_1_);
    }

    public IngestServerTester isReady()
    {
        return field_152860_A;
    }

    public long func_152844_x()
    {
        return field_152873_i.getStreamTime();
    }

    protected boolean func_152848_y()
    {
        return true;
    }

    public ErrorCode getErrorCode()
    {
        return errorCode;
    }

    public BroadcastController()
    {
        field_152872_h = Core.getInstance();

        if (Core.getInstance() == null)
        {
            field_152872_h = new Core(new StandardCoreAPI());
        }

        field_152873_i = new Stream(new DesktopStreamAPI());
    }

    protected PixelFormat func_152826_z()
    {
        return PixelFormat.TTV_PF_RGBA;
    }

    public boolean func_152817_A()
    {
        if (field_152876_l)
        {
            return false;
        }
        else
        {
            field_152873_i.setStreamCallbacks(field_177948_B);
            ErrorCode errorcode = field_152872_h.initialize(field_152868_d, System.getProperty("java.library.path"));

            if (!func_152853_a(errorcode))
            {
                field_152873_i.setStreamCallbacks(null);
                errorCode = errorcode;
                return false;
            }
            else
            {
                errorcode = field_152872_h.setTraceLevel(MessageLevel.TTV_ML_ERROR);

                if (!func_152853_a(errorcode))
                {
                    field_152873_i.setStreamCallbacks(null);
                    field_152872_h.shutdown();
                    errorCode = errorcode;
                    return false;
                }
                else if (ErrorCode.succeeded(errorcode))
                {
                    field_152876_l = true;
                    func_152827_a(BroadcastController.BroadcastState.Initialized);
                    return true;
                }
                else
                {
                    errorCode = errorcode;
                    field_152872_h.shutdown();
                    return false;
                }
            }
        }
    }

    public boolean func_152851_B()
    {
        if (!field_152876_l)
        {
            return true;
        }
        else if (isIngestTesting())
        {
            return false;
        }
        else
        {
            field_152878_n = true;
            func_152845_C();
            field_152873_i.setStreamCallbacks(null);
            field_152873_i.setStatCallbacks(null);
            ErrorCode errorcode = field_152872_h.shutdown();
            func_152853_a(errorcode);
            field_152876_l = false;
            field_152878_n = false;
            func_152827_a(BroadcastController.BroadcastState.Uninitialized);
            return true;
        }
    }

    public void statCallback()
    {
        if (broadcastState != BroadcastController.BroadcastState.Uninitialized)
        {
            if (field_152860_A != null)
            {
                field_152860_A.func_153039_l();
            }

            for (; field_152860_A != null; func_152821_H())
            {
                try
                {
                    Thread.sleep(200L);
                }
                catch (Exception exception)
                {
                    func_152820_d(exception.toString());
                }
            }

            func_152851_B();
        }
    }

    public boolean func_152818_a(String p_152818_1_, AuthToken p_152818_2_)
    {
        if (isIngestTesting())
        {
            return false;
        }
        else
        {
            func_152845_C();

            if (p_152818_1_ != null && !p_152818_1_.isEmpty())
            {
                if (p_152818_2_ != null && p_152818_2_.data != null && !p_152818_2_.data.isEmpty())
                {
                    field_152880_p = p_152818_1_;
                    authenticationToken = p_152818_2_;

                    if (func_152858_b())
                    {
                        func_152827_a(BroadcastController.BroadcastState.Authenticated);
                    }

                    return true;
                }
                else
                {
                    func_152820_d("Auth token must be valid");
                    return false;
                }
            }
            else
            {
                func_152820_d("Username must be valid");
                return false;
            }
        }
    }

    public boolean func_152845_C()
    {
        if (isIngestTesting())
        {
            return false;
        }
        else
        {
            if (isBroadcasting())
            {
                field_152873_i.stop(false);
            }

            field_152880_p = "";
            authenticationToken = new AuthToken();

            if (!field_152877_m)
            {
                return false;
            }
            else
            {
                field_152877_m = false;

                if (!field_152878_n)
                {
                    try
                    {
                        if (broadcastListener != null)
                        {
                            broadcastListener.func_152895_a();
                        }
                    }
                    catch (Exception exception)
                    {
                        func_152820_d(exception.toString());
                    }
                }

                func_152827_a(BroadcastController.BroadcastState.Initialized);
                return true;
            }
        }
    }

    public boolean func_152828_a(String p_152828_1_, String p_152828_2_, String p_152828_3_)
    {
        if (!field_152877_m)
        {
            return false;
        }
        else
        {
            if (p_152828_1_ == null || p_152828_1_.equals(""))
            {
                p_152828_1_ = field_152880_p;
            }

            if (p_152828_2_ == null)
            {
                p_152828_2_ = "";
            }

            if (p_152828_3_ == null)
            {
                p_152828_3_ = "";
            }

            StreamInfoForSetting streaminfoforsetting = new StreamInfoForSetting();
            streaminfoforsetting.streamTitle = p_152828_3_;
            streaminfoforsetting.gameName = p_152828_2_;
            ErrorCode errorcode = field_152873_i.setStreamInfo(authenticationToken, p_152828_1_, streaminfoforsetting);
            func_152853_a(errorcode);
            return ErrorCode.succeeded(errorcode);
        }
    }

    public boolean requestCommercial()
    {
        if (!isBroadcasting())
        {
            return false;
        }
        else
        {
            ErrorCode errorcode = field_152873_i.runCommercial(authenticationToken);
            func_152853_a(errorcode);
            return ErrorCode.succeeded(errorcode);
        }
    }

    public VideoParams func_152834_a(int maxKbps, int p_152834_2_, float p_152834_3_, float p_152834_4_)
    {
        int[] aint = field_152873_i.getMaxResolution(maxKbps, p_152834_2_, p_152834_3_, p_152834_4_);
        VideoParams videoparams = new VideoParams();
        videoparams.maxKbps = maxKbps;
        videoparams.encodingCpuUsage = EncodingCpuUsage.TTV_ECU_HIGH;
        videoparams.pixelFormat = func_152826_z();
        videoparams.targetFps = p_152834_2_;
        videoparams.outputWidth = aint[0];
        videoparams.outputHeight = aint[1];
        videoparams.disableAdaptiveBitrate = false;
        videoparams.verticalFlip = false;
        return videoparams;
    }

    public boolean func_152836_a(VideoParams p_152836_1_)
    {
        if (p_152836_1_ != null && isReadyToBroadcast())
        {
            videoParamaters = p_152836_1_.clone();
            audioParamaters = new AudioParams();
            audioParamaters.audioEnabled = field_152871_g && func_152848_y();
            audioParamaters.enableMicCapture = audioParamaters.audioEnabled;
            audioParamaters.enablePlaybackCapture = audioParamaters.audioEnabled;
            audioParamaters.enablePassthroughAudio = false;

            if (!func_152823_L())
            {
                videoParamaters = null;
                audioParamaters = null;
                return false;
            }
            else
            {
                ErrorCode errorcode = field_152873_i.start(p_152836_1_, audioParamaters, field_152884_t, StartFlags.None, true);

                if (ErrorCode.failed(errorcode))
                {
                    func_152831_M();
                    String s = ErrorCode.getString(errorcode);
                    func_152820_d(String.format("Error while starting to broadcast: %s", s));
                    videoParamaters = null;
                    audioParamaters = null;
                    return false;
                }
                else
                {
                    func_152827_a(BroadcastController.BroadcastState.Starting);
                    return true;
                }
            }
        }
        else
        {
            return false;
        }
    }

    public boolean stopBroadcasting()
    {
        if (!isBroadcasting())
        {
            return false;
        }
        else
        {
            ErrorCode errorcode = field_152873_i.stop(true);

            if (ErrorCode.failed(errorcode))
            {
                String s = ErrorCode.getString(errorcode);
                func_152820_d(String.format("Error while stopping the broadcast: %s", s));
                return false;
            }
            else
            {
                func_152827_a(BroadcastController.BroadcastState.Stopping);
                return ErrorCode.succeeded(errorcode);
            }
        }
    }

    public boolean func_152847_F()
    {
        if (!isBroadcasting())
        {
            return false;
        }
        else
        {
            ErrorCode errorcode = field_152873_i.pauseVideo();

            if (ErrorCode.failed(errorcode))
            {
                stopBroadcasting();
                String s = ErrorCode.getString(errorcode);
                func_152820_d(String.format("Error pausing stream: %s\n", s));
            }
            else
            {
                func_152827_a(BroadcastController.BroadcastState.Paused);
            }

            return ErrorCode.succeeded(errorcode);
        }
    }

    public boolean func_152854_G()
    {
        if (!isBroadcastPaused())
        {
            return false;
        }
        else
        {
            func_152827_a(BroadcastController.BroadcastState.Broadcasting);
            return true;
        }
    }

    public boolean func_152840_a(String p_152840_1_, long p_152840_2_, String p_152840_4_, String p_152840_5_)
    {
        ErrorCode errorcode = field_152873_i.sendActionMetaData(authenticationToken, p_152840_1_, p_152840_2_, p_152840_4_, p_152840_5_);

        if (ErrorCode.failed(errorcode))
        {
            String s = ErrorCode.getString(errorcode);
            func_152820_d(String.format("Error while sending meta data: %s\n", s));
            return false;
        }
        else
        {
            return true;
        }
    }

    public long func_177946_b(String p_177946_1_, long p_177946_2_, String p_177946_4_, String p_177946_5_)
    {
        long i = field_152873_i.sendStartSpanMetaData(authenticationToken, p_177946_1_, p_177946_2_, p_177946_4_, p_177946_5_);

        if (i == -1L)
        {
            func_152820_d(String.format("Error in SendStartSpanMetaData\n"));
        }

        return i;
    }

    public boolean func_177947_a(String p_177947_1_, long p_177947_2_, long p_177947_4_, String p_177947_6_, String p_177947_7_)
    {
        if (p_177947_4_ == -1L)
        {
            func_152820_d(String.format("Invalid sequence id: %d\n", Long.valueOf(p_177947_4_)));
            return false;
        }
        else
        {
            ErrorCode errorcode = field_152873_i.sendEndSpanMetaData(authenticationToken, p_177947_1_, p_177947_2_, p_177947_4_, p_177947_6_, p_177947_7_);

            if (ErrorCode.failed(errorcode))
            {
                String s = ErrorCode.getString(errorcode);
                func_152820_d(String.format("Error in SendStopSpanMetaData: %s\n", s));
                return false;
            }
            else
            {
                return true;
            }
        }
    }

    protected void func_152827_a(BroadcastController.BroadcastState p_152827_1_)
    {
        if (p_152827_1_ != broadcastState)
        {
            broadcastState = p_152827_1_;

            try
            {
                if (broadcastListener != null)
                {
                    broadcastListener.func_152891_a(p_152827_1_);
                }
            }
            catch (Exception exception)
            {
                func_152820_d(exception.toString());
            }
        }
    }

    public void func_152821_H()
    {
        if (field_152873_i != null && field_152876_l)
        {
            ErrorCode errorcode = field_152873_i.pollTasks();
            func_152853_a(errorcode);

            if (isIngestTesting())
            {
                field_152860_A.func_153041_j();

                if (field_152860_A.func_153032_e())
                {
                    field_152860_A = null;
                    func_152827_a(BroadcastController.BroadcastState.ReadyToBroadcast);
                }
            }

            switch (broadcastState)
            {
                case Authenticated:
                    func_152827_a(BroadcastController.BroadcastState.LoggingIn);
                    errorcode = field_152873_i.login(authenticationToken);

                    if (ErrorCode.failed(errorcode))
                    {
                        String s3 = ErrorCode.getString(errorcode);
                        func_152820_d(String.format("Error in TTV_Login: %s\n", s3));
                    }

                    break;

                case LoggedIn:
                    func_152827_a(BroadcastController.BroadcastState.FindingIngestServer);
                    errorcode = field_152873_i.getIngestServers(authenticationToken);

                    if (ErrorCode.failed(errorcode))
                    {
                        func_152827_a(BroadcastController.BroadcastState.LoggedIn);
                        String s2 = ErrorCode.getString(errorcode);
                        func_152820_d(String.format("Error in TTV_GetIngestServers: %s\n", s2));
                    }

                    break;

                case ReceivedIngestServers:
                    func_152827_a(BroadcastController.BroadcastState.ReadyToBroadcast);
                    errorcode = field_152873_i.getUserInfo(authenticationToken);

                    if (ErrorCode.failed(errorcode))
                    {
                        String s = ErrorCode.getString(errorcode);
                        func_152820_d(String.format("Error in TTV_GetUserInfo: %s\n", s));
                    }

                    func_152835_I();
                    errorcode = field_152873_i.getArchivingState(authenticationToken);

                    if (ErrorCode.failed(errorcode))
                    {
                        String s1 = ErrorCode.getString(errorcode);
                        func_152820_d(String.format("Error in TTV_GetArchivingState: %s\n", s1));
                    }

                case Starting:
                case Stopping:
                case FindingIngestServer:
                case Authenticating:
                case Initialized:
                case Uninitialized:
                case IngestTesting:
                default:
                    break;

                case Paused:
                case Broadcasting:
                    func_152835_I();
            }
        }
    }

    protected void func_152835_I()
    {
        long i = System.nanoTime();
        long j = (i - field_152890_z) / 1000000000L;

        if (j >= 30L)
        {
            field_152890_z = i;
            ErrorCode errorcode = field_152873_i.getStreamInfo(authenticationToken, field_152880_p);

            if (ErrorCode.failed(errorcode))
            {
                String s = ErrorCode.getString(errorcode);
                func_152820_d(String.format("Error in TTV_GetStreamInfo: %s", s));
            }
        }
    }

    public IngestServerTester func_152838_J()
    {
        if (isReadyToBroadcast() && ingestList != null)
        {
            if (isIngestTesting())
            {
                return null;
            }
            else
            {
                field_152860_A = new IngestServerTester(field_152873_i, ingestList);
                field_152860_A.func_176004_j();
                func_152827_a(BroadcastController.BroadcastState.IngestTesting);
                return field_152860_A;
            }
        }
        else
        {
            return null;
        }
    }

    protected boolean func_152823_L()
    {
        for (int i = 0; i < 3; ++i)
        {
            FrameBuffer framebuffer = field_152873_i.allocateFrameBuffer(videoParamaters.outputWidth * videoParamaters.outputHeight * 4);

            if (!framebuffer.getIsValid())
            {
                func_152820_d(String.format("Error while allocating frame buffer"));
                return false;
            }

            field_152874_j.add(framebuffer);
            field_152875_k.add(framebuffer);
        }

        return true;
    }

    protected void func_152831_M()
    {
        for (int i = 0; i < field_152874_j.size(); ++i)
        {
            FrameBuffer framebuffer = field_152874_j.get(i);
            framebuffer.free();
        }

        field_152875_k.clear();
        field_152874_j.clear();
    }

    public FrameBuffer func_152822_N()
    {
        if (field_152875_k.size() == 0)
        {
            func_152820_d(String.format("Out of free buffers, this should never happen"));
            return null;
        }
        else
        {
            FrameBuffer framebuffer = field_152875_k.get(field_152875_k.size() - 1);
            field_152875_k.remove(field_152875_k.size() - 1);
            return framebuffer;
        }
    }

    /**
     * caputres the current framebuffer
     */
    public void captureFramebuffer(FrameBuffer p_152846_1_)
    {
        try
        {
            field_152873_i.captureFrameBuffer_ReadPixels(p_152846_1_);
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Trying to submit a frame to Twitch");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Broadcast State");
            crashreportcategory.addCrashSection("Last reported errors", Arrays.toString(BroadcastController.field_152862_C.func_152756_c()));
            crashreportcategory.addCrashSection("Buffer", p_152846_1_);
            crashreportcategory.addCrashSection("Free buffer count", Integer.valueOf(field_152875_k.size()));
            crashreportcategory.addCrashSection("Capture buffer count", Integer.valueOf(field_152874_j.size()));
            throw new ReportedException(crashreport);
        }
    }

    /**
     * passes the framebuffer on to the video stream
     */
    public ErrorCode submitStreamFrame(FrameBuffer p_152859_1_)
    {
        if (isBroadcastPaused())
        {
            func_152854_G();
        }
        else if (!isBroadcasting())
        {
            return ErrorCode.TTV_EC_STREAM_NOT_STARTED;
        }

        ErrorCode errorcode = field_152873_i.submitVideoFrame(p_152859_1_);

        if (errorcode != ErrorCode.TTV_EC_SUCCESS)
        {
            String s = ErrorCode.getString(errorcode);

            if (ErrorCode.succeeded(errorcode))
            {
                func_152832_e(String.format("Warning in SubmitTexturePointer: %s\n", s));
            }
            else
            {
                func_152820_d(String.format("Error in SubmitTexturePointer: %s\n", s));
                stopBroadcasting();
            }

            if (broadcastListener != null)
            {
                broadcastListener.func_152893_b(errorcode);
            }
        }

        return errorcode;
    }

    protected boolean func_152853_a(ErrorCode p_152853_1_)
    {
        if (ErrorCode.failed(p_152853_1_))
        {
            func_152820_d(ErrorCode.getString(p_152853_1_));
            return false;
        }
        else
        {
            return true;
        }
    }

    protected void func_152820_d(String p_152820_1_)
    {
        field_152863_D = p_152820_1_;
        BroadcastController.field_152862_C.func_152757_a("<Error> " + p_152820_1_);
        BroadcastController.logger.error(TwitchStream.STREAM_MARKER, "[Broadcast controller] {}", new Object[] {p_152820_1_});
    }

    protected void func_152832_e(String p_152832_1_)
    {
        BroadcastController.field_152862_C.func_152757_a("<Warning> " + p_152832_1_);
        BroadcastController.logger.warn(TwitchStream.STREAM_MARKER, "[Broadcast controller] {}", new Object[] {p_152832_1_});
    }

    public interface BroadcastListener
    {
        void func_152900_a(ErrorCode p_152900_1_, AuthToken p_152900_2_);

        void func_152897_a(ErrorCode p_152897_1_);

        void func_152898_a(ErrorCode p_152898_1_, GameInfo[] p_152898_2_);

        void func_152891_a(BroadcastController.BroadcastState p_152891_1_);

        void func_152895_a();

        void func_152894_a(StreamInfo p_152894_1_);

        void func_152896_a(IngestList p_152896_1_);

        void func_152893_b(ErrorCode p_152893_1_);

        void func_152899_b();

        void func_152901_c();

        void func_152892_c(ErrorCode p_152892_1_);
    }

    public static enum BroadcastState
    {
        Uninitialized,
        Initialized,
        Authenticating,
        Authenticated,
        LoggingIn,
        LoggedIn,
        FindingIngestServer,
        ReceivedIngestServers,
        ReadyToBroadcast,
        Starting,
        Broadcasting,
        Stopping,
        Paused,
        IngestTesting
    }
}
