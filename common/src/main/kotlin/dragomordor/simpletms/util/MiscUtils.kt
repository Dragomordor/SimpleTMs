package dragomordor.simpletms.util

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import dragomordor.simpletms.SimpleTMs
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.minecraft.resources.ResourceLocation
import java.io.InputStreamReader

fun simpletmsResource(path: String) = ResourceLocation.fromNamespaceAndPath(SimpleTMs.MOD_ID, path)

@Serializable
data class MoveLearnItemDefinition(
    val moveName: String,
    val moveType: String,
)

fun loadMoveLearnItemsFromJson(jsonFilePath: String): List<MoveLearnItemDefinition> {
    // Load JSON file from resource directory
    val resourceStream =  SimpleTMs::class.java.getResourceAsStream("/$jsonFilePath")
        ?: throw IllegalArgumentException("Resource not found: $jsonFilePath")

    val jsonContent = InputStreamReader(resourceStream).use {  it.readText() }
    val itemDefinitions = Json.decodeFromString<List<MoveLearnItemDefinition>>(jsonContent)

    return itemDefinitions
}

