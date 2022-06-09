package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.stream.GuiStreamOptions;
import net.minecraft.client.gui.stream.GuiStreamUnavailable;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.stream.IStream;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.EnumDifficulty;

public class GuiOptions extends GuiScreen implements GuiYesNoCallback
{
    private static final GameSettings.Options[] field_146440_f = new GameSettings.Options[] {GameSettings.Options.FOV};
    private final GuiScreen field_146441_g;

    /** Reference to the GameSettings object. */
    private final GameSettings game_settings_1;
    private GuiButton field_175357_i;
    private GuiLockIconButton field_175356_r;
    protected String field_146442_a = "Options";

    public GuiOptions(GuiScreen p_i1046_1_, GameSettings p_i1046_2_)
    {
        field_146441_g = p_i1046_1_;
        game_settings_1 = p_i1046_2_;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        int i = 0;
        field_146442_a = I18n.format("options.title");

        for (GameSettings.Options gamesettings$options : GuiOptions.field_146440_f)
        {
            if (gamesettings$options.getEnumFloat())
            {
                buttonList.add(new GuiOptionSlider(gamesettings$options.returnEnumOrdinal(), width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), gamesettings$options));
            }
            else
            {
                GuiOptionButton guioptionbutton = new GuiOptionButton(gamesettings$options.returnEnumOrdinal(), width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), gamesettings$options, game_settings_1.getKeyBinding(gamesettings$options));
                buttonList.add(guioptionbutton);
            }

            ++i;
        }

        if (mc.theWorld != null)
        {
            EnumDifficulty enumdifficulty = mc.theWorld.getDifficulty();
            field_175357_i = new GuiButton(108, width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), 150, 20, func_175355_a(enumdifficulty));
            buttonList.add(field_175357_i);

            if (mc.isSingleplayer() && !mc.theWorld.getWorldInfo().isHardcoreModeEnabled())
            {
                field_175357_i.setWidth(field_175357_i.getButtonWidth() - 20);
                field_175356_r = new GuiLockIconButton(109, field_175357_i.xPosition + field_175357_i.getButtonWidth(), field_175357_i.yPosition);
                buttonList.add(field_175356_r);
                field_175356_r.func_175229_b(mc.theWorld.getWorldInfo().isDifficultyLocked());
                field_175356_r.enabled = !field_175356_r.func_175230_c();
                field_175357_i.enabled = !field_175356_r.func_175230_c();
            }
            else
            {
                field_175357_i.enabled = false;
            }
        }

        buttonList.add(new GuiButton(110, width / 2 - 155, height / 6 + 48 - 6, 150, 20, I18n.format("options.skinCustomisation")));
        buttonList.add(new GuiButton(8675309, width / 2 + 5, height / 6 + 48 - 6, 150, 20, "Super Secret Settings...")
        {
            public void playPressSound(SoundHandler soundHandlerIn)
            {
                SoundEventAccessorComposite soundeventaccessorcomposite = soundHandlerIn.getRandomSoundFromCategories(SoundCategory.ANIMALS, SoundCategory.BLOCKS, SoundCategory.MOBS, SoundCategory.PLAYERS, SoundCategory.WEATHER);

                if (soundeventaccessorcomposite != null)
                {
                    soundHandlerIn.playSound(PositionedSoundRecord.create(soundeventaccessorcomposite.getSoundEventLocation(), 0.5F));
                }
            }
        });
        buttonList.add(new GuiButton(106, width / 2 - 155, height / 6 + 72 - 6, 150, 20, I18n.format("options.sounds")));
        buttonList.add(new GuiButton(107, width / 2 + 5, height / 6 + 72 - 6, 150, 20, I18n.format("options.stream")));
        buttonList.add(new GuiButton(101, width / 2 - 155, height / 6 + 96 - 6, 150, 20, I18n.format("options.video")));
        buttonList.add(new GuiButton(100, width / 2 + 5, height / 6 + 96 - 6, 150, 20, I18n.format("options.controls")));
        buttonList.add(new GuiButton(102, width / 2 - 155, height / 6 + 120 - 6, 150, 20, I18n.format("options.language")));
        buttonList.add(new GuiButton(103, width / 2 + 5, height / 6 + 120 - 6, 150, 20, I18n.format("options.chat.title")));
        buttonList.add(new GuiButton(105, width / 2 - 155, height / 6 + 144 - 6, 150, 20, I18n.format("options.resourcepack")));
        buttonList.add(new GuiButton(104, width / 2 + 5, height / 6 + 144 - 6, 150, 20, I18n.format("options.snooper.view")));
        buttonList.add(new GuiButton(200, width / 2 - 100, height / 6 + 168, I18n.format("gui.done")));
    }

    public String func_175355_a(EnumDifficulty p_175355_1_)
    {
        IChatComponent ichatcomponent = new ChatComponentText("");
        ichatcomponent.appendSibling(new ChatComponentTranslation("options.difficulty"));
        ichatcomponent.appendText(": ");
        ichatcomponent.appendSibling(new ChatComponentTranslation(p_175355_1_.getDifficultyResourceKey()));
        return ichatcomponent.getFormattedText();
    }

    public void confirmClicked(boolean result, int id)
    {
        mc.displayGuiScreen(this);

        if (id == 109 && result && mc.theWorld != null)
        {
            mc.theWorld.getWorldInfo().setDifficultyLocked(true);
            field_175356_r.func_175229_b(true);
            field_175356_r.enabled = false;
            field_175357_i.enabled = false;
        }
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
                game_settings_1.setOptionValue(gamesettings$options, 1);
                button.displayString = game_settings_1.getKeyBinding(GameSettings.Options.getEnumOptions(button.id));
            }

            if (button.id == 108)
            {
                mc.theWorld.getWorldInfo().setDifficulty(EnumDifficulty.getDifficultyEnum(mc.theWorld.getDifficulty().getDifficultyId() + 1));
                field_175357_i.displayString = func_175355_a(mc.theWorld.getDifficulty());
            }

            if (button.id == 109)
            {
                mc.displayGuiScreen(new GuiYesNo(this, (new ChatComponentTranslation("difficulty.lock.title")).getFormattedText(), (new ChatComponentTranslation("difficulty.lock.question", new ChatComponentTranslation(mc.theWorld.getWorldInfo().getDifficulty().getDifficultyResourceKey()))).getFormattedText(), 109));
            }

            if (button.id == 110)
            {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(new GuiCustomizeSkin(this));
            }

            if (button.id == 8675309)
            {
                mc.entityRenderer.activateNextShader();
            }

            if (button.id == 101)
            {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(new GuiVideoSettings(this, game_settings_1));
            }

            if (button.id == 100)
            {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(new GuiControls(this, game_settings_1));
            }

            if (button.id == 102)
            {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(new GuiLanguage(this, game_settings_1, mc.getLanguageManager()));
            }

            if (button.id == 103)
            {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(new ScreenChatOptions(this, game_settings_1));
            }

            if (button.id == 104)
            {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(new GuiSnooper(this, game_settings_1));
            }

            if (button.id == 200)
            {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(field_146441_g);
            }

            if (button.id == 105)
            {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(new GuiScreenResourcePacks(this));
            }

            if (button.id == 106)
            {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(new GuiScreenOptionsSounds(this, game_settings_1));
            }

            if (button.id == 107)
            {
                mc.gameSettings.saveOptions();
                IStream istream = mc.getTwitchStream();

                if (istream.func_152936_l() && istream.func_152928_D())
                {
                    mc.displayGuiScreen(new GuiStreamOptions(this, game_settings_1));
                }
                else
                {
                    GuiStreamUnavailable.func_152321_a(this);
                }
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, field_146442_a, width / 2, 15, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
