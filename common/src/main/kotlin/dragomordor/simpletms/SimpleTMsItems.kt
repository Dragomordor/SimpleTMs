package dragomordor.simpletms

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dragomordor.simpletms.item.custom.BlankTmItem
//import dragomordor.simpletms.item.custom.MoveLearnItem
import net.minecraft.world.item.Item
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.ItemStack


@Suppress("unused", "SameParameterValue")
object SimpleTMsItems {
    internal val ITEMS: DeferredRegister<Item> = DeferredRegister.create(SimpleTMs.MOD_ID, Registries.ITEM)

    // TODO: Tags for tm and tr items
    val TR_ITEMS: MutableCollection<ItemStack> = TODO()


    // ------------------------------------------------------------
    // Register Items
    // ------------------------------------------------------------
    // Blank TM and TR
//    val BLANK_TM: RegistrySupplier<BlankTmItem> = ITEMS.register("blank_tm") {
//        BlankTmItem(singleUse = false)
//    }
    val BLANK_TM = registerBlankTmItem("tm_blank", false)
    val BLANK_TR = registerBlankTmItem("tr_blank", true)


//    // TM items
//    // TODO: Testing using tackle
//    val TM_TACKLE = registerMoveLearnItem("tm_tackle", "tackle", "Normal", false)
//
//    // TR items
//    val TR_TACKLE = registerMoveLearnItem("tr_tackle", "tackle", "Normal", true)

    // TODO: items to tags above


    // ------------------------------------------------------------
    // Register Item Functions
    // ------------------------------------------------------------

    fun registerBlankTmItem(name: String, singleUse: Boolean): RegistrySupplier<BlankTmItem> {
        val item = ITEMS.register(name) {
            BlankTmItem(singleUse)
        }
        return item
    }

//    fun registerMoveLearnItem(name: String, moveName: String, moveType: String, singleUse: Boolean): RegistrySupplier<MoveLearnItem> {
//        val item = ITEMS.register(name) {
//            MoveLearnItem(moveName, moveType, singleUse)
//        }
//        return item
//    }

    // Register all mod items
    fun registerModItems() {
        SimpleTMs.LOGGER.info("Register Mod Items for " + SimpleTMs.MOD_ID)
        ITEMS.register()
    }


    // ------------------------------------------------------------
    // Other Functions
    // ------------------------------------------------------------



}