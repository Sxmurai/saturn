package net.minecraft.client.resources;

public class Language implements Comparable<Language>
{
    private final String languageCode;
    private final String region;
    private final String name;
    private final boolean bidirectional;

    public Language(String languageCodeIn, String regionIn, String nameIn, boolean bidirectionalIn)
    {
        languageCode = languageCodeIn;
        region = regionIn;
        name = nameIn;
        bidirectional = bidirectionalIn;
    }

    public String getLanguageCode()
    {
        return languageCode;
    }

    public boolean isBidirectional()
    {
        return bidirectional;
    }

    public String toString()
    {
        return String.format("%s (%s)", name, region);
    }

    public boolean equals(Object p_equals_1_)
    {
        return this == p_equals_1_ || (p_equals_1_ instanceof Language && languageCode.equals(((Language) p_equals_1_).languageCode));
    }

    public int hashCode()
    {
        return languageCode.hashCode();
    }

    public int compareTo(Language p_compareTo_1_)
    {
        return languageCode.compareTo(p_compareTo_1_.languageCode);
    }
}
