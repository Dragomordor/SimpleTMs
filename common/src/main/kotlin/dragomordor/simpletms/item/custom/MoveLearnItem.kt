package dragomordor.simpletms.item.custom

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.giveOrDropItemStack
import dragomordor.simpletms.item.SimpleTMsItem
import dragomordor.simpletms.item.api.PokemonSelectingItemNonBattle
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

class MoveLearnItem(
    val moveName: String,
    val moveType: String,
    val singleUse: Boolean,
) : SimpleTMsItem(Properties().stacksTo(16)), PokemonSelectingItemNonBattle {

    val moveToTeach = Moves.getByName(moveName)

    // TMs/TRs can be used on the Pokémon itself under certain conditions
    override fun canUseOnPokemon(pokemon: Pokemon) = true

    // ------------------------------------------------------------------
    // Overriden methods from PokemonAndMoveSelectingItemNonBattle
    // ------------------------------------------------------------------


    // Once the TM can be used on the Pokémon, apply the move to the Pokémon
    override fun applyToPokemon(
        player: ServerPlayer,
        stack: ItemStack,
        pokemon: Pokemon) : InteractionResultHolder<ItemStack> {

        // Check if the Pokémon can learn the move
        if (!canPokemonLearnMove(pokemon, moveToTeach)) {
            // Pokémon can't learn the move, so do nothing
            // Display the failure message
            player.displayClientMessage(FailureMessage.getFailureMessage().text(), true)
            return InteractionResultHolder.fail(stack)
        } else {
            // If the Pokémon can learn the move, teach it the move

            // Teach the move on the TM/TR to the Pokémon adn put it either in a empty slot in the move set or into benched moves if the move set is full
            // TODO: Implement this
            // For now, give the player a restone for testing
            player.giveOrDropItemStack(ItemStack(Items.REDSTONE))

            // Success sound and stack shrink
            player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F)
            // Shrink the stack if player is not in creative mode
            if (!player.isCreative) {
                // If the item is single use, shrink the stack
                if (singleUse) {
                    stack.shrink(1)
                }
            }
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

    private fun canPokemonLearnMove(pokemon: Pokemon, move: MoveTemplate?): Boolean {

        // If pokemon can't learn move because of on the checks, the failure message is set using this setFailureMessage() method
        // Like so --> FailureMessage.setFailureMessage("")
        // Use lang file to get the failure message -- eg. "error.not_learnable.not_in_move_set"


        // TODO: Implement this
        return true
    }

    class FailureMessage() {
        companion object {
            lateinit var message: String

            fun getFailureMessage(): String {
                return message
            }
            fun setFailureMessage(message: String) {
                this.message = message
            }
        }
    }

}
