package dragomordor.simpletms.item.custom

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.moves.*
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.evolution.PreEvolution
import com.cobblemon.mod.common.api.pokemon.moves.Learnset
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.asTranslated
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.item.SimpleTMsItem
import dragomordor.simpletms.item.api.PokemonSelectingItemNonBattle
import dragomordor.simpletms.util.FailureMessage
import dragomordor.simpletms.util.fromLang
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.component.DataComponentMap
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
import net.minecraft.util.ColorRGBA
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.TooltipFlag
import org.apache.commons.codec.binary.Hex
import java.awt.Color


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

        val TMusuable = SimpleTMs.config.TMsUsable
        val TRusuable = SimpleTMs.config.TRsUsable

        // (2) Check if the TM/TR is usable
        if ((!TMusuable && !isTR) || (!TRusuable && isTR)) {
            val errorKey = if (isTR) "error.not_usable.trs_disabled" else "error.not_usable.tms_disabled"
            FailureMessage.setFailureMessage(fromLang(SimpleTMs.MOD_ID, errorKey))
            player.displayClientMessage(FailureMessage.getFailureMessage(), true)
            return InteractionResultHolder.fail(stack)
        }

        // (3+4) Check if the Pokémon can learn the move
        if (!canPokemonLearnMove(pokemon, moveToTeach)) {
            // Pokémon can't learn the move, so do nothing
            // Display the failure message
            player.displayClientMessage(FailureMessage.getFailureMessage(), true)
            return InteractionResultHolder.fail(stack)
        } else {

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
                } else {
                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND)
                }
            }

            // Put item on cooldown if applicable
            val cooldownTicks = SimpleTMs.config.TMCoolDownTicks
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

    // TODO: Replacer funciton for all canPokemonLearnMove functions
    private fun canPokemonLearnMove(pokemon: Pokemon, moveToTeach: MoveTemplate): Boolean {

        // (3.1) Get current move set and benched moves of the pokemon
        val currentMoves = pokemon.moveSet
        val benchedMoves = pokemon.benchedMoves
        val pokemonTranslateName = pokemon.species.translatedName
        val moveTranslatedName = moveToTeach.displayName

        val TMMovesLearnable = SimpleTMs.config.TMMovesLearnable
        val eggMovesLearnable = SimpleTMs.config.eggMovesLearnable
        val tutorMovesLearnable = SimpleTMs.config.tutorMovesLearnable
        val levelMovesLearnable = SimpleTMs.config.levelMovesLearnable
        val anyMoveLearnable = SimpleTMs.config.anyMovesLearnable

        // (3.2) Check if the move is already in the move set or benched moves
        if ((currentMoves.getMoves().stream().anyMatch { it.name == moveToTeach.name }) ||
            // (benchedMoveContainsMove(benchedMoves, moveToTeach)))
            benchedMoves.any { it.moveTemplate == moveToTeach } ) {
            FailureMessage.setFailureMessage(fromLang(SimpleTMs.MOD_ID,"error.not_learnable.already_knows_move", pokemonTranslateName, moveTranslatedName))
            return false
        }

        // (3.3) Check if any move is learnable
        if (anyMoveLearnable) {
            return true
        }

        // (3.3) Add all list of learnable moves to a list and check if the move is in the list, otherwise return false
        println("Checking if move is in learnable moves")
        val learnableMoves = mutableListOf<MoveTemplate>()
        println("Learnable moves list: $learnableMoves")

        // TM Moves
        if (TMMovesLearnable) {
            println("Adding TM moves")
            learnableMoves.addAll(pokemon.form.moves.tmMoves)
            println("Learnable moves list after tm moves: $learnableMoves")

        }
        // Tutor Moves
        if (tutorMovesLearnable) {
            println("Adding Tutor moves")
            learnableMoves.addAll(pokemon.form.moves.tutorMoves)
            println("Learnable moves list after tutor moves: $learnableMoves")
        }
        // Egg Moves
        if (eggMovesLearnable) {
            println("Adding Egg moves")
            learnableMoves.addAll(pokemon.form.moves.eggMoves)
            println("Learnable moves list after egg moves: $learnableMoves")
            // Check if the move is in the egg moves any pre-evolution
            var preEvolution = pokemon.form.preEvolution
            for (i in 0 until 4) {
                if (preEvolution?.species == null) {
                    break
                }
                learnableMoves.addAll(preEvolution.form.moves.eggMoves)
                preEvolution = preEvolution.form.preEvolution
            }
            println("Learnable moves list after pre-evolution: $learnableMoves")
        }
        // Level Moves
        if (levelMovesLearnable) {
            println("Adding Level moves")
            learnableMoves.addAll(pokemon.form.moves.getLevelUpMovesUpTo(Cobblemon.config.maxPokemonLevel))
            println("Learnable moves list after level moves: $learnableMoves")
        }

        // Remove duplicates
        println("Learnable moves list before distinct: $learnableMoves")
        learnableMoves.distinct()
        println("Learnable moves list after distinct: $learnableMoves")

        // Check if the move is in the learnable moves list
        return learnableMoves.contains(moveToTeach)
    }

    // Wrapper for canPokemonLearnMove with only pokemon as input
    private fun canPokemonLearnMove(pokemon: Pokemon): Boolean {
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

        val moveName = this.moveName
        val move = Moves.getByNameOrDummy(moveName)
        return canPokemonLearnMove(pokemon, move)
    }

    // ------------------------------------------------------------------
    // Hover Text
    // ------------------------------------------------------------------

    override fun appendHoverText(
        itemStack: ItemStack,
        tooltipContext: TooltipContext,
        list: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        // Normal hover text

        // get move from moveName
        val moveName = this.moveName
        val move = Moves.getByNameOrDummy(moveName)
//        move.accuracy
//        move.pp
//        move.power
//        move.damageCategory

        // Description
        val moveDescription = move.description
        // Make the description
        val moveDescriptionColourHexString = "CECACA"
        val moveDescriptionColour = Color.decode(moveDescriptionColourHexString)
        val moveDescriptionComponent = moveDescription.withColor(moveDescriptionColour.rgb)
        list.add(moveDescription)

        if (Screen.hasShiftDown()) {
            // Move Type
            val moveElementalType = move.elementalType
            val moveTypeName = moveElementalType.displayName
            val moveColour = moveElementalType.hue // Int given by the hue of the type
            val moveTypeComponent = moveTypeName.withColor(moveColour)
            val langTypeText = ("Move Type: ").text()
            list.add(langTypeText.append(moveTypeComponent))
            // Line break
            list.add("".text())
        }
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag)
    }
}



