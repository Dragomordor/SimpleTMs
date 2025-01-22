package dragomordor.simpletms

import dragomordor.simpletms.events.ClientEventListeners

object SimpleTMsClient {

    fun init() {
        ClientEventListeners.registerListeners()
    }
}