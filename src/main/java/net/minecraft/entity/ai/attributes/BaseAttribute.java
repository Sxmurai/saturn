package net.minecraft.entity.ai.attributes;

public abstract class BaseAttribute implements IAttribute
{
    private final IAttribute field_180373_a;
    private final String unlocalizedName;
    private final double defaultValue;
    private boolean shouldWatch;

    protected BaseAttribute(IAttribute p_i45892_1_, String unlocalizedNameIn, double defaultValueIn)
    {
        field_180373_a = p_i45892_1_;
        unlocalizedName = unlocalizedNameIn;
        defaultValue = defaultValueIn;

        if (unlocalizedNameIn == null)
        {
            throw new IllegalArgumentException("Name cannot be null!");
        }
    }

    public String getAttributeUnlocalizedName()
    {
        return unlocalizedName;
    }

    public double getDefaultValue()
    {
        return defaultValue;
    }

    public boolean getShouldWatch()
    {
        return shouldWatch;
    }

    public BaseAttribute setShouldWatch(boolean shouldWatchIn)
    {
        shouldWatch = shouldWatchIn;
        return this;
    }

    public IAttribute func_180372_d()
    {
        return field_180373_a;
    }

    public int hashCode()
    {
        return unlocalizedName.hashCode();
    }

    public boolean equals(Object p_equals_1_)
    {
        return p_equals_1_ instanceof IAttribute && unlocalizedName.equals(((IAttribute)p_equals_1_).getAttributeUnlocalizedName());
    }
}
