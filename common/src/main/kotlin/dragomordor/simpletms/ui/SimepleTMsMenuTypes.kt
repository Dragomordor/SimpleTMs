package dragomordor.simpletms.ui

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dev.architectury.registry.menu.MenuRegistry
import dragomordor.simpletms.SimpleTMs
import net.minecraft.core.registries.Registries
import net.minecraft.world.inventory.MenuType

/**
 * Registry for all SimpleTMs menu types.
 */
object SimpleTMsMenuTypes {

    private val MENUS: DeferredRegister<MenuType<*>> = DeferredRegister.create(SimpleTMs.MOD_ID, Registries.MENU)

    /**
     * Menu type for the TM/TR Case.
     * Uses extended menu to pass:
     * - isTR flag
     * - stored moves with quantities
     * - optional Pokémon filter data
     * - whether to auto-enable the filter
     */
    val MOVE_CASE_MENU: RegistrySupplier<MenuType<MoveCaseMenu>> = MENUS.register("move_case_menu") {
        MenuRegistry.ofExtended { containerId, inventory, buf ->
            val isTR = buf.readBoolean()

            // Read stored moves with quantities
            val storedMovesCount = buf.readVarInt()
            val storedMoves = mutableMapOf<String, Int>()
            repeat(storedMovesCount) {
                val moveName = buf.readUtf()
                val quantity = buf.readVarInt()
                storedMoves[moveName] = quantity
            }

            // Read Pokémon filter data
            val hasPokemon = buf.readBoolean()
            val pokemonFilterData: PokemonFilterData?
            val autoEnableFilter: Boolean

            if (hasPokemon) {
                val speciesId = buf.readUtf()
                val formName = buf.readUtf()
                val displayName = buf.readUtf()

                // Read pre-calculated learnable moves
                val learnableCount = buf.readVarInt()
                val learnableMoves = mutableSetOf<String>()
                repeat(learnableCount) {
                    learnableMoves.add(buf.readUtf())
                }

                // Read whether to auto-enable the filter
                autoEnableFilter = buf.readBoolean()

                pokemonFilterData = PokemonFilterData(speciesId, formName, displayName, learnableMoves)
            } else {
                pokemonFilterData = null
                autoEnableFilter = false
            }

            MoveCaseMenu(containerId, inventory, isTR, storedMoves, null, pokemonFilterData, autoEnableFilter)
        }
    }

    /**
     * Register all menu types. Call this during mod initialization.
     */
    fun register() {
        MENUS.register()
    }
}

/**
 * Data class holding Pokémon filter information for client-side display.
 */
data class PokemonFilterData(
    val speciesId: String,
    val formName: String,
    val displayName: String,
    val learnableMoves: Set<String>
)