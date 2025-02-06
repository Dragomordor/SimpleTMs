package dragomordor.simpletms.item.custom

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.moves.*
import com.cobblemon.mod.common.api.moves.categories.DamageCategories
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.pokemon.Pokemon
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.SimpleTMsItems
import dragomordor.simpletms.item.SimpleTMsItem
import dragomordor.simpletms.item.api.PokemonSelectingItemNonBattle
import dragomordor.simpletms.util.*
import net.minecraft.client.gui.screens.Screen
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
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.TooltipFlag
import java.awt.Color

class MoveLearnItem(
    private val moveName: String,
    private val isTR: Boolean,
    settings: Properties
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

        // (1) Get the move properties
        val moveName = this.moveName
        val isTR = this.isTR
        val moveToTeach = Moves.getByName(moveName)


        // (1) Check if the move is a valid move
        if (moveToTeach == null) {
            // Move is not valid, so do nothing
            // Display the failure message
            player.displayClientMessage(FailureMessage.getFailureMessage(), true)
            return InteractionResultHolder.fail(stack)
        }

        val moveTranslatedName = moveToTeach.displayName
        val TMusuable = SimpleTMs.config.tmsUsable
        val TRusuable = SimpleTMs.config.trsUsable

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
            val cooldownTicks = SimpleTMs.config.tmCoolDownTicks
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

    private fun canPokemonLearnMove(pokemon: Pokemon, moveToTeach: MoveTemplate): Boolean {

        // (3.1) Get current move set and benched moves of the pokemon
        val currentMoves = pokemon.moveSet
        val benchedMoves = pokemon.benchedMoves
        val pokemonTranslateName = pokemon.species.translatedName
        val moveTranslatedName = moveToTeach.displayName

        val TMMovesLearnable = SimpleTMs.config.tmMovesLearnable
        val eggMovesLearnable = SimpleTMs.config.eggMovesLearnable
        val tutorMovesLearnable = SimpleTMs.config.tutorMovesLearnable
        val levelMovesLearnable = SimpleTMs.config.levelMovesLearnable
        val anyMoveLearnable = SimpleTMs.config.anyMovesLearnable
        val excludedMoveNames = SimpleTMsItems.ALL_MOVES_EXCLUDED_FROM_TMTR_LEARNING

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
        val learnableMoves = mutableListOf<MoveTemplate>()

        // TM Moves
        if (TMMovesLearnable) {
            learnableMoves.addAll(pokemon.form.moves.tmMoves)

        }
        // Tutor Moves
        if (tutorMovesLearnable) {
            learnableMoves.addAll(pokemon.form.moves.tutorMoves)
        }
        // Egg Moves
        if (eggMovesLearnable) {
            learnableMoves.addAll(pokemon.form.moves.eggMoves)
            // Check if the move is in the egg moves any pre-evolution
            var preEvolution = pokemon.form.preEvolution
            for (i in 0 until 4) {
                if (preEvolution?.species == null) {
                    break
                }
                learnableMoves.addAll(preEvolution.form.moves.eggMoves)
                preEvolution = preEvolution.form.preEvolution
            }
        }
        // Level Moves
        if (levelMovesLearnable) {
            learnableMoves.addAll(pokemon.form.moves.getLevelUpMovesUpTo(Cobblemon.config.maxPokemonLevel))
        }

        // Remove duplicates
        learnableMoves.distinct()

        // Remove excluded moves
        learnableMoves.removeIf { excludedMoveNames.contains(it.name) }

        // Check if the move is in the learnable moves list
        if (!learnableMoves.contains(moveToTeach)) {
            FailureMessage.setFailureMessage(fromLang(SimpleTMs.MOD_ID,"error.not_learnable.not_in_learnable_moves", pokemonTranslateName, moveTranslatedName))
            return false
        } else {
            return true
        }
    }

    // Wrapper for canPokemonLearnMove with only pokemon as input
    private fun canPokemonLearnMove(pokemon: Pokemon): Boolean {
        val isTR = this.isTR

        // First check if config allows TMs and TRs
        if (!SimpleTMs.config.tmsUsable && !isTR) {
            FailureMessage.setFailureMessage(fromLang(SimpleTMs.MOD_ID,"error.not_usable.tms_disabled"))
            return false
        }
        if (!SimpleTMs.config.trsUsable && isTR) {
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
        val baseGreyColor = Color.LIGHT_GRAY

        // Move properties
        val moveName = this.moveName
        val move = Moves.getByNameOrDummy(moveName)
        val moveTranslatedName = move.displayName
        val moveType = move.elementalType
        val moveAccuracy = move.accuracy
        val movebasePP = move.pp
        val movemaxPP = move.maxPp
        val movePower = move.power
        val moveCritRatio = move.critRatio
        val moveDamageCategory = move.damageCategory
        val moveColour = moveType.hue // Int given by the hue of the type

        // Ranges for power, accuracy and crit for colour scales
        val powerRange = 0.0 to 200.0
        val accuracyRange = 0.0 to 100.0
        val critRange = 1.0 to 4.0
        val ppRange = 0.0 to 48.0

        // Colour scales
        val powerColors = Color.WHITE to Color.ORANGE
        val accuracyColors = Color.RED to Color.GREEN
        val ppColors = Color.WHITE to Color.ORANGE
        val critColors = Color.WHITE to Color.ORANGE

        // Exceptions:
        // Power = 0 -> Show 'N/A' instead of 0
        // Accuracy = -1 -> Show 'N/A' instead of -1
        // N/A should be in blue

        // Get the colour for power, accuracy and crit
        val movePowerColor = interpolateColor(movePower, powerRange.first, powerRange.second, powerColors.first, powerColors.second)
        val moveAccuracyColor = interpolateColor(moveAccuracy, accuracyRange.first, accuracyRange.second, accuracyColors.first, accuracyColors.second)
        val movePPColor = interpolateColor(movemaxPP.toDouble(), ppRange.first, ppRange.second, ppColors.first, ppColors.second)
        val moveCritColor = interpolateColor(moveCritRatio, critRange.first, critRange.second, critColors.first, critColors.second)

//        // Get colour for each way to learn the move
//        val levelUpColour = Color.YELLOW
//        val tmColour = Color.GREEN
//        val tutorColour = Color.CYAN
//        val eggColour = Color.MAGENTA

        // Normal hover text for item description
        if (!Screen.hasShiftDown()) {
            // item description
            // val itemDescription = ("Teaches the move ").text().withColor(baseGreyColor.rgb).append(moveTranslatedName.withColor(moveColour)).append(" to any Pokémon able to learn it.".text().withColor(baseGreyColor.rgb))
            val itemDescription = fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.description_start").withColor(baseGreyColor.rgb).append(moveTranslatedName.withColor(moveColour)).append(fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.description_end").withColor(baseGreyColor.rgb))
            list.add(itemDescription)

            // Hold shift for move info text
            // val holdShiftText = ("Hold".text().withColor(baseGreyColor.rgb)).append(" §oSHIFT§r ".text().withColor(Color.ORANGE.rgb)).append("for move info.".text().withColor(baseGreyColor.rgb))
            val holdShiftText = fromLang(SimpleTMs.MOD_ID, "item.tooltip.move_learn_item.hold_start").withColor(baseGreyColor.rgb).append(
                fromLang(SimpleTMs.MOD_ID, "item.tooltip.move_learn_item.hold_shift").withColor(Color.ORANGE.rgb)).append(fromLang(SimpleTMs.MOD_ID, "item.tooltip.move_learn_item.hold_shift_info").withColor(baseGreyColor.rgb))
            list.add(holdShiftText)
//
//            if (SimpleTMs.config.showPokemonThatCanLearnMove) {
//                // Hold alt for pokemon that can learn the move
//                //val holdAltText = ("Hold".text().withColor(baseGreyColor.rgb)).append(" §oALT§r ".text().withColor(Color.CYAN.rgb)).append("to see Pokémon that can learn this move.".text().withColor(baseGreyColor.rgb))
//                val holdAltText = fromLang(SimpleTMs.MOD_ID, "item.tooltip.move_learn_item.hold_start").withColor(baseGreyColor.rgb).append(
//                    fromLang(SimpleTMs.MOD_ID, "item.tooltip.move_learn_item.hold_alt").withColor(Color.CYAN.rgb)).append(fromLang(SimpleTMs.MOD_ID, "item.tooltip.move_learn_item.hold_alt_info").withColor(baseGreyColor.rgb))
//                list.add(holdAltText)
//            }
        }

        // Hold shift for move info text
        if (Screen.hasShiftDown()) {
            // Show move info
            // Move Description
            val moveDescription = move.description
            // Make the description
            val moveDescriptionComponent = moveDescription.withColor(baseGreyColor.rgb)
            list.add(moveDescriptionComponent)

            // Move Type
            // val moveTypeBase = ("Type: ").text().withColor(baseGreyColor.rgb)
            val moveTypeBase = fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.move_type").withColor(baseGreyColor.rgb)
            val moveTypeComponent = moveType.displayName.withColor(moveColour)
            list.add(moveTypeBase.append(moveTypeComponent))

            // Move Category
            // Colours for categories
            // Red for physical
            val physicalColour = Color.RED
            // Yellow for special
            val specialColour = Color.YELLOW
            // Green for status
            val statusColour = Color.GREEN
            // Get the colour for the category
            val moveCategoryColour = when (moveDamageCategory) { // Get the colour for the category
                DamageCategories.PHYSICAL -> physicalColour
                DamageCategories.SPECIAL -> specialColour
                DamageCategories.STATUS -> statusColour
                else -> {
                    baseGreyColor
                }
            }
            // Category Text
            //val moveCategoryBase = ("Category: ").text().withColor(baseGreyColor.rgb)
            val moveCategoryBase = fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.move_category").withColor(baseGreyColor.rgb)
            val moveCategoryComponent = moveDamageCategory.displayName.copy().withColor(moveCategoryColour.rgb)
            list.add(moveCategoryBase.append(moveCategoryComponent))

            // Move Power
            // val movePowerBase = ("Power: ").text().withColor(baseGreyColor.rgb)
            val movePowerBase = fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.move_power").withColor(baseGreyColor.rgb)
            var movePowerComponent = movePower.toString().text().withColor(movePowerColor.rgb)
            if (movePower == 0.0) { // If power is 0, show N/A
                movePowerComponent = "N/A".text().withColor(Color.BLUE.rgb)
            }
            list.add(movePowerBase.append(movePowerComponent))

            // Move Accuracy
            // val moveAccuracyBase = ("Accuracy: ").text().withColor(baseGreyColor.rgb)
            val moveAccuracyBase = fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.move_accuracy").withColor(baseGreyColor.rgb)
            var moveAccuracyComponent = moveAccuracy.toString().text().withColor(moveAccuracyColor.rgb)
            if (moveAccuracy == -1.0) { // If accuracy is -1, show N/A
                moveAccuracyComponent = "N/A".text().withColor(Color.BLUE.rgb)
            }
            list.add(moveAccuracyBase.append(moveAccuracyComponent))

            // Move PP
            // Show base and max PP:
            // Base PP: 10, Max PP: 12

            // val movePPBase = ("Base PP: ").text().withColor(baseGreyColor.rgb)
            val movePPBase = fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.move_pp").withColor(baseGreyColor.rgb)
            val movePPComponent = movebasePP.toString().text().withColor(movePPColor.rgb)
            // val movePPMax = (", Max PP: ").text().withColor(baseGreyColor.rgb)
            val movePPMax = fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.move_pp_max").withColor(baseGreyColor.rgb)
            val movePPMaxComponent = movemaxPP.toString().text().withColor(movePPColor.rgb)
            list.add(movePPBase.append(movePPComponent).append(movePPMax).append(movePPMaxComponent))

            // Move Crit Ratio
            //val moveCritBase = ("Crit Ratio: ").text().withColor(baseGreyColor.rgb)
            val moveCritBase = fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.move_crit_ratio").withColor(baseGreyColor.rgb)
            val moveCritComponent = moveCritRatio.toString().text().withColor(moveCritColor.rgb)
            list.add(moveCritBase.append(moveCritComponent))
        }


//        // Hold alt for pokemon that can learn the move
//        if (Screen.hasAltDown() && SimpleTMs.config.showPokemonThatCanLearnMove) {
//
//            // if all moves are learnable, don't show individual names, just say all can be learned
//            if (SimpleTMs.config.anyMovesLearnable) {
//                // list.add("item.move_learn_item.all_pokemon_can_learn".text().withColor(Color.ORANGE.rgb))
//                list.add(fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.all_pokemon_can_learn").withColor(Color.ORANGE.rgb))
//                // Stop here
//                return
//            }
//
//            // Page controls to make the list scrollable
//            val namesPerPage = 7 // Number of names to display per page
//
//            //val pokemonList = getAllPokemonThatCanLearnThisMove(moveName)
//            // val allPokemon = PokemonSpecies.species
//            val pokemonList = MoveAssociations.getAssociationsForMove(moveName)
//
//            if (pokemonList.isNotEmpty()) {
//                // sort the list by pokemon name
//
//
//                // TODO: Custom sort from config
//                if (SimpleTMs.config.pokemonSortOrder=="ALPHABETICAL_ASC") {
//                    pokemonList.sortBy { it.first.translatedName.toString() }
//                }
//                if (SimpleTMs.config.pokemonSortOrder=="ALPHABETICAL_DESC") {
//                    pokemonList.sortByDescending { it.first.translatedName.toString() }
//                }
//                if (SimpleTMs.config.pokemonSortOrder=="POKEMON_TYPE_ASC") {
//                    pokemonList.sortBy { it.first.primaryType.name }
//                }
//                if (SimpleTMs.config.pokemonSortOrder=="POKEMON_TYPE_DESC") {
//                    pokemonList.sortByDescending { it.first.primaryType.name }
//                }
//                else{
//                    // Defaults to alphabetical ascending
//                    pokemonList.sortBy { it.first.translatedName.toString() }
//                }
//
//                val numberOfPokemonThatCanLearnMove = pokemonList.size
//
//                if (numberOfPokemonThatCanLearnMove == PokemonSpecies.species.size) {
//                    list.add("item.move_learn_item.all_pokemon_can_learn".text().withColor(Color.ORANGE.rgb))
//                    return
//                }
//
//                Pages.TOTAL_PAGES = (numberOfPokemonThatCanLearnMove + namesPerPage - 1) / namesPerPage // Total number of pages
//                Pages.CURRENT_PAGE = Pages.CURRENT_PAGE.coerceIn(0, Pages.TOTAL_PAGES - 1) // Clamp currentPage within bounds
//                val start = Pages.CURRENT_PAGE * namesPerPage
//                val end = (start + namesPerPage).coerceAtMost(numberOfPokemonThatCanLearnMove)
//                val visiblePokemon = pokemonList.subList(start, end)
//
//                val numberComponent = numberOfPokemonThatCanLearnMove.toString().text().withColor(Color.ORANGE.rgb)
//                //val canBeLearnedByBase = numberComponent.append((" Pokémon can learn this move, they are: ").text().withColor(baseGreyColor.rgb))
//                val canBeLearnedByBase = numberComponent.append(fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.can_learn_base").withColor(baseGreyColor.rgb))
//                // val pokemonWayBase = ("Pokemon  - Ways they can learn it").text().withColor(Color.GRAY.rgb)
//                val pokemonWayBase = fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.pokemon_ways").withColor(Color.GRAY.rgb)
//                list.add(canBeLearnedByBase)
//                list.add(pokemonWayBase)
//
//                for (pokemon in visiblePokemon) {
//                    val pokemonSpecies = pokemon.first
//                    val waysToLearn = pokemon.second
//                    val pokemonName = pokemonSpecies.translatedName
//
//                    // Add the pokemon name and the ways they can learn to tooltip line
//                    val pokemonColour = pokemonSpecies.primaryType.hue
//                    val pokemonNameComponent = pokemonName.withColor(pokemonColour)
//                    // val levelUpComponent = ("Level Up").text().withColor(levelUpColour.rgb)
//                    val levelUpComponent = fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.level_up").withColor(levelUpColour.rgb)
//                    // val tmComponent = ("TM").text().withColor(tmColour.rgb)
//                    val tmComponent = fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.tm").withColor(tmColour.rgb)
//                    // val tutorComponent = ("Tutor").text().withColor(tutorColour.rgb)
//                    val tutorComponent = fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.tutor").withColor(tutorColour.rgb)
//                    // val eggComponent = ("Egg").text().withColor(eggColour.rgb)
//                    val eggComponent = fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.egg").withColor(eggColour.rgb)
//                    val waysComponent = listOf(
//                        "Level Up" to levelUpComponent,
//                        "TM" to tmComponent,
//                        "Tutor" to tutorComponent,
//                        "Egg" to eggComponent
//                    ).filter { waysToLearn.contains(it.first) }
//                        .map { it.second }
//                        .reduceOrNull { acc, component -> acc.append(", ").append(component) }
//                    val pokemonComponent = pokemonNameComponent.append(" - ").append(waysComponent ?: "".text())
//                    list.add(pokemonComponent)
//                }
//
//                if ((Pages.TOTAL_PAGES > 1) && (Pages.CURRENT_PAGE < Pages.TOTAL_PAGES - 1)) {
//                    list.add("...".text().withColor(baseGreyColor.rgb))
//                    // list.add("Use Mouse Scroll Wheel to navigate.".text().withColor(Color.RED.rgb))
//                    list.add(fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.scroll_wheel").withColor(Color.RED.rgb))
//                }
//
//                if (Pages.CURRENT_PAGE == Pages.TOTAL_PAGES - 1) {
//                    // list.add("End of list".text().withColor(Color.RED.rgb))
//                    list.add(fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.end_of_list").withColor(Color.RED.rgb))
//                }
//            } else {
//                // If no pokemon can learn the move, show a message
//                // list.add("No Pokémon can learn this move.".text().withColor(Color.RED.rgb))
//                list.add(fromLang(SimpleTMs.MOD_ID, "item.move_learn_item.no_pokemon").withColor(Color.RED.rgb))
//            }
//        }

        // Super call
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag)
    }











}



