package wtf.saturn.feature.impl.modules.hud;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import wtf.saturn.feature.cache.impl.hud.HUDModule;
import wtf.saturn.util.render.RenderUtil;

import java.util.LinkedHashMap;
import java.util.Map;

// this is some of the worst code ive written
public class Keystrokes extends HUDModule {
    private final Map<Integer, Key> keys = new LinkedHashMap<>();

    public Keystrokes() {
        super("Keystrokes", "The classic keystokes mod");
        setX(2.0);
        setY(26.0);

        KeyBinding[] binds = { mc.gameSettings.keyBindForward, mc.gameSettings.keyBindLeft,
                mc.gameSettings.keyBindBack, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump };
        for (KeyBinding binding : binds) {
            int keyCode = binding.getKeyCode();
            keys.put(keyCode, new Key(keyCode, false));
        }
    }

    @Override
    public void render(ScaledResolution res) {
        double dimensions = 35.0;
        double jumpHeight = (dimensions / 2) - 5.0;

        setWidth((dimensions * 3.0) + 4.0); // (width * 3) + padding
        setHeight((dimensions * 2.0) + 2.0 + jumpHeight); // height * 2 + padding + jump bar height

        int i = 0;
        double x = getX(), y = getY();
        for (Map.Entry<Integer, Key> entry : keys.entrySet()) {
            Key key = entry.getValue();
            key.state = Keyboard.isKeyDown(entry.getKey());
            keys.put(entry.getKey(), key);

            if (i == 0) {
                x = getX() + (getWidth() / 2.0) - (dimensions / 2.0);
            } else if (i == 4) {
                x = getX();
                y += dimensions + 2.0;
            }

            if (i != 4) {
                RenderUtil.drawRect(x, y, dimensions, dimensions, key.state ? 0x50000000 : 0x78000000);
                mc.fontRendererObj.drawString(key.display, (int) (x + (dimensions / 2.0) - (mc.fontRendererObj.getStringWidth(key.display) / 2.0)), (int) (y + findMiddle(dimensions)), -1);
            } else {
                RenderUtil.drawRect(x, y, getWidth(), jumpHeight, key.state ? 0x50000000 : 0x78000000);
                RenderUtil.drawLine(x + 10.0, y + (jumpHeight / 2.0), getWidth() - 20.0, 2.5, 0x35FFFFFF);
            }

            if (i == 0) {
                y += dimensions + 2.0;
                x = getX();
            } else {
                x += dimensions + 2.0;
            }

            ++i;
        }

    }

    private double findMiddle(double height) {
        return (height / 2.0) - (mc.fontRendererObj.FONT_HEIGHT / 2.0);
    }

    public static class Key {
        public boolean state = false;
        public final String display;

        public Key(int keyCode, boolean state) {
            display = Keyboard.getKeyName(keyCode);
        }
    }
}
