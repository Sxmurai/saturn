package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;

public class GuiLanguage extends GuiScreen
{
    /** The parent Gui screen */
    protected GuiScreen parentScreen;

    /** The List GuiSlot object reference. */
    private GuiLanguage.List list;

    /** Reference to the GameSettings object. */
    private final GameSettings game_settings_3;

    /** Reference to the LanguageManager object. */
    private final LanguageManager languageManager;

    /**
     * A button which allows the user to determine if the Unicode font should be forced.
     */
    private GuiOptionButton forceUnicodeFontBtn;

    /** The button to confirm the current settings. */
    private GuiOptionButton confirmSettingsBtn;

    public GuiLanguage(GuiScreen screen, GameSettings gameSettingsObj, LanguageManager manager)
    {
        parentScreen = screen;
        game_settings_3 = gameSettingsObj;
        languageManager = manager;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        buttonList.add(forceUnicodeFontBtn = new GuiOptionButton(100, width / 2 - 155, height - 38, GameSettings.Options.FORCE_UNICODE_FONT, game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT)));
        buttonList.add(confirmSettingsBtn = new GuiOptionButton(6, width / 2 - 155 + 160, height - 38, I18n.format("gui.done")));
        list = new GuiLanguage.List(mc);
        list.registerScrollButtons(7, 8);
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        list.handleMouseInput();
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            switch (button.id)
            {
                case 5:
                    break;

                case 6:
                    mc.displayGuiScreen(parentScreen);
                    break;

                case 100:
                    if (button instanceof GuiOptionButton)
                    {
                        game_settings_3.setOptionValue(((GuiOptionButton)button).returnEnumOptions(), 1);
                        button.displayString = game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
                        ScaledResolution scaledresolution = new ScaledResolution(mc);
                        int i = scaledresolution.getScaledWidth();
                        int j = scaledresolution.getScaledHeight();
                        setWorldAndResolution(mc, i, j);
                    }

                    break;

                default:
                    list.actionPerformed(button);
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        list.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRendererObj, I18n.format("options.language"), width / 2, 16, 16777215);
        drawCenteredString(fontRendererObj, "(" + I18n.format("options.languageWarning") + ")", width / 2, height - 56, 8421504);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class List extends GuiSlot
    {
        private final java.util.List<String> langCodeList = Lists.newArrayList();
        private final Map<String, Language> languageMap = Maps.newHashMap();

        public List(Minecraft mcIn)
        {
            super(mcIn, GuiLanguage.this.width, GuiLanguage.this.height, 32, GuiLanguage.this.height - 65 + 4, 18);

            for (Language language : languageManager.getLanguages())
            {
                languageMap.put(language.getLanguageCode(), language);
                langCodeList.add(language.getLanguageCode());
            }
        }

        protected int getSize()
        {
            return langCodeList.size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
            Language language = languageMap.get(langCodeList.get(slotIndex));
            languageManager.setCurrentLanguage(language);
            game_settings_3.language = language.getLanguageCode();
            mc.refreshResources();
            fontRendererObj.setUnicodeFlag(languageManager.isCurrentLocaleUnicode() || game_settings_3.forceUnicodeFont);
            fontRendererObj.setBidiFlag(languageManager.isCurrentLanguageBidirectional());
            confirmSettingsBtn.displayString = I18n.format("gui.done");
            forceUnicodeFontBtn.displayString = game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
            game_settings_3.saveOptions();
        }

        protected boolean isSelected(int slotIndex)
        {
            return langCodeList.get(slotIndex).equals(languageManager.getCurrentLanguage().getLanguageCode());
        }

        protected int getContentHeight()
        {
            return getSize() * 18;
        }

        protected void drawBackground()
        {
            drawDefaultBackground();
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn)
        {
            fontRendererObj.setBidiFlag(true);
            drawCenteredString(fontRendererObj, languageMap.get(langCodeList.get(entryID)).toString(), width / 2, p_180791_3_ + 1, 16777215);
            fontRendererObj.setBidiFlag(languageManager.getCurrentLanguage().isBidirectional());
        }
    }
}
