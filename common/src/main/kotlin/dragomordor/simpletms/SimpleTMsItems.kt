package dragomordor.simpletms

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dragomordor.simpletms.item.custom.BlankTmItem
import dragomordor.simpletms.item.custom.MoveLearnItem
import net.minecraft.world.item.Item
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike


@Suppress("unused", "SameParameterValue")
object SimpleTMsItems {

    internal val ITEMS: DeferredRegister<Item> = DeferredRegister.create(SimpleTMs.MOD_ID, Registries.ITEM)

    // TODO: Tags for tm and tr items
    val TM_ITEMS = mutableListOf<RegistrySupplier<MoveLearnItem>>()
    val TR_ITEMS = mutableListOf<RegistrySupplier<MoveLearnItem>>()

    // ------------------------------------------------------------
    // Register Items
    // ------------------------------------------------------------
    // Blank TM and TR
    val BLANK_TM = registerBlankTmItem("tm_blank", false)
    val BLANK_TR = registerBlankTmItem("tr_blank", true)

    // TM items
    // TODO: Testing using a few items
    val TM_10000000VOLTTHUNDERBOLT = registerMoveLearnItem("tm_10000000voltthunderbolt", "10000000voltthunderbolt", "Electric", false)
    val TM_ABSORB = registerMoveLearnItem("tm_absorb", "absorb", "Grass", false)
    val TM_TACKLE = registerMoveLearnItem("tm_tackle", "tackle", "Normal", false)

    // TR items
    val TR_10000000VOLTTHUNDERBOLT = registerMoveLearnItem("tr_10000000voltthunderbolt", "10000000voltthunderbolt", "Electric", true)
    val TR_ABSORB = registerMoveLearnItem("tr_absorb", "absorb", "Grass", true)
    val TR_TACKLE = registerMoveLearnItem("tr_tackle", "tackle", "Normal", true)

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

    // Register all mod items
    fun registerModItems() {
        SimpleTMs.LOGGER.info("Register Mod Items for " + SimpleTMs.MOD_ID)
        ITEMS.register()
    }

    // ------------------------------------------------------------
    // Other Functions
    // ------------------------------------------------------------



}