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
 * Filter state persists between screen openings via ClientFilterStorage.
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
        private const val POKEMON_BUTTON_SIZE = 8
    }

    private lateinit var searchBox: EditBox

    // Ownership filter button position
    private var filterButtonX = 0
    private var filterButtonY = 0

    // Pokémon filter button position
    private var pokemonButtonX = 0
    private var pokemonButtonY = 0

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

        // Check for pending Pokémon filter from party selection
        // This should enable the filter since it's a fresh selection
        val pendingFilter = SimpleTMsNetwork.consumePendingCasePokemonFilter(menu.isTR)
        if (pendingFilter != null) {
            menu.setPokemonFilter(pendingFilter) // This enables and saves
        }

        // Position filter buttons
        // Always show Pokémon filter button (even if no filter set yet, shift-click can set one)
        pokemonButtonX = leftPos + 74
        pokemonButtonY = topPos + 5
        filterButtonX = pokemonButtonX + POKEMON_BUTTON_SIZE + 2
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

        // Initialize search box with the loaded search query from menu (which loads from ClientFilterStorage)
        searchBox.value = menu.getSearchQuery()

        addRenderableWidget(searchBox)
    }

    /**
     * Called when a Pokémon is selected from the party.
     * This is invoked from the network handler.
     */
    fun onPokemonSelected(filterData: PokemonFilterData) {
        menu.setPokemonFilter(filterData)
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

        // Always render Pokémon filter button (shift-click can set a filter)
        renderPokemonFilterButton(guiGraphics, mouseX, mouseY)

        // Render ownership filter button
        renderFilterButton(guiGraphics, mouseX, mouseY)

        renderCaseSlotItems(guiGraphics, x, y)
        renderScrollbar(guiGraphics, x, y)
        renderCaseSlotHighlight(guiGraphics, x, y, mouseX, mouseY)
    }

    private fun renderPokemonFilterButton(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val hasFilter = menu.hasPokemonFilter()
        val isEnabled = menu.isPokemonFilterEnabled()
        val isHovered = isOverPokemonButton(mouseX.toDouble(), mouseY.toDouble())

        // Background
        val bgColor = when {
            hasFilter && isEnabled && isHovered -> 0xFF446644.toInt()
            hasFilter && isEnabled -> 0xFF335533.toInt()
            isHovered -> 0xFF555555.toInt()
            else -> 0xFF333333.toInt()
        }
        guiGraphics.fill(pokemonButtonX, pokemonButtonY, pokemonButtonX + POKEMON_BUTTON_SIZE, pokemonButtonY + POKEMON_BUTTON_SIZE, bgColor)

        // Border - green when enabled, gray otherwise
        val borderColor = if (hasFilter && isEnabled) 0xFF55FF55.toInt() else 0xFF888888.toInt()
        guiGraphics.renderOutline(pokemonButtonX, pokemonButtonY, POKEMON_BUTTON_SIZE, POKEMON_BUTTON_SIZE, borderColor)

        // Inner indicator - Pokéball-like icon
        val innerMargin = 2
        if (hasFilter && isEnabled) {
            // Show a filled indicator when enabled
            guiGraphics.fill(
                pokemonButtonX + innerMargin, pokemonButtonY + innerMargin,
                pokemonButtonX + POKEMON_BUTTON_SIZE - innerMargin, pokemonButtonY + POKEMON_BUTTON_SIZE - innerMargin,
                0xFFFF5555.toInt() // Red like a Pokéball
            )
        } else if (hasFilter) {
            // Has filter but disabled - show gray
            guiGraphics.fill(
                pokemonButtonX + innerMargin, pokemonButtonY + innerMargin,
                pokemonButtonX + POKEMON_BUTTON_SIZE - innerMargin, pokemonButtonY + POKEMON_BUTTON_SIZE - innerMargin,
                0xFF666666.toInt()
            )
        } else {
            // No filter set - show empty outline
            guiGraphics.renderOutline(
                pokemonButtonX + innerMargin, pokemonButtonY + innerMargin,
                POKEMON_BUTTON_SIZE - innerMargin * 2, POKEMON_BUTTON_SIZE - innerMargin * 2,
                0xFF555555.toInt()
            )
        }
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

    private fun isOverPokemonButton(mouseX: Double, mouseY: Double): Boolean {
        return mouseX >= pokemonButtonX && mouseX < pokemonButtonX + POKEMON_BUTTON_SIZE &&
                mouseY >= pokemonButtonY && mouseY < pokemonButtonY + POKEMON_BUTTON_SIZE
    }

    private fun isOverFilterButton(mouseX: Double, mouseY: Double): Boolean {
        return mouseX >= filterButtonX && mouseX < filterButtonX + FILTER_BUTTON_SIZE &&
                mouseY >= filterButtonY && mouseY < filterButtonY + FILTER_BUTTON_SIZE
    }

    private fun renderCaseSlotItems(guiGraphics: GuiGraphics, x: Int, y: Int) {
        val scrollRow = menu.getScrollRow()
        val filteredMoves = menu.getFilteredMoves()
        val filterMode = menu.getFilterMode()

        // Determine if we should show ghost items
        val showGhosts = filterMode == FilterMode.ALL || filterMode == FilterMode.MISSING_ONLY

        for (row in 0 until VISIBLE_ROWS) {
            for (col in 0 until COLUMNS) {
                val moveIndex = (scrollRow + row) * COLUMNS + col
                if (moveIndex >= filteredMoves.size) continue

                val moveName = filteredMoves[moveIndex]
                val quantity = menu.getMoveQuantity(moveName)
                val itemStack = getItemStackForMove(moveName)

                // Set damage for TMs
                if (!menu.isTR) {
                    val damage = menu.getMoveDamage(moveName)
                    if (damage > 0) {
                        itemStack.damageValue = damage
                    }
                }

                val slotX = x + CASE_SLOTS_X + col * SLOT_SIZE + 1
                val slotY = y + CASE_SLOTS_Y + row * SLOT_SIZE + 1

                if (quantity > 0) {
                    renderItemWithCount(guiGraphics, itemStack, slotX, slotY, quantity)
                } else if (showGhosts) {
                    renderGhostItem(guiGraphics, itemStack, slotX, slotY)
                }
            }
        }
    }

    private fun getItemStackForMove(moveName: String): ItemStack {
        val prefix = if (menu.isTR) "tr_" else "tm_"
        return SimpleTMsItems.getItemStackFromName(prefix + moveName)
    }

    /**
     * Render an item with its count overlay (count rendered AFTER item for proper z-order)
     */
    private fun renderItemWithCount(guiGraphics: GuiGraphics, stack: ItemStack, x: Int, y: Int, count: Int) {
        if (stack.isEmpty) return

        // Render the item first
        guiGraphics.renderItem(stack, x, y)

        // Render item decorations (including durability bar)
        guiGraphics.renderItemDecorations(font, stack, x, y, if (count > 1) count.toString() else "")

        // If count > 1, render our custom count on top (the decoration might not show it properly for virtual slots)
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

        // Pokémon filter button tooltip
        if (isOverPokemonButton(mouseX.toDouble(), mouseY.toDouble())) {
            val tooltipLines = mutableListOf<Component>()

            if (menu.hasPokemonFilter()) {
                val pokemonName = menu.getPokemonDisplayName() ?: "Unknown"
                val learnableCount = menu.getPokemonLearnableCount()
                val isEnabled = menu.isPokemonFilterEnabled()

                tooltipLines.add(Component.translatable("gui.simpletms.filter.pokemon", pokemonName))
                tooltipLines.add(Component.translatable("gui.simpletms.filter.pokemon.learnable", learnableCount))

                if (isEnabled) {
                    tooltipLines.add(Component.translatable("gui.simpletms.filter.pokemon.enabled"))
                } else {
                    tooltipLines.add(Component.translatable("gui.simpletms.filter.pokemon.disabled"))
                }
            } else {
                tooltipLines.add(Component.translatable("gui.simpletms.filter.pokemon.none"))
            }

            tooltipLines.add(Component.translatable("gui.simpletms.filter.pokemon.shift_hint"))
            guiGraphics.renderComponentTooltip(font, tooltipLines, mouseX, mouseY)
        }
        // Ownership filter button tooltip
        else if (isOverFilterButton(mouseX.toDouble(), mouseY.toDouble())) {
            val tooltipKey = when (menu.getFilterMode()) {
                FilterMode.ALL -> "gui.simpletms.filter.all"
                FilterMode.OWNED_ONLY -> "gui.simpletms.filter.owned"
                FilterMode.MISSING_ONLY -> "gui.simpletms.filter.missing"
            }
            guiGraphics.renderTooltip(font, Component.translatable(tooltipKey), mouseX, mouseY)
        }
        // Item slot tooltip
        else if (!searchBox.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
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
        // Pokémon filter button
        if (button == 0 && isOverPokemonButton(mouseX, mouseY)) {
            if (hasShiftDown()) {
                // Shift-click: Open party selection on server
                SimpleTMsNetwork.sendCasePartySelectRequest(menu.isTR)
            } else {
                // Normal click: Toggle existing filter (only if we have one)
                if (menu.hasPokemonFilter()) {
                    menu.togglePokemonFilter()
                }
            }
            return true
        }

        // Ownership filter button
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
                    SimpleTMsNetwork.sendCaseSlotClick(moveName, isShiftClick)
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