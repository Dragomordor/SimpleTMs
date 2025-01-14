package dragomordor.simpletms.item.api

import com.cobblemon.mod.common.advancement.CobblemonCriteria
import com.cobblemon.mod.common.advancement.criterion.PokemonInteractContext
import com.cobblemon.mod.common.api.callback.PartySelectCallbacks
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.*
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.AABB


interface PokemonSelectingItemNonBattle {

    fun use(player: ServerPlayer, stack: ItemStack): InteractionResultHolder<ItemStack>? {
        val entity = player.level()
            .getEntities(player, AABB.ofSize(player.position(), 16.0, 16.0, 16.0))
            .filter { player.isLookingAt(it, stepDistance = 0.1F) }
            .minByOrNull { it.distanceTo(player) } as? PokemonEntity?

        player.getBattleState()?.let { (_, _) ->
            // If the player is in a battle, fail the interaction
            return InteractionResultHolder.fail(stack)
        } ?: run {
            if (!player.isShiftKeyDown) {
                return if (entity?.equals(null) == false) {
                    val pokemon = entity.pokemon
                    if (entity.ownerUUID == player.uuid) {
                        val typedInteractionResult = applyToPokemon(player, stack, pokemon)
                        if (typedInteractionResult != null) {
                            CobblemonCriteria.POKEMON_INTERACT.trigger(
                                player,
                                PokemonInteractContext(
                                    pokemon.species.resourceIdentifier,
                                    BuiltInRegistries.ITEM.getKey(stack.item)
                                )
                            )
                            typedInteractionResult
                        } else {
                            InteractionResultHolder.pass(stack)
                        }
                    } else {
                        InteractionResultHolder.fail(stack)
                    }
                } else {
                    interactGeneral(player, stack)
                }
            }
        }
        return InteractionResultHolder.pass(stack)
    }

    // Direct from original code https://gitlab.com/cable-mc/cobblemon/-/blob/main/common/src/main/kotlin/com/cobblemon/mod/common/api/item/PokemonAndMoveSelectingItem.kt
    fun applyToPokemon(player: ServerPlayer, stack: ItemStack, pokemon: Pokemon): InteractionResultHolder<ItemStack>?
    fun canUseOnPokemon(pokemon: Pokemon): Boolean

    fun interactGeneral(player: ServerPlayer, stack: ItemStack): InteractionResultHolder<ItemStack> {
        val party = player.party().toList()
        if (party.isEmpty()) {
            return InteractionResultHolder.fail(stack)
        }

        PartySelectCallbacks.createFromPokemon(
            player = player,
            pokemon = party,
            canSelect = ::canUseOnPokemon,
            handler = { pk ->
                if (stack.isHeld(player)) {
                    applyToPokemon(player, stack, pk)
                    CobblemonCriteria.POKEMON_INTERACT.trigger(player, PokemonInteractContext(pk.species.resourceIdentifier, BuiltInRegistries.ITEM.getKey(stack.item)))
                }
            }
        )

        return InteractionResultHolder.success(stack)
    }

}


