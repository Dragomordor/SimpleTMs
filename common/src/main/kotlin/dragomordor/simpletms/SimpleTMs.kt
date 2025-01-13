package dragomordor.simpletms

import com.cobblemon.mod.common.api.drop.DropEntry
import com.cobblemon.mod.common.api.scheduling.ServerRealTimeTaskTracker
import com.cobblemon.mod.common.api.scheduling.ServerTaskTracker
import com.cobblemon.mod.common.platform.events.PlatformEvents
import com.mojang.logging.LogUtils.getLogger
import dev.architectury.registry.registries.DeferredRegister
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item
import org.slf4j.Logger

object SimpleTMs {
    const val MOD_ID = "simpletms"
    const val VERSION = "2.0.0"
    // private const val CONFIG_PATH = "config/$MOD_ID/main.json"

    @JvmField
    val LOGGER: Logger = getLogger()

    fun init() {
        LOGGER.info("Using SimpleTMs ($VERSION)")
        SimpleTMsItems.registerModItems()
    }


}