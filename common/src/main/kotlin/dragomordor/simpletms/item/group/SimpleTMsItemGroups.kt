package dragomordor.simpletms.item.group

import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.SimpleTMs.MOD_ID
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
import java.io.File
import kotlin.reflect.KFunction2


object SimpleTMsItemGroups {

    private const val GEB_CUSTOM_MOVE_JSON_PATH = "$MOD_ID/movelearnitems/geb_custom_moves.json"

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

    // Custom
    @JvmStatic val CUSTOM_TM_ITEMs_KEY = this.create("custom_tm_items", this::customTmEntries) {
        ItemStack(SimpleTMsItems.BLANK_TM.get())
    }
    @JvmStatic val CUSTOM_TR_ITEMs_KEY = this.create("custom_tr_items", this::customTrEntries) {
        ItemStack(SimpleTMsItems.BLANK_TR.get())
    }


    // ------------------------------------------------------------
    // Adding new creative tabs
    // ------------------------------------------------------------

    @JvmStatic val TM_ITEMS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(TM_ITEMs_KEY)
    @JvmStatic val TR_ITEMS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(TR_ITEMs_KEY)
    // Custom
    @JvmStatic val CUSTOM_TM_ITEMS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(CUSTOM_TM_ITEMs_KEY)
    @JvmStatic val CUSTOM_TR_ITEMS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(CUSTOM_TR_ITEMs_KEY)

    // ------------------------------------------------------------
    // Adding items to new creative tabs
    // ------------------------------------------------------------

    // TM Items
    private fun tmItemEntries(displayContext: ItemDisplayParameters, entries: Output) {
        // Add all tm items here
        entries.accept(SimpleTMsItems.BLANK_TM.get())
        for (item in SimpleTMsItems.TM_ITEMS) {
            entries.accept(item.get())
        }
    }

    // TR Items
    private fun trItemEntries(displayContext: ItemDisplayParameters, entries: Output) {
        // Add all tr items here
        entries.accept(SimpleTMsItems.BLANK_TR.get())
        for (item in SimpleTMsItems.TR_ITEMS) {
            entries.accept(item.get())
        }
    }

    // TODO: Custom tms and custom trs from users in separate tabs
    private fun customTmEntries(displayContext: ItemDisplayParameters, entries: Output) {
        // Custom items
        if (SimpleTMs.config.allowCustomMoves || File(GEB_CUSTOM_MOVE_JSON_PATH).exists()) {
            for (item in SimpleTMsItems.CUSTOM_TM_ITEMS) {
                entries.accept(item.get())
            }
        }
    }

    private fun customTrEntries(displayContext: ItemDisplayParameters, entries: Output) {
        // Custom items
        if (SimpleTMs.config.allowCustomMoves || File(GEB_CUSTOM_MOVE_JSON_PATH).exists()) {
            for (item in SimpleTMsItems.CUSTOM_TR_ITEMS) {
                entries.accept(item.get())
            }
        }
    }

    // ------------------------------------------------------------
    // Functions for adding items to new creative tabs
    // ------------------------------------------------------------

    fun register(consumer: (holder: ItemGroupHolder) -> CreativeModeTab) {
        ALL.forEach(consumer::invoke)
    }

    data class ItemGroupHolder(
        val key: ResourceKey<CreativeModeTab>,
        val displayIconProvider: () -> ItemStack,
        val entryCollector: CreativeModeTab.DisplayItemsGenerator,
        val displayName: Component = Component.translatable("itemGroup.${key.location().namespace}.${key.location().path}")
    )

    private fun create(name: String, entryCollector: CreativeModeTab.DisplayItemsGenerator, displayIconProvider: () -> ItemStack): ResourceKey<CreativeModeTab> {
        val key = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), simpletmsResource(name))
        this.ALL += ItemGroupHolder(key, displayIconProvider, entryCollector)
        return key
    }



    // ------------------------------------------------------------
    // ------------------------------------------------------------







}