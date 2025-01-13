package dragomordor.simpletms

import com.cobblemon.mod.common.platform.PlatformRegistry
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dragomordor.simpletms.item.custom.BlankTmItem
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item

@Suppress("unused", "SameParameterValue")
object SimpleTMsItems : PlatformRegistry<Registry<Item>, ResourceKey<Registry<Item>>, Item>() {
    override val registry: Registry<Item> = BuiltInRegistries.ITEM
    override val resourceKey: ResourceKey<Registry<Item>> = Registries.ITEM

    val ITEMS: DeferredRegister<Item> = DeferredRegister.create(SimpleTMs.MOD_ID, Registries.ITEM)


    // Lists
    @JvmField val tmItems = mutableListOf<Item>()
    @JvmField val trItems = mutableListOf<Item>()
    // TODO: Add more lists for other item types
        // Example, Z type moves (tms and trs separate); Fire,water etc. (elemental) tms and trs separate; standard tms and trs added through config; custom tms and trs added through config; etc.

    // ------------------------------------------------------------
    // Register Items
    // ------------------------------------------------------------
    // Blank TM and TR
    @JvmField val BLANK_TM: Item = blankTmItem("tm_blank", false)
    @JvmField val BLANK_TR: Item = blankTmItem("tr_blank", true)

//    @JvmField val BLANK_TM = blankTmItem("tm_blank", false)
//    @JvmField val BLANK_TR = blankTmItem("tr_blank", true)

    // Add items to lists
    init {
        // Automatically add ALL items with "tm_" or "tr_" prefix from this mod to respective lists
//        BuiltInRegistries.ITEM.keySet().forEach { key ->
//            if (key.namespace == "simpletms") {  // TODO: Check if this is the correct namespace
//                val item = BuiltInRegistries.ITEM[key]
//                if (item != null) {
//                    when {
//                        key.path.startsWith("tm_") -> tmItems.add(item)
//                        key.path.startsWith("tr_") -> trItems.add(item)
//                    }
//                }
//            }
//        }

        // Add blank TM and TR to respective lists
        tmItems.add(BLANK_TM)
        trItems.add(BLANK_TR)
    }


    // ------------------------------------------------------------
    // Register Item Functions
    // ------------------------------------------------------------

//    private fun blankTmItem(name: String, singleUse: Boolean): RegistrySupplier<BlankTmItem> {
//        return ITEMS.register(name) { BlankTmItem(singleUse) }
//    }

    private fun blankTmItem(name: String, singleUse: Boolean): BlankTmItem = this.create(name, BlankTmItem(singleUse))

    // Register all mod items
    fun registerModItems() {
        SimpleTMs.LOGGER.info("Register Mod Items for " + SimpleTMs.MOD_ID)
        ITEMS.register()
    }

    // ------------------------------------------------------------
    // Other Functions
    // ------------------------------------------------------------



}