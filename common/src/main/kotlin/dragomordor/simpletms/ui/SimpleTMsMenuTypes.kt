package dragomordor.simpletms.ui

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dev.architectury.registry.menu.MenuRegistry
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.block.entity.TMMachineBlockEntity
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
     * - stored moves with quantities (and damage for TMs)
     * - optional Pokémon filter data
     */
    val MOVE_CASE_MENU: RegistrySupplier<MenuType<MoveCaseMenu>> = MENUS.register("move_case_menu") {
        MenuRegistry.ofExtended { containerId, inventory, buf ->
            val isTR = buf.readBoolean()

            // Read stored moves with quantities (and damage for TMs)
            val storedMovesCount = buf.readVarInt()
            val storedMoves = mutableMapOf<String, Int>()
            val storedDamage = mutableMapOf<String, Int>()
            repeat(storedMovesCount) {
                val moveName = buf.readUtf()
                val quantity = buf.readVarInt()
                storedMoves[moveName] = quantity
                // Read damage value for TMs
                if (!isTR) {
                    val damage = buf.readVarInt()
                    storedDamage[moveName] = damage
                }
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

                pokemonFilterData = PokemonFilterData(speciesId, formName, displayName, learnableMoves)

                // Read whether this was a fresh selection (auto-enable filter) or loaded (don't auto-enable)
                autoEnableFilter = buf.readBoolean()
            } else {
                pokemonFilterData = null
                autoEnableFilter = false
            }

            MoveCaseMenu(containerId, inventory, isTR, storedMoves, null, pokemonFilterData, autoEnableFilter, storedDamage)
        }
    }

    /**
     * Menu type for the TM Machine block.
     * Uses extended menu to pass:
     * - Block position (to find the block entity)
     * - Stored moves data (TM and TR counts per move, plus TM damage)
     * - Optional Pokémon filter data (from party selection)
     */
    val TM_MACHINE_MENU: RegistrySupplier<MenuType<TMMachineMenu>> = MENUS.register("tm_machine_menu") {
        MenuRegistry.ofExtended { containerId, inventory, buf ->
            val blockPos = buf.readBlockPos()

            // Read stored moves data
            val storedMovesCount = buf.readVarInt()
            val storedMoves = mutableMapOf<String, TMMachineBlockEntity.StoredMoveData>()
            repeat(storedMovesCount) {
                val moveName = buf.readUtf()
                val tmCount = buf.readVarInt()
                val trCount = buf.readVarInt()
                val tmDamage = buf.readVarInt()  // Read TM damage
                storedMoves[moveName] = TMMachineBlockEntity.StoredMoveData(tmCount, trCount, tmDamage)
            }

            // Read optional Pokémon filter data
            val hasFilter = buf.readBoolean()
            val pokemonFilterData: PokemonFilterData? = if (hasFilter) {
                val speciesId = buf.readUtf()
                val formName = buf.readUtf()
                val displayName = buf.readUtf()
                val learnableCount = buf.readVarInt()
                val learnableMoves = mutableSetOf<String>()
                repeat(learnableCount) {
                    learnableMoves.add(buf.readUtf())
                }
                PokemonFilterData(speciesId, formName, displayName, learnableMoves)
            } else {
                null
            }

            // Try to get the block entity from the client world
            val level = inventory.player.level()
            val blockEntity = level.getBlockEntity(blockPos) as? TMMachineBlockEntity

            TMMachineMenu(containerId, inventory, blockEntity, storedMoves, pokemonFilterData)
        }
    }

    /**
     * Register all menu types. Call this during mod initialization.
     */
    fun register() {
        MENUS.register()
    }
}