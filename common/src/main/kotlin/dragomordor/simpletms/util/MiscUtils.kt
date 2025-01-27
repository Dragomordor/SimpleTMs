package dragomordor.simpletms.util

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.asTranslated
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.SimpleTMsItems
import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import java.awt.Color

// Gets the item from the registry
//fun simpletmsResource(path: String) = ResourceLocation.fromNamespaceAndPath(SimpleTMs.MOD_ID, path)
fun simpletmsResource(path: String) = ResourceLocation(SimpleTMs.MOD_ID, path)

// For move learn items registration from config
@Serializable
data class MoveLearnItemDefinition(
    val moveName: String,
)

// To get language translations
//fun fromLang(
//    prefixOrModid: String,
//    subKey: String,
//    vararg objects: Any
//) = "$prefixOrModid.$subKey".asTranslated(*objects)


fun fromLang(
    prefixOrModid: String,
    subKey: String,
    vararg objects: Any
) = "$prefixOrModid.$subKey".asTranslated(*objects)

//fun fromLang(
//    prefixOrModid: String,
//    subKey: String,
//    vararg objects: Any
//) = Component.translatable("$prefixOrModid.$subKey", *objects)


//fun fromLang(
//    prefixOrModid: String,
//    subKey: String,
//    vararg objects: Any
//): Com

// To display failure messages
class FailureMessage() {
    companion object {
        lateinit var message: Component

        fun getFailureMessage(): Component {
            return message
        }

        fun setFailureMessage(message: Component) {
            this.message = message
        }
    }
}

// To interpolate between two colors given a range
fun interpolateColor(value: Double, min: Double, max: Double, startColor: Color, endColor: Color): Color {
    val ratio = ((value - min) / (max - min)).coerceIn(0.0, 1.0) // Clamp ratio between 0 and 1
    val red = (startColor.red + (endColor.red - startColor.red) * ratio).toInt()
    val green = (startColor.green + (endColor.green - startColor.green) * ratio).toInt()
    val blue = (startColor.blue + (endColor.blue - startColor.blue) * ratio).toInt()
    return Color(red, green, blue)
}


// Same function but does not use move as key, just has list of pokemon with learn methods
fun getMovePokemonAssociation(moveName: String, speciesCollection: MutableList<Species>): MutableList<Pair<Species, MutableList<String>>> {
    val pokemonList = mutableListOf<Pair<Species, MutableList<String>>>()

    // Check each pokemon to see if they can learn the move
    for (pokemonSpecies in speciesCollection) {
        val pokemonLearnMethods = mutableListOf<String>()

        // All pokemon learnsets
        val pokemonLearnset = pokemonSpecies.moves
        val pokemonTmMoveTemplates = pokemonLearnset.tmMoves
        val pokemonTutorMoveTemplates = pokemonLearnset.tutorMoves
        val pokemonEggMoveTemplates = pokemonLearnset.eggMoves
        var preEvolution = pokemonSpecies.preEvolution
        // if egg moves are empty, check pre-evolution
        if (pokemonEggMoveTemplates.isEmpty() && preEvolution != null) {
            pokemonEggMoveTemplates.addAll(preEvolution.species.moves.eggMoves)
            // Another check here
            preEvolution = preEvolution.species.preEvolution
            if (pokemonEggMoveTemplates.isEmpty() && preEvolution != null) {
                pokemonEggMoveTemplates.addAll(preEvolution.species.moves.eggMoves)
            }
        }

        val pokemonLevelUpMoveTemplates = pokemonLearnset.getLevelUpMovesUpTo(Cobblemon.config.maxPokemonLevel).toMutableList()

        // Convert lists of MoveTemplates to lists of Strings
        val pokemonTmMoves = pokemonTmMoveTemplates.map { it.name }
        val pokemonTutorMoves = pokemonTutorMoveTemplates.map { it.name }
        val pokemonEggMoves = pokemonEggMoveTemplates.map { it.name }
        val pokemonLevelUpMoves = pokemonLevelUpMoveTemplates.map { it.name }

        val canPokemonLearnMoveTM = pokemonTmMoves.contains(moveName)
        val canPokemonLearnMoveTutor = pokemonTutorMoves.contains(moveName)
        val canPokemonLearnMoveEgg = pokemonEggMoves.contains(moveName)
        val canPokemonLearnMoveLevelUp = pokemonLevelUpMoves.contains(moveName)
        val canPokemonLearnMoveAttAll = canPokemonLearnMoveTM || canPokemonLearnMoveTutor || canPokemonLearnMoveEgg || canPokemonLearnMoveLevelUp

        // If the pokemon can learn the move in any way, add it to the list, and add the ways in which it can learn it
        if (canPokemonLearnMoveAttAll) {

            pokemonLearnMethods.apply {
                if (canPokemonLearnMoveTM && SimpleTMs.config.tmMovesLearnable) add("TM")
                if (canPokemonLearnMoveTutor && SimpleTMs.config.tutorMovesLearnable) add("Tutor")
                if (canPokemonLearnMoveEgg && SimpleTMs.config.eggMovesLearnable) add("Egg")
                if (canPokemonLearnMoveLevelUp && SimpleTMs.config.levelMovesLearnable) add("Level Up")
            }
            if (pokemonLearnMethods.isNotEmpty()) {
                // Append the Pokémon and its learning methods to the list for this move
                pokemonList.add(Pair(pokemonSpecies, pokemonLearnMethods))
            }
        }
    }
    return pokemonList
}

// Loop through all moves and get the pokemon that can learn them in a complete list
fun getAllPokemonMoveAssociations(speciesCollection: MutableList<Species>): MutableList<Pair<String, MutableList<Pair<Species, MutableList<String>>>>> {
    // For all moveNames, map to a list of pokemon that can learn it
    // This list should have : all move names (first key), then for each move name, a list of pokemon that can learn it (second key) with the methods they can learn it
    val pokemonListForAllMoves = mutableListOf<Pair<String, MutableList<Pair<Species, MutableList<String>>>>>()

    // val allMoveNames = Moves.all().map { it.name }
    // Instead only get move names for existing TMs
    val allMovesNamesWithTRItems = SimpleTMsItems.ALL_MOVE_NAMES_WITH_TR_ITEMS
    val allMovesNamesWithTMItems = SimpleTMsItems.ALL_MOVE_NAMES_WITH_TM_ITEMS

    // Combine and filter out duplicates
    val allMoveNamesWithItems = (allMovesNamesWithTMItems + allMovesNamesWithTRItems).distinct()

    for (moveName in allMoveNamesWithItems) {
        val pokemonListForThisMove = getMovePokemonAssociation(moveName, speciesCollection)
        pokemonListForAllMoves.add(Pair(moveName, pokemonListForThisMove))
    }

    return pokemonListForAllMoves
}

object MoveAssociations {
    private var POKEMON_MOVE_ASSOCIATIONS = mutableListOf<Pair<String, MutableList<Pair<Species, MutableList<String>>>>>()
    fun getMovePokemonAssociations(): MutableList<Pair<String, MutableList<Pair<Species, MutableList<String>>>>> {
        return POKEMON_MOVE_ASSOCIATIONS
    }
    private fun updateMovePokemonAssociations(pokemonSpecies: PokemonSpecies) {
        val speciesCollection = pokemonSpecies.species.toMutableList()
        POKEMON_MOVE_ASSOCIATIONS = getAllPokemonMoveAssociations(speciesCollection)
    }
    fun init(pokemonSpecies: PokemonSpecies) {
        // Clear the move associations
        POKEMON_MOVE_ASSOCIATIONS.clear()
        // Update the move associations
        updateMovePokemonAssociations(pokemonSpecies)
    }
    fun getAssociationsForMove(moveName: String): MutableList<Pair<Species, MutableList<String>>>
    {
        return POKEMON_MOVE_ASSOCIATIONS.find { it.first == moveName }?.second ?: mutableListOf()
    }
}

// For move learn item tooltip pages
object Pages {
    // Add variable CURRENT_PAGE which can be used to track the current page of the tooltip
    var CURRENT_PAGE = 0
    var TOTAL_PAGES = 0
}
