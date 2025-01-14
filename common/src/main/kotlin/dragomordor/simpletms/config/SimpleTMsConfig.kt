package dragomordor.simpletms.config

import com.cobblemon.mod.common.config.NodeCategory
import com.google.gson.GsonBuilder

class SimpleTMsConfig {

    //TODO: Implement config options

    companion object {
        val GSON = GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()
    }

    // @NodeCategory
    var eggMovesLearnable: Boolean = true
    var tutorMovesLearnable: Boolean = true
    var anyMovesLearnable: Boolean = true

}