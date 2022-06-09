package net.minecraft.client.resources;

import java.util.Map;

public class I18n
{
    private static Locale i18nLocale;
    private static final String __OBFID = "CL_00001094";

    static void setLocale(Locale i18nLocaleIn)
    {
        I18n.i18nLocale = i18nLocaleIn;
    }

    /**
     * format(a, b) is equivalent to String.format(translate(a), b). Args: translationKey, params...
     */
    public static String format(String translateKey, Object... parameters)
    {
        return I18n.i18nLocale.formatMessage(translateKey, parameters);
    }

    public static Map getLocaleProperties()
    {
        return I18n.i18nLocale.properties;
    }
}
