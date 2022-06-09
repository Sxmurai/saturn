package net.minecraft.crash;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import net.minecraft.util.ReportedException;
import net.minecraft.world.gen.layer.IntCache;
import optifine.CrashReportCpu;
import optifine.CrashReporter;
import optifine.Reflector;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrashReport
{
    private static final Logger logger = LogManager.getLogger();

    /** Description of the crash report. */
    private final String description;

    /** The Throwable that is the "cause" for this crash and Crash Report. */
    private final Throwable cause;

    /** Category of crash */
    private final CrashReportCategory theReportCategory = new CrashReportCategory(this, "System Details");

    /** Holds the keys and values of all crash report sections. */
    private final List crashReportSections = Lists.newArrayList();

    /** File of crash report. */
    private File crashReportFile;
    private boolean field_85059_f = true;
    private StackTraceElement[] stacktrace = new StackTraceElement[0];
    private static final String __OBFID = "CL_00000990";
    private boolean reported = false;

    public CrashReport(String descriptionIn, Throwable causeThrowable)
    {
        description = descriptionIn;
        cause = causeThrowable;
        populateEnvironment();
    }

    /**
     * Populates this crash report with initial information about the running server and operating system / java
     * environment
     */
    private void populateEnvironment()
    {
        theReportCategory.addCrashSectionCallable("Minecraft Version", new Callable()
        {
            private static final String __OBFID = "CL_00001197";
            public String call()
            {
                return "1.8.8";
            }
        });
        theReportCategory.addCrashSectionCallable("Operating System", new Callable()
        {
            private static final String __OBFID = "CL_00001222";
            public String call()
            {
                return System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version");
            }
        });
        theReportCategory.addCrashSectionCallable("CPU", new CrashReportCpu());
        theReportCategory.addCrashSectionCallable("Java Version", new Callable<String>()
        {
            public String call()
            {
                return System.getProperty("java.version") + ", " + System.getProperty("java.vendor");
            }
        });
        theReportCategory.addCrashSectionCallable("Java VM Version", new Callable()
        {
            private static final String __OBFID = "CL_00001275";
            public String call()
            {
                return System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor");
            }
        });
        theReportCategory.addCrashSectionCallable("Memory", new Callable()
        {
            private static final String __OBFID = "CL_00001302";
            public String call()
            {
                Runtime runtime = Runtime.getRuntime();
                long i = runtime.maxMemory();
                long j = runtime.totalMemory();
                long k = runtime.freeMemory();
                long l = i / 1024L / 1024L;
                long i1 = j / 1024L / 1024L;
                long j1 = k / 1024L / 1024L;
                return k + " bytes (" + j1 + " MB) / " + j + " bytes (" + i1 + " MB) up to " + i + " bytes (" + l + " MB)";
            }
        });
        theReportCategory.addCrashSectionCallable("JVM Flags", new Callable()
        {
            private static final String __OBFID = "CL_00001329";
            public String call()
            {
                RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();
                List list = runtimemxbean.getInputArguments();
                int i = 0;
                StringBuilder stringbuilder = new StringBuilder();

                for (Object s : list)
                {
                    if (((String) s).startsWith("-X"))
                    {
                        if (i++ > 0)
                        {
                            stringbuilder.append(" ");
                        }

                        stringbuilder.append(s);
                    }
                }

                return String.format("%d total; %s", Integer.valueOf(i), stringbuilder.toString());
            }
        });
        theReportCategory.addCrashSectionCallable("IntCache", new Callable()
        {
            private static final String __OBFID = "CL_00001355";
            public String call() throws Exception
            {
                return IntCache.getCacheSizes();
            }
        });

        if (Reflector.FMLCommonHandler_enhanceCrashReport.exists())
        {
            Object object = Reflector.call(Reflector.FMLCommonHandler_instance);
            Reflector.callString(object, Reflector.FMLCommonHandler_enhanceCrashReport, this, theReportCategory);
        }
    }

    /**
     * Returns the description of the Crash Report.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the Throwable object that is the cause for the crash and Crash Report.
     */
    public Throwable getCrashCause()
    {
        return cause;
    }

    /**
     * Gets the various sections of the crash report into the given StringBuilder
     */
    public void getSectionsInStringBuilder(StringBuilder builder)
    {
        if ((stacktrace == null || stacktrace.length <= 0) && crashReportSections.size() > 0)
        {
            stacktrace = ArrayUtils.subarray(((CrashReportCategory) crashReportSections.get(0)).getStackTrace(), 0, 1);
        }

        if (stacktrace != null && stacktrace.length > 0)
        {
            builder.append("-- Head --\n");
            builder.append("Stacktrace:\n");

            for (StackTraceElement stacktraceelement : stacktrace)
            {
                builder.append("\t").append("at ").append(stacktraceelement.toString());
                builder.append("\n");
            }

            builder.append("\n");
        }

        for (Object crashreportcategory : crashReportSections)
        {
            ((CrashReportCategory) crashreportcategory).appendToStringBuilder(builder);
            builder.append("\n\n");
        }

        theReportCategory.appendToStringBuilder(builder);
    }

    /**
     * Gets the stack trace of the Throwable that caused this crash report, or if that fails, the cause .toString().
     */
    public String getCauseStackTraceOrString()
    {
        StringWriter stringwriter = null;
        PrintWriter printwriter = null;
        Object object = cause;

        if (((Throwable)object).getMessage() == null)
        {
            if (object instanceof NullPointerException)
            {
                object = new NullPointerException(description);
            }
            else if (object instanceof StackOverflowError)
            {
                object = new StackOverflowError(description);
            }
            else if (object instanceof OutOfMemoryError)
            {
                object = new OutOfMemoryError(description);
            }

            ((Throwable)object).setStackTrace(cause.getStackTrace());
        }

        String s = object.toString();

        try
        {
            stringwriter = new StringWriter();
            printwriter = new PrintWriter(stringwriter);
            ((Throwable)object).printStackTrace(printwriter);
            s = stringwriter.toString();
        }
        finally
        {
            IOUtils.closeQuietly(stringwriter);
            IOUtils.closeQuietly(printwriter);
        }

        return s;
    }

    /**
     * Gets the complete report with headers, stack trace, and different sections as a string.
     */
    public String getCompleteReport()
    {
        if (!reported)
        {
            reported = true;
            CrashReporter.onCrashReport(this, theReportCategory);
        }

        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("---- Minecraft Crash Report ----\n");
        Reflector.call(Reflector.BlamingTransformer_onCrash, stringbuilder);
        Reflector.call(Reflector.CoreModManager_onCrash, stringbuilder);
        stringbuilder.append("// ");
        stringbuilder.append(CrashReport.getWittyComment());
        stringbuilder.append("\n\n");
        stringbuilder.append("Time: ");
        stringbuilder.append((new SimpleDateFormat()).format(new Date()));
        stringbuilder.append("\n");
        stringbuilder.append("Description: ");
        stringbuilder.append(description);
        stringbuilder.append("\n\n");
        stringbuilder.append(getCauseStackTraceOrString());
        stringbuilder.append("\n\nA detailed walkthrough of the error, its code path and all known details is as follows:\n");

        for (int i = 0; i < 87; ++i)
        {
            stringbuilder.append("-");
        }

        stringbuilder.append("\n\n");
        getSectionsInStringBuilder(stringbuilder);
        return stringbuilder.toString();
    }

    /**
     * Gets the file this crash report is saved into.
     */
    public File getFile()
    {
        return crashReportFile;
    }

    /**
     * Saves this CrashReport to the given file and returns a value indicating whether we were successful at doing so.
     */
    public boolean saveToFile(File toFile)
    {
        if (crashReportFile != null)
        {
            return false;
        }
        else
        {
            if (toFile.getParentFile() != null)
            {
                toFile.getParentFile().mkdirs();
            }

            try
            {
                FileWriter filewriter = new FileWriter(toFile);
                filewriter.write(getCompleteReport());
                filewriter.close();
                crashReportFile = toFile;
                return true;
            }
            catch (Throwable throwable)
            {
                CrashReport.logger.error("Could not save crash report to " + toFile, throwable);
                return false;
            }
        }
    }

    public CrashReportCategory getCategory()
    {
        return theReportCategory;
    }

    /**
     * Creates a CrashReportCategory
     */
    public CrashReportCategory makeCategory(String name)
    {
        return makeCategoryDepth(name, 1);
    }

    /**
     * Creates a CrashReportCategory for the given stack trace depth
     */
    public CrashReportCategory makeCategoryDepth(String categoryName, int stacktraceLength)
    {
        CrashReportCategory crashreportcategory = new CrashReportCategory(this, categoryName);

        if (field_85059_f)
        {
            int i = crashreportcategory.getPrunedStackTrace(stacktraceLength);
            StackTraceElement[] astacktraceelement = cause.getStackTrace();
            StackTraceElement stacktraceelement = null;
            StackTraceElement stacktraceelement1 = null;
            int j = astacktraceelement.length - i;

            if (j < 0)
            {
                System.out.println("Negative index in crash report handler (" + astacktraceelement.length + "/" + i + ")");
            }

            if (astacktraceelement != null && 0 <= j && j < astacktraceelement.length)
            {
                stacktraceelement = astacktraceelement[j];

                if (astacktraceelement.length + 1 - i < astacktraceelement.length)
                {
                    stacktraceelement1 = astacktraceelement[astacktraceelement.length + 1 - i];
                }
            }

            field_85059_f = crashreportcategory.firstTwoElementsOfStackTraceMatch(stacktraceelement, stacktraceelement1);

            if (i > 0 && !crashReportSections.isEmpty())
            {
                CrashReportCategory crashreportcategory1 = (CrashReportCategory) crashReportSections.get(crashReportSections.size() - 1);
                crashreportcategory1.trimStackTraceEntriesFromBottom(i);
            }
            else if (astacktraceelement != null && astacktraceelement.length >= i && 0 <= j && j < astacktraceelement.length)
            {
                stacktrace = new StackTraceElement[j];
                System.arraycopy(astacktraceelement, 0, stacktrace, 0, stacktrace.length);
            }
            else
            {
                field_85059_f = false;
            }
        }

        crashReportSections.add(crashreportcategory);
        return crashreportcategory;
    }

    /**
     * Gets a random witty comment for inclusion in this CrashReport
     */
    private static String getWittyComment()
    {
        String[] astring = new String[] {"Who set us up the TNT?", "Everything's going to plan. No, really, that was supposed to happen.", "Uh... Did I do that?", "Oops.", "Why did you do that?", "I feel sad now :(", "My bad.", "I'm sorry, Dave.", "I let you down. Sorry :(", "On the bright side, I bought you a teddy bear!", "Daisy, daisy...", "Oh - I know what I did wrong!", "Hey, that tickles! Hehehe!", "I blame Dinnerbone.", "You should try our sister game, Minceraft!", "Don't be sad. I'll do better next time, I promise!", "Don't be sad, have a hug! <3", "I just don't know what went wrong :(", "Shall we play a game?", "Quite honestly, I wouldn't worry myself about that.", "I bet Cylons wouldn't have this problem.", "Sorry :(", "Surprise! Haha. Well, this is awkward.", "Would you like a cupcake?", "Hi. I'm Minecraft, and I'm a crashaholic.", "Ooh. Shiny.", "This doesn't make any sense!", "Why is it breaking :(", "Don't do that.", "Ouch. That hurt :(", "You're mean.", "This is a token for 1 free hug. Redeem at your nearest Mojangsta: [~~HUG~~]", "There are four lights!", "But it works on my machine."};

        try
        {
            return astring[(int)(System.nanoTime() % (long)astring.length)];
        }
        catch (Throwable var2)
        {
            return "Witty comment unavailable :(";
        }
    }

    /**
     * Creates a crash report for the exception
     */
    public static CrashReport makeCrashReport(Throwable causeIn, String descriptionIn)
    {
        CrashReport crashreport;

        if (causeIn instanceof ReportedException)
        {
            crashreport = ((ReportedException)causeIn).getCrashReport();
        }
        else
        {
            crashreport = new CrashReport(descriptionIn, causeIn);
        }

        return crashreport;
    }
}
