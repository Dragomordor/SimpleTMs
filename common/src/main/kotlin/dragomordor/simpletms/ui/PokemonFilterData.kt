package dragomordor.simpletms.ui

/**
 * Data class to hold Pokémon filter information for the Move Case and TM Machine UIs.
 *
 * This is used to filter which moves are displayed based on what a specific Pokémon can learn.
 * The data is calculated server-side and synced to the client.
 *
 * @param speciesId The resource identifier of the Pokémon species (e.g., "cobblemon:pikachu")
 * @param formName The name of the Pokémon's form (e.g., "alolan", or empty for default)
 * @param displayName The localized display name of the Pokémon
 * @param learnableMoves Set of move names that this Pokémon can learn
 */
data class PokemonFilterData(
    val speciesId: String,
    val formName: String,
    val displayName: String,
    val learnableMoves: Set<String>
)