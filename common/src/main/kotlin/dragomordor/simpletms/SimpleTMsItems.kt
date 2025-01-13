package dragomordor.simpletms

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dragomordor.simpletms.item.custom.BlankTmItem
import net.minecraft.world.item.Item
import net.minecraft.core.registries.Registries


@Suppress("unused", "SameParameterValue")
object SimpleTMsItems {
    internal val ITEMS: DeferredRegister<Item> = DeferredRegister.create(SimpleTMs.MOD_ID, Registries.ITEM)

    // TODO: Lists for tm and tr items


    // ------------------------------------------------------------
    // Register Items
    // ------------------------------------------------------------
    // Blank TM and TR
//    val BLANK_TM: RegistrySupplier<BlankTmItem> = ITEMS.register("blank_tm") {
//        BlankTmItem(singleUse = false)
//    }
    val BLANK_TM = registerBlankTmItem("tm_blank", false)
    val BLANK_TR = registerBlankTmItem("tr_blank", true)

    // TODO: Add blank TM and TR to respective lists


    // ------------------------------------------------------------
    // Register Item Functions
    // ------------------------------------------------------------

    fun registerBlankTmItem(name: String, singleUse: Boolean): RegistrySupplier<BlankTmItem> {
        val item = ITEMS.register(name) {
            BlankTmItem(singleUse)
        }
        return item
    }



    // Register all mod items
    fun registerModItems() {
        SimpleTMs.LOGGER.info("Register Mod Items for " + SimpleTMs.MOD_ID)
        ITEMS.register()
    }


    // ------------------------------------------------------------
    // Other Functions
    // ------------------------------------------------------------



}