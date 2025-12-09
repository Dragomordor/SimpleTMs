package dragomordor.simpletms.item.custom

import com.cobblemon.mod.common.api.callback.PartySelectCallbacks
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.pokemon.moves.LearnsetQuery
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.getBattleState
import com.cobblemon.mod.common.util.isLookingAt
import com.cobblemon.mod.common.util.party
import dev.architectury.registry.menu.MenuRegistry
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.SimpleTMsItems
import dragomordor.simpletms.api.MoveCaseHelper
import dragomordor.simpletms.api.MoveCaseStorage
import dragomordor.simpletms.item.SimpleTMsItem
import dragomordor.simpletms.ui.MoveCaseMenu
import dragomordor.simpletms.ui.PokemonFilterData
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB

/**
 * The TM Case or TR Case item.
 *
 * - Normal right-click: Opens the case GUI (no filter)
 * - Shift + right-click: Opens party select to choose a Pokémon filter
 * - Right-click on Pokémon entity (owned): Opens case GUI with that Pokémon as filter
 */
class MoveCaseItem(val isTR: Boolean, settings: Properties) : SimpleTMsItem(settings) {

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(usedHand)

        if (level is ServerLevel && player is ServerPlayer) {
            // Check if player is in battle
            player.getBattleState()?.let {
                return InteractionResultHolder.fail(stack)
            }

            // Check if looking at a Pokémon entity
            val entity = player.level()
                .getEntities(player, AABB.ofSize(player.position(), 16.0, 16.0, 16.0))
                .filter { player.isLookingAt(it, stepDistance = 0.1F) }
                .minByOrNull { it.distanceTo(player) } as? PokemonEntity?

            if (entity != null && entity.ownerUUID == player.uuid) {
                // Looking at owned Pokémon - open case with that Pokémon as filter
                openMenu(player, entity.pokemon)
                return InteractionResultHolder.success(stack)
            }

            if (player.isShiftKeyDown) {
                // Shift-click on air: Open party select GUI
                openPartySelect(player, stack)
                return InteractionResultHolder.success(stack)
            } else {
                // Normal click on air: Open case GUI without Pokémon filter
                openMenu(player, null)
                return InteractionResultHolder.success(stack)
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
    }

    /**
     * Handle right-clicking on a Pokémon entity directly (backup for interactLivingEntity)
     */
    override fun interactLivingEntity(
        stack: ItemStack,
        player: Player,
        entity: LivingEntity,
        usedHand: InteractionHand
    ): InteractionResult {
        if (entity is PokemonEntity && player is ServerPlayer) {
            // Check if player is in battle
            player.getBattleState()?.let {
                return InteractionResult.FAIL
            }

            // Only allow interaction with owned Pokémon
            if (entity.ownerUUID == player.uuid) {
                openMenu(player, entity.pokemon)
                return InteractionResult.SUCCESS
            }
        }
        return InteractionResult.PASS
    }

    /**
     * Open the party select GUI to choose a Pokémon for filtering
     */
    private fun openPartySelect(player: ServerPlayer, stack: ItemStack) {
        val party = player.party().toList()
        if (party.isEmpty()) {
            // No Pokémon in party, just open the case without filter
            openMenu(player, null)
            return
        }

        PartySelectCallbacks.createFromPokemon(
            player = player,
            pokemon = party,
            canSelect = { true }, // All Pokémon can be selected for filtering
            handler = { pokemon ->
                // When a Pokémon is selected, open the case with that Pokémon as filter
                openMenu(player, pokemon)
            }
        )
    }

    // ========================================
    // Menu opening
    // ========================================

    private fun openMenu(player: ServerPlayer, pokemon: Pokemon?, isFreshSelection: Boolean = false) {
        val storage = MoveCaseStorage.get(player)
        val storedMoves = storage.getStoredQuantities(player.uuid, isTR)

        // Get damage values for TMs
        val storedDamage = if (!isTR) {
            storedMoves.keys.associateWith { moveName -> storage.getMoveDamage(player.uuid, moveName, false) }
        } else {
            emptyMap()
        }

        // Determine which Pokémon to use for filtering
        val filterPokemon: Pokemon? = pokemon
        val savedPokemonInfo: MoveCaseStorage.SelectedPokemonInfo?

        if (pokemon != null) {
            // New Pokémon selected - save it for future use
            val learnableMoves = getLearnableMoves(pokemon, isTR)
            val newInfo = MoveCaseStorage.SelectedPokemonInfo(
                pokemon.species.resourceIdentifier.toString(),
                pokemon.form.name,
                pokemon.species.translatedName.string,
                learnableMoves
            )
            storage.setSelectedPokemon(player.uuid, isTR, newInfo)
            savedPokemonInfo = newInfo
        } else {
            // No new Pokémon - load previously saved one (if any)
            savedPokemonInfo = storage.getSelectedPokemon(player.uuid, isTR)
        }

        val menuProvider = object : MenuProvider {
            override fun getDisplayName(): Component {
                return if (isTR) {
                    Component.translatable("container.simpletms.tr_case")
                } else {
                    Component.translatable("container.simpletms.tm_case")
                }
            }

            override fun createMenu(
                containerId: Int,
                playerInventory: Inventory,
                player: Player
            ): AbstractContainerMenu {
                return MoveCaseMenu(
                    containerId,
                    playerInventory,
                    isTR,
                    storedMoves,
                    filterPokemon,
                    // Pass saved info for server-side menu creation when no live Pokémon
                    if (filterPokemon == null && savedPokemonInfo != null) {
                        PokemonFilterData(
                            savedPokemonInfo.speciesId,
                            savedPokemonInfo.formName,
                            savedPokemonInfo.displayName,
                            savedPokemonInfo.learnableMoves
                        )
                    } else null,
                    autoEnableFilter = filterPokemon != null || isFreshSelection,
                    initialStoredDamage = storedDamage
                )
            }
        }

        MenuRegistry.openExtendedMenu(player, menuProvider) { buf ->
            buf.writeBoolean(isTR)

            // Write stored moves with quantities and damage
            buf.writeVarInt(storedMoves.size)
            for ((moveName, quantity) in storedMoves) {
                buf.writeUtf(moveName)
                buf.writeVarInt(quantity)
                // Write damage value for TMs (TRs don't have damage)
                if (!isTR) {
                    buf.writeVarInt(storedDamage[moveName] ?: 0)
                }
            }

            // Write Pokémon filter data (either from new selection or saved)
            val hasFilterData = filterPokemon != null || savedPokemonInfo != null
            buf.writeBoolean(hasFilterData)

            if (hasFilterData) {
                if (filterPokemon != null) {
                    // Use live Pokémon data
                    buf.writeUtf(filterPokemon.species.resourceIdentifier.toString())
                    buf.writeUtf(filterPokemon.form.name)
                    buf.writeUtf(filterPokemon.species.translatedName.string)

                    val learnableMoves = getLearnableMoves(filterPokemon, isTR)
                    buf.writeVarInt(learnableMoves.size)
                    for (moveName in learnableMoves) {
                        buf.writeUtf(moveName)
                    }
                } else if (savedPokemonInfo != null) {
                    // Use saved Pokémon data
                    buf.writeUtf(savedPokemonInfo.speciesId)
                    buf.writeUtf(savedPokemonInfo.formName)
                    buf.writeUtf(savedPokemonInfo.displayName)

                    buf.writeVarInt(savedPokemonInfo.learnableMoves.size)
                    for (moveName in savedPokemonInfo.learnableMoves) {
                        buf.writeUtf(moveName)
                    }
                }

                // Write whether this was a fresh selection (auto-enable filter) or loaded (don't auto-enable)
                buf.writeBoolean(filterPokemon != null)
            }
        }
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)

        val totalMoves = MoveCaseHelper.getSlotCount(isTR)
        val storedCount = getStoredCountForTooltip()

        if (storedCount >= 0) {
            val percentage = if (totalMoves > 0) (storedCount * 100) / totalMoves else 0

            val color = when {
                percentage >= 100 -> ChatFormatting.GOLD
                percentage >= 75 -> ChatFormatting.GREEN
                percentage >= 50 -> ChatFormatting.YELLOW
                percentage >= 25 -> ChatFormatting.RED
                else -> ChatFormatting.DARK_RED
            }

            tooltipComponents.add(
                Component.translatable("tooltip.simpletms.case.storage", storedCount, totalMoves, percentage)
                    .withStyle(color)
            )

            if (percentage >= 100) {
                tooltipComponents.add(
                    Component.translatable("tooltip.simpletms.case.complete")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC)
                )
            }
        } else {
            tooltipComponents.add(
                Component.translatable("tooltip.simpletms.case.capacity", totalMoves)
                    .withStyle(ChatFormatting.GRAY)
            )
        }

        // Add shift-click hint
//        tooltipComponents.add(
//            Component.translatable("tooltip.simpletms.case.shift_hint")
//                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
//        )
    }

    private fun getStoredCountForTooltip(): Int {
        try {
            val minecraft = net.minecraft.client.Minecraft.getInstance()
            val player = minecraft.player ?: return -1

            val server = minecraft.singleplayerServer
            if (server != null) {
                val serverPlayer = server.playerList.getPlayer(player.uuid)
                if (serverPlayer != null) {
                    val storage = MoveCaseStorage.get(serverPlayer)
                    return storage.getStoredMoves(player.uuid, isTR).size
                }
            }

            return -1
        } catch (e: Exception) {
            return -1
        }
    }

    companion object {
        /**
         * Get all moves that a Pokémon can learn based on config settings.
         * This mirrors the logic from MoveLearnItem.canPokemonLearnMove
         */
        fun getLearnableMoves(pokemon: Pokemon, isTR: Boolean): Set<String> {
            val cfg = SimpleTMs.config
            val learnableMoves = mutableSetOf<String>()

            // Check for any-move override
            if ((cfg.anyMovesLearnableTMs && !isTR) || (cfg.anyMovesLearnableTRs && isTR)) {
                // All moves are learnable
                return MoveCaseHelper.getSortedMoveNames(isTR).toSet()
            }

            val excludedMoves = SimpleTMsItems.ALL_MOVES_EXCLUDED_FROM_TMTR_LEARNING

            // Helper to check a learnset
            fun addFromLearnset(learnset: com.cobblemon.mod.common.api.pokemon.moves.Learnset) {
                for (moveName in MoveCaseHelper.getSortedMoveNames(isTR)) {
                    if (excludedMoves.contains(moveName)) continue
                    if (learnableMoves.contains(moveName)) continue

                    val move = Moves.getByName(moveName) ?: continue

                    // Check each category based on config
                    if (cfg.tmMovesLearnable && LearnsetQuery.TM_MOVE.canLearn(move, learnset)) {
                        learnableMoves.add(moveName)
                        continue
                    }
                    if (cfg.tutorMovesLearnable && LearnsetQuery.TUTOR_MOVES.canLearn(move, learnset)) {
                        learnableMoves.add(moveName)
                        continue
                    }
                    if (cfg.eggMovesLearnable && LearnsetQuery.EGG_MOVE.canLearn(move, learnset)) {
                        learnableMoves.add(moveName)
                        continue
                    }
                    if (cfg.levelMovesLearnable && LearnsetQuery.ANY_LEVEL.canLearn(move, learnset)) {
                        learnableMoves.add(moveName)
                        continue
                    }
                    if (cfg.legacyMovesLearnable && LearnsetQuery.LEGACY_MOVES.canLearn(move, learnset)) {
                        learnableMoves.add(moveName)
                        continue
                    }
                    if (cfg.specialMovesLearnable && LearnsetQuery.SPECIAL_MOVES.canLearn(move, learnset)) {
                        learnableMoves.add(moveName)
                        continue
                    }
                }
            }

            // Check current form
            addFromLearnset(pokemon.form.moves)

            // Check other forms of the same species
            for (form in pokemon.species.forms) {
                if (form == pokemon.form) continue
                addFromLearnset(form.moves)
            }

            // Type-based fallback
            if (cfg.primaryTypeMovesLearnable || cfg.secondaryTypeMovesLearnable) {
                val primary = pokemon.primaryType
                val secondary = pokemon.secondaryType

                for (moveName in MoveCaseHelper.getSortedMoveNames(isTR)) {
                    if (excludedMoves.contains(moveName)) continue
                    if (learnableMoves.contains(moveName)) continue

                    val move = Moves.getByName(moveName) ?: continue

                    if (cfg.primaryTypeMovesLearnable && move.elementalType == primary) {
                        learnableMoves.add(moveName)
                    }
                    if (cfg.secondaryTypeMovesLearnable && move.elementalType == secondary) {
                        learnableMoves.add(moveName)
                    }
                }
            }

            return learnableMoves
        }
    }

    /**
     * Open the menu with a fresh selection flag.
     * Called from network handler after party selection.
     */
    fun openMenuWithFreshSelection(player: ServerPlayer) {
        openMenu(player, null, isFreshSelection = true)
    }
}