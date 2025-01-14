package dragomordor.simpletms

import com.mojang.logging.LogUtils.getLogger
import net.minecraft.server.MinecraftServer
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