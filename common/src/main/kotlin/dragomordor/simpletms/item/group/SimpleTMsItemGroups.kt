package dragomordor.simpletms.item.group

import dragomordor.simpletms.SimpleTMsItems
import dragomordor.simpletms.util.simpletmsResource
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack
import net.minecraft.resources.ResourceKey
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters
import net.minecraft.world.item.CreativeModeTab.Output
import net.minecraft.world.item.ItemDisplayContext
import kotlin.reflect.KFunction2


object SimpleTMsItemGroups {
     //------------------------------------------------------------
     // Key for all item groups
     //------------------------------------------------------------
    private val ALL = arrayListOf<ItemGroupHolder>()
    // No Injectors into existing tabs for this mod

    @JvmStatic val TM_ITEMs_KEY = this.create("tm_items", this::tmItemEntries) {
        ItemStack(SimpleTMsItems.BLANK_TM.get())
    }
    @JvmStatic val TR_ITEMs_KEY = this.create("tr_items", this::trItemEntries) {
        ItemStack(SimpleTMsItems.BLANK_TR.get())
    }

    // ------------------------------------------------------------
    // Adding new creative tabs
    // ------------------------------------------------------------

    @JvmStatic val TM_ITEMS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(TM_ITEMs_KEY)
    @JvmStatic val TR_ITEMS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(TR_ITEMs_KEY)

    // ------------------------------------------------------------
    // Adding items to new creative tabs
    // ------------------------------------------------------------

    // TM Items
    private fun tmItemEntries(displayContext: ItemDisplayParameters, entries: Output) {
        // Add all tm items here
        entries.accept(SimpleTMsItems.BLANK_TM.get())
        // entries.accept(SimpleTMsItems.TM_TACKLE.get())
    }

    // TR Items
    private fun trItemEntries(displayContext: ItemDisplayParameters, entries: Output) {
        // Add all tr items here
        entries.accept(SimpleTMsItems.BLANK_TR.get())
        // entries.accept(SimpleTMsItems.TR_TACKLE.get())

        // accept all TR items -- collection
        entries.acceptAll(SimpleTMsItems.TR_ITEMS)
    }

    // TODO: Custom tms and custom trs from users in separate tabs

    // ------------------------------------------------------------
    // Functions for adding items to new creative tabs
    // ------------------------------------------------------------

    fun register(consumer: (holder: ItemGroupHolder) -> CreativeModeTab) {
        ALL.forEach(consumer::invoke)
    }


//    data class ItemGroupHolder(
//        val key: ResourceKey<CreativeModeTab>,
//        val displayIconProvider: () -> ItemStack,
//        val entryCollector: KFunction2<ItemDisplayContext, Output, Unit>,
//        val displayName: Component = Component.translatable("itemGroup.${key.location().namespace}.${key.location().path}")
//    ) {
//        fun entryCollector(adaptedParams: CreativeModeTab.ItemDisplayParameters, output: CreativeModeTab.Output) {
//
//        }
//    }

    data class ItemGroupHolder(
        val key: ResourceKey<CreativeModeTab>,
        val displayIconProvider: () -> ItemStack,
        val entryCollector: CreativeModeTab.DisplayItemsGenerator,
        val displayName: Component = Component.translatable("itemGroup.${key.location().namespace}.${key.location().path}")
    )


//    private fun create(name: String, entryCollector: KFunction2<ItemDisplayContext, Output, Unit>, displayIconProvider: () -> ItemStack): ResourceKey<CreativeModeTab> {
//        val key = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), simpletmsResource(name))
//        this.ALL += ItemGroupHolder(key, displayIconProvider, entryCollector)
//        return key
//    }

    private fun create(name: String, entryCollector: CreativeModeTab.DisplayItemsGenerator, displayIconProvider: () -> ItemStack): ResourceKey<CreativeModeTab> {
        val key = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), simpletmsResource(name))
        this.ALL += ItemGroupHolder(key, displayIconProvider, entryCollector)
        return key
    }



    // ------------------------------------------------------------
    // ------------------------------------------------------------







}