package wtf.saturn.util.sound;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import wtf.saturn.util.Globals;

/**
 * Plays sounds throughout the client
 *
 * @author aesthetical
 * @since 6/9/22
 */
public class SoundUtil implements Globals {
    public static void playSound(String name, float pitch) {
        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation(name), pitch));
    }

    public static void playButtonClick() {
        playSound("minecraft:gui.button.press", 1.0f);
    }
}
