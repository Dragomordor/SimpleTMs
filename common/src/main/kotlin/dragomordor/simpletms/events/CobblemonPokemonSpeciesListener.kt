package dragomordor.simpletms.events

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.util.MoveAssociations

object CobblemonPokemonSpeciesListener: EventHandler {
    override fun registerListeners() {
        SimpleTMsEvents.POKEMON_SPECIES_INITIALIZED.subscribe(Priority.NORMAL, this::onPokemonSpeciesInitialized)
    }

    private fun onPokemonSpeciesInitialized(pokemonSpecies: PokemonSpecies) {
        // Initialize move associations
        SimpleTMs.LOGGER.info("Initializing Move Associations for SimpleTMs. This can take a while...")
        MoveAssociations.init(pokemonSpecies)
        SimpleTMs.LOGGER.info("Move Associations initialized for SimpleTMs.")
    }


}