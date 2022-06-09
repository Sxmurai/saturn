package net.minecraft.client.gui;

import java.io.IOException;
import java.util.Random;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

public class GuiCreateWorld extends GuiScreen
{
    private final GuiScreen parentScreen;
    private GuiTextField field_146333_g;
    private GuiTextField field_146335_h;
    private String field_146336_i;
    private String gameMode = "survival";
    private String field_175300_s;
    private boolean field_146341_s = true;

    /** If cheats are allowed */
    private boolean allowCheats;
    private boolean field_146339_u;
    private boolean field_146338_v;
    private boolean field_146337_w;
    private boolean field_146345_x;
    private boolean field_146344_y;
    private GuiButton btnGameMode;
    private GuiButton btnMoreOptions;
    private GuiButton btnMapFeatures;
    private GuiButton btnBonusItems;
    private GuiButton btnMapType;
    private GuiButton btnAllowCommands;
    private GuiButton btnCustomizeType;
    private String field_146323_G;
    private String field_146328_H;
    private String field_146329_I;
    private String field_146330_J;
    private int selectedIndex;
    public String chunkProviderSettingsJson = "";

    /** These filenames are known to be restricted on one or more OS's. */
    private static final String[] disallowedFilenames = new String[] {"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

    public GuiCreateWorld(GuiScreen p_i46320_1_)
    {
        parentScreen = p_i46320_1_;
        field_146329_I = "";
        field_146330_J = I18n.format("selectWorld.newWorld");
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        field_146333_g.updateCursorCounter();
        field_146335_h.updateCursorCounter();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("selectWorld.create")));
        buttonList.add(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
        buttonList.add(btnGameMode = new GuiButton(2, width / 2 - 75, 115, 150, 20, I18n.format("selectWorld.gameMode")));
        buttonList.add(btnMoreOptions = new GuiButton(3, width / 2 - 75, 187, 150, 20, I18n.format("selectWorld.moreWorldOptions")));
        buttonList.add(btnMapFeatures = new GuiButton(4, width / 2 - 155, 100, 150, 20, I18n.format("selectWorld.mapFeatures")));
        btnMapFeatures.visible = false;
        buttonList.add(btnBonusItems = new GuiButton(7, width / 2 + 5, 151, 150, 20, I18n.format("selectWorld.bonusItems")));
        btnBonusItems.visible = false;
        buttonList.add(btnMapType = new GuiButton(5, width / 2 + 5, 100, 150, 20, I18n.format("selectWorld.mapType")));
        btnMapType.visible = false;
        buttonList.add(btnAllowCommands = new GuiButton(6, width / 2 - 155, 151, 150, 20, I18n.format("selectWorld.allowCommands")));
        btnAllowCommands.visible = false;
        buttonList.add(btnCustomizeType = new GuiButton(8, width / 2 + 5, 120, 150, 20, I18n.format("selectWorld.customizeType")));
        btnCustomizeType.visible = false;
        field_146333_g = new GuiTextField(9, fontRendererObj, width / 2 - 100, 60, 200, 20);
        field_146333_g.setFocused(true);
        field_146333_g.setText(field_146330_J);
        field_146335_h = new GuiTextField(10, fontRendererObj, width / 2 - 100, 60, 200, 20);
        field_146335_h.setText(field_146329_I);
        func_146316_a(field_146344_y);
        func_146314_g();
        func_146319_h();
    }

    private void func_146314_g()
    {
        field_146336_i = field_146333_g.getText().trim();

        for (char c0 : ChatAllowedCharacters.allowedCharactersArray)
        {
            field_146336_i = field_146336_i.replace(c0, '_');
        }

        if (StringUtils.isEmpty(field_146336_i))
        {
            field_146336_i = "World";
        }

        field_146336_i = GuiCreateWorld.func_146317_a(mc.getSaveLoader(), field_146336_i);
    }

    private void func_146319_h()
    {
        btnGameMode.displayString = I18n.format("selectWorld.gameMode") + ": " + I18n.format("selectWorld.gameMode." + gameMode);
        field_146323_G = I18n.format("selectWorld.gameMode." + gameMode + ".line1");
        field_146328_H = I18n.format("selectWorld.gameMode." + gameMode + ".line2");
        btnMapFeatures.displayString = I18n.format("selectWorld.mapFeatures") + " ";

        if (field_146341_s)
        {
            btnMapFeatures.displayString = btnMapFeatures.displayString + I18n.format("options.on");
        }
        else
        {
            btnMapFeatures.displayString = btnMapFeatures.displayString + I18n.format("options.off");
        }

        btnBonusItems.displayString = I18n.format("selectWorld.bonusItems") + " ";

        if (field_146338_v && !field_146337_w)
        {
            btnBonusItems.displayString = btnBonusItems.displayString + I18n.format("options.on");
        }
        else
        {
            btnBonusItems.displayString = btnBonusItems.displayString + I18n.format("options.off");
        }

        btnMapType.displayString = I18n.format("selectWorld.mapType") + " " + I18n.format(WorldType.worldTypes[selectedIndex].getTranslateName());
        btnAllowCommands.displayString = I18n.format("selectWorld.allowCommands") + " ";

        if (allowCheats && !field_146337_w)
        {
            btnAllowCommands.displayString = btnAllowCommands.displayString + I18n.format("options.on");
        }
        else
        {
            btnAllowCommands.displayString = btnAllowCommands.displayString + I18n.format("options.off");
        }
    }

    public static String func_146317_a(ISaveFormat p_146317_0_, String p_146317_1_)
    {
        p_146317_1_ = p_146317_1_.replaceAll("[\\./\"]", "_");

        for (String s : GuiCreateWorld.disallowedFilenames)
        {
            if (p_146317_1_.equalsIgnoreCase(s))
            {
                p_146317_1_ = "_" + p_146317_1_ + "_";
            }
        }

        while (p_146317_0_.getWorldInfo(p_146317_1_) != null)
        {
            p_146317_1_ = p_146317_1_ + "-";
        }

        return p_146317_1_;
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 1)
            {
                mc.displayGuiScreen(parentScreen);
            }
            else if (button.id == 0)
            {
                mc.displayGuiScreen(null);

                if (field_146345_x)
                {
                    return;
                }

                field_146345_x = true;
                long i = (new Random()).nextLong();
                String s = field_146335_h.getText();

                if (!StringUtils.isEmpty(s))
                {
                    try
                    {
                        long j = Long.parseLong(s);

                        if (j != 0L)
                        {
                            i = j;
                        }
                    }
                    catch (NumberFormatException var7)
                    {
                        i = s.hashCode();
                    }
                }

                WorldSettings.GameType worldsettings$gametype = WorldSettings.GameType.getByName(gameMode);
                WorldSettings worldsettings = new WorldSettings(i, worldsettings$gametype, field_146341_s, field_146337_w, WorldType.worldTypes[selectedIndex]);
                worldsettings.setWorldName(chunkProviderSettingsJson);

                if (field_146338_v && !field_146337_w)
                {
                    worldsettings.enableBonusChest();
                }

                if (allowCheats && !field_146337_w)
                {
                    worldsettings.enableCommands();
                }

                mc.launchIntegratedServer(field_146336_i, field_146333_g.getText().trim(), worldsettings);
            }
            else if (button.id == 3)
            {
                func_146315_i();
            }
            else if (button.id == 2)
            {
                if (gameMode.equals("survival"))
                {
                    if (!field_146339_u)
                    {
                        allowCheats = false;
                    }

                    field_146337_w = false;
                    gameMode = "hardcore";
                    field_146337_w = true;
                    btnAllowCommands.enabled = false;
                    btnBonusItems.enabled = false;
                    func_146319_h();
                }
                else if (gameMode.equals("hardcore"))
                {
                    if (!field_146339_u)
                    {
                        allowCheats = true;
                    }

                    field_146337_w = false;
                    gameMode = "creative";
                    func_146319_h();
                    field_146337_w = false;
                    btnAllowCommands.enabled = true;
                    btnBonusItems.enabled = true;
                }
                else
                {
                    if (!field_146339_u)
                    {
                        allowCheats = false;
                    }

                    gameMode = "survival";
                    func_146319_h();
                    btnAllowCommands.enabled = true;
                    btnBonusItems.enabled = true;
                    field_146337_w = false;
                }

                func_146319_h();
            }
            else if (button.id == 4)
            {
                field_146341_s = !field_146341_s;
                func_146319_h();
            }
            else if (button.id == 7)
            {
                field_146338_v = !field_146338_v;
                func_146319_h();
            }
            else if (button.id == 5)
            {
                ++selectedIndex;

                if (selectedIndex >= WorldType.worldTypes.length)
                {
                    selectedIndex = 0;
                }

                while (!func_175299_g())
                {
                    ++selectedIndex;

                    if (selectedIndex >= WorldType.worldTypes.length)
                    {
                        selectedIndex = 0;
                    }
                }

                chunkProviderSettingsJson = "";
                func_146319_h();
                func_146316_a(field_146344_y);
            }
            else if (button.id == 6)
            {
                field_146339_u = true;
                allowCheats = !allowCheats;
                func_146319_h();
            }
            else if (button.id == 8)
            {
                if (WorldType.worldTypes[selectedIndex] == WorldType.FLAT)
                {
                    mc.displayGuiScreen(new GuiCreateFlatWorld(this, chunkProviderSettingsJson));
                }
                else
                {
                    mc.displayGuiScreen(new GuiCustomizeWorldScreen(this, chunkProviderSettingsJson));
                }
            }
        }
    }

    private boolean func_175299_g()
    {
        WorldType worldtype = WorldType.worldTypes[selectedIndex];
        return worldtype != null && worldtype.getCanBeCreated() && (worldtype != WorldType.DEBUG_WORLD || GuiScreen.isShiftKeyDown());
    }

    private void func_146315_i()
    {
        func_146316_a(!field_146344_y);
    }

    private void func_146316_a(boolean p_146316_1_)
    {
        field_146344_y = p_146316_1_;

        if (WorldType.worldTypes[selectedIndex] == WorldType.DEBUG_WORLD)
        {
            btnGameMode.visible = !field_146344_y;
            btnGameMode.enabled = false;

            if (field_175300_s == null)
            {
                field_175300_s = gameMode;
            }

            gameMode = "spectator";
            btnMapFeatures.visible = false;
            btnBonusItems.visible = false;
            btnMapType.visible = field_146344_y;
            btnAllowCommands.visible = false;
            btnCustomizeType.visible = false;
        }
        else
        {
            btnGameMode.visible = !field_146344_y;
            btnGameMode.enabled = true;

            if (field_175300_s != null)
            {
                gameMode = field_175300_s;
                field_175300_s = null;
            }

            btnMapFeatures.visible = field_146344_y && WorldType.worldTypes[selectedIndex] != WorldType.CUSTOMIZED;
            btnBonusItems.visible = field_146344_y;
            btnMapType.visible = field_146344_y;
            btnAllowCommands.visible = field_146344_y;
            btnCustomizeType.visible = field_146344_y && (WorldType.worldTypes[selectedIndex] == WorldType.FLAT || WorldType.worldTypes[selectedIndex] == WorldType.CUSTOMIZED);
        }

        func_146319_h();

        if (field_146344_y)
        {
            btnMoreOptions.displayString = I18n.format("gui.done");
        }
        else
        {
            btnMoreOptions.displayString = I18n.format("selectWorld.moreWorldOptions");
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (field_146333_g.isFocused() && !field_146344_y)
        {
            field_146333_g.textboxKeyTyped(typedChar, keyCode);
            field_146330_J = field_146333_g.getText();
        }
        else if (field_146335_h.isFocused() && field_146344_y)
        {
            field_146335_h.textboxKeyTyped(typedChar, keyCode);
            field_146329_I = field_146335_h.getText();
        }

        if (keyCode == 28 || keyCode == 156)
        {
            actionPerformed(buttonList.get(0));
        }

        buttonList.get(0).enabled = field_146333_g.getText().length() > 0;
        func_146314_g();
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (field_146344_y)
        {
            field_146335_h.mouseClicked(mouseX, mouseY, mouseButton);
        }
        else
        {
            field_146333_g.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("selectWorld.create"), width / 2, 20, -1);

        if (field_146344_y)
        {
            drawString(fontRendererObj, I18n.format("selectWorld.enterSeed"), width / 2 - 100, 47, -6250336);
            drawString(fontRendererObj, I18n.format("selectWorld.seedInfo"), width / 2 - 100, 85, -6250336);

            if (btnMapFeatures.visible)
            {
                drawString(fontRendererObj, I18n.format("selectWorld.mapFeatures.info"), width / 2 - 150, 122, -6250336);
            }

            if (btnAllowCommands.visible)
            {
                drawString(fontRendererObj, I18n.format("selectWorld.allowCommands.info"), width / 2 - 150, 172, -6250336);
            }

            field_146335_h.drawTextBox();

            if (WorldType.worldTypes[selectedIndex].showWorldInfoNotice())
            {
                fontRendererObj.drawSplitString(I18n.format(WorldType.worldTypes[selectedIndex].func_151359_c()), btnMapType.xPosition + 2, btnMapType.yPosition + 22, btnMapType.getButtonWidth(), 10526880);
            }
        }
        else
        {
            drawString(fontRendererObj, I18n.format("selectWorld.enterName"), width / 2 - 100, 47, -6250336);
            drawString(fontRendererObj, I18n.format("selectWorld.resultFolder") + " " + field_146336_i, width / 2 - 100, 85, -6250336);
            field_146333_g.drawTextBox();
            drawString(fontRendererObj, field_146323_G, width / 2 - 100, 137, -6250336);
            drawString(fontRendererObj, field_146328_H, width / 2 - 100, 149, -6250336);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void func_146318_a(WorldInfo p_146318_1_)
    {
        field_146330_J = I18n.format("selectWorld.newWorld.copyOf", p_146318_1_.getWorldName());
        field_146329_I = p_146318_1_.getSeed() + "";
        selectedIndex = p_146318_1_.getTerrainType().getWorldTypeID();
        chunkProviderSettingsJson = p_146318_1_.getGeneratorOptions();
        field_146341_s = p_146318_1_.isMapFeaturesEnabled();
        allowCheats = p_146318_1_.areCommandsAllowed();

        if (p_146318_1_.isHardcoreModeEnabled())
        {
            gameMode = "hardcore";
        }
        else if (p_146318_1_.getGameType().isSurvivalOrAdventure())
        {
            gameMode = "survival";
        }
        else if (p_146318_1_.getGameType().isCreative())
        {
            gameMode = "creative";
        }
    }
}
