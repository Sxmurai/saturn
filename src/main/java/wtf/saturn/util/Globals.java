package wtf.saturn.util;

import net.minecraft.client.Minecraft;

/**
 * A globals class to be implemented into any class that need core client functions
 *
 * @author aesthetical
 * @since 6/7/22
 */
public interface Globals {
    Minecraft mc = Minecraft.getMinecraft();

    default boolean nullCheck() {
        return mc.thePlayer == null || mc.theWorld == null;
    }
}
