package dragomordor.simpletms.forge

import dragomordor.simpletms.SimpleTMs.MOD_ID
import dragomordor.simpletms.SimpleTMs.init
import dragomordor.simpletms.SimpleTMsClient
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLEnvironment

@Mod(MOD_ID)
object SimpleTMs {
    init {
        init()

        if (FMLEnvironment.dist == Dist.CLIENT) {
            SimpleTMsClient.init()
        }

    }


}