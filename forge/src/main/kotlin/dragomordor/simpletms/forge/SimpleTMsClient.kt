package dragomordor.simpletms.forge

import dragomordor.simpletms.SimpleTMsClient
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import thedarkcolour.kotlinforforge.forge.MOD_BUS

object SimpleTMsClient {

    fun init() {

        with(MOD_BUS) {
            addListener(::onClientSetup)
        }

    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork{
            SimpleTMsClient.init()
        }
    }

}


