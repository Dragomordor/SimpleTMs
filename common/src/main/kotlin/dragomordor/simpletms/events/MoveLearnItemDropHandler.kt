package dragomordor.simpletms.events

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.giveOrDropItemStack
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.util.getTMorTRItemFromMove
import net.minecraft.world.item.ItemStack

object MoveLearnItemDropHandler : EventHandler {

    override fun registerListeners() {
        CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.NORMAL, ::onBattleFainted)
    }

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

                // Get config options
                val DropRateTR = SimpleTMs.config.DropRateTR
                val DropRateTMtoTRRatio = SimpleTMs.config.DropRateTMtoTRRatio

                // Get an applicable move template from the killed pokemon
                val droppedMoveTemplate = getRandomApplicableMoveTemplate(killedPokemon)

                // Determine whether to drop a TM or TR
                val randomDouble = Math.random().toDouble()
                println("Random double: $randomDouble") // TODO: DEBUG
                if (randomDouble < (DropRateTR*DropRateTMtoTRRatio)) {
                    // Drop a TM
                    val droppedTM = getTMorTRItemFromMove(droppedMoveTemplate.create(), false)
                    val droppedTMStack = ItemStack(droppedTM)
                    player.giveOrDropItemStack(droppedTMStack, true)

                } else if (randomDouble < DropRateTR) {
                    // Drop a TR
                    val droppedItem = getTMorTRItemFromMove(droppedMoveTemplate.create(), true)
                    player.giveOrDropItemStack(ItemStack(droppedItem), true)
                } else {
                    // Drop nothing
                    return
                }
            }
        }
        return
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