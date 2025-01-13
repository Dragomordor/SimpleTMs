package dragomordor.simpletms.item.custom

import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.giveOrDropItemStack
import dragomordor.simpletms.item.SimpleTMsItem
import dragomordor.simpletms.item.api.PokemonAndMoveSelectingItemNonBattle
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

class BlankTmItem(singleUse: Boolean) : SimpleTMsItem(Properties().stacksTo(16)), PokemonAndMoveSelectingItemNonBattle {

    val singleUse = singleUse

    // Blank TMs can only be used on Pokémon moves, not on the Pokémon itself
    override fun canUseOnPokemon(pokemon: Pokemon) = pokemon.moveSet.any(::canUseOnMove)
    // Blank TMs can be used on any move the pokemon knows
    override fun canUseOnMove(move: Move) = true;

    override fun applyToPokemon(player: ServerPlayer, stack: ItemStack, pokemon: Pokemon, move: Move) {
        val moveToLearn = pokemon.moveSet.find { it.template == move.template }
        player.playSound(SoundEvents.ANVIL_LAND, 1.0F, 1.0F)
        // Shrink the stack if player is not in creative mode
        if (!player.isCreative) {
            // If the item is single use, shrink the stack
            if (singleUse) {
                stack.shrink(1)}
        }
        // Return a new tm item with the move to learn
            // TODO: Implement this
            // For now, give the player a golden carrot for testing
        player.giveOrDropItemStack(ItemStack(Items.GOLDEN_CARROT))
    }

    override fun use(
        world: Level,
        user: Player,
        hand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        if (world is ServerLevel && user is ServerPlayer) {
            return use(user, user.getItemInHand(hand)) ?: InteractionResultHolder.pass(user.getItemInHand(hand))
        }
        return super<SimpleTMsItem>.use(world, user, hand)
    }

}