package dragomordor.simpletms.util

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.pokemon.moves.Learnset
import com.cobblemon.mod.common.api.pokemon.moves.LearnsetQuery
import com.cobblemon.mod.common.pokemon.Pokemon
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.SimpleTMsItems

/**
 * Utility object for determining which moves a Pokémon can learn via TMs and TRs.
 * 
 * This consolidates the learnable move logic used across the mod,
 * respecting all config options for move learning.
 */
object MoveHelper {

    /**
     * Get all move names that this Pokémon can learn via TM.
     * Respects config settings for which move categories are learnable.
     * 
     * @param pokemon The Pokémon to check
     * @return Set of move names (lowercase) that can be learned via TM
     */
    fun getLearnableTMMoves(pokemon: Pokemon): Set<String> {
        return getLearnableMoves(pokemon, isTR = false)
    }

    /**
     * Get all move names that this Pokémon can learn via TR.
     * Respects config settings for which move categories are learnable.
     * 
     * @param pokemon The Pokémon to check
     * @return Set of move names (lowercase) that can be learned via TR
     */
    fun getLearnableTRMoves(pokemon: Pokemon): Set<String> {
        return getLearnableMoves(pokemon, isTR = true)
    }

    /**
     * Get all move names that this Pokémon can learn via TM or TR.
     * This is a union of both TM and TR learnable moves.
     * 
     * @param pokemon The Pokémon to check
     * @return Set of move names (lowercase) that can be learned
     */
    fun getAllLearnableMoves(pokemon: Pokemon): Set<String> {
        val moves = mutableSetOf<String>()
        moves.addAll(getLearnableTMMoves(pokemon))
        moves.addAll(getLearnableTRMoves(pokemon))
        return moves
    }

    /**
     * Check if a Pokémon can learn a specific move via TM.
     * 
     * @param pokemon The Pokémon to check
     * @param moveName The move name to check
     * @return true if the Pokémon can learn this move via TM
     */
    fun canLearnTM(pokemon: Pokemon, moveName: String): Boolean {
        return getLearnableTMMoves(pokemon).contains(moveName.lowercase())
    }

    /**
     * Check if a Pokémon can learn a specific move via TR.
     * 
     * @param pokemon The Pokémon to check
     * @param moveName The move name to check
     * @return true if the Pokémon can learn this move via TR
     */
    fun canLearnTR(pokemon: Pokemon, moveName: String): Boolean {
        return getLearnableTRMoves(pokemon).contains(moveName.lowercase())
    }

    /**
     * Check if a Pokémon can learn a specific move via either TM or TR.
     * 
     * @param pokemon The Pokémon to check
     * @param moveName The move name to check
     * @return true if the Pokémon can learn this move
     */
    fun canLearnMove(pokemon: Pokemon, moveName: String): Boolean {
        return canLearnTM(pokemon, moveName) || canLearnTR(pokemon, moveName)
    }

    /**
     * Internal method to get learnable moves based on TM/TR type.
     */
    private fun getLearnableMoves(pokemon: Pokemon, isTR: Boolean): Set<String> {
        val cfg = SimpleTMs.config
        val learnableMoves = mutableSetOf<String>()

        // Check if TMs/TRs are usable at all
        if (!isTR && !cfg.tmsUsable) return emptySet()
        if (isTR && !cfg.trsUsable) return emptySet()

        // Get the valid moves list from SimpleTMsItems
        val validMoves = if (isTR) {
            SimpleTMsItems.ALL_MOVE_NAMES_WITH_TR_ITEMS
        } else {
            SimpleTMsItems.ALL_MOVE_NAMES_WITH_TM_ITEMS
        }

        // If any move is learnable, return all valid moves
        if ((!isTR && cfg.anyMovesLearnableTMs) || (isTR && cfg.anyMovesLearnableTRs)) {
            return validMoves.map { it.lowercase() }.toSet()
        }

        // Get excluded moves
        val excludedMoves = SimpleTMsItems.ALL_MOVES_EXCLUDED_FROM_TMTR_LEARNING

        // Check each valid move
        for (moveName in validMoves) {
            val moveTemplate = Moves.getByName(moveName) ?: continue
            
            // Skip excluded moves
            if (excludedMoves.contains(moveTemplate.name)) continue

            // Check if Pokémon can learn this move
            if (canPokemonLearnMoveTemplate(pokemon, moveTemplate, cfg)) {
                learnableMoves.add(moveName.lowercase())
            }
        }

        return learnableMoves
    }

    /**
     * Check if a Pokémon can learn a specific move template based on config settings.
     * This mirrors the logic in MoveLearnItem.canPokemonLearnMove.
     */
    private fun canPokemonLearnMoveTemplate(
        pokemon: Pokemon,
        moveTemplate: MoveTemplate,
        cfg: dragomordor.simpletms.config.SimpleTMsConfig
    ): Boolean {
        // Check current form first
        if (canLearnFromLearnset(moveTemplate, pokemon.form.moves, cfg)) {
            return true
        }

        // Check all other forms of the same species
        for (form in pokemon.species.forms) {
            if (form == pokemon.form) continue
            if (canLearnFromLearnset(moveTemplate, form.moves, cfg)) {
                return true
            }
        }

        // Type-based fallback learning
        if (cfg.primaryTypeMovesLearnable || cfg.secondaryTypeMovesLearnable) {
            val primary = pokemon.primaryType
            val secondary = pokemon.secondaryType

            if (cfg.primaryTypeMovesLearnable && moveTemplate.elementalType == primary) {
                return true
            }

            if (cfg.secondaryTypeMovesLearnable && secondary != null && moveTemplate.elementalType == secondary) {
                return true
            }
        }

        return false
    }

    /**
     * Check if a move can be learned from a specific learnset based on config settings.
     */
    private fun canLearnFromLearnset(
        moveTemplate: MoveTemplate,
        learnset: Learnset,
        cfg: dragomordor.simpletms.config.SimpleTMsConfig
    ): Boolean {
        // First check if the move is in the learnset at all
        if (!LearnsetQuery.ANY.canLearn(moveTemplate, learnset)) {
            return false
        }

        // Now filter by config toggles - move is teachable if ANY enabled category matches

        // TM moves
        if (cfg.tmMovesLearnable && LearnsetQuery.TM_MOVE.canLearn(moveTemplate, learnset)) {
            return true
        }

        // Tutor moves
        if (cfg.tutorMovesLearnable && LearnsetQuery.TUTOR_MOVES.canLearn(moveTemplate, learnset)) {
            return true
        }

        // Egg moves
        if (cfg.eggMovesLearnable && LearnsetQuery.EGG_MOVE.canLearn(moveTemplate, learnset)) {
            return true
        }

        // Level-up moves
        if (cfg.levelMovesLearnable && LearnsetQuery.ANY_LEVEL.canLearn(moveTemplate, learnset)) {
            return true
        }

        // Legacy moves
        if (cfg.legacyMovesLearnable && LearnsetQuery.LEGACY_MOVES.canLearn(moveTemplate, learnset)) {
            return true
        }

        // Special moves
        if (cfg.specialMovesLearnable && LearnsetQuery.SPECIAL_MOVES.canLearn(moveTemplate, learnset)) {
            return true
        }

        return false
    }
}
