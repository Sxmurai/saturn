package net.minecraft.client.settings;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IntHashMap;

public class KeyBinding implements Comparable<KeyBinding>
{
    private static final List<KeyBinding> keybindArray = Lists.newArrayList();
    private static final IntHashMap<KeyBinding> hash = new IntHashMap();
    private static final Set<String> keybindSet = Sets.newHashSet();
    private final String keyDescription;
    private final int keyCodeDefault;
    private final String keyCategory;
    private int keyCode;

    /** Is the key held down? */
    private boolean pressed;
    private int pressTime;

    public static void onTick(int keyCode)
    {
        if (keyCode != 0)
        {
            KeyBinding keybinding = KeyBinding.hash.lookup(keyCode);

            if (keybinding != null)
            {
                ++keybinding.pressTime;
            }
        }
    }

    public static void setKeyBindState(int keyCode, boolean pressed)
    {
        if (keyCode != 0)
        {
            KeyBinding keybinding = KeyBinding.hash.lookup(keyCode);

            if (keybinding != null)
            {
                keybinding.pressed = pressed;
            }
        }
    }

    public static void unPressAllKeys()
    {
        for (KeyBinding keybinding : KeyBinding.keybindArray)
        {
            keybinding.unpressKey();
        }
    }

    public static void resetKeyBindingArrayAndHash()
    {
        KeyBinding.hash.clearMap();

        for (KeyBinding keybinding : KeyBinding.keybindArray)
        {
            KeyBinding.hash.addKey(keybinding.keyCode, keybinding);
        }
    }

    public static Set<String> getKeybinds()
    {
        return KeyBinding.keybindSet;
    }

    public KeyBinding(String description, int keyCode, String category)
    {
        keyDescription = description;
        this.keyCode = keyCode;
        keyCodeDefault = keyCode;
        keyCategory = category;
        KeyBinding.keybindArray.add(this);
        KeyBinding.hash.addKey(keyCode, this);
        KeyBinding.keybindSet.add(category);
    }

    /**
     * Returns true if the key is pressed (used for continuous querying). Should be used in tickers.
     */
    public boolean isKeyDown()
    {
        return pressed;
    }

    public String getKeyCategory()
    {
        return keyCategory;
    }

    /**
     * Returns true on the initial key press. For continuous querying use {@link isKeyDown()}. Should be used in key
     * events.
     */
    public boolean isPressed()
    {
        if (pressTime == 0)
        {
            return false;
        }
        else
        {
            --pressTime;
            return true;
        }
    }

    private void unpressKey()
    {
        pressTime = 0;
        pressed = false;
    }

    public String getKeyDescription()
    {
        return keyDescription;
    }

    public int getKeyCodeDefault()
    {
        return keyCodeDefault;
    }

    public int getKeyCode()
    {
        return keyCode;
    }

    public void setKeyCode(int keyCode)
    {
        this.keyCode = keyCode;
    }

    public int compareTo(KeyBinding p_compareTo_1_)
    {
        int i = I18n.format(keyCategory).compareTo(I18n.format(p_compareTo_1_.keyCategory));

        if (i == 0)
        {
            i = I18n.format(keyDescription).compareTo(I18n.format(p_compareTo_1_.keyDescription));
        }

        return i;
    }
}
