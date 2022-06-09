package net.minecraft.util;

import java.io.OutputStream;
import java.io.PrintStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggingPrintStream extends PrintStream
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final String domain;

    public LoggingPrintStream(String domainIn, OutputStream outStream)
    {
        super(outStream);
        domain = domainIn;
    }

    public void println(String p_println_1_)
    {
        logString(p_println_1_);
    }

    public void println(Object p_println_1_)
    {
        logString(String.valueOf(p_println_1_));
    }

    private void logString(String string)
    {
        StackTraceElement[] astacktraceelement = Thread.currentThread().getStackTrace();
        StackTraceElement stacktraceelement = astacktraceelement[Math.min(3, astacktraceelement.length)];
        LoggingPrintStream.LOGGER.info("[{}]@.({}:{}): {}", new Object[] {domain, stacktraceelement.getFileName(), Integer.valueOf(stacktraceelement.getLineNumber()), string});
    }
}
