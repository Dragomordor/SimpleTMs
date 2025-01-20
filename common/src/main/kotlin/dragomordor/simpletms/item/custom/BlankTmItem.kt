package dragomordor.simpletms.item.custom

import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.giveOrDropItemStack
import dragomordor.simpletms.SimpleTMs
import dragomordor.simpletms.SimpleTMsItems.getTMorTRItemFromMove
import dragomordor.simpletms.item.SimpleTMsItem
import dragomordor.simpletms.item.api.PokemonAndMoveSelectingItemNonBattle
import dragomordor.simpletms.util.fromLang
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import java.awt.Color

class BlankTmItem(val isTR: Boolean, settings: Properties) : SimpleTMsItem(settings), PokemonAndMoveSelectingItemNonBattle {

    // ------------------------------------------------------------------
    // Override methods from PokemonAndMoveSelectingItemNonBattle
    // ------------------------------------------------------------------

    // Blank TMs can only be used on Pokémon moves, not on the Pokémon itself
    override fun canUseOnPokemon(pokemon: Pokemon) = pokemon.moveSet.any(::canUseOnMove)

    // Blank TMs can be used on any move the pokemon knows (except for excluded moves and if config disables it)
    override fun canUseOnMove(move: Move) = canUseBlankOnMove(isTR);

    override fun applyToPokemon(player: ServerPlayer, stack: ItemStack, pokemon: Pokemon, move: Move) {
        val isTR = this.isTR

        // (1) Check if config is allowed
        if (!canUseBlank(isTR)) {
            // Display the failure message
            player.displayClientMessage(FailureMessage.getFailureMessage(), true)
            return
        }

        // (2) Check item cooldown if applicable
        val cooldownTicks = SimpleTMs.config.blankTMCooldownTicks

        if (player.cooldowns.isOnCooldown(this)) {
            // Convert cooldown ticks to hours, minutes, and seconds
            val cooldownHH = Math.floorDiv(cooldownTicks, 72000)
            val cooldownMM = Math.floorDiv(cooldownTicks- (cooldownHH * 72000), 1200)
            val cooldownSS = Math.floorDiv(cooldownTicks - (cooldownHH * 72000) - (cooldownMM * 1200), 20)
            val cooldownString = "$cooldownHH:$cooldownMM:$cooldownSS"
            // Display the failure message
            dragomordor.simpletms.util.FailureMessage.setFailureMessage(fromLang(SimpleTMs.MOD_ID,"error.not_learnable.on_cooldown", stack.displayName ,cooldownString))
            player.displayClientMessage(dragomordor.simpletms.util.FailureMessage.getFailureMessage(), true)
            return
        }

        // (3) Give the item to the player
        val newMoveLearnItem = getTMorTRItemFromMove(move, isTR)
            // If the item is not found, display the failure message
        if (newMoveLearnItem == null) {
            // Display the failure message
            player.displayClientMessage(FailureMessage.getFailureMessage(), true)
            return
        }

        player.giveOrDropItemStack(ItemStack(newMoveLearnItem))

        // Success sound
        player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1.0F, 1.0F)
        // Damage the stack if player is not in creative mode
        if (!player.isCreative) {
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND)
        }
        // Put item on cooldown if applicable
        if (!player.isCreative && !isTR && stack.count > 0 && cooldownTicks > 0) {
            player.cooldowns.addCooldown(this, cooldownTicks)
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

    private fun canUseBlankOnMove(isTR: Boolean): Boolean {
        val canUseBlank = canUseBlank(isTR)
        // TODO: Add excluded moves here?
        return canUseBlank
    }

    private fun canUseBlank(isTR: Boolean): Boolean {
        if (!SimpleTMs.config.blankTRsUsable && isTR) {
            FailureMessage.setFailureMessage(fromLang(SimpleTMs.MOD_ID, "error.not_usable.blank_trs_disabled"))
            return false
        }
        if (!SimpleTMs.config.blankTMsUsable && !isTR) {
            FailureMessage.setFailureMessage(fromLang(SimpleTMs.MOD_ID, "error.not_usable.blank_tms_disabled"))
            return false
        }
        return true
    }

    class FailureMessage() {
        companion object {
            lateinit var message: Component

            fun getFailureMessage(): Component {
                return message
            }

            fun setFailureMessage(message: Component) {
                this.message = message
            }
        }
    }

    // ------------------------------------------------------------------
    // Hover text
    // ------------------------------------------------------------------

    override fun appendHoverText(
        itemStack: ItemStack,
        tooltipContext: TooltipContext,
        list: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {

        val baseGreyColor = Color.decode("#C6BDBD")
        val isTR = this.isTR
        val itemType = if (isTR) "TR" else "TM"

        // Normal hover text
        // val blankItemDescription = ("An item that turns a Pokémon's known move into a $ to teach others.").text().withColor(baseGreyColor.rgb)
        val blankItemDescription = ("Copies a Pokémon's move into a $itemType for teaching.").text().withColor(baseGreyColor.rgb)
        list.add(blankItemDescription)

        if (Screen.hasControlDown()) {
            // Show number of uses left
            val usesLeft = (itemStack.maxDamage - itemStack.damageValue).toString().text().withColor(Color.RED.rgb)
            val usesLeftText = ("Uses left: ").text().withColor(baseGreyColor.rgb)
            list.add(usesLeftText.append(usesLeft))
        }

        // Super call
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag)
    }

}