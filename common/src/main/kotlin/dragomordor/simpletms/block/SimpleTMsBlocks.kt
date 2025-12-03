package dragomordor.simpletms.block

import com.mojang.serialization.MapCodec
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.block.custom.TMMachineBlock
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction

/**
 * Registry for all SimpleTMs blocks.
 */
object SimpleTMsBlocks {

    private val BLOCKS: DeferredRegister<Block> = DeferredRegister.create(SimpleTMs.MOD_ID, Registries.BLOCK)

    // ========================================
    // Block Registrations
    // ========================================

    val TM_MACHINE: RegistrySupplier<TMMachineBlock> = BLOCKS.register("machine_tm") {
        TMMachineBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .pushReaction(PushReaction.BLOCK)
                .strength(2F)
                .noOcclusion()
                .lightLevel { 0 } // TODO: Change to on and off states like pc?
        )
    }

    // ========================================
    // Registration
    // ========================================

    /**
     * Register all blocks. Call this during mod initialization.
     */
    fun register() {
        SimpleTMs.LOGGER.info("Registering blocks for ${SimpleTMs.MOD_ID}")
        BLOCKS.register()
    }
}
