package dragomordor.simpletms

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dragomordor.simpletms.item.custom.BlankTmItem
import dragomordor.simpletms.item.custom.MoveLearnItem
import dragomordor.simpletms.util.loadMoveLearnItemsFromJson
import dragomordor.simpletms.util.simpletmsResource
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.ItemStack


@Suppress("unused", "SameParameterValue")
object SimpleTMsItems {

    internal val ITEMS: DeferredRegister<Item> = DeferredRegister.create(SimpleTMs.MOD_ID, Registries.ITEM)
    val defaultMoveJsonPath = "simpletms/movelearnitems/default.json"
    val customMoveJsonPath = "simpletms/movelearnitems/custom.json"

    // TODO:  Z type Moves have been removed -- add file with config check to add them back if desired
    // TODO: Add a config file to allow for the removal of certain moves from the TM and TR lists
    // TODO: Tags for tm and tr items

    val TM_ITEMS = mutableListOf<RegistrySupplier<MoveLearnItem>>()
    val TR_ITEMS = mutableListOf<RegistrySupplier<MoveLearnItem>>()

    // ------------------------------------------------------------
    // Register Items
    // ------------------------------------------------------------
    // Blank TM and TR
    val BLANK_TM = registerBlankTmItem("tm_blank", false)
    val BLANK_TR = registerBlankTmItem("tr_blank", true)

    // ------------------------------------------------------------
    // Register Item Functions
    // ------------------------------------------------------------

    private fun registerBlankTmItem(name: String, isTR: Boolean): RegistrySupplier<BlankTmItem> {
        if (isTR) {
            val settings: Properties =
                Properties().
                    stacksTo(SimpleTMs.config.TRStackSize)
            val item = ITEMS.register(name) {
                BlankTmItem(isTR, settings)
            }
            return item
        } else {
            val settings: Properties =
                Properties().
                    stacksTo(SimpleTMs.config.TMStackSize)
            val item = ITEMS.register(name) {
                BlankTmItem(isTR, settings)
            }
            return item
        }
    }

    private fun registerMoveLearnItem(name: String, moveName: String, moveType: String, isTR: Boolean): RegistrySupplier<MoveLearnItem> {
        if (isTR) {
            val settings: Properties =
                Properties().
                    stacksTo(SimpleTMs.config.TRStackSize)
            val item = ITEMS.register(name) {
                MoveLearnItem(moveName, moveType, isTR, settings)
            }
            TR_ITEMS.add(item)
            return item
        } else {
            val settings: Properties =
                Properties().
                    stacksTo(SimpleTMs.config.TMStackSize)
            val item = ITEMS.register(name) {
                MoveLearnItem(moveName, moveType, isTR, settings)
            }
            TM_ITEMS.add(item)
            return item
        }
    }

    private fun registerMoveLearnItemsFromJSON(jsonFilePath: String) {
        // Load JSON file from resource directory
        val itemDefinitions = loadMoveLearnItemsFromJson(jsonFilePath)
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

    // ------------------------------------------------------------
    // Other Functions
    // ------------------------------------------------------------

    // Register all mod items
    fun registerModItems() {
        SimpleTMs.LOGGER.info("Register Mod Items for " + SimpleTMs.MOD_ID)
        // Register items from JSON
            // Default moves
        registerMoveLearnItemsFromJSON(defaultMoveJsonPath)
            // Custom moves -- check if the file contain any items
        // TODO: add a check from config file to see if the file should be loaded
        registerMoveLearnItemsFromJSON(customMoveJsonPath)

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

}