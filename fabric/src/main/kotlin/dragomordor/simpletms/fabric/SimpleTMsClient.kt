package dragomordor.simpletms.fabric

import dragomordor.simpletms.SimpleTMsClient
import dragomordor.simpletms.ui.SimpleTMsScreens
import net.fabricmc.api.ClientModInitializer

@Suppress("Unused")
class SimpleTMsClient : ClientModInitializer {
    override fun onInitializeClient() {
        SimpleTMsClient.init()
        // Screen registration
        SimpleTMsScreens.register()
    }
}
