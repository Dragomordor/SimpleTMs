package dragomordor.simpletms.ui

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.types.ElementalType
import dragomordor.simpletms.SimpleTMsItems
import dragomordor.simpletms.api.MoveCaseHelper
import dragomordor.simpletms.api.MoveCaseStorage
import dragomordor.simpletms.item.custom.MoveLearnItem
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import java.util.UUID

/**
 * Filter mode for displaying moves in the case.
 */
enum class FilterMode {
    ALL,           // Show all moves
    OWNED_ONLY,    // Show only stored moves
    MISSING_ONLY   // Show only moves not yet stored
}

/**
 * Menu/Container for the TM/TR Case.
 *
 * This menu displays a scrollable grid of slots, each representing a specific TM or TR.
 * Empty slots show a ghost/preview of the item, while filled slots show the actual item.
 *
 * The menu supports:
 * - Scrolling through the list of available moves
 * - Depositing TMs/TRs into their designated slots
 * - Withdrawing stored TMs/TRs
 * - Shift-clicking to quickly deposit/withdraw
 * - Search filtering by name and type tags (#fire, #physical, etc.)
 * - Filter toggle for owned/missing moves
 */
class MoveCaseMenu(
    containerId: Int,
    private val playerInventory: Inventory,
    val isTR: Boolean,
    initialStoredMoves: Set<String> = emptySet()
) : AbstractContainerMenu(SimpleTMsMenuTypes.MOVE_CASE_MENU.get(), containerId) {

    // The player using this menu
    private val player: Player = playerInventory.player
    private val playerUUID: UUID = player.uuid

    // Storage accessor - only available on server
    private val storage: MoveCaseStorage?
        get() = if (player is ServerPlayer) MoveCaseStorage.get(player) else null

    // Client-side cache of stored moves (synced from server on open, updated on actions)
    private val clientStoredMoves: MutableSet<String> = initialStoredMoves.toMutableSet()

    // Layout constants
    companion object {
        const val COLUMNS = 9
        const val VISIBLE_ROWS = 6  // Number of rows visible at once
        const val SLOT_SIZE = 18

        // GUI positioning
        const val CASE_SLOTS_X = 8
        const val CASE_SLOTS_Y = 18

        const val PLAYER_INV_X = 8
        const val PLAYER_INV_Y = 140
        const val PLAYER_HOTBAR_Y = 198
    }

    // All moves (unfiltered)
    private val allMoves: List<String> = MoveCaseHelper.getSortedMoveNames(isTR)

    // Filtered moves (based on search query and filter mode)
    private var filteredMoves: List<String> = allMoves

    // Current search query
    private var searchQuery: String = ""

    // Current filter mode
    private var filterMode: FilterMode = FilterMode.ALL

    // Total number of move slots (filtered)
    val totalMoveSlots: Int get() = filteredMoves.size
    val totalRows: Int get() = (totalMoveSlots + COLUMNS - 1) / COLUMNS

    // Current scroll position (row index)
    private var scrollRow: Int = 0

    // Track slot index ranges - NO case slots added to the menu!
    // We handle case slot interaction manually in clicked()
    private val playerInvStart: Int
    private val playerInvEnd: Int
    private val hotbarStart: Int
    private val hotbarEnd: Int

    init {
        // We do NOT add case slots to the menu - they are rendered manually
        // and we handle clicks on them ourselves in the clicked() method

        // Add player inventory slots (3 rows of 9)
        playerInvStart = 0
        for (row in 0 until 3) {
            for (col in 0 until 9) {
                val x = PLAYER_INV_X + col * SLOT_SIZE
                val y = PLAYER_INV_Y + row * SLOT_SIZE
                addSlot(Slot(playerInventory, col + row * 9 + 9, x, y))
            }
        }
        playerInvEnd = playerInvStart + 27

        // Add player hotbar slots
        hotbarStart = playerInvEnd
        for (col in 0 until 9) {
            val x = PLAYER_INV_X + col * SLOT_SIZE
            addSlot(Slot(playerInventory, col, x, PLAYER_HOTBAR_Y))
        }
        hotbarEnd = hotbarStart + 9
    }

    // ========================================
    // Search Functionality
    // ========================================

    /**
     * Set the search query and filter moves.
     */
    fun setSearchQuery(query: String) {
        val normalizedQuery = query.lowercase().trim()
        if (normalizedQuery == searchQuery) return

        searchQuery = normalizedQuery
        updateFilteredMoves()
        // Reset scroll when search changes
        scrollRow = 0
    }

    /**
     * Get the current search query.
     */
    fun getSearchQuery(): String = searchQuery

    /**
     * Update the filtered moves list based on current search query and filter mode.
     */
    private fun updateFilteredMoves() {
        var result = allMoves

        // Apply search filter
        if (searchQuery.isNotEmpty()) {
            result = result.filter { moveName ->
                matchesSearch(moveName, searchQuery)
            }
        }

        // Apply ownership filter
        result = when (filterMode) {
            FilterMode.ALL -> result
            FilterMode.OWNED_ONLY -> result.filter { isMoveStored(it) }
            FilterMode.MISSING_ONLY -> result.filter { !isMoveStored(it) }
        }

        filteredMoves = result
    }

    /**
     * Check if a move matches the search query.
     * Searches in: move name (internal and display), move type (with # prefix), tags (with # prefix).
     */
    private fun matchesSearch(moveName: String, query: String): Boolean {
        // Check if query contains tag searches (prefixed with #)
        val tagQueries = mutableListOf<String>()
        val nameQueries = mutableListOf<String>()

        // Split query by spaces and categorize
        query.split(" ").filter { it.isNotEmpty() }.forEach { part ->
            if (part.startsWith("#") && part.length > 1) {
                tagQueries.add(part.substring(1).lowercase())
            } else {
                nameQueries.add(part.lowercase())
            }
        }

        // Get move template from Cobblemon
        val moveTemplate = Moves.getByName(moveName)

        // Check name queries (must match internal name OR display name)
        val nameMatch = if (nameQueries.isEmpty()) {
            true
        } else {
            val internalName = moveName.lowercase()
            val displayName = getDisplayNameForMove(moveName).lowercase()

            nameQueries.all { nameQuery ->
                internalName.contains(nameQuery) || displayName.contains(nameQuery)
            }
        }

        if (!nameMatch) return false

        // Check tag queries (must match type OR damage category)
        if (tagQueries.isEmpty()) return true

        if (moveTemplate == null) return false

        return tagQueries.all { tagQuery ->
            matchesTag(moveTemplate, tagQuery)
        }
    }

    /**
     * Check if a move matches a tag query.
     * Matches against: elemental type (fire, water, etc.), damage category (physical, special, status)
     */
    private fun matchesTag(moveTemplate: MoveTemplate, tagQuery: String): Boolean {
        // Check elemental type
        val typeName = moveTemplate.elementalType.name.lowercase()
        if (typeName.contains(tagQuery)) return true

        // Check damage category
        val category = moveTemplate.damageCategory.name.lowercase()
        if (category.contains(tagQuery)) return true

        return false
    }

    /**
     * Get display name for a move.
     */
    private fun getDisplayNameForMove(moveName: String): String {
        val prefix = if (isTR) "tr_" else "tm_"
        val itemStack = SimpleTMsItems.getItemStackFromName(prefix + moveName)
        return if (!itemStack.isEmpty) {
            itemStack.hoverName.string
        } else {
            moveName
        }
    }

    /**
     * Get the filtered move list.
     */
    fun getFilteredMoves(): List<String> = filteredMoves

    // ========================================
    // Filter Mode
    // ========================================

    /**
     * Get the current filter mode.
     */
    fun getFilterMode(): FilterMode = filterMode

    /**
     * Set the filter mode.
     */
    fun setFilterMode(mode: FilterMode) {
        if (mode != filterMode) {
            filterMode = mode
            updateFilteredMoves()
            scrollRow = 0
        }
    }

    /**
     * Cycle to the next filter mode.
     */
    fun cycleFilterMode() {
        filterMode = when (filterMode) {
            FilterMode.ALL -> FilterMode.OWNED_ONLY
            FilterMode.OWNED_ONLY -> FilterMode.MISSING_ONLY
            FilterMode.MISSING_ONLY -> FilterMode.ALL
        }
        updateFilteredMoves()
        scrollRow = 0
    }

    // ========================================
    // Scroll Functionality
    // ========================================

    /**
     * Get the current scroll row.
     */
    fun getScrollRow(): Int = scrollRow

    /**
     * Get the maximum scroll row.
     */
    fun getMaxScrollRow(): Int = maxOf(0, totalRows - VISIBLE_ROWS)

    /**
     * Set the scroll position and update visible slots.
     */
    fun setScrollRow(row: Int) {
        val newRow = row.coerceIn(0, getMaxScrollRow())
        if (newRow != scrollRow) {
            scrollRow = newRow
        }
    }

    /**
     * Scroll by a delta amount (positive = down, negative = up).
     */
    fun scroll(delta: Int) {
        setScrollRow(scrollRow + delta)
    }

    /**
     * Get the move name for a visible slot, accounting for scroll position.
     * Uses the filtered list.
     */
    fun getMoveNameForVisibleSlot(visibleSlotIndex: Int): String? {
        val actualIndex = scrollRow * COLUMNS + visibleSlotIndex
        return if (actualIndex in filteredMoves.indices) filteredMoves[actualIndex] else null
    }

    /**
     * Get the actual move index for a visible slot.
     */
    fun getActualMoveIndex(visibleSlotIndex: Int): Int {
        return scrollRow * COLUMNS + visibleSlotIndex
    }

    // ========================================
    // Storage Methods
    // ========================================

    /**
     * Check if a specific move is stored by the player.
     * Uses server storage if available, otherwise falls back to client cache.
     */
    fun isMoveStored(moveName: String): Boolean {
        // On server, use actual storage
        storage?.let { return it.hasMove(playerUUID, moveName, isTR) }
        // On client, use synced cache
        return clientStoredMoves.contains(moveName)
    }

    /**
     * Get the count of stored moves.
     */
    fun getStoredCount(): Int {
        return allMoves.count { isMoveStored(it) }
    }

    /**
     * Get the total number of moves (unfiltered).
     */
    fun getTotalCount(): Int = allMoves.size

    /**
     * Update the client-side stored moves cache.
     * Called when syncing from server.
     */
    fun setClientStoredMoves(moves: Set<String>) {
        clientStoredMoves.clear()
        clientStoredMoves.addAll(moves)
    }

    /**
     * Add a move to client cache (for immediate UI feedback).
     */
    fun addToClientCache(moveName: String) {
        clientStoredMoves.add(moveName)
        // Re-filter in case filter mode is active
        if (filterMode != FilterMode.ALL) {
            updateFilteredMoves()
        }
    }

    /**
     * Remove a move from client cache (for immediate UI feedback).
     */
    fun removeFromClientCache(moveName: String) {
        clientStoredMoves.remove(moveName)
        // Re-filter in case filter mode is active
        if (filterMode != FilterMode.ALL) {
            updateFilteredMoves()
        }
    }

    /**
     * Check if this menu is still valid.
     */
    override fun stillValid(player: Player): Boolean {
        return true // The case item doesn't need to stay in a specific location
    }

    /**
     * Handle shift-clicking items between slots.
     */
    override fun quickMoveStack(player: Player, slotIndex: Int): ItemStack {
        val slot = slots.getOrNull(slotIndex) ?: return ItemStack.EMPTY
        if (!slot.hasItem()) return ItemStack.EMPTY

        val stack = slot.item
        val stackCopy = stack.copy()

        // All slots are player inventory slots, so shift-click tries to deposit into case
        val item = stack.item
        if (item is MoveLearnItem && item.isTR == isTR) {
            val moveName = item.moveName
            // Check if this move has a slot and isn't already stored
            val moveIndex = MoveCaseHelper.getSlotIndexForMove(moveName, isTR)
            if (moveIndex >= 0 && !isMoveStored(moveName)) {
                // Store the item on server
                if (player is ServerPlayer) {
                    storage?.addMove(playerUUID, moveName, isTR)
                }
                // Update client cache
                addToClientCache(moveName)
                // Consume ONE item from the stack
                stack.shrink(1)
                slot.setChanged()

                // Return a copy with count 1 to indicate we moved one item
                val result = stackCopy.copy()
                result.count = 1
                return result
            }
        }

        // If we couldn't deposit, try moving between inventory and hotbar
        if (slotIndex in playerInvStart until playerInvEnd) {
            // From main inventory to hotbar
            if (!moveItemStackTo(stack, hotbarStart, hotbarEnd, false)) {
                return ItemStack.EMPTY
            }
        } else if (slotIndex in hotbarStart until hotbarEnd) {
            // From hotbar to main inventory
            if (!moveItemStackTo(stack, playerInvStart, playerInvEnd, false)) {
                return ItemStack.EMPTY
            }
        }

        if (stack.isEmpty) {
            slot.setByPlayer(ItemStack.EMPTY)
        } else {
            slot.setChanged()
        }

        return if (stack.count == stackCopy.count) ItemStack.EMPTY else stackCopy
    }

    /**
     * Handle clicking on slots.
     * We intercept clicks in the case slot area and handle them manually.
     */
    override fun clicked(slotIndex: Int, button: Int, clickType: ClickType, player: Player) {
        // Let the parent handle player inventory slots
        super.clicked(slotIndex, button, clickType, player)
    }

    /**
     * Handle a click in the case slot area.
     * Called from the screen when clicking in the case grid.
     *
     * @param row The row in the visible grid (0-5)
     * @param col The column (0-8)
     * @param clickType The type of click
     * @return true if the click was handled
     */
    fun handleCaseSlotClick(row: Int, col: Int, clickType: ClickType): Boolean {
        val visibleIndex = row * COLUMNS + col
        val moveName = getMoveNameForVisibleSlot(visibleIndex) ?: return false

        val carriedItem = carried
        val isStored = isMoveStored(moveName)

        when (clickType) {
            ClickType.PICKUP -> {
                if (carriedItem.isEmpty) {
                    // Try to pick up stored item
                    if (isStored) {
                        // Remove from storage on server
                        if (player is ServerPlayer) {
                            storage?.removeMove(playerUUID, moveName, isTR)
                        }
                        // Update client cache
                        removeFromClientCache(moveName)
                        // Give item to cursor
                        val prefix = if (isTR) "tr_" else "tm_"
                        setCarried(SimpleTMsItems.getItemStackFromName(prefix + moveName))
                        return true
                    }
                } else {
                    // Try to deposit carried item
                    val item = carriedItem.item
                    if (item is MoveLearnItem && item.moveName == moveName && item.isTR == isTR) {
                        if (!isStored) {
                            // Add to storage on server
                            if (player is ServerPlayer) {
                                storage?.addMove(playerUUID, moveName, isTR)
                            }
                            // Update client cache
                            addToClientCache(moveName)
                            // Consume one item from cursor
                            carriedItem.shrink(1)
                            return true
                        }
                    }
                }
            }
            ClickType.QUICK_MOVE -> {
                // Shift-click on case slot - withdraw to inventory
                if (isStored) {
                    // Try to add to player inventory
                    val prefix = if (isTR) "tr_" else "tm_"
                    val itemToGive = SimpleTMsItems.getItemStackFromName(prefix + moveName)

                    // Try to insert into player inventory
                    if (moveItemStackTo(itemToGive, playerInvStart, hotbarEnd, true)) {
                        // Successfully added to inventory, remove from storage
                        if (player is ServerPlayer) {
                            storage?.removeMove(playerUUID, moveName, isTR)
                        }
                        // Update client cache
                        removeFromClientCache(moveName)
                        return true
                    }
                }
            }
            else -> {
                // Other click types not supported for case slots
            }
        }

        return false
    }

    /**
     * Check if a screen position is within the case slot area.
     * @return The (row, col) if in bounds, or null if outside
     */
    fun getCaseSlotAt(relativeX: Int, relativeY: Int): Pair<Int, Int>? {
        if (relativeX < 0 || relativeY < 0) return null
        if (relativeX >= COLUMNS * SLOT_SIZE || relativeY >= VISIBLE_ROWS * SLOT_SIZE) return null

        val col = relativeX / SLOT_SIZE
        val row = relativeY / SLOT_SIZE

        // Check if this slot actually exists (might be past the end of filtered moves list)
        val moveIndex = (scrollRow + row) * COLUMNS + col
        if (moveIndex >= filteredMoves.size) return null

        return Pair(row, col)
    }
}