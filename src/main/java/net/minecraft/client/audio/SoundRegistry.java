package net.minecraft.client.audio;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.util.RegistrySimple;
import net.minecraft.util.ResourceLocation;

public class SoundRegistry extends RegistrySimple<ResourceLocation, SoundEventAccessorComposite>
{
    private Map<ResourceLocation, SoundEventAccessorComposite> soundRegistry;

    protected Map<ResourceLocation, SoundEventAccessorComposite> createUnderlyingMap()
    {
        soundRegistry = Maps.newHashMap();
        return soundRegistry;
    }

    public void registerSound(SoundEventAccessorComposite p_148762_1_)
    {
        putObject(p_148762_1_.getSoundEventLocation(), p_148762_1_);
    }

    /**
     * Reset the underlying sound map (Called on resource manager reload)
     */
    public void clearMap()
    {
        soundRegistry.clear();
    }
}
