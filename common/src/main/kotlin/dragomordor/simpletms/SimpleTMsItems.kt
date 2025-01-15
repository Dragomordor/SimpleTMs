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
import net.minecraft.world.item.ItemStack


@Suppress("unused", "SameParameterValue")
object SimpleTMsItems {

    internal val ITEMS: DeferredRegister<Item> = DeferredRegister.create(SimpleTMs.MOD_ID, Registries.ITEM)
    val defaultMoveJsonPath = "simpletms/movelearnitems/default.json"
    val customMoveJsonPath = "simpletms/movelearnitems/custom.json"

    // TODO: Separate Z type Moves
    // TODO: hiddenpower moves should be removed


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

    fun registerBlankTmItem(name: String, singleUse: Boolean): RegistrySupplier<BlankTmItem> {
        val item = ITEMS.register(name) {
            BlankTmItem(singleUse)
        }
        return item
    }

    fun registerMoveLearnItem(name: String, moveName: String, moveType: String, singleUse: Boolean): RegistrySupplier<MoveLearnItem> {
        val item = ITEMS.register(name) {
            MoveLearnItem(moveName, moveType, singleUse)
        }
        if (singleUse) {
            TR_ITEMS.add(item)
        } else {
            TM_ITEMS.add(item)
        }
        return item
    }

    fun registerMoveLearnItemsFromJSON(jsonFilePath: String) {
        // Load JSON file from resource directory
        val itemDefinitions = loadMoveLearnItemsFromJson(jsonFilePath)
        // Register TMs
        for (itemDefinition in itemDefinitions) {
            registerMoveLearnItem(
                name = "tm_" + itemDefinition.moveName,
                moveName = itemDefinition.moveName,
                moveType = itemDefinition.moveType,
                singleUse = false
            )
        }
        // Register TRs
        for (itemDefinition in itemDefinitions) {
            registerMoveLearnItem(
                name = "tr_" + itemDefinition.moveName,
                moveName = itemDefinition.moveName,
                moveType = itemDefinition.moveType,
                singleUse = true
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