package net.minecraft.util;

public abstract class LazyLoadBase<T>
{
    private T value;
    private boolean isLoaded = false;

    public T getValue()
    {
        if (!isLoaded)
        {
            isLoaded = true;
            value = load();
        }

        return value;
    }

    protected abstract T load();
}
