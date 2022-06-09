package net.minecraft.util;

import net.minecraft.crash.CrashReport;

public class ReportedException extends RuntimeException
{
    /** Instance of CrashReport. */
    private final CrashReport theReportedExceptionCrashReport;

    public ReportedException(CrashReport report)
    {
        theReportedExceptionCrashReport = report;
    }

    /**
     * Gets the CrashReport wrapped by this exception.
     */
    public CrashReport getCrashReport()
    {
        return theReportedExceptionCrashReport;
    }

    public Throwable getCause()
    {
        return theReportedExceptionCrashReport.getCrashCause();
    }

    public String getMessage()
    {
        return theReportedExceptionCrashReport.getDescription();
    }
}
