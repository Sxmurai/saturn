package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.client.resources.data.LanguageMetadataSection;
import net.minecraft.util.StringTranslate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LanguageManager implements IResourceManagerReloadListener
{
    private static final Logger logger = LogManager.getLogger();
    private final IMetadataSerializer theMetadataSerializer;
    private String currentLanguage;
    protected static final Locale currentLocale = new Locale();
    private final Map<String, Language> languageMap = Maps.newHashMap();

    public LanguageManager(IMetadataSerializer theMetadataSerializerIn, String currentLanguageIn)
    {
        theMetadataSerializer = theMetadataSerializerIn;
        currentLanguage = currentLanguageIn;
        I18n.setLocale(LanguageManager.currentLocale);
    }

    public void parseLanguageMetadata(List<IResourcePack> p_135043_1_)
    {
        languageMap.clear();

        for (IResourcePack iresourcepack : p_135043_1_)
        {
            try
            {
                LanguageMetadataSection languagemetadatasection = iresourcepack.getPackMetadata(theMetadataSerializer, "language");

                if (languagemetadatasection != null)
                {
                    for (Language language : languagemetadatasection.getLanguages())
                    {
                        if (!languageMap.containsKey(language.getLanguageCode()))
                        {
                            languageMap.put(language.getLanguageCode(), language);
                        }
                    }
                }
            }
            catch (RuntimeException runtimeexception)
            {
                LanguageManager.logger.warn("Unable to parse metadata section of resourcepack: " + iresourcepack.getPackName(), runtimeexception);
            }
            catch (IOException ioexception)
            {
                LanguageManager.logger.warn("Unable to parse metadata section of resourcepack: " + iresourcepack.getPackName(), ioexception);
            }
        }
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        List<String> list = Lists.newArrayList("en_US");

        if (!"en_US".equals(currentLanguage))
        {
            list.add(currentLanguage);
        }

        LanguageManager.currentLocale.loadLocaleDataFiles(resourceManager, list);
        StringTranslate.replaceWith(LanguageManager.currentLocale.properties);
    }

    public boolean isCurrentLocaleUnicode()
    {
        return LanguageManager.currentLocale.isUnicode();
    }

    public boolean isCurrentLanguageBidirectional()
    {
        return getCurrentLanguage() != null && getCurrentLanguage().isBidirectional();
    }

    public void setCurrentLanguage(Language currentLanguageIn)
    {
        currentLanguage = currentLanguageIn.getLanguageCode();
    }

    public Language getCurrentLanguage()
    {
        return languageMap.containsKey(currentLanguage) ? languageMap.get(currentLanguage) : languageMap.get("en_US");
    }

    public SortedSet<Language> getLanguages()
    {
        return Sets.newTreeSet(languageMap.values());
    }
}
