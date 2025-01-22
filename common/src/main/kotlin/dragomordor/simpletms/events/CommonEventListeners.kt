package dragomordor.simpletms.events

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import dev.architectury.event.EventResult
import dev.architectury.event.events.client.ClientScreenInputEvent
import dev.architectury.event.events.common.EntityEvent
import dragomordor.simpletms.SimpleTMs
// import dragomordor.simpletms.events.CobblemonEventListeners.dropMoveLearnItemOnPlayer
import dragomordor.simpletms.util.Pages
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity

object CommonEventListeners: EventHandler {
    override fun registerListeners() {
        // EntityEvent.LIVING_DEATH.register(EntityEvent.LivingDeath { entity: LivingEntity, source: DamageSource -> onEntityFainted(entity, source) })
    }

    private fun onEntityFainted(entity: LivingEntity, source: DamageSource): EventResult? {

        // If entity is pokemon and source is player, activate onPokemonFainted.
        // Either way, return normal behaviour of event
        if (entity::class == PokemonEntity::class && source.entity?.javaClass == ServerPlayer::class.java) {
            val player = source.entity as ServerPlayer
            onPokemonFainted((entity as PokemonEntity).pokemon, player)
        }
        // Return normal behaviour of event
        return EventResult.pass()
    }


    // ------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------

    private fun scrollUpOrDownTooltip(direction: Double) {
        Pages.CURRENT_PAGE = when {
            direction < 0 && Pages.CURRENT_PAGE == Pages.TOTAL_PAGES - 1 -> 0
            direction < 0 -> Pages.CURRENT_PAGE + 1
            direction > 0 && Pages.CURRENT_PAGE == 0 -> Pages.TOTAL_PAGES - 1
            direction > 0 -> Pages.CURRENT_PAGE - 1
            else -> Pages.CURRENT_PAGE
        }
    }


    // When pokemon faints outside of battle because of player, drop TM or TR if config allows
    private fun onPokemonFainted(killedPokemon: Pokemon, player: ServerPlayer) {
        // Check if config allows dropping items when pokemon faints
        if (!SimpleTMs.config.DropOutsideOfBattle) {
            return
        }

        // Make sure killed pokemon is a wild pokemon
        if (!killedPokemon.isPlayerOwned()) {
            // Thread.sleep(10)
            // dropMoveLearnItemOnPlayer(killedPokemon, player, false)
        }
        return
    }

}