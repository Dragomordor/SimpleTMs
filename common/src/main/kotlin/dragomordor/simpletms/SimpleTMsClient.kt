package dragomordor.simpletms

import dragomordor.simpletms.events.ClientEventListeners
import dragomordor.simpletms.events.CobblemonPokemonSpeciesListener

object SimpleTMsClient {

    fun init() {
        ClientEventListeners.registerListeners()
        CobblemonPokemonSpeciesListener.registerListeners()
    }
}