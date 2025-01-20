package dragomordor.simpletms.events

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonFaintedEvent
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.giveOrDropItemStack
import com.cobblemon.mod.common.util.ifIsType
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.EntityEvent
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.SimpleTMsItems.getTMorTRItemFromMove
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack


object MoveLearnItemDropHandler : EventHandler {

    override fun registerListeners() {
        CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.NORMAL, ::onBattleFainted)
        EntityEvent.LIVING_DEATH.register(EntityEvent.LivingDeath { entity: LivingEntity, source: DamageSource -> onEntityFainted(entity, source) })
    }

    // ------------------------------------------------------------
    // Event Handlers
    // ------------------------------------------------------------

    // When a pokemon faints in battle, drop a TM or TR
    fun onBattleFainted(event: BattleFaintedEvent) {
        // Get all players involved in the battle
        val players = event.battle.players
        // If only one player is involved, proceed (only for 1 player PvE)
        if (players.size == 1) {

            val player = players.get(0)
            val killedBattlePokemon = event.killed
            val killedPokemon = killedBattlePokemon.effectedPokemon

            // Make sure killed pokemon is a wild pokemon
            if (!killedPokemon.isPlayerOwned()) {
                dropMoveLearnItemOnPlayer(killedPokemon, player, true)
            }
        }
        return
    }

    // When pokemon faints outside of battle because of player, drop TM or TR if config allows
    fun onPokemonFainted(killedPokemon: Pokemon, player: ServerPlayer) {
        // Check if config allows dropping items when pokemon faints
        if (!SimpleTMs.config.DropOutsideOfBattle) {
            return
        }

        // Make sure killed pokemon is a wild pokemon
        if (!killedPokemon.isPlayerOwned()) {
            // Thread.sleep(10)
            dropMoveLearnItemOnPlayer(killedPokemon, player, false)
        }
        return
    }

    fun onEntityFainted(entity: LivingEntity, source: DamageSource): EventResult? {

        // If entity is pokemon and source is player, activate onPokemonFainted.
        // Either way, return normal behaviour of event
        if (entity::class == PokemonEntity::class && source.entity?.javaClass == ServerPlayer::class.java) {
            val player = source.entity as ServerPlayer
            onPokemonFainted((entity as PokemonEntity).pokemon, player)
        }

        // Return normal behaviour of event
        return EventResult.pass()
    }

    // ------------------------------------------------------------
    // Helper Functions
    // ------------------------------------------------------------

    fun dropMoveLearnItemOnPlayer(killedPokemon: Pokemon, player: ServerPlayer, inBattle: Boolean) {
        // Get config options

        var DropRateTR = 0.0
        var DropRateTMtoTRRatio = 0.0
        if (inBattle) {
            // If in battle, use in battle Drop rates
            DropRateTR = SimpleTMs.config.DropRateTRInBattle
            DropRateTMtoTRRatio = SimpleTMs.config.DropRateTMtoTRRatioInBattle
        } else {
            // If outside of battle, use outside of battle Drop rates
            DropRateTR = SimpleTMs.config.DropRateTROutsideOfBattle
            DropRateTMtoTRRatio = SimpleTMs.config.DropRateTMtoTRRatioOutsideOfBattle
        }
        // Get an applicable move template from the killed pokemon
        // TODO: Change so move can be selected from list of 3/4 random moves (amount from config) -- only inside battle. Outside of battle, config whether screen is shown
        val droppedMoveTemplate = getRandomApplicableMoveTemplate(killedPokemon)
        val randomDouble = Math.random()
        val isTM = randomDouble < (DropRateTR * DropRateTMtoTRRatio)
        val isTR = randomDouble < DropRateTR

        if (isTM || isTR) {
            val isTRItem = isTR && !isTM
            val droppedItem = getTMorTRItemFromMove(droppedMoveTemplate.create(), isTRItem)
            val droppedItemStack = ItemStack(droppedItem)
            // TODO: add message in battle chat
            player.giveOrDropItemStack(droppedItemStack, true)
        }

    }



    fun getRandomApplicableMoveTemplate(pokemon: Pokemon): MoveTemplate {
        // Get all move information from pokemon
        val allMoves = Moves.all()
        val pokemonPrimaryType = pokemon.primaryType
        val pokemonSecondaryType = pokemon.secondaryType
        val learnset = pokemon.form.moves
        val levelUpMoves = learnset.getLevelUpMovesUpTo(Cobblemon.config.maxPokemonLevel)
        val levelUpMovesUpToPokemonLevel = learnset.getLevelUpMovesUpTo(pokemon.level)
        val tmMoves = learnset.tmMoves
        val tutorMoves = learnset.tutorMoves
        val eggMoves = learnset.eggMoves

        // Get config options
        val DropAny = SimpleTMs.config.DropAny
        val DropPrimaryType = SimpleTMs.config.DropPrimaryType
        val DropSecondaryType = SimpleTMs.config.DropSecondaryType
        val DropLevelList = SimpleTMs.config.DropFromLevelList
        val DropAnyLevel = SimpleTMs.config.DropAnyLevelMoveFromLevelList
        val DropTMs = SimpleTMs.config.DropFromTmMoveList
        val DropTutors = SimpleTMs.config.DropFromTutorMoveList
        val DropEggMoves = SimpleTMs.config.DropFromEggMoveList

        var moveToDrop: MoveTemplate? = null

        // If any move can be dropped, return a random move from all moves
        if (DropAny) {
            // TODO: Exclude moves based on config options
            moveToDrop = allMoves.random()
            return moveToDrop
        }
        // The rest of the checks will append to a list of applicable moves
        val applicableMoves = mutableListOf<MoveTemplate>()

        // (1) Check if primary or secondary type moves can be dropped
        if (DropPrimaryType) {
            val primaryTypeMoves = Moves.all().filter { it.elementalType == pokemonPrimaryType }
            applicableMoves.addAll(primaryTypeMoves)
        }
        if (DropSecondaryType) {
            val secondaryTypeMoves = Moves.all().filter { it.elementalType == pokemonSecondaryType }
            applicableMoves.addAll(secondaryTypeMoves)
        }

        // (2) Check if moves from level list can be dropped
            // if DropAnyLevel is true, add all level moves to applicableMoves
            // if DropLevelList is false, add all level moves up to pokemons current level to applicableMoves
        if (DropAnyLevel) {
            applicableMoves.addAll(levelUpMoves)
        } else if (DropLevelList) {
            applicableMoves.addAll(levelUpMovesUpToPokemonLevel)
        }

        // (3) Check if TM moves can be dropped
        if (DropTMs) {
            applicableMoves.addAll(tmMoves)
        }

        // (4) Check if tutor moves can be dropped
        if (DropTutors) {
            applicableMoves.addAll(tutorMoves)
        }

        // (5) Check if egg moves can be dropped
        if (DropEggMoves) {
            applicableMoves.addAll(eggMoves)
        }

        // Return a random move from the applicable moves
        moveToDrop = applicableMoves.random()
        // TODO: Exclude moves based on config options

        return moveToDrop
    }



}