package dragomordor.simpletms.item.custom

import com.cobblemon.mod.common.api.moves.*
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.lang
import dragomordor.simpletms.item.SimpleTMsItem
import dragomordor.simpletms.item.api.PokemonSelectingItemNonBattle
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.sounds.SoundSource


class MoveLearnItem(
    val moveName: String,
    val moveType: String,
    val singleUse: Boolean,
) : SimpleTMsItem(Properties().stacksTo(16)), PokemonSelectingItemNonBattle {

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
        val singleUse = this.singleUse

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

        // (2) Check if the Pokémon can learn the move
        if (!canPokemonLearnMove(pokemon, moveToTeach)) {
            // Pokémon can't learn the move, so do nothing
            // Display the failure message
            player.displayClientMessage(FailureMessage.getFailureMessage(), true)
            return InteractionResultHolder.fail(stack)
        } else {
            // (4) If the Pokémon can learn the move, teach it the move
            val currentMoves = pokemon.moveSet
            val benchedMoves = pokemon.benchedMoves

                // Put move into the move set if there is an empty slot
            if (currentMoves.hasSpace()) {
                currentMoves.add(moveToTeach.create())
            } else {
                // Put move into benched moves if there is no empty slot
                benchedMoves.add(BenchedMove(moveToTeach, 0))
            }

            // Success message
            val successMessage = lang("success.learned", pokemon.species.translatedName, moveTranslatedName)

            player.displayClientMessage(
                successMessage, true
            )

            // Success sound and stack shrink
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F)

            // Shrink the stack if player is not in creative mode
            if (!player.isCreative) {
                // If the item is single use, shrink the stack
                if (singleUse) {
                    stack.shrink(1)
                }
            }

            // TODO: From config -- add cooldown to the item

            // Success
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
            FailureMessage.setFailureMessage(lang("error.not_learnable.not_valid_move"))
            return false
        }
        val moveTranslatedName = moveToTeach.displayName


        // (2.4) Check if the move is already in the move set
        if (currentMoves.getMoves().stream().anyMatch { it.name == moveToTeach.name }) {
            FailureMessage.setFailureMessage(lang("error.not_learnable.already_knows_move", pokemonTranslateName, moveTranslatedName))
            return false
        }
        // (2.5) Check if the move is already in the benched moves

            // Check if the move is in the benched moves
        if (benchedMoveContainsMove(benchedMoves, moveToTeach)) {
            FailureMessage.setFailureMessage(lang("error.not_learnable.already_knows_move", pokemonTranslateName, moveTranslatedName))
            return false
        }
        // (2.6) Check if the move is a valid move for the Pokémon
        if (!inPokemonMoveList(pokemon, moveToTeach)) {
            FailureMessage.setFailureMessage(lang("error.not_learnable.not_in_learnable_moves", pokemonTranslateName, moveTranslatedName))
            return false
        }
        // (3) If all checks pass, return true
        return true
    }

    // Wrapper for canPokemonLearnMove with only pokemon as input
    fun canPokemonLearnMove(pokemon: Pokemon): Boolean {
        val moveName = this.moveName
        val move = Moves.getByNameOrDummy(moveName)
        return canPokemonLearnMove(pokemon, move)
    }


    class FailureMessage() {
        companion object {
            lateinit var message: Component

            fun getFailureMessage(): Component {
                return message
            }
            fun setFailureMessage(message: Component) {
                this.message = message
            }
        }
    }

    // Function to check if pokemon is capable of learning this specific move
    fun inPokemonMoveList(pokemon: Pokemon, move: MoveTemplate): Boolean {

        // (1) TODO: From config -- check if any pokemon can learn any move

        // (2) Check if the move is in the pokemons tm move list
        if (pokemon.form.moves.tmMoves.contains(move)) {
            return true
        }
        // (3) Check if the move is in the pokemons tutor move list
            // TODO: From config -- check if TMs can be used for tutor moves
        if (pokemon.form.moves.tutorMoves.contains(move)) {
            return true
        }
        // (4) Check if the move is in the pokemons egg move list
            // TODO: From config -- check if TMs can be used for egg moves
        if (pokemon.form.moves.eggMoves.contains(move)) {
            return true
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
