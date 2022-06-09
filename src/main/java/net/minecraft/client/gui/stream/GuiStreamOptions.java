package net.minecraft.client.gui.stream;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiOptionSlider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.EnumChatFormatting;

public class GuiStreamOptions extends GuiScreen
{
    private static final GameSettings.Options[] field_152312_a = new GameSettings.Options[] {GameSettings.Options.STREAM_BYTES_PER_PIXEL, GameSettings.Options.STREAM_FPS, GameSettings.Options.STREAM_KBPS, GameSettings.Options.STREAM_SEND_METADATA, GameSettings.Options.STREAM_VOLUME_MIC, GameSettings.Options.STREAM_VOLUME_SYSTEM, GameSettings.Options.STREAM_MIC_TOGGLE_BEHAVIOR, GameSettings.Options.STREAM_COMPRESSION};
    private static final GameSettings.Options[] field_152316_f = new GameSettings.Options[] {GameSettings.Options.STREAM_CHAT_ENABLED, GameSettings.Options.STREAM_CHAT_USER_FILTER};
    private final GuiScreen parentScreen;
    private final GameSettings field_152318_h;
    private String field_152319_i;
    private String field_152313_r;
    private int field_152314_s;
    private boolean field_152315_t = false;

    public GuiStreamOptions(GuiScreen parentScreenIn, GameSettings p_i1073_2_)
    {
        parentScreen = parentScreenIn;
        field_152318_h = p_i1073_2_;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        int i = 0;
        field_152319_i = I18n.format("options.stream.title");
        field_152313_r = I18n.format("options.stream.chat.title");

        for (GameSettings.Options gamesettings$options : GuiStreamOptions.field_152312_a)
        {
            if (gamesettings$options.getEnumFloat())
            {
                buttonList.add(new GuiOptionSlider(gamesettings$options.returnEnumOrdinal(), width / 2 - 155 + i % 2 * 160, height / 6 + 24 * (i >> 1), gamesettings$options));
            }
            else
            {
                buttonList.add(new GuiOptionButton(gamesettings$options.returnEnumOrdinal(), width / 2 - 155 + i % 2 * 160, height / 6 + 24 * (i >> 1), gamesettings$options, field_152318_h.getKeyBinding(gamesettings$options)));
            }

            ++i;
        }

        if (i % 2 == 1)
        {
            ++i;
        }

        field_152314_s = height / 6 + 24 * (i >> 1) + 6;
        i = i + 2;

        for (GameSettings.Options gamesettings$options1 : GuiStreamOptions.field_152316_f)
        {
            if (gamesettings$options1.getEnumFloat())
            {
                buttonList.add(new GuiOptionSlider(gamesettings$options1.returnEnumOrdinal(), width / 2 - 155 + i % 2 * 160, height / 6 + 24 * (i >> 1), gamesettings$options1));
            }
            else
            {
                buttonList.add(new GuiOptionButton(gamesettings$options1.returnEnumOrdinal(), width / 2 - 155 + i % 2 * 160, height / 6 + 24 * (i >> 1), gamesettings$options1, field_152318_h.getKeyBinding(gamesettings$options1)));
            }

            ++i;
        }

        buttonList.add(new GuiButton(200, width / 2 - 155, height / 6 + 168, 150, 20, I18n.format("gui.done")));
        GuiButton guibutton = new GuiButton(201, width / 2 + 5, height / 6 + 168, 150, 20, I18n.format("options.stream.ingestSelection"));
        guibutton.enabled = mc.getTwitchStream().isReadyToBroadcast() && mc.getTwitchStream().func_152925_v().length > 0 || mc.getTwitchStream().func_152908_z();
        buttonList.add(guibutton);
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id < 100 && button instanceof GuiOptionButton)
            {
                GameSettings.Options gamesettings$options = ((GuiOptionButton)button).returnEnumOptions();
                field_152318_h.setOptionValue(gamesettings$options, 1);
                button.displayString = field_152318_h.getKeyBinding(GameSettings.Options.getEnumOptions(button.id));

                if (mc.getTwitchStream().isBroadcasting() && gamesettings$options != GameSettings.Options.STREAM_CHAT_ENABLED && gamesettings$options != GameSettings.Options.STREAM_CHAT_USER_FILTER)
                {
                    field_152315_t = true;
                }
            }
            else if (button instanceof GuiOptionSlider)
            {
                if (button.id == GameSettings.Options.STREAM_VOLUME_MIC.returnEnumOrdinal())
                {
                    mc.getTwitchStream().updateStreamVolume();
                }
                else if (button.id == GameSettings.Options.STREAM_VOLUME_SYSTEM.returnEnumOrdinal())
                {
                    mc.getTwitchStream().updateStreamVolume();
                }
                else if (mc.getTwitchStream().isBroadcasting())
                {
                    field_152315_t = true;
                }
            }

            if (button.id == 200)
            {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(parentScreen);
            }
            else if (button.id == 201)
            {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(new GuiIngestServers(this));
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, field_152319_i, width / 2, 20, 16777215);
        drawCenteredString(fontRendererObj, field_152313_r, width / 2, field_152314_s, 16777215);

        if (field_152315_t)
        {
            drawCenteredString(fontRendererObj, EnumChatFormatting.RED + I18n.format("options.stream.changes", new Object[0]), width / 2, 20 + fontRendererObj.FONT_HEIGHT, 16777215);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
