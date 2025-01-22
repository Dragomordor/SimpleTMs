package dragomordor.simpletms.neoforge

import dragomordor.simpletms.SimpleTMsClient
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.common.NeoForge
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

object SimpleTMsClient {

    fun init() {
        with(MOD_BUS) {
            addListener(::onClientSetup)
        }
        with (NeoForge.EVENT_BUS) {
        }
    }



    private fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork{
            SimpleTMsClient.init()
        }
    }

}


