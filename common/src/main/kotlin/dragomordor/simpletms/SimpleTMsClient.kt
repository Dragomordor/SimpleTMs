package dragomordor.simpletms

import dragomordor.simpletms.events.ClientEventListeners
import dragomordor.simpletms.ui.SimpleTMsMenuTypes

object SimpleTMsClient {

    fun init() {
        ClientEventListeners.registerListeners()
    }
}