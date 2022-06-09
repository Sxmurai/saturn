package net.minecraft.profiler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.GlStateManager;
import optifine.Config;
import optifine.Lagometer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Profiler
{
    private static final Logger logger = LogManager.getLogger();

    /** List of parent sections */
    private final List sectionList = Lists.newArrayList();

    /** List of timestamps (System.nanoTime) */
    private final List timestampList = Lists.newArrayList();

    /** Flag profiling enabled */
    public boolean profilingEnabled;

    /** Current profiling section */
    private String profilingSection = "";

    /** Profiling map */
    private final Map profilingMap = Maps.newHashMap();
    private static final String __OBFID = "CL_00001497";
    public boolean profilerGlobalEnabled = true;
    private boolean profilerLocalEnabled;
    private static final String SCHEDULED_EXECUTABLES = "scheduledExecutables";
    private static final String TICK = "tick";
    private static final String PRE_RENDER_ERRORS = "preRenderErrors";
    private static final String RENDER = "render";
    private static final String DISPLAY = "display";
    private static final int HASH_SCHEDULED_EXECUTABLES = "scheduledExecutables".hashCode();
    private static final int HASH_TICK = "tick".hashCode();
    private static final int HASH_PRE_RENDER_ERRORS = "preRenderErrors".hashCode();
    private static final int HASH_RENDER = "render".hashCode();
    private static final int HASH_DISPLAY = "display".hashCode();

    public Profiler()
    {
        profilerLocalEnabled = profilerGlobalEnabled;
    }

    /**
     * Clear profiling.
     */
    public void clearProfiling()
    {
        profilingMap.clear();
        profilingSection = "";
        sectionList.clear();
        profilerLocalEnabled = profilerGlobalEnabled;
    }

    /**
     * Start section
     */
    public void startSection(String name)
    {
        if (Lagometer.isActive())
        {
            int i = name.hashCode();

            if (i == Profiler.HASH_SCHEDULED_EXECUTABLES && name.equals("scheduledExecutables"))
            {
                Lagometer.timerScheduledExecutables.start();
            }
            else if (i == Profiler.HASH_TICK && name.equals("tick") && Config.isMinecraftThread())
            {
                Lagometer.timerScheduledExecutables.end();
                Lagometer.timerTick.start();
            }
            else if (i == Profiler.HASH_PRE_RENDER_ERRORS && name.equals("preRenderErrors"))
            {
                Lagometer.timerTick.end();
            }
        }

        if (Config.isFastRender())
        {
            int j = name.hashCode();

            if (j == Profiler.HASH_RENDER && name.equals("render"))
            {
                GlStateManager.clearEnabled = false;
            }
            else if (j == Profiler.HASH_DISPLAY && name.equals("display"))
            {
                GlStateManager.clearEnabled = true;
            }
        }

        if (profilerLocalEnabled)
        {
            if (profilingEnabled)
            {
                if (profilingSection.length() > 0)
                {
                    profilingSection = profilingSection + ".";
                }

                profilingSection = profilingSection + name;
                sectionList.add(profilingSection);
                timestampList.add(Long.valueOf(System.nanoTime()));
            }
        }
    }

    /**
     * End section
     */
    public void endSection()
    {
        if (profilerLocalEnabled)
        {
            if (profilingEnabled)
            {
                long i = System.nanoTime();
                long j = ((Long) timestampList.remove(timestampList.size() - 1)).longValue();
                sectionList.remove(sectionList.size() - 1);
                long k = i - j;

                if (profilingMap.containsKey(profilingSection))
                {
                    profilingMap.put(profilingSection, Long.valueOf(((Long) profilingMap.get(profilingSection)).longValue() + k));
                }
                else
                {
                    profilingMap.put(profilingSection, Long.valueOf(k));
                }

                if (k > 100000000L)
                {
                    Profiler.logger.warn("Something's taking too long! '" + profilingSection + "' took aprox " + (double)k / 1000000.0D + " ms");
                }

                profilingSection = !sectionList.isEmpty() ? (String) sectionList.get(sectionList.size() - 1) : "";
            }
        }
    }

    /**
     * Get profiling data
     */
    public List getProfilingData(String p_76321_1_)
    {
        profilerLocalEnabled = profilerGlobalEnabled;

        if (!profilerLocalEnabled)
        {
            return new ArrayList(Arrays.asList(new Result("root", 0.0D, 0.0D)));
        }
        else if (!profilingEnabled)
        {
            return null;
        }
        else
        {
            long i = profilingMap.containsKey("root") ? ((Long) profilingMap.get("root")).longValue() : 0L;
            long j = profilingMap.containsKey(p_76321_1_) ? ((Long) profilingMap.get(p_76321_1_)).longValue() : -1L;
            ArrayList arraylist = Lists.newArrayList();

            if (p_76321_1_.length() > 0)
            {
                p_76321_1_ = p_76321_1_ + ".";
            }

            long k = 0L;

            for (Object s : profilingMap.keySet())
            {
                if (((String) s).length() > p_76321_1_.length() && ((String) s).startsWith(p_76321_1_) && ((String) s).indexOf(".", p_76321_1_.length() + 1) < 0)
                {
                    k += ((Long) profilingMap.get(s)).longValue();
                }
            }

            float f = (float)k;

            if (k < j)
            {
                k = j;
            }

            if (i < k)
            {
                i = k;
            }

            for (Object s10 : profilingMap.keySet())
            {
                String s1 = (String)s10;

                if (s1.length() > p_76321_1_.length() && s1.startsWith(p_76321_1_) && s1.indexOf(".", p_76321_1_.length() + 1) < 0)
                {
                    long l = ((Long) profilingMap.get(s1)).longValue();
                    double d0 = (double)l * 100.0D / (double)k;
                    double d1 = (double)l * 100.0D / (double)i;
                    String s2 = s1.substring(p_76321_1_.length());
                    arraylist.add(new Profiler.Result(s2, d0, d1));
                }
            }

            for (Object s3 : profilingMap.keySet())
            {
                profilingMap.put(s3, Long.valueOf(((Long) profilingMap.get(s3)).longValue() * 950L / 1000L));
            }

            if ((float)k > f)
            {
                arraylist.add(new Profiler.Result("unspecified", (double)((float)k - f) * 100.0D / (double)k, (double)((float)k - f) * 100.0D / (double)i));
            }

            Collections.sort(arraylist);
            arraylist.add(0, new Profiler.Result(p_76321_1_, 100.0D, (double)k * 100.0D / (double)i));
            return arraylist;
        }
    }

    /**
     * End current section and start a new section
     */
    public void endStartSection(String name)
    {
        if (profilerLocalEnabled)
        {
            endSection();
            startSection(name);
        }
    }

    public String getNameOfLastSection()
    {
        return sectionList.size() == 0 ? "[UNKNOWN]" : (String) sectionList.get(sectionList.size() - 1);
    }

    public static final class Result implements Comparable
    {
        public double field_76332_a;
        public double field_76330_b;
        public String field_76331_c;
        private static final String __OBFID = "CL_00001498";

        public Result(String p_i1554_1_, double p_i1554_2_, double p_i1554_4_)
        {
            field_76331_c = p_i1554_1_;
            field_76332_a = p_i1554_2_;
            field_76330_b = p_i1554_4_;
        }

        public int compareTo(Profiler.Result p_compareTo_1_)
        {
            return p_compareTo_1_.field_76332_a < field_76332_a ? -1 : (p_compareTo_1_.field_76332_a > field_76332_a ? 1 : p_compareTo_1_.field_76331_c.compareTo(field_76331_c));
        }

        public int func_76329_a()
        {
            return (field_76331_c.hashCode() & 11184810) + 4473924;
        }

        public int compareTo(Object p_compareTo_1_)
        {
            return compareTo((Profiler.Result)p_compareTo_1_);
        }
    }
}
