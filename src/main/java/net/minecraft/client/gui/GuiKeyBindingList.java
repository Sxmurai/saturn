package net.minecraft.client.gui;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.ArrayUtils;

public class GuiKeyBindingList extends GuiListExtended
{
    private final GuiControls field_148191_k;
    private final Minecraft mc;
    private final GuiListExtended.IGuiListEntry[] listEntries;
    private int maxListLabelWidth = 0;

    public GuiKeyBindingList(GuiControls controls, Minecraft mcIn)
    {
        super(mcIn, controls.width, controls.height, 63, controls.height - 32, 20);
        field_148191_k = controls;
        mc = mcIn;
        KeyBinding[] akeybinding = ArrayUtils.clone(mcIn.gameSettings.keyBindings);
        listEntries = new GuiListExtended.IGuiListEntry[akeybinding.length + KeyBinding.getKeybinds().size()];
        Arrays.sort(akeybinding);
        int i = 0;
        String s = null;

        for (KeyBinding keybinding : akeybinding)
        {
            String s1 = keybinding.getKeyCategory();

            if (!s1.equals(s))
            {
                s = s1;
                listEntries[i++] = new GuiKeyBindingList.CategoryEntry(s1);
            }

            int j = mcIn.fontRendererObj.getStringWidth(I18n.format(keybinding.getKeyDescription()));

            if (j > maxListLabelWidth)
            {
                maxListLabelWidth = j;
            }

            listEntries[i++] = new GuiKeyBindingList.KeyEntry(keybinding);
        }
    }

    protected int getSize()
    {
        return listEntries.length;
    }

    /**
     * Gets the IGuiListEntry object for the given index
     */
    public GuiListExtended.IGuiListEntry getListEntry(int index)
    {
        return listEntries[index];
    }

    protected int getScrollBarX()
    {
        return super.getScrollBarX() + 15;
    }

    /**
     * Gets the width of the list
     */
    public int getListWidth()
    {
        return super.getListWidth() + 32;
    }

    public class CategoryEntry implements GuiListExtended.IGuiListEntry
    {
        private final String labelText;
        private final int labelWidth;

        public CategoryEntry(String p_i45028_2_)
        {
            labelText = I18n.format(p_i45028_2_);
            labelWidth = mc.fontRendererObj.getStringWidth(labelText);
        }

        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
        {
            mc.fontRendererObj.drawString(labelText, mc.currentScreen.width / 2 - labelWidth / 2, y + slotHeight - mc.fontRendererObj.FONT_HEIGHT - 1, 16777215);
        }

        public boolean mousePressed(int slotIndex, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_)
        {
            return false;
        }

        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
        }

        public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
        {
        }
    }

    public class KeyEntry implements GuiListExtended.IGuiListEntry
    {
        private final KeyBinding keybinding;
        private final String keyDesc;
        private final GuiButton btnChangeKeyBinding;
        private final GuiButton btnReset;

        private KeyEntry(KeyBinding p_i45029_2_)
        {
            keybinding = p_i45029_2_;
            keyDesc = I18n.format(p_i45029_2_.getKeyDescription());
            btnChangeKeyBinding = new GuiButton(0, 0, 0, 75, 20, I18n.format(p_i45029_2_.getKeyDescription()));
            btnReset = new GuiButton(0, 0, 0, 50, 20, I18n.format("controls.reset"));
        }

        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
        {
            boolean flag = field_148191_k.buttonId == keybinding;
            mc.fontRendererObj.drawString(keyDesc, x + 90 - maxListLabelWidth, y + slotHeight / 2 - mc.fontRendererObj.FONT_HEIGHT / 2, 16777215);
            btnReset.xPosition = x + 190;
            btnReset.yPosition = y;
            btnReset.enabled = keybinding.getKeyCode() != keybinding.getKeyCodeDefault();
            btnReset.drawButton(mc, mouseX, mouseY);
            btnChangeKeyBinding.xPosition = x + 105;
            btnChangeKeyBinding.yPosition = y;
            btnChangeKeyBinding.displayString = GameSettings.getKeyDisplayString(keybinding.getKeyCode());
            boolean flag1 = false;

            if (keybinding.getKeyCode() != 0)
            {
                for (KeyBinding keybinding : mc.gameSettings.keyBindings)
                {
                    if (keybinding != this.keybinding && keybinding.getKeyCode() == this.keybinding.getKeyCode())
                    {
                        flag1 = true;
                        break;
                    }
                }
            }

            if (flag)
            {
                btnChangeKeyBinding.displayString = EnumChatFormatting.WHITE + "> " + EnumChatFormatting.YELLOW + btnChangeKeyBinding.displayString + EnumChatFormatting.WHITE + " <";
            }
            else if (flag1)
            {
                btnChangeKeyBinding.displayString = EnumChatFormatting.RED + btnChangeKeyBinding.displayString;
            }

            btnChangeKeyBinding.drawButton(mc, mouseX, mouseY);
        }

        public boolean mousePressed(int slotIndex, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_)
        {
            if (btnChangeKeyBinding.mousePressed(mc, p_148278_2_, p_148278_3_))
            {
                field_148191_k.buttonId = keybinding;
                return true;
            }
            else if (btnReset.mousePressed(mc, p_148278_2_, p_148278_3_))
            {
                mc.gameSettings.setOptionKeyBinding(keybinding, keybinding.getKeyCodeDefault());
                KeyBinding.resetKeyBindingArrayAndHash();
                return true;
            }
            else
            {
                return false;
            }
        }

        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
            btnChangeKeyBinding.mouseReleased(x, y);
            btnReset.mouseReleased(x, y);
        }

        public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
        {
        }
    }
}
