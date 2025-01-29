package dragomordor.simpletms.util

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.asTranslated
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.SimpleTMsItems
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

// For move learn item tooltip pages
object Pages {
    // Add variable CURRENT_PAGE which can be used to track the current page of the tooltip
    var CURRENT_PAGE = 0
    var TOTAL_PAGES = 0
}
