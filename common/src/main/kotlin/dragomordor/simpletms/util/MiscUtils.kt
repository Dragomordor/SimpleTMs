package dragomordor.simpletms.util

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.asTranslated
import dragomordor.simpletms.SimpleTMs
import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import java.awt.Color

// Gets the item from the registry
fun simpletmsResource(path: String) = ResourceLocation.fromNamespaceAndPath(SimpleTMs.MOD_ID, path)

// For move learn items registration from config
@Serializable
data class MoveLearnItemDefinition(
    val moveName: String,
    val Type: String,
)

// To get language translations
fun fromLang(
    prefixOrModid: String,
    subKey: String,
    vararg objects: Any
) = "$prefixOrModid.$subKey".asTranslated(*objects)

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

        val pokemonLearnset = pokemonSpecies.moves
        val pokemonMoveTemplates = pokemonLearnset.tmMoves
        val pokemonTutorMoveTemplates = pokemonLearnset.tutorMoves
        val pokemonEggMoveTemplates = pokemonLearnset.eggMoves
        // TODO - get pre evolution egg moves as well
        val pokemonLevelUpMoveTemplates = pokemonLearnset.getLevelUpMovesUpTo(Cobblemon.config.maxPokemonLevel).toMutableList()
//
//        // TODO - DEBUG if any movest besides levelmoves are empty, return dummy (excedp egg since evolutions don't have that)
//        if (pokemonMoveTemplates.isEmpty() || pokemonTutorMoveTemplates.isEmpty()) {
//            val dummyLearnMethods: MutableList<String> = mutableListOf()
//            pokemonList.add(Pair(pokemonSpecies, dummyLearnMethods))
//        }

        // Convert lists of MoveTemplates to lists of Strings
        val pokemonTmMoves = pokemonMoveTemplates.map { it.name }
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
                if (canPokemonLearnMoveTM && SimpleTMs.config.TMMovesLearnable) add("TM")
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

// For move learn item tooltip pages
object Pages {
    // Add variable CURRENT_PAGE which can be used to track the current page of the tooltip
    var CURRENT_PAGE = 0
    var TOTAL_PAGES = 0
}
