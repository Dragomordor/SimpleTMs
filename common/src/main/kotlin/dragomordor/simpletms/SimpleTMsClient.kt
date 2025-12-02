package dragomordor.simpletms

import dragomordor.simpletms.events.ClientEventListeners
import dragomordor.simpletms.network.SimpleTMsNetwork

object SimpleTMsClient {

    fun init() {
        ClientEventListeners.registerListeners()

    }
}