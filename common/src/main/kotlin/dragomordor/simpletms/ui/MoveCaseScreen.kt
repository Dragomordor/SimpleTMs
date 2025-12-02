package dragomordor.simpletms.ui

import com.mojang.blaze3d.systems.RenderSystem
import dragomordor.simpletms.SimpleTMsItems
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack

/**
 * Client-side screen for the TM/TR Case.
 *
 * Features:
 * - Displays a grid of TM/TR slots (9 columns, 6 visible rows)
 * - Empty slots show a transparent "ghost" preview of the item
 * - Stored items appear solid and can be withdrawn
 * - Scrollbar for navigating through all available moves
 * - Search box for filtering moves by name and type tags (#fire, #physical)
 * - Filter toggle for owned/missing moves
 * - Tooltips for both ghost and real items
 */
class MoveCaseScreen(
    menu: MoveCaseMenu,
    playerInventory: Inventory,
    title: Component
) : AbstractContainerScreen<MoveCaseMenu>(menu, playerInventory, title) {

    companion object {
        // Use vanilla generic container texture as base
        private val TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png")

        // Ghost item transparency (0.0 = invisible, 1.0 = opaque)
        private const val GHOST_ALPHA = 0.35f

        // Scrollbar dimensions
        private const val SCROLLBAR_X = 174
        private const val SCROLLBAR_Y = 18
        private const val SCROLLBAR_WIDTH = 6
        private const val SCROLLBAR_HEIGHT = 108  // 6 rows * 18
        private const val SCROLLER_HEIGHT = 15

        // Layout
        private const val CASE_SLOTS_X = 8
        private const val CASE_SLOTS_Y = 18
        private const val SLOT_SIZE = 18
        private const val VISIBLE_ROWS = 6
        private const val COLUMNS = 9

        // Search box dimensions
        private const val SEARCH_BOX_WIDTH = 70
        private const val SEARCH_BOX_HEIGHT = 10

        // Filter toggle dimensions
        private const val FILTER_BUTTON_SIZE = 8
        private const val FILTER_BUTTON_MARGIN = 2
    }

    // Search box widget
    private lateinit var searchBox: EditBox

    // Filter button position (calculated in init)
    private var filterButtonX = 0
    private var filterButtonY = 0

    // Scroll state
    private var scrolling = false

    init {
        // Set the image dimensions
        imageWidth = 176
        imageHeight = 222  // 6 rows of case slots + player inventory

        // Adjust inventory label position
        inventoryLabelY = imageHeight - 94
    }

    override fun init() {
        super.init()

        // Title on the left like vanilla chests
        titleLabelX = 8
        titleLabelY = 6

        // Calculate positions
        filterButtonX = leftPos + 82
        filterButtonY = topPos + 5

        // Create search box to the right of filter button
        val searchX = filterButtonX + FILTER_BUTTON_SIZE + 4
        val searchY = topPos + 4

        searchBox = EditBox(
            font,
            searchX,
            searchY,
            SEARCH_BOX_WIDTH,
            SEARCH_BOX_HEIGHT,
            Component.translatable("gui.simpletms.search")
        )
        searchBox.setMaxLength(50)
        searchBox.setBordered(true)
        searchBox.setVisible(true)
        searchBox.setTextColor(0xFFFFFF)
        searchBox.setHint(Component.translatable("gui.simpletms.search.hint"))
        searchBox.setResponder { text ->
            menu.setSearchQuery(text)
        }

        // Restore previous search query if any
        searchBox.value = menu.getSearchQuery()

        addRenderableWidget(searchBox)
    }

    override fun resize(minecraft: net.minecraft.client.Minecraft, width: Int, height: Int) {
        val previousSearch = searchBox.value
        super.resize(minecraft, width, height)
        searchBox.value = previousSearch
        menu.setSearchQuery(previousSearch)
    }

    /**
     * Render the background (texture and ghost items).
     */
    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        // Draw the background texture
        val x = leftPos
        val y = topPos

        // Draw top part (case slots area) - use generic 54-slot chest texture
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, VISIBLE_ROWS * SLOT_SIZE + CASE_SLOTS_Y + 1)

        // Draw bottom part (player inventory) - from the chest texture
        guiGraphics.blit(TEXTURE, x, y + VISIBLE_ROWS * SLOT_SIZE + CASE_SLOTS_Y + 1,
            0, 126, imageWidth, 96)

        // Draw filter toggle button
        renderFilterButton(guiGraphics, mouseX, mouseY)

        // Draw case slot items (ghost for empty, solid for stored)
        renderCaseSlotItems(guiGraphics, x, y)

        // Draw scrollbar
        renderScrollbar(guiGraphics, x, y)

        // Draw highlight for hovered case slot
        renderCaseSlotHighlight(guiGraphics, x, y, mouseX, mouseY)
    }

    /**
     * Render the filter toggle button.
     */
    private fun renderFilterButton(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val mode = menu.getFilterMode()
        val isHovered = isOverFilterButton(mouseX.toDouble(), mouseY.toDouble())

        // Button background
        val bgColor = if (isHovered) 0xFF555555.toInt() else 0xFF333333.toInt()
        guiGraphics.fill(
            filterButtonX, filterButtonY,
            filterButtonX + FILTER_BUTTON_SIZE, filterButtonY + FILTER_BUTTON_SIZE,
            bgColor
        )

        // Button border
        val borderColor = 0xFF888888.toInt()
        guiGraphics.renderOutline(
            filterButtonX, filterButtonY,
            FILTER_BUTTON_SIZE, FILTER_BUTTON_SIZE,
            borderColor
        )

        // Inner indicator based on mode
        val innerMargin = 2
        when (mode) {
            FilterMode.ALL -> {
                // Empty circle outline (no fill)
            }
            FilterMode.OWNED_ONLY -> {
                // Filled green (owned)
                guiGraphics.fill(
                    filterButtonX + innerMargin, filterButtonY + innerMargin,
                    filterButtonX + FILTER_BUTTON_SIZE - innerMargin, filterButtonY + FILTER_BUTTON_SIZE - innerMargin,
                    0xFF55FF55.toInt()
                )
            }
            FilterMode.MISSING_ONLY -> {
                // Filled red (missing)
                guiGraphics.fill(
                    filterButtonX + innerMargin, filterButtonY + innerMargin,
                    filterButtonX + FILTER_BUTTON_SIZE - innerMargin, filterButtonY + FILTER_BUTTON_SIZE - innerMargin,
                    0xFFFF5555.toInt()
                )
            }
        }
    }

    /**
     * Check if mouse is over the filter button.
     */
    private fun isOverFilterButton(mouseX: Double, mouseY: Double): Boolean {
        return mouseX >= filterButtonX && mouseX < filterButtonX + FILTER_BUTTON_SIZE &&
                mouseY >= filterButtonY && mouseY < filterButtonY + FILTER_BUTTON_SIZE
    }

    /**
     * Render items in case slots - ghost for empty, solid for stored.
     */
    private fun renderCaseSlotItems(guiGraphics: GuiGraphics, x: Int, y: Int) {
        val scrollRow = menu.getScrollRow()
        val filteredMoves = menu.getFilteredMoves()

        for (row in 0 until VISIBLE_ROWS) {
            for (col in 0 until COLUMNS) {
                val moveIndex = (scrollRow + row) * COLUMNS + col
                if (moveIndex >= filteredMoves.size) continue

                val moveName = filteredMoves[moveIndex]
                val isStored = menu.isMoveStored(moveName)

                val slotX = x + CASE_SLOTS_X + col * SLOT_SIZE + 1
                val slotY = y + CASE_SLOTS_Y + row * SLOT_SIZE + 1

                val itemStack = getItemStackForMove(moveName)

                if (isStored) {
                    // Render solid item (stored)
                    guiGraphics.renderItem(itemStack, slotX, slotY)
                    guiGraphics.renderItemDecorations(font, itemStack, slotX, slotY)
                } else {
                    // Render ghost item (not stored)
                    renderGhostItem(guiGraphics, itemStack, slotX, slotY)
                }
            }
        }
    }

    /**
     * Render a single ghost item with transparency.
     */
    private fun renderGhostItem(guiGraphics: GuiGraphics, stack: ItemStack, x: Int, y: Int) {
        if (stack.isEmpty) return

        // Set transparency
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, GHOST_ALPHA)

        // Render the item
        guiGraphics.renderItem(stack, x, y)

        // Reset color to avoid affecting other renders
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.disableBlend()
    }

    /**
     * Render highlight for hovered case slot.
     */
    private fun renderCaseSlotHighlight(guiGraphics: GuiGraphics, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        val relX = mouseX - x - CASE_SLOTS_X
        val relY = mouseY - y - CASE_SLOTS_Y

        val slotPos = menu.getCaseSlotAt(relX, relY) ?: return
        val (row, col) = slotPos

        val slotX = x + CASE_SLOTS_X + col * SLOT_SIZE
        val slotY = y + CASE_SLOTS_Y + row * SLOT_SIZE

        // Draw slot highlight (same as vanilla)
        guiGraphics.fillGradient(
            slotX + 1, slotY + 1,
            slotX + 17, slotY + 17,
            0x80FFFFFF.toInt(), 0x80FFFFFF.toInt()
        )
    }

    /**
     * Get the item stack for a move.
     */
    private fun getItemStackForMove(moveName: String): ItemStack {
        val prefix = if (menu.isTR) "tr_" else "tm_"
        return SimpleTMsItems.getItemStackFromName(prefix + moveName)
    }

    /**
     * Render the scrollbar.
     */
    private fun renderScrollbar(guiGraphics: GuiGraphics, x: Int, y: Int) {
        val maxScroll = menu.getMaxScrollRow()

        if (maxScroll <= 0) {
            // No scrolling needed - draw disabled scrollbar
            guiGraphics.fill(
                x + SCROLLBAR_X,
                y + SCROLLBAR_Y,
                x + SCROLLBAR_X + SCROLLBAR_WIDTH,
                y + SCROLLBAR_Y + SCROLLBAR_HEIGHT,
                0xFF373737.toInt()
            )
            return
        }

        // Draw scrollbar track
        guiGraphics.fill(
            x + SCROLLBAR_X,
            y + SCROLLBAR_Y,
            x + SCROLLBAR_X + SCROLLBAR_WIDTH,
            y + SCROLLBAR_Y + SCROLLBAR_HEIGHT,
            0xFF000000.toInt()
        )

        // Calculate scroller position
        val scrollProgress = menu.getScrollRow().toFloat() / maxScroll.toFloat()
        val scrollerY = (SCROLLBAR_HEIGHT - SCROLLER_HEIGHT) * scrollProgress

        // Draw scroller
        guiGraphics.fill(
            x + SCROLLBAR_X,
            y + SCROLLBAR_Y + scrollerY.toInt(),
            x + SCROLLBAR_X + SCROLLBAR_WIDTH,
            y + SCROLLBAR_Y + scrollerY.toInt() + SCROLLER_HEIGHT,
            0xFFC6C6C6.toInt()
        )
    }

    /**
     * Render labels - override to not render title if search box overlaps
     */
    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        // Render title on the left
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false)
        // Render inventory label
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false)
    }

    /**
     * Render method - handles tooltips.
     */
    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        // Render tooltip for hovered slot (player inventory handled by parent)
        renderTooltip(guiGraphics, mouseX, mouseY)

        // Render tooltip for filter button
        if (isOverFilterButton(mouseX.toDouble(), mouseY.toDouble())) {
            renderFilterButtonTooltip(guiGraphics, mouseX, mouseY)
        }
        // Render tooltip for case slots (only if not hovering over search box or filter button)
        else if (!searchBox.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
            renderCaseSlotTooltip(guiGraphics, mouseX, mouseY)
        }
    }

    /**
     * Render tooltip for the filter button.
     */
    private fun renderFilterButtonTooltip(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val mode = menu.getFilterMode()
        val tooltipKey = when (mode) {
            FilterMode.ALL -> "gui.simpletms.filter.all"
            FilterMode.OWNED_ONLY -> "gui.simpletms.filter.owned"
            FilterMode.MISSING_ONLY -> "gui.simpletms.filter.missing"
        }
        guiGraphics.renderTooltip(font, Component.translatable(tooltipKey), mouseX, mouseY)
    }

    /**
     * Render tooltip for case slots.
     */
    private fun renderCaseSlotTooltip(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val relX = mouseX - leftPos - CASE_SLOTS_X
        val relY = mouseY - topPos - CASE_SLOTS_Y

        val slotPos = menu.getCaseSlotAt(relX, relY) ?: return
        val (row, col) = slotPos

        val visibleIndex = row * COLUMNS + col
        val moveName = menu.getMoveNameForVisibleSlot(visibleIndex) ?: return

        val itemStack = getItemStackForMove(moveName)
        if (!itemStack.isEmpty) {
            guiGraphics.renderTooltip(font, itemStack, mouseX, mouseY)
        }
    }

    /**
     * Handle key presses - allow typing in search box.
     */
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        // If search box is focused, let it handle the key
        if (searchBox.isFocused) {
            if (keyCode == 256) { // Escape
                searchBox.setFocused(false)
                return true
            }
            return searchBox.keyPressed(keyCode, scanCode, modifiers)
        }

        // Focus search box when typing starts (like creative menu)
        if (searchBox.canConsumeInput()) {
            return searchBox.keyPressed(keyCode, scanCode, modifiers)
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    /**
     * Handle character typing.
     */
    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        // If search box is focused or can consume input, send to search box
        if (searchBox.isFocused || searchBox.canConsumeInput()) {
            return searchBox.charTyped(codePoint, modifiers)
        }

        // Start typing in search box when pressing a character
        if (Character.isLetterOrDigit(codePoint) || codePoint == '#') {
            searchBox.setFocused(true)
            return searchBox.charTyped(codePoint, modifiers)
        }

        return super.charTyped(codePoint, modifiers)
    }

    /**
     * Handle mouse clicks.
     */
    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        // Check if clicking on filter button
        if (button == 0 && isOverFilterButton(mouseX, mouseY)) {
            menu.cycleFilterMode()
            return true
        }

        // Check if clicking on search box
        if (searchBox.mouseClicked(mouseX, mouseY, button)) {
            setFocused(searchBox)
            return true
        }

        // Unfocus search box when clicking elsewhere
        if (searchBox.isFocused && !searchBox.isMouseOver(mouseX, mouseY)) {
            searchBox.setFocused(false)
        }

        // Check if clicking on scrollbar
        if (button == 0 && isOverScrollbar(mouseX, mouseY)) {
            scrolling = true
            updateScrollFromMouse(mouseY)
            return true
        }

        // Check if clicking on case slot area
        if (button == 0 || button == 1) {
            val relX = mouseX.toInt() - leftPos - CASE_SLOTS_X
            val relY = mouseY.toInt() - topPos - CASE_SLOTS_Y

            val slotPos = menu.getCaseSlotAt(relX, relY)
            if (slotPos != null) {
                val (row, col) = slotPos
                val clickType = if (hasShiftDown()) ClickType.QUICK_MOVE else ClickType.PICKUP
                menu.handleCaseSlotClick(row, col, clickType)
                return true
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    /**
     * Handle mouse scrolling.
     */
    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        // Don't scroll if mouse is over search box
        if (searchBox.isMouseOver(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
        }

        if (menu.getMaxScrollRow() > 0) {
            // Scroll up (positive delta) or down (negative delta)
            val scrollAmount = -scrollY.toInt().coerceIn(-1, 1)
            menu.scroll(scrollAmount)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    /**
     * Handle mouse release.
     */
    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            scrolling = false
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    /**
     * Handle mouse drag for scrollbar.
     */
    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (scrolling) {
            updateScrollFromMouse(mouseY)
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    /**
     * Check if mouse is over the scrollbar.
     */
    private fun isOverScrollbar(mouseX: Double, mouseY: Double): Boolean {
        val x = leftPos + SCROLLBAR_X
        val y = topPos + SCROLLBAR_Y
        return mouseX >= x && mouseX < x + SCROLLBAR_WIDTH &&
                mouseY >= y && mouseY < y + SCROLLBAR_HEIGHT
    }

    /**
     * Update scroll position based on mouse Y position.
     */
    private fun updateScrollFromMouse(mouseY: Double) {
        val scrollAreaTop = topPos + SCROLLBAR_Y
        val scrollAreaHeight = SCROLLBAR_HEIGHT - SCROLLER_HEIGHT

        val relativeY = (mouseY - scrollAreaTop - SCROLLER_HEIGHT / 2).coerceIn(0.0, scrollAreaHeight.toDouble())
        val scrollProgress = relativeY / scrollAreaHeight

        val targetRow = (scrollProgress * menu.getMaxScrollRow()).toInt()
        menu.setScrollRow(targetRow)
    }
}