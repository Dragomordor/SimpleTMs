package dragomordor.simpletms

import com.cobblemon.mod.common.config.constraint.IntConstraint
import com.mojang.logging.LogUtils.getLogger
import dragomordor.simpletms.config.DoubleConstraint
import dragomordor.simpletms.config.SimpleTMsConfig
import dragomordor.simpletms.events.MoveLearnItemDropEventListeners
import dragomordor.simpletms.events.CobblemonPokemonSpeciesListener
import org.slf4j.Logger
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

object SimpleTMs {
    const val MOD_ID = "simpletms"
    const val VERSION = "2.0.2"
    const val CONFIG_PATH = "config/$MOD_ID/main.json"
    @JvmField
    val LOGGER: Logger = getLogger()

    lateinit var config: SimpleTMsConfig

    fun preinit() {
        this.loadConfig()
    }

    fun init() {
        LOGGER.info("Using SimpleTMs ($VERSION)")
        SimpleTMsItems.registerModItems()
        // Event listeners
        MoveLearnItemDropEventListeners.registerListeners()
        CobblemonPokemonSpeciesListener.registerListeners()
    }

    // ------------------------------------------------------------------
    // Save and Load Config Functions
    // ------------------------------------------------------------------

    private fun loadConfig() {
        val configFile = File(CONFIG_PATH)
        configFile.parentFile.mkdirs()

        // Check config existence and load if it exists, otherwise create default.
        if (configFile.exists()) {
            try {
                val fileReader = FileReader(configFile)
                this.config = SimpleTMsConfig.GSON.fromJson(fileReader, SimpleTMsConfig::class.java)
                fileReader.close()
            } catch (exception: Exception) {
                LOGGER.error("Failed to load config file for $MOD_ID. Using default config until the following has been addressed:")
                this.config = SimpleTMsConfig()
                exception.printStackTrace()
            }

            val defaultConfig = SimpleTMsConfig()

            SimpleTMsConfig::class.memberProperties.forEach {
                val field = it.javaField!!
                it.isAccessible = true
                field.annotations.forEach {
                    when (it) {
                        is IntConstraint -> {
                            var value = field.get(config)
                            if (value is Int) {
                                value = value.coerceIn(it.min, it.max)
                                field.set(config, value)
                            }
                        }
                        is DoubleConstraint -> {
                            var value = field.get(config)
                            if (value is Double) {
                                value = value.coerceIn(it.min, it.max)
                                field.set(config, value)
                            }
                        }

                    }
                }
            }
        } else {
            this.config = SimpleTMsConfig()
        }

        this.saveConfig()


    }

    private fun saveConfig() {
        try {
            val configFile = File(CONFIG_PATH)
            val fileWriter = FileWriter(configFile)
            // put the config to json then flush the writer to commence writing.
            SimpleTMsConfig.GSON.toJson(this.config, fileWriter)
            fileWriter.flush()
            fileWriter.close()
        } catch (exception: Exception) {
            LOGGER.error("Failed to save config file for $MOD_ID. The following exception occurred:")
            exception.printStackTrace()
        }
    }

}

