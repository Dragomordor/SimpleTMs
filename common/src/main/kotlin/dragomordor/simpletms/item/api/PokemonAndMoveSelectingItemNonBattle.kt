package dragomordor.simpletms.item.api


import com.cobblemon.mod.common.api.callback.MoveSelectCallbacks
import com.cobblemon.mod.common.api.callback.PartyMoveSelectCallbacks
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.getBattleState
import com.cobblemon.mod.common.util.isHeld
import com.cobblemon.mod.common.util.isLookingAt
import com.cobblemon.mod.common.util.party
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.AABB



interface PokemonAndMoveSelectingItemNonBattle {

    fun use(player: ServerPlayer, stack: ItemStack): InteractionResultHolder<ItemStack>? {
        val entity = player.level()
            .getEntities(player, AABB.ofSize(player.position(), 16.0, 16.0, 16.0))
            .filter { player.isLookingAt(it, stepDistance = 0.1F) }
            .minByOrNull { it.distanceTo(player) } as? PokemonEntity?

        player.getBattleState()?.let { (_, actor) ->
            // If the player is in a battle, fail the interaction
            return InteractionResultHolder.fail(stack)
        } ?: run {
            // Ensure the entity is not null
            if (entity?.equals(null) == false) {
                val pokemon = entity.pokemon
                // If the player is the owner of the Pokémon, interact with it, otherwise fail the interaction
                if (entity.ownerUUID == player.uuid) {
                    return interactWithSpecific(player, stack, pokemon)
                } else {
                    return InteractionResultHolder.fail(stack)
                }
            } else {
                // If the player is not looking at a Pokémon, interact with the player's party
                    return interactGeneral(player, stack)
                }
            }
        }

    // Direct from original code https://gitlab.com/cable-mc/cobblemon/-/blob/main/common/src/main/kotlin/com/cobblemon/mod/common/api/item/PokemonAndMoveSelectingItem.kt
    fun applyToPokemon(player: ServerPlayer, stack: ItemStack, pokemon: Pokemon, move: Move)
    fun canUseOnPokemon(pokemon: Pokemon): Boolean
    fun canUseOnMove(pokemon: Pokemon, move: Move): Boolean = canUseOnMove(move)
    fun canUseOnMove(move: Move): Boolean

    fun interactGeneral(player: ServerPlayer, stack: ItemStack): InteractionResultHolder<ItemStack>? {
        PartyMoveSelectCallbacks.createFromPokemon(
            player = player,
            pokemon = player.party().toList(),
            canSelectPokemon = ::canUseOnPokemon,
            canSelectMove = ::canUseOnMove,
            handler = { pk, mv -> if (stack.isHeld(player)) applyToPokemon(player, stack, pk, mv) }
        )

        return InteractionResultHolder.success(stack)
    }

    fun interactWithSpecific(player: ServerPlayer, stack: ItemStack, pokemon: Pokemon): InteractionResultHolder<ItemStack>? {

        if (player.isShiftKeyDown) {
            return InteractionResultHolder.pass(stack)
        }

        MoveSelectCallbacks.create(
            player = player,
            moves = pokemon.moveSet.toList(),
            canSelect = ::canUseOnMove,
            handler = { move -> if (stack.isHeld(player)) applyToPokemon(player, stack, pokemon, move) }
        )
        return InteractionResultHolder.success(stack)
    }




}


