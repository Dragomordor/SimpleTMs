package dragomordor.simpletms.ui

import com.mojang.blaze3d.systems.RenderSystem
import dragomordor.simpletms.SimpleTMsItems
import dragomordor.simpletms.network.SimpleTMsNetwork
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack

/**
 * Client-side screen for the TM/TR Case.
 */
class MoveCaseScreen(
    menu: MoveCaseMenu,
    playerInventory: Inventory,
    title: Component
) : AbstractContainerScreen<MoveCaseMenu>(menu, playerInventory, title) {

    companion object {
        private val TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png")
        private const val GHOST_ALPHA = 0.35f
        private const val SCROLLBAR_X = 174
        private const val SCROLLBAR_Y = 18
        private const val SCROLLBAR_WIDTH = 6
        private const val SCROLLBAR_HEIGHT = 108
        private const val SCROLLER_HEIGHT = 15
        private const val CASE_SLOTS_X = 8
        private const val CASE_SLOTS_Y = 18
        private const val SLOT_SIZE = 18
        private const val VISIBLE_ROWS = 6
        private const val COLUMNS = 9
        private const val SEARCH_BOX_WIDTH = 70
        private const val SEARCH_BOX_HEIGHT = 10
        private const val FILTER_BUTTON_SIZE = 8
    }

    private lateinit var searchBox: EditBox
    private var filterButtonX = 0
    private var filterButtonY = 0
    private var scrolling = false

    init {
        imageWidth = 176
        imageHeight = 222
        inventoryLabelY = imageHeight - 94
    }

    override fun init() {
        super.init()
        titleLabelX = 8
        titleLabelY = 6

        filterButtonX = leftPos + 82
        filterButtonY = topPos + 5

        val searchX = filterButtonX + FILTER_BUTTON_SIZE + 4
        val searchY = topPos + 4

        searchBox = EditBox(
            font, searchX, searchY, SEARCH_BOX_WIDTH, SEARCH_BOX_HEIGHT,
            Component.translatable("gui.simpletms.search")
        )
        searchBox.setMaxLength(50)
        searchBox.setBordered(true)
        searchBox.setVisible(true)
        searchBox.setTextColor(0xFFFFFF)
        searchBox.setHint(Component.translatable("gui.simpletms.search.hint"))
        searchBox.setResponder { menu.setSearchQuery(it) }
        searchBox.value = menu.getSearchQuery()

        addRenderableWidget(searchBox)
    }

    override fun resize(minecraft: net.minecraft.client.Minecraft, width: Int, height: Int) {
        val previousSearch = searchBox.value
        super.resize(minecraft, width, height)
        searchBox.value = previousSearch
        menu.setSearchQuery(previousSearch)
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val x = leftPos
        val y = topPos

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, VISIBLE_ROWS * SLOT_SIZE + CASE_SLOTS_Y + 1)
        guiGraphics.blit(TEXTURE, x, y + VISIBLE_ROWS * SLOT_SIZE + CASE_SLOTS_Y + 1, 0, 126, imageWidth, 96)

        renderFilterButton(guiGraphics, mouseX, mouseY)
        renderCaseSlotItems(guiGraphics, x, y)
        renderScrollbar(guiGraphics, x, y)
        renderCaseSlotHighlight(guiGraphics, x, y, mouseX, mouseY)
    }

    private fun renderFilterButton(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val mode = menu.getFilterMode()
        val isHovered = isOverFilterButton(mouseX.toDouble(), mouseY.toDouble())

        val bgColor = if (isHovered) 0xFF555555.toInt() else 0xFF333333.toInt()
        guiGraphics.fill(filterButtonX, filterButtonY, filterButtonX + FILTER_BUTTON_SIZE, filterButtonY + FILTER_BUTTON_SIZE, bgColor)
        guiGraphics.renderOutline(filterButtonX, filterButtonY, FILTER_BUTTON_SIZE, FILTER_BUTTON_SIZE, 0xFF888888.toInt())

        val innerMargin = 2
        when (mode) {
            FilterMode.ALL -> {}
            FilterMode.OWNED_ONLY -> guiGraphics.fill(
                filterButtonX + innerMargin, filterButtonY + innerMargin,
                filterButtonX + FILTER_BUTTON_SIZE - innerMargin, filterButtonY + FILTER_BUTTON_SIZE - innerMargin,
                0xFF55FF55.toInt()
            )
            FilterMode.MISSING_ONLY -> guiGraphics.fill(
                filterButtonX + innerMargin, filterButtonY + innerMargin,
                filterButtonX + FILTER_BUTTON_SIZE - innerMargin, filterButtonY + FILTER_BUTTON_SIZE - innerMargin,
                0xFFFF5555.toInt()
            )
        }
    }

    private fun isOverFilterButton(mouseX: Double, mouseY: Double): Boolean {
        return mouseX >= filterButtonX && mouseX < filterButtonX + FILTER_BUTTON_SIZE &&
                mouseY >= filterButtonY && mouseY < filterButtonY + FILTER_BUTTON_SIZE
    }

    private fun renderCaseSlotItems(guiGraphics: GuiGraphics, x: Int, y: Int) {
        val scrollRow = menu.getScrollRow()
        val filteredMoves = menu.getFilteredMoves()

        for (row in 0 until VISIBLE_ROWS) {
            for (col in 0 until COLUMNS) {
                val moveIndex = (scrollRow + row) * COLUMNS + col
                if (moveIndex >= filteredMoves.size) continue

                val moveName = filteredMoves[moveIndex]
                val quantity = menu.getMoveQuantity(moveName)
                val slotX = x + CASE_SLOTS_X + col * SLOT_SIZE + 1
                val slotY = y + CASE_SLOTS_Y + row * SLOT_SIZE + 1
                val itemStack = getItemStackForMove(moveName)

                if (quantity > 0) {
                    // Set the stack count for proper rendering
                    itemStack.count = quantity
                    guiGraphics.renderItem(itemStack, slotX, slotY)
                    // Render item count decoration (shows count if > 1)
                    guiGraphics.renderItemDecorations(font, itemStack, slotX, slotY)
                } else {
                    // Ghost item (not stored)
                    renderGhostItem(guiGraphics, itemStack, slotX, slotY)
                }
            }
        }
    }

    private fun renderGhostItem(guiGraphics: GuiGraphics, stack: ItemStack, x: Int, y: Int) {
        if (stack.isEmpty) return
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, GHOST_ALPHA)
        guiGraphics.renderItem(stack, x, y)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.disableBlend()
    }

    private fun renderCaseSlotHighlight(guiGraphics: GuiGraphics, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        val slotPos = menu.getCaseSlotAt(mouseX - x - CASE_SLOTS_X, mouseY - y - CASE_SLOTS_Y) ?: return
        val (row, col) = slotPos
        val slotX = x + CASE_SLOTS_X + col * SLOT_SIZE
        val slotY = y + CASE_SLOTS_Y + row * SLOT_SIZE
        guiGraphics.fillGradient(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0x80FFFFFF.toInt(), 0x80FFFFFF.toInt())
    }

    private fun getItemStackForMove(moveName: String): ItemStack {
        val prefix = if (menu.isTR) "tr_" else "tm_"
        return SimpleTMsItems.getItemStackFromName(prefix + moveName)
    }

    private fun renderScrollbar(guiGraphics: GuiGraphics, x: Int, y: Int) {
        val maxScroll = menu.getMaxScrollRow()

        if (maxScroll <= 0) {
            guiGraphics.fill(x + SCROLLBAR_X, y + SCROLLBAR_Y, x + SCROLLBAR_X + SCROLLBAR_WIDTH, y + SCROLLBAR_Y + SCROLLBAR_HEIGHT, 0xFF373737.toInt())
            return
        }

        guiGraphics.fill(x + SCROLLBAR_X, y + SCROLLBAR_Y, x + SCROLLBAR_X + SCROLLBAR_WIDTH, y + SCROLLBAR_Y + SCROLLBAR_HEIGHT, 0xFF000000.toInt())

        val scrollProgress = menu.getScrollRow().toFloat() / maxScroll.toFloat()
        val scrollerY = (SCROLLBAR_HEIGHT - SCROLLER_HEIGHT) * scrollProgress

        guiGraphics.fill(
            x + SCROLLBAR_X, y + SCROLLBAR_Y + scrollerY.toInt(),
            x + SCROLLBAR_X + SCROLLBAR_WIDTH, y + SCROLLBAR_Y + scrollerY.toInt() + SCROLLER_HEIGHT,
            0xFFC6C6C6.toInt()
        )
    }

    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false)
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false)
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderTooltip(guiGraphics, mouseX, mouseY)

        if (isOverFilterButton(mouseX.toDouble(), mouseY.toDouble())) {
            val tooltipKey = when (menu.getFilterMode()) {
                FilterMode.ALL -> "gui.simpletms.filter.all"
                FilterMode.OWNED_ONLY -> "gui.simpletms.filter.owned"
                FilterMode.MISSING_ONLY -> "gui.simpletms.filter.missing"
            }
            guiGraphics.renderTooltip(font, Component.translatable(tooltipKey), mouseX, mouseY)
        } else if (!searchBox.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
            val slotPos = menu.getCaseSlotAt(mouseX - leftPos - CASE_SLOTS_X, mouseY - topPos - CASE_SLOTS_Y)
            if (slotPos != null) {
                val (row, col) = slotPos
                val visibleIndex = row * COLUMNS + col
                val moveName = menu.getMoveNameForVisibleSlot(visibleIndex)
                if (moveName != null) {
                    val itemStack = getItemStackForMove(moveName)
                    val quantity = menu.getMoveQuantity(moveName)
                    if (quantity > 0) {
                        itemStack.count = quantity
                    }
                    if (!itemStack.isEmpty) {
                        guiGraphics.renderTooltip(font, itemStack, mouseX, mouseY)
                    }
                }
            }
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (searchBox.isFocused) {
            if (keyCode == 256) {
                searchBox.setFocused(false)
                return true
            }
            return searchBox.keyPressed(keyCode, scanCode, modifiers)
        }
        if (searchBox.canConsumeInput()) {
            return searchBox.keyPressed(keyCode, scanCode, modifiers)
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        if (searchBox.isFocused || searchBox.canConsumeInput()) {
            return searchBox.charTyped(codePoint, modifiers)
        }
        if (Character.isLetterOrDigit(codePoint) || codePoint == '#') {
            searchBox.setFocused(true)
            return searchBox.charTyped(codePoint, modifiers)
        }
        return super.charTyped(codePoint, modifiers)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0 && isOverFilterButton(mouseX, mouseY)) {
            menu.cycleFilterMode()
            return true
        }

        if (searchBox.mouseClicked(mouseX, mouseY, button)) {
            setFocused(searchBox)
            return true
        }

        if (searchBox.isFocused && !searchBox.isMouseOver(mouseX, mouseY)) {
            searchBox.setFocused(false)
        }

        if (button == 0 && isOverScrollbar(mouseX, mouseY)) {
            scrolling = true
            updateScrollFromMouse(mouseY)
            return true
        }

        // Handle case slot clicks
        if (button == 0) {
            val relX = mouseX.toInt() - leftPos - CASE_SLOTS_X
            val relY = mouseY.toInt() - topPos - CASE_SLOTS_Y
            val slotPos = menu.getCaseSlotAt(relX, relY)

            if (slotPos != null) {
                val (row, col) = slotPos
                val visibleIndex = row * COLUMNS + col
                val moveName = menu.getMoveNameForVisibleSlot(visibleIndex)

                if (moveName != null) {
                    val isShiftClick = hasShiftDown()

                    // Send packet to server with MOVE NAME (not index)
                    SimpleTMsNetwork.sendCaseSlotClick(moveName, isShiftClick)

                    // Also handle locally for immediate feedback
                    menu.handleCaseSlotClickByName(moveName, isShiftClick)

                    return true
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (searchBox.isMouseOver(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
        }
        if (menu.getMaxScrollRow() > 0) {
            menu.scroll(-scrollY.toInt().coerceIn(-1, 1))
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) scrolling = false
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (scrolling) {
            updateScrollFromMouse(mouseY)
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    private fun isOverScrollbar(mouseX: Double, mouseY: Double): Boolean {
        val x = leftPos + SCROLLBAR_X
        val y = topPos + SCROLLBAR_Y
        return mouseX >= x && mouseX < x + SCROLLBAR_WIDTH && mouseY >= y && mouseY < y + SCROLLBAR_HEIGHT
    }

    private fun updateScrollFromMouse(mouseY: Double) {
        val scrollAreaTop = topPos + SCROLLBAR_Y
        val scrollAreaHeight = SCROLLBAR_HEIGHT - SCROLLER_HEIGHT
        val relativeY = (mouseY - scrollAreaTop - SCROLLER_HEIGHT / 2).coerceIn(0.0, scrollAreaHeight.toDouble())
        val scrollProgress = relativeY / scrollAreaHeight
        menu.setScrollRow((scrollProgress * menu.getMaxScrollRow()).toInt())
    }
}