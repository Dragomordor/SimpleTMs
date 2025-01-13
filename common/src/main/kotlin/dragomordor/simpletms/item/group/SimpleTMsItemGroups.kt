package dragomordor.simpletms.item.group


import dev.architectury.registry.registries.DeferredRegister
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.SimpleTMsItems
import dragomordor.simpletms.item.SimpleTMsItem
import dragomordor.simpletms.util.simpletmsResource
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.ItemLike
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.resources.ResourceKey
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTab.DisplayItemsGenerator
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters
import net.minecraft.world.item.CreativeModeTab.Output
import net.minecraft.world.item.Item
import org.intellij.lang.annotations.Identifier

@Suppress("unused", "UNUSED_PARAMETER")
object SimpleTMsItemGroups {
    private val ALL = arrayListOf<ItemGroupHolder>()
    // private val INJECTORS = hashMapOf<ResourceKey<CreativeModeTab>, (injector: Injector) -> Unit>()

    //val TM_GROUP: DeferredRegister<CreativeModeTab> = DeferredRegister.create(SimpleTMs.MOD_ID, Registries.CREATIVE_MODE_TAB)
    // ------------------------------------------------------------
    // Key for all item groups
    // ------------------------------------------------------------

    //TM_GROUP.register("tm_items")

    // Key for TMs
    @JvmStatic val TM_KEY = this.create("tm_items", this::tmEntries) {
        ItemStack(SimpleTMsItems.BLANK_TM)
    }
    // Key for TRs
    @JvmStatic val TR_KEY = this.create("tr_items", this::trEntries) {
        ItemStack(SimpleTMsItems.BLANK_TR)
    }

    @JvmStatic val TM_ITEMS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(TM_KEY)
    @JvmStatic val TR_ITEMS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(TR_KEY)

    // No Injectors into existing tabs for this mod


    // ------------------------------------------------------------
    // Adding items to new creative tabs
    // ------------------------------------------------------------

        //



    // ------------------------------------------------------------
    // Functions for adding items to new creative tabs
    // ------------------------------------------------------------
    private fun tmEntries(displayContext: ItemDisplayParameters, entries: Output) {
        SimpleTMsItems.tmItems.forEach(entries::accept)
    }
    private fun trEntries(displayContext: ItemDisplayParameters, entries: Output) {
        SimpleTMsItems.trItems.forEach(entries::accept)
    }


    // ------------------------------------------------------------
    // Functions for adding items to new creative tabs
    // ------------------------------------------------------------
    fun register(consumer: (holder: ItemGroupHolder) -> CreativeModeTab) {
        ALL.forEach(consumer::invoke)
    }

    private fun create(name: String, entryCollector: DisplayItemsGenerator, displayIconProvider: () -> ItemStack): ResourceKey<CreativeModeTab> {
        val key = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), simpletmsResource(name))
        this.ALL += ItemGroupHolder(key, displayIconProvider, entryCollector)
        return key
    }

    data class ItemGroupHolder(
        val key: ResourceKey<CreativeModeTab>,
        val displayIconProvider: () -> ItemStack,
        val entryCollector: CreativeModeTab.DisplayItemsGenerator,
        val displayName: Component = Component.translatable("itemGroup.${key.location().namespace}.${key.location().path}")
    )
//
//    interface Injector {
//
//        /**
//         * Places the given [item] at the start of a creative tab.
//         *
//         * @param item The [ItemLike] being added at the start of a tab.
//         */
//        fun putFirst(item: ItemLike)
//
//        /**
//         * Places the given [item] before the [target].
//         * If the [target] is not present behaves as [putLast].
//         *
//         * @param item The [ItemLike] being added before [target].
//         * @param target The [ItemLike] being targeted.
//         */
//        fun putBefore(item: ItemLike, target: ItemLike)
//
//        /**
//         * Places the given [item] after the [target].
//         * If the [target] is not present behaves as [putLast].
//         *
//         * @param item The [ItemLike] being added after [target].
//         * @param target The [ItemLike] being targeted.
//         */
//        fun putAfter(item: ItemLike, target: ItemLike)
//
//        /**
//         * Places the given [item] at the end of a creative tab.
//         *
//         * @param item The [ItemLike] being added at the end of a tab.
//         */
//        fun putLast(item: ItemLike)
//
//    }

}