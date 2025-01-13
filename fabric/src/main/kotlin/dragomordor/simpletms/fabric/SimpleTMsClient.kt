package dragomordor.simpletms.fabric


import dragomordor.simpletms.SimpleTMsClient.init
import net.fabricmc.api.ClientModInitializer


@Suppress("Unused")

class SimpleTMsClient : ClientModInitializer {
    override fun onInitializeClient() {
        init()
    }
}
