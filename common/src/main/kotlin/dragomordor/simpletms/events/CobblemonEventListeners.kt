package dragomordor.simpletms.events

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.callback.MoveSelectCallbacks
import com.cobblemon.mod.common.api.callback.MoveSelectDTO
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.giveOrDropItemStack
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.SimpleTMsItems
import dragomordor.simpletms.SimpleTMsItems.getTMorTRItemFromMove
import dragomordor.simpletms.util.fromLang
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack


object CobblemonEventListeners : EventHandler {

    override fun registerListeners() {
        CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.NORMAL, ::onBattleFainted)
        // EntityEvent.LIVING_DEATH.register(EntityEvent.LivingDeath { entity: LivingEntity, source: DamageSource -> onEntityFainted(entity, source) })
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

    // ------------------------------------------------------------
    // Helper Functions
    // ------------------------------------------------------------

    fun dropMoveLearnItemOnPlayer(killedPokemon: Pokemon, player: ServerPlayer, inBattle: Boolean) {
        // Get drop rates from config
//        val (dropRateTR, dropRateTMtoTRRatio) = if (inBattle) {
//            SimpleTMs.config.DropRateTRInBattle to SimpleTMs.config.DropRateTMtoTRRatioInBattle
//        }
////        else {
////            SimpleTMs.config.DropRateTROutsideOfBattle to SimpleTMs.config.DropRateTMtoTRRatioOutsideOfBattle
////        }

        val pokemonDisplayName = killedPokemon.species.translatedName
        val dropRateTR = SimpleTMs.config.DropRateTRInBattle
        val dropRateTMtoTRRatio = SimpleTMs.config.DropRateTMtoTRRatioInBattle
        // Get an applicable move template from the killed pokemon
//        val numberOfMovesToChooseFrom = when {
//            inBattle -> SimpleTMs.config.NumberOfMovesToChooseFromInBattle
//            else -> SimpleTMs.config.NumberOfMovesToChooseFromoutsideBattle
//        }
        val numberOfMovesToChooseFrom = SimpleTMs.config.NumberOfMovesToChooseFromInBattle

        val moveTemplatesToChooseFrom = getRandomApplicableMoveTemplates(killedPokemon, numberOfMovesToChooseFrom)
        val randomDouble = Math.random()
        val isTM = randomDouble < (dropRateTR * dropRateTMtoTRRatio)
        val isTR = randomDouble < dropRateTR

        if (isTM || isTR) {
            // Check if moveTemplatesToChooseFrom is empty. If it is, return
            if (moveTemplatesToChooseFrom.isEmpty()) {
                player.displayClientMessage(fromLang(SimpleTMs.MOD_ID, "error.no_moves_to_drop", pokemonDisplayName), true)
                return
            }

            val shouldUseMoveSelect = numberOfMovesToChooseFrom > 1
            if (shouldUseMoveSelect) {
                // Display Client message
                player.displayClientMessage(fromLang(SimpleTMs.MOD_ID, "message.select_move_to_drop", pokemonDisplayName), true)
                // if shouldUseMoveSelect, open MoveSelectCallback screen
                val movesToChooseFrom = moveTemplatesToChooseFrom.map { it.create() }
                MoveSelectCallbacks.create(
                    player = player,
                    // possibleMoves = movesToChooseFrom,
                    possibleMoves = movesToChooseFrom.map { battleMove -> MoveSelectDTO(battleMove) },
                    // title does not actually display anything afaik
                    // title = textToDisplay.text(),
                    //handler = { move -> dropMoveLearnItemOnPlayerHandler(player, move, isTR, isTM) }
                    handler = {_, index, _ -> dropMoveLearnItemOnPlayerHandler(player, movesToChooseFrom[index], isTR, isTM) }
                )
            } else {
                // Else drop a random move template
                dropMoveLearnItemOnPlayerHandler(player, moveTemplatesToChooseFrom.random().create(), isTR, isTM)
            }
        }

    }

    private fun dropMoveLearnItemOnPlayerHandler(player: ServerPlayer, move: Move, isTR: Boolean, isTM: Boolean) {
        val isTRItem = isTR && !isTM
        val droppedItem = getTMorTRItemFromMove(move, isTRItem)
        val droppedItemStack = ItemStack(droppedItem)
        player.giveOrDropItemStack(droppedItemStack, true)
    }



    private fun getRandomApplicableMoveTemplates(pokemon: Pokemon, numberOfMovesToChooseFrom: Int): List<MoveTemplate> {
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

        // If any move can be dropped, return a random move from all moves
        if (DropAny) {
            // Remove moves that do not have a TM or TR
            val applicableMoves = allMoves.filter { SimpleTMsItems.hasItemForMove(it) }
            // TODO: Exclude moves based on config options
            return applicableMoves.shuffled().take(numberOfMovesToChooseFrom)
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

        // Remove moves that do not have a TM or TR
        applicableMoves.removeIf { move -> !SimpleTMsItems.hasItemForMove(move) }

        // TODO: Exclude moves based on config options

        // Return numberOfMovesToChooseFrom random moves from applicableMoves
        return applicableMoves.shuffled().take(numberOfMovesToChooseFrom)
    }

}