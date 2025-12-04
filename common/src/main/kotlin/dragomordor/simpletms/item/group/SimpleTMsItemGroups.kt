package dragomordor.simpletms.item.group

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.item.group.CobblemonItemGroups
import com.cobblemon.mod.common.item.group.CobblemonItemGroups.Injector
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.SimpleTMs.MOD_ID
import dragomordor.simpletms.SimpleTMsItems
import dragomordor.simpletms.block.SimpleTMsBlockItems
import dragomordor.simpletms.block.SimpleTMsBlocks
import dragomordor.simpletms.util.simpletmsResource
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack
import net.minecraft.resources.ResourceKey
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters
import net.minecraft.world.item.CreativeModeTab.Output
import java.io.File


object SimpleTMsItemGroups {

    private const val GEB_CUSTOM_MOVE_JSON_PATH = "config/$MOD_ID/custom/geb_custom_moves.json"

     //------------------------------------------------------------
     // Key for all item groups
     //------------------------------------------------------------
    private val ALL = arrayListOf<ItemGroupHolder>()

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

    // Tab for cases and TM machine
    @JvmStatic val TM_STORAGE_ITEMS_KEY = this.create("tm_storage_items", this::tmStorageEntries) {
        ItemStack(SimpleTMsItems.MOVE_TM_CASE.get())
    }

    // ------------------------------------------------------------
    // Adding new creative tabs
    // ------------------------------------------------------------

    @JvmStatic val TM_ITEMS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(TM_ITEMs_KEY)
    @JvmStatic val TR_ITEMS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(TR_ITEMs_KEY)
     // Custom
    @JvmStatic val CUSTOM_TM_ITEMS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(CUSTOM_TM_ITEMs_KEY)
    @JvmStatic val CUSTOM_TR_ITEMS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(CUSTOM_TR_ITEMs_KEY)


    // Tab for cases and TM machine
    @JvmStatic val TR_STORAGE_ITEMS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(TM_STORAGE_ITEMS_KEY)

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
        if (SimpleTMs.config.allowCustomMovesAndEditing || File(GEB_CUSTOM_MOVE_JSON_PATH).exists()) {
            for (item in SimpleTMsItems.CUSTOM_TM_ITEMS) {
                entries.accept(item.get())
            }
        }
    }

    private fun customTrEntries(displayContext: ItemDisplayParameters, entries: Output) {
        // Custom items
        if (SimpleTMs.config.allowCustomMovesAndEditing || File(GEB_CUSTOM_MOVE_JSON_PATH).exists()) {
            for (item in SimpleTMsItems.CUSTOM_TR_ITEMS) {
                entries.accept(item.get())
            }
        }
    }

    // TM Storage Entries
    private fun tmStorageEntries(displayContext: ItemDisplayParameters, entries: Output) {
        entries.accept(SimpleTMsItems.MOVE_TM_CASE.get())
        entries.accept(SimpleTMsItems.MOVE_TR_CASE.get())
        entries.accept(SimpleTMsBlockItems.TM_MACHINE.get())
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