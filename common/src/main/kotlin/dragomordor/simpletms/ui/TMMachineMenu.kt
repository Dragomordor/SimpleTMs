package dragomordor.simpletms.ui

import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.pokemon.Pokemon
import dragomordor.simpletms.SimpleTMsItems
import dragomordor.simpletms.block.entity.TMMachineBlockEntity
import dragomordor.simpletms.item.custom.MoveLearnItem
import dragomordor.simpletms.util.MoveHelper
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

/**
 * Filter mode for TM/TR type display.
 */
enum class MoveTypeFilter {
    ALL,
    TM_ONLY,
    TR_ONLY
}

/**
 * Filter mode for ownership display.
 */
enum class OwnershipFilter {
    ALL,         // Show all moves
    OWNED_ONLY,  // Show only moves that are stored
    MISSING_ONLY // Show only moves that are not stored
}

/**
 * Menu/Container for the TM Machine block.
 *
 * Features:
 * - Shared/public inventory (not player-specific)
 * - Contains both TMs and TRs in one screen
 * - Per-player filtering (search, type filter, ownership filter, Pokémon filter)
 * - Multiple players can use simultaneously with independent filters
 * - Filter state persists between screen openings via ClientFilterStorage
 */
class TMMachineMenu(
    containerId: Int,
    private val playerInventory: Inventory,
    val blockEntity: TMMachineBlockEntity?,
    initialStoredMoves: Map<String, TMMachineBlockEntity.StoredMoveData> = emptyMap()
) : AbstractContainerMenu(SimpleTMsMenuTypes.TM_MACHINE_MENU.get(), containerId) {

    val player: Player = playerInventory.player

    // Client-side cache of stored moves (synced from server)
    private val clientStoredMoves: MutableMap<String, TMMachineBlockEntity.StoredMoveData> =
        initialStoredMoves.toMutableMap()

    // Per-player filter state - initialized from ClientFilterStorage on client
    private var searchQuery: String = ""
    private var typeFilter: MoveTypeFilter = MoveTypeFilter.ALL
    private var ownershipFilter: OwnershipFilter = OwnershipFilter.ALL
    private var pokemonFilterData: PokemonFilterData? = null
    private var isPokemonFilterEnabled: Boolean = false
    private var scrollRow: Int = 0

    // Cached filtered moves list
    private var filteredMoves: List<FilteredMoveEntry> = emptyList()
    private var isDirty: Boolean = true

    // All valid moves for display
    private val allMoves: List<String> = Moves.all()
        .filter { move ->
            val moveName = move.name.lowercase()
            SimpleTMsItems.ALL_MOVE_NAMES_WITH_TM_ITEMS.contains(moveName) ||
                    SimpleTMsItems.ALL_MOVE_NAMES_WITH_TR_ITEMS.contains(moveName)
        }
        .map { it.name.lowercase() }
        .sorted()

    /**
     * Entry representing a move in the filtered list.
     * Tracks whether it's available as TM, TR, or both.
     */
    data class FilteredMoveEntry(
        val moveName: String,
        val tmCount: Int,
        val trCount: Int,
        val isValidTM: Boolean,
        val isValidTR: Boolean,
        val canPokemonLearn: Boolean
    ) {
        fun hasAny(): Boolean = tmCount > 0 || trCount > 0
        fun hasTM(): Boolean = tmCount > 0
        fun hasTR(): Boolean = trCount > 0

        fun getDisplayCount(typeFilter: MoveTypeFilter): Int {
            return when (typeFilter) {
                MoveTypeFilter.ALL -> tmCount + trCount
                MoveTypeFilter.TM_ONLY -> tmCount
                MoveTypeFilter.TR_ONLY -> trCount
            }
        }
    }

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

    val playerInvStart: Int
    val playerInvEnd: Int
    val hotbarStart: Int
    val hotbarEnd: Int

    init {
        // Add player inventory slots
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

        // Load persisted filter state on client side
        if (player.level().isClientSide) {
            loadFilterState()
        }

        // Initial filter
        rebuildFilteredMoves()
    }

    /**
     * Load filter state from ClientFilterStorage (client-side only)
     */
    private fun loadFilterState() {
        typeFilter = ClientFilterStorage.getMachineTypeFilter()
        ownershipFilter = ClientFilterStorage.getMachineOwnershipFilter()
        searchQuery = ClientFilterStorage.getMachineSearchQuery()
        pokemonFilterData = ClientFilterStorage.getMachinePokemonFilter()
        isPokemonFilterEnabled = ClientFilterStorage.isMachinePokemonFilterEnabled()
    }

    /**
     * Save filter state to ClientFilterStorage (client-side only)
     */
    private fun saveFilterState() {
        if (player.level().isClientSide) {
            ClientFilterStorage.setMachineTypeFilter(typeFilter)
            ClientFilterStorage.setMachineOwnershipFilter(ownershipFilter)
            ClientFilterStorage.setMachineSearchQuery(searchQuery)
            ClientFilterStorage.setMachinePokemonFilter(pokemonFilterData)
            ClientFilterStorage.setMachinePokemonFilterEnabled(isPokemonFilterEnabled)
        }
    }

    // ========================================
    // Storage Access
    // ========================================

    /**
     * Get move data from client cache (client) or block entity (server).
     * On client side, we always use clientStoredMoves since it's synced via packets.
     * On server side, we read directly from the block entity.
     */
    private fun getMoveData(moveName: String): TMMachineBlockEntity.StoredMoveData? {
        // On client side, always use the synced cache for responsive updates
        if (player.level().isClientSide) {
            return clientStoredMoves[moveName]
        }
        // On server side, use the block entity directly
        blockEntity?.let {
            return it.getStoredQuantities()[moveName]
        }
        return clientStoredMoves[moveName]
    }

    fun getTMCount(moveName: String): Int {
        return getMoveData(moveName)?.tmCount ?: 0
    }

    fun getTRCount(moveName: String): Int {
        return getMoveData(moveName)?.trCount ?: 0
    }

    fun hasMove(moveName: String, isTR: Boolean): Boolean {
        val data = getMoveData(moveName) ?: return false
        return if (isTR) data.trCount > 0 else data.tmCount > 0
    }

    /**
     * Update client-side cache (called when receiving sync packets)
     */
    fun updateClientCache(moves: Map<String, TMMachineBlockEntity.StoredMoveData>) {
        clientStoredMoves.clear()
        clientStoredMoves.putAll(moves)
        isDirty = true
    }

    // ========================================
    // Filtering (Per-Player, Client-Side)
    // ========================================

    fun getSearchQuery(): String = searchQuery

    fun setSearchQuery(query: String) {
        if (searchQuery != query) {
            searchQuery = query
            isDirty = true
            scrollRow = 0
            saveFilterState()
        }
    }

    fun getTypeFilter(): MoveTypeFilter = typeFilter

    fun cycleTypeFilter() {
        typeFilter = when (typeFilter) {
            MoveTypeFilter.ALL -> MoveTypeFilter.TM_ONLY
            MoveTypeFilter.TM_ONLY -> MoveTypeFilter.TR_ONLY
            MoveTypeFilter.TR_ONLY -> MoveTypeFilter.ALL
        }
        isDirty = true
        scrollRow = 0
        saveFilterState()
    }

    fun setTypeFilter(filter: MoveTypeFilter) {
        if (typeFilter != filter) {
            typeFilter = filter
            isDirty = true
            scrollRow = 0
            saveFilterState()
        }
    }

    fun getOwnershipFilter(): OwnershipFilter = ownershipFilter

    fun cycleOwnershipFilter() {
        ownershipFilter = when (ownershipFilter) {
            OwnershipFilter.ALL -> OwnershipFilter.OWNED_ONLY
            OwnershipFilter.OWNED_ONLY -> OwnershipFilter.MISSING_ONLY
            OwnershipFilter.MISSING_ONLY -> OwnershipFilter.ALL
        }
        isDirty = true
        scrollRow = 0
        saveFilterState()
    }

    fun setOwnershipFilter(filter: OwnershipFilter) {
        if (ownershipFilter != filter) {
            ownershipFilter = filter
            isDirty = true
            scrollRow = 0
            saveFilterState()
        }
    }

    // ========================================
    // Pokémon Filter
    // ========================================

    fun getPokemonFilterData(): PokemonFilterData? = pokemonFilterData

    fun isPokemonFilterEnabled(): Boolean = isPokemonFilterEnabled

    fun setPokemonFilter(data: PokemonFilterData) {
        pokemonFilterData = data
        isPokemonFilterEnabled = true
        isDirty = true
        scrollRow = 0
        saveFilterState()
    }

    fun clearPokemonFilter() {
        pokemonFilterData = null
        isPokemonFilterEnabled = false
        isDirty = true
        saveFilterState()
    }

    fun togglePokemonFilter() {
        if (pokemonFilterData != null) {
            isPokemonFilterEnabled = !isPokemonFilterEnabled
            isDirty = true
            scrollRow = 0
            saveFilterState()
        }
    }

    // ========================================
    // Scroll
    // ========================================

    fun getScrollRow(): Int = scrollRow

    fun getMaxScrollRow(): Int {
        val itemCount = getDisplayItemCount()
        val totalRows = (itemCount + COLUMNS - 1) / COLUMNS
        return maxOf(0, totalRows - VISIBLE_ROWS)
    }

    fun setScrollRow(row: Int) {
        scrollRow = row.coerceIn(0, getMaxScrollRow())
    }

    fun scroll(delta: Int) {
        setScrollRow(scrollRow + delta)
    }

    /**
     * Get effective number of columns based on type filter.
     * Always 9 columns now - in ALL mode we show items flat, not side by side.
     */
    fun getEffectiveColumns(): Int = COLUMNS

    /**
     * Get total display item count for ALL mode (TMs + TRs separately)
     */
    fun getDisplayItemCount(): Int {
        val ownershipFilter = this.ownershipFilter
        val showGhosts = ownershipFilter == OwnershipFilter.ALL || ownershipFilter == OwnershipFilter.MISSING_ONLY

        return if (typeFilter == MoveTypeFilter.ALL) {
            getFilteredMoves().sumOf { entry ->
                var count = 0
                if (entry.isValidTM && (entry.tmCount > 0 || showGhosts)) count++
                if (entry.isValidTR && (entry.trCount > 0 || showGhosts)) count++
                count
            }
        } else {
            getFilteredMoves().size
        }
    }

    // ========================================
    // Filtered Moves List
    // ========================================

    fun getFilteredMoves(): List<FilteredMoveEntry> {
        if (isDirty) {
            rebuildFilteredMoves()
            isDirty = false
        }
        return filteredMoves
    }

    private fun rebuildFilteredMoves() {
        val query = searchQuery.lowercase().trim()
        val pokemonMoves = if (isPokemonFilterEnabled) pokemonFilterData?.learnableMoves else null

        filteredMoves = allMoves.mapNotNull { moveName ->
            val isValidTM = SimpleTMsItems.ALL_MOVE_NAMES_WITH_TM_ITEMS.contains(moveName)
            val isValidTR = SimpleTMsItems.ALL_MOVE_NAMES_WITH_TR_ITEMS.contains(moveName)

            // Type filter
            when (typeFilter) {
                MoveTypeFilter.TM_ONLY -> if (!isValidTM) return@mapNotNull null
                MoveTypeFilter.TR_ONLY -> if (!isValidTR) return@mapNotNull null
                MoveTypeFilter.ALL -> if (!isValidTM && !isValidTR) return@mapNotNull null
            }

            // Search filter
            if (query.isNotEmpty()) {
                val moveTemplate = Moves.getByName(moveName)
                val displayName = moveTemplate?.displayName?.string?.lowercase() ?: moveName
                val typeName = moveTemplate?.elementalType?.name?.lowercase() ?: ""

                val matchesSearch = when {
                    query.startsWith("#") -> {
                        val typeQuery = query.substring(1)
                        typeName.contains(typeQuery)
                    }
                    else -> displayName.contains(query) || moveName.contains(query)
                }
                if (!matchesSearch) return@mapNotNull null
            }

            // Pokémon filter
            val canLearn = pokemonMoves?.contains(moveName) ?: true
            if (isPokemonFilterEnabled && pokemonMoves != null && !canLearn) {
                return@mapNotNull null
            }

            val tmCount = getTMCount(moveName)
            val trCount = getTRCount(moveName)

            // Ownership filter
            when (ownershipFilter) {
                OwnershipFilter.OWNED_ONLY -> {
                    val hasOwned = when (typeFilter) {
                        MoveTypeFilter.ALL -> tmCount > 0 || trCount > 0
                        MoveTypeFilter.TM_ONLY -> tmCount > 0
                        MoveTypeFilter.TR_ONLY -> trCount > 0
                    }
                    if (!hasOwned) return@mapNotNull null
                }
                OwnershipFilter.MISSING_ONLY -> {
                    val isMissing = when (typeFilter) {
                        MoveTypeFilter.ALL -> tmCount == 0 && trCount == 0
                        MoveTypeFilter.TM_ONLY -> tmCount == 0
                        MoveTypeFilter.TR_ONLY -> trCount == 0
                    }
                    if (!isMissing) return@mapNotNull null
                }
                OwnershipFilter.ALL -> { /* No filter */ }
            }

            FilteredMoveEntry(
                moveName = moveName,
                tmCount = tmCount,
                trCount = trCount,
                isValidTM = isValidTM,
                isValidTR = isValidTR,
                canPokemonLearn = canLearn
            )
        }
    }

    fun markDirty() {
        isDirty = true
    }

    // ========================================
    // Slot Interaction
    // ========================================

    /**
     * Get the case slot position from mouse coordinates.
     * Returns (row, col) relative to visible area, or null if not over a slot.
     */
    fun getCaseSlotAt(relX: Int, relY: Int): Pair<Int, Int>? {
        if (relX < 0 || relY < 0) return null
        val col = relX / SLOT_SIZE
        val row = relY / SLOT_SIZE
        if (col >= COLUMNS || row >= VISIBLE_ROWS) return null
        return row to col
    }

    /**
     * Get move name for a visible slot index.
     * In ALL mode, accounts for double-slot moves.
     */
    fun getMoveNameForVisibleSlot(visibleIndex: Int): String? {
        val effectiveColumns = getEffectiveColumns()
        val absoluteIndex = scrollRow * effectiveColumns + visibleIndex
        val filtered = getFilteredMoves()
        return if (absoluteIndex in filtered.indices) filtered[absoluteIndex].moveName else null
    }

    /**
     * Get the filtered entry for a visible slot index.
     */
    fun getEntryForVisibleSlot(visibleIndex: Int): FilteredMoveEntry? {
        val effectiveColumns = getEffectiveColumns()
        val absoluteIndex = scrollRow * effectiveColumns + visibleIndex
        val filtered = getFilteredMoves()
        return if (absoluteIndex in filtered.indices) filtered[absoluteIndex] else null
    }

    /**
     * Handle clicking on a case slot.
     * Called on server via network packet.
     *
     * @param index For ALL mode, this is the move index in filteredMoves. For single modes,
     *              this is also the move index (visible slot offset by scroll).
     * @param isTR If true, clicking on TR slot; if false, clicking on TM slot
     * @param isShiftClick If true, transfer to inventory; if false, pick up to cursor
     * @return true if something happened
     */
    fun handleSlotClick(index: Int, isTR: Boolean, isShiftClick: Boolean): Boolean {
        if (player !is ServerPlayer) return false
        val blockEntity = this.blockEntity ?: return false

        // Get the move entry at the given index
        val filtered = getFilteredMoves()
        val moveName = if (index in filtered.indices) filtered[index].moveName else return false
        val hasItem = if (isTR) getTRCount(moveName) > 0 else getTMCount(moveName) > 0
        if (!hasItem) return false

        // Create the item stack
        val prefix = if (isTR) "tr_" else "tm_"
        val stack = SimpleTMsItems.getItemStackFromName(prefix + moveName)
        if (stack.isEmpty) return false

        if (isShiftClick) {
            // Shift-click: Transfer directly to player's inventory
            if (!player.inventory.add(stack)) {
                // Inventory full, drop at player's feet
                player.drop(stack, false)
            }
        } else {
            // Normal click: Pick up to cursor (carried item)
            val currentCarried = carried
            if (currentCarried.isEmpty) {
                // Cursor is empty, pick up the item
                setCarried(stack)
            } else if (ItemStack.isSameItemSameComponents(currentCarried, stack) &&
                currentCarried.count < currentCarried.maxStackSize) {
                // Same item type on cursor, add to it if possible
                currentCarried.grow(1)
                setCarried(currentCarried)
            } else {
                // Different item on cursor or cursor stack is full, can't pick up
                return false
            }
        }

        // Remove from storage
        blockEntity.removeMove(moveName, isTR, 1)
        isDirty = true
        return true
    }

    // ========================================
    // Menu Validation
    // ========================================

    override fun stillValid(player: Player): Boolean {
        blockEntity?.let { be ->
            val level = be.level ?: return false
            val pos = be.blockPos
            return player.distanceToSqr(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) <= 64.0
        }
        return true
    }

    override fun quickMoveStack(player: Player, slotIndex: Int): ItemStack {
        val slot = slots.getOrNull(slotIndex) ?: return ItemStack.EMPTY
        if (!slot.hasItem()) return ItemStack.EMPTY

        val stack = slot.item
        val originalStack = stack.copy()
        val item = stack.item

        // If it's a TM/TR item, try to deposit into machine
        if (item is MoveLearnItem && blockEntity != null) {
            val remaining = blockEntity.tryInsert(stack)
            if (remaining.count < originalStack.count) {
                slot.set(remaining)
                isDirty = true
                return originalStack
            }
        }

        // Standard inventory shift-click behavior
        if (slotIndex in playerInvStart until playerInvEnd) {
            if (!moveItemStackTo(stack, hotbarStart, hotbarEnd, false)) return ItemStack.EMPTY
        } else if (slotIndex in hotbarStart until hotbarEnd) {
            if (!moveItemStackTo(stack, playerInvStart, playerInvEnd, false)) return ItemStack.EMPTY
        }

        if (stack.isEmpty) slot.setByPlayer(ItemStack.EMPTY) else slot.setChanged()
        return if (stack.count == originalStack.count) ItemStack.EMPTY else originalStack
    }
}