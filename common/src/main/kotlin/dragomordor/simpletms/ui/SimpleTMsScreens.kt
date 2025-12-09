package dragomordor.simpletms.ui

import dev.architectury.event.events.client.ClientLifecycleEvent
import dev.architectury.registry.menu.MenuRegistry
import dragomordor.simpletms.SimpleTMs

/**
 * Client-side initialization for SimpleTMs screens.
 *
 * This should be called from your client mod initializer.
 *
 * Note: Due to a known issue with Architectury API on NeoForge 1.21.1,
 * MenuRegistry.registerScreenFactory may not work properly on NeoForge.
 * If you encounter issues, you may need to register the screen factory
 * directly using NeoForge's RegisterMenuScreensEvent in your NeoForge
 * client initializer:
 *
 * ```kotlin
 * @SubscribeEvent
 * fun onRegisterMenuScreens(event: RegisterMenuScreensEvent) {
 *     event.register(SimpleTMsMenuTypes.MOVE_CASE_MENU.get(), ::MoveCaseScreen)
 *     event.register(SimpleTMsMenuTypes.TM_MACHINE_MENU.get(), ::TMMachineScreen)
 * }
 * ```
 *
 * For Fabric, the Architectury method works fine.
 */
object SimpleTMsScreens {

    /**
     * Register all screen factories.
     * Call this during client initialization.
     */
    fun register() {
        ClientLifecycleEvent.CLIENT_SETUP.register {
            registerScreenFactories()
        }
    }

    private fun registerScreenFactories() {
        SimpleTMs.LOGGER.info("Registering SimpleTMs screen factories")

        // Register Move Case Screen
        try {
            MenuRegistry.registerScreenFactory(
                SimpleTMsMenuTypes.MOVE_CASE_MENU.get(),
                ::MoveCaseScreen
            )
            SimpleTMs.LOGGER.info("Successfully registered MoveCaseScreen")
        } catch (e: Exception) {
            SimpleTMs.LOGGER.error("Failed to register MoveCaseScreen via Architectury. " +
                    "If on NeoForge, you may need to use RegisterMenuScreensEvent instead.", e)
        }

        // Register TM Machine Screen
        try {
            MenuRegistry.registerScreenFactory(
                SimpleTMsMenuTypes.TM_MACHINE_MENU.get(),
                ::TMMachineScreen
            )
            SimpleTMs.LOGGER.info("Successfully registered TMMachineScreen")
        } catch (e: Exception) {
            SimpleTMs.LOGGER.error("Failed to register TMMachineScreen via Architectury. " +
                    "If on NeoForge, you may need to use RegisterMenuScreensEvent instead.", e)
        }
    }
}