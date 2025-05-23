package dragomordor.simpletms

import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.moves.MoveTemplate
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dragomordor.simpletms.SimpleTMs.LOGGER
import dragomordor.simpletms.SimpleTMs.MOD_ID
import dragomordor.simpletms.item.custom.BlankTmItem
import dragomordor.simpletms.item.custom.MoveLearnItem
import dragomordor.simpletms.util.MoveLearnItemDefinition
import dragomordor.simpletms.util.simpletmsResource
import kotlinx.serialization.json.Json
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.ItemStack
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStreamReader


@Suppress("unused", "SameParameterValue")
object SimpleTMsItems {

    private val ITEMS: DeferredRegister<Item> = DeferredRegister.create(MOD_ID, Registries.ITEM)
    private const val DEFAULT_MOVE_JSON_PATH = "$MOD_ID/movelearnitems/default.json"
    // TODO: Add custom moves path here
        // Regular Custom move path
    private const val CUSTOM_MOVE_JSON_PATH = "config/$MOD_ID/custom/custom_moves.json"
        // GEB custom moves
    private const val GEB_CUSTOM_MOVE_JSON_PATH = "config/$MOD_ID/custom/geb_custom_moves.json"


    private val defaultTMMoveConfigFile = File("config/$MOD_ID/moves/default_tm_moves.json")
    private val defaultTRMoveConfigFile = File("config/$MOD_ID/moves/default_tr_moves.json")


    private val movesExcludedFromPokemonDropsFile = File("config/$MOD_ID/moves/excluded_moves_from_pokemon_drops.json")
    private val moveExcludedFromBlankLearningFile = File("config/$MOD_ID/moves/excluded_moves_from_blank_learning.json")
    private val movesExcludedFromTMTRLearningFile = File("config/$MOD_ID/moves/excluded_moves_from_tmtr_learning.json")

    // Create empty list of all moves with items.
    val ALL_MOVE_NAMES_WITH_TM_ITEMS: MutableList<String> = mutableListOf()
    val ALL_MOVE_NAMES_WITH_TR_ITEMS: MutableList<String> = mutableListOf()
    val ALL_MOVE_TEMPLATES_WITH_ITEMS: List<MoveTemplate> = mutableListOf()

    val ALL_REMOVED_DEFAULT_MOVES: List<String> = mutableListOf()

    // Excluded moves
    val ALL_MOVES_EXCLUDED_FROM_POKEMON_DROPS: MutableList<String> = mutableListOf()
    val ALL_MOVES_EXCLUDED_FROM_BLANK_LEARNING: MutableList<String> = mutableListOf()
    val ALL_MOVES_EXCLUDED_FROM_TMTR_LEARNING: MutableList<String> = mutableListOf()


    // List of all TM and TR items
    val TM_ITEMS = mutableListOf<RegistrySupplier<MoveLearnItem>>()
    val TR_ITEMS = mutableListOf<RegistrySupplier<MoveLearnItem>>()
        // Custom
    val CUSTOM_TR_ITEMS = mutableListOf<RegistrySupplier<MoveLearnItem>>()
    val CUSTOM_TM_ITEMS = mutableListOf<RegistrySupplier<MoveLearnItem>>()



    // ------------------------------------------------------------
    // Register Items From Non-Config
    // ------------------------------------------------------------
    // Blank TM and TR
    val BLANK_TM = registerBlankTmItem("tm_blank", false)
    val BLANK_TR = registerBlankTmItem("tr_blank", true)

    // ------------------------------------------------------------
    // Register Item Functions
    // ------------------------------------------------------------

    private fun registerBlankTmItem(name: String, isTR: Boolean): RegistrySupplier<BlankTmItem> {
        val item : RegistrySupplier<BlankTmItem>
        if (isTR) {
            val settings: Properties = Properties().stacksTo(SimpleTMs.config.trStackSize)
            item = ITEMS.register(name) { BlankTmItem(isTR, settings) }
        } else {
            val settings: Properties = Properties().durability(if((SimpleTMs.config.blankTMBaseDurability)>0) SimpleTMs.config.blankTMBaseDurability else 1)
            item = ITEMS.register(name) { BlankTmItem(isTR, settings) }
        }

        return item
    }

    private fun registerMoveLearnItem(moveName: String, isTR: Boolean, isCustom: Boolean): RegistrySupplier<MoveLearnItem> {
        val itemKey = if (isTR) "tr_$moveName" else "tm_$moveName"
        val item : RegistrySupplier<MoveLearnItem>
        if (isTR) {
            val settings: Properties = Properties().stacksTo(SimpleTMs.config.trStackSize)
            item = ITEMS.register(itemKey) { MoveLearnItem(moveName, isTR, isCustom, settings) }
        } else {
            val settings: Properties = Properties().durability(if((SimpleTMs.config.tmBaseDurability)>0) SimpleTMs.config.tmBaseDurability else 1)
            item = ITEMS.register(itemKey) { MoveLearnItem(moveName, isTR, isCustom, settings) }
        }
        // Determine the appropriate lists
        val (itemList, customItemList, moveList) = if (isTR) {
            Triple(TR_ITEMS, CUSTOM_TR_ITEMS, ALL_MOVE_NAMES_WITH_TR_ITEMS)
        } else {
            Triple(TM_ITEMS, CUSTOM_TM_ITEMS, ALL_MOVE_NAMES_WITH_TM_ITEMS)
        }
        // Store in the correct list
        (if (isCustom) customItemList else itemList).add(item)
        moveList.add(moveName)
        return item
    }

    private fun registerMoveLearnItemsFromConfig(moveFile: File, isCustom: Boolean) {
        // Read Json file moveFile
        val jsonContent = FileReader(moveFile).use { it.readText() }
        val itemDefinitions = Json.decodeFromString<List<MoveLearnItemDefinition>>(jsonContent)
        // Register TMs
        for (itemDefinition in itemDefinitions) {
            // TMs
//            if (isCustom) {
//                LOGGER.info("Trying to register custom move:" + itemDefinition.moveName)
//            }
            registerMoveLearnItem(
                moveName = itemDefinition.moveName,
                isTR = true,
                isCustom = isCustom
            )
            // TRs
            registerMoveLearnItem(
                moveName = itemDefinition.moveName,
                isTR = false,
                isCustom = isCustom
            )
        }
    }

    private fun registerMoveLearnItemsFromResourceJSON(jsonFilePath: String, isCustom: Boolean) {
        // Load JSON file from resource directory
        val itemDefinitions = loadMoveLearnItemsFromJson(jsonFilePath)

        for (itemDefinition in itemDefinitions) {
//            if (isCustom) {
//                LOGGER.info("Trying to register custom move:" + itemDefinition.moveName)
//            }
            // Register TMs
            registerMoveLearnItem(
                moveName = itemDefinition.moveName,
                isTR = false,
                isCustom = isCustom
            )
            // Register TRs
            registerMoveLearnItem(
                moveName = itemDefinition.moveName,
                isTR = true,
                isCustom = isCustom
            )
        }

    }

    private fun loadMoveLearnItemsFromJson(jsonFilePath: String): List<MoveLearnItemDefinition> {
        // Load JSON file from resource directory
        val resourceStream =  SimpleTMs::class.java.getResourceAsStream("/$jsonFilePath")
            ?: throw IllegalArgumentException("Resource not found: $jsonFilePath")

        val jsonContent = InputStreamReader(resourceStream).use {  it.readText() }
        val itemDefinitions = Json.decodeFromString<List<MoveLearnItemDefinition>>(jsonContent)

        return itemDefinitions
    }


    private fun loadDefaultMoveConfig(moveConfigFile: File, isTR: Boolean) {
        moveConfigFile.parentFile.mkdirs()
        val nameString = if (isTR) "TR" else "TM"
        if (moveConfigFile.exists()) {
            // If file exists, report that it was found
            LOGGER.info("Found default moves config file for SimpleTMs for $nameString moves")
        } else {
            LOGGER.info("Default moves config file not found. Creating default moves config file")
            // Load default moves from resource
            val resourceStream =  SimpleTMs::class.java.getResourceAsStream("/$DEFAULT_MOVE_JSON_PATH")
                ?: throw IllegalArgumentException("Resource not found: $DEFAULT_MOVE_JSON_PATH")
            val jsonContent = InputStreamReader(resourceStream).use {  it.readText() }
            // copy to config json
            try {
                val fileWriter = FileWriter(moveConfigFile)
                fileWriter.write(jsonContent)
                fileWriter.close()
            } catch (exception: Exception) {
                LOGGER.error("Failed to save config file for $MOD_ID. Reason:")
                exception.printStackTrace()
            }
        }
    }





    // Excluded moves
    private fun loadExcludedMovesFromConfig(excludedMovesFile: File, excludedMoveList: MutableList<String>) {

        excludedMovesFile.parentFile.mkdirs()

        if (excludedMovesFile.exists()) {
            // If file exists, report that it was found
            LOGGER.info("Found excluded moves config file for SimpleTMs")
            // Read Json file moveFile
            val jsonContent = FileReader(excludedMovesFile).use { it.readText() }
            val moveDefinitions = Json.decodeFromString<List<MoveLearnItemDefinition>>(jsonContent)
            excludedMoveList.clear()
            for (moveDefinition in moveDefinitions) {
                excludedMoveList.add(moveDefinition.moveName)
            }
        } else {
            // if files not found, create empty files in their place (just [] in the file)
            LOGGER.info("Excluded moves config file not found. Creating empty excluded moves config file")
            try {
                val fileWriter = FileWriter(excludedMovesFile)
                fileWriter.write("[]")
                fileWriter.close()
            } catch (exception: Exception) {
                LOGGER.error("Failed to save config file for $MOD_ID. Reason:")
                exception.printStackTrace()
            }
        }
    }

    // ------------------------------------------------------------
    // Register Mod Items
    // ------------------------------------------------------------

    // Register all mod items
    fun registerModItems() {
        LOGGER.info("Register Mod Items for $MOD_ID")
        // Load Default Moves Config
        LOGGER.info("Loading default move configs for TMs and TRs")
        loadDefaultMoveConfig(defaultTMMoveConfigFile, false)
        loadDefaultMoveConfig(defaultTRMoveConfigFile, true)
        // Register items from config
        // Default moves
        LOGGER.info("Registering default move TMs and TRs")
        // If the config allows move removal, then register from configs. Otherwise, register from default internal json
        if (SimpleTMs.config.allowCustomMovesAndEditing) {
            registerMoveLearnItemsFromConfig(defaultTMMoveConfigFile, false)
        } else {
            registerMoveLearnItemsFromResourceJSON(DEFAULT_MOVE_JSON_PATH, false)
        }
        // TODO: Custom moves
            // GEB Custom Moves
        LOGGER.info("Registering custom TMs and TRs if present")
        loadGEBCustomMoves(GEB_CUSTOM_MOVE_JSON_PATH)
            // Other custom moves
        loadCustomMoves(CUSTOM_MOVE_JSON_PATH)

        // Register all items
        ITEMS.register()

        // Load excluded moves
        LOGGER.info("Loading excluded moves from config")
        loadExcludedMovesFromConfig(movesExcludedFromPokemonDropsFile, ALL_MOVES_EXCLUDED_FROM_POKEMON_DROPS)
        loadExcludedMovesFromConfig(moveExcludedFromBlankLearningFile, ALL_MOVES_EXCLUDED_FROM_BLANK_LEARNING)
        loadExcludedMovesFromConfig(movesExcludedFromTMTRLearningFile, ALL_MOVES_EXCLUDED_FROM_TMTR_LEARNING)
    }

    // ------------------------------------------------------------
    // Custom Moves
    // ------------------------------------------------------------

    private fun loadGEBCustomMoves(gebJsonFilePath: String) {
        // Check if geb file exists
        if (File(gebJsonFilePath).exists()) {
            // Register TMs adn TRs from json
            registerMoveLearnItemsFromConfig(File(gebJsonFilePath), true)
        }
    }

    private fun loadCustomMoves(customJsonFilePath: String) {
        if (SimpleTMs.config.allowCustomMovesAndEditing) {
            // Register TMs adn TRs from json
            registerMoveLearnItemsFromConfig(File(customJsonFilePath), true)
        }
    }

    // ------------------------------------------------------------
    // Other Functions
    // ------------------------------------------------------------

    fun getItemStackFromName(name: String): ItemStack {
        val identifier = simpletmsResource(name)
        val item = BuiltInRegistries.ITEM.get(identifier)
        val itemStack = ItemStack(item)
        return itemStack
    }

    private fun getItemFromName(name: String): Item {
        val identifier = simpletmsResource(name)
        val item = BuiltInRegistries.ITEM.get(identifier)
        return item
    }

    fun getTMorTRItemFromMove(move: Move, isTR: Boolean): Item {
        // Get prefix for TM or TR
        val prefix = if (isTR) "tr_" else "tm_"
        val moveName = move.name
        val newMoveLearnItem = getItemFromName(prefix + moveName)
        return newMoveLearnItem
    }

    fun hasItemForMove(move: MoveTemplate, isTR: Boolean): Boolean {
        return if (isTR) {
            ALL_MOVE_NAMES_WITH_TR_ITEMS.contains(move.name)
        } else {
            ALL_MOVE_NAMES_WITH_TM_ITEMS.contains(move.name)
        }
    }

}


