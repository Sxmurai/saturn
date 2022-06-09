package net.minecraft.client.stream;

import com.google.common.collect.Lists;
import java.util.List;
import tv.twitch.AuthToken;
import tv.twitch.ErrorCode;
import tv.twitch.broadcast.ArchivingState;
import tv.twitch.broadcast.AudioParams;
import tv.twitch.broadcast.ChannelInfo;
import tv.twitch.broadcast.EncodingCpuUsage;
import tv.twitch.broadcast.FrameBuffer;
import tv.twitch.broadcast.GameInfoList;
import tv.twitch.broadcast.IStatCallbacks;
import tv.twitch.broadcast.IStreamCallbacks;
import tv.twitch.broadcast.IngestList;
import tv.twitch.broadcast.IngestServer;
import tv.twitch.broadcast.PixelFormat;
import tv.twitch.broadcast.RTMPState;
import tv.twitch.broadcast.StartFlags;
import tv.twitch.broadcast.StatType;
import tv.twitch.broadcast.Stream;
import tv.twitch.broadcast.StreamInfo;
import tv.twitch.broadcast.UserInfo;
import tv.twitch.broadcast.VideoParams;

public class IngestServerTester
{
    protected IngestServerTester.IngestTestListener field_153044_b = null;
    protected Stream field_153045_c = null;
    protected IngestList field_153046_d = null;
    protected IngestServerTester.IngestTestState field_153047_e = IngestServerTester.IngestTestState.Uninitalized;
    protected long field_153048_f = 8000L;
    protected long field_153049_g = 2000L;
    protected long field_153050_h = 0L;
    protected RTMPState field_153051_i = RTMPState.Invalid;
    protected VideoParams field_153052_j = null;
    protected AudioParams audioParameters = null;
    protected long field_153054_l = 0L;
    protected List<FrameBuffer> field_153055_m = null;
    protected boolean field_153056_n = false;
    protected IStreamCallbacks field_153057_o = null;
    protected IStatCallbacks field_153058_p = null;
    protected IngestServer field_153059_q = null;
    protected boolean field_153060_r = false;
    protected boolean field_153061_s = false;
    protected int field_153062_t = -1;
    protected int field_153063_u = 0;
    protected long field_153064_v = 0L;
    protected float field_153065_w = 0.0F;
    protected float field_153066_x = 0.0F;
    protected boolean field_176009_x = false;
    protected boolean field_176008_y = false;
    protected boolean field_176007_z = false;
    protected IStreamCallbacks field_176005_A = new IStreamCallbacks()
    {
        public void requestAuthTokenCallback(ErrorCode p_requestAuthTokenCallback_1_, AuthToken p_requestAuthTokenCallback_2_)
        {
        }
        public void loginCallback(ErrorCode p_loginCallback_1_, ChannelInfo p_loginCallback_2_)
        {
        }
        public void getIngestServersCallback(ErrorCode p_getIngestServersCallback_1_, IngestList p_getIngestServersCallback_2_)
        {
        }
        public void getUserInfoCallback(ErrorCode p_getUserInfoCallback_1_, UserInfo p_getUserInfoCallback_2_)
        {
        }
        public void getStreamInfoCallback(ErrorCode p_getStreamInfoCallback_1_, StreamInfo p_getStreamInfoCallback_2_)
        {
        }
        public void getArchivingStateCallback(ErrorCode p_getArchivingStateCallback_1_, ArchivingState p_getArchivingStateCallback_2_)
        {
        }
        public void runCommercialCallback(ErrorCode p_runCommercialCallback_1_)
        {
        }
        public void setStreamInfoCallback(ErrorCode p_setStreamInfoCallback_1_)
        {
        }
        public void getGameNameListCallback(ErrorCode p_getGameNameListCallback_1_, GameInfoList p_getGameNameListCallback_2_)
        {
        }
        public void bufferUnlockCallback(long p_bufferUnlockCallback_1_)
        {
        }
        public void startCallback(ErrorCode p_startCallback_1_)
        {
            field_176008_y = false;

            if (ErrorCode.succeeded(p_startCallback_1_))
            {
                field_176009_x = true;
                field_153054_l = System.currentTimeMillis();
                func_153034_a(IngestServerTester.IngestTestState.ConnectingToServer);
            }
            else
            {
                field_153056_n = false;
                func_153034_a(IngestServerTester.IngestTestState.DoneTestingServer);
            }
        }
        public void stopCallback(ErrorCode p_stopCallback_1_)
        {
            if (ErrorCode.failed(p_stopCallback_1_))
            {
                System.out.println("IngestTester.stopCallback failed to stop - " + field_153059_q.serverName + ": " + p_stopCallback_1_);
            }

            field_176007_z = false;
            field_176009_x = false;
            func_153034_a(IngestServerTester.IngestTestState.DoneTestingServer);
            field_153059_q = null;

            if (field_153060_r)
            {
                func_153034_a(IngestServerTester.IngestTestState.Cancelling);
            }
        }
        public void sendActionMetaDataCallback(ErrorCode p_sendActionMetaDataCallback_1_)
        {
        }
        public void sendStartSpanMetaDataCallback(ErrorCode p_sendStartSpanMetaDataCallback_1_)
        {
        }
        public void sendEndSpanMetaDataCallback(ErrorCode p_sendEndSpanMetaDataCallback_1_)
        {
        }
    };
    protected IStatCallbacks field_176006_B = new IStatCallbacks()
    {
        public void statCallback(StatType p_statCallback_1_, long p_statCallback_2_)
        {
            switch (p_statCallback_1_)
            {
                case TTV_ST_RTMPSTATE:
                    field_153051_i = RTMPState.lookupValue((int)p_statCallback_2_);
                    break;

                case TTV_ST_RTMPDATASENT:
                    field_153050_h = p_statCallback_2_;
            }
        }
    };

    public void func_153042_a(IngestServerTester.IngestTestListener p_153042_1_)
    {
        field_153044_b = p_153042_1_;
    }

    public IngestServer func_153040_c()
    {
        return field_153059_q;
    }

    public int func_153028_p()
    {
        return field_153062_t;
    }

    public boolean func_153032_e()
    {
        return field_153047_e == IngestServerTester.IngestTestState.Finished || field_153047_e == IngestServerTester.IngestTestState.Cancelled || field_153047_e == IngestServerTester.IngestTestState.Failed;
    }

    public float func_153030_h()
    {
        return field_153066_x;
    }

    public IngestServerTester(Stream p_i1019_1_, IngestList p_i1019_2_)
    {
        field_153045_c = p_i1019_1_;
        field_153046_d = p_i1019_2_;
    }

    public void func_176004_j()
    {
        if (field_153047_e == IngestServerTester.IngestTestState.Uninitalized)
        {
            field_153062_t = 0;
            field_153060_r = false;
            field_153061_s = false;
            field_176009_x = false;
            field_176008_y = false;
            field_176007_z = false;
            field_153058_p = field_153045_c.getStatCallbacks();
            field_153045_c.setStatCallbacks(field_176006_B);
            field_153057_o = field_153045_c.getStreamCallbacks();
            field_153045_c.setStreamCallbacks(field_176005_A);
            field_153052_j = new VideoParams();
            field_153052_j.targetFps = 60;
            field_153052_j.maxKbps = 3500;
            field_153052_j.outputWidth = 1280;
            field_153052_j.outputHeight = 720;
            field_153052_j.pixelFormat = PixelFormat.TTV_PF_BGRA;
            field_153052_j.encodingCpuUsage = EncodingCpuUsage.TTV_ECU_HIGH;
            field_153052_j.disableAdaptiveBitrate = true;
            field_153052_j.verticalFlip = false;
            field_153045_c.getDefaultParams(field_153052_j);
            audioParameters = new AudioParams();
            audioParameters.audioEnabled = false;
            audioParameters.enableMicCapture = false;
            audioParameters.enablePlaybackCapture = false;
            audioParameters.enablePassthroughAudio = false;
            field_153055_m = Lists.newArrayList();
            int i = 3;

            for (int j = 0; j < i; ++j)
            {
                FrameBuffer framebuffer = field_153045_c.allocateFrameBuffer(field_153052_j.outputWidth * field_153052_j.outputHeight * 4);

                if (!framebuffer.getIsValid())
                {
                    func_153031_o();
                    func_153034_a(IngestServerTester.IngestTestState.Failed);
                    return;
                }

                field_153055_m.add(framebuffer);
                field_153045_c.randomizeFrameBuffer(framebuffer);
            }

            func_153034_a(IngestServerTester.IngestTestState.Starting);
            field_153054_l = System.currentTimeMillis();
        }
    }

    @SuppressWarnings("incomplete-switch")
    public void func_153041_j()
    {
        if (!func_153032_e() && field_153047_e != IngestServerTester.IngestTestState.Uninitalized)
        {
            if (!field_176008_y && !field_176007_z)
            {
                switch (field_153047_e)
                {
                    case Starting:
                    case DoneTestingServer:
                        if (field_153059_q != null)
                        {
                            if (field_153061_s || !field_153056_n)
                            {
                                field_153059_q.bitrateKbps = 0.0F;
                            }

                            func_153035_b(field_153059_q);
                        }
                        else
                        {
                            field_153054_l = 0L;
                            field_153061_s = false;
                            field_153056_n = true;

                            if (field_153047_e != IngestServerTester.IngestTestState.Starting)
                            {
                                ++field_153062_t;
                            }

                            if (field_153062_t < field_153046_d.getServers().length)
                            {
                                field_153059_q = field_153046_d.getServers()[field_153062_t];
                                func_153036_a(field_153059_q);
                            }
                            else
                            {
                                func_153034_a(IngestServerTester.IngestTestState.Finished);
                            }
                        }

                        break;

                    case ConnectingToServer:
                    case TestingServer:
                        func_153029_c(field_153059_q);
                        break;

                    case Cancelling:
                        func_153034_a(IngestServerTester.IngestTestState.Cancelled);
                }

                func_153038_n();

                if (field_153047_e == IngestServerTester.IngestTestState.Cancelled || field_153047_e == IngestServerTester.IngestTestState.Finished)
                {
                    func_153031_o();
                }
            }
        }
    }

    public void func_153039_l()
    {
        if (!func_153032_e() && !field_153060_r)
        {
            field_153060_r = true;

            if (field_153059_q != null)
            {
                field_153059_q.bitrateKbps = 0.0F;
            }
        }
    }

    protected boolean func_153036_a(IngestServer p_153036_1_)
    {
        field_153056_n = true;
        field_153050_h = 0L;
        field_153051_i = RTMPState.Idle;
        field_153059_q = p_153036_1_;
        field_176008_y = true;
        func_153034_a(IngestServerTester.IngestTestState.ConnectingToServer);
        ErrorCode errorcode = field_153045_c.start(field_153052_j, audioParameters, p_153036_1_, StartFlags.TTV_Start_BandwidthTest, true);

        if (ErrorCode.failed(errorcode))
        {
            field_176008_y = false;
            field_153056_n = false;
            func_153034_a(IngestServerTester.IngestTestState.DoneTestingServer);
            return false;
        }
        else
        {
            field_153064_v = field_153050_h;
            p_153036_1_.bitrateKbps = 0.0F;
            field_153063_u = 0;
            return true;
        }
    }

    protected void func_153035_b(IngestServer p_153035_1_)
    {
        if (field_176008_y)
        {
            field_153061_s = true;
        }
        else if (field_176009_x)
        {
            field_176007_z = true;
            ErrorCode errorcode = field_153045_c.stop(true);

            if (ErrorCode.failed(errorcode))
            {
                field_176005_A.stopCallback(ErrorCode.TTV_EC_SUCCESS);
                System.out.println("Stop failed: " + errorcode);
            }

            field_153045_c.pollStats();
        }
        else
        {
            field_176005_A.stopCallback(ErrorCode.TTV_EC_SUCCESS);
        }
    }

    protected long func_153037_m()
    {
        return System.currentTimeMillis() - field_153054_l;
    }

    protected void func_153038_n()
    {
        float f = (float) func_153037_m();

        switch (field_153047_e)
        {
            case Starting:
            case ConnectingToServer:
            case Uninitalized:
            case Finished:
            case Cancelled:
            case Failed:
                field_153066_x = 0.0F;
                break;

            case DoneTestingServer:
                field_153066_x = 1.0F;
                break;

            case TestingServer:
            case Cancelling:
            default:
                field_153066_x = f / (float) field_153048_f;
        }

        switch (field_153047_e)
        {
            case Finished:
            case Cancelled:
            case Failed:
                field_153065_w = 1.0F;
                break;

            default:
                field_153065_w = (float) field_153062_t / (float) field_153046_d.getServers().length;
                field_153065_w += field_153066_x / (float) field_153046_d.getServers().length;
        }
    }

    protected boolean func_153029_c(IngestServer p_153029_1_)
    {
        if (!field_153061_s && !field_153060_r && func_153037_m() < field_153048_f)
        {
            if (!field_176008_y && !field_176007_z)
            {
                ErrorCode errorcode = field_153045_c.submitVideoFrame(field_153055_m.get(field_153063_u));

                if (ErrorCode.failed(errorcode))
                {
                    field_153056_n = false;
                    func_153034_a(IngestServerTester.IngestTestState.DoneTestingServer);
                    return false;
                }
                else
                {
                    field_153063_u = (field_153063_u + 1) % field_153055_m.size();
                    field_153045_c.pollStats();

                    if (field_153051_i == RTMPState.SendVideo)
                    {
                        func_153034_a(IngestServerTester.IngestTestState.TestingServer);
                        long i = func_153037_m();

                        if (i > 0L && field_153050_h > field_153064_v)
                        {
                            p_153029_1_.bitrateKbps = (float)(field_153050_h * 8L) / (float) func_153037_m();
                            field_153064_v = field_153050_h;
                        }
                    }

                    return true;
                }
            }
            else
            {
                return true;
            }
        }
        else
        {
            func_153034_a(IngestServerTester.IngestTestState.DoneTestingServer);
            return true;
        }
    }

    protected void func_153031_o()
    {
        field_153059_q = null;

        if (field_153055_m != null)
        {
            for (int i = 0; i < field_153055_m.size(); ++i)
            {
                field_153055_m.get(i).free();
            }

            field_153055_m = null;
        }

        if (field_153045_c.getStatCallbacks() == field_176006_B)
        {
            field_153045_c.setStatCallbacks(field_153058_p);
            field_153058_p = null;
        }

        if (field_153045_c.getStreamCallbacks() == field_176005_A)
        {
            field_153045_c.setStreamCallbacks(field_153057_o);
            field_153057_o = null;
        }
    }

    protected void func_153034_a(IngestServerTester.IngestTestState p_153034_1_)
    {
        if (p_153034_1_ != field_153047_e)
        {
            field_153047_e = p_153034_1_;

            if (field_153044_b != null)
            {
                field_153044_b.func_152907_a(this, p_153034_1_);
            }
        }
    }

    public interface IngestTestListener
    {
        void func_152907_a(IngestServerTester p_152907_1_, IngestServerTester.IngestTestState p_152907_2_);
    }

    public static enum IngestTestState
    {
        Uninitalized,
        Starting,
        ConnectingToServer,
        TestingServer,
        DoneTestingServer,
        Finished,
        Cancelling,
        Cancelled,
        Failed
    }
}
