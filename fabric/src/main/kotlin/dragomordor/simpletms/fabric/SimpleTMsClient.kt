package dragomordor.simpletms.fabric

import dev.architectury.registry.menu.MenuRegistry
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.SimpleTMsClient
import dragomordor.simpletms.events.ClientEventListeners
import dragomordor.simpletms.network.SimpleTMsNetwork
import dragomordor.simpletms.ui.MoveCaseScreen
import dragomordor.simpletms.ui.SimpleTMsMenuTypes
import dragomordor.simpletms.ui.SimpleTMsScreens
import dragomordor.simpletms.ui.TMMachineScreen
import net.fabricmc.api.ClientModInitializer

@Suppress("Unused")
class SimpleTMsClient : ClientModInitializer {
    override fun onInitializeClient() {
        // Register S2C packet receivers (client-side only)
        SimpleTMsNetwork.registerClient()
        // Register event listeners from common
        ClientEventListeners.registerListeners()

        // Register screens directly on Fabric (registries are already complete)
        registerScreenFactories()
    }

    private fun registerScreenFactories() {
        SimpleTMs.LOGGER.info("Registering SimpleTMs screen factories (Fabric)")

        MenuRegistry.registerScreenFactory(
            SimpleTMsMenuTypes.MOVE_CASE_MENU.get(),
            ::MoveCaseScreen
        )

        MenuRegistry.registerScreenFactory(
            SimpleTMsMenuTypes.TM_MACHINE_MENU.get(),
            ::TMMachineScreen
        )
    }
}