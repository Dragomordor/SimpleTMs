package dragomordor.simpletms.item.custom

import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.giveOrDropItemStack
import dragomordor.simpletms.SimpleTMsItems
import dragomordor.simpletms.item.SimpleTMsItem
import dragomordor.simpletms.item.api.PokemonAndMoveSelectingItemNonBattle
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class BlankTmItem(val singleUse: Boolean) : SimpleTMsItem(Properties().stacksTo(16)), PokemonAndMoveSelectingItemNonBattle {

    // ------------------------------------------------------------------
    // Override methods from PokemonAndMoveSelectingItemNonBattle
    // ------------------------------------------------------------------

    // Blank TMs can only be used on Pokémon moves, not on the Pokémon itself
    override fun canUseOnPokemon(pokemon: Pokemon) = pokemon.moveSet.any(::canUseOnMove)

    // Blank TMs can be used on any move the pokemon knows
    override fun canUseOnMove(move: Move) = true;

    override fun applyToPokemon(player: ServerPlayer, stack: ItemStack, pokemon: Pokemon, move: Move) {

        // (0) Get singleUse from class, not interface (this)
        val singleUse = this.singleUse

        // (1) Get tm/tr for this move
        val newMoveLearnItem = getTMorTRItemFromMove(move, singleUse)
        // (2) Give the item to the player
        player.giveOrDropItemStack(ItemStack(newMoveLearnItem))

        // Success sound and shrink the stack
        player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1.0F, 1.0F)

        // Shrink the stack if player is not in creative mode
        if (!player.isCreative) {
            // If the item is single use, shrink the stack
            if (singleUse) {
                stack.shrink(1)}
        }

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


    // ------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------

    fun getTMorTRItemFromMove(move: Move, singleUse: Boolean): Item {
        // Get prefix for TM or TR
        val prefix = if (singleUse) "tr_" else "tm_"
        val moveName = move.name
        val newMoveLearnItem = SimpleTMsItems.getItemFromName(prefix + moveName)
        return newMoveLearnItem
    }


}