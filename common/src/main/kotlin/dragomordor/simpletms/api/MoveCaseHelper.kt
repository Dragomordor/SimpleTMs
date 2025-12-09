package dragomordor.simpletms.api

import dragomordor.simpletms.SimpleTMsItems

/**
 * Helper object for managing the sorted list of moves available in TM/TR cases.
 * This provides the canonical ordering of moves for slot positioning.
 */
object MoveCaseHelper {

    // Cached sorted lists - populated after items are registered
    private var sortedTMMoveNames: List<String>? = null
    private var sortedTRMoveNames: List<String>? = null

    /**
     * Get the alphabetically sorted list of all TM move names.
     * The index in this list corresponds to the slot index in the TM case.
     */
    fun getSortedTMMoveNames(): List<String> {
        if (sortedTMMoveNames == null) {
            sortedTMMoveNames = SimpleTMsItems.ALL_MOVE_NAMES_WITH_TM_ITEMS.sorted()
        }
        return sortedTMMoveNames!!
    }

    /**
     * Get the alphabetically sorted list of all TR move names.
     * The index in this list corresponds to the slot index in the TR case.
     */
    fun getSortedTRMoveNames(): List<String> {
        if (sortedTRMoveNames == null) {
            sortedTRMoveNames = SimpleTMsItems.ALL_MOVE_NAMES_WITH_TR_ITEMS.sorted()
        }
        return sortedTRMoveNames!!
    }

    /**
     * Get sorted move names based on whether it's TR or TM
     */
    fun getSortedMoveNames(isTR: Boolean): List<String> {
        return if (isTR) getSortedTRMoveNames() else getSortedTMMoveNames()
    }

    /**
     * Get the slot index for a specific move name
     * @return the slot index, or -1 if the move is not found
     */
    fun getSlotIndexForMove(moveName: String, isTR: Boolean): Int {
        return getSortedMoveNames(isTR).indexOf(moveName)
    }

    /**
     * Get the move name for a specific slot index
     * @return the move name, or null if the index is out of bounds
     */
    fun getMoveNameForSlot(slotIndex: Int, isTR: Boolean): String? {
        val moves = getSortedMoveNames(isTR)
        return if (slotIndex in moves.indices) moves[slotIndex] else null
    }

    /**
     * Get the total number of TM slots
     */
    fun getTMSlotCount(): Int {
        return getSortedTMMoveNames().size
    }

    /**
     * Get the total number of TR slots
     */
    fun getTRSlotCount(): Int {
        return getSortedTRMoveNames().size
    }

    /**
     * Get slot count based on whether it's TR or TM
     */
    fun getSlotCount(isTR: Boolean): Int {
        return if (isTR) getTRSlotCount() else getTMSlotCount()
    }

    /**
     * Calculate the number of rows needed to display all moves (9 slots per row)
     */
    fun getRowCount(isTR: Boolean): Int {
        val slotCount = getSlotCount(isTR)
        return (slotCount + 8) / 9 // Ceiling division
    }

    /**
     * Invalidate the cached lists. Call this if moves are dynamically added.
     * Typically not needed as moves are registered at startup.
     */
    fun invalidateCache() {
        sortedTMMoveNames = null
        sortedTRMoveNames = null
    }
}