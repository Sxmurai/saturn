package net.minecraft.client.gui.inventory;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class GuiContainerCreative extends InventoryEffectRenderer
{
    /** The location of the creative inventory tabs texture */
    private static final ResourceLocation creativeInventoryTabs = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    private static final InventoryBasic field_147060_v = new InventoryBasic("tmp", true, 45);

    /** Currently selected creative inventory tab index. */
    private static int selectedTabIndex = CreativeTabs.tabBlock.getTabIndex();

    /** Amount scrolled in Creative mode inventory (0 = top, 1 = bottom) */
    private float currentScroll;

    /** True if the scrollbar is being dragged */
    private boolean isScrolling;

    /**
     * True if the left mouse button was held down last time drawScreen was called.
     */
    private boolean wasClicking;
    private GuiTextField searchField;
    private List<Slot> field_147063_B;
    private Slot field_147064_C;
    private boolean field_147057_D;
    private CreativeCrafting field_147059_E;

    public GuiContainerCreative(EntityPlayer p_i1088_1_)
    {
        super(new GuiContainerCreative.ContainerCreative(p_i1088_1_));
        p_i1088_1_.openContainer = inventorySlots;
        allowUserInput = true;
        ySize = 136;
        xSize = 195;
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        if (!mc.playerController.isInCreativeMode())
        {
            mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
        }

        updateActivePotionEffects();
    }

    /**
     * Called when the mouse is clicked over a slot or outside the gui.
     */
    protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType)
    {
        field_147057_D = true;
        boolean flag = clickType == 1;
        clickType = slotId == -999 && clickType == 0 ? 4 : clickType;

        if (slotIn == null && GuiContainerCreative.selectedTabIndex != CreativeTabs.tabInventory.getTabIndex() && clickType != 5)
        {
            InventoryPlayer inventoryplayer1 = mc.thePlayer.inventory;

            if (inventoryplayer1.getItemStack() != null)
            {
                if (clickedButton == 0)
                {
                    mc.thePlayer.dropPlayerItemWithRandomChoice(inventoryplayer1.getItemStack(), true);
                    mc.playerController.sendPacketDropItem(inventoryplayer1.getItemStack());
                    inventoryplayer1.setItemStack(null);
                }

                if (clickedButton == 1)
                {
                    ItemStack itemstack5 = inventoryplayer1.getItemStack().splitStack(1);
                    mc.thePlayer.dropPlayerItemWithRandomChoice(itemstack5, true);
                    mc.playerController.sendPacketDropItem(itemstack5);

                    if (inventoryplayer1.getItemStack().stackSize == 0)
                    {
                        inventoryplayer1.setItemStack(null);
                    }
                }
            }
        }
        else if (slotIn == field_147064_C && flag)
        {
            for (int j = 0; j < mc.thePlayer.inventoryContainer.getInventory().size(); ++j)
            {
                mc.playerController.sendSlotPacket(null, j);
            }
        }
        else if (GuiContainerCreative.selectedTabIndex == CreativeTabs.tabInventory.getTabIndex())
        {
            if (slotIn == field_147064_C)
            {
                mc.thePlayer.inventory.setItemStack(null);
            }
            else if (clickType == 4 && slotIn != null && slotIn.getHasStack())
            {
                ItemStack itemstack = slotIn.decrStackSize(clickedButton == 0 ? 1 : slotIn.getStack().getMaxStackSize());
                mc.thePlayer.dropPlayerItemWithRandomChoice(itemstack, true);
                mc.playerController.sendPacketDropItem(itemstack);
            }
            else if (clickType == 4 && mc.thePlayer.inventory.getItemStack() != null)
            {
                mc.thePlayer.dropPlayerItemWithRandomChoice(mc.thePlayer.inventory.getItemStack(), true);
                mc.playerController.sendPacketDropItem(mc.thePlayer.inventory.getItemStack());
                mc.thePlayer.inventory.setItemStack(null);
            }
            else
            {
                mc.thePlayer.inventoryContainer.slotClick(slotIn == null ? slotId : ((GuiContainerCreative.CreativeSlot)slotIn).slot.slotNumber, clickedButton, clickType, mc.thePlayer);
                mc.thePlayer.inventoryContainer.detectAndSendChanges();
            }
        }
        else if (clickType != 5 && slotIn.inventory == GuiContainerCreative.field_147060_v)
        {
            InventoryPlayer inventoryplayer = mc.thePlayer.inventory;
            ItemStack itemstack1 = inventoryplayer.getItemStack();
            ItemStack itemstack2 = slotIn.getStack();

            if (clickType == 2)
            {
                if (itemstack2 != null && clickedButton >= 0 && clickedButton < 9)
                {
                    ItemStack itemstack7 = itemstack2.copy();
                    itemstack7.stackSize = itemstack7.getMaxStackSize();
                    mc.thePlayer.inventory.setInventorySlotContents(clickedButton, itemstack7);
                    mc.thePlayer.inventoryContainer.detectAndSendChanges();
                }

                return;
            }

            if (clickType == 3)
            {
                if (inventoryplayer.getItemStack() == null && slotIn.getHasStack())
                {
                    ItemStack itemstack6 = slotIn.getStack().copy();
                    itemstack6.stackSize = itemstack6.getMaxStackSize();
                    inventoryplayer.setItemStack(itemstack6);
                }

                return;
            }

            if (clickType == 4)
            {
                if (itemstack2 != null)
                {
                    ItemStack itemstack3 = itemstack2.copy();
                    itemstack3.stackSize = clickedButton == 0 ? 1 : itemstack3.getMaxStackSize();
                    mc.thePlayer.dropPlayerItemWithRandomChoice(itemstack3, true);
                    mc.playerController.sendPacketDropItem(itemstack3);
                }

                return;
            }

            if (itemstack1 != null && itemstack2 != null && itemstack1.isItemEqual(itemstack2))
            {
                if (clickedButton == 0)
                {
                    if (flag)
                    {
                        itemstack1.stackSize = itemstack1.getMaxStackSize();
                    }
                    else if (itemstack1.stackSize < itemstack1.getMaxStackSize())
                    {
                        ++itemstack1.stackSize;
                    }
                }
                else if (itemstack1.stackSize <= 1)
                {
                    inventoryplayer.setItemStack(null);
                }
                else
                {
                    --itemstack1.stackSize;
                }
            }
            else if (itemstack2 != null && itemstack1 == null)
            {
                inventoryplayer.setItemStack(ItemStack.copyItemStack(itemstack2));
                itemstack1 = inventoryplayer.getItemStack();

                if (flag)
                {
                    itemstack1.stackSize = itemstack1.getMaxStackSize();
                }
            }
            else
            {
                inventoryplayer.setItemStack(null);
            }
        }
        else
        {
            inventorySlots.slotClick(slotIn == null ? slotId : slotIn.slotNumber, clickedButton, clickType, mc.thePlayer);

            if (Container.getDragEvent(clickedButton) == 2)
            {
                for (int i = 0; i < 9; ++i)
                {
                    mc.playerController.sendSlotPacket(inventorySlots.getSlot(45 + i).getStack(), 36 + i);
                }
            }
            else if (slotIn != null)
            {
                ItemStack itemstack4 = inventorySlots.getSlot(slotIn.slotNumber).getStack();
                mc.playerController.sendSlotPacket(itemstack4, slotIn.slotNumber - inventorySlots.inventorySlots.size() + 9 + 36);
            }
        }
    }

    protected void updateActivePotionEffects()
    {
        int i = guiLeft;
        super.updateActivePotionEffects();

        if (searchField != null && guiLeft != i)
        {
            searchField.xPosition = guiLeft + 82;
        }
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        if (mc.playerController.isInCreativeMode())
        {
            super.initGui();
            buttonList.clear();
            Keyboard.enableRepeatEvents(true);
            searchField = new GuiTextField(0, fontRendererObj, guiLeft + 82, guiTop + 6, 89, fontRendererObj.FONT_HEIGHT);
            searchField.setMaxStringLength(15);
            searchField.setEnableBackgroundDrawing(false);
            searchField.setVisible(false);
            searchField.setTextColor(16777215);
            int i = GuiContainerCreative.selectedTabIndex;
            GuiContainerCreative.selectedTabIndex = -1;
            setCurrentCreativeTab(CreativeTabs.creativeTabArray[i]);
            field_147059_E = new CreativeCrafting(mc);
            mc.thePlayer.inventoryContainer.onCraftGuiOpened(field_147059_E);
        }
        else
        {
            mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
        }
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        super.onGuiClosed();

        if (mc.thePlayer != null && mc.thePlayer.inventory != null)
        {
            mc.thePlayer.inventoryContainer.removeCraftingFromCrafters(field_147059_E);
        }

        Keyboard.enableRepeatEvents(false);
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (GuiContainerCreative.selectedTabIndex != CreativeTabs.tabAllSearch.getTabIndex())
        {
            if (GameSettings.isKeyDown(mc.gameSettings.keyBindChat))
            {
                setCurrentCreativeTab(CreativeTabs.tabAllSearch);
            }
            else
            {
                super.keyTyped(typedChar, keyCode);
            }
        }
        else
        {
            if (field_147057_D)
            {
                field_147057_D = false;
                searchField.setText("");
            }

            if (!checkHotbarKeys(keyCode))
            {
                if (searchField.textboxKeyTyped(typedChar, keyCode))
                {
                    updateCreativeSearch();
                }
                else
                {
                    super.keyTyped(typedChar, keyCode);
                }
            }
        }
    }

    private void updateCreativeSearch()
    {
        GuiContainerCreative.ContainerCreative guicontainercreative$containercreative = (GuiContainerCreative.ContainerCreative) inventorySlots;
        guicontainercreative$containercreative.itemList.clear();

        for (Item item : Item.itemRegistry)
        {
            if (item != null && item.getCreativeTab() != null)
            {
                item.getSubItems(item, null, guicontainercreative$containercreative.itemList);
            }
        }

        for (Enchantment enchantment : Enchantment.enchantmentsBookList)
        {
            if (enchantment != null && enchantment.type != null)
            {
                Items.enchanted_book.getAll(enchantment, guicontainercreative$containercreative.itemList);
            }
        }

        Iterator<ItemStack> iterator = guicontainercreative$containercreative.itemList.iterator();
        String s1 = searchField.getText().toLowerCase();

        while (iterator.hasNext())
        {
            ItemStack itemstack = iterator.next();
            boolean flag = false;

            for (String s : itemstack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips))
            {
                if (EnumChatFormatting.getTextWithoutFormattingCodes(s).toLowerCase().contains(s1))
                {
                    flag = true;
                    break;
                }
            }

            if (!flag)
            {
                iterator.remove();
            }
        }

        currentScroll = 0.0F;
        guicontainercreative$containercreative.scrollTo(0.0F);
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items). Args : mouseX, mouseY
     */
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        CreativeTabs creativetabs = CreativeTabs.creativeTabArray[GuiContainerCreative.selectedTabIndex];

        if (creativetabs.drawInForegroundOfTab())
        {
            GlStateManager.disableBlend();
            fontRendererObj.drawString(I18n.format(creativetabs.getTranslatedTabLabel()), 8, 6, 4210752);
        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (mouseButton == 0)
        {
            int i = mouseX - guiLeft;
            int j = mouseY - guiTop;

            for (CreativeTabs creativetabs : CreativeTabs.creativeTabArray)
            {
                if (func_147049_a(creativetabs, i, j))
                {
                    return;
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Called when a mouse button is released.  Args : mouseX, mouseY, releaseButton
     */
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (state == 0)
        {
            int i = mouseX - guiLeft;
            int j = mouseY - guiTop;

            for (CreativeTabs creativetabs : CreativeTabs.creativeTabArray)
            {
                if (func_147049_a(creativetabs, i, j))
                {
                    setCurrentCreativeTab(creativetabs);
                    return;
                }
            }
        }

        super.mouseReleased(mouseX, mouseY, state);
    }

    /**
     * returns (if you are not on the inventoryTab) and (the flag isn't set) and (you have more than 1 page of items)
     */
    private boolean needsScrollBars()
    {
        return GuiContainerCreative.selectedTabIndex != CreativeTabs.tabInventory.getTabIndex() && CreativeTabs.creativeTabArray[GuiContainerCreative.selectedTabIndex].shouldHidePlayerInventory() && ((GuiContainerCreative.ContainerCreative) inventorySlots).func_148328_e();
    }

    private void setCurrentCreativeTab(CreativeTabs p_147050_1_)
    {
        int i = GuiContainerCreative.selectedTabIndex;
        GuiContainerCreative.selectedTabIndex = p_147050_1_.getTabIndex();
        GuiContainerCreative.ContainerCreative guicontainercreative$containercreative = (GuiContainerCreative.ContainerCreative) inventorySlots;
        dragSplittingSlots.clear();
        guicontainercreative$containercreative.itemList.clear();
        p_147050_1_.displayAllReleventItems(guicontainercreative$containercreative.itemList);

        if (p_147050_1_ == CreativeTabs.tabInventory)
        {
            Container container = mc.thePlayer.inventoryContainer;

            if (field_147063_B == null)
            {
                field_147063_B = guicontainercreative$containercreative.inventorySlots;
            }

            guicontainercreative$containercreative.inventorySlots = Lists.newArrayList();

            for (int j = 0; j < container.inventorySlots.size(); ++j)
            {
                Slot slot = new GuiContainerCreative.CreativeSlot(container.inventorySlots.get(j), j);
                guicontainercreative$containercreative.inventorySlots.add(slot);

                if (j >= 5 && j < 9)
                {
                    int j1 = j - 5;
                    int k1 = j1 / 2;
                    int l1 = j1 % 2;
                    slot.xDisplayPosition = 9 + k1 * 54;
                    slot.yDisplayPosition = 6 + l1 * 27;
                }
                else if (j >= 0 && j < 5)
                {
                    slot.yDisplayPosition = -2000;
                    slot.xDisplayPosition = -2000;
                }
                else if (j < container.inventorySlots.size())
                {
                    int k = j - 9;
                    int l = k % 9;
                    int i1 = k / 9;
                    slot.xDisplayPosition = 9 + l * 18;

                    if (j >= 36)
                    {
                        slot.yDisplayPosition = 112;
                    }
                    else
                    {
                        slot.yDisplayPosition = 54 + i1 * 18;
                    }
                }
            }

            field_147064_C = new Slot(GuiContainerCreative.field_147060_v, 0, 173, 112);
            guicontainercreative$containercreative.inventorySlots.add(field_147064_C);
        }
        else if (i == CreativeTabs.tabInventory.getTabIndex())
        {
            guicontainercreative$containercreative.inventorySlots = field_147063_B;
            field_147063_B = null;
        }

        if (searchField != null)
        {
            if (p_147050_1_ == CreativeTabs.tabAllSearch)
            {
                searchField.setVisible(true);
                searchField.setCanLoseFocus(false);
                searchField.setFocused(true);
                searchField.setText("");
                updateCreativeSearch();
            }
            else
            {
                searchField.setVisible(false);
                searchField.setCanLoseFocus(true);
                searchField.setFocused(false);
            }
        }

        currentScroll = 0.0F;
        guicontainercreative$containercreative.scrollTo(0.0F);
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

        if (i != 0 && needsScrollBars())
        {
            int j = ((GuiContainerCreative.ContainerCreative) inventorySlots).itemList.size() / 9 - 5;

            if (i > 0)
            {
                i = 1;
            }

            if (i < 0)
            {
                i = -1;
            }

            currentScroll = (float)((double) currentScroll - (double)i / (double)j);
            currentScroll = MathHelper.clamp_float(currentScroll, 0.0F, 1.0F);
            ((GuiContainerCreative.ContainerCreative) inventorySlots).scrollTo(currentScroll);
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        boolean flag = Mouse.isButtonDown(0);
        int i = guiLeft;
        int j = guiTop;
        int k = i + 175;
        int l = j + 18;
        int i1 = k + 14;
        int j1 = l + 112;

        if (!wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1)
        {
            isScrolling = needsScrollBars();
        }

        if (!flag)
        {
            isScrolling = false;
        }

        wasClicking = flag;

        if (isScrolling)
        {
            currentScroll = ((float)(mouseY - l) - 7.5F) / ((float)(j1 - l) - 15.0F);
            currentScroll = MathHelper.clamp_float(currentScroll, 0.0F, 1.0F);
            ((GuiContainerCreative.ContainerCreative) inventorySlots).scrollTo(currentScroll);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        for (CreativeTabs creativetabs : CreativeTabs.creativeTabArray)
        {
            if (renderCreativeInventoryHoveringText(creativetabs, mouseX, mouseY))
            {
                break;
            }
        }

        if (field_147064_C != null && GuiContainerCreative.selectedTabIndex == CreativeTabs.tabInventory.getTabIndex() && isPointInRegion(field_147064_C.xDisplayPosition, field_147064_C.yDisplayPosition, 16, 16, mouseX, mouseY))
        {
            drawCreativeTabHoveringText(I18n.format("inventory.binSlot"), mouseX, mouseY);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
    }

    protected void renderToolTip(ItemStack stack, int x, int y)
    {
        if (GuiContainerCreative.selectedTabIndex == CreativeTabs.tabAllSearch.getTabIndex())
        {
            List<String> list = stack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
            CreativeTabs creativetabs = stack.getItem().getCreativeTab();

            if (creativetabs == null && stack.getItem() == Items.enchanted_book)
            {
                Map<Integer, Integer> map = EnchantmentHelper.getEnchantments(stack);

                if (map.size() == 1)
                {
                    Enchantment enchantment = Enchantment.getEnchantmentById(map.keySet().iterator().next().intValue());

                    for (CreativeTabs creativetabs1 : CreativeTabs.creativeTabArray)
                    {
                        if (creativetabs1.hasRelevantEnchantmentType(enchantment.type))
                        {
                            creativetabs = creativetabs1;
                            break;
                        }
                    }
                }
            }

            if (creativetabs != null)
            {
                list.add(1, "" + EnumChatFormatting.BOLD + EnumChatFormatting.BLUE + I18n.format(creativetabs.getTranslatedTabLabel()));
            }

            for (int i = 0; i < list.size(); ++i)
            {
                if (i == 0)
                {
                    list.set(i, stack.getRarity().rarityColor + list.get(i));
                }
                else
                {
                    list.set(i, EnumChatFormatting.GRAY + list.get(i));
                }
            }

            drawHoveringText(list, x, y);
        }
        else
        {
            super.renderToolTip(stack, x, y);
        }
    }

    /**
     * Args : renderPartialTicks, mouseX, mouseY
     */
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.enableGUIStandardItemLighting();
        CreativeTabs creativetabs = CreativeTabs.creativeTabArray[GuiContainerCreative.selectedTabIndex];

        for (CreativeTabs creativetabs1 : CreativeTabs.creativeTabArray)
        {
            mc.getTextureManager().bindTexture(GuiContainerCreative.creativeInventoryTabs);

            if (creativetabs1.getTabIndex() != GuiContainerCreative.selectedTabIndex)
            {
                func_147051_a(creativetabs1);
            }
        }

        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/creative_inventory/tab_" + creativetabs.getBackgroundImageName()));
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        searchField.drawTextBox();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int i = guiLeft + 175;
        int j = guiTop + 18;
        int k = j + 112;
        mc.getTextureManager().bindTexture(GuiContainerCreative.creativeInventoryTabs);

        if (creativetabs.shouldHidePlayerInventory())
        {
            drawTexturedModalRect(i, j + (int)((float)(k - j - 17) * currentScroll), 232 + (needsScrollBars() ? 0 : 12), 0, 12, 15);
        }

        func_147051_a(creativetabs);

        if (creativetabs == CreativeTabs.tabInventory)
        {
            GuiInventory.drawEntityOnScreen(guiLeft + 43, guiTop + 45, 20, (float)(guiLeft + 43 - mouseX), (float)(guiTop + 45 - 30 - mouseY), mc.thePlayer);
        }
    }

    protected boolean func_147049_a(CreativeTabs p_147049_1_, int p_147049_2_, int p_147049_3_)
    {
        int i = p_147049_1_.getTabColumn();
        int j = 28 * i;
        int k = 0;

        if (i == 5)
        {
            j = xSize - 28 + 2;
        }
        else if (i > 0)
        {
            j += i;
        }

        if (p_147049_1_.isTabInFirstRow())
        {
            k = k - 32;
        }
        else
        {
            k = k + ySize;
        }

        return p_147049_2_ >= j && p_147049_2_ <= j + 28 && p_147049_3_ >= k && p_147049_3_ <= k + 32;
    }

    /**
     * Renders the creative inventory hovering text if mouse is over it. Returns true if did render or false otherwise.
     * Params: current creative tab to be checked, current mouse x position, current mouse y position.
     */
    protected boolean renderCreativeInventoryHoveringText(CreativeTabs p_147052_1_, int p_147052_2_, int p_147052_3_)
    {
        int i = p_147052_1_.getTabColumn();
        int j = 28 * i;
        int k = 0;

        if (i == 5)
        {
            j = xSize - 28 + 2;
        }
        else if (i > 0)
        {
            j += i;
        }

        if (p_147052_1_.isTabInFirstRow())
        {
            k = k - 32;
        }
        else
        {
            k = k + ySize;
        }

        if (isPointInRegion(j + 3, k + 3, 23, 27, p_147052_2_, p_147052_3_))
        {
            drawCreativeTabHoveringText(I18n.format(p_147052_1_.getTranslatedTabLabel()), p_147052_2_, p_147052_3_);
            return true;
        }
        else
        {
            return false;
        }
    }

    protected void func_147051_a(CreativeTabs p_147051_1_)
    {
        boolean flag = p_147051_1_.getTabIndex() == GuiContainerCreative.selectedTabIndex;
        boolean flag1 = p_147051_1_.isTabInFirstRow();
        int i = p_147051_1_.getTabColumn();
        int j = i * 28;
        int k = 0;
        int l = guiLeft + 28 * i;
        int i1 = guiTop;
        int j1 = 32;

        if (flag)
        {
            k += 32;
        }

        if (i == 5)
        {
            l = guiLeft + xSize - 28;
        }
        else if (i > 0)
        {
            l += i;
        }

        if (flag1)
        {
            i1 = i1 - 28;
        }
        else
        {
            k += 64;
            i1 = i1 + (ySize - 4);
        }

        GlStateManager.disableLighting();
        drawTexturedModalRect(l, i1, j, k, 28, j1);
        zLevel = 100.0F;
        itemRender.zLevel = 100.0F;
        l = l + 6;
        i1 = i1 + 8 + (flag1 ? 1 : -1);
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();
        ItemStack itemstack = p_147051_1_.getIconItemStack();
        itemRender.renderItemAndEffectIntoGUI(itemstack, l, i1);
        itemRender.renderItemOverlays(fontRendererObj, itemstack, l, i1);
        GlStateManager.disableLighting();
        itemRender.zLevel = 0.0F;
        zLevel = 0.0F;
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            mc.displayGuiScreen(new GuiAchievements(this, mc.thePlayer.getStatFileWriter()));
        }

        if (button.id == 1)
        {
            mc.displayGuiScreen(new GuiStats(this, mc.thePlayer.getStatFileWriter()));
        }
    }

    public int getSelectedTabIndex()
    {
        return GuiContainerCreative.selectedTabIndex;
    }

    static class ContainerCreative extends Container
    {
        public List<ItemStack> itemList = Lists.newArrayList();

        public ContainerCreative(EntityPlayer p_i1086_1_)
        {
            InventoryPlayer inventoryplayer = p_i1086_1_.inventory;

            for (int i = 0; i < 5; ++i)
            {
                for (int j = 0; j < 9; ++j)
                {
                    addSlotToContainer(new Slot(GuiContainerCreative.field_147060_v, i * 9 + j, 9 + j * 18, 18 + i * 18));
                }
            }

            for (int k = 0; k < 9; ++k)
            {
                addSlotToContainer(new Slot(inventoryplayer, k, 9 + k * 18, 112));
            }

            scrollTo(0.0F);
        }

        public boolean canInteractWith(EntityPlayer playerIn)
        {
            return true;
        }

        public void scrollTo(float p_148329_1_)
        {
            int i = (itemList.size() + 9 - 1) / 9 - 5;
            int j = (int)((double)(p_148329_1_ * (float)i) + 0.5D);

            if (j < 0)
            {
                j = 0;
            }

            for (int k = 0; k < 5; ++k)
            {
                for (int l = 0; l < 9; ++l)
                {
                    int i1 = l + (k + j) * 9;

                    if (i1 >= 0 && i1 < itemList.size())
                    {
                        GuiContainerCreative.field_147060_v.setInventorySlotContents(l + k * 9, itemList.get(i1));
                    }
                    else
                    {
                        GuiContainerCreative.field_147060_v.setInventorySlotContents(l + k * 9, null);
                    }
                }
            }
        }

        public boolean func_148328_e()
        {
            return itemList.size() > 45;
        }

        protected void retrySlotClick(int slotId, int clickedButton, boolean mode, EntityPlayer playerIn)
        {
        }

        public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
        {
            if (index >= inventorySlots.size() - 9 && index < inventorySlots.size())
            {
                Slot slot = inventorySlots.get(index);

                if (slot != null && slot.getHasStack())
                {
                    slot.putStack(null);
                }
            }

            return null;
        }

        public boolean canMergeSlot(ItemStack stack, Slot p_94530_2_)
        {
            return p_94530_2_.yDisplayPosition > 90;
        }

        public boolean canDragIntoSlot(Slot p_94531_1_)
        {
            return p_94531_1_.inventory instanceof InventoryPlayer || p_94531_1_.yDisplayPosition > 90 && p_94531_1_.xDisplayPosition <= 162;
        }
    }

    class CreativeSlot extends Slot
    {
        private final Slot slot;

        public CreativeSlot(Slot p_i46313_2_, int p_i46313_3_)
        {
            super(p_i46313_2_.inventory, p_i46313_3_, 0, 0);
            slot = p_i46313_2_;
        }

        public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
        {
            slot.onPickupFromSlot(playerIn, stack);
        }

        public boolean isItemValid(ItemStack stack)
        {
            return slot.isItemValid(stack);
        }

        public ItemStack getStack()
        {
            return slot.getStack();
        }

        public boolean getHasStack()
        {
            return slot.getHasStack();
        }

        public void putStack(ItemStack stack)
        {
            slot.putStack(stack);
        }

        public void onSlotChanged()
        {
            slot.onSlotChanged();
        }

        public int getSlotStackLimit()
        {
            return slot.getSlotStackLimit();
        }

        public int getItemStackLimit(ItemStack stack)
        {
            return slot.getItemStackLimit(stack);
        }

        public String getSlotTexture()
        {
            return slot.getSlotTexture();
        }

        public ItemStack decrStackSize(int amount)
        {
            return slot.decrStackSize(amount);
        }

        public boolean isHere(IInventory inv, int slotIn)
        {
            return slot.isHere(inv, slotIn);
        }
    }
}
