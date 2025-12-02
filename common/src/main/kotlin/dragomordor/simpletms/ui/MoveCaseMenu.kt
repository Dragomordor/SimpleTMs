package dragomordor.simpletms.ui

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.SimpleTMsItems
import dragomordor.simpletms.api.MoveCaseHelper
import dragomordor.simpletms.api.MoveCaseStorage
import dragomordor.simpletms.item.custom.MoveLearnItem
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import java.util.UUID

/**
 * Filter mode for displaying moves in the case.
 */
enum class FilterMode {
    ALL,
    OWNED_ONLY,
    MISSING_ONLY
}

/**
 * Menu/Container for the TM/TR Case.
 *
 * TMs can only store 1 per move (they have durability).
 * TRs can store up to their stack size per move.
 */
class MoveCaseMenu(
    containerId: Int,
    private val playerInventory: Inventory,
    val isTR: Boolean,
    initialStoredMoves: Map<String, Int> = emptyMap()
) : AbstractContainerMenu(SimpleTMsMenuTypes.MOVE_CASE_MENU.get(), containerId) {

    val player: Player = playerInventory.player
    private val playerUUID: UUID = player.uuid

    private val storage: MoveCaseStorage?
        get() = if (player is ServerPlayer) MoveCaseStorage.get(player) else null

    // Client-side cache: move name -> quantity
    private val clientStoredMoves: MutableMap<String, Int> = initialStoredMoves.toMutableMap()

    companion object {
        const val COLUMNS = 9
        const val VISIBLE_ROWS = 6
        const val SLOT_SIZE = 18
        const val CASE_SLOTS_X = 8
        const val CASE_SLOTS_Y = 18
        const val PLAYER_INV_X = 8
        const val PLAYER_INV_Y = 140
        const val PLAYER_HOTBAR_Y = 198
    }

    private val allMoves: List<String> = MoveCaseHelper.getSortedMoveNames(isTR)
    private var filteredMoves: List<String> = allMoves
    private var searchQuery: String = ""
    private var filterMode: FilterMode = FilterMode.ALL

    val totalMoveSlots: Int get() = filteredMoves.size
    val totalRows: Int get() = (totalMoveSlots + COLUMNS - 1) / COLUMNS

    private var scrollRow: Int = 0

    val playerInvStart: Int
    val playerInvEnd: Int
    val hotbarStart: Int
    val hotbarEnd: Int

    // Max stack size for this case type
    val maxStackSize: Int get() = if (isTR) SimpleTMs.config.trStackSize else 1

    init {
        playerInvStart = 0
        for (row in 0 until 3) {
            for (col in 0 until 9) {
                val x = PLAYER_INV_X + col * SLOT_SIZE
                val y = PLAYER_INV_Y + row * SLOT_SIZE
                addSlot(Slot(playerInventory, col + row * 9 + 9, x, y))
            }
        }
        playerInvEnd = playerInvStart + 27

        hotbarStart = playerInvEnd
        for (col in 0 until 9) {
            val x = PLAYER_INV_X + col * SLOT_SIZE
            addSlot(Slot(playerInventory, col, x, PLAYER_HOTBAR_Y))
        }
        hotbarEnd = hotbarStart + 9
    }

    // Search
    fun setSearchQuery(query: String) {
        val normalizedQuery = query.lowercase().trim()
        if (normalizedQuery == searchQuery) return
        searchQuery = normalizedQuery
        updateFilteredMoves()
        scrollRow = 0
    }

    fun getSearchQuery(): String = searchQuery

    private fun updateFilteredMoves() {
        var result = allMoves
        if (searchQuery.isNotEmpty()) {
            result = result.filter { matchesSearch(it, searchQuery) }
        }
        result = when (filterMode) {
            FilterMode.ALL -> result
            FilterMode.OWNED_ONLY -> result.filter { getMoveQuantity(it) > 0 }
            FilterMode.MISSING_ONLY -> result.filter { getMoveQuantity(it) == 0 }
        }
        filteredMoves = result

        // Adjust scroll if we're now past the end
        if (scrollRow > getMaxScrollRow()) {
            scrollRow = getMaxScrollRow()
        }
    }

    private fun matchesSearch(moveName: String, query: String): Boolean {
        val tagQueries = mutableListOf<String>()
        val nameQueries = mutableListOf<String>()

        query.split(" ").filter { it.isNotEmpty() }.forEach { part ->
            if (part.startsWith("#") && part.length > 1) {
                tagQueries.add(part.substring(1).lowercase())
            } else {
                nameQueries.add(part.lowercase())
            }
        }

        val moveTemplate = Moves.getByName(moveName)

        val nameMatch = if (nameQueries.isEmpty()) true
        else {
            val internalName = moveName.lowercase()
            val displayName = getDisplayNameForMove(moveName).lowercase()
            nameQueries.all { internalName.contains(it) || displayName.contains(it) }
        }

        if (!nameMatch) return false
        if (tagQueries.isEmpty()) return true
        if (moveTemplate == null) return false

        return tagQueries.all { matchesTag(moveTemplate, it) }
    }

    private fun matchesTag(moveTemplate: MoveTemplate, tagQuery: String): Boolean {
        if (moveTemplate.elementalType.name.lowercase().contains(tagQuery)) return true
        if (moveTemplate.damageCategory.name.lowercase().contains(tagQuery)) return true
        return false
    }

    private fun getDisplayNameForMove(moveName: String): String {
        val prefix = if (isTR) "tr_" else "tm_"
        val itemStack = SimpleTMsItems.getItemStackFromName(prefix + moveName)
        return if (!itemStack.isEmpty) itemStack.hoverName.string else moveName
    }

    fun getFilteredMoves(): List<String> = filteredMoves

    // Filter
    fun getFilterMode(): FilterMode = filterMode

    fun cycleFilterMode() {
        filterMode = when (filterMode) {
            FilterMode.ALL -> FilterMode.OWNED_ONLY
            FilterMode.OWNED_ONLY -> FilterMode.MISSING_ONLY
            FilterMode.MISSING_ONLY -> FilterMode.ALL
        }
        updateFilteredMoves()
        scrollRow = 0
    }

    // Scroll
    fun getScrollRow(): Int = scrollRow
    fun getMaxScrollRow(): Int = maxOf(0, totalRows - VISIBLE_ROWS)
    fun setScrollRow(row: Int) { scrollRow = row.coerceIn(0, getMaxScrollRow()) }
    fun scroll(delta: Int) { setScrollRow(scrollRow + delta) }

    fun getMoveNameForVisibleSlot(visibleSlotIndex: Int): String? {
        val actualIndex = scrollRow * COLUMNS + visibleSlotIndex
        return if (actualIndex in filteredMoves.indices) filteredMoves[actualIndex] else null
    }

    // Storage - now with quantities

    /**
     * Check if a move is stored (has quantity > 0)
     */
    fun isMoveStored(moveName: String): Boolean {
        return getMoveQuantity(moveName) > 0
    }

    /**
     * Get quantity of a move stored
     */
    fun getMoveQuantity(moveName: String): Int {
        storage?.let { return it.getMoveQuantity(playerUUID, moveName, isTR) }
        return clientStoredMoves[moveName] ?: 0
    }

    /**
     * Check if more of this move can be added
     */
    fun canAddMore(moveName: String): Boolean {
        return getMoveQuantity(moveName) < maxStackSize
    }

    fun getStoredCount(): Int = allMoves.count { isMoveStored(it) }
    fun getTotalCount(): Int = allMoves.size

    /**
     * Get total items stored (sum of all quantities)
     */
    fun getTotalItemsStored(): Int {
        return allMoves.sumOf { getMoveQuantity(it) }
    }

    private fun addToStorage(moveName: String, amount: Int = 1): Int {
        val currentQty = getMoveQuantity(moveName)
        val canAdd = (maxStackSize - currentQty).coerceAtLeast(0)
        val toAdd = amount.coerceAtMost(canAdd)

        if (toAdd > 0) {
            if (player is ServerPlayer) {
                storage?.addMove(playerUUID, moveName, isTR, toAdd)
            }
            clientStoredMoves[moveName] = currentQty + toAdd
            if (filterMode != FilterMode.ALL) updateFilteredMoves()
        }
        return toAdd
    }

    private fun removeFromStorage(moveName: String, amount: Int = 1): Int {
        val currentQty = getMoveQuantity(moveName)
        val toRemove = amount.coerceAtMost(currentQty)

        if (toRemove > 0) {
            if (player is ServerPlayer) {
                storage?.removeMove(playerUUID, moveName, isTR, toRemove)
            }
            val newQty = currentQty - toRemove
            if (newQty <= 0) {
                clientStoredMoves.remove(moveName)
            } else {
                clientStoredMoves[moveName] = newQty
            }
            if (filterMode != FilterMode.ALL) updateFilteredMoves()
        }
        return toRemove
    }

    override fun stillValid(player: Player): Boolean = true

    // Shift-click from player inventory - deposit as many as possible
    override fun quickMoveStack(player: Player, slotIndex: Int): ItemStack {
        val slot = slots.getOrNull(slotIndex) ?: return ItemStack.EMPTY
        if (!slot.hasItem()) return ItemStack.EMPTY

        val stack = slot.item
        val stackCopy = stack.copy()

        val item = stack.item
        if (item is MoveLearnItem && item.isTR == isTR) {
            val moveName = item.moveName
            val moveIndex = MoveCaseHelper.getSlotIndexForMove(moveName, isTR)
            if (moveIndex >= 0 && canAddMore(moveName)) {
                val added = addToStorage(moveName, stack.count)
                if (added > 0) {
                    stack.shrink(added)
                    slot.setChanged()
                    return stackCopy.copy().also { it.count = added }
                }
            }
        }

        if (slotIndex in playerInvStart until playerInvEnd) {
            if (!moveItemStackTo(stack, hotbarStart, hotbarEnd, false)) return ItemStack.EMPTY
        } else if (slotIndex in hotbarStart until hotbarEnd) {
            if (!moveItemStackTo(stack, playerInvStart, playerInvEnd, false)) return ItemStack.EMPTY
        }

        if (stack.isEmpty) slot.setByPlayer(ItemStack.EMPTY) else slot.setChanged()
        return if (stack.count == stackCopy.count) ItemStack.EMPTY else stackCopy
    }

    /**
     * Handle case slot click by move name.
     * Called from network packet on server - uses move name to avoid filter/scroll sync issues.
     */
    fun handleCaseSlotClickByName(moveName: String, isShiftClick: Boolean): Boolean {
        // Validate the move exists
        if (!allMoves.contains(moveName)) return false

        val storedQty = getMoveQuantity(moveName)
        val carriedItem = carried

        if (isShiftClick) {
            // Shift-click: withdraw all to inventory
            if (storedQty > 0) {
                val prefix = if (isTR) "tr_" else "tm_"
                val itemToGive = SimpleTMsItems.getItemStackFromName(prefix + moveName)
                itemToGive.count = storedQty

                val originalCount = itemToGive.count
                if (moveItemStackTo(itemToGive, playerInvStart, hotbarEnd, true)) {
                    val added = originalCount - itemToGive.count
                    if (added > 0) {
                        removeFromStorage(moveName, added)
                    }
                    if (!itemToGive.isEmpty) {
                        if (moveItemStackTo(itemToGive, playerInvStart, hotbarEnd, false)) {
                            val moreAdded = storedQty - added - itemToGive.count
                            if (moreAdded > 0) {
                                removeFromStorage(moveName, moreAdded)
                            }
                        }
                    }
                    return true
                }
            }
        } else {
            // Normal click
            if (carriedItem.isEmpty) {
                // Pick up stored items
                if (storedQty > 0) {
                    val pickupAmount = storedQty.coerceAtMost(64)
                    removeFromStorage(moveName, pickupAmount)
                    val prefix = if (isTR) "tr_" else "tm_"
                    val pickedUp = SimpleTMsItems.getItemStackFromName(prefix + moveName)
                    pickedUp.count = pickupAmount
                    setCarried(pickedUp)
                    return true
                }
            } else {
                // Deposit carried items
                val item = carriedItem.item
                if (item is MoveLearnItem && item.moveName == moveName && item.isTR == isTR) {
                    if (canAddMore(moveName)) {
                        val added = addToStorage(moveName, carriedItem.count)
                        if (added > 0) {
                            carriedItem.shrink(added)
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    /**
     * Handle case slot click by visible index (for client-side immediate feedback).
     */
    fun handleCaseSlotClick(visibleIndex: Int, isShiftClick: Boolean): Boolean {
        val moveName = getMoveNameForVisibleSlot(visibleIndex) ?: return false
        return handleCaseSlotClickByName(moveName, isShiftClick)
    }

    fun getCaseSlotAt(relativeX: Int, relativeY: Int): Pair<Int, Int>? {
        if (relativeX < 0 || relativeY < 0) return null
        if (relativeX >= COLUMNS * SLOT_SIZE || relativeY >= VISIBLE_ROWS * SLOT_SIZE) return null
        val col = relativeX / SLOT_SIZE
        val row = relativeY / SLOT_SIZE
        val moveIndex = (scrollRow + row) * COLUMNS + col
        if (moveIndex >= filteredMoves.size) return null
        return Pair(row, col)
    }
}