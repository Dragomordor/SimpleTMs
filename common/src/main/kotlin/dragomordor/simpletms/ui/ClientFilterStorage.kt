package dragomordor.simpletms.ui

/**
 * Client-side storage for filter settings.
 * Persists filter state between screen openings within the same game session.
 *
 * This is intentionally client-side only - filter preferences are per-player
 * and don't need to be synced to the server.
 */
object ClientFilterStorage {

    // ========================================
    // TM Machine Filter State
    // ========================================

    private var machineTypeFilter: MoveTypeFilter = MoveTypeFilter.ALL
    private var machineOwnershipFilter: OwnershipFilter = OwnershipFilter.ALL
    private var machineSearchQuery: String = ""
    private var machinePokemonFilter: PokemonFilterData? = null
    private var machinePokemonFilterEnabled: Boolean = false

    fun getMachineTypeFilter(): MoveTypeFilter = machineTypeFilter
    fun setMachineTypeFilter(filter: MoveTypeFilter) { machineTypeFilter = filter }

    fun getMachineOwnershipFilter(): OwnershipFilter = machineOwnershipFilter
    fun setMachineOwnershipFilter(filter: OwnershipFilter) { machineOwnershipFilter = filter }

    fun getMachineSearchQuery(): String = machineSearchQuery
    fun setMachineSearchQuery(query: String) { machineSearchQuery = query }

    fun getMachinePokemonFilter(): PokemonFilterData? = machinePokemonFilter
    fun setMachinePokemonFilter(filter: PokemonFilterData?) { machinePokemonFilter = filter }

    fun isMachinePokemonFilterEnabled(): Boolean = machinePokemonFilterEnabled
    fun setMachinePokemonFilterEnabled(enabled: Boolean) { machinePokemonFilterEnabled = enabled }

    // ========================================
    // TM Case Filter State
    // ========================================

    private var tmCaseFilterMode: FilterMode = FilterMode.ALL
    private var tmCaseSearchQuery: String = ""
    private var tmCasePokemonFilter: PokemonFilterData? = null
    private var tmCasePokemonFilterEnabled: Boolean = false

    fun getTMCaseFilterMode(): FilterMode = tmCaseFilterMode
    fun setTMCaseFilterMode(mode: FilterMode) { tmCaseFilterMode = mode }

    fun getTMCaseSearchQuery(): String = tmCaseSearchQuery
    fun setTMCaseSearchQuery(query: String) { tmCaseSearchQuery = query }

    fun getTMCasePokemonFilter(): PokemonFilterData? = tmCasePokemonFilter
    fun setTMCasePokemonFilter(filter: PokemonFilterData?) { tmCasePokemonFilter = filter }

    fun isTMCasePokemonFilterEnabled(): Boolean = tmCasePokemonFilterEnabled
    fun setTMCasePokemonFilterEnabled(enabled: Boolean) { tmCasePokemonFilterEnabled = enabled }

    // ========================================
    // TR Case Filter State
    // ========================================

    private var trCaseFilterMode: FilterMode = FilterMode.ALL
    private var trCaseSearchQuery: String = ""
    private var trCasePokemonFilter: PokemonFilterData? = null
    private var trCasePokemonFilterEnabled: Boolean = false

    fun getTRCaseFilterMode(): FilterMode = trCaseFilterMode
    fun setTRCaseFilterMode(mode: FilterMode) { trCaseFilterMode = mode }

    fun getTRCaseSearchQuery(): String = trCaseSearchQuery
    fun setTRCaseSearchQuery(query: String) { trCaseSearchQuery = query }

    fun getTRCasePokemonFilter(): PokemonFilterData? = trCasePokemonFilter
    fun setTRCasePokemonFilter(filter: PokemonFilterData?) { trCasePokemonFilter = filter }

    fun isTRCasePokemonFilterEnabled(): Boolean = trCasePokemonFilterEnabled
    fun setTRCasePokemonFilterEnabled(enabled: Boolean) { trCasePokemonFilterEnabled = enabled }

    // ========================================
    // Convenience Methods for Cases
    // ========================================

    fun getCaseFilterMode(isTR: Boolean): FilterMode =
        if (isTR) trCaseFilterMode else tmCaseFilterMode

    fun setCaseFilterMode(isTR: Boolean, mode: FilterMode) {
        if (isTR) trCaseFilterMode = mode else tmCaseFilterMode = mode
    }

    fun getCaseSearchQuery(isTR: Boolean): String =
        if (isTR) trCaseSearchQuery else tmCaseSearchQuery

    fun setCaseSearchQuery(isTR: Boolean, query: String) {
        if (isTR) trCaseSearchQuery = query else tmCaseSearchQuery = query
    }

    fun getCasePokemonFilter(isTR: Boolean): PokemonFilterData? =
        if (isTR) trCasePokemonFilter else tmCasePokemonFilter

    fun setCasePokemonFilter(isTR: Boolean, filter: PokemonFilterData?) {
        if (isTR) trCasePokemonFilter = filter else tmCasePokemonFilter = filter
    }

    fun isCasePokemonFilterEnabled(isTR: Boolean): Boolean =
        if (isTR) trCasePokemonFilterEnabled else tmCasePokemonFilterEnabled

    fun setCasePokemonFilterEnabled(isTR: Boolean, enabled: Boolean) {
        if (isTR) trCasePokemonFilterEnabled = enabled else tmCasePokemonFilterEnabled = enabled
    }

    // ========================================
    // Reset Methods
    // ========================================

    /**
     * Reset all filter state. Called when player logs out or changes dimensions.
     */
    fun resetAll() {
        machineTypeFilter = MoveTypeFilter.ALL
        machineOwnershipFilter = OwnershipFilter.ALL
        machineSearchQuery = ""
        machinePokemonFilter = null
        machinePokemonFilterEnabled = false

        tmCaseFilterMode = FilterMode.ALL
        tmCaseSearchQuery = ""
        tmCasePokemonFilter = null
        tmCasePokemonFilterEnabled = false

        trCaseFilterMode = FilterMode.ALL
        trCaseSearchQuery = ""
        trCasePokemonFilter = null
        trCasePokemonFilterEnabled = false
    }
}