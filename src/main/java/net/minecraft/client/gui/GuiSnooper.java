package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

public class GuiSnooper extends GuiScreen
{
    private final GuiScreen field_146608_a;

    /** Reference to the GameSettings object. */
    private final GameSettings game_settings_2;
    private final java.util.List<String> field_146604_g = Lists.newArrayList();
    private final java.util.List<String> field_146609_h = Lists.newArrayList();
    private String field_146610_i;
    private String[] field_146607_r;
    private GuiSnooper.List field_146606_s;
    private GuiButton field_146605_t;

    public GuiSnooper(GuiScreen p_i1061_1_, GameSettings p_i1061_2_)
    {
        field_146608_a = p_i1061_1_;
        game_settings_2 = p_i1061_2_;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        field_146610_i = I18n.format("options.snooper.title");
        String s = I18n.format("options.snooper.desc");
        java.util.List<String> list = Lists.newArrayList();

        for (Object s1 : fontRendererObj.listFormattedStringToWidth(s, width - 30))
        {
            list.add((String) s1);
        }

        field_146607_r = list.toArray(new String[list.size()]);
        field_146604_g.clear();
        field_146609_h.clear();
        buttonList.add(field_146605_t = new GuiButton(1, width / 2 - 152, height - 30, 150, 20, game_settings_2.getKeyBinding(GameSettings.Options.SNOOPER_ENABLED)));
        buttonList.add(new GuiButton(2, width / 2 + 2, height - 30, 150, 20, I18n.format("gui.done")));
        boolean flag = mc.getIntegratedServer() != null && mc.getIntegratedServer().getPlayerUsageSnooper() != null;

        for (Map.Entry<String, String> entry : (new TreeMap<String, String>(this.mc.getPlayerUsageSnooper().getCurrentStats())).entrySet())
        {
            this.field_146604_g.add((flag ? "C " : "") + (String)entry.getKey());
            this.field_146609_h.add(this.fontRendererObj.trimStringToWidth((String)entry.getValue(), this.width - 220));
        }

        if (flag)
        {
            for (Map.Entry<String, String> entry1 : (new TreeMap<String, String>(mc.getIntegratedServer().getPlayerUsageSnooper().getCurrentStats())).entrySet())
            {
                field_146604_g.add("S " + entry1.getKey());
                field_146609_h.add(fontRendererObj.trimStringToWidth(entry1.getValue(), width - 220));
            }
        }

        field_146606_s = new GuiSnooper.List();
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        field_146606_s.handleMouseInput();
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 2)
            {
                game_settings_2.saveOptions();
                game_settings_2.saveOptions();
                mc.displayGuiScreen(field_146608_a);
            }

            if (button.id == 1)
            {
                game_settings_2.setOptionValue(GameSettings.Options.SNOOPER_ENABLED, 1);
                field_146605_t.displayString = game_settings_2.getKeyBinding(GameSettings.Options.SNOOPER_ENABLED);
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        field_146606_s.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRendererObj, field_146610_i, width / 2, 8, 16777215);
        int i = 22;

        for (String s : field_146607_r)
        {
            drawCenteredString(fontRendererObj, s, width / 2, i, 8421504);
            i += fontRendererObj.FONT_HEIGHT;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class List extends GuiSlot
    {
        public List()
        {
            super(GuiSnooper.this.mc, GuiSnooper.this.width, GuiSnooper.this.height, 80, GuiSnooper.this.height - 40, fontRendererObj.FONT_HEIGHT + 1);
        }

        protected int getSize()
        {
            return field_146604_g.size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
        }

        protected boolean isSelected(int slotIndex)
        {
            return false;
        }

        protected void drawBackground()
        {
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn)
        {
            fontRendererObj.drawString(field_146604_g.get(entryID), 10, p_180791_3_, 16777215);
            fontRendererObj.drawString(field_146609_h.get(entryID), 230, p_180791_3_, 16777215);
        }

        protected int getScrollBarX()
        {
            return width - 10;
        }
    }
}
