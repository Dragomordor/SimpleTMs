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
     * Uses extended menu to pass the isTR flag and stored moves with quantities from server to client.
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
            MoveCaseMenu(containerId, inventory, isTR, storedMoves)
        }
    }

    /**
     * Register all menu types. Call this during mod initialization.
     */
    fun register() {
        MENUS.register()
    }
}