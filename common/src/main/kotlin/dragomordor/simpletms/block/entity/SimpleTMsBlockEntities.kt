package dragomordor.simpletms.block.entity

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.block.SimpleTMsBlocks
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntityType

/**
 * Registry for all SimpleTMs block entity types.
 */
object SimpleTMsBlockEntities {

    private val BLOCK_ENTITIES: DeferredRegister<BlockEntityType<*>> = 
        DeferredRegister.create(SimpleTMs.MOD_ID, Registries.BLOCK_ENTITY_TYPE)

    // ========================================
    // Block Entity Registrations
    // ========================================

    val TM_MACHINE: RegistrySupplier<BlockEntityType<TMMachineBlockEntity>> = 
        BLOCK_ENTITIES.register("machine_tm") {
            BlockEntityType.Builder.of(
                ::TMMachineBlockEntity,
                SimpleTMsBlocks.TM_MACHINE.get()
            ).build(null)
        }

    // ========================================
    // Registration
    // ========================================

    /**
     * Register all block entity types. Call this during mod initialization.
     */
    fun register() {
        SimpleTMs.LOGGER.info("Registering block entities for ${SimpleTMs.MOD_ID}")
        BLOCK_ENTITIES.register()
    }
}
