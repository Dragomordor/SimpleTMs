package dragomordor.simpletms.neoforge

import dragomordor.simpletms.SimpleTMsClient
import dragomordor.simpletms.ui.MoveCaseScreen
import dragomordor.simpletms.ui.SimpleTMsMenuTypes
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent
import net.neoforged.neoforge.common.NeoForge
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

object SimpleTMsClient {

    fun init() {
        with(MOD_BUS) {
            addListener(::onClientSetup)
            addListener(::onRegisterMenuScreens)
        }
        with (NeoForge.EVENT_BUS) {
        }
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            SimpleTMsClient.init()
        }
    }

    private fun onRegisterMenuScreens(event: RegisterMenuScreensEvent) {
        event.register(
            SimpleTMsMenuTypes.MOVE_CASE_MENU.get(),
            ::MoveCaseScreen
        )
    }

}