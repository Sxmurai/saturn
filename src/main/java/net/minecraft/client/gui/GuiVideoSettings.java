package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import optifine.Config;
import optifine.GuiAnimationSettingsOF;
import optifine.GuiDetailSettingsOF;
import optifine.GuiOptionButtonOF;
import optifine.GuiOptionSliderOF;
import optifine.GuiOtherSettingsOF;
import optifine.GuiPerformanceSettingsOF;
import optifine.GuiQualitySettingsOF;
import optifine.Lang;
import optifine.TooltipManager;
import shadersmod.client.GuiShaders;

public class GuiVideoSettings extends GuiScreen
{
    private final GuiScreen parentGuiScreen;
    protected String screenTitle = "Video Settings";
    private final GameSettings guiGameSettings;

    /** An array of all of GameSettings.Options's video options. */
    private static final GameSettings.Options[] videoOptions = new GameSettings.Options[] {GameSettings.Options.GRAPHICS, GameSettings.Options.RENDER_DISTANCE, GameSettings.Options.AMBIENT_OCCLUSION, GameSettings.Options.FRAMERATE_LIMIT, GameSettings.Options.AO_LEVEL, GameSettings.Options.VIEW_BOBBING, GameSettings.Options.GUI_SCALE, GameSettings.Options.USE_VBO, GameSettings.Options.GAMMA, GameSettings.Options.BLOCK_ALTERNATIVES, GameSettings.Options.FOG_FANCY, GameSettings.Options.FOG_START};
    private static final String __OBFID = "CL_00000718";
    private final TooltipManager tooltipManager = new TooltipManager(this);

    public GuiVideoSettings(GuiScreen parentScreenIn, GameSettings gameSettingsIn)
    {
        parentGuiScreen = parentScreenIn;
        guiGameSettings = gameSettingsIn;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        screenTitle = I18n.format("options.videoTitle");
        buttonList.clear();

        for (int i = 0; i < GuiVideoSettings.videoOptions.length; ++i)
        {
            GameSettings.Options gamesettings$options = GuiVideoSettings.videoOptions[i];

            if (gamesettings$options != null)
            {
                int j = width / 2 - 155 + i % 2 * 160;
                int k = height / 6 + 21 * (i / 2) - 12;

                if (gamesettings$options.getEnumFloat())
                {
                    buttonList.add(new GuiOptionSliderOF(gamesettings$options.returnEnumOrdinal(), j, k, gamesettings$options));
                }
                else
                {
                    buttonList.add(new GuiOptionButtonOF(gamesettings$options.returnEnumOrdinal(), j, k, gamesettings$options, guiGameSettings.getKeyBinding(gamesettings$options)));
                }
            }
        }

        int l = height / 6 + 21 * (GuiVideoSettings.videoOptions.length / 2) - 12;
        int i1 = 0;
        i1 = width / 2 - 155 + 0;
        buttonList.add(new GuiOptionButton(231, i1, l, Lang.get("of.options.shaders")));
        i1 = width / 2 - 155 + 160;
        buttonList.add(new GuiOptionButton(202, i1, l, Lang.get("of.options.quality")));
        l = l + 21;
        i1 = width / 2 - 155 + 0;
        buttonList.add(new GuiOptionButton(201, i1, l, Lang.get("of.options.details")));
        i1 = width / 2 - 155 + 160;
        buttonList.add(new GuiOptionButton(212, i1, l, Lang.get("of.options.performance")));
        l = l + 21;
        i1 = width / 2 - 155 + 0;
        buttonList.add(new GuiOptionButton(211, i1, l, Lang.get("of.options.animations")));
        i1 = width / 2 - 155 + 160;
        buttonList.add(new GuiOptionButton(222, i1, l, Lang.get("of.options.other")));
        l = l + 21;
        buttonList.add(new GuiButton(200, width / 2 - 100, height / 6 + 168 + 11, I18n.format("gui.done")));
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            int i = guiGameSettings.guiScale;

            if (button.id < 200 && button instanceof GuiOptionButton)
            {
                guiGameSettings.setOptionValue(((GuiOptionButton)button).returnEnumOptions(), 1);
                button.displayString = guiGameSettings.getKeyBinding(GameSettings.Options.getEnumOptions(button.id));
            }

            if (button.id == 200)
            {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(parentGuiScreen);
            }

            if (guiGameSettings.guiScale != i)
            {
                ScaledResolution scaledresolution = new ScaledResolution(mc);
                int j = scaledresolution.getScaledWidth();
                int k = scaledresolution.getScaledHeight();
                setWorldAndResolution(mc, j, k);
            }

            if (button.id == 201)
            {
                mc.gameSettings.saveOptions();
                GuiDetailSettingsOF guidetailsettingsof = new GuiDetailSettingsOF(this, guiGameSettings);
                mc.displayGuiScreen(guidetailsettingsof);
            }

            if (button.id == 202)
            {
                mc.gameSettings.saveOptions();
                GuiQualitySettingsOF guiqualitysettingsof = new GuiQualitySettingsOF(this, guiGameSettings);
                mc.displayGuiScreen(guiqualitysettingsof);
            }

            if (button.id == 211)
            {
                mc.gameSettings.saveOptions();
                GuiAnimationSettingsOF guianimationsettingsof = new GuiAnimationSettingsOF(this, guiGameSettings);
                mc.displayGuiScreen(guianimationsettingsof);
            }

            if (button.id == 212)
            {
                mc.gameSettings.saveOptions();
                GuiPerformanceSettingsOF guiperformancesettingsof = new GuiPerformanceSettingsOF(this, guiGameSettings);
                mc.displayGuiScreen(guiperformancesettingsof);
            }

            if (button.id == 222)
            {
                mc.gameSettings.saveOptions();
                GuiOtherSettingsOF guiothersettingsof = new GuiOtherSettingsOF(this, guiGameSettings);
                mc.displayGuiScreen(guiothersettingsof);
            }

            if (button.id == 231)
            {
                if (Config.isAntialiasing() || Config.isAntialiasingConfigured())
                {
                    Config.showGuiMessage(Lang.get("of.message.shaders.aa1"), Lang.get("of.message.shaders.aa2"));
                    return;
                }

                if (Config.isAnisotropicFiltering())
                {
                    Config.showGuiMessage(Lang.get("of.message.shaders.af1"), Lang.get("of.message.shaders.af2"));
                    return;
                }

                if (Config.isFastRender())
                {
                    Config.showGuiMessage(Lang.get("of.message.shaders.fr1"), Lang.get("of.message.shaders.fr2"));
                    return;
                }

                mc.gameSettings.saveOptions();
                GuiShaders guishaders = new GuiShaders(this, guiGameSettings);
                mc.displayGuiScreen(guishaders);
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, screenTitle, width / 2, 15, 16777215);
        String s = Config.getVersion();
        String s1 = "HD_U";

        if (s1.equals("HD"))
        {
            s = "OptiFine HD H8";
        }

        if (s1.equals("HD_U"))
        {
            s = "OptiFine HD H8 Ultra";
        }

        if (s1.equals("L"))
        {
            s = "OptiFine H8 Light";
        }

        drawString(fontRendererObj, s, 2, height - 10, 8421504);
        String s2 = "Minecraft 1.8.8";
        int i = fontRendererObj.getStringWidth(s2);
        drawString(fontRendererObj, s2, width - i - 2, height - 10, 8421504);
        super.drawScreen(mouseX, mouseY, partialTicks);
        tooltipManager.drawTooltips(mouseX, mouseY, buttonList);
    }

    public static int getButtonWidth(GuiButton p_getButtonWidth_0_)
    {
        return p_getButtonWidth_0_.width;
    }

    public static int getButtonHeight(GuiButton p_getButtonHeight_0_)
    {
        return p_getButtonHeight_0_.height;
    }

    public static void drawGradientRect(GuiScreen p_drawGradientRect_0_, int p_drawGradientRect_1_, int p_drawGradientRect_2_, int p_drawGradientRect_3_, int p_drawGradientRect_4_, int p_drawGradientRect_5_, int p_drawGradientRect_6_)
    {
        p_drawGradientRect_0_.drawGradientRect(p_drawGradientRect_1_, p_drawGradientRect_2_, p_drawGradientRect_3_, p_drawGradientRect_4_, p_drawGradientRect_5_, p_drawGradientRect_6_);
    }
}
