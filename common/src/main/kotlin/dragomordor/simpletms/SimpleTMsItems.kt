package dragomordor.simpletms

import com.cobblemon.mod.common.api.moves.Move
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

    internal val ITEMS: DeferredRegister<Item> = DeferredRegister.create(MOD_ID, Registries.ITEM)
    val defaultMoveJsonPath = "simpletms/movelearnitems/default.json"
    // val customMoveJsonPath = "simpletms/movelearnitems/custom.json"

    const val DEFAULT_MOVE_CONFIG_PATH = "config/$MOD_ID/default_moves.json"
    val defaultMoveConfigFile = File(DEFAULT_MOVE_CONFIG_PATH)

    // TODO: Add a config file to allow for the removal of certain moves from the TM and TR lists
    // TODO: Tags for tm and tr items

    val TM_ITEMS = mutableListOf<RegistrySupplier<MoveLearnItem>>()
    val TR_ITEMS = mutableListOf<RegistrySupplier<MoveLearnItem>>()

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

        var settings: Properties

        if (isTR) {
            settings = Properties().stacksTo(SimpleTMs.config.TRStackSize)
        } else {
            settings = Properties().stacksTo(1)
            if (SimpleTMs.config.TMBaseDurability > 0) {
                settings.durability(SimpleTMs.config.blankTMBaseDurability)
            }
        }
        val item = ITEMS.register(name) { BlankTmItem(isTR, settings) }
        return item
    }

    private fun registerMoveLearnItem(name: String, moveName: String, moveType: String, isTR: Boolean): RegistrySupplier<MoveLearnItem> {
        val item : RegistrySupplier<MoveLearnItem>
        if (isTR) {
            val settings: Properties =
                Properties()
                    .stacksTo(SimpleTMs.config.TRStackSize)
            item = ITEMS.register(name) {
                MoveLearnItem(moveName, moveType, isTR, settings)
            }
            TR_ITEMS.add(item)
        } else {
            val settings: Properties =
                Properties()
                    .stacksTo(1)
                    // TODO: if TMBaseDurability is 0, make it unbreakable
            if (SimpleTMs.config.TMBaseDurability > 0) {
                settings.durability(SimpleTMs.config.TMBaseDurability)
            }
            item = ITEMS.register(name) {
                MoveLearnItem(moveName, moveType, isTR, settings)
            }
            TM_ITEMS.add(item)
        }
        return item
    }

    private fun registerMoveLearnItemsFromConfig(moveFile: File) {
        // Read Json file moveFile
        val jsonContent = FileReader(moveFile).use { it.readText() }
        val itemDefinitions = Json.decodeFromString<List<MoveLearnItemDefinition>>(jsonContent)
        // Register TMs
        for (itemDefinition in itemDefinitions) {
            registerMoveLearnItem(
                name = "tm_" + itemDefinition.moveName,
                moveName = itemDefinition.moveName,
                moveType = itemDefinition.moveType,
                isTR = false
            )
        }
        // Register TRs
        for (itemDefinition in itemDefinitions) {
            registerMoveLearnItem(
                name = "tr_" + itemDefinition.moveName,
                moveName = itemDefinition.moveName,
                moveType = itemDefinition.moveType,
                isTR = true
            )
        }
    }

    private fun loadDefaultMoveConfig(moveConfigFile: File) {
        if (moveConfigFile.exists()) {
            // If file exists, report that it was found
            LOGGER.info("Found default moves config file for SimpleTMs")

        } else {
            LOGGER.info("Default moves config file not found. Creating default moves config file")
            // Load default moves from resource
            val resourceStream =  SimpleTMs::class.java.getResourceAsStream("/$defaultMoveJsonPath")
                ?: throw IllegalArgumentException("Resource not found: $defaultMoveJsonPath")
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

    // ------------------------------------------------------------
    // Other Functions
    // ------------------------------------------------------------

    // Register all mod items
    fun registerModItems() {
        LOGGER.info("Register Mod Items for $MOD_ID")
        // Load Default Moves Config
        LOGGER.info("Loading default moves config")
        loadDefaultMoveConfig(defaultMoveConfigFile)
        // Register items from config
            // Default moves
        LOGGER.info("Registering default move TMs and TRs")
        registerMoveLearnItemsFromConfig(defaultMoveConfigFile)
            // TODO: Custom moves
        // LOGGER.info("Registering custom move TMs and TRs")
        ITEMS.register()
    }

    fun getItemStackFromName(name: String): ItemStack {
        val identifier = simpletmsResource(name)
        val item = BuiltInRegistries.ITEM.get(identifier)
        val itemStack = ItemStack(item)
        return itemStack
    }

    fun getItemFromName(name: String): Item {
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

}