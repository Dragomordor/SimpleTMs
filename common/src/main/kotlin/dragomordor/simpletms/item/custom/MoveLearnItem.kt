package dragomordor.simpletms.item.custom

import com.cobblemon.mod.common.api.moves.*
import com.cobblemon.mod.common.pokemon.Pokemon
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.item.SimpleTMsItem
import dragomordor.simpletms.item.api.PokemonSelectingItemNonBattle
import dragomordor.simpletms.util.FailureMessage
import dragomordor.simpletms.util.fromLang
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.EquipmentSlot


class MoveLearnItem(
    val moveName: String,
    val moveType: String,
    val isTR: Boolean,
    val settings: Properties
) : SimpleTMsItem(settings), PokemonSelectingItemNonBattle {

    // ------------------------------------------------------------------
    // Override methods from PokemonSelectingItemNonBattle
    // ------------------------------------------------------------------

    // TMs/TRs can be used on the Pokémon itself under certain conditions
    override fun canUseOnPokemon(pokemon: Pokemon) = canPokemonLearnMove(pokemon)
    // override fun canUseOnPokemon(pokemon: Pokemon) = true

    // Once the TM can be used on the Pokémon, apply the move to the Pokémon
    override fun applyToPokemon(
        player: ServerPlayer,
        stack: ItemStack,
        pokemon: Pokemon) : InteractionResultHolder<ItemStack> {

        // (0) Get the moveName from class, not interface (this)
        val moveName = this.moveName
        val isTR = this.isTR

        // Get the move to teach
        val moveToTeach = Moves.getByName(moveName)

        // (1) Check if the move is a valid move
        if (moveToTeach == null) {
            // Move is not valid, so do nothing
            // Display the failure message
            player.displayClientMessage(FailureMessage.getFailureMessage(), true)
            return InteractionResultHolder.fail(stack)
        }

        val moveTranslatedName = moveToTeach.displayName

        // (2+3) Check if the Pokémon can learn the move
        if (!canPokemonLearnMove(pokemon, moveToTeach)) {
            // Pokémon can't learn the move, so do nothing
            // Display the failure message
            player.displayClientMessage(FailureMessage.getFailureMessage(), true)
            return InteractionResultHolder.fail(stack)
        } else {

            // (4) Check item cooldown if applicable
            val cooldownTicks = SimpleTMs.config.TMCoolDownTicks

            if (player.cooldowns.isOnCooldown(this)) {
                // Convert cooldown ticks to hours, minutes, and seconds
                val cooldownHH = Math.floorDiv(cooldownTicks, 72000)
                val cooldownMM = Math.floorDiv(cooldownTicks- (cooldownHH * 72000), 1200)
                val cooldownSS = Math.floorDiv(cooldownTicks - (cooldownHH * 72000) - (cooldownMM * 1200), 20)
                val cooldownString = "$cooldownHH:$cooldownMM:$cooldownSS"
                // Display the failure message
                FailureMessage.setFailureMessage(fromLang(SimpleTMs.MOD_ID,"error.not_learnable.on_cooldown", stack.displayName ,cooldownString))
                player.displayClientMessage(FailureMessage.getFailureMessage(), true)
                return InteractionResultHolder.fail(stack)
            }

            // (5) If the Pokémon can learn the move, teach it the move
            // Success message
            val successMessage = fromLang(SimpleTMs.MOD_ID,"success.learned", pokemon.species.translatedName, moveTranslatedName)
            player.displayClientMessage(
                successMessage, true
            )

            // Put move into the move set if there is an empty slot
            val currentMoves = pokemon.moveSet
            val benchedMoves = pokemon.benchedMoves
            if (currentMoves.hasSpace()) {
                currentMoves.add(moveToTeach.create())
            } else {
                // Put move into benched moves if there is no empty slot
                benchedMoves.add(BenchedMove(moveToTeach, 0))
            }

            // Success sound and stack shrink
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F)

            // Damage the stack if player is not in creative mode
            if (!player.isCreative) {
                // If the item is single use, shrink the stack
                if (isTR) {
                    stack.shrink(1)
                }
            }

            // Put item on cooldown if applicable
            if (!player.isCreative && !isTR && stack.count > 0 && cooldownTicks > 0) {
                player.cooldowns.addCooldown(this, cooldownTicks)
            }

            return InteractionResultHolder.success(stack)
        }
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (world is ServerLevel && user is ServerPlayer) {
            return use(user, user.getItemInHand(hand)) ?: InteractionResultHolder.pass(user.getItemInHand(hand))

        }
        return super<SimpleTMsItem>.use(world, user, hand)
    }

    // ------------------------------------------------------------------
    // Custom Functions
    // ------------------------------------------------------------------

    private fun canPokemonLearnMove(pokemon: Pokemon, moveToTeach: MoveTemplate?): Boolean {

        // (2.1) Get current move set and benched moves of the pokemon
        val currentMoves = pokemon.moveSet
        val benchedMoves = pokemon.benchedMoves
        val pokemonTranslateName = pokemon.species.translatedName


        // (2.2) TODO: Get cooldown of TM from config and fail if item is on cooldown

        // (2.3) Check if the moveToTeach is a valid move
        if (moveToTeach == null) {
            FailureMessage.setFailureMessage(fromLang(SimpleTMs.MOD_ID,"error.not_learnable.not_valid_move"))
            return false
        }
        val moveTranslatedName = moveToTeach.displayName


        // (2.4) Check if the move is already in the move set
        if (currentMoves.getMoves().stream().anyMatch { it.name == moveToTeach.name }) {
            FailureMessage.setFailureMessage(fromLang(SimpleTMs.MOD_ID,"error.not_learnable.already_knows_move", pokemonTranslateName, moveTranslatedName))
            return false
        }
        // (2.5) Check if the move is already in the benched moves

            // Check if the move is in the benched moves
        if (benchedMoveContainsMove(benchedMoves, moveToTeach)) {
            FailureMessage.setFailureMessage(fromLang(SimpleTMs.MOD_ID,"error.not_learnable.already_knows_move", pokemonTranslateName, moveTranslatedName))
            return false
        }
        // (2.6) Check if the move is a valid move for the Pokémon
        if (!inPokemonMoveList(pokemon, moveToTeach)) {
            FailureMessage.setFailureMessage(fromLang(SimpleTMs.MOD_ID,"error.not_learnable.not_in_learnable_moves", pokemonTranslateName, moveTranslatedName))
            return false
        }
        // (3) If all checks pass, return true
        return true
    }

    // Wrapper for canPokemonLearnMove with only pokemon as input
    fun canPokemonLearnMove(pokemon: Pokemon): Boolean {
        val isTR = this.isTR

        // First check if config allows TMs and TRs
        if (!SimpleTMs.config.TMsUsable && !isTR) {
            FailureMessage.setFailureMessage(fromLang(SimpleTMs.MOD_ID,"error.not_usable.tms_disabled"))
            return false
        }
        if (!SimpleTMs.config.TRsUsable && isTR) {
            FailureMessage.setFailureMessage(fromLang(SimpleTMs.MOD_ID,"error.not_usable.trs_disabled"))
            return false
        }

        // TODO: add excluded moves from config here

        val moveName = this.moveName
        val move = Moves.getByNameOrDummy(moveName)
        return canPokemonLearnMove(pokemon, move)
    }


    // Function to check if pokemon is capable of learning this specific move
    fun inPokemonMoveList(pokemon: Pokemon, move: MoveTemplate): Boolean {

        if (SimpleTMs.config.anyMovesLearnable) {
            return true
        }
        // (2) Check if the move is in the pokemons tm move list
        if (SimpleTMs.config.TMMovesLearnable && pokemon.form.moves.tmMoves.contains(move)) {
            return true
        }
        // (3) Check if the move is in the pokemons tutor move list
        if (SimpleTMs.config.tutorMovesLearnable && pokemon.form.moves.tutorMoves.contains(move)) {
            return true
        }
        // (4) Check if the move is in the pokemons egg move list
            // TODO: Check if egg move is in pokemons egg move list or in its pre-evolutions egg move list
        if (SimpleTMs.config.eggMovesLearnable) {
            // Current pokemon has the move as an egg move
            if (pokemon.form.moves.eggMoves.contains(move)) {
                return true
            }
            var preEvolution = pokemon.preEvolution
            if (preEvolution == null) {
                return false
            }
            while (preEvolution != null) {
                if (preEvolution.form.moves.eggMoves.contains(move)) {
                    return true
                }
                preEvolution = preEvolution.form.preEvolution
            }
            // if pre-evolution is null, but the move is not in the egg moves, do not learn the move
            return false
        }
        // (5) Check if the move is in the pokemons level up move list
        if (SimpleTMs.config.levelMovesLearnable) {
            // Get all level moves of the pokemon
            val levelMoves = pokemon.form.moves.levelUpMoves //  MutableMap<Int, MutableList<MoveTemplate>>
            // Check if the move is in the level moves
            for (level in levelMoves.keys) {
                if (levelMoves[level]!!.contains(move)) {
                    return true
                }
            }
        }

        // By default, return false
        return false
    }

    // Function to check if a move is in the benched moves
    fun benchedMoveContainsMove(benchedMoves: BenchedMoves, move: MoveTemplate): Boolean {
        for (benchedMove in benchedMoves) {
            if (benchedMove.moveTemplate == move) {
                return true
            }
        }
        return false
    }
}



