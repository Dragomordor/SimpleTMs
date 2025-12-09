package dragomordor.simpletms.block.api

import com.cobblemon.mod.common.api.callback.PartySelectCallbacks
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.party
import net.minecraft.server.level.ServerPlayer

/**
 * Interface for blocks that can open a party selection GUI.
 * 
 * Similar to PokemonSelectingItemNonBattle but for block interactions.
 * Used by TM Machine to allow filtering by a selected Pokémon.
 */
interface PokemonSelectingBlock {

    /**
     * Open the party selection GUI for the player.
     * When a Pokémon is selected, onPokemonSelected will be called.
     * 
     * @param player The server player opening the selection
     * @param onPokemonSelected Callback when a Pokémon is selected
     */
    fun openPartySelection(player: ServerPlayer, onPokemonSelected: (Pokemon) -> Unit) {
        val party = player.party().toList()
        if (party.isEmpty()) {
            return
        }

        PartySelectCallbacks.createFromPokemon(
            player = player,
            pokemon = party,
            canSelect = { canSelectPokemon(it) },
            handler = { pokemon ->
                onPokemonSelected(pokemon)
            }
        )
    }

    /**
     * Determine if a Pokémon can be selected for filtering.
     * Override this to restrict which Pokémon can be selected.
     * 
     * @param pokemon The Pokémon to check
     * @return true if the Pokémon can be selected
     */
    fun canSelectPokemon(pokemon: Pokemon): Boolean = true
}
