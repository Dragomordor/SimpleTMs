package dragomordor.simpletms

import dragomordor.simpletms.events.ClientEventListeners
import dragomordor.simpletms.ui.SimpleTMsScreens

object SimpleTMsClient {

    fun init() {
        ClientEventListeners.registerListeners()
        // Screen registration
        // SimpleTMsScreens.register()
            // Screen registration is handled per-platform:
            // - Fabric: directly in SimpleTMsClientFabric
            // - NeoForge: via RegisterMenuScreensEvent
    }
}