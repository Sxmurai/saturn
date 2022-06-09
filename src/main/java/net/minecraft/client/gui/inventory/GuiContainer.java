package net.minecraft.client.gui.inventory;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

public abstract class GuiContainer extends GuiScreen
{
    /** The location of the inventory background texture */
    protected static final ResourceLocation inventoryBackground = new ResourceLocation("textures/gui/container/inventory.png");

    /** The X size of the inventory window in pixels. */
    protected int xSize = 176;

    /** The Y size of the inventory window in pixels. */
    protected int ySize = 166;

    /** A list of the players inventory slots */
    public Container inventorySlots;

    /**
     * Starting X position for the Gui. Inconsistent use for Gui backgrounds.
     */
    protected int guiLeft;

    /**
     * Starting Y position for the Gui. Inconsistent use for Gui backgrounds.
     */
    protected int guiTop;

    /** holds the slot currently hovered */
    private Slot theSlot;

    /** Used when touchscreen is enabled. */
    private Slot clickedSlot;

    /** Used when touchscreen is enabled. */
    private boolean isRightMouseClick;

    /** Used when touchscreen is enabled */
    private ItemStack draggedStack;
    private int touchUpX;
    private int touchUpY;
    private Slot returningStackDestSlot;
    private long returningStackTime;

    /** Used when touchscreen is enabled */
    private ItemStack returningStack;
    private Slot currentDragTargetSlot;
    private long dragItemDropDelay;
    protected final Set<Slot> dragSplittingSlots = Sets.newHashSet();
    protected boolean dragSplitting;
    private int dragSplittingLimit;
    private int dragSplittingButton;
    private boolean ignoreMouseUp;
    private int dragSplittingRemnant;
    private long lastClickTime;
    private Slot lastClickSlot;
    private int lastClickButton;
    private boolean doubleClick;
    private ItemStack shiftClickedSlot;

    public GuiContainer(Container inventorySlotsIn)
    {
        inventorySlots = inventorySlotsIn;
        ignoreMouseUp = true;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        super.initGui();
        mc.thePlayer.openContainer = inventorySlots;
        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize) / 2;
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        int i = guiLeft;
        int j = guiTop;
        drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        super.drawScreen(mouseX, mouseY, partialTicks);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)i, (float)j, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        theSlot = null;
        int k = 240;
        int l = 240;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)k / 1.0F, (float)l / 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i1 = 0; i1 < inventorySlots.inventorySlots.size(); ++i1)
        {
            Slot slot = inventorySlots.inventorySlots.get(i1);
            drawSlot(slot);

            if (isMouseOverSlot(slot, mouseX, mouseY) && slot.canBeHovered())
            {
                theSlot = slot;
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                int j1 = slot.xDisplayPosition;
                int k1 = slot.yDisplayPosition;
                GlStateManager.colorMask(true, true, true, false);
                drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
                GlStateManager.colorMask(true, true, true, true);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }

        RenderHelper.disableStandardItemLighting();
        drawGuiContainerForegroundLayer(mouseX, mouseY);
        RenderHelper.enableGUIStandardItemLighting();
        InventoryPlayer inventoryplayer = mc.thePlayer.inventory;
        ItemStack itemstack = draggedStack == null ? inventoryplayer.getItemStack() : draggedStack;

        if (itemstack != null)
        {
            int j2 = 8;
            int k2 = draggedStack == null ? 8 : 16;
            String s = null;

            if (draggedStack != null && isRightMouseClick)
            {
                itemstack = itemstack.copy();
                itemstack.stackSize = MathHelper.ceiling_float_int((float)itemstack.stackSize / 2.0F);
            }
            else if (dragSplitting && dragSplittingSlots.size() > 1)
            {
                itemstack = itemstack.copy();
                itemstack.stackSize = dragSplittingRemnant;

                if (itemstack.stackSize == 0)
                {
                    s = "" + EnumChatFormatting.YELLOW + "0";
                }
            }

            drawItemStack(itemstack, mouseX - i - j2, mouseY - j - k2, s);
        }

        if (returningStack != null)
        {
            float f = (float)(Minecraft.getSystemTime() - returningStackTime) / 100.0F;

            if (f >= 1.0F)
            {
                f = 1.0F;
                returningStack = null;
            }

            int l2 = returningStackDestSlot.xDisplayPosition - touchUpX;
            int i3 = returningStackDestSlot.yDisplayPosition - touchUpY;
            int l1 = touchUpX + (int)((float)l2 * f);
            int i2 = touchUpY + (int)((float)i3 * f);
            drawItemStack(returningStack, l1, i2, null);
        }

        GlStateManager.popMatrix();

        if (inventoryplayer.getItemStack() == null && theSlot != null && theSlot.getHasStack())
        {
            ItemStack itemstack1 = theSlot.getStack();
            renderToolTip(itemstack1, mouseX, mouseY);
        }

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
    }

    /**
     * Render an ItemStack. Args : stack, x, y, format
     */
    private void drawItemStack(ItemStack stack, int x, int y, String altText)
    {
        GlStateManager.translate(0.0F, 0.0F, 32.0F);
        zLevel = 200.0F;
        itemRender.zLevel = 200.0F;
        itemRender.renderItemAndEffectIntoGUI(stack, x, y);
        itemRender.renderItemOverlayIntoGUI(fontRendererObj, stack, x, y - (draggedStack == null ? 0 : 8), altText);
        zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items). Args : mouseX, mouseY
     */
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
    }

    /**
     * Args : renderPartialTicks, mouseX, mouseY
     */
    protected abstract void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY);

    private void drawSlot(Slot slotIn)
    {
        int i = slotIn.xDisplayPosition;
        int j = slotIn.yDisplayPosition;
        ItemStack itemstack = slotIn.getStack();
        boolean flag = false;
        boolean flag1 = slotIn == clickedSlot && draggedStack != null && !isRightMouseClick;
        ItemStack itemstack1 = mc.thePlayer.inventory.getItemStack();
        String s = null;

        if (slotIn == clickedSlot && draggedStack != null && isRightMouseClick && itemstack != null)
        {
            itemstack = itemstack.copy();
            itemstack.stackSize /= 2;
        }
        else if (dragSplitting && dragSplittingSlots.contains(slotIn) && itemstack1 != null)
        {
            if (dragSplittingSlots.size() == 1)
            {
                return;
            }

            if (Container.canAddItemToSlot(slotIn, itemstack1, true) && inventorySlots.canDragIntoSlot(slotIn))
            {
                itemstack = itemstack1.copy();
                flag = true;
                Container.computeStackSize(dragSplittingSlots, dragSplittingLimit, itemstack, slotIn.getStack() == null ? 0 : slotIn.getStack().stackSize);

                if (itemstack.stackSize > itemstack.getMaxStackSize())
                {
                    s = EnumChatFormatting.YELLOW + "" + itemstack.getMaxStackSize();
                    itemstack.stackSize = itemstack.getMaxStackSize();
                }

                if (itemstack.stackSize > slotIn.getItemStackLimit(itemstack))
                {
                    s = EnumChatFormatting.YELLOW + "" + slotIn.getItemStackLimit(itemstack);
                    itemstack.stackSize = slotIn.getItemStackLimit(itemstack);
                }
            }
            else
            {
                dragSplittingSlots.remove(slotIn);
                updateDragSplitting();
            }
        }

        zLevel = 100.0F;
        itemRender.zLevel = 100.0F;

        if (itemstack == null)
        {
            String s1 = slotIn.getSlotTexture();

            if (s1 != null)
            {
                TextureAtlasSprite textureatlassprite = mc.getTextureMapBlocks().getAtlasSprite(s1);
                GlStateManager.disableLighting();
                mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
                drawTexturedModalRect(i, j, textureatlassprite, 16, 16);
                GlStateManager.enableLighting();
                flag1 = true;
            }
        }

        if (!flag1)
        {
            if (flag)
            {
                Gui.drawRect(i, j, i + 16, j + 16, -2130706433);
            }

            GlStateManager.enableDepth();
            itemRender.renderItemAndEffectIntoGUI(itemstack, i, j);
            itemRender.renderItemOverlayIntoGUI(fontRendererObj, itemstack, i, j, s);
        }

        itemRender.zLevel = 0.0F;
        zLevel = 0.0F;
    }

    private void updateDragSplitting()
    {
        ItemStack itemstack = mc.thePlayer.inventory.getItemStack();

        if (itemstack != null && dragSplitting)
        {
            dragSplittingRemnant = itemstack.stackSize;

            for (Slot slot : dragSplittingSlots)
            {
                ItemStack itemstack1 = itemstack.copy();
                int i = slot.getStack() == null ? 0 : slot.getStack().stackSize;
                Container.computeStackSize(dragSplittingSlots, dragSplittingLimit, itemstack1, i);

                if (itemstack1.stackSize > itemstack1.getMaxStackSize())
                {
                    itemstack1.stackSize = itemstack1.getMaxStackSize();
                }

                if (itemstack1.stackSize > slot.getItemStackLimit(itemstack1))
                {
                    itemstack1.stackSize = slot.getItemStackLimit(itemstack1);
                }

                dragSplittingRemnant -= itemstack1.stackSize - i;
            }
        }
    }

    /**
     * Returns the slot at the given coordinates or null if there is none.
     */
    private Slot getSlotAtPosition(int x, int y)
    {
        for (int i = 0; i < inventorySlots.inventorySlots.size(); ++i)
        {
            Slot slot = inventorySlots.inventorySlots.get(i);

            if (isMouseOverSlot(slot, x, y))
            {
                return slot;
            }
        }

        return null;
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        boolean flag = mouseButton == mc.gameSettings.keyBindPickBlock.getKeyCode() + 100;
        Slot slot = getSlotAtPosition(mouseX, mouseY);
        long i = Minecraft.getSystemTime();
        doubleClick = lastClickSlot == slot && i - lastClickTime < 250L && lastClickButton == mouseButton;
        ignoreMouseUp = false;

        if (mouseButton == 0 || mouseButton == 1 || flag)
        {
            int j = guiLeft;
            int k = guiTop;
            boolean flag1 = mouseX < j || mouseY < k || mouseX >= j + xSize || mouseY >= k + ySize;
            int l = -1;

            if (slot != null)
            {
                l = slot.slotNumber;
            }

            if (flag1)
            {
                l = -999;
            }

            if (mc.gameSettings.touchscreen && flag1 && mc.thePlayer.inventory.getItemStack() == null)
            {
                mc.displayGuiScreen(null);
                return;
            }

            if (l != -1)
            {
                if (mc.gameSettings.touchscreen)
                {
                    if (slot != null && slot.getHasStack())
                    {
                        clickedSlot = slot;
                        draggedStack = null;
                        isRightMouseClick = mouseButton == 1;
                    }
                    else
                    {
                        clickedSlot = null;
                    }
                }
                else if (!dragSplitting)
                {
                    if (mc.thePlayer.inventory.getItemStack() == null)
                    {
                        if (mouseButton == mc.gameSettings.keyBindPickBlock.getKeyCode() + 100)
                        {
                            handleMouseClick(slot, l, mouseButton, 3);
                        }
                        else
                        {
                            boolean flag2 = l != -999 && (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));
                            int i1 = 0;

                            if (flag2)
                            {
                                shiftClickedSlot = slot != null && slot.getHasStack() ? slot.getStack() : null;
                                i1 = 1;
                            }
                            else if (l == -999)
                            {
                                i1 = 4;
                            }

                            handleMouseClick(slot, l, mouseButton, i1);
                        }

                        ignoreMouseUp = true;
                    }
                    else
                    {
                        dragSplitting = true;
                        dragSplittingButton = mouseButton;
                        dragSplittingSlots.clear();

                        if (mouseButton == 0)
                        {
                            dragSplittingLimit = 0;
                        }
                        else if (mouseButton == 1)
                        {
                            dragSplittingLimit = 1;
                        }
                        else if (mouseButton == mc.gameSettings.keyBindPickBlock.getKeyCode() + 100)
                        {
                            dragSplittingLimit = 2;
                        }
                    }
                }
            }
        }

        lastClickSlot = slot;
        lastClickTime = i;
        lastClickButton = mouseButton;
    }

    /**
     * Called when a mouse button is pressed and the mouse is moved around. Parameters are : mouseX, mouseY,
     * lastButtonClicked & timeSinceMouseClick.
     */
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        Slot slot = getSlotAtPosition(mouseX, mouseY);
        ItemStack itemstack = mc.thePlayer.inventory.getItemStack();

        if (clickedSlot != null && mc.gameSettings.touchscreen)
        {
            if (clickedMouseButton == 0 || clickedMouseButton == 1)
            {
                if (draggedStack == null)
                {
                    if (slot != clickedSlot && clickedSlot.getStack() != null)
                    {
                        draggedStack = clickedSlot.getStack().copy();
                    }
                }
                else if (draggedStack.stackSize > 1 && slot != null && Container.canAddItemToSlot(slot, draggedStack, false))
                {
                    long i = Minecraft.getSystemTime();

                    if (currentDragTargetSlot == slot)
                    {
                        if (i - dragItemDropDelay > 500L)
                        {
                            handleMouseClick(clickedSlot, clickedSlot.slotNumber, 0, 0);
                            handleMouseClick(slot, slot.slotNumber, 1, 0);
                            handleMouseClick(clickedSlot, clickedSlot.slotNumber, 0, 0);
                            dragItemDropDelay = i + 750L;
                            --draggedStack.stackSize;
                        }
                    }
                    else
                    {
                        currentDragTargetSlot = slot;
                        dragItemDropDelay = i;
                    }
                }
            }
        }
        else if (dragSplitting && slot != null && itemstack != null && itemstack.stackSize > dragSplittingSlots.size() && Container.canAddItemToSlot(slot, itemstack, true) && slot.isItemValid(itemstack) && inventorySlots.canDragIntoSlot(slot))
        {
            dragSplittingSlots.add(slot);
            updateDragSplitting();
        }
    }

    /**
     * Called when a mouse button is released.  Args : mouseX, mouseY, releaseButton
     */
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        Slot slot = getSlotAtPosition(mouseX, mouseY);
        int i = guiLeft;
        int j = guiTop;
        boolean flag = mouseX < i || mouseY < j || mouseX >= i + xSize || mouseY >= j + ySize;
        int k = -1;

        if (slot != null)
        {
            k = slot.slotNumber;
        }

        if (flag)
        {
            k = -999;
        }

        if (doubleClick && slot != null && state == 0 && inventorySlots.canMergeSlot(null, slot))
        {
            if (GuiScreen.isShiftKeyDown())
            {
                if (slot != null && slot.inventory != null && shiftClickedSlot != null)
                {
                    for (Slot slot2 : inventorySlots.inventorySlots)
                    {
                        if (slot2 != null && slot2.canTakeStack(mc.thePlayer) && slot2.getHasStack() && slot2.inventory == slot.inventory && Container.canAddItemToSlot(slot2, shiftClickedSlot, true))
                        {
                            handleMouseClick(slot2, slot2.slotNumber, state, 1);
                        }
                    }
                }
            }
            else
            {
                handleMouseClick(slot, k, state, 6);
            }

            doubleClick = false;
            lastClickTime = 0L;
        }
        else
        {
            if (dragSplitting && dragSplittingButton != state)
            {
                dragSplitting = false;
                dragSplittingSlots.clear();
                ignoreMouseUp = true;
                return;
            }

            if (ignoreMouseUp)
            {
                ignoreMouseUp = false;
                return;
            }

            if (clickedSlot != null && mc.gameSettings.touchscreen)
            {
                if (state == 0 || state == 1)
                {
                    if (draggedStack == null && slot != clickedSlot)
                    {
                        draggedStack = clickedSlot.getStack();
                    }

                    boolean flag2 = Container.canAddItemToSlot(slot, draggedStack, false);

                    if (k != -1 && draggedStack != null && flag2)
                    {
                        handleMouseClick(clickedSlot, clickedSlot.slotNumber, state, 0);
                        handleMouseClick(slot, k, 0, 0);

                        if (mc.thePlayer.inventory.getItemStack() != null)
                        {
                            handleMouseClick(clickedSlot, clickedSlot.slotNumber, state, 0);
                            touchUpX = mouseX - i;
                            touchUpY = mouseY - j;
                            returningStackDestSlot = clickedSlot;
                            returningStack = draggedStack;
                            returningStackTime = Minecraft.getSystemTime();
                        }
                        else
                        {
                            returningStack = null;
                        }
                    }
                    else if (draggedStack != null)
                    {
                        touchUpX = mouseX - i;
                        touchUpY = mouseY - j;
                        returningStackDestSlot = clickedSlot;
                        returningStack = draggedStack;
                        returningStackTime = Minecraft.getSystemTime();
                    }

                    draggedStack = null;
                    clickedSlot = null;
                }
            }
            else if (dragSplitting && !dragSplittingSlots.isEmpty())
            {
                handleMouseClick(null, -999, Container.func_94534_d(0, dragSplittingLimit), 5);

                for (Slot slot1 : dragSplittingSlots)
                {
                    handleMouseClick(slot1, slot1.slotNumber, Container.func_94534_d(1, dragSplittingLimit), 5);
                }

                handleMouseClick(null, -999, Container.func_94534_d(2, dragSplittingLimit), 5);
            }
            else if (mc.thePlayer.inventory.getItemStack() != null)
            {
                if (state == mc.gameSettings.keyBindPickBlock.getKeyCode() + 100)
                {
                    handleMouseClick(slot, k, state, 3);
                }
                else
                {
                    boolean flag1 = k != -999 && (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));

                    if (flag1)
                    {
                        shiftClickedSlot = slot != null && slot.getHasStack() ? slot.getStack() : null;
                    }

                    handleMouseClick(slot, k, state, flag1 ? 1 : 0);
                }
            }
        }

        if (mc.thePlayer.inventory.getItemStack() == null)
        {
            lastClickTime = 0L;
        }

        dragSplitting = false;
    }

    /**
     * Returns if the passed mouse position is over the specified slot. Args : slot, mouseX, mouseY
     */
    private boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY)
    {
        return isPointInRegion(slotIn.xDisplayPosition, slotIn.yDisplayPosition, 16, 16, mouseX, mouseY);
    }

    /**
     * Test if the 2D point is in a rectangle (relative to the GUI). Args : rectX, rectY, rectWidth, rectHeight, pointX,
     * pointY
     */
    protected boolean isPointInRegion(int left, int top, int right, int bottom, int pointX, int pointY)
    {
        int i = guiLeft;
        int j = guiTop;
        pointX = pointX - i;
        pointY = pointY - j;
        return pointX >= left - 1 && pointX < left + right + 1 && pointY >= top - 1 && pointY < top + bottom + 1;
    }

    /**
     * Called when the mouse is clicked over a slot or outside the gui.
     */
    protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType)
    {
        if (slotIn != null)
        {
            slotId = slotIn.slotNumber;
        }

        mc.playerController.windowClick(inventorySlots.windowId, slotId, clickedButton, clickType, mc.thePlayer);
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 1 || keyCode == mc.gameSettings.keyBindInventory.getKeyCode())
        {
            mc.thePlayer.closeScreen();
        }

        checkHotbarKeys(keyCode);

        if (theSlot != null && theSlot.getHasStack())
        {
            if (keyCode == mc.gameSettings.keyBindPickBlock.getKeyCode())
            {
                handleMouseClick(theSlot, theSlot.slotNumber, 0, 3);
            }
            else if (keyCode == mc.gameSettings.keyBindDrop.getKeyCode())
            {
                handleMouseClick(theSlot, theSlot.slotNumber, GuiScreen.isCtrlKeyDown() ? 1 : 0, 4);
            }
        }
    }

    /**
     * This function is what controls the hotbar shortcut check when you press a number key when hovering a stack. Args
     * : keyCode, Returns true if a Hotbar key is pressed, else false
     */
    protected boolean checkHotbarKeys(int keyCode)
    {
        if (mc.thePlayer.inventory.getItemStack() == null && theSlot != null)
        {
            for (int i = 0; i < 9; ++i)
            {
                if (keyCode == mc.gameSettings.keyBindsHotbar[i].getKeyCode())
                {
                    handleMouseClick(theSlot, theSlot.slotNumber, i, 2);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        if (mc.thePlayer != null)
        {
            inventorySlots.onContainerClosed(mc.thePlayer);
        }
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        super.updateScreen();

        if (!mc.thePlayer.isEntityAlive() || mc.thePlayer.isDead)
        {
            mc.thePlayer.closeScreen();
        }
    }
}
