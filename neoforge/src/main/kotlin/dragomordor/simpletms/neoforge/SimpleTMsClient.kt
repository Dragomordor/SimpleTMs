package dragomordor.simpletms.neoforge

import dragomordor.simpletms.SimpleTMsClient
import dragomordor.simpletms.ui.MoveCaseScreen
import dragomordor.simpletms.ui.SimpleTMsMenuTypes
import dragomordor.simpletms.ui.SimpleTMsScreens
import dragomordor.simpletms.ui.TMMachineScreen
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
        // Register Move Case Screen (TM/TR Case items)
        event.register(
            SimpleTMsMenuTypes.MOVE_CASE_MENU.get(),
            ::MoveCaseScreen
        )

        // Register TM Machine Screen (TM Machine block)
        event.register(
            SimpleTMsMenuTypes.TM_MACHINE_MENU.get(),
            ::TMMachineScreen
        )
    }
}