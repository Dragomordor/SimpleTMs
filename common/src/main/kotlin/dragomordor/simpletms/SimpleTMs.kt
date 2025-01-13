package dragomordor.simpletms

import com.mojang.logging.LogUtils.getLogger
import dev.architectury.registry.registries.DeferredRegister
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item
import org.slf4j.Logger

object SimpleTMs {
    const val MOD_ID = "simpletms"
    //internal val ITEMS: DeferredRegister<Item> = DeferredRegister.create(MOD_ID, Registries.ITEM)

    @JvmField
    val LOGGER: Logger = getLogger()

    fun init() {
        LOGGER.info("Registering items")
        //ITEMS.register()

    }
}