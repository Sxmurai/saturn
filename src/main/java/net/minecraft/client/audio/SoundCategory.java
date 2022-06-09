package net.minecraft.client.audio;

import com.google.common.collect.Maps;
import java.util.Map;

public enum SoundCategory
{
    MASTER("master", 0),
    MUSIC("music", 1),
    RECORDS("record", 2),
    WEATHER("weather", 3),
    BLOCKS("block", 4),
    MOBS("hostile", 5),
    ANIMALS("neutral", 6),
    PLAYERS("player", 7),
    AMBIENT("ambient", 8);

    private static final Map<String, SoundCategory> NAME_CATEGORY_MAP = Maps.newHashMap();
    private static final Map<Integer, SoundCategory> ID_CATEGORY_MAP = Maps.newHashMap();
    private final String categoryName;
    private final int categoryId;

    private SoundCategory(String name, int id)
    {
        categoryName = name;
        categoryId = id;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public int getCategoryId()
    {
        return categoryId;
    }

    public static SoundCategory getCategory(String name)
    {
        return SoundCategory.NAME_CATEGORY_MAP.get(name);
    }

    static {
        for (SoundCategory soundcategory : SoundCategory.values())
        {
            if (SoundCategory.NAME_CATEGORY_MAP.containsKey(soundcategory.getCategoryName()) || SoundCategory.ID_CATEGORY_MAP.containsKey(Integer.valueOf(soundcategory.getCategoryId())))
            {
                throw new Error("Clash in Sound Category ID & Name pools! Cannot insert " + soundcategory);
            }

            SoundCategory.NAME_CATEGORY_MAP.put(soundcategory.getCategoryName(), soundcategory);
            SoundCategory.ID_CATEGORY_MAP.put(Integer.valueOf(soundcategory.getCategoryId()), soundcategory);
        }
    }
}
