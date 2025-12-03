package dragomordor.simpletms.block

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dragomordor.simpletms.SimpleTMs
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item

/**
 * Registry for block items (items that place blocks).
 * 
 * These are registered separately from blocks to allow for custom item properties.
 */
object SimpleTMsBlockItems {

    private val ITEMS: DeferredRegister<Item> = DeferredRegister.create(SimpleTMs.MOD_ID, Registries.ITEM)

    // ========================================
    // Block Item Registrations
    // ========================================

    val TM_MACHINE: RegistrySupplier<BlockItem> = ITEMS.register("machine_tm") {
        BlockItem(
            SimpleTMsBlocks.TM_MACHINE.get(),
            Item.Properties().stacksTo(64)
        )
    }

    // ========================================
    // Registration
    // ========================================

    /**
     * Register all block items. Call this during mod initialization AFTER blocks.
     */
    fun register() {
        SimpleTMs.LOGGER.info("Registering block items for ${SimpleTMs.MOD_ID}")
        ITEMS.register()
    }
}
