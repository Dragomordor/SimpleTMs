package dragomordor.simpletms.ui

import dev.architectury.registry.menu.MenuRegistry
import dragomordor.simpletms.SimpleTMs

/**
 * Client-side initialization for SimpleTMs screens.
 *
 * This should be called from your CLIENT mod initializer (not common).
 *
 * For Fabric: Call SimpleTMsScreens.register() in your client entrypoint.
 * For NeoForge: Call SimpleTMsScreens.register() in your client mod constructor or FMLClientSetupEvent.
 */
object SimpleTMsScreens {

    /**
     * Register all screen factories.
     * Call this during CLIENT initialization only.
     */
    fun register() {
        SimpleTMs.LOGGER.info("Registering SimpleTMs screen factories")

        try {
            MenuRegistry.registerScreenFactory(
                SimpleTMsMenuTypes.MOVE_CASE_MENU.get(),
                ::MoveCaseScreen
            )
            SimpleTMs.LOGGER.info("Successfully registered MoveCaseScreen")
        } catch (e: Exception) {
            SimpleTMs.LOGGER.error("Failed to register MoveCaseScreen: ${e.message}", e)
        }
    }
}