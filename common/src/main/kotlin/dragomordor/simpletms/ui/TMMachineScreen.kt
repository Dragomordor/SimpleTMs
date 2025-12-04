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
 * Client-side screen for the TM Machine.
 *
 * Features:
 * - Displays all available TMs and TRs
 * - Type filter (All/TM/TR) - in ALL mode, shows TM and TR side by side
 * - Ownership filter (All/Owned/Missing) - hides ghost items when showing owned only
 * - Search box (right-aligned)
 * - Pokémon filter with party selection (shift-click opens party GUI)
 * - Normal container click behavior (click to pick up, shift-click to transfer)
 * - Shows quantities for stored items
 * - Ghost items for moves not in storage (unless ownership filter is active)
 * - Filter state persists between screen openings
 */
class TMMachineScreen(
    menu: TMMachineMenu,
    playerInventory: Inventory,
    title: Component
) : AbstractContainerScreen<TMMachineMenu>(menu, playerInventory, title) {

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
        private const val SEARCH_BOX_WIDTH = 60
        private const val SEARCH_BOX_HEIGHT = 10
        private const val BUTTON_SIZE = 10
        private const val BUTTON_SPACING = 2
    }

    private lateinit var searchBox: EditBox

    // Button positions (calculated in init)
    private var typeFilterButtonX = 0
    private var typeFilterButtonY = 0
    private var ownershipFilterButtonX = 0
    private var ownershipFilterButtonY = 0
    private var pokemonFilterButtonX = 0
    private var pokemonFilterButtonY = 0

    private var scrolling = false

    // Track if we're waiting for party selection to complete
    private var awaitingPartySelection = false

    init {
        imageWidth = 176
        imageHeight = 222
        inventoryLabelY = imageHeight - 94
    }

    override fun init() {
        super.init()
        titleLabelX = 8
        titleLabelY = 6

        // Check for pending Pokémon filter from party selection
        val pendingFilter = SimpleTMsNetwork.consumePendingPokemonFilter()
        if (pendingFilter != null) {
            menu.setPokemonFilter(pendingFilter)
        }

        // Layout: [Title] [TypeFilter] [OwnershipFilter] [PokemonFilter] [SearchBox]
        // Buttons stack to the left of search box, search box is right-aligned

        val buttonY = topPos + 4
        val searchX = leftPos + imageWidth - SEARCH_BOX_WIDTH - 6

        // Position buttons from left to right after title
        typeFilterButtonX = leftPos + 71
        typeFilterButtonY = buttonY

        ownershipFilterButtonX = typeFilterButtonX + BUTTON_SIZE + BUTTON_SPACING
        ownershipFilterButtonY = buttonY

        pokemonFilterButtonX = ownershipFilterButtonX + BUTTON_SIZE + BUTTON_SPACING
        pokemonFilterButtonY = buttonY

        searchBox = EditBox(
            font, searchX, buttonY, SEARCH_BOX_WIDTH, SEARCH_BOX_HEIGHT,
            Component.translatable("gui.simpletms.search")
        )
        searchBox.setMaxLength(50)
        searchBox.setBordered(true)
        searchBox.setVisible(true)
        searchBox.setTextColor(0xFFFFFF)
        searchBox.setHint(Component.translatable("gui.simpletms.search.hint"))
        searchBox.setResponder { menu.setSearchQuery(it) }

        // Initialize search box with the loaded search query from menu (which loads from ClientFilterStorage)
        searchBox.value = menu.getSearchQuery()

        addRenderableWidget(searchBox)
    }

    override fun resize(minecraft: net.minecraft.client.Minecraft, width: Int, height: Int) {
        val previousSearch = searchBox.value
        super.resize(minecraft, width, height)
        searchBox.value = previousSearch
        menu.setSearchQuery(previousSearch)
    }

    // ========================================
    // Rendering
    // ========================================

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val x = leftPos
        val y = topPos

        // Draw background texture (using generic 54-slot chest texture)
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, VISIBLE_ROWS * SLOT_SIZE + CASE_SLOTS_Y + 1)
        guiGraphics.blit(TEXTURE, x, y + VISIBLE_ROWS * SLOT_SIZE + CASE_SLOTS_Y + 1, 0, 126, imageWidth, 96)

        renderTypeFilterButton(guiGraphics, mouseX, mouseY)
        renderOwnershipFilterButton(guiGraphics, mouseX, mouseY)
        renderPokemonFilterButton(guiGraphics, mouseX, mouseY)
        renderCaseSlotItems(guiGraphics, x, y)
        renderScrollbar(guiGraphics, x, y)
        renderCaseSlotHighlight(guiGraphics, x, y, mouseX, mouseY)
    }

    private fun renderTypeFilterButton(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val filter = menu.getTypeFilter()
        val isHovered = isOverTypeFilterButton(mouseX.toDouble(), mouseY.toDouble())

        val bgColor = if (isHovered) 0xFF555555.toInt() else 0xFF333333.toInt()
        guiGraphics.fill(typeFilterButtonX, typeFilterButtonY, typeFilterButtonX + BUTTON_SIZE, typeFilterButtonY + BUTTON_SIZE, bgColor)
        guiGraphics.renderOutline(typeFilterButtonX, typeFilterButtonY, BUTTON_SIZE, BUTTON_SIZE, 0xFF888888.toInt())

        // Draw indicator based on filter mode
        val innerMargin = 2

        when (filter) {
            MoveTypeFilter.ALL -> {
                // Draw split for "All" - blue left, red right
                guiGraphics.fill(
                    typeFilterButtonX + innerMargin, typeFilterButtonY + innerMargin,
                    typeFilterButtonX + BUTTON_SIZE / 2, typeFilterButtonY + BUTTON_SIZE - innerMargin,
                    0xFF5555FF.toInt()
                )
                guiGraphics.fill(
                    typeFilterButtonX + BUTTON_SIZE / 2, typeFilterButtonY + innerMargin,
                    typeFilterButtonX + BUTTON_SIZE - innerMargin, typeFilterButtonY + BUTTON_SIZE - innerMargin,
                    0xFFFF5555.toInt()
                )
            }
            MoveTypeFilter.TM_ONLY -> {
                // Blue for TM
                guiGraphics.fill(
                    typeFilterButtonX + innerMargin, typeFilterButtonY + innerMargin,
                    typeFilterButtonX + BUTTON_SIZE - innerMargin, typeFilterButtonY + BUTTON_SIZE - innerMargin,
                    0xFF5555FF.toInt()
                )
            }
            MoveTypeFilter.TR_ONLY -> {
                // Red for TR
                guiGraphics.fill(
                    typeFilterButtonX + innerMargin, typeFilterButtonY + innerMargin,
                    typeFilterButtonX + BUTTON_SIZE - innerMargin, typeFilterButtonY + BUTTON_SIZE - innerMargin,
                    0xFFFF5555.toInt()
                )
            }
        }
    }

    private fun renderOwnershipFilterButton(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val filter = menu.getOwnershipFilter()
        val isHovered = isOverOwnershipFilterButton(mouseX.toDouble(), mouseY.toDouble())

        val bgColor = if (isHovered) 0xFF555555.toInt() else 0xFF333333.toInt()
        guiGraphics.fill(ownershipFilterButtonX, ownershipFilterButtonY, ownershipFilterButtonX + BUTTON_SIZE, ownershipFilterButtonY + BUTTON_SIZE, bgColor)
        guiGraphics.renderOutline(ownershipFilterButtonX, ownershipFilterButtonY, BUTTON_SIZE, BUTTON_SIZE, 0xFF888888.toInt())

        val innerMargin = 2

        when (filter) {
            OwnershipFilter.ALL -> {
                // White circle outline for "All"
                guiGraphics.renderOutline(
                    ownershipFilterButtonX + innerMargin, ownershipFilterButtonY + innerMargin,
                    BUTTON_SIZE - innerMargin * 2, BUTTON_SIZE - innerMargin * 2,
                    0xFFFFFFFF.toInt()
                )
            }
            OwnershipFilter.OWNED_ONLY -> {
                // Green filled for "Owned"
                guiGraphics.fill(
                    ownershipFilterButtonX + innerMargin, ownershipFilterButtonY + innerMargin,
                    ownershipFilterButtonX + BUTTON_SIZE - innerMargin, ownershipFilterButtonY + BUTTON_SIZE - innerMargin,
                    0xFF55FF55.toInt()
                )
            }
            OwnershipFilter.MISSING_ONLY -> {
                // Gray/dark for "Missing"
                guiGraphics.fill(
                    ownershipFilterButtonX + innerMargin, ownershipFilterButtonY + innerMargin,
                    ownershipFilterButtonX + BUTTON_SIZE - innerMargin, ownershipFilterButtonY + BUTTON_SIZE - innerMargin,
                    0xFF666666.toInt()
                )
            }
        }
    }

    private fun renderPokemonFilterButton(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val hasFilter = menu.getPokemonFilterData() != null
        val isEnabled = menu.isPokemonFilterEnabled()
        val isHovered = isOverPokemonFilterButton(mouseX.toDouble(), mouseY.toDouble())

        val bgColor = if (isHovered) 0xFF555555.toInt() else 0xFF333333.toInt()
        guiGraphics.fill(pokemonFilterButtonX, pokemonFilterButtonY, pokemonFilterButtonX + BUTTON_SIZE, pokemonFilterButtonY + BUTTON_SIZE, bgColor)
        guiGraphics.renderOutline(pokemonFilterButtonX, pokemonFilterButtonY, BUTTON_SIZE, BUTTON_SIZE, 0xFF888888.toInt())

        // Draw Pokéball-like icon
        val innerMargin = 2
        if (hasFilter) {
            val indicatorColor = if (isEnabled) 0xFF55FF55.toInt() else 0xFF888888.toInt()
            guiGraphics.fill(
                pokemonFilterButtonX + innerMargin, pokemonFilterButtonY + innerMargin,
                pokemonFilterButtonX + BUTTON_SIZE - innerMargin, pokemonFilterButtonY + BUTTON_SIZE - innerMargin,
                indicatorColor
            )
        } else {
            // Draw empty pokeball outline
            guiGraphics.renderOutline(
                pokemonFilterButtonX + innerMargin, pokemonFilterButtonY + innerMargin,
                BUTTON_SIZE - innerMargin * 2, BUTTON_SIZE - innerMargin * 2,
                0xFFAAAAAA.toInt()
            )
        }
    }

    private fun isOverTypeFilterButton(mouseX: Double, mouseY: Double): Boolean {
        return mouseX >= typeFilterButtonX && mouseX < typeFilterButtonX + BUTTON_SIZE &&
                mouseY >= typeFilterButtonY && mouseY < typeFilterButtonY + BUTTON_SIZE
    }

    private fun isOverOwnershipFilterButton(mouseX: Double, mouseY: Double): Boolean {
        return mouseX >= ownershipFilterButtonX && mouseX < ownershipFilterButtonX + BUTTON_SIZE &&
                mouseY >= ownershipFilterButtonY && mouseY < ownershipFilterButtonY + BUTTON_SIZE
    }

    private fun isOverPokemonFilterButton(mouseX: Double, mouseY: Double): Boolean {
        return mouseX >= pokemonFilterButtonX && mouseX < pokemonFilterButtonX + BUTTON_SIZE &&
                mouseY >= pokemonFilterButtonY && mouseY < pokemonFilterButtonY + BUTTON_SIZE
    }

    private fun renderCaseSlotItems(guiGraphics: GuiGraphics, x: Int, y: Int) {
        val scrollRow = menu.getScrollRow()
        val filteredMoves = menu.getFilteredMoves()
        val typeFilter = menu.getTypeFilter()
        val ownershipFilter = menu.getOwnershipFilter()

        // Determine if we should show ghost items
        val showGhosts = ownershipFilter == OwnershipFilter.ALL || ownershipFilter == OwnershipFilter.MISSING_ONLY

        if (typeFilter == MoveTypeFilter.ALL) {
            // In ALL mode, build a flat list of displayable items (TM and TR separately)
            // This ensures no gaps - items are shown consecutively
            val displayItems = mutableListOf<DisplayItem>()

            for (entry in filteredMoves) {
                // Add TM if valid and (has count OR showing ghosts)
                if (entry.isValidTM && (entry.tmCount > 0 || showGhosts)) {
                    displayItems.add(DisplayItem(entry.moveName, false, entry.tmCount))
                }
                // Add TR if valid and (has count OR showing ghosts)
                if (entry.isValidTR && (entry.trCount > 0 || showGhosts)) {
                    displayItems.add(DisplayItem(entry.moveName, true, entry.trCount))
                }
            }

            // Now render the flat list with 9 columns
            val startIndex = scrollRow * COLUMNS
            for (i in 0 until VISIBLE_ROWS * COLUMNS) {
                val itemIndex = startIndex + i
                if (itemIndex >= displayItems.size) break

                val item = displayItems[itemIndex]
                val col = i % COLUMNS
                val row = i / COLUMNS

                val slotX = x + CASE_SLOTS_X + col * SLOT_SIZE + 1
                val slotY = y + CASE_SLOTS_Y + row * SLOT_SIZE + 1

                val prefix = if (item.isTR) "tr_" else "tm_"
                val itemStack = SimpleTMsItems.getItemStackFromName(prefix + item.moveName)

                if (item.count > 0) {
                    renderItemWithCount(guiGraphics, itemStack, slotX, slotY, item.count)
                } else {
                    renderGhostItem(guiGraphics, itemStack, slotX, slotY)
                }
            }
        } else {
            // In TM_ONLY or TR_ONLY mode, show single item per slot
            val startIndex = scrollRow * COLUMNS
            for (i in 0 until VISIBLE_ROWS * COLUMNS) {
                val moveIndex = startIndex + i
                if (moveIndex >= filteredMoves.size) break

                val entry = filteredMoves[moveIndex]
                val col = i % COLUMNS
                val row = i / COLUMNS

                val slotX = x + CASE_SLOTS_X + col * SLOT_SIZE + 1
                val slotY = y + CASE_SLOTS_Y + row * SLOT_SIZE + 1

                val (itemStack, count) = getDisplayItemForEntry(entry, typeFilter)

                if (count > 0) {
                    renderItemWithCount(guiGraphics, itemStack, slotX, slotY, count)
                } else if (showGhosts) {
                    renderGhostItem(guiGraphics, itemStack, slotX, slotY)
                }
            }
        }
    }

    /**
     * Helper data class for flat display list in ALL mode
     */
    private data class DisplayItem(val moveName: String, val isTR: Boolean, val count: Int)

    /**
     * Get the display item at a given visible slot position in ALL mode
     */
    private fun getDisplayItemAtSlot(row: Int, col: Int): DisplayItem? {
        val filteredMoves = menu.getFilteredMoves()
        val ownershipFilter = menu.getOwnershipFilter()
        val showGhosts = ownershipFilter == OwnershipFilter.ALL || ownershipFilter == OwnershipFilter.MISSING_ONLY

        // Build flat list (same logic as renderCaseSlotItems)
        val displayItems = mutableListOf<DisplayItem>()
        for (entry in filteredMoves) {
            if (entry.isValidTM && (entry.tmCount > 0 || showGhosts)) {
                displayItems.add(DisplayItem(entry.moveName, false, entry.tmCount))
            }
            if (entry.isValidTR && (entry.trCount > 0 || showGhosts)) {
                displayItems.add(DisplayItem(entry.moveName, true, entry.trCount))
            }
        }

        val scrollRow = menu.getScrollRow()
        val slotIndex = (scrollRow + row) * COLUMNS + col
        return displayItems.getOrNull(slotIndex)
    }

    /**
     * Render an item with its count overlay (count rendered AFTER item for proper z-order)
     */
    private fun renderItemWithCount(guiGraphics: GuiGraphics, stack: ItemStack, x: Int, y: Int, count: Int) {
        if (stack.isEmpty) return

        // Render the item first
        guiGraphics.renderItem(stack, x, y)

        // Then render the count on top (only if > 1)
        if (count > 1) {
            guiGraphics.pose().pushPose()
            guiGraphics.pose().translate(0f, 0f, 200f) // Ensure count is on top

            val countStr = if (count > 99) "99+" else count.toString()
            val textX = x + 17 - font.width(countStr)
            val textY = y + 9

            guiGraphics.drawString(font, countStr, textX, textY, 0xFFFFFF, true)
            guiGraphics.pose().popPose()
        }
    }

    /**
     * Determine which item to display for a move entry based on current filter.
     * Returns the ItemStack and the count to display.
     */
    private fun getDisplayItemForEntry(entry: TMMachineMenu.FilteredMoveEntry, typeFilter: MoveTypeFilter): Pair<ItemStack, Int> {
        return when (typeFilter) {
            MoveTypeFilter.TM_ONLY -> {
                val stack = SimpleTMsItems.getItemStackFromName("tm_${entry.moveName}")
                stack to entry.tmCount
            }
            MoveTypeFilter.TR_ONLY -> {
                val stack = SimpleTMsItems.getItemStackFromName("tr_${entry.moveName}")
                stack to entry.trCount
            }
            MoveTypeFilter.ALL -> {
                // This shouldn't be called in ALL mode, but handle it anyway
                val prefix = if (entry.isValidTM) "tm_" else "tr_"
                val stack = SimpleTMsItems.getItemStackFromName(prefix + entry.moveName)
                val count = entry.tmCount + entry.trCount
                stack to count
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

        // Render button tooltips
        if (isOverTypeFilterButton(mouseX.toDouble(), mouseY.toDouble())) {
            val tooltipKey = when (menu.getTypeFilter()) {
                MoveTypeFilter.ALL -> "gui.simpletms.machine.filter.all"
                MoveTypeFilter.TM_ONLY -> "gui.simpletms.machine.filter.tm"
                MoveTypeFilter.TR_ONLY -> "gui.simpletms.machine.filter.tr"
            }
            guiGraphics.renderTooltip(font, Component.translatable(tooltipKey), mouseX, mouseY)
        } else if (isOverOwnershipFilterButton(mouseX.toDouble(), mouseY.toDouble())) {
            val tooltipKey = when (menu.getOwnershipFilter()) {
                OwnershipFilter.ALL -> "gui.simpletms.machine.ownership.all"
                OwnershipFilter.OWNED_ONLY -> "gui.simpletms.machine.ownership.owned"
                OwnershipFilter.MISSING_ONLY -> "gui.simpletms.machine.ownership.missing"
            }
            guiGraphics.renderTooltip(font, Component.translatable(tooltipKey), mouseX, mouseY)
        } else if (isOverPokemonFilterButton(mouseX.toDouble(), mouseY.toDouble())) {
            val filterData = menu.getPokemonFilterData()
            val tooltipLines = mutableListOf<Component>()

            if (filterData != null) {
                if (menu.isPokemonFilterEnabled()) {
                    tooltipLines.add(Component.translatable("gui.simpletms.machine.pokemon_filter.active", filterData.displayName))
                } else {
                    tooltipLines.add(Component.translatable("gui.simpletms.machine.pokemon_filter.inactive", filterData.displayName))
                }
            } else {
                tooltipLines.add(Component.translatable("gui.simpletms.machine.pokemon_filter.none"))
            }
            tooltipLines.add(Component.translatable("gui.simpletms.machine.pokemon_filter.shift_hint"))
            guiGraphics.renderTooltip(font, tooltipLines, java.util.Optional.empty(), mouseX, mouseY)
        } else if (!searchBox.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
            // Render move item tooltip (without "Stored: x" line)
            renderMoveTooltip(guiGraphics, mouseX, mouseY)
        }
    }

    private fun renderMoveTooltip(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val slotPos = menu.getCaseSlotAt(mouseX - leftPos - CASE_SLOTS_X, mouseY - topPos - CASE_SLOTS_Y) ?: return
        val (row, col) = slotPos
        val typeFilter = menu.getTypeFilter()

        if (typeFilter == MoveTypeFilter.ALL) {
            // In ALL mode with flat list, get the display item at this slot
            val displayItem = getDisplayItemAtSlot(row, col) ?: return

            val prefix = if (displayItem.isTR) "tr_" else "tm_"
            val itemStack = SimpleTMsItems.getItemStackFromName(prefix + displayItem.moveName)
            if (itemStack.isEmpty) return

            guiGraphics.renderTooltip(font, getTooltipFromItem(minecraft!!, itemStack), java.util.Optional.empty(), mouseX, mouseY)
        } else {
            // In single mode - calculate absolute move index with scroll
            val slotIndex = row * COLUMNS + col
            val moveIndex = menu.getScrollRow() * COLUMNS + slotIndex
            val filteredMoves = menu.getFilteredMoves()

            if (moveIndex !in filteredMoves.indices) return
            val entry = filteredMoves[moveIndex]

            val (itemStack, _) = getDisplayItemForEntry(entry, typeFilter)
            if (itemStack.isEmpty) return

            // Just show the item's normal tooltip, no extra lines
            guiGraphics.renderTooltip(font, getTooltipFromItem(minecraft!!, itemStack), java.util.Optional.empty(), mouseX, mouseY)
        }
    }

    // ========================================
    // Input Handling
    // ========================================

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (searchBox.isFocused) {
            if (keyCode == 256) { // Escape
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
        // Type filter button
        if (button == 0 && isOverTypeFilterButton(mouseX, mouseY)) {
            menu.cycleTypeFilter()
            return true
        }

        // Ownership filter button
        if (button == 0 && isOverOwnershipFilterButton(mouseX, mouseY)) {
            menu.cycleOwnershipFilter()
            return true
        }

        // Pokémon filter button
        if (button == 0 && isOverPokemonFilterButton(mouseX, mouseY)) {
            if (hasShiftDown()) {
                // Shift-click: Open party selection on server
                // Mark that we're awaiting selection so we can handle the callback
                awaitingPartySelection = true
                SimpleTMsNetwork.sendMachinePartySelectRequest()
            } else {
                // Normal click: Toggle existing filter
                menu.togglePokemonFilter()
            }
            return true
        }

        // Search box
        if (searchBox.mouseClicked(mouseX, mouseY, button)) {
            setFocused(searchBox)
            return true
        }

        if (searchBox.isFocused && !searchBox.isMouseOver(mouseX, mouseY)) {
            searchBox.setFocused(false)
        }

        // Scrollbar
        if (button == 0 && isOverScrollbar(mouseX, mouseY)) {
            scrolling = true
            updateScrollFromMouse(mouseY)
            return true
        }

        // Case slot clicks - normal container behavior
        // Shift-click: Transfer to inventory
        // Normal click: Pick up item (put on cursor)
        if (button == 0 || button == 1) {
            val relX = mouseX.toInt() - leftPos - CASE_SLOTS_X
            val relY = mouseY.toInt() - topPos - CASE_SLOTS_Y
            val slotPos = menu.getCaseSlotAt(relX, relY)

            if (slotPos != null) {
                val (row, col) = slotPos
                val typeFilter = menu.getTypeFilter()
                val isShiftClick = hasShiftDown()

                if (typeFilter == MoveTypeFilter.ALL) {
                    // In ALL mode with flat list, get the display item at this slot
                    val displayItem = getDisplayItemAtSlot(row, col)
                    if (displayItem != null && displayItem.count > 0) {
                        // Find the move entry and its visible index for the network packet
                        val filteredMoves = menu.getFilteredMoves()
                        val moveIndex = filteredMoves.indexOfFirst { it.moveName == displayItem.moveName }
                        if (moveIndex >= 0) {
                            SimpleTMsNetwork.sendMachineSlotClick(moveIndex, displayItem.isTR, isShiftClick)
                            return true
                        }
                    }
                } else {
                    // In single mode - calculate absolute move index with scroll
                    val slotIndex = row * COLUMNS + col
                    val moveIndex = menu.getScrollRow() * COLUMNS + slotIndex
                    val filteredMoves = menu.getFilteredMoves()

                    if (moveIndex in filteredMoves.indices) {
                        val entry = filteredMoves[moveIndex]

                        if (entry.hasAny()) {
                            val clickTR = when {
                                typeFilter == MoveTypeFilter.TM_ONLY -> false
                                typeFilter == MoveTypeFilter.TR_ONLY -> true
                                button == 1 -> true // Right-click = TR
                                else -> entry.tmCount <= 0 // Left-click = TM if available, else TR
                            }

                            val hasItem = if (clickTR) entry.trCount > 0 else entry.tmCount > 0
                            if (hasItem) {
                                SimpleTMsNetwork.sendMachineSlotClick(moveIndex, clickTR, isShiftClick)
                                return true
                            }
                        }
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    /**
     * Called when a Pokémon is selected from the party.
     * This is invoked from the network handler.
     */
    fun onPokemonSelected(speciesId: String, formName: String, displayName: String, learnableMoves: Set<String>) {
        val filterData = PokemonFilterData(speciesId, formName, displayName, learnableMoves)
        menu.setPokemonFilter(filterData)
        awaitingPartySelection = false
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