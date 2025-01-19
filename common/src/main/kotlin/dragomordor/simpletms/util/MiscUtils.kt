package dragomordor.simpletms.util

import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.util.asTranslated
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.SimpleTMsItems
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import java.io.InputStreamReader

fun simpletmsResource(path: String) = ResourceLocation.fromNamespaceAndPath(SimpleTMs.MOD_ID, path)

@Serializable
data class MoveLearnItemDefinition(
    val moveName: String,
    val moveType: String,
)

//fun loadMoveLearnItemsFromJson(jsonFilePath: String): List<MoveLearnItemDefinition> {
//    // Load JSON file from resource directory
//    val resourceStream =  SimpleTMs::class.java.getResourceAsStream("/$jsonFilePath")
//        ?: throw IllegalArgumentException("Resource not found: $jsonFilePath")
//
//    val jsonContent = InputStreamReader(resourceStream).use {  it.readText() }
//    val itemDefinitions = Json.decodeFromString<List<MoveLearnItemDefinition>>(jsonContent)
//
//    return itemDefinitions
//}

fun fromLang(
    prefixOrModid: String,
    subKey: String,
    vararg objects: Any
) = "$prefixOrModid.$subKey".asTranslated(*objects)

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


