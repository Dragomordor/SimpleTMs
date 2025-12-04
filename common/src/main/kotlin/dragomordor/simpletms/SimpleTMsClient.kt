package dragomordor.simpletms

import dragomordor.simpletms.events.ClientEventListeners
import dragomordor.simpletms.ui.SimpleTMsMenuTypes
import dragomordor.simpletms.ui.SimpleTMsScreens

object SimpleTMsClient {

    fun init() {
        ClientEventListeners.registerListeners()
        // Screen registration
        SimpleTMsScreens.register()
    }
}