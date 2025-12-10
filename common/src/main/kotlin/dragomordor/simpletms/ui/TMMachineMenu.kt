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
    initialStoredMoves: Map<String, TMMachineBlockEntity.StoredMoveData> = emptyMap(),
    initialPokemonFilter: PokemonFilterData? = null
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
        val tmDamage: Int,  // Damage value for TMs (0 = full durability)
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

        // Apply initial Pokémon filter from menu packet (if provided)
        // This is a fresh selection from party selection, so enable it
        if (initialPokemonFilter != null) {
            pokemonFilterData = initialPokemonFilter
            isPokemonFilterEnabled = true
            // Save immediately so it persists
            if (player.level().isClientSide) {
                saveFilterState()
            }
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

    fun getTMDamage(moveName: String): Int {
        return getMoveData(moveName)?.tmDamage ?: 0
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

    /**
     * Update client-side cache immediately when withdrawing (for responsive UI).
     * Called on the client side when a slot is clicked, before the server processes it.
     * This provides immediate visual feedback while the server sync packet is in flight.
     */
    fun updateClientCacheForWithdrawal(moveName: String, isTR: Boolean, amount: Int = 1) {
        val data = clientStoredMoves[moveName] ?: return
        if (isTR) {
            data.trCount = (data.trCount - amount).coerceAtLeast(0)
        } else {
            data.tmCount = (data.tmCount - amount).coerceAtLeast(0)
        }
        if (data.isEmpty()) {
            clientStoredMoves.remove(moveName)
        }
        isDirty = true
    }

    // ========================================
    // Filtering (Per-Player, Client-Side)
    // ========================================

    fun setSearchQuery(query: String) {
        val normalizedQuery = query.lowercase().trim()
        if (normalizedQuery == searchQuery) return
        searchQuery = normalizedQuery
        isDirty = true
        scrollRow = 0
        saveFilterState()
    }

    fun getSearchQuery(): String = searchQuery

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

    fun getTypeFilter(): MoveTypeFilter = typeFilter

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

    fun getOwnershipFilter(): OwnershipFilter = ownershipFilter

    // ========================================
    // Pokémon Filter
    // ========================================

    fun hasPokemonFilter(): Boolean = pokemonFilterData != null

    fun isPokemonFilterEnabled(): Boolean = isPokemonFilterEnabled && pokemonFilterData != null

    fun togglePokemonFilter() {
        if (pokemonFilterData != null) {
            isPokemonFilterEnabled = !isPokemonFilterEnabled
            isDirty = true
            scrollRow = 0
            saveFilterState()
        }
    }

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
        scrollRow = 0
        saveFilterState()
    }

    fun getPokemonDisplayName(): String? = pokemonFilterData?.displayName

    fun getPokemonLearnableCount(): Int = pokemonFilterData?.learnableMoves?.size ?: 0

    fun getPokemonFilterData(): PokemonFilterData? = pokemonFilterData

    // ========================================
    // Scroll
    // ========================================

    fun getScrollRow(): Int = scrollRow

    fun getMaxScrollRow(): Int {
        val filtered = getFilteredMoves()
        val effectiveColumns = getEffectiveColumns()
        val totalRows = (filtered.size + effectiveColumns - 1) / effectiveColumns
        return maxOf(0, totalRows - VISIBLE_ROWS)
    }

    fun setScrollRow(row: Int) {
        scrollRow = row.coerceIn(0, getMaxScrollRow())
    }

    fun scroll(delta: Int) {
        setScrollRow(scrollRow + delta)
    }

    /**
     * Get effective columns based on type filter.
     * In ALL mode, we show TM and TR side by side, so effective columns is COLUMNS/2.
     */
    fun getEffectiveColumns(): Int {
        return if (typeFilter == MoveTypeFilter.ALL) COLUMNS / 2 else COLUMNS
    }

    // ========================================
    // Filtered Moves
    // ========================================

    fun getFilteredMoves(): List<FilteredMoveEntry> {
        if (isDirty) {
            filteredMoves = rebuildFilteredMoves()
            isDirty = false
        }
        return filteredMoves
    }

    private fun matchesSearch(moveName: String, query: String): Boolean {
        if (query.isEmpty()) return true

        val tagQueries = mutableListOf<String>()
        val nameQueries = mutableListOf<String>()

        query.split(" ").filter { it.isNotEmpty() }.forEach { part ->
            if (part.startsWith("#") && part.length > 1) {
                tagQueries.add(part.substring(1).lowercase())
            } else {
                nameQueries.add(part.lowercase())
            }
        }

        // Check name queries
        val displayName = moveName.replace("_", " ")
        for (nameQuery in nameQueries) {
            if (!displayName.contains(nameQuery) && !moveName.contains(nameQuery)) {
                return false
            }
        }

        // Check tag queries (type matching)
        if (tagQueries.isNotEmpty()) {
            val move = Moves.getByName(moveName) ?: return false
            val moveType = move.elementalType.name.lowercase()
            for (tagQuery in tagQueries) {
                if (!moveType.startsWith(tagQuery)) {
                    return false
                }
            }
        }

        return true
    }

    private fun rebuildFilteredMoves(): List<FilteredMoveEntry> {
        return allMoves.mapNotNull { moveName ->
            // Search filter
            if (!matchesSearch(moveName, searchQuery)) return@mapNotNull null

            val isValidTM = SimpleTMsItems.ALL_MOVE_NAMES_WITH_TM_ITEMS.contains(moveName)
            val isValidTR = SimpleTMsItems.ALL_MOVE_NAMES_WITH_TR_ITEMS.contains(moveName)

            // Type filter
            when (typeFilter) {
                MoveTypeFilter.TM_ONLY -> if (!isValidTM) return@mapNotNull null
                MoveTypeFilter.TR_ONLY -> if (!isValidTR) return@mapNotNull null
                MoveTypeFilter.ALL -> { /* No type filter */ }
            }

            // Pokémon learnset filter
            val canLearn = if (isPokemonFilterEnabled && pokemonFilterData != null) {
                pokemonFilterData!!.learnableMoves.contains(moveName)
            } else {
                true
            }
            if (isPokemonFilterEnabled && pokemonFilterData != null && !canLearn) {
                return@mapNotNull null
            }

            val tmCount = getTMCount(moveName)
            val trCount = getTRCount(moveName)
            val tmDamage = getTMDamage(moveName)

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
                tmDamage = tmDamage,
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

    /**
     * Handle clicking on a slot by move name.
     * Called on server via network packet.
     */
    fun handleSlotClickByName(moveName: String, isTR: Boolean, isShiftClick: Boolean): Boolean {
        if (player !is ServerPlayer) return false
        val blockEntity = this.blockEntity ?: return false

        val currentCount = if (isTR) getTRCount(moveName) else getTMCount(moveName)
        if (currentCount <= 0) return false

        // Create the item stack
        val prefix = if (isTR) "tr_" else "tm_"
        val stack = SimpleTMsItems.getItemStackFromName(prefix + moveName)
        if (stack.isEmpty) return false

        // For TMs, restore the damage value
        if (!isTR) {
            stack.damageValue = blockEntity.getTMDamage(moveName)
        }

        if (isShiftClick) {
            // Shift-click: Transfer entire stack to player's inventory
            val withdrawAmount = currentCount.coerceAtMost(stack.maxStackSize)
            stack.count = withdrawAmount

            if (!player.inventory.add(stack)) {
                // Inventory full or partially full, drop remainder at player's feet
                if (!stack.isEmpty && stack.count > 0) {
                    player.drop(stack, false)
                }
            }
            // All withdrawAmount items were taken (added to inv or dropped)
            blockEntity.removeMove(moveName, isTR, withdrawAmount)
        } else {
            // Normal click: Pick up 1 to cursor (carried item)
            val currentCarried = carried
            if (currentCarried.isEmpty) {
                // Cursor is empty, pick up the item
                setCarried(stack)
                blockEntity.removeMove(moveName, isTR, 1)
            } else if (ItemStack.isSameItemSameComponents(currentCarried, stack) &&
                currentCarried.count < currentCarried.maxStackSize) {
                // Same item type on cursor, add to it if possible
                currentCarried.grow(1)
                setCarried(currentCarried)
                blockEntity.removeMove(moveName, isTR, 1)
            } else {
                // Different item on cursor or cursor stack is full, can't pick up
                return false
            }
        }

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
            val moveName = item.moveName
            val isTR = item.isTR

            // Check if we can add more - use block entity for server, client cache for client
            val currentCount = if (isTR) getTRCount(moveName) else getTMCount(moveName)
            val maxCount = if (isTR) blockEntity.maxTRStackSize else 1
            val canAdd = (maxCount - currentCount).coerceAtLeast(0)
            val toAdd = stack.count.coerceAtMost(canAdd)

            if (toAdd > 0) {
                // Only server side processes the actual insertion
                // Client will be updated via sync packet from blockEntity.tryInsert -> syncToClients
                if (!player.level().isClientSide) {
                    val damage = if (!isTR) stack.damageValue else 0
                    blockEntity.addMove(moveName, isTR, toAdd, damage)
                }

                stack.shrink(toAdd)
                slot.setChanged()
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